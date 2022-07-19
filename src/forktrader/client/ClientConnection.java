package forktrader.client;

import java.awt.BorderLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import forks.ForkData;
import forktrader.TradeMsg;
import forktrader.types.Order;
import util.IOUtil;
import util.Ico;
import util.Util;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ClientConnection extends JPanel {
	private final ClientView view;
	public final LocalServerModel MODEL = new LocalServerModel(this);	
	public final JTable TABLE = new JTable(MODEL);
	
	private String address = "127.0.0.1";
	private int port = 1231;
	private Socket s;
	private DataOutputStream dos;
	
	public List<Order> MY_ORDERS = new ArrayList<>();
	public List<Order> ORDER_LIST  = new ArrayList<>();
	
	class LocalServerModel extends JFunTableModel<ClientConnection> {
		ClientConnection server;
		public LocalServerModel(ClientConnection server) {
			super();
			this.server = server;
			addColumn("!",   			20,	Boolean.class,	s->isRunning()).showMandatory().editable();
			addColumn("Address", 		-1,	String.class,	s->address).showMandatory().editable();
			addColumn("Port",   		40,	Integer.class,	s->port).showMandatory().editable();
			addColumn("Info",   		30,	Icon.class,		s->isRunning() ? Ico.GREEN : Ico.RED).showMandatory();
			
			onGetRowCount(() -> 1);
			onGetValueAt((r, c) -> colList.get(c).apply(server));
			onisCellEditable((r, c) -> colList.get(c).isEditable());
		}
		
		public void setValueAt(Object value, int row, int col) {
			if (0 == col) {
				server.toggleServer();
			} else if (1 == col ) {
				address = (String) value;
			} else if (2 == col) {
				port = (int) value;
			} 
			fireTableCellUpdated(row, col);
	    }
		
	}
	
	public ClientConnection(ClientView view) {
		this.view = view;
		setLayout(new BorderLayout());
		add(TABLE.getTableHeader(),BorderLayout.PAGE_START);
		add(TABLE,BorderLayout.CENTER);
		
		MODEL.colList.forEach(c -> c.finalize(TABLE,null));
		
		//if (!isRunning())
			//toggleServer();
	}
	
	
	private final void toggleServer() {
		if (isRunning())
			close();
		else
			new Thread(() -> socketThread()).start();
		SwingUtilities.invokeLater(() -> MODEL.fireTableDataChanged());
	}
	
	
	public void socketThread() {
		s = new Socket();
		try {
			s.connect(new InetSocketAddress(address,port),1000);
			DataInputStream dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
			dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			
			while (true) {
				TradeMsg t = TradeMsg.readType(dis);
				view.LOG.add("Received message: " + t);
				
				switch (t) {
				case ORDER:
					Order o = new Order(dis);
					view.LOG.add("Received an new order");
					synchronized (ORDER_LIST) {
						if (MY_ORDERS.contains(o))
							o.myOrder = true;
						ORDER_LIST.add(o);
					}
					view.refreshOrders();
					break;
				case PAIRS:
					synchronized (view.PAIR_LIST) {
						view.PAIR_LIST.clear();
						int numPairs = dis.readInt();
						for (int i = 0; i < numPairs; i++ ) {
							String sym = IOUtil.readString(dis, 10);
							ForkData.getBySymbol(sym).ifPresent(view.PAIR_LIST::add);
							SwingUtilities.invokeLater(() -> view.PAIR_MODEL.fireTableDataChanged());
						}
					}
					break;
				case DELETE:
					byte[] hash = IOUtil.readBA(dis, 32);
					view.LOG.add("Recevied delete Order: " + Util.getHexString(hash));
					synchronized (ORDER_LIST) {
						ORDER_LIST.removeIf(or ->Arrays.equals(or.hash, hash));
						view.refreshOrders();
					}
					
					break;
					
				default:
					close();
				
				}
				
				
			}
			
			
				
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		close();
	}
	
	public void sendOrder(Order o) {
		try {
			o.myOrder = true;
			MY_ORDERS.add(o);
			o.send(dos);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			close();
		}
	}
	
	public void deleteOrder(byte[] hash) {
		try {
			MY_ORDERS.removeIf(o -> Arrays.equals(o.hash, hash));
			IOUtil.write(dos, TradeMsg.DELETE.ordinal(),hash);
		} catch (IOException e) {
			close();
		}
	}

	
	public void close() {
		view.LOG.add("ClientConnection.close()");
		MY_ORDERS.clear();
		ORDER_LIST.clear();
		
		view.closeConnection();
		Util.closeQuietly(s);
		SwingUtilities.invokeLater(() -> MODEL.fireTableDataChanged());
	}

	private final boolean isRunning() {
		if (null == s)
			return false;
		return s.isBound() && !s.isClosed();
	}


	public void executeOrder(byte[] hash) {
		
		
	}




}

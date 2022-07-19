package forktrader.server;

import java.awt.BorderLayout;
import java.io.IOException;
import java.net.BindException;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import util.Ico;
import util.NetUtil;
import util.Util;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class LocalServer extends JPanel {
	public final static String[] TRADE_PAIRS = {"hdd","xtx","xbtc"};
	public final LocalServerModel MODEL = new LocalServerModel(this);	
	public final JTable TABLE = new JTable(MODEL);
	
	private static final String localAddress = NetUtil.getIPs();
	private static int port = 1231;
	private static ServerSocket  srvSocket;
	
	class LocalServerModel extends JFunTableModel<LocalServer> {
		LocalServer server;
		public LocalServerModel(LocalServer server) {
			super();
			this.server = server;
			addColumn("!",   			20,	Boolean.class,	s->LocalServer.isRunning()).showMandatory().editable();
			addColumn("Server Address", -1,	String.class,	s->LocalServer.localAddress).showMandatory();
			addColumn("Port",   		40,	Integer.class,	s->LocalServer.port).showMandatory().editable();
			addColumn("Info",   		30,	Icon.class,		s-> LocalServer.isRunning() ? Ico.GREEN : Ico.RED).showMandatory();
			
			onGetRowCount(() -> 1);
			onGetValueAt((r, c) -> colList.get(c).apply(server));
			onisCellEditable((r, c) -> colList.get(c).isEditable());
		}
		
		public void setValueAt(Object value, int row, int col) {
			if (0 == col) {
				server.toggleServer();
			} else if (2 == col) {
				port = (int) value;
			} 
			fireTableCellUpdated(row, col);
	    }
		
	}
	
	public LocalServer() {
		setLayout(new BorderLayout());
		add(TABLE.getTableHeader(),BorderLayout.PAGE_START);
		add(TABLE,BorderLayout.CENTER);
		
		MODEL.colList.forEach(c -> c.finalize(TABLE,null));
		
		//if (!isRunning())
			//toggleServer();
	}
	
	
	private final void toggleServer() {
		if (isRunning())
			Util.closeQuietly(srvSocket);
		else
			new Thread(() -> thread()).start();
		SwingUtilities.invokeLater(() -> MODEL.fireTableDataChanged());
	}
	
	private final static boolean isRunning() {
		if (null == srvSocket)
			return false;
		return srvSocket.isBound() && !srvSocket.isClosed();
	}
	
	private final void thread() {
		try {
			srvSocket = new ServerSocket(port);
			SwingUtilities.invokeLater(() -> MODEL.fireTableDataChanged());
			while (isRunning()) {
				Socket clientSocket = srvSocket.accept();
				ExchangeServer.LOG.add("Client Connected: " + clientSocket.getInetAddress());
				new Thread(() -> new Trader(clientSocket)).start();
			}
		} catch (final BindException e) {
			//MainGui.showErrorMsg("Cant Start Socket Server on port " + port);
		} catch (final IOException e) {
			if (srvSocket.isClosed())
				return;
			e.printStackTrace();
		}
	}

}

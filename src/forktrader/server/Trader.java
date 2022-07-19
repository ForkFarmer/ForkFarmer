package forktrader.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.SwingUtilities;

import forktrader.TradeMsg;
import forktrader.types.Order;
import util.IOUtil;
import util.Util;

public class Trader {
	List<Order>	ORDER_LIST = new ArrayList<>();
	boolean isMe;
	Socket s;
	DataOutputStream dos;
	private ExecutorService OS_SVC = Executors.newSingleThreadExecutor();
	
	public Trader(Socket s) {
		this.s = s;
		
		ExchangeServer.addTrader(this);
		
		DataInputStream dis = null;
		try {
			dis = new DataInputStream(new BufferedInputStream(s.getInputStream()));
			dos = new DataOutputStream(new BufferedOutputStream(s.getOutputStream()));
			
			helloTrader();
			
			while(true) {
				TradeMsg t = TradeMsg.readType(dis);
				
				if (TradeMsg.ORDER == t) {
					Order o = new Order(dis);
					
					if (ORDER_LIST.size() > 20) {
						ExchangeServer.LOG.add("Trader " + getAddress() + " hit max orders... ignoring!" );
						return;
					}
					o.owner = this;
					if (ExchangeServer.broadcastOrder(o))
						ORDER_LIST.add(o);
					SwingUtilities.invokeLater(ExchangeServer.TRADER_MODEL::fireTableDataChanged);
					
				} else if (TradeMsg.DELETE == t) {
					byte[] hash = IOUtil.readBA(dis, 32);
					Order.getOrderByHash(ORDER_LIST, hash).ifPresent(o -> ORDER_LIST.remove(o));
					SwingUtilities.invokeLater(ExchangeServer.TRADER_MODEL::fireTableDataChanged);
					ExchangeServer.deleteOrder(this, hash);
				} else {
					throw new IOException("Invalid TradeMsg");
				}
			}
			
		} catch (IOException e) {
			
		}
		
		
		Util.closeQuietly(dis);
		closeConnection();
	}


	private void helloTrader() throws IOException {
		final List<Order> list;
		
		synchronized (ExchangeServer.ORDER_LIST) {
			list = new ArrayList<>(ExchangeServer.ORDER_LIST);
		}
		
		OS_SVC.submit(() -> {
			try {
				IOUtil.write(dos, TradeMsg.PAIRS.ordinal(), LocalServer.TRADE_PAIRS);
				for (Order o : list)
					sendOrderToTrader(o);
			} catch (IOException e) {
				closeConnection();}
			}
		);
	}


	public String getAddress() {
		return s.getRemoteSocketAddress().toString();
	}
	
	
	public int getNumOrders() {
		return ORDER_LIST.size();
	}


	public void sendOrderToTrader(Order o) {
		OS_SVC.submit(() -> {
			try {
				o.send(dos);
			} catch (IOException e) {
				closeConnection();}
			}
		);
	}
	
	public void deleteOrder(byte[] hash) {
		OS_SVC.submit(() -> {
			try {
				IOUtil.write(dos, TradeMsg.DELETE.ordinal(), hash);
			} catch (IOException e) {
				e.printStackTrace();
				closeConnection();}
			}
		);
	}



	public void closeConnection() {
		ExchangeServer.LOG.add("Closing Connection with Trader " + getAddress());
		ORDER_LIST.clear();
		Util.closeQuietly(s);
		Util.closeQuietly(dos);
		OS_SVC.shutdown();
		ExchangeServer.removeTrader(this);
	}
	

}

package forktrader.server;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

import forks.ForkData;
import forktrader.types.Order;
import logging.LogModel;
import logging.LogView;
import util.Ico;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ExchangeServer extends JPanel {
	final static List<Order> ORDER_LIST = new ArrayList<>();
	final static OrderBookModel ORDER_MODEL = new OrderBookModel();
	
	final static List<Trader> TRADER_LIST = new ArrayList<>();
	final static TraderModel TRADER_MODEL = new TraderModel();
	
	private final double XCHPriceD;
	private final JLabel XCHPrice = new JLabel();
	
	private final JTable TRADER_TABLE = new JTable(TRADER_MODEL);
	private final JScrollPane TRADER_JSP = new JScrollPane(TRADER_TABLE);
	
	private final JTable ORDER_TABLE = new JTable(ORDER_MODEL);
	private final JScrollPane ORDER_JSP = new JScrollPane(ORDER_TABLE);
	private final JPanel bottomPanel = new JPanel(new BorderLayout());
	
	public final static LogModel LOG = new LogModel();
	private final LogView logView = LOG.newPanelView();
	
	private final JPanel leftPanel = new JPanel(new BorderLayout());
	private final JPanel centerPanel = new JPanel(new BorderLayout());
	
	static class OrderBookModel extends JFunTableModel<Order> {
		public OrderBookModel() {
			super();
			
			addColumn("Type",   		60,	String.class,	o->o.oType).showMandatory();
			addColumn("Symbol",   		60,	String.class,	o->o.symbol).showMandatory();
			addColumn("Amount",   		80,	Double.class,	o->o.amount).showMandatory();
			addColumn("$",   			80,	Double.class,	o->o.price).showMandatory();
			addColumn("XCH Total",  	80,	Double.class, 	o->o.xchTotal).showMandatory();
			addColumn("Owner", 			80,	String.class, 	o->o.owner).showMandatory().viewRight();
			addColumn("Executor", 		80,	String.class, 	o->o.executor).showMandatory().viewRight();
			addColumn("Order Hash", 	-1,	String.class, 	o->o.hashToString()).showMandatory().viewRight();
			
			onGetRowCount(() -> ORDER_LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(ORDER_LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	static class TraderModel extends JFunTableModel<Trader> {
		public TraderModel() {
			super();
			
			addColumn("IP",   		120,	String.class,	t->t.getAddress()).showMandatory();
			addColumn("Orders", 	-1,	Double.class,	t->t.getNumOrders()).showMandatory();
			
			onGetRowCount(() -> TRADER_LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(TRADER_LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public ExchangeServer() {
		setLayout(new BorderLayout());
		
		add (leftPanel, BorderLayout.LINE_START);
		add (centerPanel, BorderLayout.CENTER);
		centerPanel.add(XCHPrice, BorderLayout.PAGE_START);
		centerPanel.add(ORDER_JSP,BorderLayout.CENTER);
		centerPanel.add(bottomPanel, BorderLayout.PAGE_END);
		
		bottomPanel.add(logView,BorderLayout.CENTER);
		
		leftPanel.add(TRADER_JSP,BorderLayout.CENTER);
		leftPanel.add(new LocalServer(),BorderLayout.PAGE_END);
		
		XCHPriceD = ForkData.getBySymbol("xch").get().price;
		XCHPrice.setText("XCH: $" + XCHPriceD);
		
		logView.JSP.setPreferredSize(new Dimension(300,150));
		
		ORDER_JSP.setPreferredSize(new Dimension(600,500));
		TRADER_JSP.setPreferredSize(new Dimension(200,500));
		
		ORDER_MODEL.colList.forEach(c -> c.finalize(ORDER_TABLE,null));
		TRADER_MODEL.colList.forEach(c -> c.finalize(TRADER_TABLE,null));
		
		JPopupMenu POPUP_MENU = new JPopupMenu();
		
		TRADER_TABLE.setComponentPopupMenu(POPUP_MENU);
		
		
		POPUP_MENU.add(new SwingEX.JMI("Remove Traders", Ico.HIDE, () -> removeTraders()));
		
	}
	
	private void removeTraders() {
		
		synchronized (TRADER_LIST) {
			List<Trader> tList = SwingUtil.getSelected(TRADER_TABLE, TRADER_LIST);
			TRADER_LIST.removeAll(tList);
			tList.forEach(Trader::closeConnection);
		}
		
		
		
	}

	public static boolean broadcastOrder(Order o) {
		synchronized (ORDER_LIST) {
			Optional<Order> existOrder = Order.getOrderByHash(ORDER_LIST, o.hash);
			
			if (existOrder.isPresent()) {
				LOG.add("Order already exist!" + Util.getHexString(o.hash));
				return false;
			}
				
			
			LOG.add("Adding Order: " + Util.getHexString(o.hash));
			ORDER_LIST.add(o);
		}
		SwingUtilities.invokeLater(ORDER_MODEL::fireTableDataChanged);
		
		synchronized (TRADER_LIST) {
			TRADER_LIST.forEach(t -> t.sendOrderToTrader(o));
		}
		return true;
		
	}

	public static void addTrader(Trader t) {
		synchronized (TRADER_LIST) {
			LOG.add("New Trader: " + t.getAddress());
			TRADER_LIST.add(t);
		}
		SwingUtilities.invokeLater(TRADER_MODEL::fireTableDataChanged);
		
	}

	public static void removeTrader(Trader t) {
		synchronized (TRADER_LIST) {
			LOG.add("Trader Disconnected: " + t.getAddress());
			TRADER_LIST.remove(t);
		}
		
		List<Order> removeList = new ArrayList<>();
		synchronized (ORDER_LIST) {
			removeList = ORDER_LIST.stream().filter(o -> t == o.owner).collect(Collectors.toList());
			ORDER_LIST.removeAll(removeList);
		}
		
		synchronized (TRADER_LIST) {
			for (Order o : removeList)
				TRADER_LIST.forEach(tr -> tr.deleteOrder(o.hash));
		}
		
		SwingUtilities.invokeLater(ORDER_MODEL::fireTableDataChanged);
		SwingUtilities.invokeLater(TRADER_MODEL::fireTableDataChanged);
	}
	
	public static void deleteOrder(Trader req, byte[] hash) {
		LOG.add("Exchange: deleting order");
		Optional<Order> optOrder = Order.getOrderByHash(ORDER_LIST,hash);
		
		if (optOrder.isEmpty()) {
			LOG.add("could not find order " + Util.getHexString(hash));
			return;
		}
		
		Order o = optOrder.get();
		if (req != o.owner) {
			LOG.add("Trader attempted to delete order they down own");
			return;
		}
		
		synchronized (ORDER_LIST) {
			ORDER_LIST.remove(o);
			SwingUtilities.invokeLater(ORDER_MODEL::fireTableDataChanged);
		}
		
		synchronized (TRADER_LIST) {
			TRADER_LIST.forEach(t -> t.deleteOrder(o.hash));
		}
		
		
		
		
	}
	
	

}

package forktrader.client;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import forks.ForkData;
import forktrader.types.Order;
import forktrader.types.Order.TYPE;
import logging.LogModel;
import logging.LogView;
import types.Balance;
import util.I18n;
import util.Ico;
import util.swing.SwingEX;
import util.swing.SwingEX.LTPanel;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ClientView extends JPanel {
	JPanel orderViewPanel = new JPanel(new BorderLayout());
	private ForkData fd = null;
	
	public final List<ForkData> PAIR_LIST = new ArrayList<>();
	public final ForkSelectionModel PAIR_MODEL = new ForkSelectionModel();
	private final JTable PAIR_LIST_TABLE = new JTable(PAIR_MODEL);
	//private final JScrollPane FORK_LIST_JSP = new JScrollPane(FORK_LIST_TABLE);
	private final JPanel leftPanel = new JPanel(new BorderLayout());
	
	private String selectedSymbol = "";
	private final JLabel symbolLabel = new JLabel(selectedSymbol);
	
	List<Order> ORDER_LIST = new ArrayList<>();
	OrderBookModel ORDER_MODEL = new OrderBookModel();
	
	private final double XCHPriceD;
	private final JLabel XCHPrice = new JLabel();
	
	private final JTable ORDER_TABLE = new JTable(ORDER_MODEL);
	private final JScrollPane JSP = new JScrollPane(ORDER_TABLE);
	private final JPanel bottomPanel = new JPanel(new BorderLayout());
	
	private final String[] OTS = {"BUY", "SELL"};
	private final JComboBox<String> OTYPE = new JComboBox<>(OTS);

	private final LTPanel amtField = new LTPanel(12);
	private final LTPanel xchField = new LTPanel(12);
	private final JButton submitBtn = new JButton("Submit");
	
	JPanel computePanel = new JPanel(new GridLayout(2,1));
	double usdPriceDbl;
	double usdTotalDbl;
	JLabel xchPrice = new JLabel("XCH Price:                  ");
	JLabel xchTotal = new JLabel("XCH Total:                  ");
	
	public final LogModel LOG = new LogModel();
	private final LogView logView = LOG.newPanelView();
	private final ClientConnection c = new ClientConnection(this);
	
	JPanel midPanelL = new JPanel();
	JPanel midPanelC = new JPanel();
	JPanel midPanel = new JPanel();
	
	class ForkSelectionModel extends JFunTableModel<ForkData> {
		public ForkSelectionModel() {
			super();
			
			addColumn("Logo",   	 22,	Icon.class,		f->f.ico).show().fixed().colName("");
			addColumn("Symbol",  	 50,	String.class, 	f->f.coinPrefix).show().colName(I18n.ForkView.symbolColName);
			addColumn("Name",   	 80,	String.class,	f->f.displayName).colName(I18n.ForkView.nameColName);
			
			onGetRowCount(() -> PAIR_LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(PAIR_LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	class OrderBookModel extends JFunTableModel<Order> {
		public OrderBookModel() {
			super();
			
			addColumn("Type",   		60,	String.class,	o->o.oType).showMandatory();
			addColumn("Symbol",   		60,	String.class,	o->o.symbol).showMandatory();
			addColumn("Amount",   		80,	Balance.class,	Order::getAmount).showMandatory().viewRight();
			addColumn("$",   			80,	Balance.class,	Order::getPrice).showMandatory().viewRight();
			addColumn("XCH Total",  	80,	Balance.class, 	Order::xchTotal).showMandatory().viewRight();
			addColumn("Order Hash", 	-1,	String.class, 	o->o.hashToString()).showMandatory().viewRight();
			
			onGetRowCount(() -> ORDER_LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(ORDER_LIST.get(r)));
			onisCellEditable((r, c) -> false);
		}
	}
	
	public ClientView() {
		setLayout(new BorderLayout());
		add(xchPrice, BorderLayout.PAGE_START);
		
		orderViewPanel.setBorder(new TitledBorder(""));
		orderViewPanel.add(JSP,BorderLayout.CENTER);
		orderViewPanel.add(bottomPanel, BorderLayout.PAGE_END);
	
		add(orderViewPanel,BorderLayout.CENTER);
		add(leftPanel, BorderLayout.LINE_START);
		
		leftPanel.add(PAIR_LIST_TABLE.getTableHeader(),BorderLayout.PAGE_START);
		leftPanel.add(PAIR_LIST_TABLE,BorderLayout.CENTER);
		leftPanel.add(c, BorderLayout.PAGE_END);

		SwingUtilities.invokeLater(PAIR_MODEL::fireTableDataChanged);
		
		PAIR_LIST_TABLE.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		ListSelectionModel selectionModel = PAIR_LIST_TABLE.getSelectionModel();

		selectionModel.addListSelectionListener(new ListSelectionListener() {
		    public void valueChanged(ListSelectionEvent e) {
		    	if (e.getValueIsAdjusting())
		    		return;
		    	List<ForkData> fdSelection = SwingUtil.getSelected(PAIR_LIST_TABLE, PAIR_LIST);
		    	fd = (fdSelection.size() > 0) ? fdSelection.get(0) : null;
		    	updateOrderBook();
		    }
		});
		
		bottomPanel.add(midPanel,BorderLayout.PAGE_START);
		bottomPanel.add(logView,BorderLayout.CENTER);
		
		midPanel.setLayout(new BorderLayout());
		midPanel.add(midPanelL,BorderLayout.LINE_START);
		
		XCHPriceD = ForkData.getBySymbol("xch").get().price;
		XCHPrice.setText("XCH: $" + XCHPriceD);
		
		midPanel.setBorder(new TitledBorder("Create Order:"));
		midPanelL.add(OTYPE);
		midPanelL.add(amtField);
		midPanelL.add(symbolLabel);
		midPanelL.add(xchField);
		midPanelL.add(new JLabel(" XCH"));
		midPanelL.add(submitBtn);
		submitBtn.setEnabled(false);
		
		
		submitBtn.addActionListener(al -> {
			TYPE t = (0 == OTYPE.getSelectedIndex()) ? TYPE.BUY : TYPE.SELL;
			Order o = new Order(t,fd.coinPrefix,amtField.getAsDouble(),xchField.getAsDouble()); 
			c.sendOrder(o);
		});
		
		midPanelL.add(computePanel);
		logView.JSP.setPreferredSize(new Dimension(300,150));
		
		amtField.field.setHorizontalAlignment(SwingConstants.RIGHT);
		xchField.field.setHorizontalAlignment(SwingConstants.RIGHT);
		computePanel.add(xchPrice);
		computePanel.add(xchTotal);
		
		amtField.setChangeListener(() -> {
			computePrice();
		});
		
		xchField.setChangeListener(() -> {
			computePrice();
		});
		
		computePanel.setPreferredSize(new Dimension(120,30));
		
		JSP.setPreferredSize(new Dimension(600,500));
		
		ORDER_MODEL.colList.forEach(c -> c.finalize(ORDER_TABLE,null));
		PAIR_MODEL.colList.forEach(c -> c.finalize(PAIR_LIST_TABLE,null));
		
		TableCellRenderer tableRenderer = ORDER_TABLE.getDefaultRenderer(JButton.class);
		ORDER_TABLE.setDefaultRenderer(JButton.class, new JTableButtonRenderer(tableRenderer));
	    		
		DefaultTableCellRenderer greenRenderer = new DefaultTableCellRenderer(){
	        @Override
	        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
	            Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);
	            c.setBackground(null);
	            Order o = ORDER_LIST.get(row);
	            
	            if (o.myOrder)
	            	c.setBackground(Color.green);
	            return c;
	        }
	    };
	    
	    ORDER_TABLE.getColumnModel().getColumn(ORDER_MODEL.getIndex("Type")).setCellRenderer(greenRenderer);
	    
		JPopupMenu POPUP_MENU = new JPopupMenu();
		
		ORDER_TABLE.setComponentPopupMenu(POPUP_MENU);
		//JSP.setComponentPopupMenu(POPUP_MENU);
		
		
		POPUP_MENU.addPopupMenuListener(new PopupMenuListener() {
			@Override public void popupMenuCanceled(PopupMenuEvent pme) {
				
			};
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
				
			}
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				List<Order> oList = SwingUtil.getSelected(ORDER_TABLE, ORDER_LIST);
				
				POPUP_MENU.removeAll();
				if (1 != oList.size())
					return;
				
				if (oList.get(0).myOrder)
					POPUP_MENU.add(new SwingEX.JMI("Delete Trade", Ico.HIDE, () -> deleteOrder()));
				else
					POPUP_MENU.add(new SwingEX.JMI("Execute Trade", Ico.EXCHANGE, () -> executeOrder()));
			}
		});
		
		updateOrderBook();
	}
	
	private void updateOrderBook() {
		amtField.field.setText("");
		xchField.field.setText("");
		if (null == fd) {
			selectedSymbol = "";
			orderViewPanel.setBorder(new TitledBorder("Select fork"));
			ORDER_LIST.clear();
			SwingUtilities.invokeLater(ORDER_MODEL::fireTableDataChanged);
			setOrderEnable(false);
		} else {
			orderViewPanel.setBorder(new TitledBorder(fd.displayName + " Order Book"));
			setOrderEnable(true);
			selectedSymbol = fd.coinPrefix;
		}
		symbolLabel.setText(selectedSymbol + " for ");
		computePrice();
		refreshOrders();
			
	}
	
	private void setOrderEnable(boolean state) {
		submitBtn.setEnabled(state);
		amtField.field.setEnabled(state);
		xchField.field.setEnabled(state);
		OTYPE.setEnabled(state);
		
	}
	
	private void deleteOrder() {
		Order o = SwingUtil.getSelected(ORDER_TABLE, ORDER_LIST).get(0);
		c.deleteOrder(o.hash);
	}
	
	private void executeOrder() {
		Order o = SwingUtil.getSelected(ORDER_TABLE, ORDER_LIST).get(0);
		c.executeOrder(o.hash);
		
	}

	class JTableButtonRenderer implements TableCellRenderer {
		   private TableCellRenderer defaultRenderer;
		   public JTableButtonRenderer(TableCellRenderer renderer) {
		      defaultRenderer = renderer;
		   }
		   public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		      if(value instanceof Component)
		         return (Component)value;
		         return defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		   }
		}

	private void computePrice() {
		try {
		double amount = amtField.getAsDouble();
		double numXCH = xchField.getAsDouble();
		
		if (0 == numXCH || 0 == amount) {
			xchPrice.setText("$/x:");
			xchTotal.setText("$ Total:");
			submitBtn.setEnabled(false);
			return;
		}
		
		usdPriceDbl = (XCHPriceD * numXCH) / amount;
		usdTotalDbl = XCHPriceD * numXCH;
		
		
		xchPrice.setText("$/"+selectedSymbol + String.format(": %1.5f",usdPriceDbl));
		xchTotal.setText(String.format("$ Total:  %1.5f",usdTotalDbl));
		submitBtn.setEnabled((null != fd));
		} catch (Exception e) {
			submitBtn.setEnabled(false);
		}
		
		
	}

	public void refreshOrders() {
		if (null == fd)
			return;
		synchronized (c.ORDER_LIST) {
			ORDER_LIST = c.ORDER_LIST.stream().filter(o -> o.symbol.equals(fd.coinPrefix)).collect(Collectors.toList()); 
			Collections.sort(ORDER_LIST);
			SwingUtilities.invokeLater(ORDER_MODEL::fireTableDataChanged);
			
		}
		
	}

	public void closeConnection() {
		PAIR_LIST.clear();
		ORDER_LIST.clear();
		SwingUtilities.invokeLater(PAIR_MODEL::fireTableDataChanged);
		SwingUtilities.invokeLater(ORDER_MODEL::fireTableDataChanged);
		LOG.add("view.closedConnection");
	}
	
	
	

}

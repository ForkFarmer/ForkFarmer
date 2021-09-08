package forks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.JTableHeader;

import main.ForkFarmer;
import main.MainGui;
import types.Balance;
import types.Effort;
import types.ReadTime;
import types.TimeU;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	public static final ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	public static class ForkTableModel extends JFunTableModel<Fork> {
		public ForkTableModel() {
			super();
			
			addColumn("",   		22,	Icon.class,		f->f.ico).showMandatory();
			addColumn("Symbol",   	50,	String.class, 	f->f.symbol).show();
			addColumn("Balance",	140,Balance.class, 	f->f.balance).show();
			addColumn("$",			60, Double.class, 	f->f.price).show();
			addColumn("Netspace",	80, NetSpace.class, f->f.netSpace).show();
			addColumn("Height",		80, Integer.class,  f->f.height);
			addColumn("Farm Size",	80, NetSpace.class, f->f.plotSpace);
			addColumn("Version",	80, String.class,   f->f.version);
			addColumn("Sync",		80, String.class,   f->f.syncStatus);
			addColumn("Farm",		80, String.class,   f->f.farmStatus).show();
			addColumn("ETW",		140,TimeU.class,    f->f.etw);
			addColumn("24H Win",	60,	Double.class, 	f->f.dayWin);
			addColumn("Last Win",	120,TimeU.class, 	f->f.getPreviousWin());
			addColumn("Effort",		60,	Effort.class, 	Fork::getEffort);
			addColumn("Address",	-1,	String.class, 	f->f.addr).showMandatory();;
			addColumn("Reward",		40,	Double.class, 	f->f.rewardTrigger);
			addColumn("Time",		50,	ReadTime.class, f->f.readTime).show();
			addColumn("", 			22, Icon.class, 	f->f.statusIcon).showMandatory();
			
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> (3 == c || 15 == c));
		}
		
		public void setValueAt(Object value, int row, int col) {
			double newValue = (double) value;
			if (3 == col) {
				Fork.LIST.get(row).price = newValue;
				fireTableCellUpdated(row, col);
				MainGui.updateBalance();
			} else if (15 == col) {
				Fork.LIST.get(row).rewardTrigger = newValue;
				fireTableCellUpdated(row, col);
			}
			
	    }
		
	}
	
	public ForkView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		
		JSP.setPreferredSize(new Dimension(700,400));
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI("Start", 	Ico.START, 		() -> getSelected().forEach(Fork::start)));
		POPUP_MENU.add(new SwingEX.JMI("Stagger", 	Ico.START,		() -> ForkView.staggerStartDialog()));
		POPUP_MENU.add(new SwingEX.JMI("Stop", 		Ico.STOP,  		() -> getSelected().forEach(Fork::stop)));
		POPUP_MENU.addSeparator();
		
		POPUP_MENU.add(new SwingEX.JMI("View Log", 	Ico.EYE,  		ForkView::viewLog));
		//POPUP_MENU.add(new SwingEX.JMI("New Addr", 	Ico.GEAR, 		() -> getSelected().forEach(Fork::generate)));
		POPUP_MENU.add(new SwingEX.JMI("Copy", 		Ico.CLIPBOARD,  ForkView::copy));
		POPUP_MENU.add(new SwingEX.JMI("Update", 	Ico.DOLLAR,  	ForkView::updatePrices));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Refresh",	Ico.REFRESH,  	ForkView::refresh));
		POPUP_MENU.add(new SwingEX.JMI("Hide", 		Ico.HIDE,  		ForkView::removeSelected));
		POPUP_MENU.add(new SwingEX.JMI("Show Peers",Ico.P2P,	() -> getSelected().forEach(Fork::showConnections)));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Debug",		Ico.BUG,	() -> getSelected().forEach(Fork::showLastException)));
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));
		
		SwingUtil.addToolTipCol(TABLE,1,i -> {return Fork.LIST.get(i).name;});
		
		SwingUtil.setColRight(TABLE,2);
	}
	
	static private void staggerStartDialog() {
		String delay= JOptionPane.showInputDialog(ForkFarmer.FRAME,"Enter Start Interval (Seconds)", "60");
		try {
			int delayInt = Integer.parseInt(delay);
			new Thread(() -> staggerStart(delayInt)).start();
		} catch (Exception e) {
			ForkFarmer.showMsg("Error", "Error parsing delay");
		}
	}
	
	static private void staggerStart(int delay) {
		List<Fork> selList = getSelected();
		
		for (Fork f : selList) {
			f.start();
			Util.sleep(delay * 1000);
		}
	}
	
	static private void updatePrices() {
		JPanel logPanel = new JPanel(new BorderLayout());
		JTextArea jta = new JTextArea();
		JScrollPane JSP = new JScrollPane(jta);
		JSP.setPreferredSize(new Dimension(900,600));
		logPanel.add(JSP,BorderLayout.CENTER);
		
		ForkFarmer.showPopup("Paste xchforks.com table", logPanel);
		
		String xchForksTable = jta.getText();
		
		String[] rows = xchForksTable.split("\n");
		
		for (String row : rows) {
			row = row.replaceAll("\t", " ");
			row = row.replaceAll("\\s+", " ");
			String[] cols = row.split(" ");
			
			if (cols.length < 9)
				continue;
			
			for (Fork f: Fork.LIST) {

				if (cols[2].equals(f.symbol)) {
					if (f.symbol.equals("XCH"))
						f.price = Double.parseDouble(cols[7]);
					else
						f.price = Double.parseDouble(cols[8]);
				}
			}
		
			update();
		}
	}
	
	static private void refresh() {
		getSelected().forEach(Fork::refresh);
	}
	
	static private void removeSelected() {
		List<Fork> selList = getSelected();
		selList.forEach(f -> f.hidden = true);
		Fork.LIST.removeAll(selList);
		update();
	}

	static private void viewLog() {
		getSelected().forEach(Fork::viewLog);
	}
	
	static private void copy() {
		List<Fork> forkList = getSelected();
		StringBuilder sb = new StringBuilder();
		for (Fork f: forkList)
			if (null != f.addr)
				sb.append(f.addr + "\n");
		
		StringSelection stringSelection = new StringSelection(sb.toString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	private static List<Fork> getSelected() {
		return SwingUtil.getSelected(TABLE, Fork.LIST);
	}

	public static void update() {
		MODEL.fireTableDataChanged();
	}
	
	private static void fireTableRowUpdated(int row) {
		MODEL.fireTableRowsUpdated(row, row);
	}
	
	public static void update(Fork f) {
		f.getIndex().ifPresent(ForkView::fireTableRowUpdated);
	}
	
	public static void updateLogRead(Fork f) {
		f.getIndex().ifPresent(row -> {
			MODEL.fireTableCellUpdated(row, 16); // time
			MODEL.fireTableCellUpdated(row, 17); // status
		});
	}

	public static void updateNewTx(Fork f) {
		f.getIndex().ifPresent(row -> {
			MODEL.fireTableCellUpdated(row, 12); //last win
		});
	}
}

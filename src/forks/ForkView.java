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
import util.Ico;
import util.NetSpace;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingEX.JMCI;
import util.swing.SwingUtil;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	@SuppressWarnings("unchecked")
	public static Col<Fork> cols[] = new Col[] {
		new Col<Fork>("",   		22,	Icon.class,		f->f.ico),
		new Col<Fork>("Symbol",   	50,	String.class, 	f->f.symbol),
		new Col<Fork>("Balance",	140,String.class, 	Fork::getBalance),
		new Col<Fork>("$",			60, Double.class, 	f->f.price),
		new Col<Fork>("Netspace",	80, NetSpace.class, f->f.ns),
		new Col<Fork>("Height",		0, Integer.class,  f->f.height),
		new Col<Fork>("Farm Size",	0,  NetSpace.class, f->f.ps),
		new Col<Fork>("Version",	0,  String.class,   f->f.version),
		new Col<Fork>("Status",		80,  String.class,   f->f.status),
		new Col<Fork>("Estimated Win Time",	0,  String.class,   f->f.etw),
		new Col<Fork>("Address",	-1,	String.class, 	f->f.addr),
		new Col<Fork>("Time",		50,	String.class, 	Fork::getReadTime),
		new Col<Fork>("", 		 	22, Icon.class, 	f->f.statusIcon)
	};
	
	final static ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	private static final JMCI PRICE_COLUMN_CHECK = new JMCI("$", ForkView::colChanged);
	private static final JMCI NET_COLUMN_CHECK = new JMCI("Netspace", ForkView::colChanged);
	private static final JMCI HEIGHT_COLUMN_CHECK = new JMCI("Height", ForkView::colChanged);
	private static final JMCI FARM_COLUMN_CHECK = new JMCI("Farm Size", ForkView::colChanged);
	private static final JMCI VER_COLUMN_CHECK = new JMCI("Version", ForkView::colChanged);
	private static final JMCI STAT_COLUMN_CHECK = new JMCI("Status", ForkView::colChanged);
	private static final JMCI ETW_COLUMN_CHECK = new JMCI("Win Time", ForkView::colChanged);
	
	static class ForkTableModel extends JFunTableModel {
		public ForkTableModel() {
			super(cols);
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> cols[c].apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> (3 == c));
		}
		
		public void setValueAt(Object value, int row, int col) {
			double newPrice = (double) value;
			if (3 == col) {
				Fork.LIST.get(row).price = newPrice;
				fireTableCellUpdated(row, col);
				MainGui.updateBalance();
			}
	    }
	}

	public ForkView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
		Col.adjustWidths(TABLE,cols);

		JSP.setPreferredSize(new Dimension(700,400));
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		POPUP_MENU.add(new SwingEX.JMI("Start", 	Ico.START, 		() -> getSelectedForks().forEach(Fork::start)));
		POPUP_MENU.add(new SwingEX.JMI("Stagger", 	Ico.START,	() -> ForkView.staggerStartDialog()));
		POPUP_MENU.add(new SwingEX.JMI("Stop", 		Ico.STOP,  		() -> getSelectedForks().forEach(Fork::stop)));
		POPUP_MENU.addSeparator();
		
		POPUP_MENU.add(new SwingEX.JMI("View Log", 	Ico.EYE,  		ForkView::viewLog));
		POPUP_MENU.add(new SwingEX.JMI("New Addr", 	Ico.GEAR, 		() -> getSelectedForks().forEach(Fork::generate)));
		POPUP_MENU.add(new SwingEX.JMI("Copy", 		Ico.CLIPBOARD,  ForkView::copy));
		POPUP_MENU.add(new SwingEX.JMI("Update", 	Ico.DOLLAR,  	ForkView::updatePrices));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Refresh",	Ico.REFRESH,  	ForkView::refresh));
		POPUP_MENU.add(new SwingEX.JMI("Hide", 		Ico.HIDE,  		ForkView::removeSelected));
		POPUP_MENU.add(new SwingEX.JMI("Show Peers",Ico.MACHINE,	() -> getSelectedForks().forEach(Fork::showConnections)));
		
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		NET_COLUMN_CHECK.setSelected(true);
		STAT_COLUMN_CHECK.setSelected(true);
		PRICE_COLUMN_CHECK.setSelected(true);
		HEADER_MENU.add(PRICE_COLUMN_CHECK);
		HEADER_MENU.add(NET_COLUMN_CHECK);
		HEADER_MENU.add(HEIGHT_COLUMN_CHECK);
		HEADER_MENU.add(FARM_COLUMN_CHECK);
		HEADER_MENU.add(VER_COLUMN_CHECK);
		HEADER_MENU.add(VER_COLUMN_CHECK);
		HEADER_MENU.add(STAT_COLUMN_CHECK);
		HEADER_MENU.add(ETW_COLUMN_CHECK);
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
		List<Fork> selList = getSelectedForks();
		
		for (Fork f : selList) {
			f.start();
			Util.sleep(delay * 1000);
		}
	}
	
	static private void updatePrices() {
		JPanel logPanel = new JPanel(new BorderLayout());
		JTextArea jta = new JTextArea();
		JScrollPane JSP = new JScrollPane(jta);
		JSP.setPreferredSize(new Dimension(800,600));
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
	
	static private void colChanged() {
		cols[3].width = (PRICE_COLUMN_CHECK.isSelected()) ? 60 : 0;
		cols[4].width = (NET_COLUMN_CHECK.isSelected()) ? 80 : 0;
		cols[5].width = (HEIGHT_COLUMN_CHECK.isSelected()) ? 60 : 0;
		cols[6].width = (FARM_COLUMN_CHECK.isSelected()) ? 80 : 0;
		cols[7].width = (VER_COLUMN_CHECK.isSelected()) ? 80 : 0;
		cols[8].width = (STAT_COLUMN_CHECK.isSelected()) ? 80 : 0;
		cols[9].width = (ETW_COLUMN_CHECK.isSelected()) ? 160 : 0;
			
		Col.adjustWidths(TABLE,cols);
	}
	
	static private void refresh() {
		getSelectedForks().forEach(Fork::refresh);
	}
	
	static private void removeSelected() {
		List<Fork> selList = getSelectedForks();
		selList.forEach(f -> f.cancel = true);
		Fork.LIST.removeAll(selList);
		update();
	}

	static private void viewLog() {
		getSelectedForks().forEach(Fork::viewLog);
	}
	
	static private void copy() {
		List<Fork> forkList = getSelectedForks();
		StringBuilder sb = new StringBuilder();
		for (Fork f: forkList)
			if (null != f.addr)
				sb.append(f.addr + "\n");
		
		StringSelection stringSelection = new StringSelection(sb.toString());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
	}
	
	public static List<Fork> getSelectedForks() {
		return SwingUtil.getSelected(TABLE, Fork.LIST);
	}

	public static void update() {
		MODEL.fireTableDataChanged();
	}
	
	public static void fireTableRowUpdated(int row) {
		MODEL.fireTableRowsUpdated(row, row);
	}
	
	public static void fireTableLogRead(int row) {
		MODEL.fireTableCellUpdated(row, 11);
		MODEL.fireTableCellUpdated(row, 12);
	}
}

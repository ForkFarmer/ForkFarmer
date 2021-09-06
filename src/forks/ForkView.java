package forks;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.util.List;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import main.ForkFarmer;
import main.MainGui;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	@SuppressWarnings("unchecked")
	
	public static Col<Fork> cols[] = new Col[] {
		new Col<Fork>("",   		22,	Icon.class,		f->f.ico),
		new Col<Fork>("Symbol",   	50,	String.class, 	f->f.symbol),
		new Col<Fork>("Balance",	140,Double.class, 	f ->f.balance),
		new Col<Fork>("$",			60, Double.class, 	f->f.price),
		new Col<Fork>("Netspace",	80, NetSpace.class, f->f.netSpace),
		new Col<Fork>("Height",		80, Integer.class,  f->f.height),
		new Col<Fork>("Farm Size",	80, NetSpace.class, f->f.plotSpace),
		new Col<Fork>("Version",	80, String.class,   f->f.version),
		new Col<Fork>("Sync",		80, String.class,   f->f.syncStatus),
		new Col<Fork>("Farm",		80, String.class,   f->f.farmStatus),
		new Col<Fork>("ETW",		140,String.class,   f->f.etw),
		new Col<Fork>("24H Win",	60,	Double.class, 	f->f.dayWin),
		new Col<Fork>("Last Win",	120,String.class, 	f->f.getPreviousWin()),
		new Col<Fork>("Effort",		60,	String.class, 	f->f.getEffort()),
		new Col<Fork>("Address",	-1,	String.class, 	f->f.addr),
		new Col<Fork>("Reward",		40,	Double.class, 	f->f.rewardTrigger),
		new Col<Fork>("Time",		50,	String.class, 	Fork::getReadTime),
		new Col<Fork>("", 		 	22, Icon.class, 	f->f.statusIcon)
		
	};
	
	final static ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	static class ForkTableModel extends JFunTableModel {
		public ForkTableModel() {
			super(cols);
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> cols[c].apply(Fork.LIST.get(r)));
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
		POPUP_MENU.add(new SwingEX.JMI("Show Peers",Ico.MACHINE,	() -> getSelected().forEach(Fork::showConnections)));
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		cols[0].setSelectView(TABLE, HEADER_MENU, false, true);
		cols[1].setSelectView(TABLE, HEADER_MENU, false, true);
		cols[2].setSelectView(TABLE, HEADER_MENU, true, true);
		cols[3].setSelectView(TABLE, HEADER_MENU, true, true);
		cols[4].setSelectView(TABLE, HEADER_MENU, true, true);
		cols[5].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[6].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[7].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[8].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[9].setSelectView(TABLE, HEADER_MENU, true, true);
		cols[10].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[11].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[12].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[13].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[14].setSelectView(TABLE, HEADER_MENU, false, true);
		cols[15].setSelectView(TABLE, HEADER_MENU, true, false);
		cols[16].setSelectView(TABLE, HEADER_MENU, false, true);
		cols[17].setSelectView(TABLE, HEADER_MENU, false, true);
		

		SwingUtil.addToolTipCol(TABLE,1,i -> {return Fork.LIST.get(i).name;});
		
		TABLE.getColumnModel().getColumn(2).setCellRenderer(new BalanceRenderer());
	}
	
	class BalanceRenderer extends DefaultTableCellRenderer {
	    public Component getTableCellRendererComponent(
	                        JTable table, Object value,
	                        boolean isSelected, boolean hasFocus,
	                        int row, int column) {
	        JLabel c = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
	        setHorizontalAlignment(SwingConstants.RIGHT);
	        if (c.getText().startsWith("-"))
	        	c.setText("");
	        return c;
	    }
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

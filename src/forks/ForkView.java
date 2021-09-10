package forks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.JTableHeader;

import main.ForkFarmer;
import main.MainGui;
import types.Balance;
import types.Effort;
import types.ReadTime;
import types.TimeU;
import util.FFUtil;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.apache.ReversedLinesFileReader;
import util.swing.Reorderable;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.TableRowTransferHandler;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	public static final ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	public static class ForkTableModel extends JFunTableModel<Fork> implements Reorderable {
		public ForkTableModel() {
			super();
			
			addColumn("",   		22,	Icon.class,		f->f.ico).showMandatory();
			addColumn("Symbol",   	50,	String.class, 	f->f.symbol).show();
			addColumn("Name",   	80,	String.class, 	f->f.name);
			addColumn("Balance",	140,Balance.class, 	f->f.balance).show();
			addColumn("$",			60, Double.class, 	f->f.price).show();
			//addColumn("Est. $/Mth",	80, Double.class, 	f->f.estEarn);
			addColumn("Netspace",	80, NetSpace.class, f->f.netSpace).show();
			addColumn("Height",		80, Integer.class,  f->f.height);
			addColumn("Farm Size",	80, NetSpace.class, f->f.plotSpace);
			addColumn("Version",	80, String.class,   f->f.version);
			addColumn("Sync",		80, String.class,   f->f.syncStatus);
			addColumn("Farm",		80, String.class,   f->f.farmStatus).show();
			addColumn("ETW",		150,TimeU.class,    f->f.etw);
			addColumn("24H Win",	60,	Double.class, 	f->f.dayWin);
			addColumn("Last Win",	120,TimeU.class, 	f->f.getPreviousWin());
			addColumn("Effort",		60,	Effort.class, 	Fork::getEffort);
			addColumn("Address",	-1,	String.class, 	f->f.addr).showMandatory();;
			addColumn("Reward",		40,	Double.class, 	f->f.rewardTrigger);
			addColumn("Time",		50,	ReadTime.class, f->f.readTime).show();
			addColumn("", 			22, Icon.class, 	f->f.statusIcon).showMandatory();
			
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> (4 == c || 17 == c));
		}
		
		public void setValueAt(Object value, int row, int col) {
			double newValue = (double) value;
			if (4 == col) {
				Fork.LIST.get(row).price = newValue;
				fireTableCellUpdated(row, col);
				MainGui.updateBalance();
			} else if (17 == col) {
				Fork.LIST.get(row).rewardTrigger = newValue;
				fireTableCellUpdated(row, col);
			}
			
	    }

		@Override
		public void reorder(int fromIndex, int toIndex) {
			synchronized (Fork.LIST) {
				SwingUtil.mapViewToModel(TABLE,Fork.LIST);
						
				fromIndex = TABLE.convertRowIndexToModel(fromIndex);
				toIndex = TABLE.convertRowIndexToModel(toIndex);
				 
				if (toIndex > fromIndex) // need account from 'remove' in toIndx
					toIndex--;
				 
					Fork f = Fork.LIST.remove(fromIndex);
					Fork.LIST.add(toIndex, f);
				}
				ForkView.update();
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
		
		JMenuItem update = new SwingEX.JMI("Update", 	Ico.DOLLAR,  	() -> new Thread(ForkView::updatePrices).start());
		update.setToolTipText("from xchforks.com");
		
		POPUP_MENU.add(update);
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Refresh",	Ico.REFRESH,  	ForkView::refresh));
		POPUP_MENU.add(new SwingEX.JMI("Hide", 		Ico.HIDE,  		ForkView::removeSelected));
		POPUP_MENU.add(new SwingEX.JMI("Show Peers",Ico.P2P,		() -> getSelected().forEach(Fork::showConnections)));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Debug",		Ico.BUG,		() -> getSelected().forEach(Fork::showLastException)));
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));
		
		SwingUtil.addToolTipCol(TABLE,1,i -> {return Fork.LIST.get(i).name;});
		
		SwingUtil.setColRight(TABLE,3);
		
		TABLE.setDragEnabled(true);
		TABLE.setDropMode(DropMode.INSERT_ROWS);
		TABLE.setTransferHandler(new TableRowTransferHandler(TABLE));
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
		Map<String,Double> priceMap = FFUtil.getPrices();
		
		for(Entry<String,Double> priceEntry : priceMap.entrySet()) {
			String forkName = priceEntry.getKey();
			
			Fork.LIST.stream()
				.filter(f -> f.name.equals(forkName)).findAny()
				.ifPresent(f -> f.price = priceEntry.getValue());
		}
		
		update();
		
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
		JPanel logPanel = new JPanel(new BorderLayout());
		JTabbedPane JTP = new JTabbedPane();
		logPanel.add(JTP,BorderLayout.CENTER);
		
		for (Fork f : getSelected()) {
			File logFile = new File(f.logPath);
			JTextArea jta = new JTextArea();
			JScrollPane JSP = new JScrollPane(jta);
			JSP.setPreferredSize(new Dimension(1200,500));
			JTP.addTab(f.name + " log",f.ico,JSP);
			
			ReversedLinesFileReader lr = null;
			try {
				lr = new ReversedLinesFileReader(logFile,Charset.defaultCharset());
				List<String> SL = lr.readLines(50);
				String output = String.join("\n", SL);
				jta.setText(output);
			} catch (IOException e1) {
				e1.printStackTrace();
			};
			Util.closeQuietly(lr);
		}
		
		ForkFarmer.showPopup("LogView:", logPanel);
		
	}
	
	static private void copy() {
		String addrs = getSelected().stream()
				.map(f -> f.addr).filter(Objects::nonNull)
				.collect(Collectors.joining("\n"));
		Util.copyToClip(addrs);
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
			MODEL.fireTableCellUpdated(row, 18); // time
			MODEL.fireTableCellUpdated(row, 19); // status
		});
	}
	
	public static void updateBalance(Fork f) {
		f.getIndex().ifPresent(row -> {
			MODEL.fireTableCellUpdated(row, 3); // balance
		});
	}

	public static void updateNewTx(Fork f) {
		f.getIndex().ifPresent(row -> {
			MODEL.fireTableCellUpdated(row, 14); //last win
		});
	}
}

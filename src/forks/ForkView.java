package forks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JMenu;
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
import types.Wallet;
import util.FFUtil;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.apache.ReversedLinesFileReader;
import util.swing.Reorderable;
import util.swing.SwingEX;
import util.swing.SwingEX.LTPanel;
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
		int balIndex, rewIndex, fnIndex, wnIndex, tIndex, lwIndex;
		
		public ForkTableModel() {
			super();
		
			addColumn("",   		22,	Icon.class,		f->f.ico).showMandatory();
			addColumn("Symbol",   	50,	String.class, 	f->f.symbol).show();
			addColumn("Name",   	80,	String.class, 	f->f.name);
			addColumn("Balance",	120,Balance.class, 	f->f.balance).show().index(i -> balIndex=i);
			addColumn("$",			60, Double.class, 	f->f.price).show().editable();
			//addColumn("Est. $/Mth",	80, Double.class, 	f->f.estEarn);
			addColumn("Netspace",	80, NetSpace.class, f->f.netSpace).show();
			addColumn("Height",		80, Balance.class,  f->f.height);
			addColumn("Farm Size",	80, NetSpace.class, f->f.plotSpace);
			addColumn("Version",	80, String.class,   f->f.version);
			addColumn("Sync",		80, String.class,   f->f.syncStatus);
			addColumn("Farm",		80, String.class,   f->f.farmStatus).show();
			addColumn("ETW",		70, TimeU.class,    f->f.etw);
			addColumn("24H Win",	60,	Double.class, 	f->f.dayWin);
			addColumn("Last Win",	90, TimeU.class, 	f->f.getPreviousWin()).index(i -> lwIndex=i);
			addColumn("Effort",		60,	Effort.class, 	Fork::getEffort);
			addColumn("Address",	-1,	Wallet.class, 	f->f.getWallet()).showMandatory();;
			addColumn("Reward",		40,	Double.class, 	f->f.rewardTrigger).index(i -> rewIndex=i).editable();
			addColumn("#W",			40,	Integer.class, 	f->f.walletList.size());
			addColumn("Time",		50,	ReadTime.class, f->f.readTime).show().index(i -> tIndex=i);
			addColumn("FN",			30,	Boolean.class, 	f->f.fullNode).index(i -> fnIndex=i).editable();
			addColumn("WN",			30,	Boolean.class, 	f->f.walletNode).index(i -> wnIndex=i).editable();
			addColumn("", 			22, Icon.class, 	f->f.statusIcon).showMandatory();
			
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> colList.get(c).isEditable());
		}
		
		public void setValueAt(Object value, int row, int col) {
			if (balIndex == col) {
				Fork.LIST.get(row).price = (double) value;
				fireTableCellUpdated(row, col);
				MainGui.updateBalance();
			} else if (rewIndex == col) {
				Fork.LIST.get(row).rewardTrigger = (double) value;
				fireTableCellUpdated(row, col);
			} else if (fnIndex == col) {
				Fork.LIST.get(row).fullNode = (boolean) value;
				fireTableCellUpdated(row, col);
			} else if (wnIndex == col) {
				Fork.LIST.get(row).walletNode = (boolean) value;
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
	
	/*
	public static class SymbolRendered extends DefaultTableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			JLabel lbl = (JLabel)c;
			//Fork.getBySymbol(lbl.getText()).ifPresent(f -> lbl.setIcon(f.ico));
			
			return this;
		}
	}
	*/
	static final JMenu ACTION_SUBMENU = new SwingEX.JMIco("Action", Ico.ACTION);
	static final JMenu WALLET_SUBMENU = new SwingEX.JMIco("Wallet", Ico.WALLET);
	static final JMenu EXPLORE_SUBMENU = new SwingEX.JMIco("Explore", Ico.EXPLORE);
	
	static final JMenuItem STAGGER_JMI = new SwingEX.JMI("Stagger", 	Ico.START,	() -> ForkView.staggerStartDialog());
	public ForkView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
	
		JSP.setPreferredSize(new Dimension(700,400));
		
		//TABLE.setComponentPopupMenu(POPUP_MENU);
		
		TABLE.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent me) {
				if (me.isPopupTrigger()) {
					List<Fork> sel = getSelected();

					if (sel.size() < 2) {
						STAGGER_JMI.setEnabled(false);
					} else {
						WALLET_SUBMENU.setEnabled(false);
						STAGGER_JMI.setEnabled(true);
					}
					
					if (1 == sel.size()) {
						Fork f = sel.get(0);
		            	WALLET_SUBMENU.removeAll();
		            	for (int i = 0; i < f.walletList.size(); i++) {
		            		WALLET_SUBMENU.setEnabled(true);
		            		Wallet w = f.walletList.get(i);
		            		WALLET_SUBMENU.add(new SwingEX.JMI(w.index + ") " + w.fingerprint + ": " + w.addr, 	Ico.WALLET, () -> {
		            			new Thread(() -> {
		            				f.balance = new Balance();
		            				f.farmStatus = "";
		            				f.syncStatus = "";
		            				f.wallet = w;
		            				update(f);
		            				f.loadWallet();
		            			}).start();
		            		}));
		            	}
			            	
					} 
		
		            POPUP_MENU.show(TABLE, me.getX(), me.getY());
				}
			}
		});
		
		
		
		POPUP_MENU.add(ACTION_SUBMENU);
		ACTION_SUBMENU.add(new SwingEX.JMI("Start", 	Ico.START, 	() -> getSelected().forEach(Fork::start)));
		ACTION_SUBMENU.add(STAGGER_JMI);
		ACTION_SUBMENU.add(new SwingEX.JMI("Stop",		Ico.STOP,  	() -> getSelected().forEach(Fork::stop)));
		ACTION_SUBMENU.add(new SwingEX.JMI("Custom",	Ico.CLI, 	ForkView::customCommand));
		
		POPUP_MENU.add(WALLET_SUBMENU);
		
		POPUP_MENU.add(EXPLORE_SUBMENU);
		EXPLORE_SUBMENU.add(new SwingEX.JMI("View Log", 	Ico.CLIPBOARD,  		ForkView::viewLog));
		EXPLORE_SUBMENU.add(new SwingEX.JMI("Open Config", 	Ico.CLIPBOARD,  		() -> getSelected().forEach(Fork::openConfig)));
		
		POPUP_MENU.addSeparator();
		
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
		//POPUP_MENU.add(new SwingEX.JMI("Debug",		Ico.BUG,		ForkView::reorderTest));
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));
		
		SwingUtil.setColRight(TABLE,MODEL.balIndex);
		SwingUtil.setColRight(TABLE,MODEL.tIndex);
		 
		//TABLE.getColumnModel().getColumn(1).setCellRenderer(new SymbolRendered());
		
		TABLE.setDragEnabled(true);
		TABLE.setDropMode(DropMode.INSERT_ROWS);
		TABLE.setTransferHandler(new TableRowTransferHandler(TABLE));
	}
	
	static private void customCommand() {
		List<Fork> selList = getSelected();
		JPanel customPanel = new JPanel(new GridLayout(selList.size() > 1 ? 3 : 2 ,1));
		JCheckBox updateChk = new JCheckBox("Force GUI update after command");
		
		LTPanel cmdP = new SwingEX.LTPanel("CMD: " , "");
		LTPanel staggerP = new SwingEX.LTPanel("Stagger (ms): " , "0");
		
		customPanel.add(cmdP);
		if (selList.size() > 1)
			customPanel.add(staggerP);
		customPanel.add(updateChk);
		
		if (false == ForkFarmer.showPopup("Custom Command:", customPanel))
			return;
		
		long sleepDelay = Long.parseLong(staggerP.field.getText());

		new Thread( () -> {
			for (Fork f : selList) {
				String[] args = cmdP.field.getText().split(" ");
				
				String[] varArgs = new String[args.length + 1];
				varArgs[0] = f.exePath;
				for (int i = 0; i < args.length; i++)
					varArgs[i+1] = args[i];
			
				Util.runProcessWait(varArgs);
				if (updateChk.isSelected())
					f.loadWallet();
				Util.sleep(sleepDelay);
			}
		}).start();
	}
	
	/*
	static private void reorderTest() {
		TableColumnModel tcm = TABLE.getColumnModel();
	    for (int i = 0; i < MODEL.getColumnCount() - 1; i++) {
	            int location = tcm.getColumnIndex(MODEL.getColumnName(i));
	            System.out.println("LOC: " + location);
	            //tcm.moveColumn(location, i);
	   }
	}*/
	
	static private void staggerStartDialog() {
		String delay= JOptionPane.showInputDialog(ForkFarmer.FRAME,"Enter Start Interval (Seconds)", "60");
		
		if (null == delay)
			return;
		
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
		try {
			Map<String,Double> priceMap = FFUtil.getPrices();
			
			for(Entry<String,Double> priceEntry : priceMap.entrySet()) {
				String forkName = priceEntry.getKey();
				
				Fork.LIST.stream()
					.filter(f -> f.name.toLowerCase().equals(forkName.toLowerCase())).findAny()
					.ifPresent(f -> f.price = priceEntry.getValue());
			}
		} catch (Exception e) {
			
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
			jta.setEditable(false);
			JScrollPane JSP = new JScrollPane(jta);
			JSP.setPreferredSize(new Dimension(1000,800));
			JTP.addTab(f.name + " log",f.ico,JSP);
			
			ReversedLinesFileReader lr = null;
			try {
				lr = new ReversedLinesFileReader(logFile,Charset.defaultCharset());
				String s;
				int i =0;
				StringBuilder sb = new StringBuilder();
				while (null != (s = lr.readLine()) && i < 200) {
					i++;
					sb.append(s + "\n");
				}
				jta.setText(sb.toString());
				jta.setCaretPosition(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			};
			Util.closeQuietly(lr);
		}
		
		ForkFarmer.showPopup("LogView:", logPanel);
		
	}
	
	static private void copy() {
		String addrs = getSelected().stream()
				.map(f -> f.wallet.toString()).filter(Objects::nonNull)
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
	
	public static void updateBalance(Fork f) {
		f.getIndex().ifPresent(row -> {
			MODEL.fireTableCellUpdated(row, MODEL.balIndex);
		});
	}
	
	public static void logReader() {
		List<Fork> logList = new ArrayList<>(Fork.LIST);
		
		while(true) {
			for (Fork f: logList) {
				f.readLog();
				Util.sleep(5000);
			}
		}
	}
	
}

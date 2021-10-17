package forks;

import java.awt.BorderLayout;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.JTableHeader;

import ffutilities.ForkLogViewer;
import ffutilities.PortCheckerView;
import main.ForkFarmer;
import main.MainGui;
import main.Settings;
import peer.PeerView;
import types.Balance;
import types.Effort;
import types.ReadTime;
import types.TimeU;
import types.Wallet;
import types.XchForksData;
import util.FFUtil;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.swing.Reorderable;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.TableRowTransferHandler;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	public static final ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	
	public static class ForkTableModel extends JFunTableModel<Fork> implements Reorderable {
		int SYM_COLUMN, BAL_COLUMN, EQ_COLUMN, HEIGHT_COLUMN, TIME_COLUMN, LIGHT_COLUMN;
		
		public ForkTableModel() {
			super();
			
			@SuppressWarnings("unchecked")
			List<Col<Fork>> z = (List<Col<Fork>>) Settings.settings.get("ForkView Columns");
			loadColumns(z);
			
			addColumn(" ",   		22,	Icon.class,		f->f.ico).showMandatory();
			addColumn("Symbol",  	50,	String.class, 	f->f.symbol).show(true);
			addColumn("Name",   	80,	String.class,	f->f.name);
			addColumn("Balance",	100,Balance.class,	f->f.balance).show(true);
			addColumn("$",			60, Double.class, 	f->f.price).show(true).editable();
			addColumn("Equity",		60, Balance.class, 	f->f.equity);
			addColumn("Netspace",	80, NetSpace.class, f->f.netSpace).show(true);
			addColumn("Height",		80, Balance.class,  f->f.height);
			addColumn("Farm Size",	80, NetSpace.class, f->f.plotSpace);
			addColumn("Version",	80, String.class,   f->f.version);
			addColumn("Latest Ver",	80, String.class,   f->f.latestVersion);
			addColumn("Published",	80, String.class,   f->f.published);
			addColumn("Sync",		80, String.class,   f->f.syncStatus);
			addColumn("Farm",		80, String.class,   f->f.farmStatus).show(true);
			addColumn("ETW",		70, TimeU.class,    f->f.etw);
			addColumn("24H Win",	60,	Double.class, 	f->f.dayWin);
			addColumn("Last Win",	90, TimeU.class, 	f->f.getPreviousWin());
			addColumn("Effort",		60,	Effort.class, 	Fork::getEffort);
			addColumn("Address",	-1,	Wallet.class, 	f->f.wallet).showMandatory();
			addColumn("Reward",		40,	Double.class, 	f->f.rewardTrigger).editable();
			addColumn("#W",			40,	Integer.class, 	f->f.walletList.size());
			addColumn("Time",		50,	ReadTime.class, f->f.readTime).show(true);
			addColumn("FN",			30,	Boolean.class, 	f->f.fullNode).editable();
			addColumn("WN",			30,	Boolean.class, 	f->f.walletNode).editable();
			addColumn("", 			22, Icon.class, 	f->f.statusIcon).showMandatory();
			
			SYM_COLUMN = getIndex("Symbol");
			BAL_COLUMN = getIndex("Balance");
			EQ_COLUMN = getIndex("Equity");

			// these updated alot
			HEIGHT_COLUMN = getIndex("Height");
			TIME_COLUMN = getIndex("Time");
			LIGHT_COLUMN = getIndex("");
			
			onGetRowCount(() -> Fork.LIST.size());
			onGetValueAt((r, c) -> colList.get(c).apply(Fork.LIST.get(r)));
			onisCellEditable((r, c) -> colList.get(c).isEditable());
		}
		
		public void removeRow(int row) {
	        Fork.LIST.remove(row);
	    }
		
		public void setValueAt(Object value, int row, int col) {
			if (BAL_COLUMN == col) {
				Fork.LIST.get(row).updatePrice((double) value);
				fireTableCellUpdated(row, col);
				MainGui.updateTotal();
			} else if (getIndex("Reward") == col) {
				Fork.LIST.get(row).rewardTrigger = (double) value;
				fireTableCellUpdated(row, col);
			} else if (getIndex("FN") == col) {
				Fork.LIST.get(row).fullNode = (boolean) value;
				fireTableCellUpdated(row, col);
			} else if (getIndex("WN") == col) {
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
	static final JMenu COPY_SUBMENU = new SwingEX.JMIco("Copy", Ico.CLIPBOARD);
	static final JMenu STATS_SUBMENU = new SwingEX.JMIco("Stats", Ico.GRAPH);
	static final JMenu EXCHANGE_SUBMENU = new SwingEX.JMIco("Exchange", Ico.HANDSHAKE);
	
	static final JMenuItem STAGGER_JMI = new SwingEX.JMI("Stagger", 	Ico.START,	() -> ForkView.staggerStartDialog());
	public ForkView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
	
		SwingUtil.persistDimension(JSP, () -> Settings.GUI.forkViewDimension, d -> Settings.GUI.forkViewDimension = d);
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		
		POPUP_MENU.addPopupMenuListener(new PopupMenuListener() {
			@Override public void popupMenuCanceled(PopupMenuEvent pme) {
				
			};
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
				
			}
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
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
	            				f.walletAddr = w.addr;
	            				update(f);
	            				f.loadWallet();
	            			}).start();
	            		}));
	            	}
		            	
				} 
			}
		});
		
		
		POPUP_MENU.add(ACTION_SUBMENU);
			ACTION_SUBMENU.add(new SwingEX.JMI("Start", 		Ico.START, 	() -> new Thread(() -> getSelected().forEach(Fork::start)).start()));
			ACTION_SUBMENU.add(STAGGER_JMI);
			ACTION_SUBMENU.add(new SwingEX.JMI("Stop",			Ico.STOP,  	() -> new Thread(() -> getSelected().forEach(Fork::stop)).start()));
			ACTION_SUBMENU.add(new SwingEX.JMI("Custom",		Ico.CLI, 	() -> ForkStarter.newCustCMD(getSelected())));
			ACTION_SUBMENU.addSeparator();
			ACTION_SUBMENU.add(new SwingEX.JMI("Edit Start",	Ico.EDIT_START, 	ForkStarter::edit));
		
		POPUP_MENU.add(WALLET_SUBMENU);
		
		POPUP_MENU.add(EXPLORE_SUBMENU);
			EXPLORE_SUBMENU.add(new SwingEX.JMI("View Log", 	Ico.CLIPBOARD,  		() -> new ForkLogViewer(getSelected())));
			EXPLORE_SUBMENU.add(new SwingEX.JMI("Open Config", 	Ico.CLIPBOARD,  		() -> getSelected().forEach(Fork::openConfig)));
			if (Util.isHostWin())
				EXPLORE_SUBMENU.add(new SwingEX.JMI("Open Shell", 		Ico.CLI,  		ForkView::openShell));
		
		POPUP_MENU.add(COPY_SUBMENU);
			COPY_SUBMENU.add(new SwingEX.JMI("Copy Address", 	Ico.CLIPBOARD,  ForkView::copyAddress));
			COPY_SUBMENU.add(new SwingEX.JMI("Copy CSV", 		Ico.CLIPBOARD,  ForkView::copyCSV));
		
		POPUP_MENU.add(STATS_SUBMENU);
			STATS_SUBMENU.add(new SwingEX.JMI("Ports", 	Ico.PORTS, ForkView::runPortChecker));
			
		POPUP_MENU.addSeparator();
		
		POPUP_MENU.add(EXCHANGE_SUBMENU);
			EXCHANGE_SUBMENU.add(new SwingEX.JMI("xchforks.com", 			Ico.XCHF,() -> Util.openLink("https://xchforks.com/")));
			EXCHANGE_SUBMENU.addSeparator();
			EXCHANGE_SUBMENU.add(new SwingEX.JMI("forkschiaexchange.com", 	Ico.FCX, () -> Util.openLink("https://forkschiaexchange.com/?ref=orfinkat")));
		
		JMenuItem update = new SwingEX.JMI("Update", 	Ico.DOLLAR,  	() -> new Thread(ForkView::updatePrices).start());
		update.setToolTipText("from xchforks.com");
		
		POPUP_MENU.add(update);
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Refresh",	Ico.REFRESH,  	ForkView::refresh));
		POPUP_MENU.add(new SwingEX.JMI("Hide", 		Ico.HIDE,  		ForkView::removeSelected));
		POPUP_MENU.add(new SwingEX.JMI("Show Peers",Ico.P2P,		() -> getSelected().forEach(f -> 
			ForkFarmer.newFrame(f.name + ": Peer Connections", f.ico, new PeerView(f)))));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Debug",		Ico.BUG,		() -> getSelected().forEach(Fork::showLastException)));
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));
		
		SwingUtil.setColRight(TABLE,MODEL.BAL_COLUMN);
		SwingUtil.setColRight(TABLE,MODEL.TIME_COLUMN);
		SwingUtil.setColRight(TABLE,MODEL.EQ_COLUMN);
		 
		//TABLE.getColumnModel().getColumn(1).setCellRenderer(new SymbolRendered());
		
		TABLE.setDragEnabled(true);
		TABLE.setDropMode(DropMode.INSERT_ROWS);
		TABLE.setTransferHandler(new TableRowTransferHandler(TABLE));
	}
	
	static private void staggerStartDialog() {
		String delay = JOptionPane.showInputDialog(ForkFarmer.FRAME,"Enter Start Interval: (Seconds)", "60");
		
		if (null == delay)
			return;
		int delayInt = Integer.parseInt(delay);

		new Thread(() -> {
			getSelected().stream().forEach(f -> {
				ForkStarter.start(f); Util.sleep(delayInt * 1000);
			});
		}).start();
	}
	
	static private void updatePrices() {
		
		int jversion = Util.getJavaVersion();
		if (jversion < 11) {
			ForkFarmer.showMsg("Please update Java Runtime", 
					"Current java version is " + jversion + ". 11+ required for this feature \n" +
					"Please download new java release from https://www.oracle.com/java/technologies/downloads/"
			);
			return;
		}
		
		try {
			List<XchForksData> list = FFUtil.getXCHForksData();
			
			for(XchForksData d : list) {
				Fork.getBySymbol(d.symbol).ifPresent(f -> {
					if (d.price > -1)
						f.updatePrice(d.price * Settings.GUI.currencyRatio);
					if (null != d.latestVersion && !d.latestVersion.equals("Unknown")) {
						f.latestVersion = d.latestVersion;
						f.published = d.published;
					}
				});
			}
		} catch (Exception e) {
			
		}
		
		MainGui.updateTotal();
	}
	
	static private void refresh() {
		getSelected().forEach(Fork::refresh);
	}
	
	static private void removeSelected() {
		List<Fork> selList = getSelected();
		for (Fork f : selList) {
			f.hidden = true;
			SwingUtilities.invokeLater(() -> {
				MODEL.removeRow(f.getIndex());	
			});
			
		}
		
		
		MainGui.updateNumForks();
		update();
	}

	static private void copyAddress() {
		String addrs = getSelected().stream()
				.map(f -> f.wallet.toString()).filter(Objects::nonNull)
				.collect(Collectors.joining("\n"));
		Util.copyToClip(addrs);
	}
	
	static private void copyCSV() {
		StringBuilder sb = new StringBuilder();
		sb.append("Symbol,Balance,$,ETW\n");
		for (Fork f : getSelected()) {
			sb.append(Util.toString(MODEL.colList.get(MODEL.getIndex("Symbol")).getValue.apply(f)) + ",");
			sb.append(((Balance)(MODEL.colList.get(MODEL.getIndex("Balance")).getValue.apply(f))).amt +",");
			sb.append(((Balance)(MODEL.colList.get(MODEL.getIndex("$")).getValue.apply(f))).amt +",");
			sb.append(Util.toString(MODEL.colList.get(MODEL.getIndex("ETW")).getValue.apply(f)) + "\n");
		}
		
		Util.copyToClip(sb.toString());
	}
	
	static private void openShell() {
		for (Fork f : getSelected()) {
			String path = f.exePath;
			String nativeDir = path.substring(0, path.lastIndexOf(File.separator));
			try {
				Runtime.getRuntime().exec("cmd /c start cmd.exe /K " + "cd " + nativeDir);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
		}
	}
	
	private static List<Fork> getSelected() {
		return SwingUtil.getSelected(TABLE, Fork.LIST);
	}

	public static void update() {
		SwingUtilities.invokeLater ( () -> { 
			MODEL.fireTableDataChanged();
		});
	}
	
	public static void update(Fork f) {
		SwingUtilities.invokeLater(() -> {
			int row = f.getIndex();
			if (-1 != row) {
				f.updateIcon();
				MODEL.fireTableRowsUpdated(row, row);
			}
		});
	}
	
	public static void updateLog(Fork f) {
		SwingUtilities.invokeLater(() -> {
			int row = f.getIndex();
			if (-1 != row) {
				f.updateIcon();
				MODEL.fireTableCellUpdated(row, MODEL.TIME_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.LIGHT_COLUMN);
				MODEL.fireTableCellUpdated(row, MODEL.HEIGHT_COLUMN);
			}
		}); 
	}
	
	public static void logReader() {
		while(true) {
			for (Fork f: Fork.I_LIST) {
				if (false == f.hidden) {
					f.readLog();
					Util.sleep(Settings.GUI.logReaderIntraDelay);
				}
			}
			Util.sleep(Settings.GUI.logReaderExoDelay);
		}
	}
	
	private static void runPortChecker() {
		Fork.LIST.forEach(Fork::loadConfig);
		ForkFarmer.newFrame("Port Checker", Ico.PORTS, new PortCheckerView());
	}
	
	public static void daemonReader() {
		ExecutorService SVC = Executors.newFixedThreadPool(Settings.GUI.daemonReaderWorkers);
		
		try {
			// initial load
			for (Fork f: Fork.I_LIST) {
				SVC.submit(() -> {
						f.loadVersion();
						f.loadWallets();
						f.stdUpdate();  // -> Wallet/Farm Summary/GUI
				});
			}
	
			// main GUI refresh loop
			while(true) {
				for (Fork f: Fork.I_LIST) {
					SVC.submit(() -> f.stdUpdate()); 
					Util.blockUntilAvail(SVC);
					Util.sleep(Settings.GUI.daemonReaderDelay);
				}
			}
		} catch (Exception e) {
			System.out.println("Serious UNCAUGHT EXCEPTION!!!!!!!!!!!!!!!!!!!!!!");
			e.printStackTrace();
		}
	}
	
}

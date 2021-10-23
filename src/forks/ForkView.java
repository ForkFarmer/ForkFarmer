package forks;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import ffutilities.ForkLogViewer;
import ffutilities.MissingForks;
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
import util.Ico;
import util.NetSpace;
import util.Util;
import util.swing.Reorderable;
import util.swing.SwingEX;
import util.swing.SwingUtil;
import util.swing.TableRowTransferHandler;
import util.swing.jfuntable.Col;
import util.swing.jfuntable.JFunTableModel;
import web.AllTheBlocks;
import web.XchForks;

@SuppressWarnings("serial")
public class ForkView extends JPanel {
	public static final ForkTableModel MODEL = new ForkTableModel();	
	public static final JTable TABLE = new JTable(MODEL);
	private static final JScrollPane JSP = new JScrollPane(TABLE,JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	private static final JPopupMenu POPUP_MENU = new JPopupMenu();
	private static final JPopupMenu HEADER_MENU = new JPopupMenu();
	private enum SHELL {CMD, POWERSHELL, TERMINAL}
	static boolean warnJava = false;
	
	public static class ForkTableModel extends JFunTableModel<Fork> implements Reorderable {
		private int BALANCE_COLUMN, PRICE_COLUMN, EQ_COLUMN, HEIGHT_COLUMN, TIME_COLUMN, LIGHT_COLUMN;
		
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
			
			BALANCE_COLUMN = getIndex("Balance");
			PRICE_COLUMN = getIndex("$");
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
			if (PRICE_COLUMN == col) {
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
	static final JMenu TOOLS_SUBMENU = new SwingEX.JMIco("Tools", Ico.TOOLS);
	static final JMenu COMMUNITY_SUBMENU = new SwingEX.JMIco("Community", Ico.PEOPLE);
	
	static final JMenuItem STAGGER_JMI = new SwingEX.JMI("Stagger", 	Ico.START,	() -> ForkView.staggerStartDialog());
	public ForkView() {
		setLayout(new BorderLayout());
		add(JSP,BorderLayout.CENTER);
	
		SwingUtil.persistDimension(JSP, () -> Settings.GUI.forkViewDimension, d -> Settings.GUI.forkViewDimension = d);
		
		TABLE.setComponentPopupMenu(POPUP_MENU);
		JSP.setComponentPopupMenu(POPUP_MENU);
		
		POPUP_MENU.addPopupMenuListener(new PopupMenuListener() {
			@Override public void popupMenuCanceled(PopupMenuEvent pme) {
				
			};
			@Override public void popupMenuWillBecomeInvisible(PopupMenuEvent pme) {
				
			}
			@Override public void popupMenuWillBecomeVisible(PopupMenuEvent pme) {
				List<Fork> sel = getSelected();

				// clean submenus
				WALLET_SUBMENU.removeAll();
				while (COMMUNITY_SUBMENU.getItemCount() > 5)
            		COMMUNITY_SUBMENU.remove(5);
            	
				if (sel.size() < 2) {
					STAGGER_JMI.setEnabled(false);
				} else {
					WALLET_SUBMENU.setEnabled(false);
					STAGGER_JMI.setEnabled(true);
				}
				
				if (1 == sel.size()) {
					Fork f = sel.get(0);
					populateMenu(f);
				} 
			}

			
			private void populateMenu(Fork f) {
            	
            	ForkData fd = ForkData.MAP.get(f.symbol);
            	if (null != fd) {
	            	if (null != fd.websiteURL)
	            		COMMUNITY_SUBMENU.add(new SwingEX.JMI(f.name + " Homepage", 	Ico.HOME, () -> Util.openLink(fd.websiteURL)));
            		if (null != fd.discordURL)
	            		COMMUNITY_SUBMENU.add(new SwingEX.JMI(f.name + " Discord", 	Ico.DISCORD, () -> Util.openLink(fd.discordURL)));
	            	if (null != fd.gitURL)
	            		COMMUNITY_SUBMENU.add(new SwingEX.JMI(f.name + " GitHub", 	Ico.GITHUB, () -> Util.openLink(fd.gitURL)));
	            	if (null != fd.calculatorURL)
	            		COMMUNITY_SUBMENU.add(new SwingEX.JMI(f.name + " Calculator",	Ico.XCHCALC, () -> Util.openLink(fd.calculatorURL)));
            	}
            	
            		
            	
            	for (int i = 0; i < f.walletList.size(); i++) {
            		WALLET_SUBMENU.setEnabled(true);
            		Wallet w = f.walletList.get(i);

            		if (w.cold) {
            			JMenuItem jmi = new SwingEX.JMI("0) " + w.addr, 	Ico.WALLET_COLD, () ->  {
            				f.wallet = w;
            				f.walletAddr = w.addr;
            				AllTheBlocks.updateColdBalance(f);
            			});
            			((Component)jmi).setForeground(new Color(140,171,255));
            			WALLET_SUBMENU.add(jmi);
            		} else {
	            		WALLET_SUBMENU.add(new SwingEX.JMI(w.index + ") " + w.fingerprint + ": " + w.addr, 	Ico.WALLET, () -> {
	            			new Thread(() -> {
	            				f.balance = new Balance();
	            				f.syncStatus = "";
	            				f.wallet = w;
	            				f.walletAddr = w.addr;
	            				update(f);
	            				f.loadWallet();
	            			}).start();
	            		}));
            		}
            	}
            	
            	if (WALLET_SUBMENU.getItemCount() > 0)
            		WALLET_SUBMENU.addSeparator();
            	WALLET_SUBMENU.add(new SwingEX.JMI("Add Cold Wallet",	Ico.WALLET_COLD,	() -> addColdWallet(f)));
            	
	
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
			if (Util.isHostWin()) {
				EXPLORE_SUBMENU.add(new SwingEX.JMI("Open CMD", 		Ico.CLI,  			() -> ForkView.openShell(SHELL.CMD)));
				EXPLORE_SUBMENU.add(new SwingEX.JMI("Open Powershell",	Ico.POWERSHHELL,  	() -> ForkView.openShell(SHELL.POWERSHELL)));
			} else {
				EXPLORE_SUBMENU.add(new SwingEX.JMI("Open Terminal", 		Ico.TERMINAL,  		() -> ForkView.openShell(SHELL.TERMINAL)));
			}
			
		POPUP_MENU.add(COPY_SUBMENU);
			COPY_SUBMENU.add(new SwingEX.JMI("Copy Address", 	Ico.CLIPBOARD,  ForkView::copyAddress));
			COPY_SUBMENU.add(new SwingEX.JMI("Copy CSV", 		Ico.CLIPBOARD,  ForkView::copyCSV));
		
		POPUP_MENU.add(TOOLS_SUBMENU);
			TOOLS_SUBMENU.add(new SwingEX.JMI("Ports", 	Ico.PORTS, ForkView::runPortChecker));
			TOOLS_SUBMENU.add(new SwingEX.JMI("Missing",Ico.QUESTION, () -> ForkFarmer.newFrame("Missing Forks", Ico.QUESTION, new MissingForks())));
			JMenuItem update = new SwingEX.JMI("Force Update", 	Ico.DOLLAR,  	() -> new Thread(ForkView::webUpdateForced).start());
			update.setToolTipText("from xchforks.com / alltheblocks.net");
			TOOLS_SUBMENU.add(update);
			
		POPUP_MENU.add(COMMUNITY_SUBMENU);
			COMMUNITY_SUBMENU.add(new SwingEX.JMI("xchforks.com", 			Ico.XCHF,() -> Util.openLink("https://xchforks.com/")));
			COMMUNITY_SUBMENU.add(new SwingEX.JMI("alltheblocks.net", 		Ico.ATB, () -> Util.openLink("https://alltheblocks.net/")));
			COMMUNITY_SUBMENU.add(new SwingEX.JMI("forkschiaexchange.com", 	Ico.FCX, () -> Util.openLink("https://forkschiaexchange.com/?ref=orfinkat")));
			COMMUNITY_SUBMENU.add(new SwingEX.JMI("chiaforksblockchain.com", Ico.DOWNLOAD, () -> Util.openLink("https://chiaforksblockchain.com/")));
			COMMUNITY_SUBMENU.addSeparator();
			
		POPUP_MENU.addSeparator();
		
		
		POPUP_MENU.add(new SwingEX.JMI("Add Cold Wallet",	Ico.SNOW,  	() -> ForkView.addColdWallet(null)));
		POPUP_MENU.add(new SwingEX.JMI("Refresh",	Ico.REFRESH,  	ForkView::refresh));
		POPUP_MENU.add(new SwingEX.JMI("Hide", 		Ico.HIDE,  		ForkView::removeSelected));
		POPUP_MENU.add(new SwingEX.JMI("Show Peers",Ico.P2P,		() -> getSelected().forEach(f -> 
			ForkFarmer.newFrame(f.name + ": Peer Connections", f.ico, new PeerView(f)))));
		POPUP_MENU.addSeparator();
		POPUP_MENU.add(new SwingEX.JMI("Debug",			Ico.BUG,		() -> getSelected().forEach(Fork::showLastException)));
		
		
		JTableHeader header = TABLE.getTableHeader();
		header.setComponentPopupMenu(HEADER_MENU);
		
		MODEL.colList.forEach(c -> c.setSelectView(TABLE,HEADER_MENU));
		
		SwingUtil.setColRight(TABLE,MODEL.BALANCE_COLUMN);
		SwingUtil.setColRight(TABLE,MODEL.TIME_COLUMN);
		SwingUtil.setColRight(TABLE,MODEL.EQ_COLUMN);
		 
		//TABLE.getColumnModel().getColumn(1).setCellRenderer(new SymbolRendered());
		
		TABLE.setDragEnabled(true);
		TABLE.setDropMode(DropMode.INSERT_ROWS);
		TABLE.setTransferHandler(new TableRowTransferHandler(TABLE));
		
		DefaultTableCellRenderer balanceRendererR = new DefaultTableCellRenderer(){
	        @Override
	        public Component getTableCellRendererComponent(JTable table,Object value,boolean isSelected,boolean hasFocus,int row,int column) {
	            Component c = super.getTableCellRendererComponent(table,value,isSelected,hasFocus,row,column);

	            c.setForeground(Color.WHITE);
	            Fork f = Fork.LIST.get(row);
	            Wallet w =  f.wallet;
	            if (f.cold || (null != w && w.cold))
	            	c.setForeground(new Color(140,171,255));
	
	            return c;
	        }
	    };
	    balanceRendererR.setHorizontalAlignment(JLabel.RIGHT);
		
		TABLE.getColumnModel().getColumn(MODEL.BALANCE_COLUMN).setCellRenderer(balanceRendererR);
	}
	
	static private void addColdWallet(Fork f) {
		JPanel cwPanel = new JPanel(new BorderLayout());
		JTextPane jtp = new JTextPane();
		JScrollPane JSP = new JScrollPane(jtp);
		jtp.setPreferredSize(new Dimension(500,200));
		cwPanel.add(new JLabel("Enter one address per line"), BorderLayout.PAGE_START);
		cwPanel.add(JSP,BorderLayout.CENTER);
		
		if (ForkFarmer.showPopup("Cold wallet data comes from www.alltheblocks.net", cwPanel)) {
			String[] addrArray = jtp.getText().split(System.lineSeparator());
			
			new Thread(() -> {
				for (String addr : addrArray) {
					addr = addr.toLowerCase();
					if (null == f)
						Fork.newColdWallet(addr);
					else {
						if (!addr.startsWith(f.symbol.toLowerCase()))
							return;
						f.walletAddr = addr;
						f.wallet = new Wallet(addr);
						f.walletList.add(f.wallet);
						f.coldAddrList.add(addr);
					}
				}
				Util.sleep(2000);
				AllTheBlocks.updateColdForced();
			}).start();
			
		}
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
	
	public static boolean javaOld() {
		int jversion = Util.getJavaVersion();
		if (!warnJava && jversion < 11) {
			ForkFarmer.showMsg("Please update Java Runtime", 
					"Current java version is " + jversion + ". Java 11+ required for many features in FF\n" +
					"Please download new java release from https://www.oracle.com/java/technologies/downloads/"
			);
			warnJava = true;
			return true;
		}
		return false;
	}
	
	static private void webUpdateForced() {
		if (!javaOld()) {
			XchForks.updatePricesForced();
			AllTheBlocks.updateColdForced();
		}
	}
	
	static private void webUpdate() {
		if (javaOld() || !Settings.GUI.autoUpdate)
			return;
				
		XchForks.updatePrices();
		AllTheBlocks.updateColdBalances();
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
	
	static private void openShell(SHELL s) {
		for (Fork f : getSelected()) {
			String path = f.exePath;
			String nativeDir = path.substring(0, path.lastIndexOf(File.separator));
			try {
				if (SHELL.POWERSHELL == s)
					Runtime.getRuntime().exec("cmd /c start powershell.exe -noexit -command " + "cd " + nativeDir);
				else if (SHELL.CMD == s)
					Runtime.getRuntime().exec("cmd /c start cmd.exe /K " + "cd " + nativeDir);
				else if (SHELL.TERMINAL == s)
					Runtime.getRuntime().exec("gnome-terminal --working-directory=" + nativeDir);
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
			webUpdate();
			for (Fork f: Fork.I_LIST) {
				SVC.submit(() -> {
						f.loadVersion();
						f.loadWallets();
						f.stdUpdate();  // -> Wallet/Farm Summary/GUI
				});
			}
	
			// main GUI refresh loop
			while(true) {
				webUpdate();
				for (Fork f: Fork.I_LIST) {
					SVC.submit(() -> f.stdUpdate()); 
					Util.blockUntilAvail(SVC);
					Util.sleep(Settings.GUI.daemonReaderDelay);
				}
				Util.sleep(1000);
				
			}
		} catch (Exception e) {
			System.out.println("Serious UNCAUGHT EXCEPTION!!!!!!!!!!!!!!!!!!!!!!");
			e.printStackTrace();
		}
	}
	
}

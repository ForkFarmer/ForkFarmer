package forks;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.table.AbstractTableModel;

import main.ForkFarmer;
import main.MainGui;
import transaction.Transaction;
import util.Ico;
import util.Util;
import util.process.ProcessPiper;

public class Fork {
	static int NUM_THREADS = 20;
	public static ExecutorService SVC = Executors.newFixedThreadPool(NUM_THREADS);
	
	public static final List<Fork> LIST = new ArrayList<>();
	double balance;
	
	private static final String icoPath = "icons/forks/";
	private static final ImageIcon GREEN 	= Ico.loadIcon("icons/circles/green.png");
	private static final ImageIcon RED 		= Ico.loadIcon("icons/circles/red.png");
	private static final ImageIcon GRAY		= Ico.loadIcon("icons/circles/gray.png");
	private static final ImageIcon COMPLETE = Ico.loadIcon("icons/check.png");
	
	private	final String symbol;
	public final String exePath;
	public final String name;
	private ImageIcon ico;
	
	public ImageIcon sIco = GRAY;
	public String addr;
		
	public Fork(String symbol, String name, String exePath) {
		this.symbol = symbol;
		this.name = name;
		this.exePath = exePath;

		try {
			ico = Ico.loadIcon(icoPath + name + ".png",16);
		} catch (RuntimeException e) {
			ico = Ico.loadIcon(icoPath + name + ".jpg",16);
		} 
	}
	
	public String getSymbol() {
		return symbol;
	}
	
	public Icon getIcon() {
		return ico;
	}
	
	public String getAddr() {
		return addr;
	}
	
	public double getType() {
		return balance;
	};
	
	
	public void completedSuccess() {
		sIco = COMPLETE;
		((AbstractTableModel)ForkView.MODEL).fireTableDataChanged();
	}
	
	public void loadWallet () {
		String addr = ProcessPiper.run(exePath,"wallet","show");
		
		if (null == addr)
			return;
		if (addr.startsWith("Connection"))
			return;
		
		//System.out.println("Address: " + addr);
		
		String[] lines = addr.split(System.getProperty("line.separator"));
		
		if (lines.length < 6)
			return;
		
		String str = lines[5];
		str = str.substring(str.indexOf("Total Balance: ") + 15);
		str = str.substring(0,str.indexOf(" "));
		balance = Double.parseDouble(str);
		
		updateView();
		this.addr = Transaction.load(symbol,exePath);
		updateView();
		
		sIco = GREEN;
	}
	
	private void updateView() {
		getIndex().ifPresent(ForkView::fireTableRowUpdated);
	}
	
	public void start() {
		SVC.submit(() -> {
			ProcessPiper.run(exePath,"start","farmer");
			loadWallet();	
		});
		
	}
	
	public void stop() {
		SVC.submit(() -> {
			ProcessPiper.run(exePath,"stop","farmer");
			sIco = RED;
			updateView();
		});
	}
	

	public static void factory(String symbol, String name) {
		String basePath = System.getProperty("user.home") + "\\AppData\\Local\\";
		String forkBase = basePath + name.toLowerCase() + "-blockchain\\";
		MainGui.pBar.setString("Base: " + forkBase);
		String appPath;
		try {
			appPath = Util.getDir(forkBase, "app");
			String exePath = forkBase + appPath + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
			
			LIST.add(new Fork(symbol, name, exePath));
			MainGui.pBar.setString("Done add");
		} catch (IOException e) {
			// failed to load fork for whatever reason
		}
		
	}
	
	public Optional<Integer> getIndex() {
		synchronized (LIST) {
			int idx = LIST.indexOf(this);
			return (-1 != idx) ? Optional.of(idx) : Optional.empty();
		}
	}

	public void sendTX(String address, String amt, String fee) {
		String ret = ProcessPiper.run(exePath,"wallet","send","-i","1","-a",amt,"-m","0","-t",addr,"--override");
		ForkFarmer.showMsg("Send Transaction", ret);
	}


}

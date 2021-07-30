package forks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.ForkFarmer;
import transaction.Transaction;
import util.Ico;
import util.Util;
import util.apache.ReversedLinesFileReader;
import util.process.ProcessPiper;

public class Fork {
	static int NUM_THREADS = 10;
	public static ExecutorService SVC = Executors.newFixedThreadPool(NUM_THREADS);
	
	public static final List<Fork> LIST = new ArrayList<>();
	double balance = -1;
	
	private static final String icoPath = "icons/forks/";
	private static final ImageIcon GREEN 	= Ico.loadIcon("icons/circles/green.png");
	private static final ImageIcon RED 		= Ico.loadIcon("icons/circles/red.png");
	private static final ImageIcon YELLOW	= Ico.loadIcon("icons/circles/yellow.png");
	private static final ImageIcon GRAY		= Ico.loadIcon("icons/circles/gray.png");
	
	public final String symbol;
	public final String exePath;
	private final String logPath;
	public final String name;
	public ImageIcon ico;
	public ImageIcon statusIcon = GRAY;
	private double readTime;
	public String netspace = "";
	
	public String addr;
		
	public Fork(String symbol, String name, String exePath, String logPath) {
		this.symbol = symbol;
		this.name = name;
		this.exePath = exePath;
		this.logPath = logPath;

		try {
			ico = Ico.loadIcon(icoPath + name + ".png",16);
		} catch (RuntimeException e) {
			ico = Ico.loadIcon(icoPath + name + ".jpg",16);
		} 
	}
	
	public String getReadTime() {
		if (0 == readTime)
			return "";
		if (-2 == readTime)
			return "Timeout";
		DecimalFormat df = new DecimalFormat("###.##");
		return df.format(readTime);
	};
	
	public String getBalance() {
		return (-1 == balance) ? "" : Double.toString(balance);
	};
	
	public void loadWallet () {
		try {
			String addr = ProcessPiper.run(exePath,"wallet","show");
			
			if (null == addr)
				return;
			if (addr.startsWith("Connection"))
				return;
	
			String[] lines = addr.split(System.getProperty("line.separator"));
			
			if (lines.length < 6)
				return;
			for (String l : lines) {
				if (l.contains("Total Balance: ")) {
					String balanceStr = Util.getWordAfter(l, "Total Balance: ");
					balance = Double.parseDouble(balanceStr);
					break;
				}
			}
			
			this.addr = Transaction.load(symbol,exePath);
			//loadSummary();
			readLog();
			updateView();		
			
			statusIcon = (readTime <= 0 || readTime > 5) ? YELLOW : GREEN;
		} catch (Exception e) {
			statusIcon = RED;
		}
	}
	
	@SuppressWarnings("unused")
	private void loadSummary() {
		String summary = ProcessPiper.run(exePath,"farm","summary");
		String[] lines = summary.split(System.getProperty("line.separator"));
		
		for (String l : lines) {
			if (l.contains("Estimated network space: "))
				netspace = l.substring("Estimated network space: ".length());
		}
		
	}

	private void readLog() {
		
		File f = new File(logPath);
		ReversedLinesFileReader lr = null;
		try {
			lr = new ReversedLinesFileReader(f,Charset.defaultCharset());
			List<String> SL = lr.readLines(60);
			readTime = 0;
			for (String s : SL) {
				if (s.contains("Time: ")) {
					s = s.substring(s.indexOf("Time: ") + 6);
					s = s.substring(0,s.indexOf(" "));
					updateReadTime(Double.parseDouble(s));
					break;
				} else if (s.contains("took: ")) {
					s = s.substring(s.indexOf("took: ") + 6);
					s = s.substring(0,s.indexOf(" "));
					if (s.endsWith("."))
						s = s.substring(0, s.length()-1);
					updateReadTime(Double.parseDouble(s));
					break;
				} else if (s.contains("WARNING  Respond plots came too late")) {
					updateReadTime(-2);
					break;
				}
			}
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		};
		Util.closeQuietly(lr);
	}

	private void updateReadTime(double rt) {
		readTime = rt;
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
			statusIcon = RED;
			updateView();
		});
	}
	
	public void generate() {
		SVC.submit(() -> {
			addr = ProcessPiper.run(exePath,"wallet","get_address").replace("\n", "").replace("\r", "");
			updateView();
		});
	}
	
	public void viewLog() {
		JPanel logPanel = new JPanel(new BorderLayout());
		JTextArea jta = new JTextArea();
		JScrollPane JSP = new JScrollPane(jta);
		JSP.setPreferredSize(new Dimension(1200,500));
		logPanel.add(JSP,BorderLayout.CENTER);
		
		File f = new File(logPath);
		ReversedLinesFileReader lr = null;
		try {
			lr = new ReversedLinesFileReader(f,Charset.defaultCharset());
			List<String> SL = lr.readLines(30);
			String output = String.join("\n", SL);
			jta.setText(output);
		} catch (IOException e1) {
			e1.printStackTrace();
		};
		Util.closeQuietly(lr);
		
		ForkFarmer.showPopup(name + " log", logPanel);
		
	}
	
	public static void factory(String symbol, String name) {
		factory(symbol, name, name, name.toLowerCase() + "-blockchain");
	}

	public static void factory(String symbol, String name, String installName) {
		factory(symbol, name, installName, name.toLowerCase() + "-blockchain");
	}
	
	public static void factory(String symbol, String name, String installName, String appDataName) {
		String exePath;
		String forkBase = System.getProperty("user.home") + "\\AppData\\Local\\" + appDataName + "\\";
		String logPath = System.getProperty("user.home") + "\\." + installName + "\\mainnet\\log\\debug.log";
		String appPath;
		try {
			File f = new File(logPath);
			appPath = Util.getDir(forkBase, "app");
			if (name.equals("Chiarose"))
				exePath = forkBase + appPath + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
			else
				exePath = forkBase + appPath + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
			
			
			LIST.add(new Fork(symbol, name, exePath, logPath));
			if (!f.exists())
				System.out.println("WARNING: Could not read log: " + logPath);
		} catch (IOException e) {
			//e.printStackTrace();
		}
		
	}
	
	public Optional<Integer> getIndex() {
		synchronized (LIST) {
			int idx = LIST.indexOf(this);
			return (-1 != idx) ? Optional.of(idx) : Optional.empty();
		}
	}

	public void sendTX(String address, String amt, String fee) {
		String ret = ProcessPiper.run(exePath,"wallet","send","-i","1","-a",amt,"-m",fee,"-t",addr);
		ForkFarmer.showMsg("Send Transaction", ret);
	}

	

}

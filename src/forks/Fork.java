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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

import javax.swing.ImageIcon;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.ForkFarmer;
import main.MainGui;
import peer.PeerView;
import transaction.Transaction;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.apache.ReversedLinesFileReader;
import util.process.ProcessPiper;

public class Fork {
	public static ScheduledExecutorService SVC = Executors.newScheduledThreadPool(10);
	public ScheduledFuture<?> future;
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
	public String version;
	public ImageIcon ico;
	public ImageIcon statusIcon = GRAY;
	private double readTime;
	public NetSpace ns;
	public NetSpace ps;
	public String etw;
	public String status;
	
	public String addr;
	public boolean cancel;
		
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
		if (true == cancel) {
			future.cancel(true);
			return;
		}
		/*
		ScheduledThreadPoolExecutor implementation = (ScheduledThreadPoolExecutor) SVC;
        int size = implementation.getQueue().size();
		System.out.println("Load Wallet: " + name + " ---- " + size);
		*/
		
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
			
			if (null == version)
				version = ProcessPiper.run(exePath,"version");
			
			Transaction.load(this);
			loadSummary();
			readLog();
			updateView();
			
			statusIcon = (readTime <= 0 || readTime > 5) ? YELLOW : GREEN;
		} catch (Exception e) {
			statusIcon = RED;
		}
	}
	
	private void loadSummary() {
		String summary = ProcessPiper.run(exePath,"farm","summary");
		String[] lines = summary.split(System.getProperty("line.separator"));
		
		for (String l : lines) {
			if (l.contains("Estimated network space: "))
				ns = new NetSpace(l.substring("Estimated network space: ".length()));
			else if (l.contains("Total size of plots: ")) {
				ps = new NetSpace(l.substring("Total size of plots: ".length()));
				MainGui.updatePlotSize(ps);
			} else if (l.contains("Expected time to win: ")) {
				etw = l.substring("Expected time to win: ".length());
			} else if (l.contains("Farming status: ")) {
				status = l.substring("Farming status: ".length());
			} 
			
		}
		
	}

	public void readLog() {
		File f = new File(logPath);
		ReversedLinesFileReader lr = null;
		try {
			String s;
			lr = new ReversedLinesFileReader(f,Charset.defaultCharset());
			for (int i = 0; null != (s = lr.readLine()); i++) {
				readTime = 0;
				
				if (i > 50)
					break;
				
				if (s.contains("Time: ")) {
					s = Util.getWordAfter(s, "Time: ");
					updateReadTime(Double.parseDouble(s));
					break;
				} else if (s.contains("took: ")) {
					s = Util.getWordAfter(s, "took: ");
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
			// could not read log;
		};
		Util.closeQuietly(lr);
		updateView();
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
	
	public void showConnections () {
		PeerView pv = new PeerView(this);
		ForkFarmer.showPopup(name + ": Peer Connections", pv);
	}
	
	public void refresh() {
		SVC.submit(() -> loadWallet());
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

	public static void factory(String symbol, String name, String dataPath) {
		factory(symbol, name, dataPath, name.toLowerCase() + "-blockchain");
	}
	
	public static void factory(String symbol, String name, String dataPath, String daemonPath) {
		String exePath;
		String forkBase;
		String logPath;
		
		if (System.getProperty("os.name").startsWith("Windows")) {
			forkBase = System.getProperty("user.home") + "\\AppData\\Local\\" + daemonPath + "\\";
			logPath = System.getProperty("user.home") + "\\." + dataPath + "\\mainnet\\log\\debug.log";
		} else {
			forkBase = System.getProperty("user.home") + "/" + daemonPath + "/";
			logPath = System.getProperty("user.home") + "/." + dataPath.toLowerCase() + "/mainnet/log/debug.log";
		}
		
		String appPath;
		try {
			File f = new File(logPath);

			if (System.getProperty("os.name").startsWith("Windows")) {
				if ("Tad" == name || "Spare" == name || "Caldera" == name) {
					exePath = forkBase + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
					if (new File(exePath).exists())
						LIST.add(new Fork(symbol, name, exePath, logPath));
					return;
				} 
				
				appPath = Util.getDir(forkBase, "app");
				
				if (name.equals("Chiarose"))
					exePath = forkBase + appPath + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
				else
					exePath = forkBase + appPath + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
			} else {
				exePath = forkBase + "/venv/bin/" + name.toLowerCase();
				if (!new File(exePath).exists())
					return;
			}
				
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
		String ret = ProcessPiper.run(exePath,"wallet","send","-i","1","-a",amt,"-m",fee,"-t",address);
		ForkFarmer.showMsg("Send Transaction", ret);
	}

	/*public String execute(String... pargs) {
		return ProcessPiper.run(exePath,pargs);
	}*/

}

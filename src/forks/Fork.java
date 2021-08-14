package forks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
	transient private static final String NOT_FOUND = "";
	private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	public static ScheduledExecutorService SVC = Executors.newScheduledThreadPool(20);
	public static ScheduledExecutorService LOG_SVC = Executors.newScheduledThreadPool(1);
	transient public ScheduledFuture<?> walletFuture;
	transient  public ScheduledFuture<?> logFuture;
	public static List<Fork> LIST = new ArrayList<>();
	public transient double balance = -1;
	
	private static final String icoPath = "icons/forks/";
	private static final ImageIcon GREEN 	= Ico.loadIcon("icons/circles/green.png");
	private static final ImageIcon RED 		= Ico.loadIcon("icons/circles/red.png");
	private static final ImageIcon YELLOW	= Ico.loadIcon("icons/circles/yellow.png");
	private static final ImageIcon GRAY		= Ico.loadIcon("icons/circles/gray.png");
	
	public String symbol;
	public String exePath;
	
	public String name;
	transient public String version;
	transient public ImageIcon ico;
	transient public ImageIcon statusIcon = GRAY;
	transient private double readTime;
	transient public NetSpace ns;
	transient public NetSpace ps;
	transient public String etw;
	transient public String status;
	transient public int logLines;
	public double price;
	public double rewardTrigger;
	
	public String addr;
	transient public boolean cancel;
	public String logPath;
	
	public Fork() {
		
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
			walletFuture.cancel(true);
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
		} catch (Exception e) {
			statusIcon = RED;
		}
		updateReadTime(readTime);
		MainGui.updateBalance();
		updateView();
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
		File logFile = new File(logPath);
		if (true == cancel) {
			logFuture.cancel(true);
			return;
		} else if (!logFile.exists()) {
			return;
		}
		
		LocalDateTime now = LocalDateTime.now();  
		
		ReversedLinesFileReader lr = null;
		long sec;
		try {
			String s;
			lr = new ReversedLinesFileReader(logFile,Charset.defaultCharset());
			readTime = 0;
			for (int i = 0; null != (s = lr.readLine()); i++) {
				
				if (s.length() < 19)
					continue;
				sec = 0;
				try {
					String logTimeString = s.substring(0,19);
					logTimeString = logTimeString.replace("T", " ");
					LocalDateTime logTime = LocalDateTime.parse(logTimeString, DTF);
					Duration duration = Duration.between(logTime, now);
					sec = duration.getSeconds();
				} catch (Exception e) {
					// error parsing date for some reason
				}
				
				if (sec > 60 || i > 250) {
					logLines = i;
					break;
				}	
				
				if (0 == readTime) {
					if (s.contains("Time: ")) {
						s = Util.getWordAfter(s, "Time: ");
						updateReadTime(Double.parseDouble(s));
					} else if (s.contains("took: ")) {
						s = Util.getWordAfter(s, "took: ");
						if (s.endsWith("."))
							s = s.substring(0, s.length()-1);
						updateReadTime(Double.parseDouble(s));
					} else if (s.contains("WARNING  Respond plots came too late")) {
						readTime = -2;
					} else if (s.contains("Harvester did not respond")) {
						readTime = -2;
					}
				}
			}
			
		} catch (Exception e) {
			// could not read log;
			e.printStackTrace();
		};
		updateReadTime(readTime);
		Util.closeQuietly(lr);
		updateLogView();
	}

	private void updateReadTime(double rt) {
		readTime = rt;
		
		if (null == status)
			return;
		
		
		if (status.startsWith("Farming") && readTime <= 5 && readTime >= 0)
				statusIcon = GREEN;
		else if (status.startsWith("Farming") && readTime > 5 && readTime < 30)
			statusIcon = YELLOW;
		else if (status.startsWith("Syncing"))
			statusIcon = YELLOW;
		else
			statusIcon = RED;
	}

	private void updateView() {
		if (status.startsWith("Not synced"))
			statusIcon = RED;
		getIndex().ifPresent(ForkView::fireTableRowUpdated);
	}
	
	private void updateLogView() {
		getIndex().ifPresent(ForkView::fireTableLogRead);
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
			status = "";
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
		File logFile = new File(logPath);
		JPanel logPanel = new JPanel(new BorderLayout());
		JTextArea jta = new JTextArea();
		JScrollPane JSP = new JScrollPane(jta);
		JSP.setPreferredSize(new Dimension(1200,500));
		logPanel.add(JSP,BorderLayout.CENTER);
		
		ReversedLinesFileReader lr = null;
		try {
			lr = new ReversedLinesFileReader(logFile,Charset.defaultCharset());
			List<String> SL = lr.readLines(30);
			String output = String.join("\n", SL);
			jta.setText(output);
		} catch (IOException e1) {
			e1.printStackTrace();
		};
		Util.closeQuietly(lr);
		
		ForkFarmer.showPopup(name + " log", logPanel);
		
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
	
	public void loadIcon() {
		try {
			ico = Ico.loadIcon(icoPath + name + ".png",16);
		} catch (RuntimeException e) {
			try {
				ico = Ico.loadIcon(icoPath + name + ".jpg",16);
			} catch (RuntimeException ee) {}
		}
	}

	public Fork(String symbol, String name, String dataPath, String daemonPath, double price, double rewardTrigger) {
		this.exePath = NOT_FOUND;
		this.logPath = NOT_FOUND;
		this.symbol = symbol;
		this.name = name;
		this.price= price;
		this.rewardTrigger = rewardTrigger;
		
		String userHome = System.getProperty("user.home");
		String forkBase;
				
		if (System.getProperty("os.name").startsWith("Windows")) {
			forkBase = userHome + "\\AppData\\Local\\" + daemonPath + "\\";
			logPath = userHome + "\\" + dataPath.toLowerCase() + "\\mainnet\\log\\debug.log";
		} else {
			forkBase = userHome + "/" + daemonPath + "/";
			logPath = userHome + "/" + dataPath.toLowerCase() + "/mainnet/log/debug.log";
		}
		
		if (!new File(logPath).exists())
			exePath = "?";
		
		String appPath;
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				if ("Tad" == name || "Spare" == name || "Caldera" == name) {
					exePath = forkBase + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
					if (!new File(exePath).exists())
						return;

						LIST.add(this);
				} 
				
				appPath = Util.getDir(forkBase, "app");
				
				if (name.equals("Chiarose"))
					exePath = forkBase + appPath + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
				else
					exePath = forkBase + appPath + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
			} else {
				exePath = forkBase + "/venv/bin/" + name.toLowerCase();
			}

			if (!new File(exePath).exists())
				return;
			
			LIST.add(this);
			
		} catch (IOException e) {
			
		}
	}

}

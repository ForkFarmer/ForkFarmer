package forks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.ForkFarmer;
import main.MainGui;
import peer.PeerView;
import transaction.Transaction;
import types.Balance;
import types.Effort;
import types.ReadTime;
import types.TimeU;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.apache.ReversedLinesFileReader;
import util.swing.SwingEX;

public class Fork {
	transient private static final String NOT_FOUND = "";
	transient public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	transient public static ScheduledExecutorService SVC = Executors.newScheduledThreadPool(10);
	transient public static ScheduledExecutorService LOG_SVC = Executors.newScheduledThreadPool(1);
	transient public ScheduledFuture<?> walletFuture;
	transient  public ScheduledFuture<?> logFuture;
	public static List<Fork> LIST = new ArrayList<>();
	public transient Balance balance = new Balance();
	public transient int height = 0;
	
	public String symbol;
	public String exePath;
	public String name;
	transient public String version;
	transient public ImageIcon ico;
	transient public ImageIcon statusIcon = Ico.GRAY;
	transient ReadTime readTime;
	transient public NetSpace netSpace;
	transient public NetSpace plotSpace;
	transient public TimeU etw = new TimeU();
	transient public String syncStatus;
	transient public String farmStatus;
	transient public int logLines;
	transient public double dayWin;
	transient public double estEarn;
	transient public Transaction lastWin;
	transient public Exception lastException;
	
	public double price;
	public double rewardTrigger;
	
	public String addr;
	transient boolean hidden;
	public String logPath;
	
	public Fork() { // needed for YAML
		
	}
		
	public void loadWallet () {
		boolean dead = false;
		if (true == hidden) {
			walletFuture.cancel(true);
			return;
		}
		
		if ("CGN".contentEquals(symbol)) {
			addr = "*** Not Supported ***";
			return;
		}
		
		Process p = null, p2 = null;
		BufferedReader br = null, br2 = null;
		try {
			p = Util.startProcess(exePath, "wallet", "show");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String l = null;
			while ( null != (l = br.readLine())) {
				boolean readBalance = false;
				if (l.contains("Connection error")) {
					p.destroyForcibly();
					dead = true;
					break;
				} else if (l.contains("Total Balance: ")) {
					if (!readBalance || balance.balance <= 0)
						balance = new Balance(Util.getWordAfter(l, "Total Balance: "));
					readBalance = true;
					ForkView.updateBalance(this);
				} else if (l.contains("Wallet height: ")) {
					String heightStr = l.substring("Wallet height: ".length());
					height = Integer.parseInt(heightStr);
				} else if (l.contains("Sync status: ")) {
					syncStatus = l.substring("Sync status: ".length());
				}
			}
			
			if (null == version) {
				p2 = Util.startProcess(exePath, "version");
				br2 = new BufferedReader(new InputStreamReader(p2.getInputStream()));
				version = br2.readLine();
				if (!p2.waitFor(2, TimeUnit.SECONDS))
					p2.destroyForcibly();
			}
			
			if (dead) {
				syncStatus = "";
			} else {
				Transaction.load(this);
				loadSummary();
				
				synchronized(Transaction.class) {
					dayWin = Transaction.LIST.stream()
						.filter(t -> this == t.f && t.blockReward && t.getTimeSince().inMinutes() < (60*24))
						.collect(Collectors.summingDouble(Transaction::getAmount));
				}
				
				
			}
		} catch (Exception e) {
			lastException = e;
			statusIcon = Ico.RED;
		}
		
		try {
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		Util.closeQuietly(br2);
		Util.waitForProcess(p2);
		
		updateIcon();
		MainGui.updateBalance();
		updateView();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	
	private void loadSummary() {
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(exePath, "farm", "summary");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String l = null;
			while ( null != (l = br.readLine())) {
				if (l.contains("Connection error")) {
					p.destroyForcibly();
					break;
				} else if (l.contains("Estimated network space: "))
					netSpace = new NetSpace(l.substring("Estimated network space: ".length()));
				else if (l.contains("Total size of plots: ")) {
					try {
						plotSpace = new NetSpace(l.substring("Total size of plots: ".length()));
						MainGui.updatePlotSize(plotSpace);
					} catch (Exception e) {
						// nothing to do
					}
				} else if (l.contains("Expected time to win: ")) {
					etw = new TimeU(l.substring("Expected time to win: ".length()));
					estEarn = 43800 / etw.inMinutes() * price;
				} else if (l.contains("Farming status: ")) {
					farmStatus = l.substring("Farming status: ".length());
				} 
			}
		} catch (IOException e) {
			lastException = e;
		}
		ForkView.update(this);
		Util.waitForProcess(p);
		Util.closeQuietly(br);
	}

	public void readLog() {
		File logFile = new File(logPath);
		if (true == hidden) {
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
			readTime = ReadTime.EMPTY;
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
				
				if (readTime == ReadTime.EMPTY) {
					if (s.contains("Time: ")) {
						s = Util.getWordAfter(s, "Time: ");
						readTime = new ReadTime(Double.parseDouble(s));
					} else if (s.contains("took: ")) {
						s = Util.getWordAfter(s, "took: ");
						if (s.endsWith("."))
							s = s.substring(0, s.length()-1);
						readTime = new ReadTime(Double.parseDouble(s));
						break;
					} else if (s.contains("WARNING  Respond plots came too late")) {
						readTime = ReadTime.TIMEOUT;
						break;
					} else if (s.contains("Harvester did not respond")) {
						readTime = ReadTime.TIMEOUT;
						break;
					}
				}
			}
			
		} catch (Exception e) {
			lastException = e;
		};
		
		updateIcon();
		Util.closeQuietly(lr);
	}

	private void updateIcon() {
		if (null == farmStatus)
			return;
		
		if (farmStatus.startsWith("Farming") && readTime.time <= 5 && readTime.time >= 0)
			statusIcon = Ico.GREEN;
		else if (farmStatus.startsWith("Farming") && readTime == ReadTime.EMPTY)
			statusIcon = Ico.GREEN;
		else if (farmStatus.startsWith("Farming") && readTime.time > 5 && readTime.time < 30)
			statusIcon = Ico.YELLOW;
		else if (farmStatus.startsWith("Syncing"))
			statusIcon = Ico.YELLOW;
		else
			statusIcon = Ico.RED;
		ForkView.updateLogRead(this);
	}

	private void updateView() {
		if (null == farmStatus)
			statusIcon = Ico.RED;
		else if (farmStatus.startsWith("Not synced"))
			statusIcon = Ico.RED;
		ForkView.update(this);
	}
	
	public void start() {
		SVC.submit(() -> {
			Util.runProcessWait(exePath,"start","farmer");
			loadWallet();	
		});
	}
	
	public void stop() {
		SVC.submit(() -> {
			Util.runProcessWait(exePath,"stop","farmer");
			statusIcon = Ico.RED;
			farmStatus = "";
			updateView();
		});
	}
	
	public void generate() {
		SVC.submit(() -> {
			
			//addr = ProcessPiper.run(exePath,"wallet","get_address").replace("\n", "").replace("\r", "");
			updateView();
		});
	}
	
	public void showConnections () {
		ForkFarmer.showPopup(name + ": Peer Connections", new PeerView(this));
	}
	
	public void showLastException () {
		JPanel exceptionPanel = new JPanel(new BorderLayout());
		JTextArea jta = new JTextArea();
		JScrollPane JSP = new JScrollPane(jta);
		JSP.setPreferredSize(new Dimension(800,300));
		JMenuBar MENU = new JMenuBar();
		
		MENU.add(new SwingEX.Btn("show wallet", Ico.CLI,  () -> {
			StringBuilder sb = new StringBuilder();
			sb.append("Running: wallet show");
			sb.append("ExePath: " + exePath);
			
			String ret = Util.runProcessWait(exePath,"wallet","show");
			sb.append("\n" +  ret);
			jta.setText(sb.toString());
		}));
		
		MENU.add(new SwingEX.Btn("farm summary", Ico.CLI,  () -> {
			StringBuilder sb = new StringBuilder();
			sb.append("Running: wallet show\n");
			sb.append("ExePath: " + exePath + "\n");
			
			String ret = Util.runProcessWait(exePath,"farm","summary");
			sb.append("\n" +  ret);
			jta.setText(sb.toString());
		}));
		
		
		exceptionPanel.add(JSP,BorderLayout.CENTER);
		exceptionPanel.add(MENU,BorderLayout.PAGE_START);
		
		if (null != lastException) {
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			lastException.printStackTrace(pw);
			String sStackTrace = sw.toString(); // stack trace as a string
			jta.setText(sStackTrace);
		} else
			jta.setText("Congrats! No exceptions found");
		
		ForkFarmer.showPopup(name + ": Exception",  exceptionPanel);
	}
	
	public void refresh() {
		SVC.submit(() -> loadWallet());
	}
	
	public Optional<Integer> getIndex() {
		synchronized (LIST) {
			int idx = LIST.indexOf(this);
			return (-1 != idx) ? Optional.of(idx) : Optional.empty();
		}
	}

	public void sendTX(String address, String amt, String fee) {
		Process p = null;
		BufferedReader br = null;
		
		try {
			p = Util.startProcess(exePath,"wallet","send","-i","1","-a",amt,"-m",fee,"-t",address);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder sb = new StringBuilder();
			
			String l = null;
			while ( null != (l = br.readLine()))
				sb.append(l + "\n");
			
			ForkFarmer.showMsg("Send Transaction", sb.toString());
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Util.closeQuietly(br);
		Util.waitForProcess(p);
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
			if (symbol.equals("NCH"))
				logPath = userHome + "\\.chia\\ext9\\log\\debug.log";
		} else {
			forkBase = userHome + "/" + daemonPath + "/";
			logPath = userHome + "/" + dataPath.toLowerCase() + "/mainnet/log/debug.log";
		}
		
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				
				if ("Spare" == name || "Caldera" == name || "SSDCoin" == name || name.equals("Stor"))
					exePath = forkBase + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
				else if (name.equals("Chiarose") || name.equals("NChainExt9") )
					exePath = forkBase + Util.getDir(forkBase, "app") + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
				else
					exePath = forkBase + Util.getDir(forkBase, "app") + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
			} else {
				exePath = forkBase + "/venv/bin/" + name.toLowerCase();
			}

			if (!new File(exePath).exists())
				return;
			
			LIST.add(this);
		} catch (IOException e) {
			// Didn't load the fork for whatever reason
		}
	}

	public TimeU getPreviousWin() {
		return (null == lastWin) ? TimeU.NEVER : lastWin.getTimeSince();
	}

	public static Optional<Fork> getBySymbol(String symbol) {
		return LIST.stream().filter(f -> f.symbol.equals(symbol)).findAny();
	}
	
	public void loadIcon() {
		ico = Ico.getForkIcon(name);
	}

	public Effort getEffort() {
		return new Effort((etw.inMinutes() > 0 && null != lastWin) ?
				(int) (((double)lastWin.getTimeSince().inMinutes() / (double)etw.inMinutes()) * (double)100) : 0);
		
	}

}

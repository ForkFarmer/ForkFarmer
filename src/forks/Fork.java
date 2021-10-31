package forks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import org.yaml.snakeyaml.Yaml;

import debug.DebugView;
import ffutilities.ForkPorts;
import logging.LogView;
import main.ForkFarmer;
import main.MainGui;
import transaction.Transaction;
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

public class Fork {
	private static final ExecutorService SVC = Executors.newFixedThreadPool(2);
	public static List<Fork> LIST = new ArrayList<>();
	public static List<Fork> I_LIST;

	public transient Balance balance = new Balance();
	public transient Balance equity = new Balance();
	public transient Balance height = new Balance();

	public String symbol;
	public String exePath;
	public String name;
	
	public List<String> coldAddrList = new ArrayList<>();
	
	transient public ForkData fd;
	transient public ForkPorts fp = new ForkPorts();
	transient public String version;
	transient public String latestVersion;
	transient public String published;
	transient public ImageIcon ico;
	transient public ImageIcon statusIcon = Ico.GRAY;
	transient public ReadTime readTime = ReadTime.EMPTY;
	transient public NetSpace netSpace;
	transient public NetSpace plotSpace;
	transient public TimeU etw = new TimeU();
	transient public String syncStatus;
	transient public String farmStatus;
	transient public double dayWin;
	transient public double estEarn;
	transient public Transaction lastWin;
	transient public Exception lastException;
	transient public List<Wallet> walletList = new ArrayList<>();
	transient boolean hidden;
	transient String lastTimeStamp;
	transient LocalDateTime lastTimeUpdate;
	transient public Wallet wallet = Wallet.EMPTY;
	transient public boolean xchfSupport;
	
	public String walletAddr;
	public boolean fullNode = true;
	public boolean walletNode = true;
	public double price;
	public double fullReward;
	
	public String logPath;
	public String configPath;
	public boolean cold;
	
	public Fork() { // needed for YAML
		
	}
	
	public void loadWallets() {
		if (cold)
			return;
		
		LogView.add(name + " getting receive address");
		
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(exePath, "keys", "show");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			//check for passPhrase
			char f = (char)br.read();
			if (f == '(') {
				p.destroyForcibly();
				wallet = new Wallet("Error! remove passphrase");
				hidden = true;
			}
			
			int walletIndex = 1;
			String l = null;
			while ( null != (l = br.readLine())) {
				if (l.startsWith("Fingerprint: " )) {
					String fingerprint = l.substring("Fingerprint: ".length());;
					while (null != (l = br.readLine())) {
						if (l.contains("First wallet address: ")) {
							String address = l.substring("First wallet address: ".length());
							walletList.add(new Wallet(fingerprint, address,walletIndex));
							walletIndex++;
							break;
						}
					}
				}
				
			}
			
			
		} catch (Exception e) {
			lastException = e;
			e.printStackTrace();
		}
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		
		if (1 == walletList.size()) { 
			wallet = walletList.get(0);
		} else if (walletList.size() > 1) {
			wallet = Wallet.SELECT;
			if (null != walletAddr)
				walletList.stream().filter(w -> w.addr.equals(walletAddr)).findAny().ifPresent(w -> {wallet = w; walletAddr = w.addr;});
		}
		
		LogView.add(name + " done getting receive address");
	}
	
	public void loadVersion() {
		if (cold)
			return;
		
		LogView.add(name + " getting software verion");
		
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(exePath, "version");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			version = br.readLine();
		} catch (Exception e) {
			version = "error";
		}
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		
		LogView.add(name + " done getting software verion");
	}
		
	@SuppressWarnings("unused")
	public void loadWallet () {
		if (!walletNode || -1 == wallet.index || wallet.cold)
			return;

		if ("CGN".equals(symbol)) {
			balance = Balance.NOT_SUPPORTED;
			return;
		}
		
		LogView.add(name + " loading wallet");
		
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(exePath, "wallet", "show");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String l = null;
			Balance newBalance = new Balance();
			while ( null != (l = br.readLine())) {
				
				// No online backup file found
				// Press S to skip restore from backup file
				// Press F to use your own backup file:
				if (l.contains(" Press S")) { 
					System.out.println("detected press S/F");
					PrintWriter pw = new PrintWriter(p.getOutputStream());
					pw.println("S"); 
					pw.close();
				}
				
				if (l.contains("Choose")) { // little bit of a hack for multiple wallets.. but it works for now
					PrintWriter pw = new PrintWriter(p.getOutputStream());
					char[] buf = new char[4096];
					br.read(buf);
					int numWallets = new String(buf).split("\r\n|\r|\n").length - 1;
					pw.println(wallet.index);
					pw.close();
				}
				
				if (l.contains("Connection error")) {
					p.destroyForcibly();
					syncStatus = "";
					break;
				} else if (l.contains("-Total Balance: ")) {
					newBalance.add(Double.parseDouble(Util.getWordAfter(l, "-Total Balance: ")));
				} else if (l.contains("Wallet height: ")) {
					
					String heightStr = Util.getWordAfter(l, "Wallet height: ");
					height = new Balance(Integer.parseInt(heightStr));
				} else if (l.contains("Sync status: ")) {
					syncStatus = l.substring("Sync status: ".length());
				}
			}
			
			boolean balancedChanged = updateBalance(newBalance);
			
			if ("" != syncStatus) {
				synchronized(Transaction.class) {
					if (balancedChanged)
						Transaction.load(this);
					dayWin = Transaction.LIST.stream()
						.filter(t -> this == t.f && t.blockReward && t.getTimeSince().inMinutes() < (60*24))
						.collect(Collectors.summingDouble(Transaction::getAmount));
				}
			}
				
		} catch (Exception e) {
			lastException = e;
			statusIcon = Ico.RED;
		}
		
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		
		updateIcon();
		LogView.add(name + " done loading wallet");
	}
	
	public void loadFarmSummary() {
		if (cold)
			return;
		
		LogView.add(name + " loading farm summary");
		
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(exePath, "farm", "summary");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String l = null;
			while ( null != (l = br.readLine())) {
				/*
				if (l.contains("Connection error") && false == fullNode) {
					p.destroyForcibly();
					break;
				} else 
				*/
				if (l.contains("Estimated network space: "))
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
					if (etw.inMinutes()  > 0 && price > 0)
						estEarn = 43800 / etw.inMinutes() * price;
				} else if (l.contains("Farming status: ")) {
					farmStatus = l.substring("Farming status: ".length());
				} 
			}
		} catch (IOException e) {
			lastException = e;
		}
		Util.waitForProcess(p);
		Util.closeQuietly(br);
		
		LogView.add(name + " done loading farm summary");
	}
	
	public void readLog() {
		if (cold)
			return;
		
		boolean readH = false, readT = false;
		File logFile = new File(logPath);
		if (!logFile.exists())
			return;
		
		String firstTime = null;
		LocalDateTime now = LocalDateTime.now(); 
		LocalDateTime logTime = null;
		ReversedLinesFileReader lr = null;
		boolean updateWallet = false;
		
		// If we haven't seen a time update event from log make sure we clear currently displayed one
		if (null != lastTimeUpdate && Duration.between(lastTimeUpdate, now).getSeconds() > 40)
			readTime = ReadTime.EMPTY;
		
		try {
			String s,t;
			lr = new ReversedLinesFileReader(logFile,Charset.defaultCharset());
			for (int i = 0; null != (s = lr.readLine()); i++) {
				if (s.length() < 24)
					continue;
				
				String timeStamp = s.substring(0,23);
				if (null == firstTime)
					firstTime = timeStamp;
				logTime = FFUtil.parseTime(s);
				
				// don't read long time events that are too old in the log
				if (!readT && null != logTime && Duration.between(logTime, now).getSeconds() > 40) {
					readTime = ReadTime.EMPTY;
					readT = true;
				}
				
				if (i > 250 || timeStamp.equals(lastTimeStamp)) {
					lastTimeStamp = firstTime;
					break;
				}	
				
				if (!readT && null != (t = Util.wordAfterIfExist(s, "Time: "))) {
					readTime = new ReadTime(Double.parseDouble(t));
					readT = true;
				} else if (!readT && null != (t = Util.wordAfterIfExist(s, "Time: "))) {
					if (t.endsWith("."))
						t = t.substring(0, s.length()-1);
					readTime = new ReadTime(Double.parseDouble(t));
					readT = true;
				} else if (!readT && s.contains("WARNING  Respond plots came too late")) {
					readTime = ReadTime.TIMEOUT;
					readT = true;
				} else if (!readT && s.contains("Harvester did not respond")) {
					readTime = ReadTime.TIMEOUT;
					readT = true;
				} else if (!readH && null != (t = Util.wordAfterIfExist(s, "wallet peak to height "))) {
					t = t.replace(",", "");
					height = new Balance(Integer.parseInt(t));
					readH = true;
				} else if (s.contains("Adding coin") || s.contains("Removing coin")) {
					updateWallet = true;
				}
					
				
			}
			
		} catch (Exception e) {
			lastException = e;
		};
		
		if (updateWallet) {
			if (((ThreadPoolExecutor)SVC).getQueue().size() < 2) {
				SVC.submit(() -> {loadWallet(); ForkView.update(this);});
			} else {
				System.out.println("Worker threads are stuck");
			}
		}
				
		if (readT)
			lastTimeUpdate = now;
		
		Util.closeQuietly(lr);
		ForkView.updateLog(this);
	}

	void updateIcon() {
		if (readTime.time <= 5 && readTime.time >= 0)
			statusIcon = Ico.GREEN;
		else if (readTime.time > 5 && readTime.time < 30)
			statusIcon = Ico.YELLOW;
		else
			statusIcon = Ico.RED;
		
		if (null != farmStatus) {
			if (farmStatus.equals("Farming") && readTime == ReadTime.EMPTY)
				statusIcon = Ico.GREEN; // proof times < 5sec are don't show up in log for many of xch for default log levels.
			if (farmStatus.equals("Syncing"))
				statusIcon = Ico.YELLOW;
			if (farmStatus.startsWith("Not Synched"))
				statusIcon = Ico.RED;
			if (farmStatus.startsWith("Not running") && !walletNode)
				statusIcon = Ico.GREEN;
		}
		
		if (cold)
			statusIcon = Ico.SNOW;
		
	}
	
	public void start() {
		ForkStarter.start((this));
		loadWallet();
	}
	
	public void stop() {
		Util.runProcessWait(exePath,"stop","farmer");
		statusIcon = Ico.RED;
		farmStatus = null;
		ForkView.update(this);
	}
	
	public void generate() {
		new Thread(() -> {
			
			//addr = ProcessPiper.run(exePath,"wallet","get_address").replace("\n", "").replace("\r", "");
			updateIcon();
		}).start();
	}
	
	
	
	public void showLastException () {
		ForkFarmer.newFrame(name + ": Debug View", ico, new DebugView(this));
	}
	
	public void refresh() {
		new Thread(() -> loadWallet()).start();
	}
	
	public int getIndex() {
		return LIST.indexOf(this);
	}

	public void sendTX(String address, String amt, String fee) {
		Process p = null;
		BufferedReader br = null;
		
		try {
			p = Util.startProcess(exePath,"wallet","send","-i",Integer.toString(wallet.index),"-a",amt,"-m",fee,"-t",address);
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
	
	public TimeU getPreviousWin() {
		return (null == lastWin) ? TimeU.NEVER : lastWin.getTimeSince();
	}

	public static Optional<Fork> getBySymbol(String symbol) {
		return LIST.stream().filter(f -> f.symbol.equals(symbol)).findAny();
	}
	
	public Effort getEffort() {
		return (etw.inMinutes() > 0 && null != lastWin) ?
				new Effort((int) (((double)lastWin.getTimeSince().inMinutes() / (double)etw.inMinutes()) * (double)100)) : Effort.EMPTY;
	}

	
	public void updatePrice(double p) {
		if (p != price) {
			price = p;
			refreshEquity();
		}
	}
	
	public boolean updateBalance(Balance b) {
		if (b.amt == balance.amt && !balance.toString().equals(""))
			return false;
		balance = b;
		refreshEquity();
		return true;
	}
	
	public void refreshEquity() {
		equity = new Balance(balance.amt * price,2);
		MainGui.updateTotal();
		ForkView.update(this);
	}
	
	@SuppressWarnings("unchecked")
	public void loadConfig() {
		try {
			File f = new File(configPath);
			InputStream inputStream = new FileInputStream(f);
			Yaml yaml = new Yaml();
			Map<String, Object> cfgMap = yaml.load(inputStream);
			
			fp.daemon = (int) cfgMap.get("daemon_port");
			
			Map<String, Object> farmerMap = (Map<String, Object>) cfgMap.get("farmer");
			Map<String, Object> fullNodeMap = (Map<String, Object>) cfgMap.get("full_node");
			Map<String, Object> harvesterMap = (Map<String, Object>) cfgMap.get("harvester");
			
			fp.fullnode = (int) fullNodeMap.get("port");
			fp.fullnode_rpc = (int) fullNodeMap.get("rpc_port");
			
			fp.harvester = (int) harvesterMap.get("port");
			fp.harvester_rpc = (int) harvesterMap.get("rpc_port");
			
			fp.farmer = (int) farmerMap.get("port");
			fp.farmer_rpc = (int) farmerMap.get("rpc_port");
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void newColdWallet(String address) {
		ForkData.getbyAddress(address).ifPresent(fd -> {
			if (null != fd.atbPath) {
				Fork f = new Fork();
					
				f.ico = fd.ico;
				f.price = fd.price;
				f.name = fd.displayName;
				f.symbol = fd.coinPrefix;
				f.wallet = new Wallet(address);
				f.walletAddr = address;
				f.statusIcon = Ico.SNOW;
				f.cold = true;
				f.farmStatus = "Cold";
				f.readTime = new ReadTime(0);
				f.fd = fd;
				Fork.LIST.add(f);
				ForkView.update();
			}
		});
	}

	public static Optional<Fork> getByAddress(String address) {
		return LIST.stream().filter(f -> !f.cold && address.startsWith(f.symbol.toLowerCase())).findAny();	
	}
	
}

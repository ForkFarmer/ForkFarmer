package forks;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;

import org.yaml.snakeyaml.Yaml;

import cats.Cat;
import debug.DebugView;
import ffutilities.ForkPorts;
import main.ForkFarmer;
import main.MainGui;
import peer.Peer;
import transaction.Transaction;
import transaction.TransactionView;
import transaction.Transaction.TYPE;
import types.Balance;
import types.Percentage;
import types.ReadTime;
import types.TimeU;
import types.Wallet;
import util.FFUtil;
import util.I18n;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.YamlUtil;
import util.apache.ReversedLinesFileReader;
import util.crypto.Bech32;
import util.json.JsonArray;
import util.json.JsonException;
import util.json.JsonObject;
import util.json.Jsoner;

public class Fork {
	private static final ExecutorService SVC = Executors.newFixedThreadPool(2);
	public static List<Fork> LIST = new ArrayList<>();
	public static List<Fork> FULL_LIST;

	public transient Balance balance = new Balance();
	public transient Balance equity = new Balance();
	public transient Balance walletHeight = new Balance();
	public transient Balance fullnodeHeight = new Balance();
	public transient Balance maxpeerHeight = new Balance();

	public String symbol;
	public String name;
	
	public List<String> coldAddrList = new ArrayList<>();
	
	transient public Map<Integer,Cat> assetMap = new HashMap<>();
	
	transient public ForkData fd;
	transient public ForkPorts fp = new ForkPorts();
	transient public String version;
	transient public String latestVersion;
	transient public String published;
	transient public ImageIcon ico;
	transient public ImageIcon statusIcon = Ico.GRAY;
	transient public ReadTime readTime = ReadTime.EMPTY;
	transient public NetSpace plotSpace;
	transient public String syncStatus;
	transient public String farmStatus;
	transient public double dayWin;
	transient public long dayWinBC;
	transient public double estEarn;
	transient public Transaction lastWin;
	transient public Exception lastException;
	transient public List<Peer> peerList = new ArrayList<>();
	transient public List<Wallet> walletList = new ArrayList<>();
	transient public boolean nothing;
	transient String lastTimeStamp;
	transient LocalDateTime lastTimeUpdate;
	transient public Wallet wallet = Wallet.EMPTY;
	transient public boolean xchfSupport;
	transient public Percentage load = Percentage.EMPTY;
	transient public int numH;
	transient public double upload;
	transient public double download;
		
	public String walletAddr;
	public boolean fullNode = true;
	public boolean walletNode = true;
	public double price;
	public double fullReward;
	public String passFile;
	
	public String exePath;
	public String logPath;
	public String configPath;
	public boolean cold;
	public boolean hidden;
	
	public String hexColor;
	transient public Color bgColor;
	
	public Fork() { // needed for YAML
		
	}
	
	public void loadWallets() {
		if (cold)
			return;

		ForkFarmer.LOG.add(name + " getting receive address");
			
		Process p = null;
		BufferedReader br = null;
		try {
			if (null != passFile)
				p = Util.startProcess(fd.exePath, "--passphrase-file", passFile, "keys", "show");
			else
				p = Util.startProcess(fd.exePath, "keys", "show");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));

			//check for passPhrase
			char f = (char)br.read();
			if (f == '(') {
				p.destroyForcibly();
				wallet = new Wallet("Set Passphrase: Action -> Set Pass File");
				hidden = true;
				ForkView.update(this);
			}
			
			int walletIndex = 1;
			String l = null;
			while ( null != (l = br.readLine())) {
				if (l.startsWith("Fingerprint: " )) {
					String fingerprint = l.substring("Fingerprint: ".length());;
					while (null != (l = br.readLine())) {
						if (l.contains("First wallet address: ")) {
							String address = Util.getWordAfter(l, "First wallet address: ");
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
		
		ForkFarmer.LOG.add(name + " done getting receive address");
	}
	
	public void loadVersion() {
		if (cold)
			return;
		
		ForkFarmer.LOG.add(name + " getting software verion");
		
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(fd.exePath, "version");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			version = br.readLine();
		} catch (Exception e) {
			version = "error";
		}
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		
		ForkFarmer.LOG.add(name + " done getting software verion");
	}
	
	public void loadBalanceFN() {
		if (!fd.rpc)
			return;
		if (null == wallet || Wallet.EMPTY == wallet || Wallet.SELECT == wallet || Wallet.NOT_SUPPORTED == wallet)
			return;
		
		
		byte[] ph = Bech32.decode(wallet.addr).toBA();
		if (null == ph)
			return;
		
		String phs = Util.getHexString(ph);
				
		String query =  Util.isHostWin()? "{\\\"puzzle_hash\\\": \\\"" + phs  +"\\\"}" : "{\"wallet_id\": \"" + phs + "\"}";

		Process p = null;
		BufferedReader br = null;
		
		try {
			p = Util.startProcess(fd.exePath, "rpc", "full_node", "get_coin_records_by_puzzle_hash",query);
			
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			JsonObject jo = (JsonObject)Jsoner.deserialize(br);
			
			boolean success = (boolean) jo.get("success");

			long totalBalance = 0;
			if (success) {
				JsonArray cRecords = (JsonArray) jo.get("coin_records");
				
				for (Object item : cRecords) {
					
					jo = (JsonObject)item;
					JsonObject co = (JsonObject) (jo).get("coin");
					totalBalance += ((BigDecimal)co.get("amount")).longValue();
				}
				
				Balance b = new Balance((double)totalBalance/(double)fd.mojoPerCoin);
		    	updateBalance(b);
		    	fd.localFN = true;
			}
			
		} catch (JsonException je) {
			// issues with the json rpc query
		} catch (Exception e) {
			lastException = e;
		}
		
		Util.closeQuietly(br);
		Util.waitForProcess(p);
	}

	@SuppressWarnings("unused")
	public void loadWallet () {
		if(cold || (!walletNode && fullNode)) {
			loadBalanceFN();
			return;
		}
					
		if (!walletNode || -1 == wallet.index || wallet.cold)
			return;
		
		if ("CGN".equals(symbol)) {
			balance = Balance.NOT_SUPPORTED;
			return;
		}
		
		ForkFarmer.LOG.add(name + " loading wallet");
		
		Process p = null;
		BufferedReader br = null;
		Balance newBalance = new Balance();
		try {
			if (fd.rpc) {
				String query =  Util.isHostWin()? "{\\\"wallet_id\\\": 1}" : "{\"wallet_id\": 1}";
				p = Util.startProcess(fd.exePath, "rpc", "wallet", "get_wallet_balance",query);
				br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				JsonObject jo = (JsonObject)Jsoner.deserialize(br);
				
				boolean success = (boolean) jo.get("success");
				if (success) {
					jo = (JsonObject) jo.get("wallet_balance");
					long balance = ((BigDecimal)jo.get("confirmed_wallet_balance")).longValue();
					newBalance = new Balance((double)balance/(double)fd.mojoPerCoin);
				}
				if (null != walletHeight && null != fullnodeHeight) {
					 syncStatus = walletHeight.amt >= this.fullnodeHeight.amt -1 ? "Synced" : "Not Synced";
				} else
					syncStatus = "";
				
			} else {
				boolean startBalances = false;
				p = Util.startProcess(fd.exePath, "wallet", "show");
				br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				String l = null;
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
					} else if (l.contains("Wallet height: ")) {
						String heightStr = Util.getWordAfter(l, "Wallet height: ");
						walletHeight = new Balance(Integer.parseInt(heightStr));
					} else if (l.contains("Sync status: ")) {
						syncStatus = l.substring("Sync status: ".length());
					} else if (l.contains("Balances,")) {
						startBalances = true;
					}
						
					
					if (l.startsWith("Wallet ID")) {
						String idNAme = l;
						String totalLine 	 = br.readLine();
						String pendingLine 	 = br.readLine();
						String spendableLine = br.readLine();
						if (idNAme.contains("STANDARD_WALLET"))
							newBalance.add(Double.parseDouble(Util.getWordAfter(totalLine, "-Total Balance: ")));
					}
					
				}
			}
		} catch (JsonException je) {
			// issues with the json rpc query
		} catch (Exception e) {
			lastException = e;
			statusIcon = Ico.RED;
		}
		
		boolean balancedChanged = updateBalance(newBalance);
		
		if ("" != syncStatus) {
			synchronized(Transaction.class) {
				if (balancedChanged)
					Transaction.load(this,1);
				dayWin = Transaction.LIST.stream()
					.filter(t -> this == t.f && TYPE.REWARD == t.t && t.getTimeSince().inMinutes() < (60*24))
					.collect(Collectors.summingDouble(Transaction::getAmount));
				dayWinBC = Transaction.LIST.stream()
						.filter(t -> this == t.f && TYPE.REWARD == t.t && t.getTimeSince().inMinutes() < (60*24)).count();
			}
		}
		
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		
		updateIcon();
		ForkFarmer.LOG.add(name + " done loading wallet");
	}
	
	public void loadFarmSummary() {
		if (cold)
			return;
		int numH = 0;
		ForkFarmer.LOG.add(name + " loading farm summary");
		
		Process p = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(fd.exePath, "farm", "summary");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			String l = null;
			while ( null != (l = br.readLine())) {
				if (l.contains("Estimated network space: ")) {
					NetSpace.parse(l.substring("Estimated network space: ".length())).ifPresent(n -> fd.netspace = n);
				} else if (l.contains("Total size of plots: ")) {
					try {
						NetSpace.parse(l.substring("Total size of plots: ".length()))
							.ifPresent(ps -> {plotSpace = ps; MainGui.updatePlotSize(ps);});
					} catch (Exception e) {
						// nothing to do
					}
				} else if (l.contains("Expected time to win: ")) {
					TimeU.parse(l.substring("Expected time to win: ".length()))
						.ifPresent(etw -> fd.etw = etw);
					
					//if (etw.inMinutes()  > 0 && price > 0)
						//estEarn = 43800 / etw.inMinutes() * price;
				} else if (l.contains("Farming status: ")) {
					farmStatus = l.substring("Farming status: ".length());
				} else if (l.startsWith("Local Harvester") || l.startsWith("Remote Harvester")) {
					numH++;
				}
			}
		} catch (IOException e) {
			lastException = e;
		}
		Util.waitForProcess(p);
		Util.closeQuietly(br);
		
		if (!fd.etw.known() && fd.netspace.known() && null != plotSpace && plotSpace.known()) {
			fd.etw = new TimeU((int) (fd.netspace.szTB / plotSpace.szTB * 18.75));
		}
		this.numH = numH; 
		
		ForkFarmer.LOG.add(name + " done loading farm summary");
	}
	
	public void readLog() {
		if (cold)
			return;
		
		boolean readWH = false, readT = false, readFNH = false;
		File logFile = new File(logPath);
		if (!logFile.exists())
			return;
		
		String firstTime = null;
		LocalDateTime now = LocalDateTime.now(); 
		LocalDateTime logTime = null;
		ReversedLinesFileReader lr = null;
		boolean updateWallet = false;
		
		// If we haven't seen a time update event from log make sure we clear currently displayed one
		if (null != lastTimeUpdate && Duration.between(lastTimeUpdate, now).getSeconds() > 45) {
			readTime = ReadTime.EMPTY;
			load = Percentage.EMPTY;
		}
		
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
				if (!readT && null != logTime && Duration.between(logTime, now).getSeconds() > 45) {
					readTime = ReadTime.EMPTY;
					load = Percentage.EMPTY;
					readT = true;
				}
				
				if (i > 1000 || timeStamp.equals(lastTimeStamp)) {
					break;
				}	
				
				if (null != (t = Util.wordAfterIfExist(s, "percent full: ")))
					load = new Percentage(t);
				
				if (s.contains("Farmed unfinished_block") & (!walletNode || fd.coinPrefix.toUpperCase().equals("SIX") || fd.coinPrefix.toUpperCase().equals("OZT"))) {
					Transaction.fromLog(this,s);
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
				} else if (!readWH && null != (t = Util.wordAfterIfExist(s, "wallet peak to height "))) {
					t = t.replace(",", "");
					walletHeight = new Balance(Integer.parseInt(t));
					readWH = true;
				} else if (!readWH && null != (t = Util.wordAfterIfExist(s, "Peak set to : "))) {
					walletHeight = new Balance(Integer.parseInt(t));
					readWH = true;
				} else if (!readWH && null != (t = Util.wordAfterIfExist(s, "Peak set to: "))) {
					walletHeight = new Balance(Integer.parseInt(t));
					readWH = true;
				} else if (!readFNH && null != (t = Util.wordAfterIfExist(s, "Added blocks "))) {
					int multi = t.indexOf('-');
					if (-1 != multi)
						t = t.substring(multi);
					fullnodeHeight = new Balance(Integer.parseInt(t));
					readFNH = true;
				} else if (s.contains("Adding coin") || s.contains("Removing coin")) {
					updateWallet = true;
				} else if(!readFNH && null != (t = Util.wordAfterIfExist(s, "Updated peak to height "))) {
					t = t.replace(",", "");
					fullnodeHeight = new Balance(Integer.parseInt(t));
					readFNH = true;
				}
					
				
			}
			lastTimeStamp = firstTime;
			
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
	
	public List<Peer> loadPeers() {
		peerList.clear();
		boolean singleMode = false;
		Process p = null;
		BufferedReader br = null;
		
		try {
			if (fd.newPeer)
				p = Util.startProcess(fd.exePath, "peer", "-c", "full_node");
			else
				p = Util.startProcess(fd.exePath, "show", "-c");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				
			String l = null;
			while ( null != (l = br.readLine())) {
				if (l.contains("has been renamed")) { // 'chia show -c' has been renamed to 'chia peer -c'
					fd.newPeer = true;
					while ( null != (l = br.readLine())) {}
					Util.waitForProcess(p);
					Util.closeQuietly(br);
					return loadPeers(); // TODO: warning potential infinite loop
				}
				
				if (l.startsWith("Connections:")) {
					l = br.readLine();
					if (l.startsWith("Type") && l.contains("Hash"))
						singleMode = true;
					continue;
				}
					
				if (l.contains("FULL_NODE ")) {
					if (singleMode) {
						peerList.add(Peer.factorySingleLine(l));
		           	} else {
		           		String l2 = br.readLine();
		           		peerList.add(Peer.factoryMultiLine(l + l2));
		           	}
				}
	           		
			}
		} catch (Exception e) {
			lastException = e;
		};
		
		Util.waitForProcess(p);
		Util.closeQuietly(br);
		
		peerList.stream().map(z -> z.height).mapToInt(z -> z).max().ifPresent(i -> maxpeerHeight = new Balance(i));
		
		upload = peerList.stream().map(z -> z.ul).mapToDouble(z -> z).sum();
		download = peerList.stream().map(z -> z.dl).mapToDouble(z -> z).sum();
		
		return peerList;
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
		
		if (Ico.GREEN == statusIcon || Ico.YELLOW == statusIcon)
			fd.atbIcon = Ico.ATB_G;
		
		if (cold)
			statusIcon = Ico.SNOW;
		
	}
	
	public void start() {
		ForkStarter.start((this));
		loadWallet();
	}
	
	public void stop() {
		Util.runProcessWait(fd.exePath,"stop","farmer");
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
		ForkFarmer.newFrame(name + I18n.Fork.showDebugFrameTitle, ico, new DebugView(this));
	}
	
	public Optional<Integer> getIndex() {
		int idx = LIST.indexOf(this);
		return (-1 != idx) ? Optional.of(idx) : Optional.empty();
	}

	public void sendTX(int index, String address, String amt, String fee) {
		Process p = null;
		BufferedReader br = null;
		String txHash = null;
		
		try {
			Transaction t = new Transaction(this,"",Double.parseDouble(amt),"Pending Asset Transaction",new TimeU().toString(),TYPE.PENDING);
			p = Util.startProcess(fd.exePath,"wallet","send","-i",Integer.toString(index),"-a",amt,"-m",fee,"-t",address);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			StringBuilder sb = new StringBuilder();
			
			String l = null;
			while ( null != (l = br.readLine())) {
				sb.append(l + "\n");
				txHash = Util.wordAfterIfExist(l, "-tx ");
				
			}
			
			if (null != txHash) {
				txHash = txHash.substring(2);
				
				t.hash = txHash;
				t.setStatus(address, TYPE.MEMPOOL);
			} else {
				t.setStatus("Failed: " + sb.toString(), TYPE.ERROR);
			}
			t.update();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		TransactionView.refresh();
	}
	
	public TimeU getPreviousWin() {
		return (null == lastWin) ? TimeU.NEVER : lastWin.getTimeSince();
	}

	public static Optional<Fork> getBySymbol(String symbol) {
		return LIST.stream().filter(f -> f.symbol.equals(symbol)).findAny();
	}
	
	public Percentage getEffort() {
		TimeU etw = fd.etw;
		return (etw.known() && null != lastWin) ?
				new Percentage((int) (((double)lastWin.getTimeSince().inMinutes() / (double)etw.inMinutes()) * (double)100)) : Percentage.EMPTY;
	}
	
	public ImageIcon getATBStatus() {
		return statusIcon.equals(Ico.GREEN) ? Ico.ATB_G : fd.atbIcon; 
	}
		
	public void updatePrice(double p) {
		if (p != price) {
			price = p;
			refreshEquity();
		}
		if (null != fd)
			fd.price = p;
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
	
	public static Fork newColdWallet(String address) {
		Optional<ForkData> ofd = ForkData.getbyAddress(address);
		
		if (!ofd.isPresent())
			return null;
		
		ForkData fd = ofd.get();
		if (null == fd.atbPath)
			return null;
		final Fork f = new Fork();
		f.ico = fd.ico;
		f.price = fd.price;
		f.name = fd.displayName;
		f.symbol = fd.coinPrefix;
		f.wallet = new Wallet(address);
		f.walletAddr = address;
		f.walletNode = false;
		f.statusIcon = Ico.SNOW;
		f.cold = true;
		f.farmStatus = "Cold";
		f.readTime = new ReadTime(0);
		f.fd = fd;
		Fork.LIST.add(f);
		ForkView.update();
		return f;
	}

	public static Optional<Fork> getByAddress(String address) {
		return LIST.stream().filter(f -> !f.cold && address.startsWith(f.symbol.toLowerCase())).findAny();	
	}
	
	public void startup() {
		loadVersion();
		checkRPC();
		loadWallets();
		if (!hidden) {
			loadWallet();
			loadFarmSummary();
			loadPeers();
		}
		ForkView.update(this);
	}
	
	private void checkRPC() {
		Process p = null;
		BufferedReader br = null;
		String l;
		
		try {
			p = Util.startProcess(fd.exePath,"rpc");
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
			
			while ( null != (l = br.readLine())) {
				if (l.contains("Commands:")) {
					fd.rpc = true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Util.closeQuietly(br);
		Util.waitForProcess(p);
		
	}

	public List<String> getPlotDirsFromConfig(){
		List<String> chiaPlotDirs = null;
		try {
			String harvesterPlotDirsKey = "harvester.plot_directories";
			YamlUtil chiaCofingYaml = new YamlUtil(this.configPath);
			chiaPlotDirs = chiaCofingYaml.getValueByKey(harvesterPlotDirsKey, null);
		} catch (Exception e) {
			lastException = e;
		}
		if(chiaPlotDirs == null){
			chiaPlotDirs = new ArrayList<>(0);
		}
		return chiaPlotDirs;
	}

	// map item: {fingerprint:[24 words]}
	public List<Map<String, String>> getPrivateKeys() {
		String chiaKeyResponseString = Util.runProcessWait(fd.exePath, "keys", "show", "--show-mnemonic-seed");
		List<Map<String, String>> keyList = new ArrayList<>();
		if (chiaKeyResponseString != null) {
			boolean startFlag = false;
			String[] lines = chiaKeyResponseString.split("\\r?\\n");
			Map<String, String> keyword = null;
			for (String line : lines) {
				if (line != null && line.length() > 0 && line.trim().length() > 0) {
					if (line.startsWith("Fingerprint: ")) {
						String Fingerprint = line.substring("Fingerprint: ".length());
						keyword = new HashMap<>(2);
						keyList.add(keyword);
						keyword.put("Fingerprint", Fingerprint);
					}
					if (startFlag) {
						keyword.put("words", line.trim());
						startFlag = false;
					}
					if (line.indexOf("Mnemonic seed (24 secret words):") > -1) {
						startFlag = true;
					}
				}
			}
		}
		return keyList;
	}

	/**
	 *
	 * @param keyFilePath - The key txt file
	 * @return if add success will return the fingerprint
	 */
	public String importPrivateKey(String keyFilePath) {
		try {
			String execResult = Util.runProcessWait(fd.exePath, "keys", "add", "-f", keyFilePath);
			if(execResult != null && execResult.indexOf("Added private key with public key fingerprint") > -1){
				return execResult.substring("Added private key with public key fingerprint".length()+1).trim();
			}
		} catch (Exception e) {
		}
		return null;
	}

	public void login(Wallet w) {
		wallet = w;
		walletAddr = w.addr;
		Process p = null;
		BufferedReader br = null;
		@SuppressWarnings("unused")
		String l = null;
		
		if (fd.rpc) {
			String query = "{\\\"fingerprint\\\": " + w.fingerprint + "}";
			try {
				p = Util.startProcess(fd.exePath, "rpc", "wallet", "log_in",query);		
				br = new BufferedReader(new InputStreamReader(p.getInputStream()));
				
				while ( null != (l = br.readLine()))
					continue;
				
			} catch (Exception e) {
				
			}
			Util.closeQuietly(br);
			Util.waitForProcess(p);
		}

		
	}

}

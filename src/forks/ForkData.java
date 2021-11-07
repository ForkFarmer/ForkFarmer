package forks;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.ImageIcon;

import types.ReadTime;
import types.TimeU;
import types.Wallet;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.json.JsonObject;

public class ForkData {
	public static final String GITHUB_URL = "https://www.github.com/";
	
	public static Map<String,ForkData> MAP = new HashMap<>();
	public static List<ForkData> LIST = new ArrayList<>();
	private static String USER_HOME = System.getProperty("user.home");
	
	public String coinPrefix;
	public String displayName;
	public String userFolder;
	public String daemonFolder;
	public String daemonFolder2;
	public double price;
	public double nftReward;
	public double baseRatio;
	public double fullReward;
	public long mojoPerCoin;
	
	// computed values
	public String rootPath;
	public String logPath;
	public String configPath;
	public String exePath;
	public ImageIcon ico;
	
	public String discordURL;
	public String gitPath;
	public String xchForksURL;
	public String calculatorURL;
	public String websiteURL;
	public String twitterURL;
	public String exeName;
	String basePath;
	
	public long peakHeight;
	public long peakAge;
	public ImageIcon atbIcon;
	
	public NetSpace netspace;
	public TimeU etw = TimeU.BLANK;
	
	public String atbPath;
	
	public ForkData(JsonObject jo) {
		
		try {
			coinPrefix = ((String) jo.get("coinPrefix")).toUpperCase();
			displayName = (String) jo.get("displayName");
			userFolder = (String) jo.get("userFolder");
			daemonFolder = (String) jo.get("daemonFolder");
			daemonFolder2 = (String) jo.get("daemonFolder2");
			price =  ((BigDecimal)jo.get("price")).doubleValue();
			fullReward = ((BigDecimal)jo.get("reward")).doubleValue();
			baseRatio = ((BigDecimal)jo.get("baseRatio")).doubleValue();
			nftReward = fullReward * baseRatio;
			ico = Ico.getForkIcon(coinPrefix.toLowerCase());
		
			atbPath = (String) jo.get("pathName");
			discordURL = (String) jo.get("discordURL");
			gitPath = (String) jo.get("gitPath");
			xchForksURL = (String) jo.get("xchForksURL");
			websiteURL = (String) jo.get("websiteURL");
			twitterURL = (String) jo.get("twitterURL");
			exeName = (String) jo.get("exeName");
			
			calculatorURL = (String) jo.get("cfcalc");
			mojoPerCoin = jo.containsKey("mojoPerCoin") ? ((BigDecimal)jo.get("mojoPerCoin")).longValue() : 0;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (System.getProperty("os.name").startsWith("Windows"))
			setDirectoriesWin();
		else
			setDirectriesLinux();
		
		MAP.put(coinPrefix, this);
		LIST.add(this);
	}

	private void setDirectriesLinux() {
		basePath = USER_HOME + "/" + daemonFolder + "/";
		logPath = USER_HOME + "/" + userFolder.toLowerCase() + "/mainnet/log/debug.log";
		configPath = USER_HOME + "/" + userFolder.toLowerCase() + "/mainnet/config/config.yaml";
		
		if (coinPrefix.equals("NCH")) {
			logPath = USER_HOME + "/.chia/ext9/log/debug.log";
			configPath = USER_HOME + "/.chia/ext9/config/config.yaml";
			basePath = USER_HOME + "/ext9-blockchain/";
		}

		exePath = basePath + "/venv/bin/" + displayName.toLowerCase();
		if (!new File(exePath).exists())
			exePath = basePath + "/venv/bin/chia";
		
		if (!new File(exePath).exists() && null != daemonFolder2) {
			exePath = USER_HOME + "/" + daemonFolder2 + "/venv/bin/" + displayName.toLowerCase();
		}
		
	}
	
	private void setDirectoriesWin() {
		basePath = USER_HOME + "\\AppData\\Local\\" + daemonFolder + "\\";
		rootPath = USER_HOME + "\\" + userFolder.toLowerCase() + "\\mainnet\\";
		
		logPath = USER_HOME + "\\" + userFolder.toLowerCase() + "\\mainnet\\log\\debug.log";
		configPath = USER_HOME + "\\" + userFolder.toLowerCase() + "\\mainnet\\config\\config.yaml";
		
		if (coinPrefix.equals("NCH")) {
			logPath = USER_HOME + "\\.chia\\ext9\\log\\debug.log";
			configPath = USER_HOME + "\\.chia\\ext9\\config\\config.yaml";
		}
		
		if (!new File(configPath).exists()) { // try environment keys
			String envKey = displayName.toUpperCase() + "_ROOT";
			String val = System.getenv().get(envKey);
			if (null != val) {
				logPath =  val + "\\log\\debug.log";
				configPath = val + "\\config\\config.yaml"; 
			}
		}
		
		exePath = basePath + "\\resources\\app.asar.unpacked\\daemon\\" + displayName + ".exe";
		if (!new File(exePath).exists()) {
			List<String> dirs = Util.getDir(basePath, "app"); // check all the "app" folders
			for (String appDir : dirs) {
				
				if (null != exeName)
					exePath = basePath + appDir + "\\resources\\app.asar.unpacked\\daemon\\" + exeName + ".exe";
				if (new File(exePath).exists())
					break;
				exePath = basePath + appDir + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
				if (new File(exePath).exists())
					break;
				exePath = basePath + appDir + "\\resources\\app.asar.unpacked\\daemon\\" + displayName + ".exe";
				if (new File(exePath).exists())
					break;
			}
			
		}
	}
	
	private void loadFork() {
		if (!new File(exePath).exists())
			return;
		
		Fork f = new Fork();
		f.name = displayName;
		f.fullReward = fullReward;
		f.symbol = coinPrefix;
		f.exePath = exePath;
		f.price = price;
		f.configPath = configPath;
		f.logPath = logPath;
		f.ico = ico;
		f.fd = this;
		
		Fork.LIST.add(f);
	}

	public static void loadFix() {
		
		// first see if we need to patch exePath for any forks
		for (Fork f: Fork.LIST) {
			if (f.cold) {
				f.statusIcon = Ico.SNOW;
				f.wallet = new Wallet(f.walletAddr);
				f.farmStatus = "Cold";
				f.readTime = new ReadTime(0);
			}
			
			for (String addr : f.coldAddrList) {
				Wallet w = new Wallet(addr);
				f.walletList.add(w);
				if (addr.equals(f.walletAddr))
					f.wallet = w;
			}
			
			f.fd = MAP.get(f.symbol);
			
			if (null == f.fd) 
				continue;
			f.ico = f.fd.ico;
			
			if (null != f.exePath && !new File(f.exePath).exists())
				f.exePath = f.fd.exePath;
			
			if (null != f.logPath && !new File(f.logPath).exists()) {
				f.logPath = f.fd.logPath;
				f.configPath = f.fd.configPath;
			}
		}
		
		// next, see if there is any new forks to load
		for (ForkData ft : LIST) {
			if (!Fork.LIST.stream().anyMatch(f -> ft.coinPrefix.equals(f.symbol))) {
				ft.loadFork();
			}
		
		}
	
		
	}
	
	public static Optional<ForkData> getbyAddress(String address) {
		return LIST.stream().filter(f -> address.startsWith(f.coinPrefix.toLowerCase())).findAny();	
	}

	public static Optional<ForkData> getBySymbol(String prefix) {
		return LIST.stream().filter(f -> prefix.equals(f.coinPrefix.toLowerCase())).findAny();
	}
}

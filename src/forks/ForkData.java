package forks;

import java.io.File;
import java.io.FileNotFoundException;
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
	public String daemonFolder3;
	public double price;
	public double nftReward;
	public double baseRatio;
	public double fullReward;
	public long mojoPerCoin;
	public long preFarm;
	
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
	String daemonBase;
	
	public long peakHeight;
	public long peakAge;
	public ImageIcon atbIcon;
	public boolean hidden;
	
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
			daemonFolder3 = (String) jo.get("daemonFolder3");
			price =  ((BigDecimal)jo.get("price")).doubleValue();
			fullReward = ((BigDecimal)jo.get("reward")).doubleValue();
			baseRatio = ((BigDecimal)jo.get("baseRatio")).doubleValue();
			preFarm = ((BigDecimal)jo.get("prefarm")).longValue();
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
		daemonBase = USER_HOME + "/" + daemonFolder + "/";
		logPath = USER_HOME + "/" + userFolder.toLowerCase() + "/mainnet/log/debug.log";
		configPath = USER_HOME + "/" + userFolder.toLowerCase() + "/mainnet/config/config.yaml";
		
		if (coinPrefix.equals("NCH")) {
			logPath = USER_HOME + "/.chia/ext9/log/debug.log";
			configPath = USER_HOME + "/.chia/ext9/config/config.yaml";
			daemonBase = USER_HOME + "/ext9-blockchain/";
		}

		
		checkDaemonLinux(USER_HOME + "/" + daemonFolder + "/");
		if (new File(exePath).exists() || null == daemonFolder2)
			return;
		checkDaemonLinux(USER_HOME + "/" + daemonFolder2 + "/");
		if (new File(exePath).exists() || null == daemonFolder3)
			return;
		checkDaemonLinux(USER_HOME + "/" + daemonFolder3 + "/");
	}
	
	private void checkDaemonLinux(String daemonPath) {
		exePath = daemonPath + "/venv/bin/" + displayName.toLowerCase();
		if (new File(exePath).exists())
			return;
		
		exePath = daemonPath + "/venv/bin/chia";
		
		if (new File(exePath).exists() || null == exeName)
			return;
		
		exePath = daemonPath + "/venv/bin/" + exeName.toLowerCase();
	}
	
	private void setDirectoriesWin() {
		rootPath = USER_HOME + "\\" + userFolder.toLowerCase() + "\\mainnet\\";
		
		logPath = rootPath + "log\\debug.log";
		configPath = rootPath + "config\\config.yaml";
		
		if (coinPrefix.equals("NCH")) {
			logPath = USER_HOME + "\\.chia\\ext9\\log\\debug.log";
			configPath = USER_HOME + "\\.chia\\ext9\\config\\config.yaml";
		}
		
		if (coinPrefix.equals("HCX")) {
			logPath = USER_HOME + "\\.chinilla\\vanillanet\\log\\debug.log";
			configPath = USER_HOME + "\\.chinilla\\vanillanet\\config\\config.yaml";
		}
		
		if (!new File(configPath).exists()) { // try environment keys
			String envKey = displayName.toUpperCase() + "_ROOT";
			rootPath = System.getenv().get(envKey);
			if (null != rootPath) {
				logPath =  rootPath + "\\log\\debug.log";
				configPath = rootPath + "\\config\\config.yaml"; 
			}
		}
		
		checkDaemonWin(USER_HOME + "\\AppData\\Local\\" + daemonFolder + "\\");
		if (new File(exePath).exists() || null == daemonFolder2)
			return;
		checkDaemonWin(USER_HOME + "\\AppData\\Local\\" + daemonFolder2 + "\\");
		
	}
	
	public void checkDaemonWin(String daemonBase) {
		exePath = daemonBase + "\\resources\\app.asar.unpacked\\daemon\\" + displayName + ".exe";
		if (new File(exePath).exists())
			return;
		
		// check program files
		exePath = System.getenv("ProgramFiles") + "\\" + displayName + "\\resources\\app.asar.unpacked\\daemon\\" + displayName + ".exe";
		
		if (new File(exePath).exists())
			return;
		
		List<String> dirs = Util.getDir(daemonBase, "app"); // check all the "app" folders
		for (String appDir : dirs) {
			if (null != exeName)
				exePath = daemonBase + appDir + "\\resources\\app.asar.unpacked\\daemon\\" + exeName + ".exe";
			if (new File(exePath).exists())
				return;
			exePath = daemonBase + appDir + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
			if (new File(exePath).exists())
				return;
			exePath = daemonBase + appDir + "\\resources\\app.asar.unpacked\\daemon\\" + displayName + ".exe";
			if (new File(exePath).exists())
				return;
		}
	}
	
	
	
	public Fork loadFork() throws FileNotFoundException {
		if (!new File(exePath).exists())
			throw new FileNotFoundException(exePath);
		
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
		
		return f;
	}

	public static void loadFix() {
		// first see if we need to patch exePath for any forks
		for (Fork f: Fork.LIST) {
			f.bgColor = Util.hexToColor(f.hexColor);
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
				try {
					Fork f = ft.loadFork();
					Fork.LIST.add(f);
				} catch (FileNotFoundException e) {
					// fork doesn't exist... skip.
				}
					
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

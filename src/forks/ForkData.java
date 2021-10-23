package forks;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.ImageIcon;

import org.json.simple.JSONObject;

import types.ReadTime;
import types.Wallet;
import util.Ico;
import util.Util;

public class ForkData {
	public static Map<String,ForkData> MAP = new HashMap<>();
	public static List<ForkData> LIST = new ArrayList<>();
	private static String USER_HOME = System.getProperty("user.home");
	
	public String coinPrefix;
	public String displayName;
	public String userFolder;
	public String daemonFolder;
	public String daemonFolder2;
	public double price;
	public double rewardTrigger;
	public long mojoPerCoin;
	
	// computed values
	public String logPath;
	public String configPath;
	public String exePath;
	public ImageIcon ico;
	
	public String discordURL;
	public String gitURL;
	public String xchForksURL;
	public String calculatorURL;
	public String websiteURL;
	
	public String atbPath;
	
	
	
	public ForkData(JSONObject jo) {
		coinPrefix = ((String) jo.get("coinPrefix")).toUpperCase();
		displayName = (String) jo.get("displayName");
		userFolder = (String) jo.get("userFolder");
		daemonFolder = (String) jo.get("daemonFolder");
		daemonFolder2 = (String) jo.get("daemonFolder2");
		price = (Double) jo.get("price");
		rewardTrigger = (Double) jo.get("rewardTrigger");
		ico = Ico.getForkIcon(coinPrefix.toLowerCase());
	
		atbPath = (String) jo.get("pathName");
		discordURL = (String) jo.get("discordURL");
		gitURL = (String) jo.get("gitURL");
		xchForksURL = (String) jo.get("xchForksURL");
		websiteURL = (String) jo.get("websiteURL");
		
		calculatorURL = (String) jo.get("cfcalc");
		Long mojoPerCoinL = (Long) jo.get("mojoPerCoin");
		mojoPerCoin = (null != mojoPerCoinL) ? mojoPerCoinL : 0;
		
		if (System.getProperty("os.name").startsWith("Windows"))
			setDirectoriesWin();
		else
			setDirectriesLinux();
		
		MAP.put(coinPrefix, this);
		LIST.add(this);
	}

	private void setDirectriesLinux() {
		String forkBase = USER_HOME + "/" + daemonFolder + "/";
		logPath = USER_HOME + "/" + userFolder.toLowerCase() + "/mainnet/log/debug.log";
		configPath = USER_HOME + "/" + userFolder.toLowerCase() + "/mainnet/config/config.yaml";
		
		if (coinPrefix.equals("NCH")) {
			logPath = USER_HOME + "/.chia/ext9/log/debug.log";
			configPath = USER_HOME + "/.chia/ext9/config/config.yaml";
			forkBase = USER_HOME + "/ext9-blockchain/";
		}

		exePath = forkBase + "/venv/bin/" + displayName.toLowerCase();
		if (!new File(exePath).exists())
			exePath = forkBase + "/venv/bin/chia";
		
		if (!new File(exePath).exists() && null != daemonFolder2) {
			exePath = USER_HOME + "/" + daemonFolder2 + "/venv/bin/" + displayName.toLowerCase();
		}
		
	}

	private void setDirectoriesWin() {
		String forkBase = USER_HOME + "\\AppData\\Local\\" + daemonFolder + "\\";
		
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
		
		exePath = forkBase + "\\resources\\app.asar.unpacked\\daemon\\" + displayName + ".exe";
		if (!new File(exePath).exists()) {
			List<String> dirs = Util.getDir(forkBase, "app"); // check all the "app" folders
			for (String appDir : dirs) {
				exePath = forkBase + appDir + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
				if (new File(exePath).exists())
					break;
				exePath = forkBase + appDir + "\\resources\\app.asar.unpacked\\daemon\\" + displayName + ".exe";
				if (new File(exePath).exists())
					break;
			}
			
		}
	}

	public static Optional<ForkData> getbyAddress(String address) {
		return LIST.stream().filter(f -> address.startsWith(f.coinPrefix.toLowerCase())).findAny();	
	}
	
	private void loadFork() {
		if (!new File(exePath).exists())
			return;
		
		Fork f = new Fork();
		f.name = displayName;
		f.rewardTrigger = rewardTrigger;
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
	
}

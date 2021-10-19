package forks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.ImageIcon;

import types.ReadTime;
import types.Wallet;
import util.Ico;
import util.Util;

public class ForkTemplate {
	public static Map<String,ForkTemplate> MAP = new HashMap<>();
	public static List<ForkTemplate> LIST = new ArrayList<>();
	private static String USER_HOME = System.getProperty("user.home");
	
	public String symbol;
	public String name;
	public String dataFolder;
	public String daemonFolder;
	public double price;
	public double rewardTrigger;
	
	// computed values
	public String logPath;
	public String configPath;
	public String exePath;
	public ImageIcon ico;
	
	public ForkTemplate(String symbol, String name, String dataFolder, String daemonFolder, double price, double rewardTrigger) {
		this.symbol = symbol;
		this.name = name;
		this.dataFolder = dataFolder;
		this.daemonFolder = daemonFolder;
		this.price = price;
		this.rewardTrigger = rewardTrigger;
		this.ico = Ico.getForkIcon(name);
		
		String forkBase;
				
		if (System.getProperty("os.name").startsWith("Windows")) {
			forkBase = USER_HOME + "\\AppData\\Local\\" + daemonFolder + "\\";
			
			logPath = USER_HOME + "\\" + dataFolder.toLowerCase() + "\\mainnet\\log\\debug.log";
			configPath = USER_HOME + "\\" + dataFolder.toLowerCase() + "\\mainnet\\config\\config.yaml";
			if (symbol.equals("NCH")) {
				logPath = USER_HOME + "\\.chia\\ext9\\log\\debug.log";
				configPath = USER_HOME + "\\.chia\\ext9\\config\\config.yaml";
			}
			
			if (!new File(configPath).exists()) { // try enviroment keys
				String envKey = name.toUpperCase() + "_ROOT";
				String val = System.getenv().get(envKey);
				if (null != val) {
					logPath =  val + "\\log\\debug.log";
					configPath = val + "\\config\\config.yaml"; 
				}
			}
		} else {
			forkBase = USER_HOME + "/" + daemonFolder + "/";
			logPath = USER_HOME + "/" + dataFolder.toLowerCase() + "/mainnet/log/debug.log";
			configPath = USER_HOME + "/" + dataFolder.toLowerCase() + "/mainnet/config/config.yaml";
			
			if (symbol.equals("NCH")) {
				logPath = USER_HOME + "/.chia/ext9/log/debug.log";
				configPath = USER_HOME + "/.chia/ext9/config/config.yaml";
				forkBase = USER_HOME + "/ext9-blockchain/";
			}
			
		}
		
		try {
			if (System.getProperty("os.name").startsWith("Windows")) {
				exePath = forkBase + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
				if (!new File(exePath).exists()) {
					List<String> dirs = Util.getDir(forkBase, "app"); // check all the "app" folders
					for (String appDir : dirs) {
						exePath = forkBase + appDir + "\\resources\\app.asar.unpacked\\daemon\\chia.exe";
						if (new File(exePath).exists())
							break;
						exePath = forkBase + appDir + "\\resources\\app.asar.unpacked\\daemon\\" + name + ".exe";
						if (new File(exePath).exists())
							break;
					}
					
				}
			} else {
				exePath = forkBase + "/venv/bin/" + name.toLowerCase();
				if (!new File(exePath).exists())
					exePath = forkBase + "/venv/bin/chia";
			}

			
		} catch (IOException e) {
			// Didn't load properly for whatever reason
		}
		
		MAP.put(symbol, this);
		LIST.add(this);
		
	}
	
	public static Optional<ForkTemplate> getbyAddress(String address) {
		return LIST.stream().filter(f -> address.startsWith(f.symbol.toLowerCase())).findAny();	
	}
	
	

	private void loadFork() {
		if (!new File(exePath).exists())
			return;
		
		Fork f = new Fork();
		f.name = name;
		f.rewardTrigger = rewardTrigger;
		f.symbol = symbol;
		f.exePath = exePath;
		f.price = price;
		f.configPath = configPath;
		f.logPath = logPath;
		f.ico = ico;
		
		Fork.LIST.add(f);
	}

	public static void loadFix() {
		
		// first see if we need to patch exePath for any forks
		for (Fork f: Fork.LIST) {
			if (f.cold) {
				f.statusIcon = Ico.SNOW;
				f.wallet = new Wallet("",f.walletAddr,0);
				f.farmStatus = "Cold";
				f.readTime = new ReadTime(0);
			}
			
			ForkTemplate ft = MAP.get(f.symbol);
			if (null == ft)
				continue;
			f.ico = ft.ico;
			
			if (null != f.exePath && !new File(f.exePath).exists())
				if (null != ft)
					f.exePath = ft.exePath;
			
			if (null != f.logPath && !new File(f.logPath).exists())
				if (null != ft)
					f.logPath = ft.logPath;
					f.configPath = ft.configPath;
			}
		
		// next, see if there is any new forks to load
		for (ForkTemplate ft : LIST) {
			if (!Fork.LIST.stream().anyMatch(f -> ft.symbol.equals(f.symbol))) {
				ft.loadFork();
			}
		
		}
	}
	
}

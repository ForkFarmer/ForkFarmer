package forks;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
	
	public ForkTemplate(String symbol, String name, String dataFolder, String daemonFolder, double price, double rewardTrigger) {
		this.symbol = symbol;
		this.name = name;
		this.dataFolder = dataFolder;
		this.daemonFolder = daemonFolder;
		this.price = price;
		this.rewardTrigger = rewardTrigger;
		
		String forkBase;
				
		if (System.getProperty("os.name").startsWith("Windows")) {
			forkBase = USER_HOME + "\\AppData\\Local\\" + daemonFolder + "\\";
			logPath = USER_HOME + "\\" + dataFolder.toLowerCase() + "\\mainnet\\log\\debug.log";
			configPath = USER_HOME + "\\" + dataFolder.toLowerCase() + "\\mainnet\\config\\config.yaml";
			if (symbol.equals("NCH"))
				logPath = USER_HOME + "\\.chia\\ext9\\log\\debug.log";
		} else {
			forkBase = USER_HOME + "/" + daemonFolder + "/";
			logPath = USER_HOME + "/" + dataFolder.toLowerCase() + "/mainnet/log/debug.log";
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
			}

			if (!new File(exePath).exists())
				return;
			
			MAP.put(symbol, this);
			LIST.add(this);
		} catch (IOException e) {
			// Didn't load the fork for whatever reason
		}
		
	}

	private void loadFork() {
		Fork f = new Fork();
		f.name = name;
		f.symbol = symbol;
		f.exePath = exePath;
		f.price = price;
		f.configPath = configPath;
		f.logPath = logPath;
		Fork.LIST.add(f);
	}

	public static void loadFix() {

		// first see if we need to patch exepath for any forks
		for (Fork f: Fork.LIST) {
			if (!new File(f.exePath).exists()) {
				ForkTemplate ft = MAP.get(f.symbol);
				if (null != ft)
					f.exePath = ft.exePath;
			}
		}
		
		// next, see if there is any new forks to load
		for (ForkTemplate ft : LIST) {
			if (!Fork.LIST.stream().anyMatch(f -> ft.symbol.equals(f.symbol))) {
				ft.loadFork();
			}
		
		}
	}
	
}

package main;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import forks.Fork;
import forks.ForkView;
import util.Ico;
import util.swing.SwingUtil;

public class Settings {
	private static final String SETTINGS_PATH = "FF_Settings.yaml";
	public static Gui GUI = new Gui();
	
	static Map<String, Object> Settings = new HashMap<>();
	
	public static class Gui {
		public int logReaderIntraDelay;
		public int logReaderExoDelay;
		public int daemonReaderWorkers;
		public int daemonReaderDelay;
		public Dimension txViewDimension;
		public Dimension forkViewDimension;
	}
	
	@SuppressWarnings("unchecked")
	public static void Load() {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(new File(SETTINGS_PATH));
			Yaml yaml = new Yaml();
			Map<String, Object> settings = yaml.load(inputStream);
			GUI = (Gui) settings.get("Gui Settings");
			Fork.LIST = (List<Fork>) settings.get("Fork List");
		
			/*
			List<Col<Fork>> colList = (List<Col<Fork>>) settings.get("Fork View Columns");
			
			for (Col<Fork> c : colList) {
				Col<Fork> newCol = ForkView.MODEL.colMap.get(c.name);
				newCol.width = c.width;
				newCol.show = c.show;
			}
			*/
			
		} catch (Exception e) {
			//e.printStackTrace();
			loadDefaults(); // problem reading or parsing settings, use defaults
			GUI.logReaderIntraDelay = 100;
			GUI.logReaderExoDelay = 5000;
			GUI.daemonReaderWorkers = 2;
			GUI.daemonReaderDelay = 5000;
			GUI.txViewDimension = new Dimension(900,250);
			GUI.forkViewDimension = new Dimension(700,400);
		}
		Fork.LIST.forEach(f -> f.ico = Ico.getForkIcon(f.name));
		ForkView.update();
	}
	
	public static void loadDefaults() {
		// xchforks.com order
		new Fork("XCH","Chia", ".chia", "chia-blockchain", 215.0, .25);
		new Fork("XFX","Flax", ".flax", "flax-blockchain", 0.85, .25);
		new Fork("NCH","NChainExt9", ".chia", "NChainExt9-blockchain", 0.25,2);
		new Fork("SPARE","Spare", ".spare-blockchain", "spare-blockchain", .04,.5);
		new Fork("SIT","Silicoin", ".silicoin", "silicoin-blockchain", .3,.25);
		new Fork("XFL","Flora", ".flora", "flora-blockchain", .1,.5);
		new Fork("XCC","Chives", ".chives", "chives-blockchain", 0.01,22.5);
		new Fork("CGN","Chaingreen", ".chaingreen", "chaingreen-blockchain", .0003,62.5);
		new Fork("GDOG","Greendoge", ".greendoge", "greendoge-blockchain", .0058,2.5);
		new Fork("HDD","Hddcoin", ".hddcoin", "hddcoin-blockchain", .1,.25);
		new Fork("XDG","DogeChia", ".dogechia", "dogechia-blockchain", .05,.25);
		new Fork("XGJ","Goji", ".goji-blockchain", "goji-blockchain", 0.02,.25);
		new Fork("TAD","Tad", ".tad", "tad-blockchain", .02,2.0);
		new Fork("AVO","Avocado", ".avocado", "avocado-blockchain", .005,.25);
		new Fork("APPLE","Apple", ".apple", "apple-blockchain", 0.04,.25);
		new Fork("XKA","Kale", ".kale", "kale-blockchain", .01,.25);
		new Fork("XBTC","BTCgreen", ".btcgreen", "btcgreen-blockchain", .01,1);
		new Fork("COV","Covid", ".covid", "covid-blockchain", 0.002,10.0);
		new Fork("XMZ","Maize", ".maize", "maize-blockchain", 0.01,0.25);
		new Fork("XTX","Taco", ".taco", "taco-blockchain", .02,.25);
		new Fork("XSE","Seno", ".seno2", "seno-blockchain", 0.015,.25);
		new Fork("XCD","CryptoDoge", ".cryptodoge", "cryptodoge", 0.00,2500);
		new Fork("WHEAT","Wheat", ".wheat", "wheat-blockchain", .02,0.25);
		new Fork("SOCK","Socks", ".socks", "socks-blockchain", 0.01,.25);
		new Fork("XCR","Chiarose", ".chiarose", "chia-rosechain", 0.0004,25.0);
		new Fork("XMX","Melati", ".melati", "melati-blockchain", .01,.25);
		new Fork("CAC","Cactus", ".cactus", "cactus-blockchain", .02,.25);
		new Fork("CANS","Cannabis", ".cannabis", "cannabis-blockchain", 0,16);
		new Fork("SCM","Scam", ".scam", "scam-blockchain", 0.00,10.0);		
		new Fork("XEQ","Equality", ".equality", "equality-blockchain", 0.0,3.5);
		new Fork("XSC","Sector", ".sector", "sector-blockchain", .002,.25);
		new Fork("XFK","Fork", ".fork", "fork-blockchain", 0.0,6.25);
		new Fork("SIX","Lucky", ".lucky", "lucky-blockchain", 0.0,4.0);
		
		new Fork("PIPS","PipsCoin", ".pipscoin", "pipscoin-blockchain", 0.0,5);
		new Fork("OZT","GoldCoin", ".goldcoin", "goldcoin-blockchain", 0.0,.25);
	
		new Fork("VAG","Cunt", ".cunt", "cunt-blockchain", 0.0,8.625);
		new Fork("XCL","Caldera", ".caldera", "caldera-blockchain", 0.0,.25);
		new Fork("SRN","Shamrock", ".shamrock", "shamrock-blockchain", 0.0,.25);
		new Fork("XBR","Beer", ".beernetwork", "beer-blockchain", 0.0,.25);
		new Fork("FFK","Fishery", ".fishery", "fishery", 0.0,.075);
		new Fork("XOL","Olive", ".olive", "olive-blockchain", 0.0,.25);
		new Fork("SSD","SSDCoin", ".ssdcoin", "sddcoin-blockchain", 0.0,.25);
		new Fork("XKW","Kiwi", ".kiwi", "kiwi-blockchain", 0.0,.25);
		new Fork("XCHA","XCHA", ".xcha", "xcha-blockchain", 0.0,.25);
		new Fork("XBT","Beet", ".beet", "beet-blockchain", 0.0,.25);
		new Fork("XTH","Thyme", ".thyme", "thyme-blockchain", 0.0,.25);
		new Fork("LLC","LittleLamboCoin", ".littlelambocoin", "littlelambocoin", 0.0,.25);
		new Fork("XACH","Achi", ".achi", "achi-blockchain", 0.0,.25);
		new Fork("STOR","Stor", ".stor", "stor-blockchain", 0.0,.5);
		new Fork("XNT","Skynet", ".skynet", "skynet-blockchain", 0.0, .625);
		new Fork("PEA","Peas", ".peas", "peas-blockchain", 0.0,.25);
		new Fork("XKM","Mint", ".mint", "mint-blockchain", 0.0,.25);
		new Fork("LCH","Lotus", ".lotus", "lotus-blockchain", 0.0,.25);
		new Fork("MGA","Mogua", ".mogua", "mogua-blockchain", 0.0,2.5);
	}
	
	public static void Save() {
		synchronized (Fork.LIST) {
			SwingUtil.mapViewToModel(ForkView.TABLE,Fork.LIST);
		}
		
		Settings.put("Fork List", Fork.LIST);
		Settings.put("Gui Settings", GUI);
		//Settings.put("Fork View Columns", ForkView.MODEL.colList);
		
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(SETTINGS_PATH);
			Yaml yaml = new Yaml(options);
			yaml.dump(Settings, writer);
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

package main;

import java.awt.Dimension;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import forks.Fork;
import forks.ForkStarter;
import forks.ForkTemplate;
import forks.ForkView;
import transaction.TransactionView;
import util.Ico;
import util.swing.SwingUtil;

public class Settings {
	private static final String SETTINGS_PATH = "FF_Settings.yaml";
	public static Gui GUI = new Gui();
	
	public static Map<String, Object> settings = new HashMap<>();
	
	public static class Gui {
		public int logReaderIntraDelay;
		public int logReaderExoDelay;
		public int daemonReaderWorkers;
		public int daemonReaderDelay;
		public Dimension txViewDimension;
		public Dimension forkViewDimension;
		
		public String currencySymbol = "$";
		public double currencyRatio = 1;
		public String custLastCustom = "";
		public String custLastDelay = "60";
		public boolean custForceUpdate = false;
	}
	
	@SuppressWarnings("unchecked")
	public static void Load() {
		new ForkTemplate("XCH","Chia", ".chia", "chia-blockchain", 160.0, .25);
		new ForkTemplate("XFX","Flax", ".flax", "flax-blockchain", 0.4, .25);
		new ForkTemplate("NCH","NChainExt9", ".chia", "NChainExt9-blockchain", 0.2,2);
		new ForkTemplate("SPARE","Spare", ".spare-blockchain", "spare-blockchain", .04,.25);
		new ForkTemplate("TSIT","Silicoin", ".silicoin", "silicoin-blockchain", .3,.25);
		new ForkTemplate("XFL","Flora", ".flora", "flora-blockchain", .1,.5);
		new ForkTemplate("XCC","Chives", ".chives", "chives-blockchain", 0.01,22.5);
		new ForkTemplate("CGN","Chaingreen", ".chaingreen", "chaingreen-blockchain", .0003,62.5);
		new ForkTemplate("GDOG","Greendoge", ".greendoge", "greendoge-blockchain", .058,2.5);
		new ForkTemplate("HDD","Hddcoin", ".hddcoin", "hddcoin-blockchain", .15,.25);
		new ForkTemplate("XDG","DogeChia", ".dogechia", "dogechia-blockchain", .05,.25);
		new ForkTemplate("XGJ","Goji", ".goji-blockchain", "goji-blockchain", 0.02,.25);
		new ForkTemplate("TAD","Tad", ".tad", "tad-blockchain", .02,2.0);
		new ForkTemplate("AVO","Avocado", ".avocado", "avocado-blockchain", .005,.25);
		new ForkTemplate("APPLE","Apple", ".apple", "apple-blockchain", 0.04,.25);
		new ForkTemplate("XKA","Kale", ".kale", "kale-blockchain", .01,.25);
		new ForkTemplate("XBTC","BTCgreen", ".btcgreen", "btcgreen-blockchain", .01,1);
		new ForkTemplate("COV","Covid", ".covid", "covid-blockchain", 0.002,10.0);
		new ForkTemplate("XMZ","Maize", ".maize", "maize-blockchain", 0.01,0.25);
		new ForkTemplate("XTX","Taco", ".taco", "taco-blockchain", .02,.25);
		new ForkTemplate("XSE","Seno", ".seno2", "seno-blockchain", 0.015,.25);
		new ForkTemplate("XCD","CryptoDoge", ".cryptodoge", "cryptodoge", 0.00,2500);
		new ForkTemplate("WHEAT","Wheat", ".wheat", "wheat-blockchain", .02,0.25);
		new ForkTemplate("SOCK","Socks", ".socks", "socks-blockchain", 0.01,.25);
		new ForkTemplate("XCR","Chiarose", ".chiarose", "chia-rosechain", 0.0004,25.0);
		new ForkTemplate("XMX","Melati", ".melati", "melati-blockchain", .01,.25);
		new ForkTemplate("CAC","Cactus", ".cactus", "cactus-blockchain", .02,.25);
		new ForkTemplate("CANS","Cannabis", ".cannabis", "cannabis-blockchain", 0,16);
		new ForkTemplate("SCM","Scam", ".scam", "scam-blockchain", 0.00,10.0);		
		new ForkTemplate("XEQ","Equality", ".equality", "equality-blockchain", 0.0,3.5);
		new ForkTemplate("XSC","Sector", ".sector", "sector-blockchain", .002,.25);
		new ForkTemplate("XFK","Fork", ".fork", "fork-blockchain", 0.0,6.25);
		new ForkTemplate("SIX","Lucky", ".lucky", "lucky-blockchain", 0.0,4.0);
		new ForkTemplate("PIPS","PipsCoin", ".pipscoin", "pipscoin-blockchain", 0.0,5);
		new ForkTemplate("OZT","GoldCoin", ".goldcoin", "goldcoin-blockchain", 0.0,.25);
		new ForkTemplate("VAG","Cunt", ".cunt", "cunt-blockchain", 0.0,8.625);
		new ForkTemplate("XCL","Caldera", ".caldera", "caldera-blockchain", 0.0,.25);
		new ForkTemplate("SRN","Shamrock", ".shamrock", "shamrock-blockchain", 0.0,.25);
		new ForkTemplate("XBR","Beer", ".beernetwork", "beer-blockchain", 0.0,.25);
		new ForkTemplate("FFK","Fishery", ".fishery", "fishery", 0.0,.075);
		new ForkTemplate("XOL","Olive", ".olive", "olive-blockchain", 0.0,.25);
		new ForkTemplate("SSD","SSDCoin", ".ssdcoin", "sddcoin-blockchain", 0.0,.25);
		new ForkTemplate("XKW","Kiwi", ".kiwi", "kiwi-blockchain", 0.0,2);
		new ForkTemplate("XCHA","XCHA", ".xcha", "xcha-blockchain", 0.0,.25);
		new ForkTemplate("XBT","Beet", ".beet", "beet-blockchain", 0.0,.25);
		new ForkTemplate("XTH","Thyme", ".thyme", "thyme-blockchain", 0.0, .5890);
		new ForkTemplate("LLC","LittleLamboCoin", ".littlelambocoin", "littlelambocoin", 0.0,.25);
		new ForkTemplate("XACH","Achi", ".achi", "achi-blockchain", 0.0,.25);
		new ForkTemplate("STOR","Stor", ".stor", "stor-blockchain", 0.0,.5);
		new ForkTemplate("XNT","Skynet", ".skynet", "skynet-blockchain", 0.0, .625);
		new ForkTemplate("PEA","Peas", ".peas", "peas-blockchain", 0.0,1);
		new ForkTemplate("XKM","Mint", ".mint", "mint-blockchain", 0.0,.5);
		new ForkTemplate("LCH","Lotus", ".lotus", "lotus-blockchain", 0.0,.25);
		new ForkTemplate("MGA","Mogua", ".mogua", "mogua-blockchain", 0.0,1.25);
		new ForkTemplate("TRZ","Tranzact", ".tranzact", "tranzact-blockchain", 0.0,.625);
		new ForkTemplate("XSLV","Salvia", ".salvia", "salvia-blockchain", 0.0,.25);
		new ForkTemplate("STAI","Staicoin", ".staicoin", "staicoin-blockchain", 0.0,1);
		new ForkTemplate("XVM","Venidium", ".venidium", "venidium-blockchain", 0.0,80);
		new ForkTemplate("MELON","mELON", ".melon", "melon-blockchain", 0.0,1);
		new ForkTemplate("XKJ","Kujenga", ".kujenga", "kujenga-blockchain", 0.0,.25);
		new ForkTemplate("AEC","Aedge", ".aedge", "aedge-blockchain", 0.0,.25);
		
		InputStream inputStream;
		
		try {
			inputStream = new FileInputStream(new File(SETTINGS_PATH));
			Yaml yaml = new Yaml();
			settings = yaml.load(inputStream);
			GUI = (Gui) settings.get("Gui Settings");
			Fork.LIST = (List<Fork>) settings.get("Fork List");
		} catch (Exception e) {
			e.printStackTrace();
			GUI.logReaderIntraDelay = 100;
			GUI.logReaderExoDelay = 5000;
			GUI.daemonReaderWorkers = 2;
			GUI.daemonReaderDelay = 5000;
			GUI.txViewDimension = new Dimension(900,250);
			GUI.forkViewDimension = new Dimension(700,400);
		}
		
		
		ForkTemplate.loadFix();		
		Fork.LIST.forEach(f -> f.ico = Ico.getForkIcon(f.name));
		Fork.I_LIST = new ArrayList<>(Fork.LIST);
		ForkView.update();
		
	}
	
	public static void Save() {
		synchronized (Fork.LIST) {
			SwingUtil.mapViewToModel(ForkView.TABLE,Fork.LIST);
		}
		settings.clear();
		settings.put("Fork List", Fork.LIST);
		settings.put("Gui Settings", GUI);
		settings.put("ForkView Columns", ForkView.MODEL.getColsDisplayOrder(ForkView.TABLE));
		settings.put("TxView Columns", TransactionView.MODEL.getColsDisplayOrder(TransactionView.TABLE));
		settings.put("ForkStarter", ForkStarter.getSettings());
		
		
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(SETTINGS_PATH);
			Yaml yaml = new Yaml(options);
			yaml.dump(settings, writer);
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}

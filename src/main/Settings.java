package main;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import forks.Fork;

public class Settings {
	private static final String SETTINGS_PATH = "FF_Settings.yaml";
	
	
	public static void Load() {
		InputStream inputStream;
		try {
			inputStream = new FileInputStream(new File(SETTINGS_PATH));
			Yaml yaml = new Yaml();
			Fork.LIST = yaml.load(inputStream);
		} catch (Exception e) {
			loadDefaults(); // problem reading or parsing settings, use defaults
		}
		Fork.LIST.forEach(Fork::loadIcon);
	}
	
	public static void loadDefaults() {
		new Fork("XCH","Chia", ".chia", "chia-blockchain", 245.0, .25);
		new Fork("XFX","Flax", ".flax", "flax-blockchain", 0.9, .25);
		new Fork("SIT","Silicoin", ".silicoin", "silicoin-blockchain", .35,.25);
		new Fork("SPARE","Spare", ".spare", "spare-blockchain", .04,.5);
		new Fork("CGN","Chaingreen", ".chaingreen", "chaingreen-blockchain", .004,62.5);
		new Fork("XFL","Flora", ".flora", "flora-blockchain", .05,3.5);
		new Fork("XGJ","Goji", ".goji-blockchain", "goji-blockchain", 0.02,.25);
		new Fork("XDG","DogeChia", ".dogechia", "dogechia-blockchain", .04,.25);
		new Fork("HDD","Hddcoin", ".hddcoin", "hddcoin-blockchain", .03,.25);
		new Fork("GDOG","Greendoge", ".greendoge", "greendoge-blockchain", .005,12.5);
		new Fork("AVO","Avocado", ".avocado", "avocado-blockchain", .01,.25);
		new Fork("XCR","Chiarose", ".chiarose", "chia-rosechain", 0.0004,25.0);
		new Fork("XSE","Seno", ".seno2", "seno-blockchain", 0.02,.25);
		new Fork("APPLE","Apple", ".apple", "apple-blockchain", 0.04,.25);
		new Fork("XKA","Kale", ".kale", "kale-blockchain", .015,.25);
		new Fork("XTX","Taco", ".taco", "taco-blockchain", .02,.25);
		new Fork("TAD","Tad", ".tad", "tad-blockchain", .013,2.0);
		new Fork("XMX","Melati", ".melati", "melati-blockchain", .03,.25);
		new Fork("WHEAT","Wheat", ".wheat", "wheat-blockchain", .02,12.5);
		new Fork("XMZ","Maize", ".maize", "maize-blockchain", 0.003,22.5);
		new Fork("SOCK","Socks", ".socks", "socks-blockchain", 0.01,.25);
		new Fork("CAC","Cactus", ".cactus", "cactus-blockchain", .04,.25);
		new Fork("XBTC","BTCgreen", ".btcgreen", "btcgreen-blockchain", 0,1);
		new Fork("CANS","Cannabis", ".cannabis", "cannabis-blockchain", 0,16);
		new Fork("COV","Covid", ".covid", "covid-blockchain", 0.001,10.0);
		new Fork("XEQ","Equality", ".equality", "equality-blockchain", 0.0,3.5);
		new Fork("XSC","Sector", ".sector", "sector-blockchain", .002,.25);
		new Fork("XFK","Fork", ".fork", "fork-blockchain", 0.0,6.25);
		new Fork("SCM","Scam", ".scam", "scam-blockchain", 0.00,10.0);
		new Fork("XCC","Chives", ".chives", "chives-blockchain", 0.0,22.5);
		new Fork("VAG","Cunt", ".cunt", "cunt-blockchain", 0.0,8.625);
		new Fork("XCL","Caldera", ".caldera", "caldera-blockchain", 0.0,.25);
		new Fork("SIX","Lucky", ".lucky", "lucky-blockchain", 0.0,4.0);
		new Fork("XCD","CryptoDoge", ".cryptodoge", "cryptodoge", 0.00,2500);
		new Fork("SRN","Shamrock", ".shamrock", "shamrock-blockchain", 0.0,.25);
		new Fork("PIPS","PipsCoin", ".pipscoin", "pipscoin-blockchain", 0.0,5);
		new Fork("XBR","Beer", ".beernetwork", "beer-blockchain", 0.0,.25);
		new Fork("FFK","Fishery", ".fishery", "fishery", 0.0,.075);
		new Fork("XOL","Olive", ".olive", "olive-blockchain", 0.0,.25);
		new Fork("NCH","NChainExt9", ".chia", "NChainExt9-blockchain", 0.0,-1);
		new Fork("SSD","SSDCoin", ".ssdcoin", "sddcoin-blockchain", 0.0,.25);
		new Fork("XKW","Kiwi", ".kiwi", "kiwi-blockchain", 0.0,.25);
		new Fork("XCHA","XCHA", ".xcha", "xcha-blockchain", 0.0,.25);
	}
	
	public static void Save() {
		DumperOptions options = new DumperOptions();
		options.setIndent(2);
		options.setPrettyFlow(true);
		options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
		options.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
		
		PrintWriter writer;
		try {
			writer = new PrintWriter(SETTINGS_PATH);
			Yaml yaml = new Yaml(options);
			yaml.dump(Fork.LIST, writer);
			
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	

}

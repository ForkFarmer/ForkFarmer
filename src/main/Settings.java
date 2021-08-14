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
		} catch (FileNotFoundException e) {
			loadDefaults();
		}
	}
	
	public static void loadDefaults() {
		new Fork("XCH","Chia", ".chia", "chia-blockchain", 269.0, .25);
		new Fork("XFX","Flax", ".flax", "flax-blockchain", 0.7, .25);
		new Fork("SPARE","Spare", ".spare", "spare-blockchain", .05,.5);
		new Fork("CGN","Chaingreen", ".chaingreen", "chaingreen-blockchain", .005,62.5);
		new Fork("XGJ","Goji", ".goji-blockchain", "goji-blockchain", 0.02,.25);
		new Fork("XSE","Seno", ".seno2", "seno-blockchain", 0.02,.25);
		new Fork("XFL","Flora", ".flora", "flora-blockchain", .05,3.5);
		new Fork("AVO","Avocado", ".avocado", "avocado-blockchain", .03,.25);
		new Fork("XKA","Kale", ".kale", "kale-blockchain", .03,.25);
		new Fork("XTX","Taco", ".taco", "taco-blockchain", .03,.25);
		new Fork("XDG","DogeChia", ".dogechia", "dogechia-blockchain", .04,.25);
		new Fork("SIT","Silicoin", ".silicoin", "silicoin-blockchain", .4,.25);
		new Fork("GDOG","Greendoge", ".greendoge", "greendoge-blockchain", .002,12.5);
		new Fork("HDD","Hddcoin", ".hddcoin", "hddcoin-blockchain", .04,.25);
		new Fork("XEQ","Equality", ".equality", "equality-blockchain", 0.0,3.5);
		new Fork("SOCK","Socks", ".socks", "socks-blockchain", 0.01,.25);
		new Fork("WHEAT","Wheat", ".wheat", "wheat-blockchain", .04,12.5);
		new Fork("XMX","Melati", ".melati", "melati-blockchain", .04,.25);
		new Fork("TAD","Tad", ".tad", "tad-blockchain", .04,2.0);
		new Fork("CANS","Cannabis", ".cannabis", "cannabis-blockchain", .01,16);
		new Fork("XSC","Sector", ".sector", "sector-blockchain", .002,.25);
		new Fork("CAC","Cactus", ".cactus", "cactus-blockchain", .04,.25);
		new Fork("CHIVES","Chives", ".chives", "chives-blockchain", 0.0,22.5);
		new Fork("VAG","Cunt", ".cunt", "cunt-blockchain", 0.0,.25);
		new Fork("XCL","Caldera", ".caldera", "caldera-blockchain", 0.0,.25);
		new Fork("APPLE","Apple", ".apple", "apple-blockchain", 0.07,.25);
		new Fork("XMZ","Maize", ".maize", "maize-blockchain", 0.003,22.5);
		new Fork("COV","Covid", ".covid", "covid-blockchain", 0.001,10.0);
		new Fork("SRN","Shamrock", ".shamrock", "shamrock-blockchain", 0.0,.25);
		new Fork("XFK","Fork", ".fork", "fork-blockchain", 0.0,6.25);
		new Fork("XBTC","BTCgreen", ".btcgreen", "btcgreen-blockchain", 0.07,1);
		new Fork("XCR","Chiarose", ".chiarose", "chia-rosechain", 0.00,25.0);
		new Fork("XCD","Chiadoge", ".chiadoge", "chiadoge", 0.00,2500);
		new Fork("SCM","Scam", ".scam", "scam-blockchain", 0.00,10);
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

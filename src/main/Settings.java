package main;

import java.awt.Dimension;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import forks.Fork;
import forks.ForkData;
import forks.ForkStarter;
import forks.ForkView;
import transaction.TransactionView;
import util.FFUtil;
import util.Util;
import util.json.JsonArray;
import util.json.JsonObject;
import util.swing.SwingUtil;

public class Settings {
	private static final String SETTINGS_PATH = "FF_Settings.yaml";
	
	public class Currency {
		String name;
		char symbol;
		double ratio;
		
		Currency(String name, char symbol, double ratio) {
			this.symbol = symbol;
			this.ratio = ratio;
		}
		
	}
	
	public static Gui GUI = new Gui();
	public static Map<String, Object> settings = new HashMap<>();
	public static JsonObject colMap;
	
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
		public boolean autoUpdate = true;
		public boolean lockColumns = false;
		public int httpServerPort = 12321;
	}
	
	@SuppressWarnings("unchecked")
	public static void Load() {
		JsonArray forkArray = (JsonArray)FFUtil.loadIntJSON("forks.json");
		forkArray.forEach(o-> new ForkData((JsonObject)o));
		
		colMap = (JsonObject)FFUtil.loadIntJSON("columns.json");
		
		InputStream inputStream;
		
		try {
			
			inputStream = new FileInputStream(new File(SETTINGS_PATH));
			Yaml yaml = new Yaml();
			settings = yaml.load(inputStream);
			GUI = (Gui) settings.get("Gui Settings");
			Fork.LIST = (List<Fork>) settings.get("Fork List");
		} catch (Exception e) {
			if (FileNotFoundException.class == e.getClass())
				System.out.println("FF_Settings.yaml not found. Populating with defaults");
			else
				e.printStackTrace();
			GUI.logReaderIntraDelay = 100;
			GUI.logReaderExoDelay = 5000;
			GUI.daemonReaderWorkers = 2;
			GUI.daemonReaderDelay = 5000;
			GUI.txViewDimension = new Dimension(900,250);
			GUI.forkViewDimension = new Dimension(700,400);
		} 
		
		ForkData.loadFix();
		Fork.FULL_LIST = new ArrayList<>(Fork.LIST);
		Fork.LIST.removeIf(f -> f.hidden);
		ForkView.update();
		
	}
	
	public static void Save() {
		synchronized (Fork.LIST) {
			SwingUtil.mapViewToModel(ForkView.TABLE,Fork.LIST);
		}
		
		Fork.FULL_LIST.stream().filter(f -> f.hidden).forEach(f -> Fork.LIST.add(f));
		
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
		options.setAllowUnicode(true);
		
		BufferedWriter br = null;
		try {
			br = Files.newBufferedWriter(Paths.get(SETTINGS_PATH));
			
			Yaml yaml = new Yaml(options);
			yaml.dump(settings, br);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Util.closeQuietly(br);
	}

}

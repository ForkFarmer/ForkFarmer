package main;

import java.util.HashMap;
import java.util.Map;

public class Args {
	public static String[] args;
	public static final Map<String,String> ARG_MAP = new HashMap<>();
	
	public static void handle () {
		for (int i = 0; i< args.length -1; i+=2) {
			String key = args[i];
			String val = args[i+1];
			ARG_MAP.put(key,val);
		}
		
		
	}
	/*
	private static void argStart() {
		for (String s: ForkFarmer.args) {
			for (Fork f: Fork.LIST) {
				if (f.symbol.equals(s)) {
					System.out.println("Launching: " + f.exePath + " start farmer");
					Util.runProcessWait(f.exePath,"start","farmer");
					Util.sleep(30000);
				}
			}
		}
		System.out.println("done launching forks... exiting");
		System.exit(0);
	}
	*/
	

}

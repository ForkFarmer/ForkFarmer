package main;

import java.util.HashMap;
import java.util.Map;

import forks.Fork;
import forks.ForkController;

public class Args {
	public static String[] args;
	public static final Map<String,String> ARG_MAP = new HashMap<>();
	
	public static void handle () {
		for (int i = 0; i< args.length -1; i+=2) {
			String key = args[i];
			key = key.toLowerCase();
			String val = args[i+1];
			ARG_MAP.put(key,val);
		}
		
		new Thread(Args::argStart).start();
		
	}
	
	private static void argStart() {
	
		String sVal = ARG_MAP.get("-stagger");
		
		if(null != sVal) {
			//System.out.println("stagger start: " + Fork.LIST.size() + "delayInt: " + Integer.parseInt(sVal));
			ForkController.staggerStart(Fork.LIST, Integer.parseInt(sVal));
		}
		
		
	}
	
	

}

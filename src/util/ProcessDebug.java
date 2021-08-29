package util;

import java.util.ArrayList;
import java.util.List;

public class ProcessDebug {
	static List<ProcessDebug> LIST = new ArrayList<>();
	
	Process p;
	String arg;
	
	public ProcessDebug(Process p, String arg) {
		this.p = p;
		this.arg = arg;
		
		add(this);
	}
	
	public static synchronized void add(ProcessDebug pu) {
		LIST.add(pu);
	}
	
	public static synchronized void remove(Process p) {
		ProcessDebug pu = LIST.stream().filter(pz -> pz.p == p).findFirst().get();
		LIST.remove(pu);
		System.out.println("Current Active processes: " + LIST.size());
		for (ProcessDebug pz : LIST)
			System.out.println("- " + pz.arg);
	}
	
	

}

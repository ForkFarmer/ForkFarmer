package types;

import util.Util;

public class Percentage implements Comparable<Percentage> {
	public static Percentage EMPTY = new Percentage(-1);
	
	public double effort;
	
	public Percentage(int e) {
		effort = e;
	}
	
	public Percentage(String str) {
		effort = Util.round(Double.parseDouble(str.replace("%", "")),1);
	}
	
	public String toString() {
		return (effort >= 0) ? effort + "%" : "";
	}
	
	public int compareTo(Percentage e) {
		return (int)(effort - e.effort);
	}
}

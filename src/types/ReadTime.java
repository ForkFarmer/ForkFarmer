package types;

import util.I18n;

import java.text.DecimalFormat;

public class ReadTime implements Comparable<ReadTime> {
	public static final ReadTime EMPTY = new ReadTime("");
	public static final ReadTime TIMEOUT = new ReadTime(I18n.ReadTime.readTimeout);
	public final double time;
	private final String timeStr;
	
	public ReadTime(double time) {
		this.time = time;
		DecimalFormat df = new DecimalFormat("0.00");
		timeStr = df.format(time);
	}
	
	public ReadTime(String msg) {
		timeStr = msg;
		time = -1;
	}
	
	public String toString() {
		return timeStr;
	};
	
	public int compareTo(ReadTime rt) {
		return (int)(time - rt.time);
	}
	
	
}

package types;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;

import util.FFUtil;

public class TimeU implements Comparable<TimeU> {
	private static final long BIG = 999999;
	public static final TimeU NEVER = new TimeU("Never",BIG);
	public static final TimeU BLANK = new TimeU("",BIG);
	String str = "";
	long minutes;
	
	public TimeU(String str) { // used for etw
		minutes = convertToMinutes(str);
		
		str = str.replace(" ", "");
		str = str.replace("and", "");
		str = str.replace("years", "y ");
		str = str.replace("year", "y ");
		str = str.replace("months", "mo ");
		str = str.replace("month", "mo ");
		str = str.replace("weeks", "w ");
		str = str.replace("week", "w ");
		str = str.replace("days", "d ");
		str = str.replace("day", "d ");
		str = str.replace("hours", "h ");
		str = str.replace("hour", "h ");
		str = str.replace("minutes", "m ");
		str = str.replace("minute", "m ");
		str = str.replace("and", "");
		
		
		this.str = str;
	}
	
	public static Optional<TimeU> parse(String str) {
		return (str.equals("Unknown")) ? Optional.empty() : Optional.of(new TimeU(str));  
	}
	
	public long inMinutes() {
		return minutes;
	}
	
	public TimeU() {
		str = LocalDateTime.now().format(FFUtil.DTF);
	}
	
	public TimeU(String s, long m) {
		str = s;
		minutes = m;
	}
	
	public TimeU(long seconds) {
		minutes = seconds/60;
		StringBuilder sb = new StringBuilder();
		
		int day = (int) (seconds/60/60/24);
		if (day > 0) {
			sb.append(day + "d ");
			seconds -= (day * (60*60*24));
		}
		int hours = (int) (seconds/60/60);
		if (hours > 0) {
			sb.append(String.format("%02d", hours) + "h ");
			seconds -= (hours * (60*60));
		}
		int min = (int) (seconds/60);
		sb.append(String.format("%02d", min) + "m ");
		
		str = sb.toString();
	}

	public String toString() {
		return str;
	}
	
	public int compareTo(TimeU tm) {
		return (int)(minutes - tm.minutes);
	}
	
	private static long convertToMinutes(String etw) {
		long etw_minutes = 0;
		
		long hour_minutes = 60;
		long day_minutes = 24 * hour_minutes;
		long week_minutes = 7 * day_minutes;
		long months_minutes = 43800;
		long year_minutes = 12 * months_minutes;
		
		String[] etwA = etw.split(" ");
		
		for (int i = 0; i < etwA.length; i+= 2) {
			if (etwA[i].equals("and"))
				i++;
			if (etwA[i].equals("Never"))
				return 0;
			if (etwA[i].equals("Now"))
				return 0;

			String key = etwA[i+1];
			long val = Long.parseLong(etwA[i]);
			
			if (key.startsWith("year"))
				etw_minutes += val * year_minutes;
			if (key.startsWith("month"))
				etw_minutes += val * months_minutes;
			if (key.startsWith("week"))
				etw_minutes += val * week_minutes;
			if (key.startsWith("day"))
				etw_minutes += val * day_minutes;
			if (key.startsWith("hour"))
				etw_minutes += val * hour_minutes;
			if (key.startsWith("minute"))
				etw_minutes += val;
		}
		return etw_minutes;
	}
	
	public static TimeU getTimeSince(LocalDateTime before) {
		if (null == before)
			return TimeU.NEVER;
		LocalDateTime now = LocalDateTime.now();
		return new TimeU(Duration.between(before,now).getSeconds());
	}

	public boolean known() {
		return BIG != minutes;
	}

}

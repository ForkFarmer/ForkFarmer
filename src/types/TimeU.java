package types;

public class TimeU implements Comparable<TimeU> {
	String str = "";
	long minutes;
	
	
	public TimeU(String str) {
		minutes = convertToMinutes(str);
		this.str = str;
	}
	
	public long inMinutes() {
		return minutes;
	}
	
	public TimeU() {
		// TODO Auto-generated constructor stub
	}
	
	public TimeU(long seconds) {
		minutes = seconds/60;
		
		if (seconds < 60)
			str = seconds + " Seconds";
		if (seconds < 60*60)
			str = (seconds /60) + " Minutes";
		else
			str = (seconds/60/60) + " Hours";
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
	
	

}

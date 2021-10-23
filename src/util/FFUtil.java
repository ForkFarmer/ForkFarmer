package util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FFUtil {
	transient public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public static LocalDateTime parseTime(String s) {
		if (s.length() < 19)
			return null;
		
		try {
			String logTimeString = s.substring(0,19);
			logTimeString = logTimeString.replace("T", " ");
			return LocalDateTime.parse(logTimeString, DTF);
		} catch (Exception e) {
			return null;
		}
	}
}

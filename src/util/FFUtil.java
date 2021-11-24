package util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import util.json.Jsoner;

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
	
	public static Object loadIntJSON(String s) {
		Object o = null;
		try {
			InputStream is = Util.getResourceAsStream(s);
			o = Jsoner.deserialize(new InputStreamReader(is, "UTF-8"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		return o;
	}
}

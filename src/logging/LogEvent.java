package logging;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class LogEvent {

	private final String time;
	private final String str;
	
	public LogEvent(String str) {
		this.time = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS).toString();
		this.str = str;
	}
	
	public String toString() {
		return time + str;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getDetails() {
		return str;
	}
	
}
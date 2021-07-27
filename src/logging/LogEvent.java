package logging;

public class LogEvent {

	private final String time;
	private final String description;
	
	public LogEvent(String time, String description) {
		this.time = time;
		this.description = description;
	}
	
	
	public String toString() {
		return time + description;
	}
	
	public String getTime() {
		return time;
	}
	
	public String getDetails() {
		return description;
	}
	
}
package types;

public class Effort implements Comparable<Effort> {
	public static Effort EMPTY = new Effort(-1);
	
	int effort;
	
	public Effort(int e) {
		effort = e;
	}
	
	public String toString() {
		return (effort > 0) ? effort + "%" : "";
	}
	
	public int compareTo(Effort e) {
		return (int)(effort - e.effort);
	}
}

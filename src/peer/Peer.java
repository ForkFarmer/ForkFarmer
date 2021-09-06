package peer;

public class Peer {
	final String address;
	final String time;
	final double ul;
	final double dl;
	final int height;
	
	
	public Peer (String address, String time, double ul, double dl, int height) {
		this.address = address;
		this.time = time;
		this.ul = ul;
		this.dl = dl;
		this.height = height;
	}

	
	/*
	0 FULL_NODE
	1 80.81.15.25
	2 8444/8444
	3 87a118fa...
	4 Aug
	5 01
	6 16:54:11
	7 6.0|11.8
	8 -SB
	9 Height:
	10 655519
	11 -Hash:
	12 ad8be9fa...
	 */
	public static Peer factoryMultiLine(String nodeStr) {
		String[] lines = nodeStr.split("\\s+");
		
		String address = lines[1] + ":" + lines[2].substring(lines[2].indexOf("/")+1);
		String time = lines[4] + " " + lines[5] + " " + lines[6];
		int sep = lines[7].indexOf("|");
		double ul = Double.parseDouble(lines[7].substring(0, sep));
		double dl = Double.parseDouble(lines[7].substring(sep+1));
		int height = Integer.parseInt(lines[10]);
		
		return new Peer(address,time, ul, dl, height);
	}
	
	/*
	0 FULL_NODE
	1 90.174.132.53
	2 8444/8444
	3 98a158db...
	4 Sep
	5 05
	6 15:42:45
	7 0.0|0.0
	8 576768
	10 1065d8e5...
	 */
	public static Peer factorySingleLine(String nodeStr) {
		String[] lines = nodeStr.split("\\s+");
		
		String address = lines[1] + ":" + lines[2].substring(lines[2].indexOf("/")+1);
		String time = lines[4] + " " + lines[5] + " " + lines[6];
		int sep = lines[7].indexOf("|");
		double ul = Double.parseDouble(lines[7].substring(0, sep));
		double dl = Double.parseDouble(lines[7].substring(sep+1));
		int height = Integer.parseInt(lines[8]);
		return new Peer(address,time, ul, dl, height);
	}
	

}

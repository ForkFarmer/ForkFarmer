package peer;

public class Peer {
	
	/*
	 * 
0	 * FULL_NODE
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
	final String address;
	final String time;
	final double ul;
	final double dl;
	final String height;
	
	
	public Peer (String nodeStr) {
		String[] lines = nodeStr.split("\\s+");
		
		address = lines[1] + ":" + lines[2].substring(lines[2].indexOf("/")+1);
		time = lines[4] + " " + lines[5] + " " + lines[6];
		int sep = lines[7].indexOf("|");
		ul = Double.parseDouble(lines[7].substring(0, sep));
		dl = Double.parseDouble(lines[7].substring(sep+1));
		height = lines[10];
	}

}

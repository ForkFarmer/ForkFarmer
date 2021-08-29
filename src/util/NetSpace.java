package util;

public class NetSpace implements Comparable<NetSpace> {
	
	public double szTB;
	public String s;
	
	public NetSpace(String nsString) {
		s = nsString;
		szTB = Double.parseDouble(nsString.substring(0,nsString.indexOf(' ')));
		if (s.endsWith("PiB"))
			szTB = szTB * 1024;
		if (s.endsWith("EiB"))
			szTB = szTB * 1024 * 1024;
	}

	@Override
	public int compareTo(NetSpace ns) {
		return (int)(szTB - ns.szTB);
	}
	
	public String toString() {
		return (null != s ) ? s : "";
	}
}

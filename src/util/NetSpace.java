package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

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
	
	public static Optional<NetSpace> parse(String nString) {
		if (nString.equals("Unknown"))
			return Optional.empty();
		
		return Optional.of(new NetSpace(nString));
	}

	public NetSpace(BigDecimal bytes) {
		szTB = bytes.divide(new BigDecimal("1099511627776")).doubleValue(); 
		s =  tbToString(szTB);
	}

	@Override
	public int compareTo(NetSpace ns) {
		return (int)(szTB - ns.szTB);
	}
	
	public String toString() {
		return (null != s ) ? s : "";
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	private static String tbToString(final double tb) {
		if (tb < 1000)
			return round(tb,2) + " TiB";
		if (tb < 1024 * 1024)
			return round((tb/1024),2) + " PiB";
		if (tb < 1024 * 1024 * 1024)
			return round((tb/(1024*1024)),2) + " EiB";
		return round(tb/(1024*1024*1024),2) + " ZiB";
	}
	
	public boolean known() {
		return szTB > 0;
	}
	
}

package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/** Common network functions */
public final class NetUtil {
	
	private static final String[] SERV_LIST = {
		"http://checkip.amazonaws.com",
		"http://www.icanhazip.com",
		"http://www.trackip.net/ip",
		"http://www.myexternalip.com/raw",
		"http://www.ipecho.net/plain"
	};
	
	public static String getExternalIP() {
		for (final String url : SERV_LIST) {
			try {
				final URL ipURL = new URL(url);
				final BufferedReader br = new BufferedReader(new InputStreamReader(ipURL.openStream()));
				return br.readLine();
			} catch (final IOException e) {
				continue;
			}
		}
		return "No Internet";

	}
	
	public static String getLocalIP() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			return "<Error>";
		}
	}
	
	public static Set<InetAddress> getLocalIPs() {
		Set<InetAddress> iSet = new HashSet<>();
		
		Enumeration<NetworkInterface> niEnum;
		try {
			niEnum = NetworkInterface.getNetworkInterfaces();
			while(null != niEnum && niEnum.hasMoreElements())
			{
				final NetworkInterface netInterface = niEnum.nextElement();
				final Enumeration<InetAddress> inetEnum = netInterface.getInetAddresses();
				while (inetEnum.hasMoreElements())
					iSet.add(inetEnum.nextElement());
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
				return iSet;
	}

	public static String getIPs() {
		return getLocalIP() + " | " + getExternalIP();
	}
	
	public static int randPort() {
		Random r = new Random();
		return r.nextInt(65534)+1;
	}
	
}

package util;

import java.awt.Color;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import util.json.JsonException;
import util.json.JsonObject;
import util.json.Jsoner;

public class Util {
	
	// Not sure if this belongs in this class
	public static String dateTimeHeader() {
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss z");
		String header = "============ " + sdf.format(Calendar.getInstance().getTime()) + " ============\n";
		return header;
	}
	
	public static boolean baEquals(byte[] a, byte[] b, int z) {
		for (int i = 0; i < z; i++)
			if (a[i] != b[i])
				return false;

		return true;
	}

	public static int boundValue(int val, int min, int max) {
		if (val <= min)
			return min;
		if (val >= max)
			return max;
		return val;
	}
	
	public static void openFile(String path) {
		try {
			Desktop.getDesktop().open(new File(path));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static List<String> getDir(String base, String target) {
		try { 
			return Files.list(new File(base).toPath())
					 .map(p -> p.getFileName().toString())
					 .filter(s->s.startsWith(target))
					 .filter(s -> new File(base + s).isDirectory())
					 .collect(Collectors.toList());
		} catch (IOException e) {
			return new ArrayList<>();
		}
	}
	
	public static double round(double value, int places) {
	    if (places < 0) throw new IllegalArgumentException();

	    BigDecimal bd = BigDecimal.valueOf(value);
	    bd = bd.setScale(places, RoundingMode.HALF_UP);
	    return bd.doubleValue();
	}
	
	public static String byteToHex(final byte b)
	{
		final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();
		final StringBuffer sb = new StringBuffer();
		final int v = b & 0xFF;
		sb.append(HEX_ARRAY[v >>> 4]).append(HEX_ARRAY[v & 0x0F]);
		return sb.toString();
	}
	
	public static void deleteDir (String path) throws IOException {
		Files.walk(new File(path).toPath())
	    .sorted(Comparator.reverseOrder())
	    .map(Path::toFile)
	    .forEach(File::delete);
	}
	
	public static String getHexString(final byte[] b) {
		return getHexString(b, ByteOrder.BIG_ENDIAN, b.length, "");
	}
	
	public static String getHexString(final byte[] b, final ByteOrder bo, final int len,final String delimiter) {
		if (len == 0)
			return "";
		final StringBuilder sbResult = new StringBuilder();

		if (ByteOrder.BIG_ENDIAN == bo) {
			for (int i = 0; i < len-1; i++) {
				sbResult.append(byteToHex(b[i])).append(delimiter);
			}
			sbResult.append(byteToHex(b[len-1]));
		} else {
			for (int i = len-1; i > 0; i--) {
				sbResult.append(byteToHex(b[i])).append(delimiter);
			}
			sbResult.append(byteToHex(b[0]));
		}
		return sbResult.toString();
	}
	
	public static long getLineCount(String str) {
		return str.chars().filter(x -> x == '\n').count() + 1;
	}


	public static URL getResource(final String path) {
		return Util.class.getClassLoader().getResource(path);
	}
	
	public static InputStream getResourceAsStream(final String path) {
		return Util.class.getClassLoader().getResourceAsStream(path);
	}


	public static byte[] hexStringToByteArray(String s) {
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
	
	public static void openDirectory(String dirPath) {
		try {
			Desktop.getDesktop().open(new File(dirPath));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String randIP() {
		Random r = new Random();
		return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
	}
	
	public static void sleep(final long ms)
	{
		try {
			Thread.sleep(ms);
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	
	}

	public static int randPort() {
		Random r = new Random();
		return r.nextInt(10000);
	}
	
	public static String secToTime(int s) {
		int sec = s % 60;
		int min = (s/60)%60;
		int hours = (s/60)/60;
		
		if (1 == hours)
			return "1 hour and " + min + " minutes";
		if (hours > 1)
			return hours + " hours and " + min + " minutes";
		if (min > 1)
			return min + " minutes";
		if (1 == min)
			return "1 minute and " + sec + " seconds";
		return sec + " seconds";
	}
	
	public static String loadToString(File f) throws IOException {
		return new String(Files.readAllBytes(f.toPath()));
	}
	
	//probably use false here
	public static String byteToString(final long bytes, final boolean si) {
		final int unit = si ? 1000 : 1024;
		if (bytes < unit) return bytes + " B";
		final int exp = (int) (Math.log(bytes) / Math.log(unit));
		final String pre = (!si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (!si ? "" : "i"); // si is inverted because MSFT is incorrect
		return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
	public static void copyToClip(String s) {
		StringSelection ss = new StringSelection(s);
		Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();
		clip.setContents(ss, null);
	}
	
	
	
	public static Process startProcess(String... args) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(args);
		pb.redirectError();
		Process p = pb.start();
		//new ProcessDebug(p, Arrays.stream(args).collect(Collectors.joining()));
		return p;
	}
	
	
	
	public static void waitForProcess(Process p ) {
		try {
			if (null != p) {
				if (!p.waitFor(60, TimeUnit.SECONDS))
					p.destroyForcibly();
				//ProcessDebug.remove(p);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public static void closeQuietly(Closeable s) {
		if (null == s)
			return;
		
		try {
			s.close();
		} catch (IOException e) {
			// close quietly
		}
	}
	
	public static void runIfAble (Runnable r) {
		if (null != r)
			r.run();
	}
	
	public static <T> void consumeIfAble(Consumer<T> c, T o) {
		if (null != c)
			c.accept(o);
	}
	
	public static <T> String listToString(List<T> l) {
		StringBuilder sb = new StringBuilder();
		for (T e : l)
			sb.append(e.toString() + "\n");
		return sb.toString();
	}
	
	public static boolean isHostWin() {
		return System.getProperty("os.name").contains("Win");
	}
	
	public static String getWordAfter(String str, String target) {
		str = str.substring(str.indexOf(target) + target.length());
		int endIndex = str.indexOf(" ");
		return (-1 != endIndex) ? str.substring(0,endIndex) : str;
	}
	
	public static String wordAfterIfExist(String str, String target) {
		int targetIndex = str.indexOf(target);
		if (-1 == targetIndex)
			return null; 
		str = str.substring(str.indexOf(target) + target.length());
		int endIndex = str.indexOf(" ");
		return (-1 != endIndex) ? str.substring(0,endIndex) : str;
	}
	
	public static String runProcessWait(String... args) {
		ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		try {
			Process p = startProcess(args);
			InputStreamConsumer isc = new InputStreamConsumer(p.getInputStream(), baos);
			isc.start();	
			p.waitFor();
			isc.join();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toString();
		
	}
	
	public static String runProcessDebug(String... args) {
		ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		try {
			ProcessBuilder pb = new ProcessBuilder()
							.command(args)
							.redirectErrorStream(true);
			Process p = pb.start();
			InputStreamConsumer isc = new InputStreamConsumer(p.getInputStream(), baos);
			isc.start();	
			if (!p.waitFor(10, TimeUnit.SECONDS))
				p.destroyForcibly();
			isc.join();
		} catch (InterruptedException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return baos.toString();
	}

	public static void blockUntilAvail(ExecutorService svc) {
		while (((ThreadPoolExecutor)svc).getQueue().size() > 0) // block queue
			sleep(500);
	}
	
	public static int getJavaVersion() {
	    String version = System.getProperty("java.version");
	    if(version.startsWith("1.")) {
	        version = version.substring(2, 3);
	    } else {
	        int dot = version.indexOf(".");
	        if(dot != -1) { version = version.substring(0, dot); }
	    } return Integer.parseInt(version);
	}

	public static String toString(Object o) {
		return (null != o) ? o.toString() : "";
	}

	public static void openLink(String url) {
		if (null == url)
			return;
		try {
	    	if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
	    		Desktop.getDesktop().browse(new URI(url));
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	}
	
	public static String colorToHex (Color color) {
	   String hex = String.format("#%02x%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha() );
	   hex=hex.toUpperCase();
       return hex;
	}
	
	public static Color hexToColor(String hex) 
	{
		if (null == hex)
			return null;
	    hex = hex.replace("#", "");
	    switch (hex.length()) {
	        case 6:
	            return new Color(
	            Integer.valueOf(hex.substring(0, 2), 16),
	            Integer.valueOf(hex.substring(2, 4), 16),
	            Integer.valueOf(hex.substring(4, 6), 16));
	        case 8:
	            return new Color(
	            Integer.valueOf(hex.substring(0, 2), 16),
	            Integer.valueOf(hex.substring(2, 4), 16),
	            Integer.valueOf(hex.substring(4, 6), 16),
	            Integer.valueOf(hex.substring(6, 8), 16));
	    }
	    return null;
	}

	public static JsonObject completeJsonRPC(Process p) throws JsonException {
		BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		JsonObject jo = (JsonObject)Jsoner.deserialize(br);
		Util.waitForProcess(p);
		Util.closeQuietly(br);
		boolean success = (boolean) jo.get("success");
		return (success) ? jo : null;
	}
	
}
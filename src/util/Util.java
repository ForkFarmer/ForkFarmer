package util;

import java.awt.Desktop;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteOrder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.function.Consumer;

import javax.swing.ImageIcon;

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
	
	public static String getDir(String base, String target) throws IOException {
		try {
		 return Files.list(new File(base).toPath()).map(p -> p.getFileName().toString()).filter(s->s.startsWith(target)).findAny().get();
		} catch (NoSuchElementException e) {
			System.out.println("Base: " + " Target: " + target);
			throw new IOException("Couldn't get directory: ");
		}
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


	public static URL getResource(final String path)
	{
		return Util.class.getResource(path);
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
	
	public static ImageIcon loadIcon(final String path)
	{
		try {
			return new ImageIcon(getResource(path));
		} catch (Exception e) {
			throw new RuntimeException("Check your resources for missing icon: " + path);
		}
	}
	
	public static ImageIcon loadIcon(final String path, final String txt)
	{
		return new ImageIcon(getResource(path),txt);
	}
	
	public static ImageIcon loadIcon(final String path, final String txt, final int sz) {
		return new ImageIcon(Util.loadIcon(path).getImage().getScaledInstance(sz,sz, Image.SCALE_SMOOTH),txt);
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
		return str.substring(0,str.indexOf(" "));
	}
	
}
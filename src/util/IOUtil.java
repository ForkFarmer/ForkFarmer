package util;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class IOUtil {
	
	public static void writeString(DataOutputStream dos, String str) throws IOException {
		dos.writeInt(str.getBytes().length);
		dos.write(str.getBytes());
	}
	
	public static String readString(DataInputStream dis, int maxLen) throws IOException {
		int length = dis.readInt();
		if (length > maxLen)
			throw new IOException("String too long: " + length);
		byte[] strB = new byte[length];
		dis.read(strB);
		return new String(strB);
	}
	
	
	public static void write(DataOutputStream dos, Object... objs) throws IOException {
		synchronized(dos) {
			for (Object o : objs) {
				if (String.class == o.getClass())
					writeString(dos,(String)o);
				else if (Integer.class == o.getClass() || int.class == o.getClass()) {
					dos.writeInt((Integer)o);
				} else if (byte[].class == o.getClass()) {
					dos.writeInt(((byte[])o).length);
					dos.write((byte[])o);
				} else if (Double.class == o.getClass() || double.class == o.getClass()) {
					dos.writeDouble((double)o);
				} else if (String[].class == o.getClass()) { 
					String[] strArray = (String[])o;
					dos.writeInt(strArray.length);
					for (String s : strArray)
						writeString(dos,s);
				} else {
					throw new IOException("Unknown type: " + o.getClass());
				}
			}
			dos.flush();
		}
	}

	public static byte[] readBA(DataInputStream dis, int len) throws IOException {
		int inLen = dis.readInt();
		if (inLen != len)
			throw new IOException("Unexpected Length! expected: " + len + " recevied " + inLen);
		byte[] ba = new byte[len];
		dis.read(ba);
		return ba;
	}

}

package forktrader.types;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import forks.ForkData;
import forktrader.TradeMsg;
import forktrader.server.Trader;
import types.Balance;
import util.IOUtil;
import util.Util;

public class Order implements Comparable<Order> {
	public enum TYPE {BUY,SELL};
	
	public Trader owner;
	public Trader executor;
	
	public byte[] hash;
	public TYPE oType;
	public String symbol;
	public double amount;
	public double xchTotal;
	
	public transient double price;
	public transient boolean myOrder;
	
	public Order(TYPE t, String symbol , double amount, double xchTotal) {
		this.oType = t;
		this.symbol = symbol;
		this.amount = amount;
		this.xchTotal = xchTotal;
		price = ForkData.getBySymbol("xch").get().price * xchTotal /amount;
		computeHash();
	}
	
	public Order(DataInputStream dis) throws IOException {
		hash = IOUtil.readBA(dis,32);
		oType = TYPE.values()[dis.readInt()];
		symbol = IOUtil.readString(dis, 10);
		amount = dis.readDouble();
		xchTotal = dis.readDouble();
		price = ForkData.getBySymbol("xch").get().price * xchTotal /amount;
		
		if (!verifyHash())
			throw new IOException("Invalid Order Hash");
	}
	
	public void send(DataOutputStream dos) throws IOException {
		IOUtil.write(dos, TradeMsg.ORDER.ordinal(), hash, oType.ordinal(), symbol, amount, xchTotal);
	}
	
	public Balance getAmount() {
		return new Balance(amount);
	}
	
	public Balance getPrice() {
		return new Balance(price);
	}
	
	public Balance xchTotal() {
		return new Balance(xchTotal);
	}
	
	public String hashToString() {
		return Util.getHexString(hash, ByteOrder.BIG_ENDIAN, 10, "");
	}
	
	public void computeHash() {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			md.update(getHashData());
			hash = md.digest();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public boolean verifyHash() {
		try {
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(getHashData());
		byte[] hashData = md.digest();
		return Arrays.equals(hashData, hash);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
		
	}
	
	private byte[] getHashData() throws IOException {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(baos);
		dos.writeInt(oType.ordinal());
 		dos.writeBytes(symbol);
		dos.writeDouble(amount);
		dos.writeDouble(xchTotal);
		dos.close();
		byte[] data = baos.toByteArray();
		baos.close();
		return data;
	}
	
	public int compareTo(Order o) {
		return (int)(o.price * 1000 - price * 10000);
	}
	
	public boolean equals(Object o) {
		if (o == this)
			return true;
		
		if (!(o instanceof Order))
			return false;
		
		Order ord = (Order)o;
		
		return Arrays.equals(ord.hash, hash);
	}
	
	public static Optional<Order> getOrderByHash(List<Order> oList, byte[] hash) {
		synchronized (oList) {
			return oList.stream().filter(o -> Arrays.equals(o.hash,hash)).findAny();
		}
	}

	
	

}

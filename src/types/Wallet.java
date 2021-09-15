package types;

public class Wallet {
	public static final Wallet EMPTY = new Wallet("","",-1);
	public static final Wallet NOT_SUPPORTED = new Wallet("","Not Supported",-1);
	public static final Wallet SELECT = new Wallet ("", "< Select Wallet >",-1); 
	
	public String fingerprint;
	public String addr;
	public int index;
	
	public Wallet(String fingerprint, String key, int index) {
		this.fingerprint = fingerprint;
		this.addr = key;
		this.index = index;
	}
	
	public String toString() {
		return addr;
	}
	 
}

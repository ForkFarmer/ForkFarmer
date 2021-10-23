package types;

public class Wallet {
	public static final Wallet EMPTY = new Wallet("","",-1);
	public static final Wallet NOT_SUPPORTED = new Wallet("","Not Supported",-1);
	public static final Wallet SELECT = new Wallet ("", "< Select Wallet >",-1); 
	
	transient public String fingerprint;
	transient public int index;

	public String addr;
	public boolean cold;
	
	public Wallet(String fingerprint, String key, int index) {
		this.fingerprint = fingerprint;
		this.addr = key;
		this.index = index;
	}
	
	public Wallet(String key) {
		cold = true;
		addr = key;
	}
	
	public String toString() {
		return addr;
	}
	 
}

package types;

import java.text.DecimalFormat;
import java.text.ParseException;

public class Balance implements Comparable<Balance> {
	public static final Balance NOT_SUPPORTED = new Balance("*Not Supported*",0);
	public double amt;
	String str = "";
	
	public Balance() {}
	
	public Balance(String s, double amt) {
		this.str = s;
		this.amt = amt;
	}
	
	public Balance(double amt) {
		this.amt = amt;
		updateStr();
	}
	
	public Balance(double amt, int decimal) {
		DecimalFormat df;
		this.amt = amt;
		if (2 == decimal)
			df = new DecimalFormat("#,##0.##");
		else
			df = new DecimalFormat("#,##0.####");
		str = df.format(amt);
	}
	
	public Balance(int amt) {
		this.amt = amt;
		updateStr();
	}
	
	public Balance(String balanceStr) throws ParseException {
		amt = Double.parseDouble(balanceStr);
		updateStr();
	}

	public String toString() {
		return str;
	}

	public int compareTo(Balance b) {
		return (int)(amt - b.amt);
	}

	public void add(double amount) {
		amt += amount;
		updateStr();
	}

	public void add(Balance b) {
		amt += b.amt;
		updateStr();
	}

	private void updateStr() {
		try {
			DecimalFormat df = null;
			if (amt < 100) 
				df = new DecimalFormat("#,###0.#####");
			else if (amt <= 1000) 
				df = new DecimalFormat("#,###0.##");
			else
				df = new DecimalFormat("#,###");
			str = df.format(amt);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

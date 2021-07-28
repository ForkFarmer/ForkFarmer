package transaction;

import java.util.HashMap;
import java.util.Map;
import javax.swing.Icon;
import forks.Fork;
import util.Ico;
import util.process.ProcessPiper;

public class Transaction implements Comparable<Transaction> {
	public static final Map<String,Transaction> TMAP = new HashMap<>();
	
	String hash;
	String type;
	String amount;
	String target;
	String date;
	String symbol;
	
	public Transaction(String symbol, String hash, String type, String amount, String target, String date) {
		this.symbol = symbol;
		this.hash = hash;
		this.type = type;
		this.amount = amount;
		this.target = target;
		this.date = date;
	}
	
	public String getSymbol() { 
		return symbol;
	}
	
	public String getAmount() {
		return amount;
	}
	
	public String getTarget() {
		return target;
	}
	
	public String getDate() {
		return date;
	}
	
	public Icon getIconR() {
		for (Fork f : Fork.LIST) {
			if (null == f.addr)
				continue;
			if (f.addr.equals(target))
				return Ico.loadIcon("icons/arrows/right.png");
		}
		return Ico.loadIcon("icons/arrows/left.png");
		
	}

	public static String load(String symbol, String exePath) {
		String rcvAddress = null;
		String trans = ProcessPiper.run(exePath,"wallet","get_transactions");
		
		//System.out.println("Trans: " + trans);

		String[] lines = trans.split(System.getProperty("line.separator"));
		
		if (lines.length < 5)
			return null;

		try {
			for (int i = 0; i < lines.length; i+=6) {
				if (lines[i].startsWith("Press q"))
					i++;
				
				String tHash   = lines[i].replace("Transaction ", "");
				String status  = lines[i+1].replace("Status: ", "");
				String amount  = lines[i+2].replace("Amount: ", "");
				String address = lines[i+3].replace("To address: ", "");
				String date    = lines[i+4].replace("Created at: ", "");
				
				Transaction t = new Transaction(symbol,tHash,status,amount,address,date);
				TMAP.put(t.hash, t);
				
				TransactionView.updateView();
				if (null != rcvAddress)
					continue;
				String firstWord = amount.substring(0, amount.indexOf(' '));
				
				if (symbol.equals("XFL") && firstWord.contentEquals("0.5"))
					rcvAddress =  address;
				else if (symbol.equals("SPARE") && firstWord.contentEquals("0.5"))
					rcvAddress =  address;
				else if (symbol.equals("CGN") && firstWord.contentEquals("62.5"))
					rcvAddress =  address;
				else if (symbol.equals("CAN") && firstWord.contentEquals("16"))
					rcvAddress =  address;
				else if (symbol.equals("GDOG") && firstWord.contentEquals("12.5"))
					rcvAddress =  address;
				else if (symbol.equals("GDOG") && firstWord.contentEquals("12.5"))
					rcvAddress =  address;
				else if (symbol.equals("XCD") && firstWord.contentEquals("2500"))
					rcvAddress =  address;
				else if (symbol.equals("XCR") && firstWord.contentEquals("25"))
					rcvAddress =  address;
				else if (symbol.equals("CANS") && firstWord.contentEquals("16"))
					rcvAddress =  address;
				else if (symbol.equals("COV") && firstWord.contentEquals("10"))
					rcvAddress =  address;
				else if (firstWord.contentEquals("0.25"))
					rcvAddress =  address;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return rcvAddress;
	}
	

	@Override
	public int compareTo(Transaction t) {
		return this.date.compareTo(t.date);
	}
}

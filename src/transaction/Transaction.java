package transaction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import forks.Fork;
import util.Ico;
import util.process.ProcessPiper;

public class Transaction {
	ImageIcon L_ARROW = Ico.loadIcon("icons/arrows/left.png");
	ImageIcon R_ARROW = Ico.loadIcon("icons/arrows/right.png");
	
	private static final Set<String> TSET = new HashSet<>();
	public static final List<Transaction> LIST = new ArrayList<>();
	public static boolean newTX = false;
	
	public final Fork   f;
	public final String hash;
	public final String amount;
	public final String target;
	public final String date;
	
	public Transaction(Fork f, String hash, String amount, String target, String date) {
		this.f = f;
		this.hash = hash;
		this.amount = amount;
		this.target = target;
		this.date = date;
	}
	
	public ImageIcon getIcon() {
		if (null != f.addr)
			if (f.addr.equals(target))
				return R_ARROW;
		return L_ARROW;
	}
	
	public static void load(Fork f) {
		String trans = ProcessPiper.run(f.exePath,"wallet","get_transactions");

		String[] lines = trans.split(System.getProperty("line.separator"));
		
		if (lines.length < 5)
			return; // no transactions?

		int i = 0;
		
		// skip the choose a wallet prompt.
		for (;i < lines.length; i++)
			if (lines[i].contains("Transaction"))
				break;
		
		try {
			for (; i < lines.length; i+=6) {
				if (lines[i].startsWith("Press q"))
					i++;
				
				/*	Transaction d4a61a26367ef4a548ca5ccad94c5eb5f65b277f258271193199861dea311812
				    Status: Confirmed
				    Amount: 400 xtx
				    To address: xtx1d6ge0nrk8u9j6aammmtspq628pxjge98u2aqxm32key48k49j0csjdm5ch
				    Created at: 2021-07-25 15:42:07
				 */
			
				String tHash   = lines[i].replace("Transaction ", "");
				if (TSET.contains(tHash)) // stop parsing if we already have this transaction
					continue;
				
//				String status  = lines[i+1].replace("Status: ", "");
				String amount = lines[i+2].substring(8);
				String address = lines[i+3].substring(12);
				String date = lines[i+4].substring(12);
				
				Transaction t = new Transaction(f, tHash,amount,address,date); 
				LIST.add(t);
				TSET.add(tHash);
				newTX = true;
				
				if (null != f.addr)
					continue;
				
				String firstWord = amount.substring(0, amount.indexOf(' '));
				String symbol = f.symbol;
				
				if (symbol.equals("XFL") && firstWord.contentEquals("0.5"))
					f.addr =  address;
				if (symbol.equals("XFL") && firstWord.contentEquals("0,5"))
					f.addr =  address;
				else if (symbol.equals("SPARE") && firstWord.contentEquals("0.5"))
					f.addr =  address;
				else if (symbol.equals("SPARE") && firstWord.contentEquals("0,5"))
					f.addr =  address;
				else if (symbol.equals("CGN") && firstWord.contentEquals("62.5"))
					f.addr =  address;
				else if (symbol.equals("CGN") && firstWord.contentEquals("62,5"))
					f.addr =  address;
				else if (symbol.equals("CAN") && firstWord.contentEquals("16"))
					f.addr =  address;
				else if (symbol.equals("GDOG") && firstWord.contentEquals("12.5"))
					f.addr =  address;
				else if (symbol.equals("GDOG") && firstWord.contentEquals("12,5"))
					f.addr =  address;
				else if (symbol.equals("XCD") && firstWord.contentEquals("2500"))
					f.addr =  address;
				else if (symbol.equals("XEQ") && firstWord.contentEquals("3.5"))
					f.addr =  address;
				else if (symbol.equals("XEQ") && firstWord.contentEquals("3,5"))
					f.addr =  address;
				else if (symbol.equals("CHIVES") && firstWord.contentEquals("22.5"))
					f.addr =  address;
				else if (symbol.equals("CHIVES") && firstWord.contentEquals("22,5"))
					f.addr =  address;
				else if (symbol.equals("TAD") && firstWord.contentEquals("2"))
					f.addr =  address;
				else if (symbol.equals("TAD") && firstWord.contentEquals("2"))
					f.addr =  address;
				else if (symbol.equals("XCR") && firstWord.contentEquals("25"))
					f.addr =  address;
				else if (symbol.equals("CANS") && firstWord.contentEquals("16"))
					f.addr =  address;
				else if (symbol.equals("COV") && firstWord.contentEquals("10"))
					f.addr =  address;
				else if (symbol.equals("XFK") && firstWord.contentEquals("6.25"))
					f.addr =  address;
				else if (symbol.equals("XFK") && firstWord.contentEquals("6,25"))
					f.addr =  address;
				else if (firstWord.contentEquals("0.25") || firstWord.contentEquals("0,25"))
					f.addr =  address;
				else if (symbol.equals("XCH") && firstWord.contentEquals("1E-10"))
					f.addr =  address;

			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if (newTX) {
			TransactionView.refresh();
			newTX = false;
		}
	}

}

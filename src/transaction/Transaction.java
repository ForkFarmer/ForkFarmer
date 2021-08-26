package transaction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import forks.Fork;
import util.Ico;
import util.Util;

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
		Process p = null;
		PrintWriter pw = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(f.exePath, "wallet", "get_transactions");
			pw = new PrintWriter(p.getOutputStream());
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
			for (int i = 0; i < 50; i++)
				pw.println("c");
			pw.close();
			
			String l = null;
			while ( null != (l = br.readLine())) {
				if (l.contains("c to continue")) {
					pw.println("c");
					pw.flush();
				}
				
				if (l.startsWith("Connection error")) {
					p.destroyForcibly();
					break;
				}
				
				if (l.startsWith("Transaction ")) {
					String tHash   = l.replace("Transaction ", "");
					if (TSET.contains(tHash)) // stop parsing if we already have this transaction
						continue;
					TSET.add(tHash);
					
					@SuppressWarnings("unused")
					String status  = br.readLine();
					String amount = br.readLine().substring(8);
					String address = br.readLine().substring(12);
					String date = br.readLine().substring(12);
					
					Transaction t = new Transaction(f, tHash,amount,address,date); 
					
					if (!amount.startsWith("0 "))
						LIST.add(t);
					newTX = true;
					
					if (null != f.addr)
						continue;
					
					String firstWord = amount.substring(0, amount.indexOf(' '));
					firstWord.replace(",", ".");
					
					if (f.rewardTrigger == Double.parseDouble(firstWord))
						f.addr =  address;
					else if (firstWord.contentEquals("0.25") || firstWord.contentEquals("0,25")) //default
						f.addr =  address;
					else if (firstWord.contentEquals("1E-10")) // probably faucet?
						f.addr =  address;

				}

			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Util.closeQuietly(br);
		Util.closeQuietly(pw);
		Util.waitForProcess(p);

		if (newTX) {
			TransactionView.refresh();
			newTX = false;
		}
		
	}

}

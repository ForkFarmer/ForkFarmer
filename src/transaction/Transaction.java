package transaction;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.ImageIcon;

import forks.Fork;
import forks.ForkView;
import types.Balance;
import types.Effort;
import types.TimeU;
import util.FFUtil;
import util.Ico;
import util.Util;

public class Transaction {
	ImageIcon L_ARROW = Ico.loadIcon("icons/arrows/left.png");
	ImageIcon R_ARROW = Ico.loadIcon("icons/arrows/right.png");
	
	private static final Set<String> TSET = new HashSet<>();
	public static final List<Transaction> LIST = new ArrayList<>();
	
	public final Fork   f;
	public String hash = "" ;
	public Balance amount;
	public String target ="";
	public String date = "";
	public boolean blockReward;
	Effort effort = Effort.EMPTY;
	
	public Transaction(Fork f, String hash, double amount, String target, String date, boolean blockReward) {
		this.f = f;
		this.hash = hash;
		this.amount = new Balance(amount);
		this.target = target;
		this.date = date;
		this.blockReward = blockReward;
		
		if (blockReward && getTimeSince().inMinutes() < 5)
			effort = f.getEffort();
	}
	
	public double getAmount() {
		return amount.balance;
	}
	
	public ImageIcon getIcon() {
		if (null != f.wallet.addr)
			if (f.wallet.addr.equals(target))
				return R_ARROW;
		return L_ARROW;
	}
	
	public TimeU getTimeSince() {
		LocalDateTime now = LocalDateTime.now();
		return new TimeU(Duration.between(LocalDateTime.parse(date, FFUtil.DTF),now).getSeconds());
	}
	
	public static boolean load(Fork f) {
		boolean newTX = false;

		if (-1 == f.wallet.index)
			return false;

		Process p = null;
		PrintWriter pw = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(f.exePath, "wallet", "get_transactions");
			pw = new PrintWriter(p.getOutputStream());
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
			pw.println(f.wallet.index); // hack to 
			for (int i = 0; i < 50; i++)
				pw.println("c");
			pw.close();
			
			String l = null;
			while ( null != (l = br.readLine())) {
				boolean blockReward = false;
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
					newTX = true;
					
					@SuppressWarnings("unused")
					String status  = br.readLine();
					String amountStr = br.readLine().substring(8);
					
					String firstWord = amountStr.substring(0, amountStr.indexOf(' '));
					firstWord.replace(",", ".");
					
					double amount = Double.parseDouble(firstWord);
					String address = br.readLine().substring(12);
					String date = br.readLine().substring(12);
					
					synchronized (Transaction.LIST) { 
					
						Optional<Transaction> oT = LIST.stream().filter(z -> z.f.symbol.equals(f.symbol) && z.date.equals(date)).findAny();
					
						if (Math.abs(Double.parseDouble(firstWord) - f.rewardTrigger) < .01)
							blockReward = true;
						else if (firstWord.equals("1E-10")) // probably faucet?
							blockReward = true;
						else if (firstWord.equals("1E-7")) // probably faucet?
							blockReward = true;

						if (oT.isPresent()) {
							Transaction t = oT.get();
							t.amount.add(amount);
							t.blockReward |= blockReward;
							t.effort = f.getEffort();
						} else {
							Transaction t = new Transaction(f, tHash,amount,address,date, blockReward); 
							if (0 != amount)
								LIST.add(t);
						}
						
					}

				}

			}
				
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		Util.closeQuietly(br);
		Util.closeQuietly(pw);
		Util.waitForProcess(p);
			
		if (newTX) {
			synchronized(Transaction.LIST) {
			// update fork last win handle
			f.lastWin = LIST.stream()
					.filter(t -> f == t.f && t.blockReward)
					.reduce((a,b) -> a.getTimeSince().inMinutes() < b.getTimeSince().inMinutes() ? a:b).orElse(null);
			}
			
			ForkView.update(f);
			TransactionView.refresh();
		}

		return newTX;
		
	}
	
	public void browse() {
		String name = f.name;
		String addr = target;
	    try {
	    	if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
	    		Desktop.getDesktop().browse(new URI("https://" + name + ".posat.io/address/" + addr));
	    	}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
	
	}
	
}

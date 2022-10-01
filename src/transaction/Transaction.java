package transaction;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.ImageIcon;

import forks.Fork;
import forks.ForkView;
import main.ForkFarmer;
import types.Balance;
import types.Percentage;
import types.TimeU;
import util.FFUtil;
import util.Ico;
import util.Util;
import web.AllTheBlocks;

public class Transaction {
	public enum TYPE {IN, OUT, REWARD, MEMPOOL, ERROR ,PENDING}
	
	ImageIcon L_ARROW = Ico.loadIcon("icons/arrows/left.png");
	ImageIcon R_ARROW = Ico.loadIcon("icons/arrows/right.png");
	ImageIcon MP 	  = Ico.loadIcon("icons/mp.png");
	
	private static final Set<String> TSET = new HashSet<>();
	public static final List<Transaction> LIST = new ArrayList<>();
	
	public final Fork   f;
	
	public String hash = "" ;
	public Balance amount;
	public Balance value;
	public String str ="";
	public String date = "";
	public TimeU lastWinTime = TimeU.BLANK;
	public TYPE t;
	Percentage effort = Percentage.EMPTY;
	
	public Transaction(Fork f, String hash, double amt, String str, String date, TYPE t) {
		this.f = f;
		this.hash = hash;
		this.str = str;
		this.date = date;
		this.t = t;
		setAmount(amt);
		
		if (0 == amt)
			return;
		
		synchronized(LIST) {
			LIST.add(this);
		}
		
		updateWinEffort();
	}
	
	public void updateWinEffort() {
		if (TYPE.REWARD != t || TimeU.BLANK != this.lastWinTime)
			return;
		
		if (null == f.lastWin)
			f.lastWin = this;
		
		lastWinTime = f.lastWin.getTimeSince();
		effort = f.getEffort();
		
		synchronized(LIST) {
			f.lastWin = LIST.stream()
				.filter(tx -> f == tx.f && TYPE.REWARD == tx.t)
				.reduce((a,b) -> a.getTimeSince().inMinutes() < b.getTimeSince().inMinutes() ? a:b).orElse(null);
		}
		ForkView.update(f);
	}
	
	public void setAmount(double amt) {
		amount = new Balance(amt);
		value = new Balance(amt * f.price,2);
	}
	
	public ImageIcon getIco() {
		return (TYPE.REWARD == t) ? Ico.TROPHY : null;
	}
	
	
	// copy constructor for TxReport
	public Transaction(Transaction t) {
		this.f = t.f;
		setAmount(t.getAmount());
	}

	public double getAmount() {
		return amount.amt;
	}
	
	public Optional<Integer> getIndex() {
		int idx = LIST.indexOf(this);
		return (-1 != idx) ? Optional.of(idx) : Optional.empty();
	}
	
	public ImageIcon getIcon() {
		switch (t) {
		case IN: 		return R_ARROW;
		case MEMPOOL: 	return MP;
		case OUT:		return L_ARROW;
		case REWARD:	return R_ARROW;
		case ERROR:		return Ico.STOP;
		case PENDING:	return Ico.TARGET;
		default:		return null;
		}
	}
	
	public String getName() {
		return f.name;
	}
	
	public TimeU getTimeSince() {
		return TimeU.getTimeSince(LocalDateTime.parse(date, FFUtil.DTF));
	}
	
	public static Transaction load(Fork f) {
		return load(f,50);
	}
	
	public static Transaction load(Fork f, int pages) {
		Transaction newTX = null;
		
		int txRead = 0;
		if (-1 == f.wallet.index)
			return null;
		
		ForkFarmer.LOG.add(f.name + " getting transactions");
		
		Process p = null;
		PrintWriter pw = null;
		BufferedReader br = null;
		try {
			p = Util.startProcess(f.exePath, "wallet", "get_transactions");
			pw = new PrintWriter(p.getOutputStream());
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
			pw.println(f.wallet.index); // hack to 
			for (int i = 0; i < pages; i++)
				pw.println("c");
			pw.println("q");
			pw.close();
			
			String l = null;
			while ( null != (l = br.readLine())) {
				TYPE blockType = TYPE.IN;
				if (l.contains("c to continue")) {
					pw.println("c");
					pw.flush();
				}
				
				if (l.startsWith("Connection error")) {
					p.destroyForcibly();
					break;
				}
				
				if (l.startsWith("Transaction ")) {
					txRead++;
					if (f.symbol.toUpperCase().equals("SIX") &&  txRead > 40) {
						p.destroyForcibly();
						break;
					}
					String tHash   = l.replace("Transaction ", "");
					
					if (TSET.contains(tHash)) {
						continue;
					}
					TSET.add(tHash);
					
					@SuppressWarnings("unused")
					String status  = br.readLine();
					String amountStr = br.readLine();

					if (amountStr.startsWith("Amount: "))
						amountStr = amountStr.substring("Amount: ".length());
					else if (amountStr.startsWith("Amount sent: ")) {
						amountStr = amountStr.substring("Amount sent: ".length());
						blockType = TYPE.OUT;
					} else if (amountStr.startsWith("Amount received: ")) {
						amountStr = amountStr.substring("Amount received: ".length());
						blockType = TYPE.IN;
					} else if (amountStr.startsWith("Amount rewarded: ")) {
						amountStr = amountStr.substring("Amount rewarded: ".length());
						blockType = TYPE.IN;
					} else if (amountStr.startsWith("Amount sent in trade: ")) {
						amountStr = amountStr.substring("Amount sent in trade: ".length());
						blockType = TYPE.OUT;
					} else if (amountStr.startsWith("Amount received in trade: ")) {
						amountStr = amountStr.substring("Amount received in trade: ".length());
						blockType = TYPE.IN;
					}
					
					String firstWord = amountStr.substring(0, amountStr.indexOf(' '));
					firstWord.replace(",", ".");
					
					double amount = Double.parseDouble(firstWord);
					String address = br.readLine().substring(12);
					String date = br.readLine().substring(12);
					
					synchronized (Transaction.LIST) { 
						Optional<Transaction> oT = LIST.stream().filter(z -> z.hash.equals(tHash)).findAny();
						if (oT.isPresent()) {
							Transaction t = oT.get();
							t.t = TYPE.OUT;
							t.update();
							continue;
						}
						
 						oT = LIST.stream().filter(z -> z.f.symbol.equals(f.symbol) && z.date.equals(date)).findAny();
					
 						if (Math.abs(amount - f.fd.nftReward) < .02)
							blockType = TYPE.REWARD;
 						
						if (oT.isPresent()) {
							newTX = oT.get();
							
							newTX.setAmount(newTX.getAmount() + amount);
							
							if (TYPE.REWARD == newTX.t || TYPE.REWARD == blockType)
								newTX.t = TYPE.REWARD;
							
							if (Math.abs(amount - f.fd.nftReward) < .02)
								newTX.t = TYPE.REWARD;
							newTX.updateWinEffort();
						} else {
							newTX = new Transaction(f, tHash,amount,address,date, blockType);
						}
					}

				}

			}
				
		} catch (Exception e) {
			e.printStackTrace();
			f.lastException = e;
		}
		
		Util.closeQuietly(br);
		Util.closeQuietly(pw);
		Util.waitForProcess(p);
		
		ForkFarmer.LOG.add(f.name + " done getting transactions");

		if (null != newTX)
			TransactionView.refresh();
		
		return newTX;
	}
	
	public void browse() {
		AllTheBlocks.browseTX(f.fd.atbPath, str);
	}

	public static void fromLog(Fork f, String s) {
		String timeStamp = s.substring(0,19);
		timeStamp = timeStamp.replace("T", " ");
		
		String txHAsh = Util.getWordAfter(s, "Farmed unfinished_block ");

		new Transaction(f,txHAsh,f.fd.fullReward,"Log Farming Reward",timeStamp,TYPE.REWARD);
		TransactionView.refresh();
	}
	
	public void update() {
		TransactionView.update(this);
	}

	public void setStatus(String msg, TYPE type) {
		t = type;
		str = msg;
		this.update();
		
	}

	/* not used currently
	public static void check(Fork f, String txHash) {
		Process p = null;
		BufferedReader br = null;
		
		try {
			p = Util.startProcess(f.exePath,"wallet","get_transaction","-tx",txHash);
			br = new BufferedReader(new InputStreamReader(p.getInputStream()));
		
			String amountStr = null;
			String l = null;
			while ( null != (l = br.readLine())) {
				if (l.startsWith("Amount sent: ")) {
					amountStr = l.substring("Amount sent: ".length());
					
					String firstWord = amountStr.substring(0, amountStr.indexOf(' '));
					firstWord.replace(",", ".");
					
					double amount = Double.parseDouble(firstWord);
					String address = br.readLine().substring(12);
					String date = br.readLine().substring(12);
					
					Transaction t = new Transaction(f,txHash,amount,address,date,TYPE.OUT);
					add(t);
				}
				
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		Util.closeQuietly(br);
		Util.waitForProcess(p);
	}
	*/
	
	
}

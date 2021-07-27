package main;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import forks.Fork;
import forks.ForkView;
import transaction.Transaction;
import transaction.TransactionView;
import util.process.ProcessPiper;

@SuppressWarnings("serial")
public class MainGui extends JPanel {
	public static int numForks;
	public static ForkView FV = new ForkView();
	// *** Process Panel ***
	public static JProgressBar pBar = new JProgressBar();
	JButton refreshBtn = new JButton("Refresh");
	
	// *** Transaction Panel ***
	static JTextField targetAddress = new JTextField(20);
	static JTextField targetAmt = new JTextField(10);
	static JTextField targetFee = new JTextField(5);
	static JButton sendBtn = new JButton("Send");
	
	GridBagConstraints c = new GridBagConstraints();
	
	public MainGui() {
		setLayout(new BorderLayout());
	
		// ** Progress Panel ***
		//JPanel progPannel = new JPanel(new BorderLayout());
		//ProgPannel.add(pBar, BorderLayout.CENTER);
		//progPannel.add(refreshBtn,BorderLayout.LINE_END);
		//pBar.setStringPainted(true);
		
		refreshBtn.addActionListener(e -> new Thread(() -> refresh()).start());
		
		// *** Transaction Panel ***
		JPanel tPanel = new JPanel();
		tPanel.setLayout(new GridBagLayout());
		c = new GridBagConstraints();
		tPanel.setBorder(new TitledBorder("Create Transaction: (address, amt, fee)"));

		c.fill = GridBagConstraints.BOTH;
		targetFee.setText("0");
		
		c.gridx=1;
        c.weightx=1;
        tPanel.add(targetAddress,c);
        c.gridx=2;
        c.weightx=0;
        tPanel.add(targetAmt,c);
        c.gridx=3;
        c.weightx=0;
        tPanel.add(targetFee,c);
        c.weightx=0;
        c.gridx=4;
        tPanel.add(sendBtn,c);
        
        sendBtn.addActionListener(e -> sendTx());
        

        JPanel PEPanel = new JPanel(new BorderLayout());
        PEPanel.add(tPanel,BorderLayout.CENTER);
        PEPanel.add(new TransactionView(),BorderLayout.PAGE_END);
     	
        //add(progPannel,BorderLayout.PAGE_START);
		add(FV, BorderLayout.CENTER);
		add(PEPanel, BorderLayout.PAGE_END);
		
		pBar.setString("Detecting Installed Forks...");
		Fork.LIST.clear();
		Fork.factory("XCH","Chia");
		Fork.factory("XFX","Flax");
		
		Fork.factory("XFL","Flora");
		Fork.factory("CGN","Chaingreen");
		Fork.factory("XGJ","Goji");
		Fork.factory("XSE","Seno");
		Fork.factory("AVO","Avocado");
		Fork.factory("XKA","Kale");
		Fork.factory("XTX","Taco");
		Fork.factory("XDG","DogeChia");
		
		Fork.factory("SPARE","Spare");
		Fork.factory("XCR","Chiarose");
		Fork.factory("SIT","Silicoin");
		Fork.factory("XCD","Chiadoge");
		
		Fork.factory("XEQ","Equality");
		Fork.factory("SOCK","Socks");
		Fork.factory("WHEAT","Wheat");
		Fork.factory("XMX","Melati");
		Fork.factory("TAD","Tad");
		Fork.factory("CAN", "Cannabis");
		Fork.factory("XSC","Sector");
		Fork.factory("CAC","Cactus");
		Fork.factory("Chives","Chives");
		
		numForks = Fork.LIST.size();
		pBar.setString(numForks + " forks installed");
		FV.setBorder(new TitledBorder(numForks + " Forks Intalled" ));
		refresh();
		pBar.setMaximum(numForks);
	}
	
	private void sendTx() {
		String address = targetAddress.getText();
		
		Fork target = null;
		for (Fork f : Fork.LIST) {
			if (address.startsWith(f.getSymbol().toLowerCase())) {
				f.sendTX(address,targetAmt.getText(),targetFee.getText());
				return;
			}
		}
		
		ForkFarmer.showMsg("Error", "No suitable fork found for address prefix");
		
	}

	public void refresh() {
		Transaction.LIST.clear();
		for (Fork f : Fork.LIST)
			Fork.SVC.submit(() -> f.loadWallet());
	}
	

}

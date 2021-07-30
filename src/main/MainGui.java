package main;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import forks.Fork;
import forks.ForkView;
import transaction.TransactionView;

@SuppressWarnings("serial")
public class MainGui extends JPanel {
	public static int numForks;
	public static ForkView FV = new ForkView();
	
	// *** Transaction Panel ***
	static JTextField targetAddress = new JTextField(20);
	static JTextField targetAmt = new JTextField(10);
	static JTextField targetFee = new JTextField(5);
	static JButton sendBtn = new JButton("Send");
	
	GridBagConstraints c = new GridBagConstraints();
	
	public MainGui() {
		setLayout(new BorderLayout());
	
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
        PEPanel.add(tPanel,BorderLayout.PAGE_START);
        PEPanel.add(new TransactionView(),BorderLayout.CENTER);

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        	splitPane.setTopComponent(FV);
        	splitPane.setBottomComponent(PEPanel);
        add (splitPane);
        			
		Fork.LIST.clear();
		Fork.factory("XCH","Chia");
		Fork.factory("XFX","Flax");
		
		Fork.factory("XFL","Flora");
		Fork.factory("CGN","Chaingreen","Chaingreen");
		Fork.factory("XGJ","Goji","goji-blockchain");
		Fork.factory("XSE","Seno","seno2");
		Fork.factory("AVO","Avocado");
		Fork.factory("XKA","Kale");
		Fork.factory("XTX","Taco");
		Fork.factory("XDG","DogeChia");
		
		Fork.factory("SPARE","Spare");
		Fork.factory("XCR","Chiarose","Chiarose","chia-rosechain");
		Fork.factory("SIT","Silicoin");
		Fork.factory("XCD","Chiadoge");
		Fork.factory("GDOG","Greendoge");
		Fork.factory("HDD","Hddcoin");
		
		Fork.factory("XEQ","Equality");
		Fork.factory("SOCK","Socks");
		Fork.factory("WHEAT","Wheat");
		Fork.factory("XMX","Melati");
		Fork.factory("TAD","Tad", "tad-coin");
		Fork.factory("CANS", "Cannabis");
		Fork.factory("XSC","Sector");
		Fork.factory("CAC","Cactus");
		Fork.factory("CHIVES","Chives");
		Fork.factory("APPLE","Apple");
		Fork.factory("XMZ","Maize");
		Fork.factory("COV","Covid");
		Fork.factory("SRN","Shamrock");
		
		numForks = Fork.LIST.size();
		FV.setBorder(new TitledBorder(numForks + " Forks Intalled" ));
		
		ScheduledExecutorService SVC = Executors.newSingleThreadScheduledExecutor();
		SVC.scheduleAtFixedRate(MainGui::refresh, 0, 60, TimeUnit.SECONDS);
	}
	
	private void sendTx() {
		String address = targetAddress.getText();
		
		for (Fork f : Fork.LIST) {
			if (address.startsWith(f.symbol.toLowerCase())) {
				f.sendTX(address,targetAmt.getText(),targetFee.getText());
				return;
			}
		}
		
		ForkFarmer.showMsg("Error", "No suitable fork found for address prefix");
	}

	private static void refresh() {
		for (Fork f : Fork.LIST)
			Fork.SVC.submit(() -> f.loadWallet());
	}
}

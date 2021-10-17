package main;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.TitledBorder;

import forks.Fork;
import forks.ForkView;
import transaction.TransactionView;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.swing.SwingEX;
import util.swing.SwingEX.LTPanel;

@SuppressWarnings("serial")
public class MainGui extends JPanel {
	public static ForkView FV = new ForkView();
	
	// *** Transaction Panel ***
	static JTextField targetAddress = new JTextField(20);
	static JTextField targetAmt = new JTextField(10);
	static JTextField targetFee = new JTextField(5);
	static JButton sendBtn = new JButton("Send");
	private static NetSpace plotSize = new NetSpace("0 TiB");
	private static final JLabel plotlbl = new JLabel("Farm Size: ?");
	private static final JLabel valuelbl = new JLabel("Value: $0.0");
	private static final JLabel forklbl = new JLabel(Integer.toString(Fork.LIST.size()) + " Forks", SwingConstants.CENTER);
	
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
        c.gridx=5;
        tPanel.add(new SwingEX.Btn("",Ico.GEAR, () -> {
        	JPanel settingPanel = new JPanel(new GridBagLayout());
        	
        	JPanel logReader = new JPanel(new GridLayout(2,1));
        	logReader.setBorder(new TitledBorder("Log Reader:"));
        	JPanel daemonReader = new JPanel(new GridLayout(2,1));
        	daemonReader.setBorder(new TitledBorder("Daemon Reader:"));
        	JPanel curencyPanel = new JPanel(new GridLayout(1,2));
        	curencyPanel.setBorder(new TitledBorder("Currency:"));
        	
        	
        	LTPanel lriSleep = new SwingEX.LTPanel("    Intra Delay (ms): " , Integer.toString(Settings.GUI.logReaderIntraDelay));
    		LTPanel lreSleep = new SwingEX.LTPanel("    Exo Delay (ms): " , Integer.toString(Settings.GUI.logReaderExoDelay));
    		
    		LTPanel dWorkers = new SwingEX.LTPanel("    Worker threads (requires restart): " , Integer.toString(Settings.GUI.daemonReaderWorkers));
    		LTPanel dIntraSleep = new SwingEX.LTPanel("    Delay (ms): " , Integer.toString(Settings.GUI.daemonReaderDelay));
    		
    		LTPanel curencySymbol = new SwingEX.LTPanel("    Symbol: " , Settings.GUI.currencySymbol);
    		LTPanel curencyRatio = new SwingEX.LTPanel("    Ratio x*$: " , Double.toString(Settings.GUI.currencyRatio));
    		
    		        	
    		logReader.add(lriSleep);
    		logReader.add(lreSleep);
    		
    		daemonReader.add(dWorkers);
    		daemonReader.add(dIntraSleep);
    		
    		curencyPanel.add(curencySymbol);
    		curencyPanel.add(curencyRatio);
    		
    		GridBagConstraints gbc = new GridBagConstraints();
    		gbc.fill = GridBagConstraints.HORIZONTAL;
        	settingPanel.add(logReader,gbc);
        	gbc.gridy=1;
        	settingPanel.add(daemonReader,gbc);
        	gbc.gridy=2;
        	settingPanel.add(curencyPanel,gbc);
        	
        	if (true == ForkFarmer.showPopup("Settings:", settingPanel)) {
        		Settings.GUI.logReaderIntraDelay = lriSleep.getAsInt();
        		Settings.GUI.logReaderExoDelay = lreSleep.getAsInt();
        		Settings.GUI.daemonReaderWorkers = dWorkers.getAsInt();
        		Settings.GUI.daemonReaderDelay = dIntraSleep.getAsInt();
        		Settings.GUI.currencySymbol = curencySymbol.getText();
        		Settings.GUI.currencyRatio = curencyRatio.getAsDouble();
        		
        		MainGui.updateTotal();
        	}
        		
        	
        }));
        	
        
        sendBtn.addActionListener(e -> sendTx());
        		
        JPanel PEPanel = new JPanel(new BorderLayout());
        PEPanel.add(tPanel,BorderLayout.PAGE_START);
        PEPanel.add(new TransactionView(),BorderLayout.CENTER);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel topCenter = new JPanel(new BorderLayout());
        topCenter.add(forklbl, BorderLayout.CENTER);
        
        topPanel.add(valuelbl,BorderLayout.LINE_START);
        topPanel.add(topCenter, BorderLayout.CENTER);
        topPanel.add(plotlbl,BorderLayout.LINE_END);
        
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        	splitPane.setTopComponent(FV);
        	splitPane.setBottomComponent(PEPanel);
        add (topPanel,BorderLayout.PAGE_START);
        add (splitPane, BorderLayout.CENTER);
        
        updatePlotSize(plotSize);
        			
		// compute fork refresh delay
		new Thread(ForkView::daemonReader).start();
		new Thread(ForkView::logReader).start();
		ForkView.TABLE.setAutoCreateRowSorter(true);
		
		Args.handle();
	}
	
	private void sendTx() {
		String address = targetAddress.getText();
		
		for (Fork f : Fork.LIST) {
			if (address.startsWith(f.symbol.toLowerCase())) {
				f.sendTX(address,targetAmt.getText(),targetFee.getText());
				targetAddress.setText("");
				targetAmt.setText("");
				return;
			}
		}
		
		ForkFarmer.showMsg("Error", "No suitable fork found for address prefix");
	}
	public static void updatePlotSize(NetSpace ps) {
		if (ps.szTB > plotSize.szTB) {
			plotSize = ps;
			plotlbl.setText("Farm Size: " + ps.toString());
		}
	}

	public static void updateTotal() {
		double totalValue = 0;

		for (Fork f : Fork.LIST)
			totalValue += f.equity.amt;
		
		valuelbl.setText("Value: " + Settings.GUI.currencySymbol + Util.round(totalValue, 2));
	}
	
	public static void updateNumForks() {
		forklbl.setText(Integer.toString(Fork.LIST.size()) + " Forks");
	}
	
}

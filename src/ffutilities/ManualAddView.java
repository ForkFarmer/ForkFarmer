package ffutilities;

import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.util.stream.Collectors;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import forks.Fork;
import forks.ForkData;
import types.Wallet;
import util.swing.SwingEX.LTPanel;

@SuppressWarnings("serial")
public class ManualAddView extends JPanel {
	String[] symArray = ForkData.LIST.stream().map(fd -> fd.coinPrefix).collect(Collectors.toList()).toArray(new String[0]);
	final JComboBox<String> symbolBox = new JComboBox<String>(symArray);
	
	final LTPanel configPath = new LTPanel("Config Path: ");
	final LTPanel exePath = new LTPanel("Exe Path: ");
	final LTPanel logPath = new LTPanel("Log Path: ");
	final JCheckBox walletNode = new JCheckBox("Wallet Node");
	final JCheckBox fullNode = new JCheckBox("Full Node");
	
	public ManualAddView() {
		setLayout(new GridLayout(5,1));
		
		add(symbolBox);
		add(configPath);
		add(exePath);
		add(logPath);
		JPanel nodeType = new JPanel(new GridLayout(1,2));
		
		nodeType.add(walletNode);
		nodeType.add(fullNode);
		
		walletNode.setSelected(true);
		fullNode.setSelected(true);
		
		add(nodeType);
	}

	public Fork loadFork() throws FileNotFoundException {
		String symbol = symbolBox.getSelectedItem().toString().toLowerCase();
		ForkData fd = ForkData.getBySymbol(symbol).get();
		
		fd.configPath = configPath.getText();
		fd.exePath = exePath.getText();
		fd.logPath = logPath.getText();
		Fork f = fd.loadFork();

		f.fullNode = fullNode.isSelected();
		f.walletNode = walletNode.isSelected();
		
		f.wallet = new Wallet("","Restart FF to update", -1);
		
		return f;
	}

}

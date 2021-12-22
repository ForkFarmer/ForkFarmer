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
import util.I18n;
import util.swing.SwingEX.LTPanel;

@SuppressWarnings("serial")
public class ManualAddView extends JPanel {
	String[] symArray = ForkData.LIST.stream().map(fd -> fd.coinPrefix).collect(Collectors.toList()).toArray(new String[0]);
	final JComboBox<String> symbolBox = new JComboBox<String>(symArray);
	
	final LTPanel configPath = new LTPanel(I18n.ManualAddView.configPath);
	final LTPanel exePath = new LTPanel(I18n.ManualAddView.exePath);
	final LTPanel logPath = new LTPanel(I18n.ManualAddView.logPath);
	final JCheckBox walletNode = new JCheckBox(I18n.ManualAddView.walletNode);
	final JCheckBox fullNode = new JCheckBox(I18n.ManualAddView.fullNode);
	
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
		
		f.wallet = new Wallet("",I18n.ManualAddView.afterLoadTip, -1);
		
		return f;
	}

}

package main;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

import util.I18n;

@SuppressWarnings("serial")
public class TransactionPanel extends JPanel {
	public JTextField targetAddress = new JTextField(20);
	public JTextField targetAmt = new JTextField(10);
	public JTextField targetFee = new JTextField(6);
	public JButton sendBtn = new JButton(I18n.MainGui.sendBtn);
	
	GridBagConstraints c = new GridBagConstraints();
	
	public TransactionPanel() {
		
		setLayout(new GridBagLayout());
		
		setBorder(new TitledBorder(I18n.MainGui.createTransaction));

		c.fill = GridBagConstraints.BOTH;
		targetFee.setText(".0000001");
				
		c.gridx=1;
		c.weightx=1;
		add(targetAddress,c);
		c.gridx=2;
		c.weightx=0;
		add(targetAmt,c);
		c.gridx=3;
		c.weightx=0;
		add(targetFee,c);
		c.weightx=0;
		c.gridx=4;
		add(sendBtn,c);
	}
}

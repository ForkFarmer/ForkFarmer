package forks;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import main.ForkFarmer;
import main.Settings;
import util.I18n;
import util.Util;
import util.swing.SwingEX.LTPanel;
import util.swing.jfuntable.KVPair;
import util.swing.jfuntable.KVTable;

public class ForkStarter {
	public static List<KVPair> LIST;
	
	public static String getStartCmd(String key) {
		return LIST.stream().filter(kv -> kv.key.equals(key)).findAny().get().value;
	}
	
	static public void activate(List<Fork> fList) {
		Process p = null;

		for (Fork f: fList) {
			
			System.out.println("Running activate on " + f.name);
			System.out.println("Base Dir is: " + f.fd.daemonBase);
		
			StringBuilder sb = new StringBuilder();
			sb.append("'cd ");
			sb.append(f.fd.daemonBase);
			sb.append(" && . ./activate && ");
			sb.append(f.name.toLowerCase());
			sb.append(" start farmer-no-wallet'");
			
			String script = sb.toString();
			try {
				
				System.out.println("Starting activate script");
				System.out.println("Script: + " + script);
				
				ProcessBuilder pb = new ProcessBuilder("bash", "-c", script);
				p = pb.start();
				p.wait();
				System.out.println("Done running activate script");
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	static public void custom(List<Fork> fList, String script) {
		Process p = null;

		for (Fork f: fList) {
			
			System.out.println("Running activate on " + f.name);
			System.out.println("Base Dir is: " + f.fd.daemonBase);
		
			try {
				
				System.out.println("Starting custom activate script");
				System.out.println("Script: + " + script);
				
				ProcessBuilder pb = new ProcessBuilder("bash", "-c", script);
				p = pb.start();
				p.wait();
				System.out.println("Stopping activate script");
				
				
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	static public void edit() {
		getSettings();
		JPanel ESPanel = new JPanel(new BorderLayout());
		
		ESPanel.add(new JLabel(I18n.ForkStarter.editStartTableTitle), BorderLayout.PAGE_START);
		ESPanel.add(new KVTable(LIST), BorderLayout.CENTER);
		
		JOptionPane.showConfirmDialog(ForkFarmer.FRAME,ESPanel,I18n.ForkStarter.editStartPanelTitle, JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);
	}
	
	
	static public void start(Fork f) {
		getSettings();
		
		if (f.walletNode && f.fullNode)
			exeCustCMD(f,getStartCmd("FN & WN"));
		else if (f.walletNode && !f.fullNode)
			exeCustCMD(f,getStartCmd("WN"));
		else if (!f.walletNode && f.fullNode)
			exeCustCMD(f,getStartCmd("FN"));
		else
			exeCustCMD(f,getStartCmd("Neither"));
	}
	
	static public void newCustCMD(List<Fork> selList) {
		JPanel customPanel = new JPanel(new GridLayout(selList.size() > 1 ? 3 : 2 ,1));
		JCheckBox updateChk = new JCheckBox(I18n.ForkStarter.customImmeditateRunLabel);
		
		LTPanel cmdP = new LTPanel(I18n.ForkStarter.customCmdLabel , Settings.GUI.custLastCustom);
		LTPanel staggerP = new LTPanel(I18n.ForkStarter.customStaggerIntevalLabel, Settings.GUI.custLastDelay);
		updateChk.setSelected(Settings.GUI.custForceUpdate);
		customPanel.add(cmdP);
		if (selList.size() > 1)
			customPanel.add(staggerP);
		customPanel.add(updateChk);
		
		if (false == ForkFarmer.showPopup(I18n.ForkStarter.customCmdPanelTitle, customPanel))
			return;
		
		Settings.GUI.custLastCustom = cmdP.field.getText();
		Settings.GUI.custLastDelay = staggerP.field.getText();
		Settings.GUI.custForceUpdate = updateChk.isSelected();
		
		long sleepDelay = Long.parseLong(staggerP.field.getText());
		
		new Thread(() -> {
			for (Fork f : selList) {
				exeCustCMD(f, cmdP.field.getText());
				if (updateChk.isSelected())
					f.loadWallet();
				Util.sleep(sleepDelay);
			}
		}).start();
	}
	
	static private void exeCustCMD(Fork f, String cmd) {
		String[] cmds = cmd.split(",");

		for (String s : cmds) {
			String[] args = s.split(" ");
			
			String[] varArgs = null;
			if (null == f.passFile) {
				varArgs = new String[args.length + 1];
				varArgs[0] = f.fd.exePath;
				for (int i = 0; i < args.length; i++)
					varArgs[i+1] = args[i];
			} else {
				varArgs = new String[args.length + 3];
				varArgs[0] = f.fd.exePath;
				varArgs[1] = "--passphrase-file";
				varArgs[2] = f.passFile;
				for (int i = 0; i < args.length; i++)
					varArgs[i+3] = args[i];
			}
				
			Util.runProcessWait(varArgs);
		}
	}

	@SuppressWarnings("unchecked")
	public static Object getSettings() {
		if (null == LIST) // read settings if not already populated
			LIST = (List<KVPair>) Settings.settings.get("ForkStarter");
		if (null == LIST) { // if no settings populate with defaults
			LIST = new ArrayList<>();
			LIST.add(new KVPair("FN & WN","start farmer"));
			LIST.add(new KVPair("FN","start farmer-no-wallet"));
			LIST.add(new KVPair("WN","start wallet"));
			LIST.add(new KVPair("Neither","start harvester"));
		}
		return LIST;
	}

}

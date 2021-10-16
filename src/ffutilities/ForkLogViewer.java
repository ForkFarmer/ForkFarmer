package ffutilities;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;

import forks.Fork;
import main.ForkFarmer;
import util.Ico;
import util.Util;
import util.apache.ReversedLinesFileReader;
import util.swing.SwingEX;

@SuppressWarnings("serial")
public class ForkLogViewer extends JFrame {
	JPanel logPanel = new JPanel(new BorderLayout());
	JCheckBox autoUpdate = new JCheckBox("Auto update");
	SwingEX.LTPanel linesField = new SwingEX.LTPanel("Lines to read:   ", "");
	JPanel bottomPanel = new JPanel(new BorderLayout());
	
	JTabbedPane JTP = new JTabbedPane();
	List<LogViewer> list = new ArrayList<>();
	
	
	public ForkLogViewer(List<Fork> selected) {
		super();
		if (0 == selected.size()) {
			dispose();
			return;
		}
		
		bottomPanel.add(autoUpdate, BorderLayout.LINE_START);
		bottomPanel.add(linesField, BorderLayout.LINE_END);
		logPanel.add(bottomPanel,BorderLayout.PAGE_END);
		
		linesField.setPreferredSize(new Dimension (150,18));
		autoUpdate.setSelected(true);
		
		if (1 == selected.size()) {
			Fork f = selected.get(0);
			LogViewer flv = new LogViewer(f);
			list.add(flv);
			setIconImage(f.ico.getImage());
			setTitle(f.name + " log view");
			logPanel.add(flv,BorderLayout.CENTER);
		} else {
			setIconImage(Ico.LOGO.getImage());
			setTitle("Log Viewer");
			logPanel.add(JTP,BorderLayout.CENTER);
			for (Fork f : selected) {
				LogViewer flv = new LogViewer(f);
				list.add(flv);
				JTP.addTab(f.name + " log",f.ico, flv);
			}
		}
		
		
		setContentPane(logPanel);
		pack();
		setLocationRelativeTo(ForkFarmer.FRAME);

		setVisible(true);
		
		addWindowListener(new WindowAdapter() {
		    public void windowClosing(WindowEvent e) {
		    	list.forEach(flv -> flv.killed = true);
		    }
		});
		
		updateDimension();
		list.forEach(flv -> flv.startLogThread());
		
		this.addComponentListener(new ResizeListener());
		
	}
	
	class ResizeListener extends ComponentAdapter {
        public void componentResized(ComponentEvent e) {
        	updateDimension();
        }
}
	
	public void updateDimension() {
		int height = logPanel.getSize().height-60;
		linesField.field.setText(String.valueOf(height/16));
	}
	
	public class LogViewer extends JPanel {
		final Fork f;
		final JTextArea jta = new JTextArea();
		boolean killed;
		
		public LogViewer(Fork f) {
			this.f = f;
			setLayout(new BorderLayout());
			
			jta.setEditable(false);
			JScrollPane JSP = new JScrollPane(jta);
			
			JSP.setPreferredSize(new Dimension(1000,800));
			
			add(JSP, BorderLayout.CENTER);
			
			updateText();
			
			
		}
		
		public void startLogThread() {
			new Thread(() -> {
				while (!killed) {
					if (autoUpdate.isSelected())
						updateText();
					Util.sleep(1000);
				}
			}).start();
		}
		
		public void updateText() {
			File logFile = new File(f.logPath);
			ReversedLinesFileReader lr = null;
			try {
				lr = new ReversedLinesFileReader(logFile,Charset.defaultCharset());
				String s;
				int i =0;
				StringBuilder sb = new StringBuilder();
				while (null != (s = lr.readLine()) && i < getLines()) {
					i++;
					sb.append(s + "\n");
				}
				jta.setText(sb.toString());
				jta.setCaretPosition(0);
			} catch (IOException e1) {
				e1.printStackTrace();
			};
			Util.closeQuietly(lr);
		}
		
		private int getLines() {
			try {
				return Integer.parseInt(linesField.getText());
			} catch (Exception e ) {
				return 100;
			}
		}
	}
	

}

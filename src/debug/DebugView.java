package debug;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import forks.Fork;
import util.Util;

@SuppressWarnings("serial")
public class DebugView extends JPanel {
	Fork f;
	
	JTextArea textArea = new JTextArea();
	JScrollPane JSP = new JScrollPane(textArea);
	
	public DebugView(Fork f) {
		this.f = f;
		setLayout(new BorderLayout());
		JSP.setPreferredSize(new Dimension(750,600));
		add(JSP, BorderLayout.CENTER);
				
		new Thread(() -> {
			String ret;
			StringBuilder sb = new StringBuilder();
			sb.append("Running debug for " + f.name + "\n");
		
			if (null != f.lastException) {
				StringWriter sw = new StringWriter();
				PrintWriter pw = new PrintWriter(sw);
				f.lastException.printStackTrace(pw);
				String sStackTrace = sw.toString(); // stack trace as a string
				sb.append(sStackTrace + "\n");
			} else
				sb.append("FF Exceptions: None\n");
			sb.append("--------------------------------\n\n");
			
			if (!f.cold) {
			
				if (f.walletNode) {
					sb.append("Running wallet show\n");
					sb.append("ExePath: " + f.exePath + "\n");
					textArea.setText(sb.toString());
					
					ret = Util.runProcessDebug(f.exePath,"wallet","show");
					sb.append("\n" +  ret);
					sb.append("--------------------------------\n\n");
				} else {
					sb.append("walletNode=false for " + f.name + " skipping wallet test\n");
				}
				
				sb.append("Running: farm summary\n");
				sb.append("ExePath: " + f.exePath + "\n");
				textArea.setText(sb.toString());
				
				ret = Util.runProcessDebug(f.exePath,"farm","summary");
				sb.append("\n" +  ret);
				sb.append("--------------------------------\n\n");
			}
			sb.append(" == Debug Complete ==\n");
			textArea.setText(sb.toString());
		}).start();
		
	}
	

}

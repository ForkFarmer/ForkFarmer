package util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import main.ForkFarmer;

public class FFUtil {
	transient public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	public static Map<String,Double> getPrices() {
		Map<String,Double> priceMap = new HashMap<>();
		
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();
			
			StringBuilder sb = new StringBuilder();
	        
	        String plainCredentials = "psgjolbu;psgjolbu"; //did this so auth string not scraped
	        for (char c : plainCredentials.toCharArray())
	        	sb.append((char)(c-1));
	        String base64Credentials = new String(Base64.getEncoder().encode(sb.toString().getBytes()));
	                String authorizationHeader = "Basic " + base64Credentials;
	        
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://xchforks.com/partners/forkfarmer/vwap.csv"))
	                .timeout(Duration.ofSeconds(5))
	                .header("Authorization", authorizationHeader)
	                .build();
	
	        HttpResponse<String> response;
	        
	        try {
	        	response = client.send(request,
				        HttpResponse.BodyHandlers.ofString());
	        	
	        	String csvReponse = response.body();
	        	Scanner scanner = new Scanner(csvReponse);
	        	scanner.useDelimiter(",");
	        	while (scanner.hasNextLine()) {
	        		String line = scanner.nextLine();
	        		line = line.replaceAll("\"", "");
	        		String[] split = line.split(",");
	        		if (split[1].equals("\\N"))
	        			split[1] = "0";
	        		priceMap.put(split[0], Double.parseDouble(split[1]));
	        	}
	        	scanner.close();
	        	
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			JPanel logPanel = new JPanel(new BorderLayout());
			JTextArea jta = new JTextArea();
			JScrollPane JSP = new JScrollPane(jta);
			JSP.setPreferredSize(new Dimension(900,600));
			logPanel.add(JSP,BorderLayout.CENTER);
			
			ForkFarmer.showPopup("Paste xchforks.com table", logPanel);
			
			String xchForksTable = jta.getText();
			
			String[] rows = xchForksTable.split("\n");
			
			for (String row : rows) {
				row = row.replaceAll("\t", " ");
				row = row.replaceAll("\\s+", " ");
				String[] cols = row.split(" ");
				
				if (cols.length < 9)
					continue;
				
				priceMap.put(cols[1], Double.parseDouble(cols[7]));
			}
		}
		return priceMap;
	}
	
	public static LocalDateTime parseTime(String s) {
		if (s.length() < 19)
			return null;
		
		try {
			String logTimeString = s.substring(0,19);
			logTimeString = logTimeString.replace("T", " ");
			return LocalDateTime.parse(logTimeString, DTF);
		} catch (Exception e) {
			return null;
		}
		
		
	}

}

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
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import forks.Fork;
import main.ForkFarmer;
import types.Balance;
import types.XchForksData;

public class FFUtil {
	transient public static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	
	
	public static Balance getAllTheBlocksBalance(Fork f) {
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		
		HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.alltheblocks.net/" + f.name.toLowerCase()+"/address/"+f.walletAddr))
                .timeout(Duration.ofSeconds(5))
                .build();
		
		HttpResponse<String> response;
        
        try {
        	response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
        	
        	String jsonResponse = response.body();
        	
        	JSONParser parser = new JSONParser();
        	JSONObject jo = (JSONObject) parser.parse(jsonResponse);
        	
        	long balance = (long)jo.get("balance");
        	Balance b = new Balance((double)balance/(double)1000000000/(double)1000);
        	return b;
        	
        } catch (Exception e) {
        	f.lastException = e;
			f.statusIcon = Ico.RED;
			return new Balance("error",0);
		} 
	}
	
	public static List<XchForksData> getXCHForksData() {
		List<XchForksData> list = new ArrayList<>();
		
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();
			
			//StringBuilder sb = new StringBuilder();
	        
	        String plainCredentials = "orfinkat:gqr7654pjn348c3u"; //did this so auth string not scraped
	        //for (char c : plainCredentials.toCharArray())
	        	//sb.append((char)(c-1));
	        String base64Credentials = new String(Base64.getEncoder().encode(plainCredentials.getBytes()));
	                String authorizationHeader = "Basic " + base64Credentials;
	        
	        HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://xchforks.com/api/v1/listings"))
	                .timeout(Duration.ofSeconds(5))
	                .header("Authorization", authorizationHeader)
	                .build();
	
	        HttpResponse<String> response;
	        
	        try {
	        	response = client.send(request,
				        HttpResponse.BodyHandlers.ofString());
	        	
	        	String jsonResponse = response.body();
	        	
	        	JSONParser parser = new JSONParser();
	        	JSONArray jsonArray = (JSONArray) parser.parse(jsonResponse);
	        	
	        	for(Object o : jsonArray)
        			list.add(new XchForksData((JSONObject) o));
	        	
	        } catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			e.printStackTrace();
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
				
				XchForksData data = new XchForksData();
				data.price = Double.parseDouble(cols[7]);
				data.symbol = cols[2];
				
				list.add(data);
			}
		}
		
		return list;
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

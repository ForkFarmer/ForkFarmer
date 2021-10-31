package web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import forks.Fork;
import logging.LogView;
import main.MainGui;
import types.Balance;
import util.Ico;

// Interacts with alltheblocks.net API. Thanks!
public class AllTheBlocks {
	public static LocalDateTime lastUpdate;
	private static int UPDATE_RATE_SEC = 60*5;
	
	@SuppressWarnings("unchecked")
	public static String createJSonReq(List<String> addrList) {
		JSONObject reqObject = new JSONObject();
		reqObject.put("addresses", addrList);
		return reqObject.toJSONString();
	}
	
	public static void updateColdBalances() {
		if (null == lastUpdate)
			lastUpdate = LocalDateTime.now();
		else if (Duration.between(lastUpdate, LocalDateTime.now()).getSeconds() < UPDATE_RATE_SEC)
			return; // too early to update
		updateColdForced();
	}
	
	@SuppressWarnings("unchecked")
	public static void updateColdForced() {
		lastUpdate = LocalDateTime.now();
		
		LogView.add("alltheblocks.net cold wallet update");
		
		try {
			List<String> addrList = new ArrayList<>();
			for (Fork f: Fork.LIST) {
				if (null != f.wallet && f.wallet.cold)
					addrList.add(f.wallet.addr);
			}
			
			if (0 == addrList.size())
				return;
			
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();
			
			HttpRequest request = HttpRequest.newBuilder()
		            .uri(URI.create("https://api.alltheblocks.net/atb/watchlist"))
		            .header("Content-Type", "application/json")
		            .POST(BodyPublishers.ofString(createJSonReq(addrList)))
		            .build();
	
			HttpResponse<String> response;
	      
        	response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
	        	
        	String jsonResponse = response.body();
	        	
        	JSONParser parser = new JSONParser();
        	JSONArray ja = (JSONArray) parser.parse(jsonResponse);
	
        	ja.forEach(item -> {
        	    JSONObject jo = (JSONObject) item;
        	    String address = (String)jo.get("address");
	            	
           	for (Fork f: Fork.LIST) {
            		if (null != f.wallet && f.wallet.addr.equals(address))
            			updateFork(f,jo);
           		}
	        });
        	MainGui.updateTotal();
        } catch (Exception e) {
        	e.printStackTrace();
		} 
	}
	
	public static void updateColdBalance(Fork f) {
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();

		if (null == f.fd.atbPath) {
			f.lastException = new Exception(f.name + " not supported by alltheblocks.net");
			f.statusIcon = Ico.RED;
			f.updateBalance(new Balance("error",0));
		}
		
		if (0 == f.fd.mojoPerCoin) {
			f.lastException = new Exception(f.name + " missing mojoPerCoin");
			f.statusIcon = Ico.RED;
			f.updateBalance(new Balance("error",0));
		}
		
		HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://api.alltheblocks.net/" + f.fd.atbPath + "/address/" +f.walletAddr))
                .timeout(Duration.ofSeconds(5))
                .build();
		
		HttpResponse<String> response;
        
        try {
        	response = client.send(request,
			        HttpResponse.BodyHandlers.ofString());
        	
        	String jsonResponse = response.body();
        	
        	JSONParser parser = new JSONParser();
        	JSONObject jo = (JSONObject) parser.parse(jsonResponse);
        	
        	updateFork(f,jo);
        } catch (Exception e) {
        	f.lastException = e;
			f.statusIcon = Ico.RED;
			f.updateBalance(new Balance("error",0));
		} 
	}

	private static void updateFork(Fork f, JSONObject jo) {
		long balance = (long)jo.get("balance");
    	long balanceBefore = (long)jo.get("balanceBefore");
    	        	
    	f.dayWin = (double)(balance-balanceBefore)/(double)f.fd.mojoPerCoin;
    	Balance b = new Balance((double)balance/(double)f.fd.mojoPerCoin);
    	f.updateBalance(b);
	}
}

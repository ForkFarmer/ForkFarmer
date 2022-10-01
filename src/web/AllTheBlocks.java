package web;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import forks.Fork;
import forks.ForkData;
import forks.ForkView;
import main.ForkFarmer;
import main.MainGui;
import types.Balance;
import util.Ico;
import util.NetSpace;
import util.Util;
import util.json.JsonArray;
import util.json.JsonObject;
import util.json.Jsoner;

// Interacts with alltheblocks.net API. Thanks!
public class AllTheBlocks {
	public static LocalDateTime lastUpdate;
	private static int UPDATE_RATE_SEC = 60*5;
	
	public static String createJSonReq(List<String> addrList) {
		JsonObject reqObject = new JsonObject();
		reqObject.put("addresses", addrList);
		return Jsoner.serialize(reqObject);
	}
	
	public static void updateATB() {
		if (null == lastUpdate)
			lastUpdate = LocalDateTime.now();
		else if (Duration.between(lastUpdate, LocalDateTime.now()).getSeconds() < UPDATE_RATE_SEC)
			return; // too early to update
		updateColdForced();
		updateStatsForced();
	}
	
	public static void updateColdForced() {
		lastUpdate = LocalDateTime.now();
		
		ForkFarmer.LOG.add("alltheblocks.net cold wallet update");
		
		try {
			List<String> addrList = new ArrayList<>();
			for (Fork f: Fork.LIST) {
				if (null != f.wallet && f.wallet.cold)
					addrList.add(f.wallet.addr);
				else if (!f.walletNode)
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
	        	
        	Object o = Jsoner.deserialize(jsonResponse);
        	
        	if (o instanceof JsonObject) {
        		updateForkWallet((JsonObject)o);
        	} else if (o instanceof JsonArray) {
        		JsonArray ja = (JsonArray)o;
        		ja.forEach(item -> {
        			updateForkWallet((JsonObject)item);
        		});
        	}
        	MainGui.updateTotal();
        } catch (Exception e) {
        	e.printStackTrace();
		} 
	}
	
	private static void updateForkWallet(JsonObject jo) {
		String address = (String)jo.get("address");
		for (Fork f: Fork.LIST) {
				if (null != f.wallet && f.wallet.addr.equals(address))
					updateFork(f,jo);
		}
	}
	
	public static void updateColdBalance(Fork f) {
		
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
		
        try {
        	updateFork(f,HttpUtil.requestJSO("https://api.alltheblocks.net/" + f.fd.atbPath + "/address/" +f.walletAddr).jo);
        } catch (Exception e) {
        	f.lastException = e;
			f.statusIcon = Ico.RED;
			f.updateBalance(new Balance("error",0));
		} 
	}

	private static void updateFork(Fork f, JsonObject jo) {
		long balance = ((BigDecimal)jo.get("balance")).longValue();
    	long balanceBefore = ((BigDecimal)jo.get("balanceBefore")).longValue();
    	        	
    	f.dayWin = (double)(balance-balanceBefore)/(double)f.fd.mojoPerCoin;
    	Balance b = new Balance((double)balance/(double)f.fd.mojoPerCoin);
    	f.updateBalance(b);
	}
	
	public static void updateStatsForced() {
		ForkFarmer.LOG.add("alltheblocks.net chain stats");
        try {
        	String jsonResponse = HttpUtil.request("https://api.alltheblocks.net/atb/blockchain/settings-and-stats").body();
        	JsonArray ja = (JsonArray) Jsoner.deserialize(jsonResponse);
        	
        	ForkFarmer.LOG.add("ATB received info for " + ja.size() + "chains");
        	ja.forEach(item -> {
        	    JsonObject jo = (JsonObject) item;
        	    String symbol = (String)jo.get("coinPrefix");
        	    
        	    JsonObject stats = (JsonObject) jo.get("stats");
        	    
        	    NetSpace ns = new NetSpace(((BigDecimal)(stats.get("netspaceBytes"))));
        	    long peakHeight = ((BigDecimal)stats.get("peakHeight")).longValue();
        	    long peakAge = ((BigDecimal)stats.get("peakAgeSeconds")).longValue();
        	    
        	    ForkData.getBySymbol(symbol.toLowerCase()).ifPresent(fd -> {
        	    	fd.peakHeight = peakHeight;
        	    	fd.peakAge = peakAge;
        	    	fd.netspace = ns;
        	    	
        	    	if (0 == peakHeight)
        	    		fd.atbIcon = null;
        	    	else
        	    		fd.atbIcon = peakAge < 600 ? Ico.ATB_G : Ico.ATB_R;
        	    });
        	});
        	
        } catch (Exception e) {
        	e.printStackTrace();
		} 
        ForkView.update();
	}
	
	public static List<String> getPeers(String pathName) {
		List<String> peerList = new ArrayList<>();
		try {
        	String jsonResponse = HttpUtil.request("https://api.alltheblocks.net/atb/peer/recent?amount=6&excludeV4=false&excludeV6=true").body();
        	JsonArray ja = (JsonArray) Jsoner.deserialize(jsonResponse);
        	
        	ja.stream().map(o -> (JsonObject)o).filter(jo -> ((String)jo.get("pathName")).equals(pathName)).forEach(item -> {
        	    JsonObject jo = (JsonObject) item;
        	    JsonArray peerArray = (JsonArray) jo.get("peers");
        	    
        	    peerArray.stream().map(o -> (JsonObject)o).forEach(pa -> {
        	    	peerList.add((String)pa.get("host") + ":" + (BigDecimal)pa.get("port"));
        	    });
        	    
        	});
        } catch (Exception e) {
        	e.printStackTrace();
		} 	

		return peerList;
	}

	public static void browseTX(String atbPath, String address) {
		if (null != atbPath)
			Util.openLink("https://alltheblocks.net/" + atbPath + "/address/" + address);
	}
}

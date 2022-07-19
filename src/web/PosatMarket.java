package web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;

import util.json.JsonArray;
import util.json.Jsoner;

public class PosatMarket {
	private static LocalDateTime lastUpdate;
	private static int UPDATE_RATE_SEC = 60*60;
	
	// Interacts with xchforks.com API. Thanks!
	public static void update() {
		if (null == lastUpdate)
			lastUpdate = LocalDateTime.now();
		else if (Duration.between(lastUpdate, LocalDateTime.now()).getSeconds() < UPDATE_RATE_SEC)
			return; // too early to update
		
		updateForced();
	}

	private static void updateForced() {
		
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();

			HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://mrkt.posat.io/api/prices/v2"))
	                .timeout(Duration.ofSeconds(5))
	                .build();
	
			HttpResponse<String> response;
	        
     		response = client.send(request, HttpResponse.BodyHandlers.ofString());
     	
     		String jsonResponse = response.body();
     	
     		JsonArray jsonArray = (JsonArray) Jsoner.deserialize(jsonResponse);
     		
     		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	


}

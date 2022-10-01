package web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import cats.Cat;
import util.json.JsonObject;
import util.json.Jsoner;

public class TailDatabase {
	
	public static void queryAsset(Cat c) {
		try {
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();

			HttpRequest request = HttpRequest.newBuilder()
		            .uri(URI.create("https://api.taildatabase.com/enterprise/tail/" + c.assetID))
		            .header("Content-Type", "application/json")
		            .header("x-api-version", "2")
		            .build();
	
			HttpResponse<String> response;
	      
			response = client.send(request,HttpResponse.BodyHandlers.ofString());

			String jsonResponse = response.body();
			
			JsonObject jo = (JsonObject) Jsoner.deserialize(jsonResponse);
			
			c.update(jo);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}

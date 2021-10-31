package web;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import forks.Fork;
import forks.ForkView;
import logging.LogView;

public class Github {
	private static LocalDateTime lastUpdate;
	private static int UPDATE_RATE_SEC = 60*65;
	
	public static void getVersion(List<Fork> list) {
		if (null == lastUpdate)
			lastUpdate = LocalDateTime.now();
		else if (Duration.between(lastUpdate, LocalDateTime.now()).getSeconds() < UPDATE_RATE_SEC)
			return; // too early to update
		getVersionForced(list);
	}
	
	public static void getVersionForced(List<Fork> list) {

		LogView.add("Github.com pulling version information");
		for (Fork f : list) {
			if (f.xchfSupport)
				continue;
			
			if (null == f.fd.gitPath)
				continue;
			
			HttpClient client = HttpClient.newBuilder()
					.connectTimeout(Duration.ofSeconds(5))
					.build();
			
			HttpRequest request = HttpRequest.newBuilder()
	                .uri(URI.create("https://api.github.com/repos/" + f.fd.gitPath + "/releases/latest"))
	                .timeout(Duration.ofSeconds(5))
	                .build();
			
			HttpResponse<String> response;
			
	        
	        try {
	        	response = client.send(request,
				        HttpResponse.BodyHandlers.ofString());
	        	
	        	String jsonResponse = response.body();
	        	
	        	JSONParser parser = new JSONParser();
	        	JSONObject jo = (JSONObject) parser.parse(jsonResponse);
	        	
	        	String tagName = (String) jo.get("tag_name");
	        	String releasedOn = (String) jo.get("published_at");
	        	
	        	if (null != tagName && null != releasedOn) {
	        		f.latestVersion = tagName;
	        		f.published = releasedOn;
	        		ForkView.update(f);
	        	}
	        	
	        } catch (Exception e) {
	        	e.printStackTrace();
			} 
		}
			
			
		}
}
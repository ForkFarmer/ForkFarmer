package web;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import util.json.JsonException;

public class HttpUtil {
	
	
	public static HttpResponse<String> request (String url) throws IOException, InterruptedException {
		HttpClient client = HttpClient.newBuilder()
				.connectTimeout(Duration.ofSeconds(5))
				.build();
		
		HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(5))
                .build();
		
		return client.send(request, HttpResponse.BodyHandlers.ofString());
	}
	
	public static JSObj requestJSO(String url) throws IOException, InterruptedException, JsonException {
		return new JSObj(request(url));
	}
	
}

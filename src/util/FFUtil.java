package util;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class FFUtil {
	
	public static Map<String,Double> getPrices() {
		Map<String,Double> priceMap = new HashMap<>();
		
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
		return priceMap;
	}

}

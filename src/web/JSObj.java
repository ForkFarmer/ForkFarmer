package web;

import java.net.http.HttpResponse;

import util.json.JsonException;
import util.json.JsonObject;
import util.json.Jsoner;

public class JSObj {

	JsonObject jo;
	
	public JSObj(HttpResponse<String> response) throws JsonException {
		String jsonResponse = response.body();
		jo = (JsonObject) Jsoner.deserialize(jsonResponse);
	}

	public String getStr(String key) {
		return (String) jo.get(key);
	}

}

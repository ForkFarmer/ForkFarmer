package cats;

import types.Balance;
import util.json.JsonObject;

public class Cat {
	public String assetID;
	public String name;
	public String code;

	public String web;
	public String discord;
	public String twitter;
	public double balance;
	public double price;
	public Balance equity;
	
	public String chain;
	public int wid;
	
	
	public Cat(JsonObject jo) {
		try {
			name = ((String) jo.get("name"));
			chain = ((String) jo.get("chain"));
			assetID = ((String) jo.get("id"));
			web = ((String) jo.get("web"));
			discord = ((String) jo.get("discord"));
			twitter = ((String) jo.get("twitter"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void updateEquity() {
		equity = new Balance(balance * price);
	}


	public Cat(int wid, String name, String assetID) {
		this.wid = wid;
		this.name = name;
		this.assetID = assetID;
	}

	public Cat(String assetID) {
		this.assetID = assetID;
		name = "?";
	}


	public String getWID() {
		return (0 != wid) ? Integer.toString(wid) : "";
	}

	public void update(JsonObject jo) {
		name = (String) jo.get("name");
		code = (String) jo.get("code");
		String s = (String) jo.get("website_url");
		if (null != s && !s.equals("string"))
			web = s;
		s = (String) jo.get("discord_url");
		if (null != s && !s.equals("string"))
			discord = s;
		s = (String) jo.get("twitter_url");
		if (null != s && !s.equals("string"))
			twitter = s;
		
		JsonObject joHG = (JsonObject) jo.get("hashgreen");
		if (null != joHG) {
			String priceStr = (String) joHG.get("price");
			if (null != priceStr && !priceStr.equals("null")) {
				price = Double.parseDouble(priceStr);
			}
		}
		updateEquity();
	}
}

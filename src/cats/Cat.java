package cats;

import util.json.JsonObject;

public class Cat {
	public String name;
	public String chain;
	public String id;
	public String web;
	public String discord;
	public double balance;
	
	
	public Cat(JsonObject jo) {
		try {
			name = ((String) jo.get("name"));
			chain = ((String) jo.get("chain"));
			id = ((String) jo.get("id"));
			web = ((String) jo.get("web"));
			discord = ((String) jo.get("discord"));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}

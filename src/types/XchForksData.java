package types;

import org.json.simple.JSONObject;

public class XchForksData {
	public String name;
	public String symbol;
	public double price;
	public long height;
	public long totalFarmed;
	public String latestVersion;
	public String published;

	public XchForksData(JSONObject jo) {
		name = (String)jo.get("name");
		symbol = (String)jo.get("symbol");
		
		String priceStr = (String)jo.get("price");
		price =  (null != priceStr && !priceStr.equals("null")) ? Double.parseDouble(priceStr) : -1;
		latestVersion = (String)jo.get("latestVersion"); 
		published = (String)jo.get("published");
	}


	public XchForksData() {
		
	}
}

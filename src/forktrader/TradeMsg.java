package forktrader;

import java.io.DataInputStream;
import java.io.IOException;

public enum TradeMsg {
	ORDER, PAIRS, DELETE, TRADE_REQEST, TRADE_ACCEPT;
	
	public static TradeMsg readType(DataInputStream dis) throws IOException {
		return TradeMsg.values()[dis.readInt()];
	}
	
}

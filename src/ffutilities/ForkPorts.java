package ffutilities;

public class ForkPorts {
	public int daemon;
	public int farmer;
	public int farmer_rpc;
	public int fullnode;
	public int fullnode_rpc;
	public int harvester;
	public int harvester_rpc;
	public int wallet;
	public int wallet_rpc;
	
	
	public boolean anyMatch(int port) {
		if (port == daemon)
			return true;
		if (port == farmer)
			return true;
		if (port == farmer_rpc)
			return true;
		if (port == fullnode)
			return true;
		if (port == harvester)
			return true;
		if (port == harvester_rpc)
			return true;
		if (port == wallet)
			return true;
		if (port == wallet_rpc)
			return true;
		return false;
	}
	
}

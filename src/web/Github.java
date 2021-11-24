package web;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import forks.Fork;
import forks.ForkView;
import main.ForkFarmer;

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

		ForkFarmer.LOG.add("Github.com pulling version information");
		for (Fork f : list) {
			if (f.xchfSupport)
				continue;
			
			if (null == f.fd.gitPath)
				continue;
	        
	        try {
	        	String requestPath = "https://api.github.com/repos/" + f.fd.gitPath + "/releases/latest";
	        	ForkFarmer.LOG.add("Github: " + requestPath);
	        	
	        	JSObj jo = HttpUtil.requestJSO(requestPath);
	        	
	        	String tagName = jo.getStr("tag_name"); 
	        	String releasedOn = jo.getStr("published_at");
	        	
	        	if (null != tagName && null != releasedOn) {
	        		f.latestVersion = tagName;
	        		f.published = releasedOn;
	        		ForkView.update(f);
	        	}
	        	
	        } catch (Exception e) {
	        	e.printStackTrace();
			} 
		}
			
		ForkFarmer.LOG.add("Done Github.com pulling version information");	
	}
		
}
package util.process;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProcessThread {
	ByteArrayOutputStream oStream =  new ByteArrayOutputStream();
	InputStreamConsumer isc;
	Process p;
	long pid=0;
	
	public ProcessThread(String... pArgs) throws IOException {
		ProcessBuilder pb = new ProcessBuilder(pArgs);
	
		pb.redirectError();
		p = pb.start();
		
		isc = new InputStreamConsumer(p.getInputStream(), oStream);
		isc.start();
	}
	
	public long getPid() {
		return p.pid();
	}
	
	public String getResults() {
		try {
			p.waitFor();
			isc.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		return oStream.toString();		
	}
	
}

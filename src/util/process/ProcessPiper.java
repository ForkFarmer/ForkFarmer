package util.process;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

public class ProcessPiper {
	
	public static String run(String... pArgs) {
		 ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		 
		  try {
	            ProcessBuilder pb = new ProcessBuilder(pArgs);
	            pb.redirectError();
	            Process p = pb.start();
	           // long pid = p.pid();
	         
	            InputStreamConsumer isc = new InputStreamConsumer(p.getInputStream(), baos);
	                isc.start();
	            
	            OutputStream stdin = p.getOutputStream();
	            PrintWriter pw = new PrintWriter(stdin);
	            pw.println("1");
	            for (int i = 0; i < 20; i++)
	            	pw.println("c");
	            pw.close();
	            
	            p.waitFor(5, TimeUnit.SECONDS);
          
	            isc.join();
	            
	           
	            return baos.toString();
	        } catch (IOException | InterruptedException exp) {
	        	exp.printStackTrace();
	        }
		  return null;
	}
	
	/*
	public static String run_get_wallet(String... pArgs) {
		 ByteArrayOutputStream baos =  new ByteArrayOutputStream();
		 
		  try {
	            ProcessBuilder pb = new ProcessBuilder(pArgs);
	            pb.redirectError();
	            Process p = pb.start();
	           // long pid = p.pid();
	         
	            InputStreamConsumer isc = new InputStreamConsumer(p.getInputStream(), baos);
	                isc.start();
	            
	            OutputStream stdin = p.getOutputStream();
	            PrintWriter pw = new PrintWriter(stdin);
            	pw.println("2");
	            pw.close();
	            
	            p.waitFor(5, TimeUnit.SECONDS);
         
	            isc.join();
	           
	            return baos.toString();
	        } catch (IOException | InterruptedException exp) {
	        	exp.printStackTrace();
	        }
		  return null;
	}
	*/

}

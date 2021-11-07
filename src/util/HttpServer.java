package util;
import java.awt.Component;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

import main.ForkFarmer;

public class HttpServer {
	public static ServerSocket serverSocket; 
	public static boolean isRunning;
	
	public static void start(int port) {
	    try {
			serverSocket = new ServerSocket(port);
			isRunning = true;
			new Thread(HttpServer::serverThread).start();
		} catch (IOException e) {
			ForkFarmer.showMsg("Server Port Error", "Could not bind to port " + port);
		}
	}
	
	public static void serverThread() {
		// repeatedly wait for connections, and process
		OutputStream os = null;
		BufferedWriter out = null;
		BufferedReader in = null;
		Socket clientSocket = null;
	    while (isRunning) {
	        
	    	try {
		    	clientSocket = serverSocket.accept();
	
		        in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		        
		        os = clientSocket.getOutputStream();
		        out = new BufferedWriter(new OutputStreamWriter(os));
	
		        String s;
		        while ((s = in.readLine()) != null) {
		            //System.out.println(s);
		            if (s.isEmpty()) {
		                break;
		            }
		        }
	
		        out.write("HTTP/1.0 200 OK\r\n");
		        out.write("Date: Fri, 31 Dec 1999 23:59:59 GMT\r\n");
		        out.write("Server: ForkFarmer\r\n");
		        out.write("Content-Type: Image/jpeg\r\n");
	//	        out.write("Content-Length: 59\r\n");
		        out.write("\r\n");
		        out.flush();
		        
				Component c = ForkFarmer.FRAME;
				BufferedImage img = new BufferedImage(c.getWidth(), c.getHeight(), BufferedImage.TYPE_INT_RGB);
				c.paint(img.getGraphics());
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				ImageIO.write(img, "jpg", baos);
				os.write(baos.toByteArray());
			
				Util.closeQuietly(out);
				Util.closeQuietly(os);
		    	Util.closeQuietly(in);
		    	Util.closeQuietly(clientSocket);
	    	} catch (Exception e) {
		    	
		    }
	    	Util.closeQuietly(out);
	    	Util.closeQuietly(os);
	    	Util.closeQuietly(in);
	    	Util.closeQuietly(clientSocket);
	    }
	    
	}
	
	public static void stop() {
		Util.closeQuietly(serverSocket);
		isRunning = false;
	}
	
}

package util.process;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamConsumer extends Thread {
		
    	private byte[] buf = new byte[8000];
        private BufferedInputStream is;
        private BufferedOutputStream os;

        public InputStreamConsumer(InputStream is,OutputStream os) {
            this.is = new BufferedInputStream(is);
            this.os = new BufferedOutputStream(os);
        }

        @Override
        public void run() {
        	
            try {
                int readLen = -1;
                while ((readLen = is.read(buf)) != -1) {
                	//System.out.println("Read: " + buf.length);
               	   	os.write(buf, 0, readLen);
                }
                os.close();
            } catch (IOException exp) {
                exp.printStackTrace();
            }

        }

    }
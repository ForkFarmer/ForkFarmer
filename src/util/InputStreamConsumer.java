package util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class InputStreamConsumer extends Thread {
    	private byte[] buf = new byte[128];
        private BufferedInputStream is;
        private BufferedOutputStream os;

        public InputStreamConsumer(InputStream is,OutputStream os) {
            this.is = new BufferedInputStream(is);
            this.os = new BufferedOutputStream(os);
        }

        @Override
        public void run() {
        	
            try {
                int value = -1;
                while ((value = is.read(buf)) != -1)
                	os.write(buf, 0, value);
                os.close();
            } catch (IOException exp) {
                exp.printStackTrace();
            }

        }

    }
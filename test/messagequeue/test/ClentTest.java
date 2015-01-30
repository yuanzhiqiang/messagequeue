package messagequeue.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class ClentTest {

	
	public static void main(String[] args) throws Exception{
		Socket s = new Socket("localhost", 8999);
		OutputStream out = s.getOutputStream(); 
		InputStream in = s.getInputStream();
		String msg = "Hello:3";
		ByteBuffer buffer = ByteBuffer.allocate(1024*50);
		buffer.putInt(1);
		buffer.putLong(4l);
		buffer.putLong(5l);
		buffer.putInt(msg.getBytes().length);
		buffer.put(msg.getBytes());
		buffer.flip();
		byte[] dst = new byte[buffer.limit()];
		buffer.get(dst);
		out.write(dst);
		while(true){
			byte[] b = new byte[1024*5];
			int count = in.read(b);
			byte[] receivemsg = new byte[count - 24];
			System.arraycopy(b , 24, receivemsg, 0, receivemsg.length);
			System.out.println(new String(receivemsg));
		}
	}
	
	
	
}

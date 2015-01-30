package messagequeue.test;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class P2PClentTest {

	
	public static void main(String[] args) throws Exception{
		Socket s = new Socket("localhost", 8999);
		OutputStream out = s.getOutputStream(); 
		InputStream in = s.getInputStream();
		String msg = "Hello:2";
		ByteBuffer buffer = ByteBuffer.allocate(1024*50);
		buffer.putInt(1);
		buffer.putLong(1);
		buffer.putLong(3l);
		buffer.putLong(2l);
		buffer.putInt(msg.getBytes().length);
		buffer.put(msg.getBytes());
		buffer.flip();
		byte[] dst = new byte[buffer.limit()];
		buffer.get(dst);
		out.write(dst);
		while(true){
			byte[] b = new byte[1024*5];
			int count = in.read(b);
			byte[] receivemsg = new byte[count - 20];
			System.arraycopy(b , 20, receivemsg, 0, receivemsg.length);
			System.out.println(new String(receivemsg));
		}
	}
	
	
	
}

package com.messagequeque.server;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import com.messageq.log.Log;
import com.messageq.network.NIOServerHandler;
import com.messageq.network.PacketLine.segment;
import com.messageq.util.ServerUtil;

public class MQServer {

	static HashMap<Long, SelectionKey> map = new HashMap<Long, SelectionKey>();
	static HashMap<Long, LinkedBlockingQueue<segment>> receivemap = new HashMap<Long, LinkedBlockingQueue<segment>>();
	static ExecutorService service = Executors.newFixedThreadPool(100);
	int SERVER_PORT = 8999;
	String SERVER_IP = "127.0.0.1";
	int MAX_CONNECT = 10;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Log ml = null;
		Thread.currentThread().setName("main");
		try {
			ml = new Log();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		NIOServerHandler server = ServerUtil.startServerHandler("" + 8999, ml);
		Thread t = new Thread(server);
		t.setName("serverhandler");
		t.start();
		segment se = null;
		MessageQueue.setServer(server, map);

		while (true) {
			while ((se = server.getNewSegement()) != null) {

				long localuser = se.p.getSender();
				long remoteuser = se.p.getreceiver();
				System.out.println("receive " + localuser + ":"
						+ new String(se.p.getArgs()));

				map.put(localuser, se.key);
				LinkedBlockingQueue<segment> queue = receivemap.get(remoteuser);
				LinkedBlockingQueue<segment> sendQueue = receivemap
						.get(localuser);
				if (sendQueue != null) {
					synchronized (sendQueue) {
						sendQueue.notify();
					}
				}
				if (queue == null) {
					queue = new LinkedBlockingQueue<segment>();
					MessageQueue messagequeue = new MessageQueue(remoteuser,
							queue);
					receivemap.put(remoteuser, queue);
					service.submit(messagequeue);
				}
				synchronized (queue) {
					queue.add(se);
					queue.notify();
				}

				System.out.println("user:" + remoteuser
						+ "'s queue received msg from " + localuser);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}

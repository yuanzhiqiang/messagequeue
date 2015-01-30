package com.messagequeque.server;

import java.nio.channels.SelectionKey;
import java.util.HashMap;
import java.util.concurrent.LinkedBlockingQueue;

import com.messageq.BasicMessage.PtoPPacket;
import com.messageq.network.NIOServerHandler;
import com.messageq.network.PacketLine.segment;

public class MessageQueue implements Runnable {
	private final long user;
	private static NIOServerHandler server;
	private static HashMap<Long, SelectionKey> map;
	private final LinkedBlockingQueue<segment> receive;

	public MessageQueue(long user, LinkedBlockingQueue<segment> receive) {
		this.user = user;
		this.receive = receive;
	}

	public static void setServer(NIOServerHandler server,
			HashMap<Long, SelectionKey> map) {
		MessageQueue.server = server;
		MessageQueue.map = map;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("messagequeue");
		while (true) {
			try {
				synchronized (receive) {
					if (receive.isEmpty()) {
						System.out.println("receive is empty,wait...");
						receive.wait();
					} else {
						System.out.println("receive is not empty,working...");
					}
					segment se = receive.poll();
					long receiver = se.p.getreceiver();
					long sender = se.p.getSender();
					SelectionKey key = map.get(receiver);

					if (key != null) {
						System.out.println("send:" + new String(se.p.getArgs())
								+ " to " + sender);
						server.pushWriteSegement(key, se.p);
					} else {
						receive.add(se);
						receive.wait();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}

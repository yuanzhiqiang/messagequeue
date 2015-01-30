package com.messageq.network;

import java.nio.channels.SelectionKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import com.messageq.BasicMessage.MessagePacket;
import com.messageq.BasicMessage.PtoPPacket;

public class PacketLine {
	private BlockingQueue<PacketNode> queue = null;

	public PacketLine() {
		queue = new ArrayBlockingQueue<PacketNode>(1000);
	}

	public class PacketNode {
		SelectionKey key = null;
		MessagePacket p = null;
		PacketNode next = null;

		PacketNode(SelectionKey key, MessagePacket p) {
			this.key = key;
			this.p = p;
		}
	}

	public class segment {
		public segment(SelectionKey key, MessagePacket p) {
			this.key = key;
			this.p = p;
		}

		public SelectionKey key = null;
		public MessagePacket p = null;
	}

	private boolean addNode(PacketNode node) {
		return queue.offer(node);
	}

	public boolean addNode(SelectionKey key, MessagePacket p) {
		return addNode(new PacketNode(key, p));
	}

	public synchronized segment popNode() {
		PacketNode head = queue.poll();
		if (head == null) {
			return null;
		}
		SelectionKey key = head.key;
		MessagePacket p = head.p;
		return new segment(key, p);
	}

	public boolean hasNext() {
		return !queue.isEmpty();
	}

	/*
	 * remove all node which key is this key
	 */
	public synchronized void removeNode(SelectionKey key) {
		Iterator<PacketNode> i = queue.iterator();
		Collection<PacketNode> removed = new ArrayList<PacketNode>();
		PacketNode tmp = null;
		while (i.hasNext()) {
			tmp = i.next();
			if (tmp.key == key) {
				removed.add(tmp);
				continue;
			}
		}
		queue.removeAll(removed);
	}

	/**
	 * pop the first packet of the key
	 * 
	 * @param key
	 * @return
	 */
	public synchronized MessagePacket popNode(SelectionKey key) {
		Iterator<PacketNode> i = queue.iterator();
		PacketNode tmp = null;
		while (i.hasNext()) {
			tmp = i.next();
			if (tmp.key == key) {
				queue.remove(tmp);
				return tmp.p;
			}
		}
		return null;
	}

	public int remain() {
		return queue.size();
	}
}

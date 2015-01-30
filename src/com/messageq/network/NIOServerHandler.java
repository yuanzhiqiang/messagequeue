package com.messageq.network;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.messageq.BasicMessage.MessageConstans;
import com.messageq.BasicMessage.MessagePacket;
import com.messageq.BasicMessage.PtoPPacket;
import com.messageq.BasicMessage.PubPacket;
import com.messageq.BasicMessage.SubPacket;
import com.messageq.log.Log;
import com.messageq.network.PacketLine.segment;
import com.messageq.util.SystemUtil;
import com.messageq.util.TimeCounter;

public class NIOServerHandler implements INIOHandler, Runnable {
	PacketLine pipeline = null;
	PacketLine waitWritePipeLine = null;
	Selector selector = null;
	private SelectionKeyManager manager = null;
	private SelectionKey inprocesskey = null;
	private ByteBuffer buffer = null;
	private int magic = 0;
	private long type = 0;
	private long sender = 0;
	private long receiver = 0;
	private long user = 0;
	private int topic = 0;
	private int argsize = 0;
	private byte[] args = null;
	private MessagePacket p = null;
	private TimeCounter tc = null;
	private Log ml = null;
	private int port = 0;
	ServerSocketChannel serverchannel = null;

	public NIOServerHandler(int port, Log ml, SelectionKeyManager manager) {
		this.port = port;
		this.manager = manager;
		if (ml != null) {
			this.ml = ml;
		} else {
			try {
				ml = new Log();
				System.out.println("ml is" + (ml == null));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		buffer = ByteBuffer.allocate(1024 * 1024 * 4);
		buffer.clear();
		tc = new TimeCounter(10000);
		pipeline = new PacketLine();
		waitWritePipeLine = new PacketLine();
	}

	public NIOServerHandler(int port, Log ml) {
		this(port, ml, new SelectionKeyManager());
	}

	@Override
	public void processConnect() throws IOException {
	}

	@Override
	public void processRead() throws IOException {
		SocketChannel channel = (SocketChannel) inprocesskey.channel();
		try {
			if (receive(channel) <= 0) {
				System.out.println("read node from "
						+ channel.socket().getInetAddress().getHostAddress()
						+ " fail,cancel this key!");
				manager.addCancelInterest(inprocesskey);
				return;
			}
		} catch (IOException e) {
			System.out.println("IO Exception:read node from "
					+ channel.socket().getInetAddress().getHostAddress()
					+ " fail,exclude the node!");
			manager.addCancelInterest(inprocesskey);
			return;
		}
		if (type == MessageConstans.P2P)
			p = new PtoPPacket(sender, receiver, args);
		else if(type == MessageConstans.SUB)
			p = new SubPacket(user, args);
		else
			p = new PubPacket(user, args);
		// add until true
		while (!pipeline.addNode(inprocesskey, p)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		argsize = 0;
	}

	// }

	@Override
	public void processWrite() throws IOException {
		while (waitWritePipeLine.hasNext()) {
			segment se = waitWritePipeLine.popNode();
			if (se != null) {
				SelectionKey sk = se.key;
				MessagePacket p = se.p;
				SocketChannel channel = (SocketChannel) sk.channel();
				ByteBuffer buffer = p.getBuffer();
				if (buffer != null) {
					if (channel.write((ByteBuffer) buffer.flip()) < buffer
							.capacity()) {
						ml.warn("server send little bytes than expected!");
					}
					System.out
							.println("[NIOServerHandler]write a segment to client:"
									+ SystemUtil.byteToString(p.getArgs()));
				}
			}

		}
	}

	// }

	@Override
	public void processError(Exception e) {
	}

	/**
	 * remove all data want to send to this key
	 * 
	 * @param key
	 */
	public void removeWriteKey(SelectionKey key) {
		waitWritePipeLine.removeNode(key);
	}

	private int receive(SocketChannel channel) throws IOException {
		buffer.clear();
		buffer.limit(12);
		channel.read(buffer);
		if (buffer.position() != buffer.limit()) {
			buffer.clear();
			return 0;
		}
		buffer.flip();
		magic = buffer.getInt();
		if (magic != MessageConstans.MAGIC) {
			return 0;
		}
		type = buffer.getLong();
		buffer.clear();
		if(type == MessageConstans.P2P){
			buffer.limit(24);
			channel.read(buffer);
			sender = buffer.getLong();
			receiver = buffer.getLong();
			argsize = buffer.getInt();
		}else if(type == MessageConstans.PUB){
			user = buffer.getLong();
			topic = buffer.getInt();
			argsize = buffer.getInt();
		}else if(type == MessageConstans.SUB){
			user = buffer.getLong();
			topic = buffer.getInt();
			argsize = 0;
		}else{
			return 0;
		}
		// over the packet size;
		if (argsize > buffer.capacity() - 24) {
			ml.error("packet args size is over the packet size limit"
					+ ",and the limit" + "is " + (buffer.capacity() - 24) + ","
					+ "but the packet has " + argsize);
			return -1;
		}
		buffer.clear();
		if (argsize == 0) {
			return 24;
		} else {
			buffer.limit(argsize);
			tc.timeRefresh();
			while (buffer.remaining() > 0) {
				if (tc.isTimeout()) {
					ml.error("have one  packet read error...please check the link?");
					buffer.clear();
					return 0;
				}
				channel.read(buffer);
			}
			buffer.flip();
			args = new byte[argsize];
			buffer.get(args, 0, argsize);
			buffer.clear();
		}
		return argsize + 24;
	}

	public void init() {
		Thread t = new Thread() {
			public void run() {
				while (true)
					try {
						while (!waitWritePipeLine.hasNext()) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						processWrite();
					} catch (IOException e) {
						e.printStackTrace();
					}
			}
		};
		t.setDaemon(true);
		t.setName("processwrite");
		t.start();
	}

	@Override
	public void run() {

		init();
		// 1.open selector
		try {
			selector = Selector.open();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		// 2.start server
		try {
			serverchannel = startServer(port, selector);
		} catch (ClosedChannelException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		int signals = 0;
		while (true) {
			CheckInterest();
			try {
				signals = selector.select(2000);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			if (signals == 0) {
				continue;
			}
			Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			SocketChannel channel = null;
			while (it.hasNext()) {
				SelectionKey key = it.next();
				if (!key.isValid()) {
					it.remove();
					continue;
				}
				if (key.isAcceptable()) {
					System.out.println("[NIOServerHandler]accept a connection");
					try {
						channel = serverchannel.accept();
						System.out.println("ml is" + (ml == null));
						ml.log("accept a new connection from "
								+ channel.socket().getInetAddress()
										.getHostAddress());
					} catch (IOException e) {
						e.printStackTrace();
					}
					registerChannel(selector, channel, SelectionKey.OP_READ);
					it.remove();
					continue;
				}
				it.remove();
				execute(key);
			}

		}
	}

	private void execute(SelectionKey key) {
		if (key.isReadable()) {
			System.out.println("[NIOServerhandler]isreadable!!!!!");
			inprocesskey = key;
			try {
				processRead();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private SelectionKey registerChannel(Selector selector,
			SocketChannel channel, int opRead) {

		if (channel == null) {
			return null;
		}
		try {
			channel.configureBlocking(false);
			return channel.register(selector, opRead);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

	}

	/**
	 * check all the registered need read key and need cancel key;
	 */
	private void CheckInterest() {
		while (true) {
			SelectionKey key = manager.popNeedCancelKey();
			if (key != null) {
				ml.log("add node from "
						+ ((SocketChannel) key.channel()).socket()
								.getInetAddress().getHostAddress()
						+ " to delete node");
				manager.deletenode(key);
				key.cancel();
			} else {
				break;
			}
		}
	}

	private ServerSocketChannel startServer(int port, Selector selector)
			throws IOException, ClosedChannelException {
		ServerSocketChannel serverChannel = ServerSocketChannel.open();
		serverChannel.configureBlocking(false);
		ServerSocket serverSocket = serverChannel.socket();
		serverSocket.bind(new InetSocketAddress(port));
		serverChannel.register(selector, SelectionKey.OP_ACCEPT);
		return serverChannel;
	}

	public segment getNewSegement() {
		return pipeline.popNode();
	}

	public MessagePacket getChannelRecv(SelectionKey key) {
		return pipeline.popNode(key);
	}

	public void pushWriteSegement(SelectionKey key, MessagePacket p) {
		while (!waitWritePipeLine.addNode(key, p)) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	public SelectionKeyManager getNodeList() {
		return manager;
	}

	public void flush() {
		try {
			processWrite();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			selector.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}

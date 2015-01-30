package com.messageq.BasicMessage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public abstract class MessagePacket {

	private int magic = 0;
	private long type = 0;

	public MessagePacket(ByteBuffer buffer) {
		buffer.flip();
		magic = buffer.getInt();
	}

	MessagePacket(int magic, long type) {
		this.magic = magic;
		this.type = type;
	}

	public MessagePacket(long sender, long receiver, String args) {
		if (args == null) {

			this.magic = MessageConstans.MAGIC;
			return;
		}
		this.magic = MessageConstans.MAGIC;
	}

	public ByteBuffer getBuffer() {
		ByteBuffer buffer = null;
		return buffer;
	}

	public static MessagePacket getOnePacket(ByteBuffer buffer)
			throws IOException {
//		if (buffer.remaining() < 24) {
//			return null;
//		}
//		int magic = buffer.getInt();
//		if (magic != BasicMessage.MAGIC) {
//			throw new IOException("bad packet");
//		}
//		long type = buffer.getLong();
//		if (type == BasicMessage.P2P) {
//			return new PtoPPacket(magic, type, buffer);
//		} else if (type == BasicMessage.PUB) {
//			return new PubPacket(magic, type, buffer);
//		} else if (type == BasicMessage.SUB) {
//			return new SubPacket(magic, type, buffer);
//		}
		return null;
	}
	
	public long getSender() {
		return -1;
	}

	public long getreceiver() {
		return -1;
	}

	public abstract byte[] getArgs();
}

package com.messageq.BasicMessage;

import java.nio.ByteBuffer;

import com.messageq.log.Log;

public class PtoPPacket extends MessagePacket {

	public byte[] getArgs() {
		return args;
	}

	public void setArgs(byte[] args) {
		this.args = args;
	}

	public long getSender() {
		return sender;
	}

	public long getreceiver() {
		return receiver;
	}

	private long sender = 0;
	private long receiver = 0;
	private int argsize = 0;
	byte[] args = null;

	public PtoPPacket(long sender, long receiver, byte[] args) {
		super(MessageConstans.MAGIC, MessageConstans.P2P);
		if (args == null) {
			this.sender = sender;
			this.receiver = receiver;
			this.argsize = 0;
			return;
		}
		this.sender = sender;
		this.receiver = receiver;
		this.argsize = args.length;
		this.args = args;
	}

	public ByteBuffer getBuffer() {
		ByteBuffer buffer = null;
		if (args == null) {
			buffer = ByteBuffer.allocate(24);
			buffer.clear();
		} else {
			buffer = ByteBuffer.allocate(args.length + 24);
		}
		buffer.putLong(sender);
		buffer.putLong(receiver);
		buffer.putInt(argsize);
		if (args != null) {
			buffer.put(args);
		}
		return buffer;
	}

//	public static PtoPPacket getOnePacket(ByteBuffer buffer) {
//		if (buffer.remaining() < 24) {
//			return null;
//		}
//		int version = buffer.getInt();
//		if (version != BasicMessage.MAGIC) {
//			System.out
//					.println("the remote host's version is not compatible with us ,"
//							+ " maybe this will make no sense!");
//		}
//		long sender = buffer.getLong();
//		long receiver = buffer.getLong();
//		int argsize = buffer.getInt();
//
//		if (buffer.remaining() < argsize) {
//			buffer.rewind();
//			return null;
//		} else {
//			byte[] args = new byte[argsize];
//			buffer.get(args, 0, argsize);
//			return new PtoPPacket(sender, receiver, new String(args));
//		}
//	}

	public int size() {
		return argsize + 24;
	}
}

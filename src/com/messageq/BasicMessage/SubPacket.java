package com.messageq.BasicMessage;

import java.nio.ByteBuffer;

public class SubPacket extends MessagePacket {

	@Override
	public byte[] getArgs() {
		return args;
	}

	public void setArgs(byte[] args) {
		this.args = args;
	}

	private long user = 0;
	private int argsize = 0;
	byte[] args = null;

	public SubPacket(long user, byte[] args) {
		super(MessageConstans.MAGIC, MessageConstans.SUB);
		this.user = user;
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
		buffer.putLong(user);
		buffer.putInt(argsize);
		if (args != null) {
			buffer.put(args);
		}
		return buffer;
	}

	// public static SubPacket getOnePacket(ByteBuffer buffer) {
	// if (buffer.remaining() < 24) {
	// return null;
	// }
	// int magic = buffer.getInt();
	// if (magic != BasicMessage.MAGIC) {
	// System.out
	// .println("the remote host's version is not compatible with us ,"
	// + " maybe this will make no sense!");
	// }
	// long user = buffer.getLong();
	// int argsize = buffer.getInt();
	//
	// if (buffer.remaining() < argsize) {
	// buffer.rewind();
	// return null;
	// } else {
	// byte[] args = new byte[argsize];
	// buffer.get(args, 0, argsize);
	// return new SubPacket(user, new String(args));
	// }
	// }

	public int size() {
		return argsize + 24;
	}
}

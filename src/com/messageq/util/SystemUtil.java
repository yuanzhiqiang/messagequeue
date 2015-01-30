package com.messageq.util;

public class SystemUtil {

	private static String machineName = null;

	public static String getLogpath() {
		return "/Users/yuanzq/Desktop/work/liequ/messagequeue/";
	}

	public static String byteToString(byte[] b) {
		if (b == null)
			return "";
		return new String(b);
	}

}
package com.messageq.log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.messageq.util.SystemUtil;

public class Log{
	String className = "";
	String filePath = "";
	FileWriter fw = null;
	boolean newfile = false;
	SimpleDateFormat DateFormat = new SimpleDateFormat(
			"yyyy/MM/dd HH:mm:ss:SSS");
	Date date = new Date();

	public Log() throws Exception {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		this.className = stack[1].getClassName();
		String logpath = SystemUtil.getLogpath();
		File dir = new File(logpath);
		if (!dir.isDirectory() || !dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("cann't make log dir ,please check:"
						+ dir.getPath());
			}
		}
		File f = new File(logpath + "monitorlog");
		if (!f.exists()) {
			f.createNewFile();
			newfile = true;
		}
		fw = new FileWriter(f, true);
		if (newfile) {
			fw.write(headString() + "\n");
		}
	}

	public Log(String filePath) throws Exception {
		StackTraceElement stack[] = (new Throwable()).getStackTrace();
		this.className = stack[1].getClassName();
		File f = new File(filePath);
		String logpath = f.getParent();
		File dir = new File(logpath);
		if (!dir.isDirectory() || !dir.exists()) {
			if (!dir.mkdirs()) {
				throw new Exception("cann't make log dir ,please check:"
						+ dir.getPath());
			}
		}
		if (!f.exists()) {
			f.createNewFile();
			newfile = true;
		}
		fw = new FileWriter(f, true);
		if (newfile) {
			fw.write(headString() + "\n");
		}
	}

	public static void loglog(String message) {
		System.out.println("loglog: " + message);
	}

	public void log(String message) {
		synchronized (fw) {
			try {
				fw.write(headString() + "[message]" + message + "\n");
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fw.notify();
		}

	}

	public void logWithThreadName(String message) {
		synchronized (fw) {
			try {
				fw.write(headString() + "[message]["
						+ Thread.currentThread().getName() + "]" + message
						+ "\n");
				fw.flush();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			fw.notify();
		}

	}

	public void warn(String message) {

		try {
			fw.write(headString() + "[warn]" + message + "\n");
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void error(String message) {

		try {
			fw.write(headString() + "[error]" + message + "\n");
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String headString() {
		date.setTime(System.currentTimeMillis());
		return "[" + DateFormat.format(date) + "]" + "[" + className + "]";
	}

	public void flush() {
		try {
			fw.flush();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void close() {
		try {
			fw.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

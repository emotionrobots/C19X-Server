package org.c19x.server.session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.c19x.util.Logger;

public class AuditLog implements AutoCloseable {
	private final static String tag = AuditLog.class.getName();
	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSS");
	private PrintWriter log;

	public AuditLog(File file) {
		try {
			log = new PrintWriter(new FileOutputStream(file, true), true);
		} catch (Throwable e) {
			Logger.error(tag, "Unable to open audit log (file={})", file, e);
		}
	}

	public void logIn(String userName, boolean success) {
		log("logIn", "user=" + userName, "success=" + success);
	}

	public void logOut(Session session, boolean success) {
		log("logOut", "user=" + session.user.name, "success=" + success);
	}

	public void changePassword(String userName, boolean success) {
		log("changePassword", "user=" + userName, "success=" + success);
	}

	public synchronized void log(String event, String... values) {
		final String timestamp = simpleDateFormat.format(new Date());
		final String entry = timestamp + "\t" + event + "\t" + String.join(";", values);
		Logger.info(tag, "Log (entry={})", entry);
		log.println(entry);
		log.flush();
	}

	@Override
	public void close() {
		log.flush();
		log.close();
	}
}

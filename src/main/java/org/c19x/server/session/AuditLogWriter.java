package org.c19x.server.session;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.c19x.util.Logger;

public class AuditLogWriter implements AutoCloseable {
	private final static String tag = AuditLogWriter.class.getName();
	public final static AuditLogWriter shared = new AuditLogWriter(new File("config/audit.txt"));
	private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSSS");
	private PrintWriter log;

	public AuditLogWriter(File file) {
		try {
			log = new PrintWriter(new FileOutputStream(file, true), true);
		} catch (Throwable e) {
			Logger.error(tag, "Unable to open audit log (file={})", file, e);
		}
	}

	public void logIn(String userName, boolean success) {
		write("logIn", "user=" + userName, "success=" + success);
	}

	public void logOut(Session session, boolean success) {
		write("logOut", "user=" + session.user.name, "success=" + success);
	}

	public void changePassword(String userName, boolean success) {
		write("changePassword", "user=" + userName, "success=" + success);
	}

	private synchronized void write(String event, String... values) {
		final String timestamp = simpleDateFormat.format(new Date());
		final String entry = timestamp + "\t" + event + "\t" + String.join(";", values);
		Logger.info(tag, "{}", entry);
		log.println(entry);
		log.flush();
	}

	@Override
	public void close() throws Exception {
		log.flush();
		log.close();
	}
}

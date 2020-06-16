package org.c19x.server.session;

import java.util.Arrays;

import org.c19x.util.Logger;

public class AuditLogWriter {
	private final static String tag = AuditLogWriter.class.getName();
	public final static AuditLogWriter shared = new AuditLogWriter();

	public void logIn(String userName, boolean success) {
		write("logIn", "user=" + userName, "success=" + success);
	}

	public void logOut(Session session, boolean success) {
		write("logOut", "user=" + session.user.name, "success=" + success);
	}

	public void changePassword(String userName, boolean success) {
		write("changePassword", "user=" + userName, "success=" + success);
	}

	private void write(String event, String... values) {
		Logger.info(tag, "{} {}", event, Arrays.toString(values));
	}
}

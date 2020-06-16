package org.c19x.server.session;

public class Session {
	public final User user;
	public final String token;
	public long startTime;
	public long expiryTime;

	public Session(final User user, final String token, final long startTime, final long expiryTime) {
		super();
		this.user = user;
		this.token = token;
		this.startTime = startTime;
		this.expiryTime = expiryTime;
	}

	@Override
	public String toString() {
		return "Session [user=" + user + ", token=" + token + ", startTime=" + startTime + ", expiryTime=" + expiryTime
				+ "]";
	}
}

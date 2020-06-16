package org.c19x.server.session;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManager {
	private final static Logger logger = LoggerFactory.getLogger(SessionManager.class);
	private final static int secureRandomSeedSize = 2048;
	private final static int sessionTokenSize = 32;
	private final static int sessionIdleExpiryMillis = 30 * 60 * 1000; // 30 minutes
	private final static int sessionLoginDelayMillis = 1000;

	private final File usersFile;
	private int usersFileLastHash = 0;
	private final Map<String, Session> sessions = new HashMap<>();
	private final Map<String, User> users = new HashMap<>();
	private final Map<String, Integer> userDelays = new HashMap<>();
	private final SecureRandom secureRandom;

	public SessionManager(final File usersFile) {
		logger.debug("Seeding session manager");
		final byte[] seed = new byte[secureRandomSeedSize];
		(new SecureRandom()).nextBytes(seed);
		this.secureRandom = new SecureRandom(seed);
		logger.debug("Session manager ready");

		this.usersFile = usersFile;
	}

	private final static String sha256(String string) {
		try {
			final MessageDigest digest = MessageDigest.getInstance("SHA-256");
			final byte[] hash = digest.digest(string.getBytes(StandardCharsets.UTF_8));
			final StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			logger.error("SHA256 failed", e);
			return "";
		}
	}

	public boolean addUser(final String userName, final String password) {
		loadUsers();
		if (!users.containsKey(userName)) {
			final String entry = userName + "\t" + password + "\n";
			try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(usersFile, true))) {
				bufferedWriter.write(entry);
				bufferedWriter.close();
				usersFileLastHash = 0;
				logger.info("Add user completed successfully (name={},file={})", userName, usersFile);
				return true;
			} catch (Exception e) {
				logger.error("Add user failed due to exception (name={},file={})", userName, usersFile, e);
				return false;
			}
		} else {
			logger.error("Add user failed as user already exists (name={},file={})", userName, usersFile);
			return false;
		}
	}

	public boolean removeUser(final String userName, final String password) {
		loadUsers();
		if (users.containsKey(userName) && users.get(userName).hashOfPassword.equals(password)) {
			final File newUsersFile = new File(usersFile.getParentFile(), usersFile.getName() + ".tmp");
			boolean userWasRemoved = false;
			try (final BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(newUsersFile, true))) {
				final List<String> lines = Files.readAllLines(usersFile.toPath());
				for (final String line : lines) {
					if (!line.startsWith(userName + "\t")) {
						bufferedWriter.write(line + "\n");
					} else {
						userWasRemoved = true;
					}
				}
				bufferedWriter.close();
				FileSystem.delete(usersFile);
				FileSystem.move(newUsersFile, usersFile);
				logger.info("Remove user completed successfully (name={},file={})", userName, usersFile);
				usersFileLastHash = 0;
				return userWasRemoved;
			} catch (Exception e) {
				logger.error("Remove user failed due to exception (name={},file={})", userName, usersFile, e);
				return false;
			}
		} else {
			logger.info("Remove user failed as user does not exist (name={},file={})", userName, usersFile);
			return false;
		}
	}

	private synchronized final void loadUsers() {
		try {
			final List<String> lines = Files.readAllLines(usersFile.toPath());
			final int hash = String.join("\n", lines).hashCode();
			if (hash != usersFileLastHash) {
				logger.debug("Loading users (file={},changed=true,hash={},lastHash={})", usersFile, hash,
						usersFileLastHash);
				final Set<User> accounts = new HashSet<>();
				Files.readAllLines(usersFile.toPath()).iterator().forEachRemaining(line -> {
					if (!line.startsWith("#") && !(line.trim().isEmpty())) {
						final String[] fields = line.split("\t");
						if (fields.length >= 2) {
							final String name = fields[0];
							final String hashOfPassword = fields[1];
							final User user = new User(name, hashOfPassword);
							accounts.add(user);
							// Additional data
							if (fields.length >= 3) {
								final String permissions = fields[2];
								Arrays.asList(permissions.split(",")).stream().map(p -> p.trim().toLowerCase())
										.forEach(p -> user.permissions.add(p));
							}
						}
					}
				});
				users.clear();
				accounts.forEach(user -> users.put(user.name, user));
				logger.debug("Loaded users (file={},accounts={})", usersFile, accounts.size());
				usersFileLastHash = hash;
			} else {
				logger.debug("Load users skipped (file={},changed=false,hash={},lastHash={})", usersFile, hash,
						usersFileLastHash);
			}
		} catch (Exception e) {
			logger.error("Failed to read users file (file={})", usersFile, e);
		}
	}

	/**
	 * Log in to get a session token.
	 *
	 * @param userName
	 * @param hashOfPassword
	 * @return
	 */
	public Session logIn(final String userName, final String hashOfPassword) {
		logger.debug("Log in request (userName={},hashOfPassword={})", userName, hashOfPassword);
		loadUsers();
		final User user = users.get(userName);
		if (user != null) {
			if (user.hashOfPassword.equals(hashOfPassword)) {
				final String token = generateSessionToken(user);
				final long startTime = System.currentTimeMillis();
				final long expiryTime = startTime + sessionIdleExpiryMillis;
				final Session session = new Session(user, token, startTime, expiryTime);
				sessions.put(token, session);
				userDelays.remove(userName);
				logger.debug("Log in successful (userName={},token={})", userName, token);
				AuditLogWriter.shared.logIn(userName, true);
				return session;
			} else {
				logger.debug("Log in failed, incorrect password (userName={})", userName);
				AuditLogWriter.shared.logIn(userName, false);
				if (!userDelays.containsKey(userName)) {
					userDelays.put(userName, sessionLoginDelayMillis);
				} else {
					userDelays.put(userName, userDelays.get(userName) * 2);
				}
				try {
					logger.debug("Log in retry delay (userName={},delay={})", userName, userDelays.get(userName));
					Thread.sleep(userDelays.get(userName));
				} catch (InterruptedException e) {
				}
			}
		}
		logger.debug("Log in failed, unknown user (userName={})", userName);
		return null;
	}

	/**
	 * Log out to destroy a session token.
	 *
	 * @param token
	 * @return
	 */
	public Session logOut(final String token) {
		final Session session = sessions.remove(token);
		AuditLogWriter.shared.logOut(session, false);
		session.expiryTime = 0;
		return session;
	}

	/**
	 * Change user password.
	 *
	 * @param userName
	 * @param hashOfPassword
	 * @param hashOfNewPassword
	 * @return
	 */
	public boolean changeUserPassword(final String userName, final String hashOfPassword,
			final String hashOfNewPassword) {
		boolean result = false;
		if (removeUser(userName, hashOfPassword)) {
			result = addUser(userName, hashOfNewPassword);
		}
		AuditLogWriter.shared.changePassword(userName, result);
		return result;
	}

	/**
	 * Check if token is valid and extend expiry time
	 *
	 * @param token
	 * @return
	 */
	public Session isAuthorised(final String token) {
		final Session session = sessions.get(token);
		if (session != null) {
			final long currentTime = System.currentTimeMillis();
			if (currentTime <= session.expiryTime) {
				session.expiryTime = currentTime + sessionIdleExpiryMillis;
				return session;
			} else {
				sessions.remove(token);
				AuditLogWriter.shared.logOut(session, true);
				session.expiryTime = 0;
			}
		}
		return null;
	}

	/**
	 * Generate random session token.
	 *
	 * @param user
	 * @return
	 */
	private String generateSessionToken(final User user) {
		final StringBuilder tokenBuilder = new StringBuilder(sessionTokenSize);
		for (int i = sessionTokenSize; i-- > 0;) {
			tokenBuilder.append(secureRandom.nextInt(10));
		}
		return tokenBuilder.toString();
	}
}

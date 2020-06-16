package org.c19x.server.session;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import sherlock.util.UriUtil;

/**
 * Handler for session management.
 *
 * @author user
 */
public class SessionManagerHandler implements HttpHandler {
	private final Logger logger = LoggerFactory.getLogger(SessionManagerHandler.class);

	private final SessionManager sessionManager;

	public SessionManagerHandler() {
		this.sessionManager = Configuration.getSessionManager();
	}

	@Override
	public void handle(HttpExchange x) throws IOException {
		logger.debug("Request (uri={})", x.getRequestURI());
		final Map<String, String> query = UriUtil.getQueryParameters(x.getRequestURI());
		switch (query.getOrDefault("f", "")) {
		case "login":
			handleLogIn(x, query);
			break;
		case "logout":
			handleLogOut(x, query);
			break;
		case "change":
			handleChange(x, query);
			break;
		case "lock":
			handleLock(x, query);
			break;
		case "unlock":
			handleUnlock(x, query);
			break;
		case "refresh":
			handleRefresh(x, query);
			break;
		}
	}

	private final void handleLogIn(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String userName = query.get("u");
		final String hashOfPassword = query.get("p");
		final Session session = sessionManager.logIn(userName, hashOfPassword);
		if (session == null) {
			x.sendResponseHeaders(204, 0);
			logger.error("Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		} else {
			final JSONObject jsonResult = toJSON(session);
			final String jsonString = jsonResult.toString(2);
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			logger.debug("Response (code=200,length={})", responseBytes.length);
		}
	}

	private final void handleLogOut(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String token = query.get("token");
		final Session session = sessionManager.logOut(token);
		if (session == null) {
			x.sendResponseHeaders(204, 0);
			logger.error("Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		} else {
			final JSONObject jsonResult = toJSON(session);
			final String jsonString = jsonResult.toString(2);
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			logger.debug("Response (code=200,length={})", responseBytes.length);
		}
	}

	private final void handleLock(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String token = query.get("token");
		final Session session = sessionManager.lock(token);
		if (session == null) {
			x.sendResponseHeaders(204, 0);
			logger.error("Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		} else {
			final JSONObject jsonResult = toJSON(session);
			final String jsonString = jsonResult.toString(2);
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			logger.debug("Response (code=200,length={})", responseBytes.length);
		}
	}

	private final void handleUnlock(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String userName = query.get("u");
		final String hashOfPassword = query.get("p");
		final String token = query.get("token");
		final boolean result = sessionManager.unlock(userName, hashOfPassword, token);
		if (result) {
			final JSONObject jsonResult = new JSONObject();
			jsonResult.put("success", result);
			final String jsonString = jsonResult.toString(2);
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			logger.debug("Response (code=200,length={})", responseBytes.length);
		} else {
			x.sendResponseHeaders(204, 0);
			logger.error("Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		}
	}

	private final void handleChange(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String userName = query.get("u");
		final String hashOfPassword = query.get("p");
		final String hashOfNewPassword = query.get("n");
		final boolean result = sessionManager.changeUserPassword(userName, hashOfPassword, hashOfNewPassword);
		if (result) {
			final JSONObject jsonResult = new JSONObject();
			jsonResult.put("success", result);
			final String jsonString = jsonResult.toString(2);
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			logger.debug("Response (code=200,length={})", responseBytes.length);
		} else {
			x.sendResponseHeaders(204, 0);
			logger.error("Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		}
	}

	private final void handleRefresh(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String token = query.get("token");
		final Session session = sessionManager.isAuthorised(token);
		if (session == null) {
			x.sendResponseHeaders(204, 0);
			logger.error("Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		} else {
			final JSONObject jsonResult = toJSON(session);
			final String jsonString = jsonResult.toString(2);
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			logger.debug("Response (code=200,length={})", responseBytes.length);
		}
	}

	private final static JSONObject toJSON(final Session session) {
		final JSONObject jsonResult = new JSONObject();
		jsonResult.put("token", session.token);
		final JSONArray jsonPermissions = new JSONArray();
		session.user.permissions.forEach(p -> jsonPermissions.put(p));
		jsonResult.put("permissions", jsonPermissions);
		jsonResult.put("expiryTime", Long.toString(session.expiryTime));
		return jsonResult;
	}

}

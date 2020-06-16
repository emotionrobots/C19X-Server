package org.c19x.server.session;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.c19x.util.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

/**
 * Handler for session management.
 *
 * @author user
 */
public class SessionManagerHandler implements HttpHandler {
	private final static String tag = SessionManagerHandler.class.getName();
	private final SessionManager sessionManager;

	public SessionManagerHandler() {
		this.sessionManager = SessionManager.shared;
	}

	@Override
	public void handle(HttpExchange x) throws IOException {
		Logger.debug(tag, "Request (uri={})", x.getRequestURI());
		final Map<String, String> query = getQueryParameters(x.getRequestURI());
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
		case "refresh":
			handleRefresh(x, query);
			break;
		}
	}

	private final static Map<String, String> getQueryParameters(final URI uri) {
		final Map<String, String> parameters = new HashMap<>(2);
		for (final String p : uri.getRawQuery().split("&")) {
			final String[] keyValue = p.split("=", 2);
			if (keyValue.length == 2) {
				try {
					final String key = java.net.URLDecoder.decode(keyValue[0], "UTF-8");
					final String value = java.net.URLDecoder.decode(keyValue[1], "UTF-8");
					parameters.put(key, value);
				} catch (Throwable e) {
					Logger.warn(tag, "Failed to decode query parameter (parameter={},uri={})", p, uri.getRawQuery(), e);
				}
			} else {
				Logger.warn(tag, "Failed to parse query parameter (parameter={},uri={})", p, uri.getRawQuery());
			}
		}
		return parameters;
	}

	private final void handleLogIn(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String userName = query.get("u");
		final String hashOfPassword = query.get("p");
		final Session session = sessionManager.logIn(userName, hashOfPassword);
		if (session == null) {
			x.sendResponseHeaders(204, 0);
			Logger.error(tag, "Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		} else {
			final JSONObject jsonResult = toJSON(session);
			final String jsonString = jsonResult.toJSONString();
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			Logger.debug(tag, "Response (code=200,length={})", responseBytes.length);
		}
	}

	private final void handleLogOut(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String token = query.get("token");
		final Session session = sessionManager.logOut(token);
		if (session == null) {
			x.sendResponseHeaders(204, 0);
			Logger.error(tag, "Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		} else {
			final JSONObject jsonResult = toJSON(session);
			final String jsonString = jsonResult.toJSONString();
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			Logger.debug(tag, "Response (code=200,length={})", responseBytes.length);
		}
	}

	@SuppressWarnings("unchecked")
	private final void handleChange(HttpExchange x, Map<String, String> query) throws IOException {
		x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
		final String userName = query.get("u");
		final String hashOfPassword = query.get("p");
		final String hashOfNewPassword = query.get("n");
		final boolean result = sessionManager.changeUserPassword(userName, hashOfPassword, hashOfNewPassword);
		if (result) {
			final JSONObject jsonResult = new JSONObject();
			jsonResult.put("success", result);
			final String jsonString = jsonResult.toJSONString();
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			Logger.debug(tag, "Response (code=200,length={})", responseBytes.length);
		} else {
			x.sendResponseHeaders(204, 0);
			Logger.error(tag, "Response (code=204,length=0)");
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
			Logger.error(tag, "Response (code=204,length=0)");
			x.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
			x.sendResponseHeaders(204, 0);
		} else {
			final JSONObject jsonResult = toJSON(session);
			final String jsonString = jsonResult.toJSONString();
			final byte[] responseBytes = jsonString.getBytes();
			x.getResponseHeaders().add("Content-Type", "application/json");
			x.sendResponseHeaders(200, responseBytes.length);
			final OutputStream outputStream = x.getResponseBody();
			outputStream.write(responseBytes);
			outputStream.close();
			Logger.debug(tag, "Response (code=200,length={})", responseBytes.length);
		}
	}

	@SuppressWarnings("unchecked")
	private final static JSONObject toJSON(final Session session) {
		final JSONObject jsonResult = new JSONObject();
		jsonResult.put("token", session.token);
		final JSONArray jsonPermissions = new JSONArray();
		session.user.permissions.forEach(p -> jsonPermissions.add(p));
		jsonResult.put("permissions", jsonPermissions);
		jsonResult.put("expiryTime", Long.toString(session.expiryTime));
		return jsonResult;
	}

}

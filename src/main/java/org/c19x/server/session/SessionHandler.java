package org.c19x.server.session;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.util.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * Handler for session management.
 *
 * @author user
 */
public class SessionHandler extends AbstractHandler {
	private final static String tag = SessionHandler.class.getName();
	private final SessionManager sessionManager;

	public SessionHandler(final SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		final String function = request.getParameter("function");
		Logger.debug(tag, "Handle (function={},address={})", function, request.getRemoteAddr().hashCode());
		try {
			switch (function) {
			case "login":
				handleLogIn(request, response);
				break;
			case "logout":
				handleLogOut(request, response);
				break;
			case "change":
				handleChange(request, response);
				break;
			case "refresh":
				handleRefresh(request, response);
				break;
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			Logger.warn(tag, "Failed (function={},address={})", function, request.getRemoteAddr().hashCode(), e);
		} finally {
			baseRequest.setHandled(true);
		}

	}

	// function=login&user=userName&password=passwordHash
	private final void handleLogIn(HttpServletRequest request, HttpServletResponse response) throws IOException {
		response.addHeader("Access-Control-Allow-Origin", "*");
		response.addHeader("Access-Control-Allow-Headers", "Origin, X-Requested-With, Content-Type, Accept");
		final String userName = request.getParameter("user");
		final String hashOfPassword = request.getParameter("password");
		final Session session = sessionManager.logIn(userName, hashOfPassword);
		if (session == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			Logger.debug(tag, "Unauthorised (function=logIn,address={},userName={})",
					request.getRemoteAddr().hashCode(), userName);
		} else {
			final String json = toJSON(session).toJSONString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter printWriter = response.getWriter();
			printWriter.write(json);
			printWriter.flush();
			printWriter.close();
			Logger.debug(tag, "Success (function=logIn,address={},userName={})", request.getRemoteAddr().hashCode(),
					userName);
		}
	}

	// function=logout&token=token
	private final void handleLogOut(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String token = request.getParameter("token");
		final Session session = sessionManager.logOut(token);
		if (session == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			Logger.debug(tag, "Unauthorised (function=logOut,address={})", request.getRemoteAddr().hashCode());
		} else {
			final String json = toJSON(session).toJSONString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter printWriter = response.getWriter();
			printWriter.write(json);
			printWriter.flush();
			printWriter.close();
			Logger.debug(tag, "Success (function=logOut,address={},userName={})", request.getRemoteAddr().hashCode(),
					session.user.name);
		}
	}

	@SuppressWarnings("unchecked")
	private final void handleChange(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String userName = request.getParameter("user");
		final String hashOfPassword = request.getParameter("password");
		final String hashOfNewPassword = request.getParameter("newpassword");
		final boolean success = sessionManager.changeUserPassword(userName, hashOfPassword, hashOfNewPassword);
		if (!success) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			Logger.debug(tag, "Unauthorised (function=change,address={},userName={})",
					request.getRemoteAddr().hashCode(), userName);
		} else {
			final JSONObject j = new JSONObject();
			j.put("success", true);
			final String json = j.toJSONString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter printWriter = response.getWriter();
			printWriter.write(json);
			printWriter.flush();
			printWriter.close();
			Logger.debug(tag, "Success (function=change,address={},userName={})", request.getRemoteAddr().hashCode(),
					userName);
		}
	}

	private final void handleRefresh(HttpServletRequest request, HttpServletResponse response) throws IOException {
		final String token = request.getParameter("token");
		final Session session = sessionManager.isAuthorised(token);
		if (session == null) {
			response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
			Logger.debug(tag, "Unauthorised (function=refresh,address={})", request.getRemoteAddr().hashCode());
		} else {
			final String json = toJSON(session).toJSONString();
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter printWriter = response.getWriter();
			printWriter.write(json);
			printWriter.flush();
			printWriter.close();
			Logger.debug(tag, "Success (function=refresh,address={},userName={})", request.getRemoteAddr().hashCode(),
					session.user.name);
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

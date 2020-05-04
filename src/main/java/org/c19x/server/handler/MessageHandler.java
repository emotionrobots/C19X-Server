package org.c19x.server.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.server.data.Devices;
import org.c19x.util.Logger;
import org.c19x.util.SecurityUtil;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class MessageHandler extends AbstractHandler {
	private final static String tag = MessageHandler.class.getName();
	private final Devices devices;

	public MessageHandler(final Devices devices) {
		this.devices = devices;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			final String key = request.getParameter("key");
			final String value = request.getParameter("value");
			final byte[] sharedSecret = devices.getSharedSecret(key);
			if (sharedSecret != null) {
				final String timeWindow = SecurityUtil.decrypt(sharedSecret, value);
				if (Math.abs(Long.parseLong(timeWindow) - System.currentTimeMillis()) < 150000) {
					response.setStatus(HttpServletResponse.SC_OK);
					final String message = devices.getMessage(key);
					if (message != null) {
						final PrintWriter printWriter = response.getWriter();
						printWriter.print(message);
						printWriter.flush();
						printWriter.close();
					}
					Logger.debug(tag, "Success (address={},key={},message={})", request.getRemoteAddr(), key, message);
				} else {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					Logger.debug(tag, "Unauthorised (address={},key={},timeWindow={})", request.getRemoteAddr(), key,
							timeWindow);
				}
			} else {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				Logger.debug(tag, "Unregistered (address={},key={})", request.getRemoteAddr(), key);
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			Logger.warn(tag, "Failed (address={})", request.getRemoteAddr(), e);
		} finally {
			baseRequest.setHandled(true);
		}
	}
}
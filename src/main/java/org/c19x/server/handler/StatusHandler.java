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

public class StatusHandler extends AbstractHandler {
	private final static String tag = StatusHandler.class.getName();
	private final Devices devices;

	public StatusHandler(final Devices devices) {
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
				final String decrypted = SecurityUtil.decrypt(sharedSecret, value);
				final String timeWindow = decrypted.substring(0, decrypted.indexOf(','));
				final String status = decrypted.substring(timeWindow.length() + 1);
				if (Math.abs(Long.parseLong(timeWindow) - System.currentTimeMillis()) < 150000) {
					devices.setStatus(key, status);
					response.setStatus(HttpServletResponse.SC_OK);
					final PrintWriter printWriter = response.getWriter();
					printWriter.print(status);
					printWriter.flush();
					printWriter.close();
					Logger.debug(tag, "Success (address={},key={},status={})", request.getRemoteAddr(), key, status);
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
package org.c19x.server.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.server.data.Devices;
import org.c19x.util.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class RegistrationHandler extends AbstractHandler {
	private final static String tag = RegistrationHandler.class.getName();
	private final Devices devices;

	public RegistrationHandler(final Devices devices) {
		this.devices = devices;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			final String registration = devices.register();
			response.setContentType("text/plain; charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter printWriter = response.getWriter();
			printWriter.print(registration);
			printWriter.flush();
			printWriter.close();
			Logger.debug(tag, "Success (address={},serialNumber={})", request.getRemoteAddr(),
					registration.split(",")[0]);
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			Logger.warn(tag, "Failed", e);
		} finally {
			baseRequest.setHandled(true);
		}
	}

}
package org.c19x.server.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.server.data.Parameters;
import org.c19x.util.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ParametersHandler extends AbstractHandler {
	private final static String tag = ParametersHandler.class.getName();
	private String data = "";

	public synchronized void set(final Parameters parameters) {
		this.data = parameters.toJSON();
		Logger.info(tag, "Updated (data={})", data);
	}

	private synchronized String get() {
		return data;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			response.setContentType("application/json");
			response.setCharacterEncoding("UTF-8");
			response.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter printWriter = response.getWriter();
			printWriter.write(get());
			printWriter.flush();
			printWriter.close();
			Logger.debug(tag, "Success (fromAddress={})", request.getRemoteAddr().hashCode());
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			Logger.warn(tag, "Failed", e);
		} finally {
			baseRequest.setHandled(true);
		}
	}
}
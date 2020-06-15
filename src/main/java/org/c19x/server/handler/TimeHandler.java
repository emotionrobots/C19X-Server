package org.c19x.server.handler;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.util.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class TimeHandler extends AbstractHandler {
	private final static String tag = TimeHandler.class.getName();

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			response.setContentType("text/plain; charset=utf-8");
			response.setStatus(HttpServletResponse.SC_OK);
			final PrintWriter printWriter = response.getWriter();
			printWriter.print(Long.toString(System.currentTimeMillis()));
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
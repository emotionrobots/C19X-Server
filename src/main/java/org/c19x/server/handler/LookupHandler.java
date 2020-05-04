package org.c19x.server.handler;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.server.data.Lookup;
import org.c19x.util.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class LookupHandler extends AbstractHandler {
	private final static String tag = LookupHandler.class.getName();
	private byte[] data = new byte[0];

	public synchronized void set(final Lookup lookup) {
		this.data = lookup.getData();
		Logger.info(tag, "Updated (data={})", data.length);
	}

	private synchronized byte[] get() {
		return data;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			response.setContentType("application/octet-stream");
			response.setStatus(HttpServletResponse.SC_OK);
			final OutputStream outputStream = response.getOutputStream();
			outputStream.write(get());
			outputStream.flush();
			outputStream.close();
			Logger.debug(tag, "Success (fromAddress={})", request.getRemoteAddr());
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			Logger.warn(tag, "Failed", e);
		} finally {
			baseRequest.setHandled(true);
		}
	}
}
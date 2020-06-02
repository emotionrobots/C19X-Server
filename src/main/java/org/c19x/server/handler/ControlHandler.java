package org.c19x.server.handler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.server.data.Devices;
import org.c19x.server.data.InfectionData;
import org.c19x.server.data.Parameters;
import org.c19x.util.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;

public class ControlHandler extends AbstractHandler {
	private final static String tag = ControlHandler.class.getName();
	private final Devices devices;
	private final Parameters parameters;
	private final InfectionDataHandler infectionDataHandler;

	public ControlHandler(final Devices devices, final Parameters parameters,
			final InfectionDataHandler infectionDataHandler) {
		this.devices = devices;
		this.parameters = parameters;

		this.infectionDataHandler = infectionDataHandler;
	}

	@Override
	public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
			throws IOException, ServletException {
		try {
			final String passwordHashBase64 = request.getParameter("password");
			if (!parameters.getPasswordHashBase64().equals(passwordHashBase64)) {
				response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
				Logger.debug(tag, "Unauthorised (address={},password={})", request.getRemoteAddr(), passwordHashBase64);
			} else {
				final String command = request.getParameter("command");
				switch (command) {
				case "infectionData": {
					infectionDataHandler.set(new InfectionData(devices, parameters.getRetention()));
					Logger.debug(tag, "Updated infection data");
					break;
				}
				case "status": {
					final String serialNumber = request.getParameter("serialNumber");
					final String status = request.getParameter("status");
					devices.setStatus(serialNumber, status);
					Logger.debug(tag, "Set status (serialNumber={},status={})", serialNumber, status);
					break;
				}
				case "message": {
					final String serialNumber = request.getParameter("serialNumber");
					final String message = request.getParameter("message");
					devices.setMessage(serialNumber, message);
					Logger.debug(tag, "Set message (serialNumber={},message={})", serialNumber, message);
					break;
				}
				default: {
					Logger.warn(tag, "Unknown command (address={},command={})", request.getRemoteAddr(), command);
				}
				}
				response.setStatus(HttpServletResponse.SC_OK);
				Logger.debug(tag, "Success (address={},command={})", request.getRemoteAddr(), command);
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			Logger.warn(tag, "Failed (address={})", request.getRemoteAddr(), e);
		} finally {
			baseRequest.setHandled(true);
		}
	}
}
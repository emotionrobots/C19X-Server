package org.c19x.server.handler;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.c19x.server.data.Devices;
import org.c19x.server.data.InfectionData;
import org.c19x.server.data.Parameters;
import org.c19x.util.Logger;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ControlHandler extends AbstractHandler {
	private final static String tag = ControlHandler.class.getName();
	// @formatter:off
	private final static String helpText = 
			  "C19X Server Control Commands\n" 
			+ "============================\n\n"
			+ "GET /control\n"
			+ "Display this help message.\n\n"
			+ "GET /control?command=list&password=[passwordHashBase64]\n"
			+ "List all registered devices, contact pattern and status.\n\n"
			+ "GET /control?command=status&serialNumber=[serialNumber]&status=[statusRawValue]&password=[passwordHashBase64]\n"
			+ "Set status of registered device with [serialNumber] to [statusRawValue].\n\n"
			+ "GET /control?command=message&serialNumber=[serialNumber]&message=[message]&password=[passwordHashBase64]\n"
			+ "Set message for registered device with [serialNumber] to [message].\n\n"
			+ "GET /control?command=infectionData&password=[passwordHashBase64]\n"
			+ "Update infection data immediately.";
	// @formatter:on
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
			final String command = request.getParameter("command");
			if (command == null) {
				response.setContentType("text/plain");
				response.setCharacterEncoding("UTF-8");
				final PrintWriter printWriter = response.getWriter();
				printWriter.write(helpText);
				printWriter.flush();
				printWriter.close();
				Logger.debug(tag, "Help");
			} else {
				final String passwordHashBase64 = request.getParameter("password");
				if (!parameters.getPasswordHashBase64().equals(passwordHashBase64)) {
					response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
					Logger.debug(tag, "Unauthorised (address={},password={})", request.getRemoteAddr(),
							passwordHashBase64);
				} else {
					switch (command) {
					case "infectionData": {
						final InfectionData infectionData = new InfectionData(devices, parameters);
						infectionDataHandler.set(infectionData);
						response.setContentType("application/json");
						response.setCharacterEncoding("UTF-8");
						final PrintWriter printWriter = response.getWriter();
						printWriter.write(infectionData.toJSON());
						printWriter.flush();
						printWriter.close();
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
					case "list": {
						response.setContentType("application/json");
						response.setCharacterEncoding("UTF-8");
						final PrintWriter printWriter = response.getWriter();
						final String list = list(devices);
						printWriter.write(list);
						printWriter.flush();
						printWriter.close();
						Logger.debug(tag, "Listed devices");
						break;
					}
					case "summary": {
						response.setContentType("application/json");
						response.setCharacterEncoding("UTF-8");
						final PrintWriter printWriter = response.getWriter();
						final String list = summary(devices);
						printWriter.write(list);
						printWriter.flush();
						printWriter.close();
						Logger.debug(tag, "Summary");
						break;
					}
					default: {
						Logger.warn(tag, "Unknown command (address={},command={})", request.getRemoteAddr(), command);
					}
					}
					response.setStatus(HttpServletResponse.SC_OK);
					Logger.debug(tag, "Success (address={},command={})", request.getRemoteAddr(), command);
				}
			}
		} catch (Throwable e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			Logger.warn(tag, "Failed (address={})", request.getRemoteAddr(), e);
		} finally {
			baseRequest.setHandled(true);
		}
	}

	@SuppressWarnings("unchecked")
	private final static String summary(final Devices devices) {
		final JSONObject o = new JSONObject();
		{
			// STATUS
			long healthy = 0, symptomatic = 0, confirmedDiagnosis = 0;
			for (final String serialNumber : devices.getSerialNumbers()) {
				switch (devices.getStatus(serialNumber)) {
				case "0":
					healthy++;
					break;
				case "1":
					symptomatic++;
					break;
				case "2":
					confirmedDiagnosis++;
					break;
				}
			}
			final JSONObject j = new JSONObject();
			j.put("healthy", Long.toString(healthy));
			j.put("symptomatic", Long.toString(symptomatic));
			j.put("confirmedDiagnosis", Long.toString(confirmedDiagnosis));
			j.put("total", Long.toString(healthy + symptomatic + confirmedDiagnosis));
			o.put("status", j);
		}
		return o.toJSONString();
	}

	@SuppressWarnings("unchecked")
	private final static String list(final Devices devices) {
		final JSONArray j = new JSONArray();
		final List<String> serialNumbers = new ArrayList<>(devices.getSerialNumbers());
		serialNumbers.sort((a, b) -> Long.compare(Long.parseLong(a), Long.parseLong(b)));
		serialNumbers.forEach(n -> {
			final JSONObject o = new JSONObject();
			o.put("serialNumber", n);
			final String status = devices.getStatus(n);
			o.put("status", (status == null ? "0" : status));
			final String message = devices.getMessage(n);
			if (message != null) {
				o.put("message", message);
			}
			final String pattern = devices.getPattern(n);
			if (pattern != null) {
				o.put("pattern", pattern);
			}
			j.add(o);
		});
		return j.toJSONString();
	}
}
package org.c19x.server.data;

import org.c19x.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Parameters {
	private final static String tag = Parameters.class.getName();

	private String server = "https://appserver-test.c19x.org";
	// Retention period
	private int retention = 14;
	// Signal strength threshold is based on the mean - standard deviation of signal
	// strength measurements at 2 meter distance presented in the paper :
	// Sekara, Vedran & Lehmann, Sune. (2014). The Strength of Friendship Ties in
	// Proximity Sensor Data. PloS one. 9. e100915. 10.1371/journal.pone.0100915.
	private int proximity = -77;
	// Exposure threshold
	private int exposure = 15;
	// Default advice
	// 0 = No restriction
	// 1 = Stay at home
	// 2 = Self-isolation
	private int advice = 1;

	@SuppressWarnings("unchecked")
	public final String toJSON() {
		final JSONObject j = new JSONObject();
		j.put("server", server);
		j.put("advice", Integer.toString(advice));
		j.put("retention", Integer.toString(retention));
		j.put("proximity", Integer.toString(proximity));
		j.put("exposure", Integer.toString(exposure));
		final String s = j.toJSONString();
		return s;
	}

	@SuppressWarnings("unchecked")
	public final void fromJSON(final String string) {
		try {
			final JSONObject j = (JSONObject) new JSONParser().parse(string);
			if (Boolean.parseBoolean((String) j.getOrDefault("active", "false"))) {
				server = (String) j.getOrDefault("server", server);
				advice = Integer.parseInt((String) j.getOrDefault("advice", Integer.toString(advice)));
				retention = Integer.parseInt((String) j.getOrDefault("retention", Integer.toString(retention)));
				proximity = Integer.parseInt((String) j.getOrDefault("proximity", Integer.toString(proximity)));
				exposure = Integer.parseInt((String) j.getOrDefault("exposure", Integer.toString(exposure)));
			}
		} catch (ParseException e) {
			Logger.warn(tag, "Failed to parse string", e);
		}
	}

	public String getServer() {
		return server;
	}

	public int getAdvice() {
		return advice;
	}

	public int getRetention() {
		return retention;
	}

	public int getProximity() {
		return proximity;
	}

	public int getExposure() {
		return exposure;
	}

	@Override
	public String toString() {
		return "Parameters [server=" + server + ", retention=" + retention + ", proximity=" + proximity + ", exposure="
				+ exposure + ", advice=" + advice + "]";
	}
}

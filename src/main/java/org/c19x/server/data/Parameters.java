package org.c19x.server.data;

import java.io.File;
import java.nio.file.Files;

import org.c19x.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Parameters {
	private final static String tag = Parameters.class.getName();

	protected String server = "https://preprod.c19x.org";
	// Retention period
	protected int retention = 14;
	// Signal strength threshold is based on the mean - standard deviation of signal
	// strength measurements at 2 meter distance presented in the paper :
	// Sekara, Vedran & Lehmann, Sune. (2014). The Strength of Friendship Ties in
	// Proximity Sensor Data. PloS one. 9. e100915. 10.1371/journal.pone.0100915.
	protected int proximity = -77;
	// Exposure threshold
	protected int exposure = 15;
	// Default advice
	// 0 = No restriction
	// 1 = Stay at home
	// 2 = Self-isolation
	protected int advice = 1;
	// Infection data update delay in minutes
	protected int update = 24 * 60;
	// Symptomatic report expiry in days to discount report from infection report
	// updates.
	protected int expireSymptomatic = 8;
	// Confirmed diagnosis report expiry in days to discount report from infection
	// report updates.
	protected int expireConfirmedDiagnosis = 8;
	// Registration expiry in days to delete inactive devices.
	protected int expireInactivity = 21;

	public Parameters() {
	}

	public Parameters(final File file) {
		fromJSON(file);
	}

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

	public final void fromJSON(final File file) {
		if (file.exists() && file.canRead()) {
			try {
				final String json = new String(Files.readAllBytes(file.toPath()));
				fromJSON(json);
			} catch (Exception e) {
				Logger.warn(tag, "Failed to read file (file={})", e, file);
			}
		}
	}

	@SuppressWarnings("unchecked")
	public final void fromJSON(final String string) {
		try {
			final JSONObject j = (JSONObject) new JSONParser().parse(string);
			if (Boolean.parseBoolean((String) j.getOrDefault("active", "false"))) {
				// Device parameters
				server = (String) j.getOrDefault("server", server);
				advice = Integer.parseInt((String) j.getOrDefault("advice", Integer.toString(advice)));
				retention = Integer.parseInt((String) j.getOrDefault("retention", Integer.toString(retention)));
				proximity = Integer.parseInt((String) j.getOrDefault("proximity", Integer.toString(proximity)));
				exposure = Integer.parseInt((String) j.getOrDefault("exposure", Integer.toString(exposure)));
				// Server parameters
				update = Integer.parseInt((String) j.getOrDefault("update", Integer.toString(update)));
				expireSymptomatic = Integer
						.parseInt((String) j.getOrDefault("expireSymptomatic", Integer.toString(update)));
				expireConfirmedDiagnosis = Integer
						.parseInt((String) j.getOrDefault("expireConfirmedDiagnosis", Integer.toString(update)));
				expireInactivity = Integer
						.parseInt((String) j.getOrDefault("expireInactivity", Integer.toString(update)));
			}
		} catch (Exception e) {
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

	public int getUpdate() {
		return update;
	}

	public int getExpireSymptomatic() {
		return expireSymptomatic;
	}

	public int getExpireConfirmedDiagnosis() {
		return expireConfirmedDiagnosis;
	}

	public int getExpireInactivity() {
		return expireInactivity;
	}

	@Override
	public String toString() {
		return "Parameters [server=" + server + ", retention=" + retention + ", proximity=" + proximity + ", exposure="
				+ exposure + ", advice=" + advice + "]";
	}
}

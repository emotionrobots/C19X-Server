package org.c19x.server.data;

import java.util.Arrays;

import org.c19x.util.Logger;
import org.json.simple.JSONObject;

public class InfectionData {
	private final static String tag = InfectionData.class.getName();
	private final String json;

	public InfectionData(final Devices devices, final Parameters parameters) {
		final JSONObject jsonObject = get(devices, parameters);
		this.json = jsonObject.toJSONString();
	}

	public String toJSON() {
		return json;
	}

	@SuppressWarnings("unchecked")
	private final static JSONObject get(final Devices devices, final Parameters parameters) {
		final JSONObject j = new JSONObject();
		final int days = parameters.getRetention();
		final long expireSymptomatic = parameters.getExpireSymptomatic() * 24 * 60 * 60 * 1000;
		final long expireConfirmedDiagnosis = parameters.getExpireConfirmedDiagnosis() * 24 * 60 * 60 * 1000;
		final long now = System.currentTimeMillis();
		devices.getSerialNumbers().forEach(serialNumber -> {
			try {
				final String status = devices.getStatus(serialNumber);
				if (status != null && !status.equals("0")) {
					final long statusTimestamp = devices.getStatusTimestamp(serialNumber);
					// Check report expiry
					if (status.equals("1") && (now - statusTimestamp) > expireSymptomatic) {
						return;
					}
					if (status.equals("2") && (now - statusTimestamp) > expireConfirmedDiagnosis) {
						return;
					}
					// Release beacon code seeds
					final DayCodes dayCodes = devices.getCodes(serialNumber);
					final long[] beaconCodeSeeds = dayCodes.getBeaconCodeSeeds(days);
					Logger.debug(tag, "Infectious (serialNumber={},status={},beaconCodeSeeds={})", serialNumber, status,
							Arrays.toString(beaconCodeSeeds));
					for (final long beaconCodeSeed : beaconCodeSeeds) {
						j.put(Long.toString(beaconCodeSeed), status);
					}
				}
			} catch (Throwable e) {
				Logger.warn(tag, "Failed to set infection data (serialNumber={})", serialNumber, e);
			}
		});
		return j;
	}
}

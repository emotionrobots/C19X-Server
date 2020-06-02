package org.c19x.server.data;

import java.util.Arrays;

import org.c19x.util.Logger;
import org.json.simple.JSONObject;

public class InfectionData {
	private final static String tag = InfectionData.class.getName();
	private final String json;

	public InfectionData(final Devices devices, final int days) {
		final JSONObject jsonObject = get(devices, days);
		this.json = jsonObject.toJSONString();
	}

	public String toJSON() {
		return json;
	}

	@SuppressWarnings("unchecked")
	private final static JSONObject get(final Devices devices, final int days) {
		final JSONObject j = new JSONObject();
		devices.getSerialNumbers().forEach(serialNumber -> {
			try {
				final String status = devices.getStatus(serialNumber);
				if (status != null && !status.equals("0")) {
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

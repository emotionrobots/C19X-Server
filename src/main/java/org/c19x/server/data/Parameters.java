package org.c19x.server.data;

import org.c19x.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class Parameters {
	private final static String tag = Parameters.class.getName();

	private String serverAddress = "https://appserver-test.c19x.org";
	// 0 = No restriction
	// 1 = Stay at home
	// 2 = Self-isolation
	private int governmentAdvice = 0;
	// Retention period
	private int retentionPeriod = 14;
	// Signal strength threshold is based on the mean - standard deviation of signal
	// strength measurements at 2 meter distance presented in the paper :
	// Sekara, Vedran & Lehmann, Sune. (2014). The Strength of Friendship Ties in
	// Proximity Sensor Data. PloS one. 9. e100915. 10.1371/journal.pone.0100915.
	private float signalStrengthThreshold = (-82.03f - -4.57f);
	// Bluetooth discovery inquiry scan lasts for about 12 seconds, thus a multiple
	// of this value offers the sample period for consecutive timestamps, taking
	// into account signal drop outs.
	private int contactDurationThreshold = 5 * 60000;
	// Exposure threshold
	private int exposureDurationThreshold = 15 * 60000;
	private int beaconReceiverOnDuration = 15000;
	private int beaconReceiverOffDuration = 85000;

	public final String toJSON() {
		final JSONObject j = new JSONObject();
		j.put("serverAddress", getServerAddress());
		j.put("governmentAdvice", Integer.toString(getGovernmentAdvice()));
		j.put("retentionPeriod", Integer.toString(getRetentionPeriod()));
		j.put("signalStrengthThreshold", Float.toString(getSignalStrengthThreshold()));
		j.put("contactDurationThreshold", Integer.toString(getContactDurationThreshold()));
		j.put("exposureDurationThreshold", Integer.toString(getExposureDurationThreshold()));
		j.put("beaconReceiverOnDuration", Integer.toString(getBeaconReceiverOnDuration()));
		j.put("beaconReceiverOffDuration", Integer.toString(getBeaconReceiverOffDuration()));
		final String s = j.toJSONString();
		return s;
	}

	public final void fromJSON(final String string) {
		try {
			final JSONObject j = (JSONObject) new JSONParser().parse(string);
			if (Boolean.parseBoolean((String) j.getOrDefault("active", "false"))) {
				setServerAddress((String) j.getOrDefault("serverAddress", getServerAddress()));
				setGovernmentAdvice(Integer.parseInt(
						(String) j.getOrDefault("governmentAdvice", Integer.toString(getGovernmentAdvice()))));
				setRetentionPeriod(Integer
						.parseInt((String) j.getOrDefault("retentionPeriod", Integer.toString(getRetentionPeriod()))));
				setSignalStrengthThreshold(Float.parseFloat((String) j.getOrDefault("signalStrengthThreshold",
						Float.toString(getSignalStrengthThreshold()))));
				setContactDurationThreshold(Integer.parseInt((String) j.getOrDefault("contactDurationThreshold",
						Integer.toString(getContactDurationThreshold()))));
				setExposureDurationThreshold(Integer.parseInt((String) j.getOrDefault("exposureDurationThreshold",
						Integer.toString(getExposureDurationThreshold()))));
				setBeaconReceiverOnDuration(Integer.parseInt((String) j.getOrDefault("beaconReceiverOnDuration",
						Integer.toString(getBeaconReceiverOnDuration()))));
				setBeaconReceiverOffDuration(Integer.parseInt((String) j.getOrDefault("beaconReceiverOffDuration",
						Integer.toString(getBeaconReceiverOffDuration()))));
			}
		} catch (ParseException e) {
			Logger.warn(tag, "Failed to parse string", e);
		}
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public int getGovernmentAdvice() {
		return governmentAdvice;
	}

	public void setGovernmentAdvice(int governmentAdvice) {
		this.governmentAdvice = governmentAdvice;
	}

	public int getRetentionPeriod() {
		return retentionPeriod;
	}

	public void setRetentionPeriod(int retentionPeriod) {
		this.retentionPeriod = retentionPeriod;
	}

	public float getSignalStrengthThreshold() {
		return signalStrengthThreshold;
	}

	public void setSignalStrengthThreshold(float signalStrengthThreshold) {
		this.signalStrengthThreshold = signalStrengthThreshold;
	}

	public int getContactDurationThreshold() {
		return contactDurationThreshold;
	}

	public void setContactDurationThreshold(int contactDurationThreshold) {
		this.contactDurationThreshold = contactDurationThreshold;
	}

	public int getExposureDurationThreshold() {
		return exposureDurationThreshold;
	}

	public void setExposureDurationThreshold(int exposureDurationThreshold) {
		this.exposureDurationThreshold = exposureDurationThreshold;
	}

	public int getBeaconReceiverOnDuration() {
		return beaconReceiverOnDuration;
	}

	public void setBeaconReceiverOnDuration(int beaconReceiverOnDuration) {
		this.beaconReceiverOnDuration = beaconReceiverOnDuration;
	}

	public int getBeaconReceiverOffDuration() {
		return beaconReceiverOffDuration;
	}

	public void setBeaconReceiverOffDuration(int beaconReceiverOffDuration) {
		this.beaconReceiverOffDuration = beaconReceiverOffDuration;
	}

	@Override
	public String toString() {
		return "Parameters [serverAddress=" + serverAddress + ", governmentAdvice=" + governmentAdvice
				+ ", retentionPeriod=" + retentionPeriod + ", signalStrengthThreshold=" + signalStrengthThreshold
				+ ", contactDurationThreshold=" + contactDurationThreshold + ", exposureDurationThreshold="
				+ exposureDurationThreshold + ", beaconReceiverOnDuration=" + beaconReceiverOnDuration
				+ ", beaconReceiverOffDuration=" + beaconReceiverOffDuration + "]";
	}

}

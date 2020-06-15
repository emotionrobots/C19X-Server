package org.c19x.server.data;

import java.io.File;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.c19x.util.KeyValueStore;
import org.c19x.util.Logger;
import org.c19x.util.SecurityUtil;

/**
 * All registered devices.
 * 
 * @author user
 *
 */
public class Devices {
	@SuppressWarnings("unused")
	private final static String tag = Devices.class.getName();
	private final static int sharedSecretLength = 32;
	private final KeyValueStore parameters;
	private final KeyValueStore registrations;
	private final KeyValueStore statuses;
	private final KeyValueStore patterns;
	private final KeyValueStore messages;
	private final KeyValueStore timestamps;
	private final Map<String, DayCodes> codes;

	public Devices(final File folder) {
		parameters = new KeyValueStore(new File(folder, "parameters"));
		registrations = new KeyValueStore(new File(folder, "registrations"));
		statuses = new KeyValueStore(new File(folder, "statuses"));
		messages = new KeyValueStore(new File(folder, "messages"));
		patterns = new KeyValueStore(new File(folder, "patterns"));
		timestamps = new KeyValueStore(new File(folder, "timestamps"));
		codes = new ConcurrentHashMap<>();
		generateCodes();
	}

	/**
	 * Register activity associated with serial number to retain data.
	 * 
	 * @param serialNumber
	 */
	public void touch(final String serialNumber) {
		timestamps.put(serialNumber, Long.toString(System.currentTimeMillis()));
	}

	public void clear(final long retention) {
		final long deleteBefore = System.currentTimeMillis() - (retention * 24 * 60 * 60 * 1000);
		timestamps.entries().forEach(e -> {
			final long timestamp = Long.parseLong(e.getValue());
			if (timestamp < deleteBefore) {
				final String serialNumber = e.getKey();
				unregister(serialNumber);
				Logger.debug(tag, "Unregistered (serialNumber={},timestamp={})", serialNumber, new Date(timestamp));
			}
		});
	}

	private void unregister(final String serialNumber) {
		registrations.remove(serialNumber);
		statuses.remove(serialNumber);
		messages.remove(serialNumber);
		patterns.remove(serialNumber);
		timestamps.remove(serialNumber);
		codes.remove(serialNumber);
	}

	protected void generateCodes() {
		registrations.entries().parallelStream().forEach(e -> {
			final String serialNumber = e.getKey();
			final byte[] sharedSecret = Base64.getDecoder().decode(e.getValue());
			final DayCodes c = new DayCodes(sharedSecret);
			codes.put(serialNumber, c);
		});
	}

	protected String getSerialNumber() {
		synchronized (parameters) {
			String value = parameters.get("serialNumber");
			if (value == null) {
				parameters.put("serialNumber", "1");
				return "1";
			} else {
				value = Long.toString(Long.parseLong(value) + 1);
				parameters.put("serialNumber", value);
				return value;
			}
		}
	}

	protected String register(final String serialNumber, final byte[] sharedSecret) {
		final String sharedSecretInBase64 = Base64.getEncoder().encodeToString(sharedSecret);
		registrations.put(serialNumber, sharedSecretInBase64);
		codes.put(serialNumber, new DayCodes(sharedSecret));
		return serialNumber + "," + sharedSecretInBase64;
	}

	public String register() {
		synchronized (registrations) {
			final String serialNumber = getSerialNumber();
			final byte[] sharedSecret = new byte[sharedSecretLength];
			SecurityUtil.getSecureRandom().nextBytes(sharedSecret);
			return register(serialNumber, sharedSecret);
		}
	}

	public byte[] getSharedSecret(final String serialNumber) {
		final String sharedSecretInBase64 = registrations.get(serialNumber);
		if (sharedSecretInBase64 != null) {
			return Base64.getDecoder().decode(sharedSecretInBase64);
		} else {
			return null;
		}
	}

	public DayCodes getCodes(final String serialNumber) {
		DayCodes c = codes.get(serialNumber);
		if (c == null) {
			final byte[] sharedSecret = getSharedSecret(serialNumber);
			c = new DayCodes(sharedSecret);
			codes.put(serialNumber, c);
		}
		return c;
	}

	public void setStatus(final String serialNumber, final String status) {
		final String value = status + "," + Long.toString(System.currentTimeMillis());
		statuses.put(serialNumber, value);
	}

	public String getStatus(final String serialNumber) {
		final String value = statuses.get(serialNumber);
		return value.substring(0, value.indexOf(','));
	}

	public long getStatusTimestamp(final String serialNumber) {
		final String value = statuses.get(serialNumber);
		return Long.parseLong(value.substring(value.indexOf(',') + 1));
	}

	public void setPattern(final String serialNumber, final String pattern) {
		patterns.put(serialNumber, pattern);
	}

	public String getPattern(final String serialNumber) {
		return patterns.get(serialNumber);
	}

	public void setMessage(final String serialNumber, final String message) {
		messages.put(serialNumber, message);
	}

	public String getMessage(final String serialNumber) {
		final String message = messages.get(serialNumber);
		return message;
	}

	/**
	 * Get all registered devices.
	 * 
	 * @return
	 */
	public Set<String> getSerialNumbers() {
		return registrations.keys();
	}
}

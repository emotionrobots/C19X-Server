package org.c19x.server.data;

import java.io.File;
import java.util.Base64;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.c19x.util.KeyValueStore;
import org.c19x.util.SecurityUtil;

/**
 * All registered devices.
 * 
 * @author user
 *
 */
public class Devices {
	private final static String tag = Devices.class.getName();
	private final static int sharedSecretLength = 32;
	private final KeyValueStore parameters;
	private final KeyValueStore registrations;
	private final KeyValueStore statuses;
	private final KeyValueStore messages;
	private final Map<String, DayCodes> codes;

	public Devices(final File folder) {
		parameters = new KeyValueStore(new File(folder, "parameters"));
		registrations = new KeyValueStore(new File(folder, "registrations"));
		statuses = new KeyValueStore(new File(folder, "statuses"));
		messages = new KeyValueStore(new File(folder, "messages"));
		codes = new ConcurrentHashMap<>();
		generateCodes();
	}

	private void generateCodes() {
		registrations.entries().parallelStream().forEach(e -> {
			final String serialNumber = e.getKey();
			final byte[] sharedSecret = Base64.getDecoder().decode(e.getValue());
			final DayCodes c = new DayCodes(sharedSecret);
			codes.put(serialNumber, c);
		});
	}

	private String getSerialNumber() {
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

	public String register() {
		synchronized (registrations) {
			final String serialNumber = getSerialNumber();
			final byte[] sharedSecret = new byte[sharedSecretLength];
			SecurityUtil.getSecureRandom().nextBytes(sharedSecret);
			final String sharedSecretInBase64 = Base64.getEncoder().encodeToString(sharedSecret);
			registrations.put(serialNumber, sharedSecretInBase64);
			codes.put(serialNumber, new DayCodes(sharedSecret));
			return serialNumber + "," + sharedSecretInBase64;
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
		statuses.put(serialNumber, status);
	}

	public String getStatus(final String serialNumber) {
		return statuses.get(serialNumber);
	}

	public void setMessage(final String serialNumber, final String message) {
		messages.put(serialNumber, message);
	}

	public String getMessage(final String serialNumber) {
		return messages.get(serialNumber);
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

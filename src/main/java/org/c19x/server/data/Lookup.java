package org.c19x.server.data;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import org.c19x.util.Logger;

public class Lookup {
	private final static String tag = Lookup.class.getName();
	private final byte[] data;

	public Lookup(final Devices devices, final int retention) {
		this.data = getLookup(devices, Configuration.range, retention);
	}

	public byte[] getData() {
		return data;
	}

	public boolean isInfected(final long serialNumber) {
		return get(data, (int) Math.abs(serialNumber % Configuration.range));
	}

	/**
	 * Get lookup table for all device statuses.
	 * 
	 * @param devices
	 * @param size
	 * @param retention
	 * @return
	 */
	private final static byte[] getLookup(final Devices devices, final int range, final int retention) {
		Logger.debug(tag, "Generating lookup table");
		final int size = range / 8;
		final byte[] infectious = new byte[size];
		// Fill lookup table with random data
		Logger.debug(tag, "Filling lookup table with random data");
		// SecurityUtil.getSecureRandom().nextBytes(infectious);
		// Fill lookup table with device data
		Logger.debug(tag, "Filling lookup table with actual data");
		devices.getSerialNumbers().forEach(serialNumber -> {
			try {
				final String status = devices.getStatus(serialNumber);
				final boolean value = (status != null && !status.equals("0"));
				final DayCodes codes = devices.getCodes(serialNumber);
				for (long code : codes.get(retention)) {
					final int index = (int) Math.abs(code % range);
					set(infectious, index, value);
					if (value) {
						Logger.debug(tag, "Infectious (serialNumber={},code={},index={})", serialNumber, code, index);
					}
				}
			} catch (Throwable e) {
				Logger.warn(tag, "Failed to set lookup values (serialNumber={})", serialNumber, e);
			}
		});
		return infectious;
	}

	private final static void set(final byte[] data, final int index, final boolean value) {
		if (value) {
			data[index / 8] |= (1 << (index % 8));
		} else {
			data[index / 8] &= ~(1 << (index % 8));
		}
	}

	private final static boolean get(final byte[] data, final int index) {
		return ((data[index / 8] >> (index % 8)) & 1) != 0;
	}

	/**
	 * Decompress data.
	 *
	 * @param compressedData Compressed data.
	 * @return Source data, or null on failure.
	 */
	public static byte[] decompress(byte[] compressedData) {
		final Inflater inflater = new Inflater();
		inflater.setInput(compressedData);
		try {
			final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(compressedData.length);
			final byte[] buffer = new byte[1024];
			while (!inflater.finished()) {
				int count = inflater.inflate(buffer);
				byteArrayOutputStream.write(buffer, 0, count);
			}
			byteArrayOutputStream.close();
			final byte[] data = byteArrayOutputStream.toByteArray();
			return data;
		} catch (IOException | DataFormatException e) {
			return null;
		}
	}
}

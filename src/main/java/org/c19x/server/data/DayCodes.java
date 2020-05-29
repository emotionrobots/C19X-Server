package org.c19x.server.data;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

import org.c19x.util.Logger;

public class DayCodes {
	private final static String tag = DayCodes.class.getName();
	private final static long epoch = getEpoch();
	public final static BigInteger range = new BigInteger("9223372036854775807");
	private final static int days = 365 * 5;
	private final static int codesPerDay = 24 * 10;
	private final static long dayMillis = 24 * 60 * 60 * 1000;

	private final long[] values;

	public DayCodes(final byte[] sharedSecret) {
		values = getValues(sharedSecret);
	}

	public String getHumanReadableCode() {
		return longToText(get() % (long) Math.pow(26, 6));
	}

	private final static String longToText(long value) {
		if (value > 0) {
			final char c = (char) ('A' + (int) (value % 26));
			return longToText(value / 26) + c;
		} else {
			return "";
		}
	}

	public long get() {
		final int today = (int) ((System.currentTimeMillis() - epoch) / dayMillis);
		return values[today];
	}

	public long[] getBeaconCodeSeeds(int days) {
		final long[] dayCodes = get(days);
		final long[] beaconCodeSeeds = new long[dayCodes.length];
		for (int i = dayCodes.length; i-- > 0;) {
			beaconCodeSeeds[i] = seedOf(dayCodes[i]);
		}
		return beaconCodeSeeds;
	}

	private final static long seedOf(final long dayCode) {
		final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
		byteBuffer.putLong(0, dayCode);
		// Reverse bytes
		final byte[] data = byteBuffer.array();
		final byte[] reversed = new byte[] { data[7], data[6], data[5], data[4], data[3], data[2], data[1], data[0] };
		// Hash of reversed
		try {
			final MessageDigest sha = MessageDigest.getInstance("SHA-256");
			final byte[] hash = sha.digest(reversed);
			final long seed = longValue(hash);
//			Logger.debug(tag, "seedOf (dayCode={},data={},hash={},seed={})", dayCode, Arrays.toString(data),
//					Base64.getEncoder().encodeToString(hash), seed);
			return seed;
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to transform day code to beacon code seed", e);
			return 0;
		}
	}

	public final static long[] beaconCodes(final long beaconCodeSeed) {
		final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
		byteBuffer.putLong(0, beaconCodeSeed);
		final byte[] data = byteBuffer.array();
		final long[] codes = new long[codesPerDay];
		try {
			final MessageDigest sha = MessageDigest.getInstance("SHA-256");
			byte[] hash = sha.digest(data);
			for (int i = codes.length; i-- > 0;) {
				codes[i] = longValue(hash);
				sha.reset();
//				Logger.debug(tag, "Beacon code (seed={},number={},hash={},code={})", beaconCodeSeed, i,
//						Base64.getEncoder().encodeToString(hash), codes[i]);
				hash = sha.digest(hash);
			}
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to get codes", e);
		}
		return codes;
	}

	/**
	 * Get codes for today and previous N days
	 * 
	 * @param days Days to include, minimum 1.
	 * @return
	 */
	public long[] get(int days) {
		assert (days >= 1);
		final int end = (int) ((System.currentTimeMillis() - epoch) / dayMillis);
		final long[] result = new long[days];
		System.arraycopy(values, end - days - 1, result, 0, days);
		return result;
	}

	private final static long[] getValues(final byte[] sharedSecret) {
		final long[] codes = new long[days];
		try {
			final MessageDigest sha = MessageDigest.getInstance("SHA-256");
			byte[] hash = sha.digest(sharedSecret);
			for (int i = codes.length; i-- > 0;) {
				codes[i] = longValue(hash);
				sha.reset();
//				Logger.debug(tag, "Day code (day={},hash={},code={},seed={})", i,
//						Base64.getEncoder().encodeToString(hash), codes[i], seedOf(codes[i]));
				hash = sha.digest(hash);
			}
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to get codes", e);
		}
		return codes;
	}

	private final static long longValue(final byte[] hash) {
		final ByteBuffer byteBuffer = ByteBuffer.wrap(hash);
		return byteBuffer.getLong(0);
	}

	private final static long getEpoch() {
		try {
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			simpleDateFormat.setLenient(false);
			return simpleDateFormat.parse("2020-01-01 00:00").getTime();
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to get epoch", e);
			return 0;
		}
	}
}

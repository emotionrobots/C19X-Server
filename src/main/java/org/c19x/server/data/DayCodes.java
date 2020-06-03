package org.c19x.server.data;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.Arrays;

import org.c19x.util.Logger;

public class DayCodes {
	private final static String tag = DayCodes.class.getName();
	private final static long epoch = epoch();
	private final static int days = 365 * 5;
	private final static long dayMillis = 24 * 60 * 60 * 1000;
	private final long[] values;

	public DayCodes(final byte[] sharedSecret) {
		values = dayCodes(sharedSecret, days);
	}

	public final static long[] beaconCodes(final long beaconCodeSeed, final int count) {
		final ByteBuffer byteBuffer = ByteBuffer.allocate(Long.BYTES);
		byteBuffer.putLong(0, beaconCodeSeed);
		final byte[] data = byteBuffer.array();
		final long[] codes = new long[count];
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

	protected final static long beaconCodeSeed(final long dayCode) {
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

	protected final static long[] dayCodes(final byte[] sharedSecret, final int count) {
		final long[] codes = new long[count];
		try {
			final MessageDigest sha = MessageDigest.getInstance("SHA-256");
			byte[] hash = sha.digest(sharedSecret);
			for (int i = codes.length; i-- > 0;) {
				codes[i] = longValue(hash);
				sha.reset();
				hash = sha.digest(hash);
			}
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to get codes", e);
		}
		return codes;
	}

	protected final static long epoch() {
		try {
			final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			simpleDateFormat.setLenient(false);
			return simpleDateFormat.parse("2020-01-01 00:00").getTime();
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to get epoch", e);
			return 0;
		}
	}

	private final static String longToText(long value) {
		if (value > 0) {
			final char c = (char) ('A' + (int) (value % 26));
			return longToText(value / 26) + c;
		} else {
			return "";
		}
	}

	private final static long longValue(final byte[] hash) {
		final ByteBuffer byteBuffer = ByteBuffer.wrap(hash);
		return byteBuffer.getLong(0);
	}

	protected final static int today() {
		final int today = (int) ((System.currentTimeMillis() - epoch) / dayMillis);
		return today;
	}

	/**
	 * Get subsequence of day codes.
	 * 
	 * @param codes
	 * @param from  Inclusive.
	 * @param to    Exclusive.
	 * @return
	 */
	protected final static long[] getDayCodes(final long[] dayCodes, final int from, final int to) {
		return Arrays.copyOfRange(dayCodes, from, to);
	}

	/**
	 * Get subsequence of beacon code seeds based on subsequence of day codes.
	 * 
	 * @param codes
	 * @param from  Inclusive.
	 * @param to    Exclusive.
	 * @return
	 */
	protected final static long[] getBeaconCodeSeeds(final long[] dayCodes, final int from, final int to) {
		final long[] codes = Arrays.copyOfRange(dayCodes, from, to);
		for (int i = codes.length; i-- > 0;) {
			codes[i] = beaconCodeSeed(codes[i]);
		}
		return codes;
	}

	public long get() {
		return values[today()];
	}

	protected long[] getDayCodes(final int from, final int to) {
		return getDayCodes(values, from, to);
	}

	protected long[] getBeaconCodeSeeds(final int from, final int to) {
		return getBeaconCodeSeeds(values, from, to);
	}

	/**
	 * Get codes for today and previous N days
	 * 
	 * @param days Days to include, minimum 1.
	 * @return
	 */
	public long[] get(int days) {
		assert (days >= 1);
		final int to = today() + 1;
		final int from = to - days;
		return getDayCodes(values, from, to);
	}

	public long[] getBeaconCodeSeeds(int days) {
		assert (days >= 1);
		final int to = today() + 1;
		final int from = to - days;
		return getBeaconCodeSeeds(values, from, to);
	}

	public String getHumanReadableCode() {
		return longToText(get() % (long) Math.pow(26, 6));
	}
}

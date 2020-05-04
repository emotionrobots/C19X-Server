package org.c19x.server.data;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;

import org.c19x.util.Logger;

public class Codes {
	private final static String tag = Codes.class.getName();
	private final static long epoch = getEpoch();
	public final static BigInteger range = new BigInteger("9223372036854775807");
	private final static int days = 365 * 10;
	private final static long dayMillis = 24 * 60 * 60 * 1000;

	private final long[] values;

	public Codes(final byte[] sharedSecret) {
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
				codes[i] = new BigInteger(hash).mod(range).longValue();
				sha.reset();
				hash = sha.digest(hash);
				// System.err.println(i + " -> " + codes[i]);
			}
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to get codes", e);
		}
		return codes;
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

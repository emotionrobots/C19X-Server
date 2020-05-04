package org.c19x.server.data;

import java.math.BigInteger;
import java.util.Base64;

import org.c19x.util.SecurityUtil;
import org.junit.Test;

public class ParametersTest {

	@Test
	public void toJSON() {
		final Parameters parameters = new Parameters();
		System.err.println(parameters.toJSON());

	}

	@Test
	public void bigInteger() {
		final byte[] bytes = new byte[32];
		SecurityUtil.getSecureRandom().nextBytes(bytes);
		final BigInteger bigInteger = new BigInteger(bytes);
		System.err.println(bigInteger);
		System.err.println(Base64.getEncoder().encodeToString(bytes));
	}

}

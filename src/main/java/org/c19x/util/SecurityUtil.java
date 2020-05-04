package org.c19x.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class SecurityUtil {
	private final static String tag = SecurityUtil.class.getName();
	private static SecureRandom secureRandom = null;

	public final static void main(String[] a) throws Throwable {
		final byte[] key = "12345678901234561234567890123456".getBytes();
		final String bundle = "aRV88VgpyzVi3aNCnkJO4g==,pfk0PpMZScVyQOxyHgR6tA==";
		System.err.println(decrypt(key, bundle));
	}

	/**
	 * Get a securely seeded secure random number generator. The implementation uses
	 * an instance of the SHA1 PRNG which is securely seeded using a separate
	 * instance of secure random.
	 *
	 * @return
	 */
	public synchronized final static SecureRandom getSecureRandom() {
		if (secureRandom == null) {
			try {
				// Get an instance of the SUN SHA1 PRNG
				secureRandom = SecureRandom.getInstance("SHA1PRNG");
				// Securely seed
				final SecureRandom randomForSeeding = new SecureRandom();
				// NIST SP800-90A suggests 440 bits for SHA1 seed
				final byte[] seed = randomForSeeding.generateSeed(55);
				secureRandom.setSeed(seed);
				// Securely start
				secureRandom.nextBytes(new byte[256 + secureRandom.nextInt(1024)]);
			} catch (Exception e) {
				Logger.error(tag, "Failed to initialise pseudo random number generator", e);
			}
		}
		return secureRandom;
	}

	public final static String encrypt(final byte[] key, final String value) {
		assert (key.length == 256);
		try {
			final byte[] iv = new byte[16];
			getSecureRandom().nextBytes(iv);

			final IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
			final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

			final byte[] encrypted = cipher.doFinal(value.getBytes());
			final String bundle = Base64.getEncoder().encodeToString(iv) + ","
					+ Base64.getEncoder().encodeToString(encrypted);
			Logger.info(tag, "AES |{}|", bundle);
			return bundle;
		} catch (Exception e) {
			return null;
		}
	}

	public final static String decrypt(final byte[] key, final String bundle) {
		try {
			final String ivString = bundle.substring(0, bundle.indexOf(','));
			final String cryptString = bundle.substring(ivString.length() + 1);

			final IvParameterSpec ivParameterSpec = new IvParameterSpec(Base64.getUrlDecoder().decode(ivString));
			final SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");

			final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

			final String clearText = new String(cipher.doFinal(Base64.getUrlDecoder().decode(cryptString)));
			return clearText;
		} catch (Throwable e) {
			Logger.warn(tag, "Failed to decrypt (bundle={})", bundle, e);
			return null;
		}
	}

	public final static String sha(byte[] data) {
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
			final byte[] digest = messageDigest.digest(data);
			final BigInteger bigInteger = new BigInteger(1, digest);
			String description = bigInteger.toString(16);
			while (description.length() < 128) {
				description = "0" + description;
			}
			return description;
		} catch (Throwable e) {
			Logger.warn(tag, "SHA512 failed", e);
			return null;
		}
	}

	public final static void hmac(final byte[] sharedSecret, final long time) {
		try {
			final Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(sharedSecret, "HmacSHA256"));
			final byte[] message = "1234".getBytes();
			final byte[] hash = mac.doFinal(message);
			Logger.info(tag, "HMAC (hash={})", Base64.getEncoder().encodeToString(hash));
		} catch (Throwable e) {

		}

	}

	public final static String sha512(byte[] data) {
		try {
			final MessageDigest messageDigest = MessageDigest.getInstance("SHA-512");
			final byte[] digest = messageDigest.digest(data);
			final BigInteger bigInteger = new BigInteger(1, digest);
			String description = bigInteger.toString(16);
			while (description.length() < 128) {
				description = "0" + description;
			}
			return description;
		} catch (Throwable e) {
			Logger.warn(tag, "SHA512 failed", e);
			return null;
		}
	}

}

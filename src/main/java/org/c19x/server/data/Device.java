package org.c19x.server.data;

/**
 * Registered device.
 * 
 * @author user
 *
 */
public class Device {
	private final long serialNumber;
	private final byte[] sharedSecret;
	private int status = 0;
	private String message = null;
	private transient Codes codes;

	public Device(long serialNumber, byte[] sharedSecret) {
		super();
		this.serialNumber = serialNumber;
		this.sharedSecret = sharedSecret;
		this.codes = new Codes(sharedSecret);
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public long getSerialNumber() {
		return serialNumber;
	}

	public byte[] getSharedSecret() {
		return sharedSecret;
	}

	public String getMessage() {
		return "The NHS needs to contact you. Please go to www.nhs.uk/c19x and enter code '"
				+ codes.getHumanReadableCode() + "' to provide details.";
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Codes getCodes() {
		return codes;
	}

	@Override
	public String toString() {
		return "Device [serialNumber=" + serialNumber + ", status=" + status + ", message=" + message + "]";
	}
}

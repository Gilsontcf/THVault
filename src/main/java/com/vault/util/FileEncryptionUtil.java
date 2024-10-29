package com.vault.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

public class FileEncryptionUtil {

	private static final String ALGORITHM = "AES";
	private SecretKeySpec secretKey;

	public FileEncryptionUtil(String base64Key) {
		byte[] decodedKey = Base64.getDecoder().decode(base64Key);
		this.secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
	}

	public byte[] encrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}

	public byte[] decrypt(byte[] data) throws Exception {
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);
		return cipher.doFinal(data);
	}
}

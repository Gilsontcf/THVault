package com.vault.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * Utility class for encrypting and decrypting file chunks.
 * Uses AES algorithm with a Base64 encoded secret key.
 */
public class FileEncryptionUtil {

    private static final String ALGORITHM = "AES";
    private SecretKeySpec secretKey;

    /**
     * Constructs the FileEncryptionUtil with a Base64 encoded AES key.
     * @param base64Key Base64 encoded AES encryption key.
     */
    public FileEncryptionUtil(String base64Key) {
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        this.secretKey = new SecretKeySpec(decodedKey, ALGORITHM);
    }

    /**
     * Encrypts a byte array using AES encryption.
     * @param data Data to encrypt.
     * @return Encrypted byte array.
     * @throws Exception if encryption fails.
     */
    public byte[] encrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }

    /**
     * Decrypts a byte array using AES encryption.
     * @param data Data to decrypt.
     * @return Decrypted byte array.
     * @throws Exception if decryption fails.
     */
    public byte[] decrypt(byte[] data) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(data);
    }
}

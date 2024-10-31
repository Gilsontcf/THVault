package vault.util;

import com.vault.util.FileEncryptionUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FileEncryptionUtilTest {

    private FileEncryptionUtil fileEncryptionUtil;
    private final String aesKey = "MTIzNDU2Nzg5MDEyMzQ1Ng==";  // Mock key for testing purposes

    @BeforeEach
    void setUp() {
        fileEncryptionUtil = new FileEncryptionUtil(aesKey);
    }

    @Test
    void testEncryptAndDecrypt() throws Exception {
        String originalText = "Sensitive data";
        byte[] encryptedData = fileEncryptionUtil.encrypt(originalText.getBytes());

        assertNotNull(encryptedData);

        byte[] decryptedData = fileEncryptionUtil.decrypt(encryptedData);

        assertNotNull(decryptedData);
        assertEquals(originalText, new String(decryptedData));
    }
}

package com.hmg.role.sdk.fetcher.crypto;

import java.util.Arrays;
import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Java 8-compatible AES-256-GCM decryption service. Accepts input formatted as: IV (12 bytes) ||
 * ciphertext+tag (tag = 16 bytes).
 */
public class DataDecryptionService {
    private static final int AES256_KEY_LENGTH = CryptoConstants.AES256_KEY_LENGTH;
    private static final int IV_LENGTH_BYTES = CryptoConstants.IV_LENGTH_BYTES;
    private static final int GCM_TAG_LENGTH_BITS = CryptoConstants.GCM_TAG_LENGTH_BITS;
    private static final String TRANSFORMATION = CryptoConstants.TRANSFORMATION;
    private static final String ALGORITHM = CryptoConstants.ALGORITHM;

    /**
     * Decrypt data produced by the matching AES-GCM encryptor. Input must be: IV || (ciphertext +
     * tag)
     *
     * @param ivAndCiphertext the concatenated IV and ciphertext+tag
     * @param key 32-byte AES-256 key
     * @return plaintext bytes
     * @throws IllegalArgumentException if inputs are invalid
     * @throws RuntimeException if decryption fails (e.g., authentication/tag failure)
     */
    public byte[] decrypt(byte[] ivAndCiphertext, byte[] key) {
        require256BitKey(key);

        if (ivAndCiphertext == null || ivAndCiphertext.length < IV_LENGTH_BYTES + 1) {
            throw new IllegalArgumentException(
                    "Invalid input: too short to contain IV and ciphertext");
        }

        // Split into IV and ciphertext+tag
        byte[] iv = Arrays.copyOfRange(ivAndCiphertext, 0, IV_LENGTH_BYTES);
        byte[] ciphertextWithTag =
                Arrays.copyOfRange(ivAndCiphertext, IV_LENGTH_BYTES, ivAndCiphertext.length);

        try {
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(key, ALGORITHM);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, gcmSpec);
            // If you used AAD during encryption, supply the exact same bytes here:
            // cipher.updateAAD(aadBytes); // unused since currently no authentication is used

            return cipher.doFinal(ciphertextWithTag);
        } catch (AEADBadTagException badTag) {
            // Authentication failed: wrong key/IV/AAD or tampered ciphertext.
            throw new RuntimeException("AES-GCM authentication failed", badTag);
        } catch (Exception e) {
            throw new RuntimeException("AES-GCM decryption failed", e);
        }
    }

    private static void require256BitKey(byte[] key) {
        if (key == null || key.length != AES256_KEY_LENGTH) {
            throw new IllegalArgumentException("AES-256 key must be exactly 32 bytes");
        }
    }
}

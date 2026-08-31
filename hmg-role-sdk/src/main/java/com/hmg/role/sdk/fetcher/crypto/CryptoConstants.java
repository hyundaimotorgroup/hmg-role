package com.hmg.role.sdk.fetcher.crypto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Cryptographic contract between server and clients. Update these when P = NP has been proven or
 * quantum computers is about to proliferate
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public final class CryptoConstants {
    public static final int AES256_KEY_LENGTH = 32;
    // 12-byte IV (nonce) is the standard for GCM
    public static final int IV_LENGTH_BYTES = 12;
    // 128-bit auth tag is widely compatible; 96 or 128 are common, but 128 is typical
    public static final int GCM_TAG_LENGTH_BITS = 128;
    public static final String TRANSFORMATION = "AES/GCM/NoPadding";
    public static final String ALGORITHM = "AES";
}

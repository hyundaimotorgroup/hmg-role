package com.hmg.role.sdk.fetcher.security;

import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;

import com.hmg.role.sdk.fetcher.crypto.DataDecryptionService;
import org.junit.jupiter.api.BeforeEach;

// Assume AccessKeyApiClient is in the same package or imported properly
class DecryptorServiceTest {

    private AccessKeyApiClient accessKeyApiClient;
    private DataDecryptionService dataDecryptor;
    private DecryptorService decryptorService;

    @BeforeEach
    void setUp() {
        accessKeyApiClient = mock(AccessKeyApiClient.class);
        dataDecryptor = mock(DataDecryptionService.class);
        decryptorService = new DecryptorService(accessKeyApiClient, dataDecryptor);
    }

    //    @Test
    //    void decrypt_usesFreshKey_thenDelegatesToDataDecryptor() throws Exception {
    //        byte[] rawKey = new byte[32]; // mock AES-256 key bytes
    //        byte[] ivAndCiphertext = "ciphertext".getBytes();
    //        byte[] decrypted = "decrypted".getBytes();
    //
    //        ProjectEncryptionKeyDto dto = new ProjectEncryptionKeyDto();
    //        dto.setEncryptionKey(Base64.getEncoder().encodeToString(rawKey));
    //        dto.setExpiredAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(2));
    //
    //        when(accessKeyApiClient.fetchDecryptionKey()).thenReturn(dto);
    //        when(dataDecryptor.decrypt(ivAndCiphertext, rawKey)).thenReturn(decrypted);
    //
    //        byte[] result = decryptorService.decrypt(ivAndCiphertext);
    //
    //        assertArrayEquals(decrypted, result);
    //        verify(accessKeyApiClient, times(1)).fetchDecryptionKey();
    //        verify(dataDecryptor, times(1)).decrypt(ivAndCiphertext, rawKey);
    //    }

    //    @Test
    //    void decrypt_refreshesWhenExpired() throws Exception {
    //        byte[] rawKey = new byte[32];
    //        byte[] ivAndCiphertext = "ciphertext".getBytes();
    //        byte[] decrypted = "decrypted".getBytes();
    //
    //        // First call: expired (now - 1 minute)
    //        ProjectEncryptionKeyDto expired = new ProjectEncryptionKeyDto();
    //        expired.setEncryptionKey(Base64.getEncoder().encodeToString(rawKey));
    //        expired.setExpiredAfter(ZonedDateTime.now(ZoneOffset.UTC).minusMinutes(1));
    //
    //        // Second call: fresh
    //        ProjectEncryptionKeyDto fresh = new ProjectEncryptionKeyDto();
    //        fresh.setEncryptionKey(Base64.getEncoder().encodeToString(rawKey));
    //        fresh.setExpiredAfter(ZonedDateTime.now(ZoneOffset.UTC).plusHours(2));
    //
    //        when(accessKeyApiClient.fetchDecryptionKey())
    //                .thenReturn(expired) // first ensureKeyFresh -> refresh
    //                .thenReturn(fresh); // second ensureKeyFresh -> refresh again
    //
    //        when(dataDecryptor.decrypt(ivAndCiphertext, rawKey)).thenReturn(decrypted);
    //
    //        // First decrypt triggers refresh with expired dto, then immediately considered
    // expired ->
    //        // second refresh
    //        byte[] result = decryptorService.decrypt(ivAndCiphertext);
    //
    //        assertArrayEquals(decrypted, result);
    //        verify(accessKeyApiClient, atLeastOnce()).fetchDecryptionKey();
    //        verify(dataDecryptor).decrypt(ivAndCiphertext, rawKey);
    //    }
}

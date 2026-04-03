package com.workflow.common.object.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * AES encrypt/decrypt for persisted workflow transaction payloads, using a symmetric key from the configured JKS.
 * Note: format differs from any prior institution-specific libraries; existing ciphertext in the database is not
 * automatically migrated.
 */
@Component
@Slf4j
public class SecureData {

    private static final String AES = "AES/CBC/PKCS5Padding";
    private static final int IV_LEN = 16;

    private static final String[] SYMBOL_LIST = new String[]{"/", "&", "?", "+", "-", "\r", "\n"};
    private static final String[] SYMBOL_DESC_LIST =
            new String[]{"_slash_", "_and_", "_que_", "_add_", "_min_", "_endL1_", "_endL2_"};

    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKey secretKey;

    @Value("${jks.store.location}")
    private String jksStoreLocation;

    @Value("${jks.key.alias}")
    private String jksKeyAlias;

    @Value("${jks.storepass}")
    private String jksStorePass;

    @Value("${jks.keypass}")
    private String jksKeyPass;

    private SecretKey getSecretKey() {
        if (secretKey != null) {
            return secretKey;
        }
        KeyStore ks;
        try {
            ks = KeyStore.getInstance("JKS");
        } catch (Exception e) {
            throw new IllegalStateException("JKS key store type not available", e);
        }
        try (InputStream jksInputStream =
                     Thread.currentThread().getContextClassLoader().getResourceAsStream(jksStoreLocation)) {
            if (jksInputStream == null) {
                throw new IllegalStateException("JKS not found on classpath: " + jksStoreLocation);
            }
            ks.load(jksInputStream, jksStorePass.toCharArray());
            secretKey = (SecretKey) ks.getKey(jksKeyAlias, jksKeyPass.toCharArray());
            return secretKey;
        } catch (Exception e) {
            throw new IllegalStateException("Failed to load secret key from JKS", e);
        }
    }

    public String encrypt(final String dataToEncrypt) {
        try {
            byte[] iv = new byte[IV_LEN];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey(), new IvParameterSpec(iv));
            byte[] cipherBytes = cipher.doFinal(dataToEncrypt.getBytes(StandardCharsets.UTF_8));
            byte[] combined = new byte[iv.length + cipherBytes.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(cipherBytes, 0, combined, iv.length, cipherBytes.length);
            return specialCharacterEncode(Base64.getEncoder().encodeToString(combined));
        } catch (Exception e) {
            log.error("encrypt failed: {}", e.getMessage());
            throw new IllegalStateException("Exception while encrypting value", e);
        }
    }

    public String decrypt(final String dataToDecrypt) {
        try {
            String decoded = specialCharacterDecode(dataToDecrypt);
            byte[] combined = Base64.getDecoder().decode(decoded);
            if (combined.length < IV_LEN) {
                throw new IllegalArgumentException("invalid ciphertext");
            }
            byte[] iv = new byte[IV_LEN];
            System.arraycopy(combined, 0, iv, 0, IV_LEN);
            byte[] cipherBytes = new byte[combined.length - IV_LEN];
            System.arraycopy(combined, IV_LEN, cipherBytes, 0, cipherBytes.length);
            Cipher cipher = Cipher.getInstance(AES);
            cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), new IvParameterSpec(iv));
            return new String(cipher.doFinal(cipherBytes), StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("decrypt failed: {}", e.getMessage());
            throw new IllegalStateException("Exception while decrypting value", e);
        }
    }

    private static String specialCharacterEncode(final String input) {
        return StringUtils.replaceEach(input, SYMBOL_LIST, SYMBOL_DESC_LIST);
    }

    private static String specialCharacterDecode(final String input) {
        return StringUtils.replaceEach(input, SYMBOL_DESC_LIST, SYMBOL_LIST);
    }
}

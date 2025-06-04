package com.github.pw2712gz.authbackend.util;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * Utility class for parsing PEM-encoded RSA keys.
 */
public class PemUtils {

    private static final String BEGIN_PUBLIC = "-----BEGIN PUBLIC KEY-----";
    private static final String END_PUBLIC = "-----END PUBLIC KEY-----";
    private static final String BEGIN_PRIVATE = "-----BEGIN PRIVATE KEY-----";
    private static final String END_PRIVATE = "-----END PRIVATE KEY-----";

    /**
     * Parses a PEM-formatted RSA public key into a PublicKey instance.
     */
    public static PublicKey parsePublicKey(String pem) throws Exception {
        String clean = pem.replace(BEGIN_PUBLIC, "")
                .replace(END_PUBLIC, "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(clean);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePublic(spec);
    }

    /**
     * Parses a PEM-formatted RSA private key into a PrivateKey instance.
     */
    public static PrivateKey parsePrivateKey(String pem) throws Exception {
        String clean = pem.replace(BEGIN_PRIVATE, "")
                .replace(END_PRIVATE, "")
                .replaceAll("\\s+", "");

        byte[] decoded = Base64.getDecoder().decode(clean);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        return KeyFactory.getInstance("RSA").generatePrivate(spec);
    }
}

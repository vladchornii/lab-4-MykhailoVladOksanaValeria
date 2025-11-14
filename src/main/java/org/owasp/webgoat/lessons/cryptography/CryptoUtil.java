/*
 * SPDX-FileCopyrightText: Copyright © 2019 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.cryptography;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.RSAKeyGenParameterSpec;
import java.util.Base64;
import javax.xml.bind.DatatypeConverter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CryptoUtil {

    private static final BigInteger[] FERMAT_PRIMES = {
            BigInteger.valueOf(3),
            BigInteger.valueOf(5),
            BigInteger.valueOf(17),
            BigInteger.valueOf(257),
            BigInteger.valueOf(65537)
    };

    public static KeyPair generateKeyPair()
            throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        RSAKeyGenParameterSpec kpgSpec =
                new RSAKeyGenParameterSpec(
                        2048, FERMAT_PRIMES[new SecureRandom().nextInt(FERMAT_PRIMES.length)]);
        keyPairGenerator.initialize(kpgSpec);
        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Повертає приватний ключ у вигляді Base64-рядка (без PEM-хедерів).
     */
    public static String getPrivateKeyInPEM(KeyPair keyPair) {
        return Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded());
    }

    public static String signMessage(String message, PrivateKey privateKey) {

        log.debug("start signMessage");
        String signature = null;

        try {
            Signature instance = Signature.getInstance("SHA256withRSA");
            instance.initSign(privateKey);
            instance.update(message.getBytes(StandardCharsets.UTF_8));

            signature = Base64.getEncoder()
                    .encodeToString(instance.sign());

            log.info("signed the signature with result: {}", signature);
        } catch (Exception e) {
            log.error("Signature signing failed", e);
        }

        log.debug("end signMessage");
        return signature;
    }

    public static boolean verifyMessage(
            String message, String base64EncSignature, PublicKey publicKey) {

        log.debug("start verifyMessage");
        boolean result = false;

        try {
            base64EncSignature =
                    base64EncSignature.replace("\r", "").replace("\n", "").replace(" ", "");

            byte[] decodedSignature = Base64.getDecoder().decode(base64EncSignature);

            Signature instance = Signature.getInstance("SHA256withRSA");
            instance.initVerify(publicKey);
            instance.update(message.getBytes(StandardCharsets.UTF_8));

            result = instance.verify(decodedSignature);

            log.info("Verified the signature with result: {}", result);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
        }

        log.debug("end verifyMessage");
        return result;
    }

    public static boolean verifyAssignment(String modulus, String signature, PublicKey publicKey) {

        boolean result = false;

        if (modulus != null && signature != null) {
            result = verifyMessage(modulus, signature, publicKey);

            RSAPublicKey rsaPubKey = (RSAPublicKey) publicKey;
            if (modulus.length() == 512) {
                modulus = "00".concat(modulus);
            }
            result =
                    result
                            && DatatypeConverter.printHexBinary(rsaPubKey.getModulus().toByteArray())
                            .equals(modulus.toUpperCase());
        }
        return result;
    }

    /**
     * Відновлює приватний ключ із Base64-представлення (без PEM-хедерів).
     */
    public static PrivateKey getPrivateKeyFromPEM(String privateKeyBase64)
            throws NoSuchAlgorithmException, InvalidKeySpecException {

        byte[] decoded = Base64.getDecoder().decode(privateKeyBase64);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(spec);
    }
}

/*
 *   Copyright © 2025 PSPDFKit GmbH. All rights reserved.
 *
 *   The PSPDFKit Sample applications are licensed with a modified BSD license.
 *   Please see License for details. This notice may not be removed from this file.
 */

package com.pspdfkit.instant.example.utils;

import android.content.Context;
import android.util.Base64;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Date;
import java.util.Map;

public class JwtGenerator {
    private final Context context;

    public JwtGenerator(Context context) {
        this.context = context;
    }

    /**
     * Generates a JWT token with the RS256 algorithm using a private key from the assets folder.
     * @param claims Map of claims to include in the JWT.
     * @param privateKeyPath Path to the .pem file in the assets folder.
     * @return The generated JWT token.
     * @throws RuntimeException if the token generation fails.
     */
    public String generateJwtToken(Map<String, Object> claims, String privateKeyPath) {
        if (privateKeyPath == null) {
            privateKeyPath = "keys/jwt.pem"; // Default path in assets
        }
        try {
            // Load the private key from the specified path in assets
            PrivateKey privateKey = loadPrivateKeyFromAssets(privateKeyPath);
            // Build and sign the JWT token
            return Jwts.builder()
                    .setClaims(claims)
                    .setIssuedAt(new Date())
                    .setExpiration(new Date(System.currentTimeMillis() + 60 * 60 * 1000)) // 1 hour expiry
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Loads a private key from a .pem file in the assets folder.
     * @param path The path to the .pem file in the assets folder.
     * @return The loaded PrivateKey object.
     * @throws RuntimeException if the private key loading fails.
     */
    private PrivateKey loadPrivateKeyFromAssets(String path) {
        try {
            // Read the PEM file from assets
            String privateKeyPEM = new BufferedReader(
                            new InputStreamReader(context.getAssets().open(path)))
                    .lines()
                    .reduce("", (accumulator, actual) -> accumulator + actual + "\n");
            // Remove PEM headers and whitespace
            String keyString = privateKeyPEM
                    .replace("-----BEGIN RSA PRIVATE KEY-----", "")
                    .replace("-----END RSA PRIVATE KEY-----", "")
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replace("\n", "")
                    .replace("\r", "")
                    .trim();

            // Decode the base64 encoded key and generate the PrivateKey object
            byte[] keyBytes = Base64.decode(keyString, Base64.DEFAULT);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key from assets", e);
        }
    }

    public static String generateJwtToken(Context context, Map<String, Object> claims, String privateKeyPath) {
        return new JwtGenerator(context).generateJwtToken(claims, privateKeyPath);
    }
}

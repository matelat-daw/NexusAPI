package com.futureprograms.NexusAPI.security;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class AspNetIdentityPasswordVerifier {
    public static String hashPassword(String password) throws Exception {
        int prf = 2; // 2 = HMACSHA512
        int iterCount = 10000;
        int saltLen = 16;
        int subKeyLen = 32;

        byte[] salt = new byte[saltLen];
        new SecureRandom().nextBytes(salt);

        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterCount, subKeyLen * 8);
        SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
        byte[] subKey = skf.generateSecret(spec).getEncoded();

        ByteBuffer buffer = ByteBuffer.allocate(1 + 4 + 4 + 4 + saltLen + subKeyLen);
        buffer.put((byte)0x01); // formato
        buffer.putInt(prf);
        buffer.putInt(iterCount);
        buffer.putInt(saltLen);
        buffer.put(salt);
        buffer.put(subKey);

        return Base64.getEncoder().encodeToString(buffer.array());
    }

    public static boolean verifyPassword(String password, String identityHash) {
        try {
            byte[] hashBytes = Base64.getDecoder().decode(identityHash);
            if (hashBytes[0] != 0x01) return false;

            int prf = ByteBuffer.wrap(hashBytes, 1, 4).getInt();
            System.out.println("Valor de prf: " + prf);
            int iterCount = ByteBuffer.wrap(hashBytes, 5, 4).getInt();
            int saltLen = ByteBuffer.wrap(hashBytes, 9, 4).getInt();

            byte[] salt = new byte[saltLen];
            System.arraycopy(hashBytes, 13, salt, 0, saltLen);

            int subKeyLen = hashBytes.length - 13 - saltLen;
            byte[] expectedSubKey = new byte[subKeyLen];
            System.arraycopy(hashBytes, 13 + saltLen, expectedSubKey, 0, subKeyLen);

            String algorithm = "PBKDF2WithHmacSHA512";

            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, iterCount, subKeyLen * 8);
            SecretKeyFactory skf = SecretKeyFactory.getInstance(algorithm);
            byte[] actualSubKey = skf.generateSecret(spec).getEncoded();

            if (actualSubKey.length != expectedSubKey.length) return false;
            for (int i = 0; i < actualSubKey.length; i++) {
                if (actualSubKey[i] != expectedSubKey[i]) return false;
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }
}
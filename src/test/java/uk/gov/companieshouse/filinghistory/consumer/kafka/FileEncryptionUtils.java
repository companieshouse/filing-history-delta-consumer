package uk.gov.companieshouse.filinghistory.consumer.kafka;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class FileEncryptionUtils {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String FACTORY_INSTANCE = "PBKDF2WithHmacSHA256";
    private static final int TAG_LENGTH_BIT = 128;
    private static final int IV_LENGTH_BYTE = 12;
    private static final int SALT_LENGTH_BYTE = 16;
    private static final String ALGORITHM_TYPE = "AES";
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;
    private static final String ENCRYPTED_EXT = ".enc";

    public static void encryptFile(String password, File inputFile) {
        try {
            byte[] salt = getRandomNonce(SALT_LENGTH_BYTE);
            SecretKey secretKey = getSecretKey(password, salt);

            byte[] iv = getRandomNonce(IV_LENGTH_BYTE);
            Cipher cipher = initCipher(Cipher.ENCRYPT_MODE, secretKey, iv);

            byte[] content = Files.readAllBytes(inputFile.toPath());
            byte[] encryptedBytes = cipher.doFinal(content);
            byte[] cipherBytes = ByteBuffer.allocate(iv.length + salt.length + encryptedBytes.length)
                    .put(iv)
                    .put(salt)
                    .put(encryptedBytes)
                    .array();

            File encryptedFile = new File(inputFile.getPath() + ENCRYPTED_EXT);
            try (FileOutputStream outputStream = new FileOutputStream(encryptedFile)) {
                outputStream.write(cipherBytes);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException("Invalid cipher configuration", e);
        }
    }

    public static String decryptFile(String password, File encryptedFile) {
        try {
            byte[] encryptedBytes = Files.readAllBytes(encryptedFile.toPath());
            ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedBytes);

            byte[] iv = new byte[IV_LENGTH_BYTE];
            byteBuffer.get(iv);

            byte[] salt = new byte[SALT_LENGTH_BYTE];
            byteBuffer.get(salt);

            byte[] contentBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(contentBytes);

            SecretKey secretKey = getSecretKey(password, salt);
            Cipher cipher = initCipher(Cipher.DECRYPT_MODE, secretKey, iv);

            byte[] decryptedMessageByte = cipher.doFinal(contentBytes);
            return new String(decryptedMessageByte, UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new IllegalArgumentException("Invalid cipher configuration", e);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static byte[] getRandomNonce(int length) {
        byte[] nonce = new byte[length];
        new SecureRandom().nextBytes(nonce);
        return nonce;
    }

    private static SecretKey getSecretKey(String password, byte[] salt) {
        try {
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
            SecretKeyFactory factory = SecretKeyFactory.getInstance(FACTORY_INSTANCE);

            return new SecretKeySpec(factory.generateSecret(spec).getEncoded(), ALGORITHM_TYPE);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("Invalid encryption algorithm configuration", e);
        }
    }

    private static Cipher initCipher(int mode, SecretKey secretKey, byte[] iv) {
        try {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(mode, secretKey, new GCMParameterSpec(TAG_LENGTH_BIT, iv));

            return cipher;
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidAlgorithmParameterException |
                 InvalidKeyException e) {
            throw new IllegalArgumentException("Invalid cipher configuration", e);
        }
    }
}
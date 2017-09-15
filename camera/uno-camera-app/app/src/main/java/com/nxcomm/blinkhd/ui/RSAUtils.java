package com.nxcomm.blinkhd.ui;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class RSAUtils {

  public static byte[] encryptRSA(byte[] publicKey, byte[] plainData) throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

    PublicKey enKey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(publicKey));
    final Cipher enCipher = Cipher.getInstance("RSA");
    enCipher.init(Cipher.ENCRYPT_MODE, enKey);
    byte[] cipherData = enCipher.doFinal(plainData);

    return cipherData;

  }

  public static byte[] decryptRSA(byte[] privateKey, byte[] encryptData)

      throws InvalidKeySpecException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

    PrivateKey deKey = KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(privateKey));
    final Cipher cipher = Cipher.getInstance("RSA");
    cipher.init(Cipher.DECRYPT_MODE, deKey);
    byte[] plainData = cipher.doFinal(encryptData);
    return plainData;

  }
}

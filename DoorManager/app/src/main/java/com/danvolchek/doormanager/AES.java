package com.danvolchek.doormanager;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

//This file is adapted from https://stackoverflow.com/questions/6788018/android-encryption-decryption-using-aes/6788456#6788456 and https://stackoverflow.com/questions/20888851/does-aes-cbc-really-requires-iv-parameter
class AES {
    private byte[] key;
    private byte[] iv;

    AES(byte[] key, byte[] iv){
        this.key = key;
        this.iv = iv;
    }

    byte[] encrypt(byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(this.key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        IvParameterSpec ivParams = new IvParameterSpec(iv);
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParams);
        return cipher.doFinal(clear);
    }
}

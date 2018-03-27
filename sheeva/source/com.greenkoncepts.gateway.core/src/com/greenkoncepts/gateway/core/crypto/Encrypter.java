package com.greenkoncepts.gateway.core.crypto;

import java.security.MessageDigest;
import java.security.spec.KeySpec;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Encrypter {
  private KeySpec keySpec;
  private SecretKey key;
  private IvParameterSpec iv;
  private Logger mLogger = LoggerFactory.getLogger(getClass().getSimpleName());
  
  public Encrypter(String keyString, String ivString) {
    try {
      final MessageDigest md = MessageDigest.getInstance("md5");
      final byte[] digestOfPassword = md.digest(Base64.decodeBase64(keyString.getBytes("utf-8")));
      final byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);
      for (int j = 0, k = 16; j < 8;) {
        keyBytes[k++] = keyBytes[j++];
      }
      
      keySpec = new DESedeKeySpec(keyBytes);
      
      key = SecretKeyFactory.getInstance("DESede").generateSecret(keySpec);
      
      iv = new IvParameterSpec(ivString.getBytes());
    } catch(Exception e) {
      mLogger.error("Exception", e);
    }
  }
  
  public String encrypt(String value) {
    try {
      Cipher ecipher = Cipher.getInstance("DESede/CBC/PKCS5Padding","SunJCE");
      ecipher.init(Cipher.ENCRYPT_MODE, key, iv);
      
      if(value==null)
        return null;
      
      // Encode the string into bytes using utf-8
      byte[] utf8 = value.getBytes("UTF8");
      
      // Encrypt
      byte[] enc = ecipher.doFinal(utf8);
      
      // Encode bytes to base64 to get a string
      return new String(Base64.encodeBase64(enc),"UTF-8");
    } catch (Exception e) {
      mLogger.error("Exception", e);
    }
    return null;
  }
  
  public String decrypt(String value) {
    try {
      Cipher dcipher = Cipher.getInstance("DESede/CBC/PKCS5Padding","SunJCE");
      dcipher.init(Cipher.DECRYPT_MODE, key, iv);
      
      if(value==null)
        return null;
      
      // Decode base64 to get bytes
      byte[] dec = Base64.decodeBase64(value.getBytes());
      
      // Decrypt
      byte[] utf8 = dcipher.doFinal(dec);
      
      // Decode using utf-8
      return new String(utf8, "UTF8");
    } catch (Exception e) {
      mLogger.error("Exception", e);
    }
    return null;
  }
  
  public static void main (String[] args) {
	  //byte[] bytes={0,1,0,2,0,3,0,4,0,5,0,6,0,7,0,8};
	  //	IvParameterSpec iv = new IvParameterSpec(bytes);
	  //	System.out.println(iv.getIV().toString());
//	    Encrypter test = new Encrypter("kem gateway","[B@21b6d");
//	    String en = test.encrypt("noCandy4u");
//	    System.out.println(en);
//	    String de = test.decrypt(en);
//	    System.out.println(de);
  }
} 

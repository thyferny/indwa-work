/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.  
 * ValidatorUtility
 * Mar 27, 2012
 */
package com.alpine.license.validator;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

/**
 * @author Gary
 *
 */
public class LiecnseDecryptor {
	public static final String ENCRYPT_KEY = "Pig&Mars-Alpine-Solutions-2010R15";
	public static final String SEPARATOR_MARK = ";;";
	public static final String ENCODE = "UTF-8";
//	public static final String PRODUCT_NAME = "AlpineIlluminator";
	public static final String IGNORE = "-1";

	private static Cipher getCipher(int mode,String productName){
		Cipher pbeCipher = null;
		try {
			PBEKeySpec pbeKeySpec = new PBEKeySpec(ENCRYPT_KEY.toCharArray());
			PBEParameterSpec pbeParamSpec = new PBEParameterSpec(productName.getBytes("UTF-8"), 17);
			SecretKeyFactory keyFac = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
			SecretKey pbeKey = keyFac.generateSecret(pbeKeySpec);
			pbeCipher = Cipher.getInstance("PBEWithSHA1AndDESede");
			pbeCipher.init(mode, pbeKey, pbeParamSpec);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return pbeCipher;
	}
	public static void main(String[] args) throws Exception {
		System.out.println(encrypt("AlpineIlluminator"));
	}
	public static String encrypt(String productName) throws Exception{
		String license = "Pig&Mars-Alpine-Solutions-2010R15;;AlpineIlluminator;;3;;2099-07-01;;1357617291218;;-1;;3";
		byte[] keybyte = license.getBytes(ENCODE);
		byte[] encrypted = getCipher(Cipher.ENCRYPT_MODE,productName).doFinal(keybyte);
		byte[] keybyteBase64 = Base64.encodeBase64(encrypted);
		return new String(keybyteBase64, ENCODE);
	}
	
	public static String decrypt(String key,String productName) throws Exception{
		byte[] keybyteBase64 = key.getBytes(ENCODE);
		byte[] keybyte = Base64.decodeBase64(keybyteBase64);
		byte[] decrypted = getCipher(Cipher.DECRYPT_MODE,productName).doFinal(keybyte);
//		getCipher(Cipher.DECRYPT_MODE,productName).
		return new String(decrypted, ENCODE);
	}
}

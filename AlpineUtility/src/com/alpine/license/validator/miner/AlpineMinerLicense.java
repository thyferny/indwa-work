/**
 * ClassName AlpineLicense.java
 *
 * Version information: 1.00
 *
 * Data: 2010-5-28
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.license.validator.miner;

import java.net.SocketException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import org.apache.commons.codec.binary.Base64;

import com.alpine.license.validator.LiecnseDecryptor;
import com.alpine.license.validator.illuminator.ILicenseValidator.ValidationResult;
import com.alpine.resources.LanguagePackUtility;
import org.apache.log4j.Logger;

public class AlpineMinerLicense {
    private static final Logger itsLogger = Logger.getLogger(AlpineMinerLicense.class);
    private static final SimpleDateFormat SDF = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	private static final String TRAIL_EXPIRE_DAY = "2013-06-30";
	private String encryptkey = "Pig&Mars-Alpine-Solutions-2010R15";
	private String alpineKey = "AlpinePM";
	public static String time_mark = "::";
	public static String separator_mark = "|";
	public static String File_Name = ".ald.bin";
	private long ExpiredTime =  0;  
	private String trialkey = "AlpineMiner 90days Trial License";
	private String username = null;
	private String companyname = null;
 
	private ArrayList<String> multipleMacAddr = new ArrayList<String>();
 	                         

	private PBEKeySpec pbeKeySpec;
	private PBEParameterSpec pbeParamSpec;
	private SecretKeyFactory keyFac;
	private SecretKey pbeKey;
	private Cipher pbeCipher;
	private String macAddr;

	private void setUp(int mode) {
		try {
			pbeKeySpec = new PBEKeySpec(encryptkey.toCharArray());
			pbeParamSpec = new PBEParameterSpec(alpineKey.getBytes("UTF-8"), 17);
			keyFac = SecretKeyFactory.getInstance("PBEWithSHA1AndDESede");
			pbeKey = keyFac.generateSecret(pbeKeySpec);
			pbeCipher = Cipher.getInstance("PBEWithSHA1AndDESede");
			if (mode == Cipher.ENCRYPT_MODE) {
				pbeCipher.init(mode, pbeKey, pbeParamSpec);
			} else {
				pbeCipher.init(mode, pbeKey, pbeParamSpec);
			}
			
		} catch (Exception e) {
			itsLogger.error(e.getMessage(),e);
		
		}
	}

	public AlpineMinerLicense(String user, String company) throws Exception {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ExpiredTime=dateFormat.parse(TRAIL_EXPIRE_DAY).getTime();
		this.username = user;
		this.companyname = company;
		try {
			this.multipleMacAddr = MacAddressUtil.getMacAddress();
		} catch (SocketException e) {
			itsLogger.error(e.getMessage(),e);
		
		} 		
	}


	private String decrypt(String key) throws Exception {
		byte[] keybyteBase64 = key.getBytes("UTF-8");
		byte[] keybyte = Base64.decodeBase64(keybyteBase64);
		byte[] decrypted = pbeCipher.doFinal(keybyte);
		String decryptedStr = new String(decrypted);
		return decryptedStr;
	}

	public ValidationResult validLicense(String key) {
		ValidationResult result = null;
	 
		if(validateOldLicense(key) == false){
			try{
			  result = validateNewLicense(key);
			}catch(Exception e){
				return ValidationResult.UNKNOWN_LICENSE;
			}
		}else{
			result = 	ValidationResult.PASS;
		}
		return result ;
	}

	//user + company + 	  expireDate + macAddress;
	private ValidationResult validateNewLicense(String license) {
		
		ValidationResult valid = null;
		try {
			String[] comboKey = LiecnseDecryptor.decrypt(license,alpineKey).split(LiecnseDecryptor.SEPARATOR_MARK);
			String user = comboKey[2];
			String company= comboKey[3];
			String expireDate= comboKey[4];
			String macAddress= comboKey[5];
			 
			if(LiecnseDecryptor.ENCRYPT_KEY.equals(comboKey[0])
					&& alpineKey.equals(comboKey[1])){//basic info passed validate
				if(LiecnseDecryptor.IGNORE.equals( user)==false&&user.equals(username)==false ){
					valid = ValidationResult.USER_UNMATCH;
					valid.setMessage(LanguagePackUtility.USER_UNMATCH);					 
				}else if(LiecnseDecryptor.IGNORE.equals( company)==false&&company.equals(companyname)==false ){
					valid = ValidationResult.COMPANY_UNMATCH ;
					valid.setMessage(LanguagePackUtility.COMPANY_UNMATCH);
				}else if(LiecnseDecryptor.IGNORE.equals( macAddress)==false  && false==MacAddressUtil.isAVlidateMacAddress(macAddress)){
					valid = ValidationResult.MACHINE_UNMATCHED;
					valid.setMessage(LanguagePackUtility.MACHINE_UNMATCHED);
				}
				else if(LiecnseDecryptor.IGNORE.equals( expireDate)==false  && true==isExpire(expireDate)){
					valid = ValidationResult.EXPIRED ;
					valid.setMessage(LanguagePackUtility.EXPIRED_LICENSE + expireDate);
				}
				if(valid == null){
					valid = ValidationResult.PASS;
					valid.setMessage("");
				}
			}else{
				valid = ValidationResult.UNKNOWN_LICENSE;
				valid.setMessage("");
			}
		} catch (Exception e) {
			e.printStackTrace();
			valid = ValidationResult.UNKNOWN_LICENSE;
			valid.setMessage("");
		}
		return valid;
 
	}

	private boolean validateOldLicense(String key) {
		boolean valid = false;
		try {
			setUp(Cipher.DECRYPT_MODE);
			String combokey = decrypt(key);

			int timeIndex = combokey.lastIndexOf(AlpineMinerLicense.time_mark);
			int companyIndex = combokey
					.lastIndexOf(AlpineMinerLicense.separator_mark);

			if (timeIndex != -1) {

				String time = combokey.substring(timeIndex
						+ AlpineMinerLicense.time_mark.length(), combokey.length());
				long installed = Long.valueOf(time).longValue();

				if (companyIndex != 1) {
					String firstPartKey = combokey.substring(0, companyIndex);

					String alpine = combokey.substring(companyIndex
							+ AlpineMinerLicense.separator_mark.length(), timeIndex);

					if (alpine.contains(alpineKey)) {

						String firstPart = decrypt(firstPartKey);
						// Official License key
						if (firstPart.contains(this.encryptkey)
								&& firstPart.contains(username)
								&& firstPart.contains(companyname)){
							for (String addr: multipleMacAddr) {
								if (firstPart.contains(addr)) {
									valid = true;
									break;
								}else	if (addr!=null&&firstPart.contains(addr.toUpperCase())) {
									valid = true;
									break;
								}
								else	if (addr!=null&&firstPart.contains(addr.toUpperCase().replace(":", "-"))) {
									valid = true;
									break;
								}
							}
							
						} else {
							long current = System.currentTimeMillis();
							if (current <= installed) {
								valid = true;
							}
						}

					}
				}
			}
		} catch (Exception e) {
			itsLogger.error(e.toString());
		}
		return valid;
	}
	

	
	 
	//
		public AlpineMinerLicense(String user, String company, String macAddr) {
			this.username = user;
			this.companyname = company;
			this.macAddr = macAddr;
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			try {
				ExpiredTime=dateFormat.parse(TRAIL_EXPIRE_DAY).getTime();
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		public String getLicenseKey() throws Exception {
			setUp(Cipher.ENCRYPT_MODE);
			String key = username + companyname + this.encryptkey + macAddr;
			String firstPartKey = encrypt(key);
			String combokey = firstPartKey + AlpineMinerLicense.separator_mark
					+ alpineKey + AlpineMinerLicense.time_mark
					+ System.currentTimeMillis();
			return encrypt(combokey);
		}

		public String getTrialKey() throws Exception {
			setUp(Cipher.ENCRYPT_MODE);
			String firstPartKey = encrypt(trialkey);
			long stamp =   ExpiredTime; // Time bomb
	
			String combokey = firstPartKey + AlpineMinerLicense.separator_mark
					+ alpineKey + AlpineMinerLicense.time_mark + stamp;
			return encrypt(combokey);
		}

		private String encrypt(String key) throws Exception {
			byte[] lkey = pbeCipher.doFinal(key.getBytes("UTF-8"));
			byte[] lkeyBase64 = Base64.encodeBase64(lkey);
			String base64Encoded = new String(lkeyBase64, "UTF-8");
			return base64Encoded;
		} 
	

		public long getTrialLicenseExpiredTime() {
	 
			return ExpiredTime;
		}
		private static boolean isExpire(String deadline){
			Date deadlineDate = null;
			try {
				deadlineDate = SDF.parse(deadline + " 23:59:59");
			} catch (ParseException e) {
				return true;
			}
			return new Date().getTime() > deadlineDate.getTime();
		}
}

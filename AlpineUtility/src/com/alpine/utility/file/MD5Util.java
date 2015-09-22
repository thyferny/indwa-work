/**
 * ClassName MD5Util.java
 *
 * Version information: 1.00
 *
 * Data: 2011-5-30
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.codec.binary.Hex;

import org.apache.log4j.Logger;

public class MD5Util {
    private static final Logger itsLogger = Logger.getLogger(MD5Util.class);
		static MessageDigest md = null;

		static {

			try {
				md = MessageDigest.getInstance("MD5");
			} catch (NoSuchAlgorithmException ne) {
				itsLogger.error(ne.getMessage(),ne);
			}

		}

		public static String md5(File f) {
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(f);
				byte[] buffer = new byte[8192];
				int length;
				while ((length = fis.read(buffer)) != -1) {
					md.update(buffer, 0, length);
				}
				return new String(Hex.encodeHex(md.digest()));

			} catch (FileNotFoundException e) {
                itsLogger.error("md5 file " + f.getAbsolutePath() + " failed:"
                        + e.getMessage());
				return null;

			} catch (IOException e) {
                itsLogger.error("md5 file " + f.getAbsolutePath() + " failed:"
						+ e.getMessage());
				return null;
			} finally {
				try {
					if (fis != null)
						fis.close();
				} catch (IOException e) {
                    itsLogger.error(e.getMessage(),e);

                }

			}

		}
	}


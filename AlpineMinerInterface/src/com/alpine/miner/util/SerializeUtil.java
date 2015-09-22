/**
 * ClassName CommonMethod.java
 *
 * Version information: 1.00
 *
 * Data: 2011-3-31
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.util;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import sun.misc.BASE64Decoder;

import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

public class SerializeUtil {
    private static final Logger itsLogger=Logger.getLogger(SerializeUtil.class);

    public static Object stringToObject(String str) {
		BASE64Decoder decode = new BASE64Decoder();

		Object out = null;
		if (str != null) {
			try {		
				ByteArrayInputStream bios = new ByteArrayInputStream(decode.decodeBuffer(str));
				ObjectInputStream ois = new ObjectInputStream(bios);
				out = ois.readObject();
			} catch (Exception e) {
				itsLogger.error(StringUtil.class.getName()+"\n"+e.toString());
				return null;
			}
		}
		return out;
	}
}

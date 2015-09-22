package com.alpine.utility.file;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.log4j.Logger;

public class AlpineFileUtility {
	private static final String TAB = "\t";
	private static Logger itsLogger = Logger.getLogger(AlpineFileUtility.class);

	public enum DELIMETERS {
		COMMA, DOT, TAB, SPACE;

		private static final Map<DELIMETERS, String> DELIMS;

		static {
			DELIMS = new HashMap<DELIMETERS, String>();
			DELIMS.put(TAB, "\t");
			DELIMS.put(COMMA, ",");
			DELIMS.put(SPACE, " ");
		}

		public static String getDelimeter(DELIMETERS d) {
			return DELIMS.get(d);
		}

		public String getDelimeter() {
			return DELIMS.get(this);
		}

	}

	public static <T> String serializeUsingJAXB(T entity) throws JAXBException {
		if (null == entity) {
			throw new IllegalArgumentException(
					"Obejct that is getting serialized is null");
		}
		JAXBContext context = JAXBContext.newInstance(entity.getClass());
		Marshaller m = context.createMarshaller();
		m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

		StringWriter writer = new StringWriter();
		m.marshal(entity, writer);
		return writer.toString();
	}

	public static <T> T deserializeStringXMLRepIntoObject(Class<T> clazz,
			String xml) {
		try {

			File file = new File(xml);
			JAXBContext context = JAXBContext.newInstance(clazz);
			Unmarshaller um = context.createUnmarshaller();
			Object obj = um.unmarshal(file);
			try {
				return clazz.cast(obj);
			} catch (ClassCastException exc) {
				throw new RuntimeException("Expected class " + clazz + " but was " + obj.getClass(), exc);
			}
		} catch (JAXBException exc) {
			throw new RuntimeException("Error unmarshalling XML response", exc);
		}
	}

	public static String[] readAndConvertDataForPigResultSet(String filePath) {
		return readTheLinesOfTheFileIntoPigData(filePath, "((", "))");
	}
	
	public static String[] readAndConvertDataForPigResultSetOne(String filePath) {
		return readTheLinesOfTheFileIntoPigData(filePath, "(", ")");
	}

	public static String[] readTheLines(String filePath) {
		return readTheLinesOfTheFileIntoPigData(filePath, null, null);
	}

	public static String[] readTheLinesOfTheFileIntoPigData(String filePath,
			String lineStart, String lineEnd) {
		if (null == filePath || "".equals(filePath.trim())) {
			throw new IllegalArgumentException(
					"make sure file name and delimeters are valid");
		}
		List<String> lines = new ArrayList<String>();
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("File line is:" + strLine);
				}
				lines.add(((null == lineStart) ? "" : lineStart) + strLine
						+ (((lineEnd == null) ? "" : lineEnd)));
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			String errString = "Could not parse the file due to:";
			itsLogger.error(errString, e);
			throw new RuntimeException(errString, e);
		}
		return lines.toArray(new String[] {});
	}

	public static String[] readTheLinesOfTheFileIntoPigData(String filePath,
			String delimeter) {
		if (null == filePath || "".equals(filePath.trim()) || null == delimeter
				|| "".equals(delimeter.trim())) {
			throw new IllegalArgumentException(
					"make sure file name and delimeters are valid");
		}
		List<String> lines = new ArrayList<String>();
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("File line is:" + strLine);
				}
				lines.add(strLine);
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("New line is:" + strLine);
				}
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			String errString = "Could not parse the file due to:";
			itsLogger.error(errString, e);
			throw new RuntimeException(errString, e);
		}
		return lines.toArray(new String[] {});
	}

	public static String[] readTheLinesOfTheFile(String filePath) {
		return readTheLinesOfTheFile(filePath, null, null);
	}

	/**
	 * Description of public static String[] convertIntoPigScript(String ...
	 * script) The method is designed to convert the lines into valid
	 * alpinePigscript that we can use
	 * 
	 * @param String
	 *            ... script: Raw alpinePig source
	 * @return Valid AlpinePig script
	 * 
	 */
	public static String[] convertIntoPigScript(String... script) {
		// Assuming # and -- are comments
		// Assuming \ at the end of a line is suggesting that line continue
		if (null == script) {
			throw new IllegalArgumentException("Script is null");
		}
		List<String> lines = new ArrayList<String>(script.length);
		String previousLine = null;
		boolean isCommented = false;
		for (String line : script) {
			line = line.replace(TAB, "");
			if (null == line || line.trim().equals("") || line.startsWith("--")) {
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("Line[" + line + "] is a comment line");
				}
				continue;
			}
			String trimmed = line.trim();
			if (trimmed.startsWith("/*") && trimmed.endsWith("*/")) {
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("Line is a comment line that is[" + line
							+ "]");
				}
				continue;
			}
			if (trimmed.startsWith("/*")) {
				if (isCommented) {
					throw new IllegalArgumentException(
							"Got multiple comment line inside of each other and we do not support that");
				}
				isCommented = true;
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("Line is a comment line that is[" + line
							+ "]");
				}
				continue;
			}

			if (isCommented) {
				if (trimmed.endsWith("*/")) {
					isCommented = false;
				}
				continue;
			}

			if (line.endsWith("\\")) {
				previousLine = ((null == previousLine) ? "" : previousLine)
						+ line.substring(0, line.length() - 1);
				continue;
			}
			if (null != previousLine) {
				lines.add(previousLine + line);
				previousLine = null;
				continue;
			}

			lines.add(line);
		}

		if (null != previousLine) {
			lines.add(previousLine);
		}

		return lines.toArray(new String[] {});
	}

	public static String[] readAlpinePigScript(InputStream inputStream) {
		return convertIntoPigScript(readTheLinesOfTheFile(inputStream));
	}

	public static String[] readTheLinesOfTheFile(InputStream inputStream) {
		if (null == inputStream) {
			String errMessage = "Input stream is null.. ";
			itsLogger.error(errMessage);
			throw new IllegalArgumentException(errMessage);
		}
		List<String> lines = new ArrayList<String>();
		try {

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(inputStream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("File line is:" + strLine);
				}
				lines.add(strLine);
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			String errString = "Could not parse the file due to:";
			itsLogger.error(errString, e);
			throw new RuntimeException(errString, e);
		}
		return lines.toArray(new String[] {});

	}

	public static String[] readTheLinesOfTheFile(String filePath,
			String lineStart, String lineEnd) {
		if (null == filePath || "".equals(filePath.trim())) {
			throw new IllegalArgumentException(
					"make sure file name and delimeters are valid");
		}
		List<String> lines = new ArrayList<String>();
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("File line is:" + strLine);
				}
				lines.add(((null == lineStart) ? "" : lineStart) + strLine
						+ ((lineEnd == null) ? "" : lineEnd));
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			String errString = "Could not parse the file due to:";
			itsLogger.error(errString, e);
			throw new RuntimeException(errString, e);
		}
		return lines.toArray(new String[] {});
	}

	public static String[] readTheLinesOfTheFileIntoPigDataAndReplaceOriginalFileDelim(
			String filePath, String orgDelim, String reqDelimeter) {
		if (null == filePath || "".equals(filePath.trim()) || null == orgDelim
				|| "".equals(orgDelim.trim()) || null == orgDelim
				|| "".equals(orgDelim.trim())) {
			throw new IllegalArgumentException(
					"make sure file name and delimeters are valid");
		}
		List<String> lines = new ArrayList<String>();
		try {
			// Open the file that is the first
			// command line parameter
			FileInputStream fstream = new FileInputStream(filePath);

			// Get the object of DataInputStream
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			String strLine;

			// Read File Line By Line
			while ((strLine = br.readLine()) != null) {
				// Print the content on the console
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("File line is:" + strLine);
				}
				String newLine = strLine.replace(orgDelim, reqDelimeter);
				lines.add(newLine);
				if (itsLogger.isDebugEnabled()) {
					itsLogger.debug("New line is:" + strLine);
				}
			}
			// Close the input stream
			in.close();
		} catch (Exception e) {// Catch exception if any
			String errString = "Could not parse the file due to:";
			itsLogger.error(errString, e);
			throw new RuntimeException(errString, e);
		}
		return lines.toArray(new String[] {});
	}

}
/**
 * Classname FileUtility.java
 *
 * Version information:1.00
 *
 * Data:2010-6-3
 *
 * COPYRIGHT (C) 2010 Alpine Solution. All rights Reserved
 */
package com.alpine.utility.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author Administrator
 * 
 */
public class FileUtility {

	/**
	 * @param file
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	public static StringBuffer readFiletoString(File file)
			throws FileNotFoundException, IOException {
		FileInputStream fileInputStream = new FileInputStream(file);
		StringBuffer propsStr = readStreamToString(fileInputStream);
		return propsStr;
	}

	public static StringBuffer readStreamToString(InputStream fileInputStream)
			throws IOException {
		StringBuffer propsStr = new StringBuffer();
		byte[] buffer = new byte[1024];
		while (true) {
			if (fileInputStream.available() < 1024) {
				int remain;
				while ((remain = fileInputStream.read()) != -1) {
					propsStr = propsStr.append(new String(
							new byte[] { (byte) remain }));
				}
				break;
			} else {
				fileInputStream.read(buffer);
				propsStr = propsStr.append(new String(buffer));
			}
		}
		fileInputStream.close();
		return propsStr;
	}

	public static String copy(File sourceFile, String tartgetDirectory)
			throws Exception {

		if (sourceFile.exists() == false) {
			throw new RuntimeException("Source file does not exists:"
					+ sourceFile.getAbsolutePath());

		}

		File targetDir = new File(tartgetDirectory);

		if (targetDir.exists() == false) {
			targetDir.mkdir();
		}

		String newFilePathName = tartgetDirectory + File.separator
				+ sourceFile.getName();

		return copyFile(sourceFile, newFilePathName);

	}

	/**
	 * @param sourceFile
	 * @param newFilePathName
	 * @throws IOException
	 */
	public static String copyFile(File sourceFile, String newFilePathName)
			throws IOException {
		File targetFile = new File(newFilePathName);
		if (targetFile.exists()
				&& targetFile.toURI().equals(sourceFile.toURI())) {
			// source = target need not copy...
			return newFilePathName;
		}
		int bytesum = 0;
		int byteread = 0;
		// File oldfile = new File(oldPath);
		if (sourceFile.exists()) {
			InputStream inStream = null;
			FileOutputStream fs = null;
			try {
				inStream = new FileInputStream(sourceFile);

				fs = new FileOutputStream(newFilePathName);

				byte[] buffer = new byte[1444];
				while ((byteread = inStream.read(buffer)) != -1) {
					bytesum += byteread;

					fs.write(buffer, 0, byteread);
				}
				return newFilePathName;

			} catch (Exception e) {
				e.printStackTrace();

			} finally {
				inStream.close();
				fs.close();
				fs.flush();
			}
		}
		return null;
	}

	/**
	 * @param exeFilePathName
	 * @param exeFileContent
	 * @throws IOException
	 */
	public static void writeFile(String filePathName, String fileContent)
			throws IOException {

		File file = new File(filePathName);
		FileOutputStream iStream = null;
		try {
			iStream = new FileOutputStream(file);
			iStream.write(fileContent.getBytes());
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			iStream.close();
		}

	}

	/**
	 * @param rootDir
	 */
	public static void cleanDir(File rootDir) {
		File files[] = rootDir.listFiles();
		if (files.length == 0)
			return;
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				files[i].delete();
			} else if (files[i].isDirectory()) {
				cleanDir(files[i]);
				files[i].delete();
			}
		}

		// TODO Auto-generated method stub

	}

	/**
	 * @param tempDir
	 * @param jarFileNames
	 * @param classDir
	 * @throws IOException
	 */
	public static void extractJarFiles(String tempDir,
			List<String> jarFileNames, String classDir) throws IOException {
		for (Iterator iterator = jarFileNames.iterator(); iterator.hasNext();) {
			String jarFileName = (String) iterator.next();
			String jarFilePath = tempDir + File.separator + jarFileName;
			extractJarFile(jarFilePath, classDir);

		}

	}

	/**
	 * @param jarFilePath
	 * @param tempDir
	 * @throws IOException
	 */
	public static void extractJarFile(String jarFilePath, String targetDir)
			throws IOException {
		JarFile jarFile = new JarFile(jarFilePath);
		Enumeration<JarEntry> entries = jarFile.entries();
		while (entries.hasMoreElements()) {
			JarEntry jarEntry = (JarEntry) entries.nextElement();

			String fileName = jarEntry.getName();

			File file = new File(targetDir + File.separator + fileName);
			if (jarEntry.isDirectory()) {

				if (file.exists() == false) {
					file.mkdir();
				}
			}

		}
		entries = jarFile.entries();

		while (entries.hasMoreElements()) {
			JarEntry jarEntry = (JarEntry) entries.nextElement();

			String fileName = jarEntry.getName();

			File file = new File(targetDir + File.separator + fileName);
			if (jarEntry.isDirectory() == false) {

				FileOutputStream fileOutPutStream = new FileOutputStream(file);
				InputStream inStream = jarFile.getInputStream(jarEntry);
				try {
					copy(inStream, fileOutPutStream);
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					inStream.close();
					fileOutPutStream.close();
				}

			}
			//

		}

		// InputStream in = jarFile.getInputStream(
		// jarFile.getEntry("META-INF/MANIFEST.MF"));

		// TODO Auto-generated method stub

	}

	/**
	 * @param inStream
	 * @param fileOutPutStream
	 * @throws IOException
	 */
	private static void copy(InputStream inStream,
			FileOutputStream fileOutPutStream) throws IOException {
		byte[] buffer = new byte[1444];
		int bytesum = 0;
		int byteread = 0;
		while ((byteread = inStream.read(buffer)) != -1) {
			bytesum += byteread;

			fileOutPutStream.write(buffer, 0, byteread);
		}

	}

	public static void appendContent(File configFile, String content) {
		// create a temp file
		// write the file into tempfile,
		// write the string into tempfile

		java.io.FileWriter fw = null;
		java.io.PrintWriter pw = null;
		try {
			fw = new java.io.FileWriter(configFile, true);
			pw = new java.io.PrintWriter(fw);
			pw.println(content);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (pw != null)
				pw.close();
			if (fw != null)
				try {
					fw.close();
				} catch (IOException e) {
				}
		}

	}

	public static String getFileSeparator() {
		String operatSystem = System.getProperty("os.name");
		if (operatSystem.startsWith("Windows")) {
			return File.separator + File.separator;
		} else if (operatSystem.startsWith("Linux")) {
			return File.separator;
		} else if (operatSystem.startsWith("Mac OS")) {
			return File.separator;
		} else {
			return File.separator;
		}
	}

	public static String getFileNameFromPath(String filePath) {
		int startIndex = filePath.lastIndexOf(File.separator) + 1;
		return filePath.substring(startIndex, filePath.length());

	}

	public static void copy(File sourceFile, String tartgetDirectory, boolean overwrite) throws IOException { 
		if (sourceFile.exists() == false) {
			throw new RuntimeException("Source file does not exists:"
					+ sourceFile.getAbsolutePath());

		}

		File targetDir = new File(tartgetDirectory);

	

		String newFilePathName = tartgetDirectory + File.separator
				+ sourceFile.getName();
		if (overwrite==false &&new File(newFilePathName).exists()==true){
			return;
		}
		
			
		if (targetDir.exists() == false) {
				targetDir.mkdir();
		}
		
		copyFile(sourceFile, newFilePathName);
		
		
	}
}

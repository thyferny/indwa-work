/**
/**
 * ClassName XmlDocManager.java
 *
 * Version information: 1.00
 *
 * Data: 2010-3-26
 *
 * COPYRIGHT   2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.utility.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.codec.binary.Base64;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.apache.log4j.Logger;

/**
 * 
 * @author John Zhao
 * 
 */
public class XmlDocManager {
    private static final Logger itsLogger = Logger.getLogger(XmlDocManager.class);
    public static String PASSWORD = "password";

	public static String OUTPUT_TABLE = "outputTable";
	public static String OUTPUT_FILE = "resultsName";
	public static String SELECTED_OUTPUT_TABLE = "selectedTable";
	public static String SELECTED_OUTPUT_FILE = "selectedFile";
	public static String SVD_UmatrixTable = "UmatrixTable";
	public static String SVD_VmatrixTable = "VmatrixTable";
	public static String SVD_SmatrixTable = "singularValueTable";
	public static String SVD_UmatrixTableFull = "UmatrixTableF";
	public static String SVD_VmatrixTableFull = "VmatrixTableF";
	public static String SVD_SmatrixTableFull = "singularValueTableF";
	public static String PCA_outputTable = "PCAQoutputTable";
	public static String PCA_QvalueOutputTable = "PCAQvalueOutputTable";
	public static String PR_CustomerTable = "customerTable";
	public static String PR_SelectionTable = "selectionTable";
	public static String PRE_RecommendationTable = "recommendationTable";
	public static String PRE_PreTable = "preTable";
	public static String PRE_PostTable = "postTable";
	public static String PLDA_PLDAModelOutputTable = "PLDAModelOutputTable";
	public static String PLDA_TopicOutTable = "topicOutTable";
	public static String PLDA_DocTopicOutTable = "docTopicOutTable";
	public static String PLDA_PLDADocTopicOutputTable= "PLDADocTopicOutputTable";
	public static String NAME_HD_copyToTableName= "copyToTableName";
	
	public static String TABLE_NAME = "tableName";
	private Document xmlDoc = null;
	private Node root = null;
	public static final String ENCODING_UTF8 = "UTF-8";

	public static List<String> OUTPUTTABLElIST = new ArrayList<String>();
	static {
		OUTPUTTABLElIST.add(OUTPUT_TABLE);
		OUTPUTTABLElIST.add(SVD_UmatrixTable);
		OUTPUTTABLElIST.add(SVD_VmatrixTable);
		OUTPUTTABLElIST.add(SVD_SmatrixTable);
		OUTPUTTABLElIST.add(SVD_UmatrixTableFull);
		OUTPUTTABLElIST.add(SVD_VmatrixTableFull);
		OUTPUTTABLElIST.add(SVD_SmatrixTableFull);
		OUTPUTTABLElIST.add(PCA_outputTable);
		OUTPUTTABLElIST.add(PCA_QvalueOutputTable);
		OUTPUTTABLElIST.add(PR_CustomerTable);
		OUTPUTTABLElIST.add(PR_SelectionTable);
		OUTPUTTABLElIST.add(PRE_RecommendationTable);
		OUTPUTTABLElIST.add(PRE_PreTable);
		OUTPUTTABLElIST.add(PRE_PostTable);
		OUTPUTTABLElIST.add(PLDA_PLDAModelOutputTable);
		OUTPUTTABLElIST.add(PLDA_TopicOutTable);
		OUTPUTTABLElIST.add(PLDA_DocTopicOutTable);
		OUTPUTTABLElIST.add(PLDA_PLDADocTopicOutputTable);
		OUTPUTTABLElIST.add(NAME_HD_copyToTableName);
		OUTPUTTABLElIST.add(OUTPUT_FILE);
	}

	/**
	 * create xml document object
	 * 
	 * @throws ParserConfigurationException
	 */
	public void createXmlDoc() throws ParserConfigurationException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();
		xmlDoc = docBuilder.newDocument();
	}

	/**
	 * create document root node
	 * 
	 * @param rootElementName
	 */
	public void createRootNode(String rootElementName) {
		if (root == null) {
			root = xmlDoc.createElement(rootElementName);
			xmlDoc.appendChild(root);
		}
	}

	public Node getRootNode() {
		return root;
	}

	public Document getXmlDoc() {
		return xmlDoc;
	}

	public Node createNewNodeAt(String nodeName, Node parentNode) {
		Element node = xmlDoc.createElement(nodeName);
		parentNode.appendChild(node);
		return node;
	}

	/**
	 * translate document to string
	 * 
	 * @param node
	 * @return
	 */
	public String xmlToString(Node node) {
		if (node == null) {
			node = xmlDoc;
		}
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (Exception e) {
            itsLogger.error(e);
		}
		return null;
	}


	public void parseXMLFile(String fileName)
			throws ParserConfigurationException, SAXException, IOException {
		File file = new File(fileName);
		parseXMLFile(file);
	}

	public void parseXMLFile(File file) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		xmlDoc = db.parse(file);
		root = xmlDoc.getDocumentElement();
	}

	public void parseXMLFile(InputStream is) throws ParserConfigurationException,
			SAXException, IOException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		xmlDoc = db.parse(is);
		root = xmlDoc.getDocumentElement();
	}

	public ArrayList<Node> getNodeListByTag(String tag) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		NodeList catNodeList = xmlDoc.getElementsByTagName(tag);
		if(catNodeList!=null){
			for (int i = 0; i < catNodeList.getLength(); i++) {
				nodeList.add(catNodeList.item(i));
			}
		}
		return nodeList;
	}

	public String getElementValue(Node node, String tag) {
		String value = null;
		NodeList nodeList = node.getChildNodes();
		for (int i = 0; i < nodeList.getLength(); i++) {
			Node childNode = nodeList.item(i);
			if (childNode.getNodeName().equals(tag)) {
				value = childNode.getTextContent();
				break;
			}
		}
		return value;
	}

	public ArrayList<Node> getNodeList(Node node, String tag) {
		ArrayList<Node> nodeList = new ArrayList<Node>();
		NodeList childNodeList = node.getChildNodes();
		for (int i = 0; i < childNodeList.getLength(); i++) {
			Node childNode = childNodeList.item(i);
			if (childNode.getNodeName().equals(tag)) {
				nodeList.add(childNode);
			}
		}
		return nodeList;
	}

	public void saveXmlFile(File file) throws IOException {
		BufferedWriter writer = null;
		writer = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(file), ENCODING_UTF8));
		writer.write(xmlToString(null));
		writer.close();
	}

	public String xmlToStringEnglish(Node node) {
		if (node == null) {
			node = xmlDoc;
		}
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (Exception e) {
			itsLogger.error(e);
		}
		return null;
	}

	public String xmlToLocalString(Node node) {
		return xmlToStringEnglish(node);
	}

	public String getLocalEncoding() {
		return ENCODING_UTF8;
	}

	public static String encryptedPassword(String password) {
		try {
			byte[] lkey = password.getBytes("UTF-8");
			byte[] lkeyBase64 = Base64.encodeBase64(lkey);
			String base64Encoded = new String(lkeyBase64, "UTF-8");
			return base64Encoded;
		} catch (Exception e) {
			e.printStackTrace();
			return password;
		}
	}

	public static String decryptedPassword(String password) {
		try {
			byte[] lkey = password.getBytes("UTF-8");
			byte[] lkeyBase64 = Base64.decodeBase64(lkey);
			String base64Encoded = new String(lkeyBase64, "UTF-8");
			return base64Encoded;
		} catch (Exception e) {
			e.printStackTrace();
			return password;
		}
	}

	public static void addOutputList(String output) {
		if (!OUTPUTTABLElIST.contains(output)) {
			OUTPUTTABLElIST.add(output);
		}
	}

}

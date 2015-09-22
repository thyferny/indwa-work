/**
 * ClassName  DataTableHTMLExporter.java
 *
 * Version information: 1.00
 *
 * Data: 2010-6-4
 *
 * COPYRIGHT (C) 2010 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.datamining.workflow.exporter.html;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.sql.Connection;
import java.util.List;
import java.util.Locale;

 

import com.alpine.datamining.api.VisualizationOutPut;
import com.alpine.datamining.api.impl.db.DataBaseInfo;
import com.alpine.datamining.api.impl.db.cluster.KMeansAnalyzer;
import com.alpine.datamining.api.impl.visual.DataTableVisualizationOutPut;
import com.alpine.datamining.api.impl.visual.resource.VisualLanguagePack;
import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.datamining.workflow.util.DBTableCSVWiter;
import com.alpine.datamining.workflow.util.ToHtmlWriter;
import com.alpine.miner.view.ui.dataset.TableEntity;
import com.alpine.utility.db.AlpineUtil;
import com.alpine.utility.file.StringUtil;
import com.alpine.utility.hadoop.HadoopFile;
import org.apache.log4j.Logger;
import com.alpine.utility.profile.ProfileUtility;
import com.alpine.utility.tools.ProfileReader;
import com.alpine.utility.tools.StringHandler;

/**
 * @author John Zhao
 * 
 */
public class DataTableHTMLExporter implements VisualOutPutHTMLExporter {
    private static final Logger itsLogger =Logger.getLogger(DataTableHTMLExporter.class);
    // only for real table...
	public static final int MAX_ROWS = 200;
	// for all table
	public static final int MAX_COLS = 6;
	private boolean isKmeansFirstTable;
	
	private final static Locale locale=Locale.getDefault();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.alpine.datamining.exporter.VisualOutPutExporter#export(com.alpine
	 * .datamining.api.VisualizationOutPut)
	 */
	@Override
	public StringBuffer export(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList, String rootPath) throws Exception {
		DataTableVisualizationOutPut out = (DataTableVisualizationOutPut) visualizationOutPut;
		StringBuffer result = new StringBuffer();
		TableEntity tableEntity = out.getTableEntity();
		// real table , into csv file...
		if (out.getTableName() != null
				&& out.getTableName().trim().length() > 0) {

			DataBaseInfo dbinfo = out.getDbInfo();
			if(dbinfo!=null){
				Connection connection = AnalyticResultHTMLExporter.connectionMap
						.get(dbinfo.getUrl());
				if (connection == null) {

					connection = AlpineUtil.createConnection(  dbinfo.getUserName(), dbinfo
							.getPassword(), dbinfo.getUrl(), dbinfo.getSystem(),Locale.getDefault(),dbinfo.getUseSSL());
					AnalyticResultHTMLExporter.connectionMap.put(dbinfo.getUrl(),
							connection);
				}
			}
			String csvFileName=null;
			if(StringUtil.isEmpty(out.getSchemaName())){
				csvFileName="-" + getCleanName( StringHandler.removeDoubleQ(out.getTableName()))
						+ ".csv";
			}else{
				csvFileName="-" + getCleanName(visualizationOutPut.getName())
				+ "-" + getCleanName( StringHandler.removeDoubleQ(out.getSchemaName())) + "-" +
						getCleanName( StringHandler.removeDoubleQ(out.getTableName()))
				+ ".csv";
			}
			
			String relativeRootPath=null;
			if(rootPath.startsWith("./")){//batch run
				relativeRootPath=rootPath+csvFileName;
			}else{
				String filePathWithoutFileName=rootPath.substring(0, rootPath.lastIndexOf(File.separator));
				String fileName=rootPath.substring(rootPath.lastIndexOf(File.separator)+1, rootPath.length());
				String folderName=filePathWithoutFileName.substring(filePathWithoutFileName.lastIndexOf(File.separator)+1, filePathWithoutFileName.length());
				relativeRootPath="./"+File.separator+folderName+File.separator+fileName+csvFileName;
			}
	
			String filePath = rootPath + csvFileName;

			filePath = DBTableCSVWiter.write(filePath, tableEntity.getColumn(),tableEntity.getItem());
			result.append(createCSVTableOverView(out, relativeRootPath));
			return result;
			// CSVWiter
		} else {// pdf table, but kemans are special...
			ToHtmlWriter htmlWriter=new ToHtmlWriter();
			exportToHtml(visualizationOutPut, tempFileList, out, result,
					tableEntity,htmlWriter,rootPath);
			return htmlWriter.toStringBuffer();
		}

		
	}

	//pivotal :38560435
	/***
	 * Caused by: java.io.FileNotFoundException: ./random_sampling.afm-2012-10-29-14-35/random_sampling_resource/content.html-/tmp/pdefault_prefix_rsamp_0_1-/tmp/pdefault_prefix_rsamp_0_1-/tmp/pdefault_prefix_rsamp_0_1.csv (No such file or directory)
	at java.io.FileOutputStream.open(Native Method)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:179)
	at java.io.FileOutputStream.<init>(FileOutputStream.java:131)
	at com.alpine.datamining.workflow.util.DBTableCSVWiter.write(DBTableCSVWiter.java:43)
	... 12 more
 
	 */
	private String getCleanName(String name) {
		if (name.lastIndexOf(File.separator)>-1){
			return name.substring(name.lastIndexOf(File.separator)+1,name.length());
		}
		else if (name.lastIndexOf(HadoopFile.SEPARATOR)>-1){
			return name.substring(name.lastIndexOf(HadoopFile.SEPARATOR)+1,name.length());
		}
		else{
			return name;
		}
	}

	protected void exportToHtml(VisualizationOutPut visualizationOutPut,
			List<String> tempFileList, DataTableVisualizationOutPut out,
			StringBuffer result, TableEntity tableEntity,ToHtmlWriter htmlWriter
			,String rootPath)
			throws MalformedURLException, IOException {
		String[] columns = tableEntity.getColumn();
		List<String[]> items = tableEntity.getItem();

		// show all columns...
		int rowNumbers = items.size();
		int colNumbers = columns.length;
//		StringBuffer t = new StringBuffer();
		boolean more_than_max_rows = false;

		// only table need the limitation...
		if (rowNumbers > MAX_ROWS && out.getTableName() != null) {
			more_than_max_rows = true;
			rowNumbers = MAX_ROWS;
		}

		// can only have header...so need not the row numbers >0
		if (colNumbers > 0) {
			// 6 columns split!
			int splitLevel = ((colNumbers - 1) / MAX_COLS) + 1;

			if (isKMeans(visualizationOutPut)) {
				int purlCol = colNumbers - 2;
				splitLevel = ((purlCol - 1) / (MAX_COLS - 2)) + 1;
				colNumbers = colNumbers + (splitLevel - 1) * 2;
			}
			if (more_than_max_rows == true) {
				htmlWriter.writeP(getMoreThanMAXRowsParagrap(out).toString());
//				result.append(getMoreThanMAXRowsParagrap(out));
			}
//			result.append(createTableOverView(out, colNumbers, rowNumbers));
			htmlWriter.writeP(createTableOverView(out, colNumbers, rowNumbers).toString());

			isKmeansFirstTable = true;
			for (int i = 0; i < splitLevel; i++) {
				if (i > 0) {
					isKmeansFirstTable = false;
				}
//				t.append("<table>");
//				t.append(drawTable(colNumbers, i + 1, rowNumbers, tableEntity,
//						tempFileList, visualizationOutPut));
//				t.append("</table>");

				if (splitLevel > 1) {
					int indexTo = (i + 1) * MAX_COLS;
					if (indexTo > colNumbers) {
						indexTo = colNumbers;
					}

					String content = WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Columns_From,locale)
							+ " " + String.valueOf(i * MAX_COLS + 1) + " "
							+ WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Columns_To ,locale)+ " "
							+ String.valueOf(indexTo) + ":";
//					result.append("<p>").append(content).append("</p>");
					htmlWriter.writeP(content);

				}
				htmlWriter.writeTable(drawTable(colNumbers, i + 1, rowNumbers, tableEntity,
						tempFileList, visualizationOutPut,rootPath).toString());
//				if (t != null) {
//					result.append(t);
//				}

			}

		}
	}

	/**
	 * @param out
	 * @param filePath
	 * @return
	 */
	private StringBuffer createCSVTableOverView(
			DataTableVisualizationOutPut out, String filePath) {
		String overView = null;
		if(StringUtil.isEmpty(out.getSchemaName())){
			overView = WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.CSVFile_OVerView,locale).replace("{1}", out.getTableName())
					.replace(
							"{2}",
							String.valueOf(ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT)));
		}else{
			overView = WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.CSVTable_OVerView,locale).replace("{1}",
					out.getSchemaName()).replace("{2}", out.getTableName())
					.replace(
							"{3}",
							String.valueOf(ProfileReader.getInstance().getParameter(ProfileUtility.UI_TABLE_LIMIT)));
		}

		ToHtmlWriter htmlWriter=new ToHtmlWriter();
		ToHtmlWriter linkWriter=new ToHtmlWriter();
		linkWriter.writeLink(filePath);
		htmlWriter.writeP(overView+linkWriter.toString());

		return htmlWriter.toStringBuffer();

	}

	/**
	 * @param out
	 * @param colNumbers
	 * @param rowNumbers
	 * @return
	 */
	private StringBuffer createTableOverView(DataTableVisualizationOutPut out,
			int colNumbers, int rowNumbers) {
		String overView = "";
		if (out.getTableName() != null) {// means a real table
			if (out.getSchemaName() != null
					&& out.getSchemaName().trim().length() > 0) {
				overView = overView + WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Name,locale)
						+ out.getSchemaName() + "." + out.getTableName() + ", ";
			} else {
				overView = overView + WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Table_Name,locale)
						+ out.getTableName() + ", ";

			}

		}
		overView = overView + WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Columns_numbers,locale) + colNumbers
				+ ", " + WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Row_numbers,locale) + (rowNumbers-1) + "\n";
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(overView);
		return strBuf;
	}

	/**
	 * @param colNumbers
	 * @param splitLevel
	 *            - form 1 ++
	 * @param rowNumbers
	 * @param tableEntity
	 * @param tempFileList
	 * @param visualizationOutPut
	 * @throws BadElementException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private StringBuffer drawTable(int colNumbers, int splitLevel,
			int rowNumbers, TableEntity tableEntity, List<String> tempFileList,
			VisualizationOutPut visualizationOutPut,String rootPath)
			throws MalformedURLException, IOException {

		String[] columns = tableEntity.getColumn();
		List<String[]> items = tableEntity.getItem();

		// not the last!
		int clos = 0;
		if (colNumbers - MAX_COLS * (splitLevel - 1) > MAX_COLS) {
			// 6
			clos = MAX_COLS;
		} else {// <6
			clos = colNumbers - MAX_COLS * (splitLevel - 1);
		}
		// avoid
		if (colNumbers < 1 || clos < 1) {
			return null;
		}
		int[] colIndexs = new int[clos];
		if (isKMeans(visualizationOutPut)) {
			countKmeansColIndex(splitLevel, colIndexs);
		} else {
			for (int i = 0; i < colIndexs.length; i++) {
				colIndexs[i] = i + MAX_COLS * (splitLevel - 1);
			}
		}

		ToHtmlWriter htmlWriter=new ToHtmlWriter();
//		StringBuffer t = new StringBuffer();

		columns = getSplitedColumns(columns, colIndexs);

		addColumnHeader(columns, htmlWriter);

		// split table...

		for (int i = 0; i < rowNumbers; i++) {

//			if (visualizationOutPut.getAnalyzer() instanceof KMeansAnalyzer
//					&& visualizationOutPut.getName().equals(
//							VisualLanguagePack.CLUSTER_PROFILES)
//					&& display != null) {
//				addKmeansProfilesRow(tableEntity, items.get(i), htmlWriter, colIndexs,
//						i, display, tempFileList, splitLevel,rootPath);
//			} else {
				addRow(items.get(i), htmlWriter, colIndexs);
//			}
		}

		return htmlWriter.toStringBuffer();

	}

	/**
	 * @param splitLevel
	 * @param colIndexs
	 */
	private void countKmeansColIndex(int splitLevel, int[] colIndexs) {
		for (int i = 0; i < colIndexs.length; i++) {
			if (i == 0) {
				colIndexs[i] = 0;
			} else if (i == 1) {
				colIndexs[i] = 1;
			} else {
				if (splitLevel == 1) {
					colIndexs[i] = i;
				} else {
					colIndexs[i] = i + MAX_COLS * (splitLevel - 1) - 2
							* (splitLevel - 1);
				}

			}

		}
	}

	/**
	 * @param visualizationOutPut
	 * @return
	 */
	private boolean isKMeans(VisualizationOutPut visualizationOutPut) {
		return visualizationOutPut.getAnalyzer() instanceof KMeansAnalyzer
				&& visualizationOutPut.getName().equals(
						VisualLanguagePack.CLUSTER_PROFILES);
	}

	/**
	 * @param columns
	 * @param cloIndexs
	 * @return
	 */
	private String[] getSplitedColumns(String[] columns, int[] colIndexs) {
		String[] newColumns = new String[colIndexs.length];
		for (int i = 0; i < newColumns.length; i++) {
			newColumns[i] = columns[colIndexs[i]];
		}

		return newColumns;
	}

	/**
	 * @param tableEntity
	 * @param strings
	 * @param t
	 * @param cloIndexs
	 * @param display
	 * @param tempFileList
	 * @throws BadElementException
	 * @throws IOException
	 * @throws MalformedURLException
	 */
	private void addKmeansProfilesRow(TableEntity tableEntity, String[] row,
			ToHtmlWriter htmlWriter, int[] cloIndexs, int rowNumber, //Display display,
			List<String> tempFileList, int splitLevel,String rootPath)
			throws MalformedURLException, IOException {
		String imageFilePreffix = String.valueOf(System.currentTimeMillis());
		String tempDir = System.getProperty("java.io.tmpdir");
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		if (row.length > MAX_COLS) {
			for (int i = 0; i < cloIndexs.length; i++) {
				// 0 and 1
				if (isKmeansFirstTable == true) {

				}
				int index = cloIndexs[i];
				String value = row[index];
				itsLogger.debug(
						"splitLevel=" + splitLevel + " i=" + i + " rowNumber="
								+ rowNumber + " value=" + value);

				createKMeansRowCell(tableEntity, tdWriter, rowNumber,
						imageFilePreffix, tempDir, i, value, //display,
						tempFileList, splitLevel,rootPath);

			}
		} else {
			for (int i = 0; i < row.length; i++) {
				String value = row[i];

				createKMeansRowCell(tableEntity, tdWriter, rowNumber,
						imageFilePreffix, tempDir, i, value, //display,
						tempFileList, splitLevel,rootPath);

			}
		}
		htmlWriter.writeTR(tdWriter.toString());
	}

	/**
	 * @param tableEntity
	 * @param t
	 * @param rowNumber
	 * @param imageFilePreffix
	 * @param tempDir
	 * @param i
	 * @param value
	 * @param display
	 * @param tempFileList
	 * @throws BadElementException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	private void createKMeansRowCell(TableEntity tableEntity, ToHtmlWriter htmlWriter,
			int rowNumber, String imageFilePreffix, String tempDir, int i,
			String value, //Display display, 
			List<String> tempFileList,
			int splitLevel,String rootPath) throws MalformedURLException, IOException {
		
		int j = rootPath.lastIndexOf(File.separator);
		String curdir = rootPath.substring(0, j);
		String path = curdir + File.separator + imageFilePreffix + "_"
				+ rowNumber + "_" + i + "_" + splitLevel+".jpg";
		tempFileList.add(path);
		if (value != null) {

			if (i == 0) {
				htmlWriter.writeTD(value);
//				t.append("<td>").append(value).append("</td>");

			}
//			else if (i == 1) {
//				Image image = tableEntity.createRandomColorCatetoryImage(
//						rowNumber, display, tableEntity.getMaxCategoryWidth(),
//						value.split(","));
//				addImage(htmlWriter, path, image);
//			} else {// i+1>3, the UI is from 3
//				int index = i + 1;
//				if (isKmeansFirstTable == false) {
//					index = (splitLevel - 1) * MAX_COLS + index
//							- (splitLevel - 1) * 2;
//				}
//				Image image = tableEntity.createCategoryScaleImage(rowNumber,
//						index, display, tableEntity.getMaxCategoryWidth(),
//						value.split(","));
//				// how to add image?
//				addImage(htmlWriter, path, image);
//
//			}

		} else {
			htmlWriter.writeTD("");
//			t.append("<td>").append("").append("</td>");

		}
	}

	/**
	 * @param t
	 * @param path
	 * @param image
	 * @throws BadElementException
	 * @throws MalformedURLException
	 * @throws IOException
	 */
//	private void addImage(ToHtmlWriter htmlWriter, String path, Image image)
//			throws MalformedURLException, IOException {
//		ToHtmlWriter imageWriter=new ToHtmlWriter();
//		saveImage(image, path);
//		imageWriter.writeImg(path);
//		htmlWriter.writeTD(imageWriter.toString());
////		t.append("<p><img src=\"").append(path).append("\"></p>");
//
//	}

	/**
	 * @param image
	 * @param string
	 * @return
	 */
//	private void saveImage(Image image, String path) {
//
//		ImageLoader loader = new ImageLoader();
//		loader.data = new ImageData[] { image.getImageData() };
//		loader.save(path, SWT.IMAGE_JPEG);
//		LogService.getInstance().logDebug("saveImage:path=" + path);
//		// image.dispose();
//
//	}

	/**
	 * @param out
	 * @return
	 */
	private StringBuffer getMoreThanMAXRowsParagrap(
			DataTableVisualizationOutPut out) {
		String content = WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.Too_Many_Rows,locale);
		if (out.getTableName() != null) {
			content = content + WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.See_Table_in_Database,locale)
					+ out.getTableName();
		} else {
			content = content + WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.See_Table_in_WorkBench,locale);
		}
		StringBuffer strBuf = new StringBuffer();
		strBuf.append(content);
		return strBuf;
	}

	private void addColumnHeader(String[] columns, ToHtmlWriter htmlWriter) {
//		t.append("<tr>");
		ToHtmlWriter tdWriter=new ToHtmlWriter();
		for (int i = 0; i < columns.length; i++) {
			tdWriter.writeTD(columns[i]);
//			t.append("<td>").append(columns[i]).append("</td>");
		}
		htmlWriter.writeTR(tdWriter.toString());
//		t.append("</tr>");
	}

	private void addRow(String[] row, ToHtmlWriter htmlWriter, int[] cloIndexs) {
		ToHtmlWriter tdWriter=new ToHtmlWriter();
//		t.append("<tr>");
		for (int i = 0; i < cloIndexs.length; i++) {

			int index = cloIndexs[i];
			if(row.length>index){
			if (row[index] != null)
				tdWriter.writeTD(row[index]);
//				t.append("<td>").append(row[index]).append("</td>");
			else
				tdWriter.writeTD("");
//				t.append("<td>").append("").append("</td>");
			}
		}
		htmlWriter.writeTR(tdWriter.toString());
//		t.append("</tr>");

	}

}

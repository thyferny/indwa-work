/**
 * ClassName :WebFlowReportGenerator.java
 *
 * Version information: 3.0
 *
 * Data: 2011-11-2
 *
 * COPYRIGHT (C) 2011 Alpine Solutions. All Rights Reserved.
 **/
package com.alpine.miner.impls.report.model;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.batik.apps.rasterizer.DestinationType;
import org.apache.batik.apps.rasterizer.SVGConverter;
import org.apache.batik.apps.rasterizer.SVGConverterException;
import org.apache.commons.io.FileUtils;

import com.alpine.datamining.workflow.resources.WorkFlowLanguagePack;
import com.alpine.miner.impls.report.FlowResult;
import com.alpine.miner.impls.report.OperatorOutput;
import com.alpine.miner.impls.report.OperatorResult;
import com.alpine.miner.impls.report.html.HTMLReportGenerator;
import com.alpine.miner.impls.web.resource.PreferenceInfo;
import com.alpine.miner.impls.web.resource.ResourceManager;
import com.alpine.miner.interfaces.TempFileManager;
import com.alpine.miner.interfaces.resource.Persistence;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterFactory;
import com.alpine.miner.workflow.operator.parameter.helper.OperatorParameterHelper;
import com.alpine.utility.file.StringUtil;
import org.apache.log4j.Logger;

/**
 * @author zhaoyong
 *
 */
public class WebFlowReportGenerator {

    private static Logger itsLogger = Logger.getLogger(WebFlowReportGenerator.class);
    private static final String REPORT_ZIP = "flow_report.zip";


    //TODO for nls use...
    private Locale locale;

    //HashMap<OperatorOutput,String> outputIndexMap = new HashMap<OperatorOutput,String>();


    public WebFlowReportGenerator(Locale locale ){
        this.locale = locale;

    }

    public static final String DIR_HTML_RESOURCE = "html_resource";


    private  ReporterModel generateModel(FlowResult report,String resourceDir) throws Exception{

        ReporterModel model= new ReporterModel();
        List<IndexItem> indexList=new ArrayList<IndexItem> ();
        String[][] flowMetaInfo = report.getFlowMetaInfo();
        int i=0;
        String title = WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OverView,locale);
        Chapter chapter = toChapter(title,flowMetaInfo,i);
        model.appendChapter(chapter) ;
        indexList.add(buildIndex(chapter)) ;
        List<OperatorResult> opepratorReports = report.getOperatorResults();

        if(opepratorReports!=null){
            for (Iterator<OperatorResult> iterator = opepratorReports.iterator(); iterator
                    .hasNext();) {
                i=i+1;
                OperatorResult operatorResult = iterator .next();
                chapter = toChapter(operatorResult,i ,resourceDir);
                indexList.add(buildIndex(chapter)) ;
                model.appendChapter(chapter) ;
            }
        }

        model.setIndex(indexList) ;
        return model;
    }

    private IndexItem buildIndex(Chapter chapter) {
        IndexItem index = new IndexItem("index_"+chapter.getId(), chapter.getTitle(), chapter.getId());
        List<Chapter> chapters = chapter.getSubChapters();
        if(chapters!=null){
            for (Iterator iterator = chapters.iterator(); iterator.hasNext();) {
                Chapter subChapter = (Chapter) iterator.next();
                IndexItem item = buildIndex(subChapter) ;
                index.addChild(item) ;
            }
        }

        return index;

    }

    /**
     * @param resourceDir
     * @param index
     * @param operatorResult
     * @return
     * @throws Exception
     */
    private   Chapter toChapter(OperatorResult operatorResult, int index ,String resourceDir ) throws Exception {
        String[][] metaInfo = operatorResult.getNodeMetaInfo();
        OperatorOutput operatorOutput = operatorResult.getOperatorOutput();
        String title = operatorResult.getName();
        //id has blank will cause bad link (MINERWEB-458)
        title=title.trim();
        Chapter operatorChapter  = new Chapter(title,title,String.valueOf(index));
        operatorChapter.appendSubChapter(toNodeInfoChapter(metaInfo,index)) ;
        operatorChapter.appendSubChapter(toInputChapter(operatorResult.getOperatorInput(),index )) ;
        operatorChapter.appendSubChapter(toOutputChapter(operatorOutput,index ,resourceDir)) ;

        operatorChapter.setStyleId(HTMLReportGenerator.CSS_TOP_CHAPTER_TITLE) ;
        return operatorChapter;
    }


    private   Chapter toInputChapter(String[][] operatorInput, int index
    ) {
        Chapter chapter = new Chapter("node_input"+index,
                WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_Input,locale),
                String.valueOf(index));
        chapter.setStyleId(HTMLReportGenerator.CSS_CHAPTER_TITLE);
        //1 source info
        //2 paramter...

        TableParagraph paragraph = new TableParagraph("Input");
        chapter.appendParagraph(paragraph) ;
        paragraph.setStyleId(HTMLReportGenerator.CSS_PROPERTY_TABLE) ;
        addStringPropToRow(operatorInput, paragraph);


        return chapter;
    }

    /**
     * @param operatorOutput
     * @param resourceDir
     * @return
     * @throws Exception
     */
    private   Chapter toOutputChapter(OperatorOutput operatorOutput,int index  ,String resourceDir) throws Exception {
        Chapter nodeInfoChapter = new Chapter("node_output"+index,
                WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OutPut,locale),String.valueOf(index));
        nodeInfoChapter.setStyleId(HTMLReportGenerator.CSS_CHAPTER_TITLE) ;
        fillOutPutChapter(nodeInfoChapter,operatorOutput		,resourceDir	,0);

        return nodeInfoChapter;
    }

    private    void fillOutPutChapter(Chapter chapter,
                                      OperatorOutput operatorOutput ,String resourceDir,int level )
            throws IOException, SVGConverterException {

        if(operatorOutput.getType()==OperatorOutput.TYPE_COMPOSITE){
            if(level==0){
                //addOutPutTitleParagraoh(chapters,operatorOutput.getName(),operatorOutput,level);
                OperatorOutput[] subOutPuts = operatorOutput.getOutPuts();
                if(subOutPuts!=null){
                    for (int j = 0; j < subOutPuts.length; j++) {
                        fillOutPutChapter(chapter,subOutPuts[j],resourceDir,level+1);
                    }

                }
            }else{
                Chapter targeChapter;
                //layerd, skip the empty parent (2 layered)
                if(StringUtil.isEmpty(operatorOutput.getName())){
                    targeChapter=chapter;
                }else{
                    Chapter subchapter = new Chapter(getChapterTitleID(operatorOutput, level),  operatorOutput.getName(), "") ;
                    subchapter.setStyleId(HTMLReportGenerator.CSS_SUB_CHAPTER_PREFIX+level);
                    chapter.appendSubChapter(subchapter);
                    targeChapter= subchapter;


                }
                OperatorOutput[] subOutPuts = operatorOutput.getOutPuts();
                if(subOutPuts!=null){
                    for (int j = 0; j < subOutPuts.length; j++) {
                        fillOutPutChapter(targeChapter,subOutPuts[j],resourceDir,level+1);
                    }

                }
            }
        }

        else{
            Chapter subchapter = new Chapter(getChapterTitleID(operatorOutput, level),  operatorOutput.getName(), "") ;
            subchapter.setStyleId(HTMLReportGenerator.CSS_SUB_CHAPTER_PREFIX+level);
            chapter.appendSubChapter(subchapter);

            if(operatorOutput.getType()==OperatorOutput.TYPE_TEXT){
//			addOutPutTitleParagraoh(chapters,operatorOutput.getName(),operatorOutput,level);
                String text = operatorOutput.getText();
                text=text.replace("\n", "<br/>");

                Paragraph textParagraph =new Paragraph(null,text);
                subchapter.appendParagraph(textParagraph);
                textParagraph.setStyleId(HTMLReportGenerator.CSS_OUTPUT_TEXT);

            }
            else if(operatorOutput.getType()==OperatorOutput.TYPE_DATA_TABLE){
                String[][] tableData = operatorOutput.getTableData();
                if(tableData==null){
                    return;
                }
                //	addOutPutTitleParagraoh(chapters,operatorOutput.getName(),operatorOutput,level);
                List<List<Paragraph>> tableRows = new ArrayList<List<Paragraph>>();
                if(tableData!=null){
                    for (int i = 1; i < tableData.length; i++) {
                        String[] row = tableData[i];
                        List<Paragraph> tableRow = new ArrayList<Paragraph>();
                        if(row!=null){
                            for (int j = 0; j < row.length; j++) {
                                tableRow.add(new Paragraph(row[j])) ;
                            }
                            tableRows.add(tableRow) ;
                        }
                    }
                }
                TableParagraph tableParagraph= new TableParagraph("", null,
                        tableData[0], tableRows);
                tableParagraph.setStyleId(HTMLReportGenerator.CSS_OUTPUT_TABLE) ;
                subchapter.appendParagraph(tableParagraph);
            }
            else if(operatorOutput.getType()==OperatorOutput.TYPE_CHART){
                //addOutPutTitleParagraoh(chapters,operatorOutput.getName(),operatorOutput,level);
                String svg = operatorOutput.getSvg();
                //name can not contains the special char, so use image is most safe, can not use the operator name directly
                String operatorName ="Image";
                if(StringUtil.isEmpty(svg)==false){

                    ImageParagraph svgParagraph = createImageParagraph(resourceDir,
                            svg, operatorName);
                    subchapter.appendParagraph(svgParagraph);
                }
                //special for dojocharting gfx
                String[] svgLegends = operatorOutput.getSvg_legend();
                if(svgLegends!=null&&svgLegends.length>0){
                    TableParagraph legendTable = new TableParagraph(null);
//				legendTable.setTitle(operatorName) ;
                    legendTable.setStyleId(HTMLReportGenerator.CSS_LEGEND_TABLE) ;
                    String[] svgLegendLabels=operatorOutput.getSvg_legend_labels();
                    List<Paragraph> row = new ArrayList<Paragraph> ();
                    for (int i = 0; i < svgLegends.length; i++) {
                        String lsvg = svgLegends[i];
                        String label =svgLegendLabels[i];
                        row.add(createImageParagraph(resourceDir,
                                lsvg, operatorName+i)) ;
                        row.add(new Paragraph(label)) ;

                    }
                    legendTable.appendRow(row);

                    subchapter.appendParagraph(legendTable);
                }



            }else if(operatorOutput.getType()==OperatorOutput.VISUAL_TYPE_SCATT_MATRIX){
                //Add by Will
                //addOutPutTitleParagraoh(chapters,operatorOutput.getName(),operatorOutput,level);
                String[][] tableData = operatorOutput.getTableData();
                String[] tableHeader = operatorOutput.getTableGroupHeader();
                String[][] cellType = operatorOutput.getTableGroupCellType();//text, svg...
                List<List<Paragraph>> tableRows = new ArrayList<List<Paragraph>>();
                ////#MINERWEB-871 begin
                int fractionDigits = 4;
                try {
                    fractionDigits = Integer.parseInt(ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_ALG,PreferenceInfo.KEY_DECIMAL_PRECISION));
                } catch (NumberFormatException e1) {
                    fractionDigits = 4;
                } catch (Exception e1) {
                    fractionDigits = 4;
                }
                MathContext mc = new MathContext(fractionDigits, RoundingMode.HALF_UP);
                //end
                if(tableData!=null&&tableData.length>0){
                    for (int i = 0; i < tableData.length; i++) {
                        List<Paragraph> rowList = new ArrayList<Paragraph>();
                        String[] rowData = tableData[i];
                        if(rowData !=null){
                            for (int j = 0; j < rowData.length; j++) {
                                if(cellType[i][j].equals("svg")==true){
                                    ImageParagraph svgParagraph = createImageParagraph(resourceDir,
                                            rowData[j], i+"_"+j);
                                    rowList.add(svgParagraph);
                                }else{//text
                                    //#MINERWEB-871
                                    String textData = rowData[j];
                                    BigDecimal bdc = null;
                                    try {
                                        bdc = new BigDecimal(textData,mc);
                                        textData = String.valueOf(bdc);
                                    } catch (Exception e) {
                                        textData = textData.length()>15?(textData.substring(0,15)+"..."):textData;
                                    }
                                    rowList.add(new Paragraph(textData)) ;
                                }

                            }
                        }
                        tableRows.add(rowList) ;
                    }
                }


                TableParagraph tableParagraph= new TableParagraph("", null,
                        tableHeader, tableRows);
                tableParagraph.setStyleId(HTMLReportGenerator.CSS_OUTPUT_TABLE4SCATTER) ;
                subchapter.appendParagraph(tableParagraph);
                //create a good table with image ..

            }

            else if(operatorOutput.getType()==OperatorOutput.TYPE_TABLED_GROUP){
                //addOutPutTitleParagraoh(chapters,operatorOutput.getName(),operatorOutput,level);
                String[][] tableData = operatorOutput.getTableData();
                String[] tableHeader = operatorOutput.getTableGroupHeader();
                String[] columnType = operatorOutput.getTableGroupColumnType();//text, svg...
                List<List<Paragraph>> tableRows = new ArrayList<List<Paragraph>>();
                if(tableData!=null&&tableData.length>0){
                    for (int i = 0; i < tableData.length; i++) {
                        List<Paragraph> rowList = new ArrayList<Paragraph>();
                        String[] rowData = tableData[i];
                        if(rowData !=null){
                            for (int j = 0; j < rowData.length; j++) {
                                if(columnType[j].equals("svg")==true){
                                    ImageParagraph svgParagraph = createImageParagraph(resourceDir,
                                            rowData[j], i+"_"+j);
                                    rowList.add(svgParagraph);
                                }else{
                                    rowList.add(new Paragraph(rowData[j])) ;
                                }

                            }
                        }
                        tableRows.add(rowList) ;
                    }
                }


                TableParagraph tableParagraph= new TableParagraph("", null,
                        tableHeader, tableRows);
                tableParagraph.setStyleId(HTMLReportGenerator.CSS_TABLED_GROUP) ;
                subchapter.appendParagraph(tableParagraph);
                //create a good table with image ..

            }
        }
    }

    private String getChapterTitleID(OperatorOutput operatorOutput, int level) {
        if(operatorOutput.getName()!=null){
            return "output_"+operatorOutput.getName().hashCode()+"_"+System.currentTimeMillis()+"_" +Math.random();
        }else {
            return "output_"+System.currentTimeMillis()+"_" +Math.random();
        }
    }

    private ImageParagraph createImageParagraph(String resourceDir, String svg,
                                                String operatorName) throws IOException, SVGConverterException {
        //byte[] bytes = svg.getBytes(Persistence.ENCODING);

        String svgName = operatorName+"_"+System.currentTimeMillis()+".svg";
        String svgPath = resourceDir+File.separator+svgName;
        FileUtils.writeStringToFile(new File(svgPath), svg,Persistence.ENCODING) ;
//		FileUtility.writeFile(svgPath, svg) ;
        SVGConverter svgConverter = new SVGConverter();
        svgConverter.setDestinationType(DestinationType.PNG);
        svgConverter.setSources(new String[]{ new File(svgPath).toURI().toString() });
        String targetImgPath =svgPath+".png";
        svgConverter.setDst(new File(targetImgPath ));
        svgConverter.execute();
        //relative path
        ImageParagraph svgParagraph= new ImageParagraph("", "",svgName+".png");
        svgParagraph.setStyleId(HTMLReportGenerator.CSS_OUTPUT_IMAGE) ;

        return svgParagraph;
    }

    /**
     * @param metaInfo
     * @return
     */
    private   Chapter toNodeInfoChapter(String[][] metaInfo,int index) {
        Chapter nodeInfoChapter = new Chapter("node_overview"+index,
                WorkFlowLanguagePack.getMessage(WorkFlowLanguagePack.AnalyticResultExporter_OverView,locale)
                ,String.valueOf(0));
        nodeInfoChapter.setStyleId(HTMLReportGenerator.CSS_CHAPTER_TITLE);
        TableParagraph nodeInfoParagraph = new TableParagraph("node_overview_paragraph" );

        addStringPropToRow(metaInfo, nodeInfoParagraph);

        nodeInfoChapter.appendParagraph(nodeInfoParagraph);

        return nodeInfoChapter;
    }

    /**
     * @param title
     * @param metaInfo
     * @param index
     * @return
     */
    private   Chapter toChapter(String title ,String[][] metaInfo, int index) {
        //String title = "Overview";
        Chapter flowOverview  = new Chapter("flow_overview",title,String.valueOf(index));
        flowOverview.setStyleId(HTMLReportGenerator.CSS_TOP_CHAPTER_TITLE) ;
        TableParagraph flowInfoTable = new TableParagraph("flow_overview_paragraph");


        addStringPropToRow(metaInfo, flowInfoTable);



        flowOverview.appendParagraph(flowInfoTable) ;
        //flowOverview.appendParagraph(paragraph);
        return flowOverview;
    }


    private   void addStringPropToRow(String[][] metaInfo,
                                      TableParagraph flowInfoTable) {


        if(metaInfo!=null){
            for (int i = 0; i < metaInfo.length; i++) {
                String[] values = metaInfo[i];
                if(values!=null){
                    String value = "";
                    if(values.length==2){
                        value = values[1];

                    }
                    String parameterName = values[0].trim();
                    OperatorParameterHelper paremterHelper = OperatorParameterFactory.INSTANCE.getHelperByParamName(parameterName);
                    String parameterLabel = paremterHelper.getParameterLabel(parameterName, locale);
                    addLabelValueTableRow(flowInfoTable,parameterLabel,value);
                }
            }



        }

    }

    /**
     * @param flowInfoTable
     * @param laebl
     * @param value
     */
    private   void addLabelValueTableRow(TableParagraph flowInfoTable,
                                         String laebl, String value) {
        List<Paragraph> row = Arrays.asList(new Paragraph[]{
                new Paragraph(laebl),new Paragraph(value),
        });
        flowInfoTable.appendRow(row) ;


    }

    public String exportHTMLReport(String flowName,  FlowResult flowResult, boolean sendToChorus) throws Exception,
            IOException {
        String reportFileName = null;
        String reportRootPath = TempFileManager.INSTANCE.getTempFolder4Report();
        String folderName= String.valueOf(System.currentTimeMillis());
        String reportDir = reportRootPath+File.separator+folderName;

//				flowName


        makeDirIfNotExist(reportDir);


        String zipFolder =reportRootPath+File.separator+folderName+"_zip"+File.separator;

        makeDirIfNotExist(zipFolder );

        reportFileName=reportDir+File.separator+"index.html";
        String resourceDir = reportDir+File.separator +DIR_HTML_RESOURCE;
        makeDirIfNotExist(resourceDir);

        ReporterModel reportModel =  generateModel(flowResult,resourceDir );
        boolean isIE = false;
        if(flowResult.isIE()!=null&&flowResult.isIE()!="undefined"){
            isIE=true;
        }
        String contentHtml=HTMLReportGenerator.toContentHtml(reportModel,isIE,locale) ;


        String contentFileName = resourceDir+File.separator+"content.html" ;
        //left tree page
        String indexFileName = resourceDir+File.separator+"index.html" ;
        //report. html
        FileUtils.writeStringToFile(new File(contentFileName), contentHtml, Persistence.ENCODING) ;

        String treeIndexHtml =  HTMLReportGenerator.toIndexHtml(reportModel.getIndex(),isIE,locale) ;

        FileUtils.writeStringToFile(new File(indexFileName), treeIndexHtml,Persistence.ENCODING) ;
        //left frame , content html...
        String reportHtml= HTMLReportGenerator.creatManiPageHtml(DIR_HTML_RESOURCE+File.separator+"index.html",
                DIR_HTML_RESOURCE+File.separator+"content.html","index.html",isIE,locale) ;

        FileUtils.writeStringToFile(new File(reportFileName), reportHtml, Persistence.ENCODING) ;

        //http url
        String outputFileName = TempFileManager.INSTANCE.getTempFolder4Report()
                +File.separator+ folderName+"_zip"+File.separator+REPORT_ZIP;

        if (sendToChorus)
        {
            outputFileName = createChorusFilePath(folderName) + File.separator+REPORT_ZIP;
        }

        //file 				//folder...
        zipFolderToFile(reportDir, outputFileName);
        try{
            FileUtils.deleteDirectory(new File(reportDir ))  ;
        }catch(Exception e){
            e.printStackTrace();
            //nothing to do because it is not important
        }
        String zipFileName="/"	+TempFileManager.TYPE_REPORT+"/"+folderName+"_zip"+"/"+REPORT_ZIP ;
        if (sendToChorus)
        {
            return File.separator+"alpine" + File.separator + "flowresults" +File.separator+ folderName+"_zip" + File.separator+REPORT_ZIP;
        }
        return zipFileName;
    }

    private static String createChorusFilePath(String foldername)
    {
        String resultPath;
        try {
            String homedir = System.getenv("EDCHOME");
            if (homedir == null) homedir = ".";

            File f= new File(homedir);

            if (f == null)
            {
                return null;
            }

            f = new File(f.getAbsolutePath());

            f = new File(f, "chorus-apps/hot-deploy/client/public/alpine/flowresults/" + foldername + "_zip");
            if (!f.exists())
            {
                f.mkdirs();
            }

            resultPath = f.getCanonicalPath();

        } catch (IOException e) {
            itsLogger.error(e.getMessage(),e);
            resultPath=System.getProperty("java.io.tmpdir");
        }
        itsLogger.info("Will print file to: " + resultPath);
        return resultPath ;
    }

    /**
     * @param sourceFolder
     * @param zipFileName
     * @throws Exception
     */
    private void zipFolderToFile(String sourceFolder, String zipFileName)
    {
        File f = new File (zipFileName);
        if(f.exists()==false){
            try {
                f.createNewFile();
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        itsLogger.info("Saving zip file to: " + f.getAbsolutePath());
        // create a ZipOutputStream to zip the data to
        ZipOutputStream zos =null;;
        try {
            zos = new ZipOutputStream(new FileOutputStream(
                    zipFileName));
            zipDir(sourceFolder, zos,sourceFolder);


        } catch ( Exception e) {
            itsLogger.error(e.getMessage(),e) ;

            e.printStackTrace();
        }finally{
            if(zos!=null){
                try {
                    zos.close();
                } catch (IOException e) {
                    itsLogger.error(e.getMessage(),e) ;
                    e.printStackTrace();
                }
            }
        }



    }

    // here is the code for the method
    public void zipDir(String dir2zip, ZipOutputStream zos, CharSequence relativeRoot) throws Exception {

        File zipDir = new File(dir2zip);
        // get a listing of the directory content
        String[] dirList = zipDir.list();
        byte[] readBuffer = new byte[2156];
        int bytesIn = 0;
        // loop through dirList, and zip the files
        for (int i = 0; i < dirList.length; i++) {
            File f = new File(zipDir, dirList[i]);
            if (f.isDirectory()) {
                // if the File object is a directory, call this
                // function again to add its content recursively
                String filePath = f.getPath();
                zipDir(filePath, zos,relativeRoot);
                // loop again
                continue;
            }

            // create a FileInputStream on top of f
            FileInputStream fis = new FileInputStream(f);


            String relativePath =f.getPath().replace(relativeRoot, "");
            //MINERWEB-694 make sure the zip file can be opend in both windows and linux;
            relativePath=relativePath.replace("\\", "/");

            ZipEntry anEntry = new ZipEntry(relativePath);
            // place the zip entry in the ZipOutputStream object
            zos.putNextEntry(anEntry);
            // now write the content of the file to the ZipOutputStream
            while ((bytesIn = fis.read(readBuffer)) != -1) {
                zos.write(readBuffer, 0, bytesIn);
            }
            // close the Stream
            fis.close();
        }

    }

    private void makeDirIfNotExist(String reportFileName) {
        File dir = new File(reportFileName);
        if (dir.exists() == false) {
            dir.mkdir();
        }
    }




}

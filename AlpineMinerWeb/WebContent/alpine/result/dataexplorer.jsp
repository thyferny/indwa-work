<%@ include file="../../alpine/commons/jstl.jsp"%>
<fmt:bundle basename="app">
	<html>
	<head>
	<%@ page pageEncoding="UTF-8"%>
<%@ page  import="com.alpine.miner.utils.SysConfigManager" %>
<%@page import="com.alpine.miner.impls.web.resource.ResourceManager"%>
<%@page import="com.alpine.miner.impls.web.resource.PreferenceInfo"%>
    <%@ page import="java.net.URLDecoder" %>
    <%@ page import="java.net.URLEncoder" %>

    <%
		String title_key = request.getParameter("title_key");
			String load_method = request.getParameter("load_method");

			String dbTableName = request.getParameter("tableName");
            if(null!=dbTableName){
                dbTableName = new String(dbTableName.getBytes(SysConfigManager.INSTANCE.getServerEncoding()),
                        "UTF-8");
            }

			String dbSchemaName = request.getParameter("schemaName");
			String dbConnName = request.getParameter("dbConnectionName");
            if(null!=dbConnName){
                dbConnName = new String(dbConnName.getBytes(SysConfigManager.INSTANCE.getServerEncoding()),
                        "UTF-8");
            }

			String baseURL = request.getContextPath();//"http://" + request.getHeader("Host") + request.getContextPath();
			String path = request.getContextPath();
			String valueDomain = request.getParameter("valueDomain");
			String scopeDomain = request.getParameter("scopeDomain");
            String useApproximation = request.getParameter("useApproximation");
			String categoryType = request.getParameter("categoryType");
			String progressImage = baseURL + "/images/progressBar.gif";
			String resourceType = request.getParameter("resourceType");
			String analysisColumn = request.getParameter("analysisColumn");
			String referenceColumn = request
					.getParameter("referenceColumn");
			String categoryColumn = request.getParameter("categoryColumn");
			String columnBins = request.getParameter("columnBins");
			String isGeneratedTable = request
					.getParameter("isGeneratedTable");

			String idColumn = request.getParameter("idColumn");
			String valueColumn = request.getParameter("valueColumn");

			String groupByColumn = request.getParameter("groupByColumn");

			String columnNameIndex = request
					.getParameter("columnNameIndex");
			String analysisValue = request.getParameter("analysisValue");
			String analysisSeries = request.getParameter("analysisSeries");
			String analysisType = request.getParameter("analysisType");
			String referenceColumnType = request.getParameter("referenceColumnType");
			
			//scatter plot matrix accuracy
			String scatterMatrixAccuracy = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_ALG,PreferenceInfo.KEY_MAX_SCATTER_POINTS);
          // for hadoop file structure
         // String connectionKey = request.getParameter("connectionKey");
         // String hadooppath = request.getParameter("path");
         // String operatorKey = request.getParameter("operatorKey");


        //

        String hadoopConnectKey = null;
        if(request.getParameter("hadoopConnectKey")!=null){
            hadoopConnectKey =  URLEncoder.encode(request.getParameter("hadoopConnectKey"));
        }
        String hadoopFilePath = null;
        if(request.getParameter("hadoopFilePath") != null){
        	hadoopFilePath = URLEncoder.encode(request.getParameter("hadoopFilePath"));
        }
        String operatorUID = request.getParameter("operatorUID");
        String flowInfoKey = null;
        if(request.getParameter("flowInfoKey")!=null){
            flowInfoKey =  URLEncoder.encode(request.getParameter("flowInfoKey"));
        }
        String isHDFileOperator = request.getParameter("isHDFileOperator");

	%>
	<link rel="shortcut icon" href="<%=path%>/favicon.ico"
		type="image/vnd.microsoft.icon">
	<link rel="icon" href="<%=path%>/favicon.ico"
		type="image/vnd.microsoft.icon">

	<title><fmt:message key="<%=title_key%>" /></title>
    <style type="text/css">
      .scatter-matric-text{}
      .scatter-matric-text .bigger{font-weight:bolder;height:30px;font-size:20px;}
      .scatter-matric-text .smaller{font-weight:bolder;height:10px;font-size:10px;}
	  #progressBarInstance img{width:100%!important;}
	  #scattermatrixchartdlg div{overflow:hidden;}
    </style>
	<%@ include file="/alpine/commons/dojoinclude.jsp"%>
    <%@ include file="/alpine/commons/graphicsinclude.jsp"%>


    <script type="text/javascript">
	var CONST_scatterMatrixAccuracy = <%=scatterMatrixAccuracy%>;
	</script>
    <script type="text/javascript" src="../../js/alpine/alpineConstants.js"		charset="utf-8"></script>
	<script type="text/javascript" src="../../js/alpine/common.js"		charset="utf-8"></script>
	<script type="text/javascript" src="../../js/alpine/dataStore.js"		charset="utf-8"></script> 
	<script type="text/javascript" src="../../js/alpine/logView.js"		charset="utf-8"></script>
	<script type="text/javascript" src="../../js/alpine/popupComponent.js"		charset="utf-8"></script> <!-- 
<script type="text/javascript" src="../../js/alpine/prototype.js"
	charset="utf-8"></script>
  -->
	<script type="text/javascript" src="../../js/alpine/result.js"		charset="utf-8"></script>
    <script type="text/javascript" src="../../js/alpine/httpService.js" charset="utf-8"></script>


    <script type="text/javascript">
    //MINERWEB-877
   try{
	    window.moveTo(0,0);        
	    window.resizeTo(screen.availWidth,screen.availHeight); 
	    window.outerWidth=screen.availWidth;        
	    window.outerHeight=screen.availHeight;   
   }catch(e){
   }

	// declare alpine.nls first, to avoid overwirte the alpine functions..
    if (!alpine) alpine = {};
        alpine.nls = {
			error : "<fmt:message key='Error'/>",
			no_login : "<fmt:message key='no_login'/>",
			result_not_found : "<fmt:message key='result_not_found'/>",
			message_unknow_error : "<fmt:message key='message_unknow_error'/>",
			OK: "<fmt:message key='OK'/>",
			Yes: "<fmt:message key='Yes'/>",
			No: "<fmt:message key='No'/>",
			Cancel: "<fmt:message key='Cancel'/>",
			message_unknow_error: "<fmt:message key='message_unknow_error'/>",
			can_not_connect_server : "<fmt:message key='can_not_connect_server'/>",
			chart_barchart_direction_horizontal: "<fmt:message key='chart_barchart_direction_horizontal'/>",
			chart_barchart_direction_vertical: "<fmt:message key='chart_barchart_direction_vertical'/>",
			chart_barchart_direction_title: "<fmt:message key='chart_barchart_direction_title'/>",
			scatter_plot_matrix_img_click_tooltip :  "<fmt:message key='scatter_plot_matrix_img_click_tooltip'/>",
			scatter_plot_matrix_img_tooltip :  "<fmt:message key='scatter_plot_matrix_img_tooltip'/>",
			scatter_plot_matrix_txt_tooltip :  "<fmt:message key='scatter_plot_matrix_txt_tooltip'/>",
			scatter_plot_legend_SCATTERMATRIX_LINE :  "<fmt:message key='SCATTERMATRIX_LINE'/>",
			scatter_plot_legend_SCATTERMATRIX_VALUE : "<fmt:message key='SCATTERMATRIX_VALUE'/>",
            hadoop_prop_right_menu_file_explorer:"<fmt:message key='hadoop_prop_right_menu_file_explorer'/>",
            box_plot_min : "<fmt:message key='box_plot_min'/>",
            box_plot_25 : "<fmt:message key='box_plot_25'/>",
            box_plot_median : "<fmt:message key='box_plot_median'/>",
            box_plot_75 : "<fmt:message key='box_plot_75'/>",
            box_plot_max : "<fmt:message key='box_plot_max'/>",
            box_plot_mean : "<fmt:message key='box_plot_mean'/>",
            series:  "<fmt:message key="Scope_Domain" />"

	};
   

   dojo.require("dojox.widget.Standby");
   dojo.require("alpine.layout.StandbySpinner");
   dojo.require("alpine.spinner");
   dojo.require("alpine.import.CSVUtil");

   //dojo.require("proalpine.ps.HadoopFileOperatorPropertyHelper");
   dojo.require("alpine.flow.HadoopOperatorsDataExplorerManager");
   dojo.require("alpine.props.HadoopDataTypeUtil");

   dojo.require("dijit.Dialog");
   dojo.require("dojo.parser");
   dojo.require("dijit.layout.ContentPane");
   dojo.require("dijit.layout.LayoutContainer");
   dojo.require("dijit.layout.TabContainer");
   dojo.require("dijit.ProgressBar");
   dojo.require("dijit.TitlePane");
   dojo.require("dojo._base.json");
   dojo.require("dojo._base.xhr");
   dojo.require("dojo.data.ItemFileReadStore");   
   dojo.require("dojo.data.ItemFileWriteStore"); 
   dojo.require("dojo.string");
   dojo.require("dijit.form.Select");
   dojo.require("dojox.charting.action2d.Highlight");	 
   dojo.require("dojox.charting.action2d.Tooltip");
   dojo.require("dojox.charting.axis2d.Default");
   dojo.require("dojox.charting.Chart");
   dojo.require("dojox.charting.DataSeries");
   dojo.require("dojox.charting.plot2d.ClusteredColumns");
   dojo.require("dojox.charting.plot2d.ClusteredBars");
   dojo.require("dojox.charting.plot2d.Grid");
   dojo.require("dojox.charting.plot2d.Lines");
   dojo.require("dojox.charting.plot2d.Markers");	 
   dojo.require("dojox.charting.plot2d.Scatter");
   dojo.require("dojox.charting.themes.Bahamation");  
   dojo.require("dojox.charting.widget.Legend");
   dojo.require("dojox.charting.widget.SelectableLegend");
   dojo.require("dojox.grid.DataGrid");
   dojo.require("dojox.json.ref");

   var analysisColumn = "<%=analysisColumn%>";
   var analysisSeries = "<%=analysisSeries%>";
   var analysisType = "<%=analysisType%>";
   var analysisValue = "<%=analysisValue%>";
   var baseURL= "<%=baseURL%>";
   var categoryColumn = "<%=categoryColumn%>";
   var categoryType = "<%=categoryType%>";
   var columnBins = '<%=columnBins%>';
   var columnNameIndex = "<%=columnNameIndex%>";
   var dbConnName=  "<%=dbConnName%>";
   var dbSchemaName=  "<%=dbSchemaName%>";
   var dbTableName=  "<%=dbTableName%>";
   var groupByColumn ="<%=groupByColumn%>";
   var  idColumn ="<%=idColumn%>";
   var isGeneratedTable= "<%=isGeneratedTable%>";
   var loginURL = "<%=path%>"+ "/index.jsp";
   //var progressImage = "<%=progressImage%>" ;
   var resourceType = "<%=resourceType%>";
   var scopeDomain = "<%=scopeDomain%>";
   var valueColumn ="<%=valueColumn%>";
   var valueDomain = "<%=valueDomain%>";
    var useApproximation = "<%=useApproximation%>";

    var referenceColumn = "<%=referenceColumn%>";
    var ds = new httpService();

    var hadoopConnectKey = decodeURIComponent("<%=hadoopConnectKey%>");
    var hadoopFilePath = "<%=hadoopFilePath%>";
    var operatorUID = "<%=operatorUID%>";
    var flowInfoKey = decodeURIComponent("<%=flowInfoKey%>");
    var title_key = "<%=title_key%>";
    var isHDFileOperator = "<%=isHDFileOperator%>";

	 function loadTableData(  ){

			var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getTableData"
			+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
			+"&resourceType="+resourceType+"&isGeneratedTable="+isGeneratedTable;

			 getVisualizationModel(requestUrl,"dataexplorer_id");

	}

	//add By Will
	function loadScatPlotMartixData(){
		var requestUrl = baseURL
		+ "/main/dataexplorer.do?method=getScatPlotMartixData"
		+ "&dbConnName="+dbConnName
		+ "&dbSchemaName="+dbSchemaName 
		+ "&dbTableName="+dbTableName
		+ "&columnNameIndex="+columnNameIndex
		+"&resourceType="+resourceType
		+"&isGeneratedTable="+isGeneratedTable;


		getVisualizationModel(requestUrl,"dataexplorer_id");
   }

   function loadScatPlotMartixData4Hadoop(){
       var requestUrl = baseURL
               + "/main/hadoopDataexplorer.do?method=getScatterPlotMatrixData4Hadoop"
               + "&hadoopConnectKey="+hadoopConnectKey
               + "&hadoopFilePath="+hadoopFilePath
               + "&operatorUID="+operatorUID
               + "&flowInfoKey="+flowInfoKey
               +"&columnNameIndex="+columnNameIndex
               +"&isHDFileOperator="+isHDFileOperator
               +"&resourceType="+resourceType;

       getVisualizationModel(requestUrl,"dataexplorer_id");
   }


    function gethadoopContent4DataExplorer(){
       var dataViewContainerID = 'dataexplorer_id';
        alpine.flow.HadoopOperatorsDataExplorerManager.show_hadoop_data_explorer(hadoopConnectKey,hadoopFilePath,operatorUID,flowInfoKey,isHDFileOperator,function(data){
            var vdata = buildData4HadoopVisualizationTableModel(data);
            //if(vdata!=false){
                showVislualizationModel(vdata,dataViewContainerID);
           // }

        });
    }

    function showXMLFileContent4DataExplorer(vdata){
        var dataViewContainerID = 'dataexplorer_id';
        showVislualizationModel(vdata,dataViewContainerID);
    }

    function buildData4HadoopVisualizationTableModel(data){
        if(data!=null && data.error!=null){
            popupComponent.alert(data.error,function(){window.close();});
            return false;
        }
        var module = {};
        module.visualType = 6;
        module.visualData = [];
        module.isGenerateReport = true;
        module.out_id = new Date().getTime()+"_"+Math.random();
        module.out_title = alpine.nls[title_key];
        module.visualData.tableName = hadoopFilePath;
        module.visualData.columns = [];
        module.visualData.columnTypes = [];
        module.visualData.items = [];

        var dataContentObj = {};

        for(var key in data.content){
            dataContentObj.tableKey =  key;
            dataContentObj.dataContent = data.content[key];
        }

        dataContent = dataContentObj.dataContent;
        dataDelimiter = data.delimiter;
        columnNameList = data.columnNameList;
        columnTypeList = data.columnTypeList;
        includeHeader = data.includeHeader;
        other = data.other;
        quoteChar = data.quoteChar;
        escapChar = data.escapChar;
        includeHeader = data.includeHeader;
        var subModule = dojo.clone(module);
        subModule.visualType = 0;
        subModule.visualData.tableName = dataContentObj.tableKey;
        subModule.out_title = subModule.out_title+":"+dataContentObj.tableKey
        if(""==dataDelimiter || null==dataDelimiter){
            subModule.visualType = 1;  // VISUAL_TYPE_TEXT
            subModule.visualData.text = dataContent;
           // dojo.create("textarea",{rows:"36",cols:"120",sytel:"width:100%;",innerHTML:dataContent},dojo.byId("dataexplorer_id"));
            //return false;
        }else{
            buildTableModuleData(subModule,dataContent);
        }
        module.visualData.push(subModule);
        return module;
    }

    function buildTableModuleData(module,datacontent){
        if(null!=dataContent && null!=dataDelimiter && null!=includeHeader){
            var splitter = _getDelimiterByName(dataDelimiter);
            var csvParams = {
                separator: splitter,
                quote: quoteChar,
                escaped: escapChar
            };


           // var dataObj =  alpine.props.HadoopFileOperatorPropertyHelper.buildFileContent(datacontent,dataDelimiter,escapChar,quoteChar,other);
            var dataObj = dataExplorer4buildFileContent(datacontent,dataDelimiter,escapChar,quoteChar);

            if(dataObj!=null && dataObj.length>0){
                var maxColNum = 1;
                for(var i=0;i<dataObj.length;i++){
                    if(maxColNum<dataObj[i].length){
                        maxColNum = dataObj[i].length;
                    }
                }

                if(includeHeader!=null && includeHeader.toUpperCase()=="TRUE"){
                    var firstRow = dataObj[0];
                    var headerName =  columnNameList;
                    if(null==headerName){
                        //headerName = new Array(maxColNum);
                        headerName = [];
                        for(var ttt=0;ttt<maxColNum;ttt++){
                            headerName.push(dataObj[0][ttt]);
                        }
                    }
                    for(var j=0;j<maxColNum;j++){
                        if(firstRow==null){
                            firstRow = "";
                        }

                        if(headerName[j]!=null && ""!=headerName[j]){
                            module.visualData.columns.push(headerName[j]);
                        }else{
                            module.visualData.columns.push(firstRow[j]);
                        }

                        //module.visualData.columns.push(firstRow[j]);
                        if(null!=columnTypeList){
                            module.visualData.columnTypes.push(getColumnType(columnTypeList[j]));
                        }else{
                            module.visualData.columnTypes.push("string");
                        }
                    }
                    //build item
                    for(var kk=1;kk<dataObj.length;kk++){
                        var tempItemObj = {};
                        for(var ll=0;ll<maxColNum;ll++){
                            //alert("dataObj["+kk+"]["+ll+"]"+dataObj[kk][ll]);
                            //alert("module.visualData.columns["+ll+"]"+module.visualData.columns[ll]);

                            tempItemObj[module.visualData.columns[ll]] = dataObj[kk][ll];
                        }
                        module.visualData.items.push(tempItemObj);
                    }

                }else if(null!=columnNameList && columnNameList.length>0){
                    var headerName =  columnNameList;
                    if(null==headerName){
                       // headerName = new Array(maxColNum);
                        headerName = [];
                        for(var ttt=0;ttt<maxColNum;ttt++){
                            headerName.push("Column"+ttt);
                        }
                    }
                    for(var m=0;m<maxColNum;m++){
                        if(headerName[m]!=null){
                            module.visualData.columns.push(headerName[m]);
                        }else{
                            module.visualData.columns.push("column"+m);
                        }
                        if(null!=columnTypeList){
                             module.visualData.columnTypes.push(getColumnType(columnTypeList[j]));
                        }else{
                            module.visualData.columnTypes.push("string");
                        }
                    }
                    //build item
                    for(var kk=0;kk<dataObj.length;kk++){
                        var tempItemObj = {};
                        for(var ll=0;ll<maxColNum;ll++){
                            //alert("dataObj["+kk+"]["+ll+"]"+dataObj[kk][ll]);
                            //alert("module.visualData.columns["+ll+"]"+module.visualData.columns[ll]);

                            tempItemObj[module.visualData.columns[ll]] = dataObj[kk][ll];
                        }
                        module.visualData.items.push(tempItemObj);
                    }
                }else{
                    for(var j=0;j<maxColNum;j++){
                        module.visualData.columns.push("column"+j);
                        if(null!=columnTypeList){
                            module.visualData.columnTypes.push(getColumnType(columnTypeList[j]));
                        }else{
                            module.visualData.columnTypes.push("string");
                        }
                    }
                    //build item
                    for(var kk=0;kk<dataObj.length;kk++){
                        var tempItemObj = {};
                        for(var ll=0;ll<maxColNum;ll++){
                            //alert("dataObj["+kk+"]["+ll+"]"+dataObj[kk][ll]);
                            //alert("module.visualData.columns["+ll+"]"+module.visualData.columns[ll]);

                            tempItemObj[module.visualData.columns[ll]] = dataObj[kk][ll];
                        }
                        module.visualData.items.push(tempItemObj);
                    }

                }
            }
        }
    }

    function dataExplorer4buildFileContent(content,deliminter,escapChar,quoteChar){

        var realDeliminter = _getDelimiterByName(deliminter);

        var csvParams = {
            separator: realDeliminter,
            quote: quoteChar,
            escaped: escapChar
        };

        var returnArray = [];
        if(content!=null){
            var rowDatas = content.split("\n");

            if(null!=rowDatas){

                returnArray = 	alpine.import.CSVUtil.csvArrayToArrayOfArrays(rowDatas, csvParams) ;
//                for(var i=0;i<rowDatas.length;i++){
//
//                    if(""==rowDatas[i]){continue;}
//                    returnArray.push( csvUtil.csvArrayToArrayOfArrays(rowDatas[i], csvParams) );
//                }
            }
        }
        //remove empty data
        _removeEmptyData(returnArray);
        return returnArray;
    }

    function _removeEmptyData(returnArray){
        if(null!=returnArray && returnArray.length>0){
            var emptyLine = [];
            for(var i=0;i<returnArray.length;i++){
                if(returnArray[i]==""){
                    emptyLine.push(i);
                }
            }

            for(var j=0;j<emptyLine.length;j++){
                returnArray.splice(emptyLine[j],1);
            }
        }
    }

    function _getDelimiterByName(delimiterName){
        switch (delimiterName.toUpperCase()){
            case "TAB":
                return "\t";
                break;
            case "COMMA":
                return ",";
                break;
            case "SEMICOLON":
                return ";";
                break;
            case "SPACE":
                return " ";
                break;
            default:
                return other;
        }
    }

    function _getDataContent(delimiter,dataRows){
        var splitData =[];
        if(null!=dataRows){
            var rows = dataRows;
            if(null!=rows && rows.length>0){
                for(var i=0;i<rows.length;i++){
                    var tempSplitData = rows[i].split(delimiter);
                    if(null!=tempSplitData && tempSplitData!=""){
                        splitData.push(tempSplitData);
                    }
                }
            }
        }
        return splitData;
    }

    function getColumnType(columnType){
        if(alpine.props.HadoopDataTypeUtil.isNumberType(columnType)==true){
            return "number";
        }else{
            return "string";
        }
    }


	 	function loadUnivariteData(){
			
			var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getUniverrateData"
				+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
			 + "&columnNameIndex="+columnNameIndex
			 + "&referenceColumn="+referenceColumn
			 +"&resourceType="+resourceType
			   +"&isGeneratedTable="+isGeneratedTable;

			 getVisualizationModel(requestUrl,"dataexplorer_id");

		}
		
	function loadBarchartData(){

		var requestUrl = baseURL
		+ "/main/dataexplorer.do?method=getBarchartData"
			+ "&dbConnName="+dbConnName
		+ "&dbSchemaName="+dbSchemaName 
		+ "&dbTableName="+dbTableName
		+"&valueDomain="+valueDomain
		+"&scopeDomain="+scopeDomain
		+"&categoryType="+categoryType
		+"&resourceType="+resourceType
		+"&isGeneratedTable="+isGeneratedTable;

		 getVisualizationModel(requestUrl,"dataexplorer_id"); 

	}

    function getBoxAndWiskData4Hadoop(){
        var requestUrl = baseURL
                + "/main/hadoopDataexplorer.do?method=getBoxAndWiskData4Hadoop"
                + "&hadoopConnectKey="+hadoopConnectKey
                + "&hadoopFilePath="+hadoopFilePath
                + "&operatorUID="+operatorUID
                + "&flowInfoKey="+flowInfoKey
                +"&analysisValue="+analysisValue
                +"&analysisSeries="+analysisSeries
                +"&analysisType="+analysisType
                +"&useApproximation="+useApproximation
                +"&isHDFileOperator="+isHDFileOperator
                +"&resourceType="+resourceType;

        getVisualizationModel(requestUrl,"dataexplorer_id");

    }

	function loadBarchartData4Hadoop(){
       var requestUrl = baseURL
		+ "/main/hadoopDataexplorer.do?method=getBarchartData4Hadoop"
			+ "&hadoopConnectKey="+hadoopConnectKey
		+ "&hadoopFilePath="+hadoopFilePath
		+ "&operatorUID="+operatorUID
		+ "&flowInfoKey="+flowInfoKey
		+"&valueDomain="+valueDomain
		+"&scopeDomain="+scopeDomain
		+"&categoryType="+categoryType
        +"&isHDFileOperator="+isHDFileOperator
		+"&resourceType="+resourceType;

		 getVisualizationModel(requestUrl,"dataexplorer_id");

	}

	function loadTimeSeriesData(  ){
		
		var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getTimeSeriesData"
			+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
			+"&idColumn="+idColumn
			+"&valueColumn="+valueColumn
			+"&groupByColumn="+groupByColumn
			+"&resourceType="+resourceType   
			+"&isGeneratedTable="+isGeneratedTable;

		getVisualizationModel(requestUrl,"dataexplorer_id");
	}

    function loadStatisticsData(  ){
		
		var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getStatisticsData"
			+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
		 	+ "&columnNameIndex="+columnNameIndex
		 	+"&resourceType="+resourceType
		 	+"&isGeneratedTable="+isGeneratedTable;

		 getVisualizationModel(requestUrl,"dataexplorer_id");

	}

    function loadStatisticsData4Hadoop(){
        var requestUrl = baseURL
                + "/main/hadoopDataexplorer.do?method=getStatisticsData4Hadoop"
                + "&hadoopConnectKey="+hadoopConnectKey
                + "&hadoopFilePath="+hadoopFilePath
                + "&operatorUID="+operatorUID
                + "&flowInfoKey="+flowInfoKey
                +"&columnNameIndex="+columnNameIndex
                +"&isHDFileOperator="+isHDFileOperator
                +"&resourceType="+resourceType;
		 getVisualizationModel(requestUrl,"dataexplorer_id");

	}

	function loadScatterData(  ){		
		var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getScatterData"
			+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
			+"&analysisColumn="+analysisColumn
			+"&referenceColumn="	+referenceColumn
			+"&categoryColumn="+categoryColumn
			+"&referenceColumnType=<%=referenceColumnType%>"
			+"&resourceType="+resourceType 
			+"&isGeneratedTable="+isGeneratedTable;


		 getVisualizationModel(requestUrl,"dataexplorer_id");

	}


 	function loadBoxAndWiskerData(  ){

		var requestUrl = baseURL
		+ "/main/dataexplorer.do?method=getBoxAndWiskData"
			+ "&dbConnName="+dbConnName
		+ "&dbSchemaName="+dbSchemaName 
		+ "&dbTableName="+dbTableName
		+"&analysisValue="+analysisValue
		+"&analysisSeries="+analysisSeries
		+"&analysisType="+analysisType
		+"&resourceType="+resourceType
		+"&isGeneratedTable="+isGeneratedTable;


		 getVisualizationModel(requestUrl,"dataexplorer_id");

	}

	function loadCorrelationData(  ){
		
		var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getCorrelationData"
			+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
			+ "&columnNameIndex="+columnNameIndex
			+"&resourceType="+resourceType
			+"&isGeneratedTable="+isGeneratedTable;


			getVisualizationModel(requestUrl,"dataexplorer_id");

		}

	function loadFrequencyData(  ){
			
			var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getFrequencyData"
				+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
			 + "&columnNameIndex="+columnNameIndex
			 +"&resourceType="+resourceType
			   +"&isGeneratedTable="+isGeneratedTable;


			 getVisualizationModel(requestUrl,"dataexplorer_id");

    }

	function loadFrequencyData4Hadoop(){
        var requestUrl = baseURL
                + "/main/hadoopDataexplorer.do?method=getFrequencyData4Hadoop"
                + "&hadoopConnectKey="+hadoopConnectKey
                + "&hadoopFilePath="+hadoopFilePath
                + "&operatorUID="+operatorUID
                + "&flowInfoKey="+flowInfoKey
                + "&columnNameIndex="+columnNameIndex
                +"&resourceType="+resourceType
                +"&isHDFileOperator="+isHDFileOperator;
			 getVisualizationModel(requestUrl,"dataexplorer_id");
    }

	function loadHisogramData(  ){
			var requestUrl = baseURL
			+ "/main/dataexplorer.do?method=getHistogramData"
				+ "&dbConnName="+dbConnName
			+ "&dbSchemaName="+dbSchemaName 
			+ "&dbTableName="+dbTableName
			 + "&columnNameIndex="+columnNameIndex
			 + "&columnBins="+columnBins
			 +"&resourceType="+resourceType
			   +"&isGeneratedTable="+isGeneratedTable;

	
			 getVisualizationModel(requestUrl,"dataexplorer_id");
	}
	function loadHisogramData4Hadoop(  ){
        var requestUrl = baseURL
                + "/main/hadoopDataexplorer.do?method=getHistogramData4Hadoop"
                + "&hadoopConnectKey="+hadoopConnectKey
                + "&hadoopFilePath="+hadoopFilePath
                + "&operatorUID="+operatorUID
                + "&flowInfoKey="+flowInfoKey
                + "&columnNameIndex="+columnNameIndex
                + "&columnBins="+columnBins
                +"&isHDFileOperator="+isHDFileOperator
                +"&resourceType="+resourceType;


			 getVisualizationModel(requestUrl,"dataexplorer_id");
	}

 
  //this does not work in IE 
 //dojo.addOnLoad(result); 
</script>
	</head>
	<body class="soria" onload="<%=load_method%>()" onunload = "releaseResultTab('dataexplorer_id')" id="coverScopeId">
	<div id="dataexplorer_id" style="width: 100%;height:100%">
	
	</div>

    <div id="scattermatrixchartdlg" dojoType="dijit.Dialog" title="<fmt:message key='Scat_Plot_Martix'/>"
         style="background: #ffffff;overflow:hidden;width:820px;height:600px">
        <div dojoType="dijit.layout.ContentPane" region="center" id="scattermatrixchartContainer" style="width:800px;height:500px;overflow:visible;"></div>
        <div dojoType="dijit.layout.ContentPane" region="bottom" style="width:800px">
            <div style="overflow:hidden;width:100%;text-align: right;">	<button  dojoType=dijit.form.Button type="button" baseClass="primaryButton" id ="scattermatrixchartdlg_OK" onClick="dijit.byId('scattermatrixchartdlg').hide();">
                <fmt:message key='Done'/>
            </button>
            </div>
        </div>

    </div>
	</body>
	</html>
</fmt:bundle>
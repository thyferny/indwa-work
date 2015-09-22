/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * File: DataExplorerHelper
 * Author: robbie (mostly refactored from dataexplorer.js)
 * Date: 12-12-12
 */

define([
    "dojo/dom",
    "dojo/_base/lang",
    "dojo/_base/array",
    "dijit/registry",
    "alpine/flow/WorkFlowVariableReplacer",
    "alpine/flow/WorkFlowManager",
    "alpine/props/HadoopDataTypeUtil",
    "alpine/layout/ColumnSelect/ColumnSelect"
],function(dom, lang, array, registry, workFlowVariableReplacer, workFlowManager, hadoopDataTypeUtil, ColumnSelect){

    //define constants
    var ID_ANALYSIS_VALUE = "id_analysisValue";
    var ID_ANALYSIS_SERIES = "id_analysisSeries";
    var ID_ANALYSIS_TYPE = "id_analysisType";

    var ID_ANALYSIS_COLUMN = "id_analysisColumn";
    var ID_REFERENCE_COLUMN = "id_referenceColumn";
    var ID_CATEGORY_COLUMN = "id_categoryColumn";

    var ID_VALUE_DOMAIN = "id_valueDomian";
    var ID_SCOPE_DOMAIN = "id_scopeDomain";
    var ID_CATEGORY_TYPE = "id_CategoryType";

    var ID_IDCOLUMN = "id_IDColumn";
    var ID_VALUE_COLUMN = "id_valueColumn";
    var ID_GROUPBY_COLUMN = "id_groupByColumn";

    var constants = {
        CANVAS: "FlowDisplayPanelPersonal"
    };

    function _buildColumnSelector(operator, availableColumns, numberRequiredColumns, callback) {
        var dataItems = _prepareDataItems(availableColumns);

        new ColumnSelect({
            dataItems: dataItems,
            requiredCols: numberRequiredColumns,
            okButtonFn: function (selectedItems) {
                callback(operator,selectedItems);
            }
        });
    }

    function _getColumnNames(operator, callback, coltype, is_multiple_column_selection, colsRequired) {

        var requestUrl;
        var columnType = coltype ? coltype : "all";
        var columnsRequired = colsRequired ? colsRequired : 0;
        var isHadoopOperator = false;
        if(null!=operator.operatorType && operator.operatorType.toUpperCase()=="HADOOP"){
            isHadoopOperator = true;
        }
        var de_avaliable_columns;
        if (isHadoopOperator) {
            requestUrl =  baseURL+"/main/hadoopDataexplorer.do?method=getHadoopColumnNameWithType&operatorUID="+operator.uid;
            ds.post(requestUrl,workFlowManager.getEditingFlow(),function(data){
                if(data==null || (null!=data && data.error!=null)){
                    popupComponent.alert(data.error);
                    return false;
                }
                if(null!=data && data.error==null){
                    de_avaliable_columns = _buildHadoopColumnNameDatas(data,operator);
                    // Pivotal:41377557 (should filter non-numeric columns )
                    if("number"==columnType && de_avaliable_columns!=null){
                        var arrLen = de_avaliable_columns.length;
                        var numTypeColumns = [];
                        for(var i=0;i<arrLen;i++){
                            if(de_avaliable_columns[i][1]=="number"){
                                numTypeColumns.push(de_avaliable_columns[i]);
                            }
                        }
                        de_avaliable_columns = numTypeColumns;
                    }
                    //end
                    if (is_multiple_column_selection) {

                        _buildColumnSelector(operator, de_avaliable_columns, columnsRequired, callback);

                    } else {
                        callback(operator, de_avaliable_columns);
                    }
                }
            },null,true,null);
        } else {
            var dbinfo=replaceVriableForPojo(getDBInfo(operator));
            if(!dbinfo.connection || !dbinfo.schema || !dbinfo.table){
                popupComponent.alert(alpine.nls.leak_datasource_to_dataexplorer);
                return;
            }

            requestUrl = baseURL + "/main/dataexplorer.do?method=getColumnNamesWithType"
                + "&dbConnName="+dbinfo.connection
                + "&dbSchemaName="+dbinfo.schema
                + "&dbTableName="+dbinfo.table
                +"&columnType="+columnType
                +"&resourceType="+workFlowManager.getEditingFlow().type+"&isGeneratedTable="+isTableGenerated(operator);

            ds.get(requestUrl,function(data){
                de_avaliable_columns = data;

                if(is_multiple_column_selection){

                    _buildColumnSelector(operator, de_avaliable_columns, columnsRequired, callback);

                }else{
                    callback(operator,de_avaliable_columns);
                }
            },null, false, constants.CANVAS);
        }

    }

    function _buildHadoopColumnNameDatas(data,operator){
        var columns = [];
        if(null!=data && null!=data.columnNameList && null!=data.columnTypeList){
            for(var i=0;i<data.columnNameList.length;i++){
                var dataType = data.columnTypeList[i];
                if(hadoopDataTypeUtil.isNumberType(dataType) == true){
                    dataType = "number";
                }
                columns.push([data.columnNameList[i],dataType]);
            }
        }
        return columns;
    }

    function _prepareDataItems(deItems) {
        var dataItems = [];
        array.forEach(deItems, function(item){
            dataItems.push({
                colName:item[0],
                id: item[0]
            });
        });
        return dataItems;
    }

    function replaceVriableForPojo(pojo){
        var newPojo = {};
        workFlowVariableReplacer.init();
        for(var field in pojo){
            newPojo[field] = workFlowVariableReplacer.replaceVariable(pojo[field]);
        }
        workFlowVariableReplacer.finalize();
        return newPojo;
    }

    function bindingLabelCuter(selectIds){
        for(var i = 0;i<selectIds.length;i++){
            var selectWidget = registry.byId(selectIds[i]);
            if(!selectWidget){
                continue;//widget for argument is not dojo widget.
            }
            dojo.connect(selectWidget, "onChange", selectWidget, function(newVal){
                if(null!=newVal && this.getOptions(newVal)!=null&&
                    undefined!=newVal && this.getOptions(newVal)!=undefined	){

                    var showLabel = this.getOptions(newVal).label;
                    if(showLabel.length > 30){
                        showLabel = showLabel.substr(0, 30) + "...";
                    }
                    this._setDisplay(showLabel);
                }
            });
        }
    }

    function fillSeletOptionsWithColumnNames(selectIDs,select_Types,columnNames){
        if(selectIDs){
            for ( var x = 0; x < selectIDs.length; x++) {
                var id= selectIDs[x];
                var type=select_Types[x];
                var select=registry.byId(id);
                if(select){
                    fillSelectOptions(select,columnNames,type,true);
                }
            }
        }
    }

    function fillSelectOptions(mySelect,optionNames,type,clear) {
        if(clear){
            var options=mySelect.getOptions();

            if(options){
                array.forEach(options,function(i){
                    mySelect.removeOption(i);
                });
            }
        }

        //can support null value :MINERWEB-290, second point in MINERWEB-738
        if(ID_CATEGORY_COLUMN==mySelect.id ||
            ID_GROUPBY_COLUMN == mySelect.id ||
            ID_ANALYSIS_SERIES == mySelect.id ||
            "id_CategoryType" == mySelect.id ||
            "id_scopeDomain" == mySelect.id ||
            ID_ANALYSIS_TYPE == mySelect.id){
            mySelect.addOption({
                value: "",
                label: "&nbsp;"
            });

        }

        for(var i=0;i<optionNames.length;i++){
            if(type==""||type==optionNames[i][1]||type.indexOf(optionNames[i][1])>-1){		
                mySelect.addOption({
                    value: optionNames[i][0],
                    label: optionNames[i][0]
                });
            }
        }

    }

    //data explorer
    function showTableData(operator){
        var dbinfo=replaceVriableForPojo(getDBInfo(operator));

        if(!dbinfo.connection || !dbinfo.schema || !dbinfo.table){
            popupComponent.alert(alpine.nls.leak_datasource_to_dataexplorer);
            return;
        }
        var url=baseURL + "/alpine/result/dataexplorer.jsp?dbConnectionName="
            + dbinfo.connection+"&schemaName="+dbinfo.schema+"&tableName="+dbinfo.table
            +"&resourceType="+workFlowManager.getEditingFlow().type+"&isGeneratedTable="+isTableGenerated(operator)
            +"&title_key="+"Data_Explorer"  +"&load_method="+"loadTableData";

        var resultWindow=window.open(url,	"_blank",   get_open_window_options());
        resultWindow.focus();
    }

    //start bar chart
    function showBarChart(operator){
        _getColumnNames(operator,showBarChartDialog);
    }

    function showBarChartDialog(operator,columnNames){
        var select_IDS=[ID_VALUE_DOMAIN,ID_SCOPE_DOMAIN,ID_CATEGORY_TYPE];
        bindingLabelCuter(select_IDS);
        var select_TypeS=["number","",""];//"" means all
        fillSeletOptionsWithColumnNames(select_IDS,select_TypeS,columnNames);
        dom.byId("barChartDialog_OK").onclick= function(){
            if( registry.byId("barChartDialog_OK").get("disabled")==true){
                return false;
            }
            showBarChartPage(operator);
        };
        if(registry.byId("id_valueDomian").get("value")==""){
            registry.byId("barChartDialog_OK").set("disabled",true);
        }else{
            registry.byId("barChartDialog_OK").set("disabled",false);
        }
        registry.byId("barChartDialog").titleBar.style.display = "none";
        registry.byId("barChartDialog").show();
    }

    function showBarChartPage(operator){

        var dbInfo=replaceVriableForPojo(getDBInfo(operator));
        var valueDomain=registry.byId(ID_VALUE_DOMAIN).get("value");
        var scopeDomain=registry.byId(ID_SCOPE_DOMAIN).get("value");
        var categoryType=registry.byId(ID_CATEGORY_TYPE).get("value");
        var url = null;


        if(scopeDomain=="" && categoryType==""){
            popupComponent.alert(alpine.nls.barchart_Series_Category_set_tip);
            return false;
        }
        if(categoryType==scopeDomain && scopeDomain==valueDomain && valueDomain!=""){
            popupComponent.alert(alpine.nls.anlysis_column_same);
            return ;
        }
        if(categoryType==scopeDomain){
            popupComponent.alert(alpine.nls.anlysis_column_same_Category_Series);
            return;
        }
        registry.byId("barChartDialog").hide();

        if(_isHadoopOperator(operator)==true){
            var hadoopInfo = replaceVriableForPojo(alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(operator));
            var eidtFlowInfo = workFlowManager.getEditingFlow();
            url= baseURL + "/alpine/result/dataexplorer.jsp?load_method=loadBarchartData4Hadoop"
                +"&hadoopConnectKey="+hadoopInfo.connectionName
                +"&hadoopFilePath="+hadoopInfo.outputFilePath
                +"&operatorUID="+operator.uid
                +"&flowInfoKey="+eidtFlowInfo.key
                +"&valueDomain="+valueDomain
                +"&scopeDomain="+scopeDomain
                +"&categoryType="+categoryType
                +"&isHDFileOperator="+(operator.classname=="HadoopFileOperator"?true:false)
                +"&title_key="+"Bar_Chart"
                +"&resourceType="+workFlowManager.getEditingFlow().type;
        }else{
            url= baseURL + "/alpine/result/dataexplorer.jsp?dbConnectionName="
                + dbInfo.connection+"&schemaName="+dbInfo.schema
                +"&tableName="+dbInfo.table
                +"&valueDomain="+valueDomain
                +"&scopeDomain="+scopeDomain
                +"&categoryType="+categoryType
                +"&resourceType="+workFlowManager.getEditingFlow().type
                +"&isGeneratedTable="+isTableGenerated(operator)
                +"&title_key="+"Bar_Chart"
                +"&load_method="+"loadBarchartData";

        }
        var resultWindow=window.open(url,"_blank",get_open_window_options());
        resultWindow.focus();
    }
    //end bar chart

    //start box and whisker
    function showBoxAndWiskerChart(operator){
        _getColumnNames(operator,showBoxChartDialog);
    }

    function showBoxChartDialog(operator,columnNames){
        var select_IDS=[ID_ANALYSIS_VALUE,ID_ANALYSIS_SERIES,ID_ANALYSIS_TYPE];
        bindingLabelCuter(select_IDS);
        var select_TypeS=["number","",""];
        fillSeletOptionsWithColumnNames(select_IDS,select_TypeS,columnNames);
        dom.byId("boxwhiskerPlotDialog_OK").onclick= function(){
            showBoxAndWiskerPage(operator);
        };
        registry.byId("boxwhiskerDialog").titleBar.style.display = "none";
        registry.byId("boxwhiskerDialog").show();
    }

    function showBoxAndWiskerPage(operator){

        var dbInfo=replaceVriableForPojo(getDBInfo(operator));
        var analysisValue=registry.byId(ID_ANALYSIS_VALUE).value;
        var analysisSeries=registry.byId(ID_ANALYSIS_SERIES).value;
        var analysisType=registry.byId(ID_ANALYSIS_TYPE).value;
        if(analysisSeries == "" && analysisType == ""){

        }else if(analysisValue==analysisSeries||analysisValue==analysisType
            ||analysisSeries==analysisType){
            popupComponent.alert(alpine.nls.anlysis_column_same);
            return;
        }
        registry.byId("boxwhiskerDialog").hide();


        if(_isHadoopOperator(operator)==true){
            var hadoopInfo = replaceVriableForPojo(alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(operator));
            var eidtFlowInfo = workFlowManager.getEditingFlow();
            url= baseURL + "/alpine/result/dataexplorer.jsp?load_method=getBoxAndWiskData4Hadoop"
                +"&hadoopConnectKey="+hadoopInfo.connectionName
                +"&hadoopFilePath="+hadoopInfo.outputFilePath
                +"&operatorUID="+operator.uid
                +"&flowInfoKey="+eidtFlowInfo.key
                +"&analysisValue="+analysisValue
                +"&analysisSeries="+analysisSeries
                +"&analysisType="+analysisType
                +"&useApproximation=true"
                +"&title_key="+"Box_and_Wisker_Chart"
                +"&isHDFileOperator="+(operator.classname=="HadoopFileOperator"?true:false)
                +"&resourceType="+workFlowManager.getEditingFlow().type;
        }else{
            url=baseURL + "/alpine/result/dataexplorer.jsp?dbConnectionName="
                + dbInfo.connection+"&schemaName="+dbInfo.schema
                +"&tableName="+dbInfo.table
                +"&analysisValue="+analysisValue
                +"&analysisSeries="+analysisSeries
                +"&analysisType="+analysisType
                +"&resourceType="
                +workFlowManager.getEditingFlow().type
                +"&isGeneratedTable="+isTableGenerated(operator)
                +"&title_key="+"Box_and_Wisker_Chart"
                +"&load_method="+"loadBoxAndWiskerData";

        }

        var resultWindow=window.open(url, "_blank",get_open_window_options());

        resultWindow.focus();
    }
    //end box and whisker

    //start Correlation
    function showCorrelationAnalysis(operator){
        _getColumnNames(operator,showCorrelationAnalysisPage,"number",true,2);
    }

    function showCorrelationAnalysisPage(operator,columnNames) {

//	var columnNameIndex = buildColumnNames(columnNames);
        var columnNameIndex = columnNames.join(",");
        var dbInfo=replaceVriableForPojo(getDBInfo(operator));

        var dataExplorerForm = dom.byId("dataExplorerForm");
        var window_id="chart"+new Date().getTime();
        dataExplorerForm.target = window_id;
        dom.byId("dataExplorerForm_dbConnectionName").value = dbInfo.connection;
        dom.byId("dataExplorerForm_schemaName").value = dbInfo.schema;
        dom.byId("dataExplorerForm_tableName").value = dbInfo.table;
        dom.byId("dataExplorerForm_columnNameIndex").value = columnNameIndex;
        dom.byId("dataExplorerForm_columnBins").value = "";
        dom.byId("dataExplorerForm_resourceType").value = workFlowManager.getEditingFlow().type;
        dom.byId("dataExplorerForm_isGeneratedTable").value = isTableGenerated(operator);
        dom.byId("dataExplorerForm_title_key").value = "Correlation_Analysis";
        dom.byId("dataExplorerForm_load_method").value = "loadCorrelationData";


        window.open("about:blank", window_id, get_open_window_options());

        dataExplorerForm.submit();

    }
    //end Correlation

    //start Frequency
    function showFrequencyAnalysis(operator){
        _getColumnNames(operator,showFrequencyAnalysisPage,null,true,1);
    }

    function showFrequencyAnalysisPage(operator ,columnNames){

//	var columnNameIndex = buildColumnNames(columnNames);
        var columnNameIndex = columnNames.join(",");
        var dbInfo=replaceVriableForPojo(getDBInfo(operator));

        var dataExplorerForm = dom.byId("dataExplorerForm");
        var window_id="chart"+new Date().getTime();
        dataExplorerForm.target = window_id;
        dom.byId("dataExplorerForm_dbConnectionName").value = dbInfo.connection;
        dom.byId("dataExplorerForm_schemaName").value = dbInfo.schema;
        dom.byId("dataExplorerForm_tableName").value = dbInfo.table;
        dom.byId("dataExplorerForm_columnNameIndex").value = columnNameIndex;
        dom.byId("dataExplorerForm_columnBins").value = "";
        dom.byId("dataExplorerForm_resourceType").value = workFlowManager.getEditingFlow().type;
        dom.byId("dataExplorerForm_isGeneratedTable").value = isTableGenerated(operator);
        dom.byId("dataExplorerForm_title_key").value = "Frequency_Analysis";
        dom.byId("dataExplorerForm_load_method").value = "loadFrequencyData";

        if(operator.operatorType == "HADOOP"){
            var hadoopInfo = replaceVriableForPojo(alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(operator));
            var eidtFlowInfo = workFlowManager.getEditingFlow();
            dom.byId("dataExplorerForm_load_hadoopConnectKey").value = hadoopInfo.connectionName;
            dom.byId("dataExplorerForm_load_hadoopFilePath").value = hadoopInfo.outputFilePath;
            dom.byId("dataExplorerForm_load_operatorUID").value = operator.uid;
            dom.byId("dataExplorerForm_load_flowInfoKey").value = eidtFlowInfo.key;
            dom.byId("dataExplorerForm_load_isHDFileOperator").value = (operator.classname=="HadoopFileOperator"?true:false);
            dom.byId("dataExplorerForm_load_method").value = "loadFrequencyData4Hadoop";
        }

        window.open("about:blank", window_id, get_open_window_options());

        dataExplorerForm.submit();

    }
    //end Frequency

    //start histogram
    function showHistogramChart(operator){
        _getColumnNames(operator,function(operator, fullColumns){
            var allColumnStore = {
                name: "name",
                identifier: "name",
                items: []
            };

            if(operator.operatorType == "HADOOP"){
                if(fullColumns.length>0){
                    for(var i=0;i<fullColumns.length;i++){
                        if(fullColumns[i][1]=="number"){
                            allColumnStore.items.push({name:fullColumns[i][0]});
                        }
                    }
                }
            } else {
                for(var i = 0;i < fullColumns.length;i++){
                    allColumnStore.items.push({name:fullColumns[i][0]});
                }
            }

            var allProp = {
                store: allColumnStore,
                columnBinsModel:{
                    columnBins:[]
                }
            };

            alpine.props.histogramPropertyHelper.showHistogramDialog(allProp,null,function(columnBins){
                var dataExplorerForm = dom.byId("dataExplorerForm");
                var window_id="chart"+new Date().getTime();
                dataExplorerForm.target =window_id;
                var dbInfo = replaceVriableForPojo(getDBInfo(operator));
                dom.byId("dataExplorerForm_dbConnectionName").value = dbInfo.connection;
                dom.byId("dataExplorerForm_schemaName").value = dbInfo.schema;
                dom.byId("dataExplorerForm_tableName").value = dbInfo.table;
                dom.byId("dataExplorerForm_columnNameIndex").value = "";
                dom.byId("dataExplorerForm_columnBins").value = dojox.json.ref.toJson(columnBins);
                dom.byId("dataExplorerForm_resourceType").value = workFlowManager.getEditingFlow().type;
                dom.byId("dataExplorerForm_isGeneratedTable").value = isTableGenerated(operator);
                dom.byId("dataExplorerForm_title_key").value = "Histogram_Chart";
                dom.byId("dataExplorerForm_load_method").value = "loadHisogramData";
                if(operator.operatorType == "HADOOP"){
                    var hadoopInfo = replaceVriableForPojo(alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(operator));
                    var eidtFlowInfo = workFlowManager.getEditingFlow();
                    dom.byId("dataExplorerForm_load_hadoopConnectKey").value = hadoopInfo.connectionName;
                    dom.byId("dataExplorerForm_load_hadoopFilePath").value = hadoopInfo.outputFilePath;
                    dom.byId("dataExplorerForm_load_operatorUID").value = operator.uid;
                    dom.byId("dataExplorerForm_load_flowInfoKey").value = eidtFlowInfo.key;
                    dom.byId("dataExplorerForm_load_isHDFileOperator").value = (operator.classname=="HadoopFileOperator"?true:false);
                    dom.byId("dataExplorerForm_load_method").value = "loadHisogramData4Hadoop";
                }
                window.open("about:blank",
                    window_id, get_open_window_options());
                dataExplorerForm.submit();
            });
        },"number");

    }
    //end histogram

    //start scatter plot matrix
    function showScatPlotMartix(operator){
        _getColumnNames(operator,showScatPlotMartixPage,"number",true,2);
    }

    function showScatPlotMartixPage(operator,columnNames){
        var columnNameIndex = columnNames.join(",");
        var dbInfo=replaceVriableForPojo(getDBInfo(operator));

        var dataExplorerForm = dom.byId("dataExplorerForm");
        var window_id="chart"+new Date().getTime();
        dataExplorerForm.target = window_id;
        dom.byId("dataExplorerForm_dbConnectionName").value = dbInfo.connection;
        dom.byId("dataExplorerForm_schemaName").value = dbInfo.schema;
        dom.byId("dataExplorerForm_tableName").value = dbInfo.table;
        dom.byId("dataExplorerForm_columnNameIndex").value = columnNameIndex;
        dom.byId("dataExplorerForm_columnBins").value = "";
        dom.byId("dataExplorerForm_resourceType").value = workFlowManager.getEditingFlow().type;
        dom.byId("dataExplorerForm_isGeneratedTable").value = isTableGenerated(operator);
        dom.byId("dataExplorerForm_title_key").value = "Scat_Plot_Martix";
        dom.byId("dataExplorerForm_load_method").value = "loadScatPlotMartixData";

        if(operator.operatorType == "HADOOP"){
            var hadoopInfo = replaceVriableForPojo(alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(operator));
            var eidtFlowInfo = workFlowManager.getEditingFlow();
            dom.byId("dataExplorerForm_load_hadoopConnectKey").value = hadoopInfo.connectionName;
            dom.byId("dataExplorerForm_load_hadoopFilePath").value = hadoopInfo.outputFilePath;
            dom.byId("dataExplorerForm_load_operatorUID").value = operator.uid;
            dom.byId("dataExplorerForm_load_flowInfoKey").value = eidtFlowInfo.key;
            dom.byId("dataExplorerForm_load_isHDFileOperator").value = (operator.classname=="HadoopFileOperator"?true:false);
            dom.byId("dataExplorerForm_load_method").value = "loadScatPlotMartixData4Hadoop";
        }

        window.open("about:blank", window_id, get_open_window_options());

        dataExplorerForm.submit();
    }
    //end scatter plot matrix

    //start scatter chart
    function showScatterChart(operator){
        _getColumnNames(operator,showScatterChartDialog);
    }

    function showScatterChartDialog(operator,columnNames){
        var select_IDS=[ID_ANALYSIS_COLUMN,ID_REFERENCE_COLUMN,ID_CATEGORY_COLUMN];
        bindingLabelCuter(select_IDS);
        var select_TypeS=["number","","cate_date"];
        fillSeletOptionsWithColumnNames(select_IDS,select_TypeS,columnNames);
        var obj={"columnNames":columnNames};

        var scatterPlotDialog_OKHandler = dojo.connect(dom.byId("scatterPlotDialog_OK"),"onclick",obj,function(){
            dojo.disconnect(scatterPlotDialog_OKHandler);
            showScatterChartPage(operator,obj.columnNames);

        });
        registry.byId("scatterPlotDialog").titleBar.style.display = "none";
        registry.byId("scatterPlotDialog").show();
        //alpine.props.ColumnNamePropertySelectHelper.showColumnSelectionDialog4Menu(operator,columnNames);
    }

    function showScatterChartPage(operator,columnNames){

        var dbInfo=replaceVriableForPojo(getDBInfo(operator));
        var analysisColumn=registry.byId(ID_ANALYSIS_COLUMN).value;
        var referenceColumn=registry.byId(ID_REFERENCE_COLUMN).value;
        var categoryColumn=registry.byId(ID_CATEGORY_COLUMN).value;
        if(analysisColumn==referenceColumn
            ||analysisColumn==categoryColumn
            ||referenceColumn==categoryColumn){
            popupComponent.alert(alpine.nls.anlysis_column_same);
            return;
        }
        registry.byId("scatterPlotDialog").hide();
        var url = baseURL + "/alpine/result/dataexplorer.jsp?dbConnectionName="
            + dbInfo.connection+"&schemaName="+dbInfo.schema+"&tableName="+dbInfo.table
            +"&analysisColumn="+analysisColumn+"&referenceColumn="+referenceColumn
            +"&categoryColumn="+categoryColumn+"&referenceColumnType="+getreferenceColumnType4scate(columnNames,referenceColumn)+"&resourceType="+workFlowManager.getEditingFlow().type
            +"&isGeneratedTable="+isTableGenerated(operator)
            +"&title_key="+"Scatter_Plot_Chart"
            +"&load_method="+"loadScatterData";
        var resultWindow=window.open(url,
            "_blank",get_open_window_options());

        resultWindow.focus();

    }

    function getreferenceColumnType4scate(colnumNames,referenceColumn){
        var rtnVal="";
        if(null!=colnumNames && colnumNames.length>1){
            for(var i=0;i<colnumNames.length;i++){
                if(colnumNames[i][0]==referenceColumn){
                    rtnVal = colnumNames[i][1];
                    break;
                }
            }
        }
        return rtnVal;
    }
    //end scatter chart

    //start summary statistics
    function showSummarayStatistics(operator){
        _getColumnNames(operator,showSummarayStatisticsPage,null,true,1);
    }

    function showSummarayStatisticsPage(operator ,columnNames){

//	var columnNameIndex = buildColumnNames(columnNames);
        var columnNameIndex = columnNames.join(",");
        var dbInfo=replaceVriableForPojo(getDBInfo(operator));

        var dataExplorerForm = dom.byId("dataExplorerForm");
        var window_id="chart"+new Date().getTime();
        dataExplorerForm.target = window_id;
        dom.byId("dataExplorerForm_dbConnectionName").value = dbInfo.connection;
        dom.byId("dataExplorerForm_schemaName").value = dbInfo.schema;
        dom.byId("dataExplorerForm_tableName").value = dbInfo.table;
        dom.byId("dataExplorerForm_columnNameIndex").value = columnNameIndex;
        dom.byId("dataExplorerForm_columnBins").value = "";
        dom.byId("dataExplorerForm_resourceType").value = workFlowManager.getEditingFlow().type;
        dom.byId("dataExplorerForm_isGeneratedTable").value = isTableGenerated(operator);
        dom.byId("dataExplorerForm_title_key").value = "Summary_Statistics";
        dom.byId("dataExplorerForm_load_method").value = "loadStatisticsData";

        if(operator.operatorType == "HADOOP"){
            var hadoopInfo = replaceVriableForPojo(alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(operator));
            var eidtFlowInfo = workFlowManager.getEditingFlow();
            dom.byId("dataExplorerForm_load_hadoopConnectKey").value = hadoopInfo.connectionName;
            dom.byId("dataExplorerForm_load_hadoopFilePath").value = hadoopInfo.outputFilePath;
            dom.byId("dataExplorerForm_load_operatorUID").value = operator.uid
            dom.byId("dataExplorerForm_load_flowInfoKey").value = eidtFlowInfo.key
            dom.byId("dataExplorerForm_load_isHDFileOperator").value = (operator.classname=="HadoopFileOperator"?true:false);
            dom.byId("dataExplorerForm_load_method").value = "loadStatisticsData4Hadoop";
        }

        window.open("about:blank", window_id, get_open_window_options());

        dataExplorerForm.submit();

    }
    //end summary statistics

    //start time series
    function showTimeSeriesChart(operator){
        _getColumnNames(operator,showTimeSeriesDialog);
    }

    function showTimeSeriesDialog(operator,columnNames){
        var select_IDS=[ID_IDCOLUMN,ID_VALUE_COLUMN,ID_GROUPBY_COLUMN];
        bindingLabelCuter(select_IDS);
        //todo will only support the none-time type
        var select_TypeS=["number_date","number",""];

        fillSeletOptionsWithColumnNames(select_IDS,select_TypeS,columnNames);

        dom.byId("timeSeriesDialog_OK").onclick= function(){
            showTimeSeriesPage(operator);
        };
        registry.byId("timeSeriesDialog").titleBar.style.display = "none";
        registry.byId("timeSeriesDialog").show();
    }

    function showTimeSeriesPage(operator){

        var dbInfo=replaceVriableForPojo(getDBInfo(operator));
        var idColumn=registry.byId(ID_IDCOLUMN).value;
        var valueColumn=registry.byId(ID_VALUE_COLUMN).value;
        var groupByColumn=registry.byId(ID_GROUPBY_COLUMN).value;
        if(idColumn==valueColumn){
            popupComponent.alert(alpine.nls.anlysis_column_same);
            return;
        }

        registry.byId("timeSeriesDialog").hide();
        var url =baseURL + "/alpine/result/dataexplorer.jsp?dbConnectionName="
                + dbInfo.connection+"&schemaName="+dbInfo.schema+"&tableName="+dbInfo.table
                +"&idColumn="+idColumn+"&valueColumn="+valueColumn
                +"&groupByColumn="+groupByColumn+"&resourceType="+workFlowManager.getEditingFlow().type
                +"&isGeneratedTable="+isTableGenerated(operator)
                +"&title_key="+"TimeSeries_Chart"
                +"&load_method="+"loadTimeSeriesData"
            ;
        var resultWindow=window.open(url,"_blank",get_open_window_options());

        resultWindow.focus();

    }
    //end time series

    //start univariate
    function showUnivariateChart(operator){
        //this columns should only caontain number for value as y acis
        //getColumnNames(operator,showUnivariateChartPage ,"number",true,true,false);
        _getColumnNames(operator,_showUnivariateDialog,"number",false);
    }

    function _showUnivariateDialog(operator, columnNames){
        registry.byId("univariatePlotDialog").titleBar.style.display = "none";

        var dataItems = [];
        array.forEach(columnNames, function(item){
            dataItems.push({
                colName:item[0],
                id: item[0]
            });
        });

        fillSelectOptions(registry.byId(de_refrecen_column_delect_id),columnNames,"number",true);

        new ColumnSelect({
            dialogId: "univariatePlotDialog",
            tableContainer: "univariateColumnDialogTableHolder",
            gridId: "univariateColNamesItemGrid4Property",
            filterId: "dataexplorer_columns_filter_for_univariate",
            allButton: "univariate_column_selectall",
            noneButton: "univariate_column_selectnone",
            okButton: "btn_ok_4_univariate_columnName_select",
            cancelButton: "btn_cancel_4_univariate_columnName_select",
            dataItems: dataItems,
            requiredCols: 1,
            validate: function(selectedItem){
                var referenceColumn = registry.byId(de_refrecen_column_delect_id).get("value");
                if(array.indexOf(selectedItem, referenceColumn) != -1){
                    popupComponent.alert(alpine.nls.alert_same_reference_column);
                    return false;
                }
                return true;
            },
            okButtonFn: function(selectedItem){
                showUnivariateChartPage(operator, selectedItem);
            }
        });
    }

    function showUnivariateChartPage(operator ,columnNames){
        var columnNameIndex = columnNames.join(",");
        var dbInfo=replaceVriableForPojo(getDBInfo(operator));
        var referenceColumn=registry.byId(de_refrecen_column_delect_id).value;

        var url= baseURL + "/alpine/result/dataexplorer.jsp?dbConnectionName="
                + dbInfo.connection+"&schemaName="+dbInfo.schema+"&tableName="+dbInfo.table
                +"&columnNameIndex="+columnNameIndex+"&referenceColumn="
                +referenceColumn+"&resourceType="+workFlowManager.getEditingFlow().type
                +"&isGeneratedTable="+isTableGenerated(operator)
                +"&title_key="+"Univariate_Plot_Chart"
                +"&load_method="+"loadUnivariteData"
            ;
        var resultWindow=window.open(url,
            "_blank",get_open_window_options());
        resultWindow.focus();

    }
    //end univariate

    //start table metadata
    function show_table_metadata(operator, contentPanelId){
        workFlowVariableReplacer.init();
        var dbInfo = replaceVriableForPojo(getDBInfo(operator));
        if(!dbInfo.connection || !dbInfo.schema || !dbInfo.table){
            popupComponent.alert(alpine.nls.leak_datasource_to_dataexplorer);
            return;
        }
        var url = baseURL + "/main/dataexplorer.do?method=getTableMetadata" +
            "&dbConnName=" + dbInfo.connection +
            "&dbSchemaName=" + dbInfo.schema +
            "&dbTableName=" + dbInfo.table +
            "&operatorClass=" + operator.classname +
            "&resourceType=" + workFlowManager.getEditingFlow().type;
        ds.get(url,function(metadata){
            workFlowVariableReplacer.finalize();
            if(metadata.error_code){
                handle_error_result(metadata);
                return;
            }
            buildTableMetaDataGrid(metadata);
        },function(text,args){
            workFlowVariableReplacer.finalize();
        }, false, contentPanelId);
    }

    function buildTableMetaDataGrid(result){
        registry.byId("tableColumnDialog").titleBar.style.display = "none";
        registry.byId("tableColumnDialog").show();
        var grid = registry.byId("tableColumnGrid");
        var dataStore = new dojo.data.ItemFileWriteStore({
            data: {
                items: result
            }
        });
        if(!grid){
            grid = new dojox.grid.DataGrid({
                store: dataStore,
                query: { "columnName": "*" },
                structure: [
                    {name: alpine.nls.table_column_grid_head_columnName,field: "columnName",width: "50%"},
                    {name: alpine.nls.table_column_grid_head_columnType,field: "columnType",width: "50%"}
                ]
            },"tableColumnGrid");
            grid.startup();
        } else {
            grid.setStore(dataStore);
        }
    }

    //end table metadata

    function isTableGenerated(operator){
        var isTableGenerated = operator.classname != "DbTableOperator" && operator.hasDbTableInfo;
        if(isTableGenerated != true && operator.classname == "PivotOperator"){
            isTableGenerated == true;
        }
        return isTableGenerated;
    }

    function _isHadoopOperator(operator){
        var isHadoopOperator = false;
        if(null!=operator.operatorType && operator.operatorType.toUpperCase()=="HADOOP"){
            isHadoopOperator = true;
        }
        return isHadoopOperator;
    }

    return {
        getColumnNames: _getColumnNames,
        showTableData: showTableData,
        showBarChart: showBarChart,
        showBoxAndWiskerChart: showBoxAndWiskerChart,
        showCorrelationAnalysis: showCorrelationAnalysis,
        showFrequencyAnalysis: showFrequencyAnalysis,
        showHistogramChart: showHistogramChart,
        showScatPlotMartix: showScatPlotMartix,
        showScatterChart: showScatterChart,
        showSummarayStatistics: showSummarayStatistics,
        showTimeSeriesChart: showTimeSeriesChart,
        showUnivariateChart: showUnivariateChart,
        show_table_metadata: show_table_metadata
    }

});
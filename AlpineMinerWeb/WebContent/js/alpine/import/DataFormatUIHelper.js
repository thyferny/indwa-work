/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * ImportDataUIHelper.js 
 * Author Gary
 * Aug 8, 2012
 */
define([
    "dojo/_base/lang",
    "alpine/import/ImportDataManager",
    "alpine/datasourceexplorer/DatabaseExplorerUIHelper",
    "alpine/import/CSVUtil",
    "alpine/datasourceexplorer/DataSourceExplorerUIHelper",
    "alpine/util/ValidationHelper",
    "alpine/util/DataTypeUtils"
], function(lang, importHandler, databaseHandler, csvUtil, datasourceHandler, validationHelper, dataTypeUtils){
    var constants = {
            DIALOG: "alpine_import_dataformat_dialog",
            FORM: "alpine_import_dataformat_form",
            TABLE_NAME: "alpine_import_dataformat_tablename",
            PREVIEW_GRID: "alpine_import_dataformat_formatGrid",
            DELIMITER_SELECTOR: "alpine_import_dataformat_option_delimiter",
            DELIMITER_INPUT: "alpine_import_dataformat_delimiter",
            ESCAPE_INPUT: "alpine_import_dataformat_option_escape",
            QUOTE_INPUT: "alpine_import_dataformat_option_quote",
            INCLUDE_HEADER: "alpine_import_dataformat_option_header",
            FORMAT_SUBMIT: "alpine_import_dataformat_format_submit",
            FORMAT_ABORT: "alpine_import_dataformat_format_abort",
            FORMAT_CANCEL: "alpine_import_dataformat_format_cancel",
            CONFIG_SWITCHER: "alpine_import_dataformat_option_switcher",
            CONFIG_CONTAINER: "alpine_import_dataformat_option_container",
            COLUMN_NAME_PREFIX: "columnName_",
            COLUMN_TYPE_PREFIX: "importColumnHeadSelect_",
            COLUMN_CONF_AREA: "columnConfArea",
            COLUMN_IS_INCLUDE_PREFIX: "columnIsInclude_",
			COLUMN_ALLOW_EMPTY_PREFIX: "columnAllowEmpty_",
			
			IMPORT_ERROR_DIALOG: "alpine_import_dataformat_errorMsgDialog",
			IMPORT_ERROR_GRID: "alpine_import_dataformat_errorMsgGrid"
    };
    var selectImportWidgets = [];
    var columnNameWidgets = [];
    var configWidgets = [];
    var widgetsEvents = [];
    var errorMsgList;
    
    var dataTypes = ["VARCHAR","BIGINT","BOOLEAN","DATE","DATETIME","DOUBLE","INTEGER","NUMERIC","CHAR"];
    var uploadData = null;
    var isConfigShown = false;
    var limitSize = null;
    var columnSize = null;

    dojo.ready(function(){
        dojo.connect(dijit.byId(constants.DIALOG), "onShow", function(){
            dijit.byId(constants.DIALOG).titleBar.style.display='none';
            
            // give default value to parameter fields.
            dijit.byId(constants.ESCAPE_INPUT).set("value", "\\");
            dijit.byId(constants.QUOTE_INPUT).set("value", "\"");
        });
        dojo.connect(dijit.byId(constants.DIALOG), "onFocus", function(){
        	dojo.byId(constants.TABLE_NAME).focus();
        	dojo.byId(constants.TABLE_NAME).select();
        });
        dojo.connect(dojo.byId(constants.CONFIG_SWITCHER), "onclick", function(){
            isConfigShown = !isConfigShown;
            if(isConfigShown == true){
                dojo.byId(constants.CONFIG_SWITCHER).innerHTML = alpine.nls.import_data_pane_format_label_hide_additional_option;
                dojo.style(dojo.byId(constants.CONFIG_CONTAINER), "display", "");
                dojo.style(dojo.byId(constants.COLUMN_CONF_AREA), "display", "");
                var containerHeight = dojo.contentBox(dojo.byId(constants.CONFIG_CONTAINER)).h;
                var tableMaxHeight = 450 - containerHeight;
                dojo.style(dojo.byId(constants.PREVIEW_GRID), "height", tableMaxHeight+"px");
            }else{
                dojo.byId(constants.CONFIG_SWITCHER).innerHTML = alpine.nls.import_data_pane_format_label_show_additional_option;
                dojo.style(dojo.byId(constants.CONFIG_CONTAINER), "display", "none");
                dojo.style(dojo.byId(constants.COLUMN_CONF_AREA), "display", "none");
                dojo.style(dojo.byId(constants.PREVIEW_GRID), "height", "450px");
            }
        });
        dojo.connect(dijit.byId(constants.DELIMITER_SELECTOR), "onChange", function(val){
            var isOther = val == "Other";
            if(!isOther){
                buildFileStructureContentGrid(uploadData, true);
            }else{
                dijit.byId(constants.DELIMITER_INPUT).reset();
            }
            dijit.byId(constants.DELIMITER_INPUT).set("disabled", !isOther);
            dojo.style(dijit.byId(constants.DELIMITER_INPUT).domNode, "display", isOther ? "" : "none");
        });
        dojo.connect(dijit.byId(constants.INCLUDE_HEADER), "onChange", function(){
            buildFileStructureContentGrid(uploadData, false);
        });

        dojo.connect(dijit.byId(constants.DELIMITER_INPUT), "onBlur", function(){
            if(this.validate()){
                buildFileStructureContentGrid(uploadData, true);
            }
        });

        dojo.connect(dijit.byId(constants.ESCAPE_INPUT), "onChange", function(){
            if(this.validate()){
                buildFileStructureContentGrid(uploadData, true);
            }
        });

        dojo.connect(dijit.byId(constants.QUOTE_INPUT), "onChange", function(){
            if(this.validate()){
                buildFileStructureContentGrid(uploadData, true);
            }
        });
        
        dojo.connect(dijit.byId(constants.FORMAT_SUBMIT), "onClick", function(){
        	var isValidate = dijit.byId(constants.FORM).validate();
        	if(!isValidate){
        		return;
        	}
        	importData();
        });
        
        dojo.connect(dijit.byId(constants.FORMAT_ABORT), "onClick", function(){
        	importHandler.abortImport(function(){
            	switchImportButton(false);
        	});
        });

        dojo.connect(dijit.byId(constants.FORMAT_CANCEL), "onClick", function(){
            importHandler.release(function(){
                _release();
                dijit.byId(constants.DIALOG).hide();
            });
        });
    });

    /*
     * args
     *  .dataArray
     *  .fileName
     */
    function _startup(args){
        uploadData = args.dataArray;
        var fileName;
        if (args.fileName.indexOf(".") != -1) {
            fileName = args.fileName.substring(0, args.fileName.indexOf("."));
        } else {
            fileName = args.fileName;
        }
        dijit.byId(constants.TABLE_NAME).set('value', fileName);
        if (uploadData[0] != null) {
            var guessedDelimiter = csvUtil.guessDelimiter(uploadData[0]);
            dijit.byId(constants.DELIMITER_SELECTOR).set('value',guessedDelimiter);
        }

        var rowDatas = buildFileContent(uploadData);
        var colSize = _getMaxColumnNumber(rowDatas);
        if(colSize>800){
            var errorTip = alpine.nls.hadoop_prop_file_structure_split_max_column_tip.replace("###",""+800);
            popupComponent.alert(errorTip);
            return false;
        }

        buildFileStructureContentGrid(uploadData, true);
        dijit.byId(constants.DIALOG).show();
        limitSize = args.limitSize;
    }



    function buildFileStructureContentGrid(data, clearSetting){
        var rowDatas = buildFileContent(data);
        columnSize = _getMaxColumnNumber(rowDatas);
        if(clearSetting == true){
            var tableDom = buildTable();
            if(tableDom != null){
                buildTableHeader(tableDom, rowDatas, columnSize);
                var dataBody = dojo.create("tbody", {id: "alpineImportDataBody", className: "alpineImportDataTableBody"} ,tableDom);
                buildTableBody(dataBody, rowDatas, columnSize);
            }
        }else{
            var tableBody = dojo.byId("alpineImportDataBody");
            dojo.empty(tableBody);
            buildTableBody(tableBody, rowDatas, columnSize);
        }
    }

    function buildFileContent(rowDatas){
        var returnArray = [];
        if(rowDatas != null){
            var csvParams = {
                separator: getRealDeliminter(dijit.byId(constants.DELIMITER_SELECTOR).get("value")),
                quote: dijit.byId(constants.QUOTE_INPUT).get("value"),
                escaped: dijit.byId(constants.ESCAPE_INPUT).get("value")
            };
            returnArray = csvUtil.csvArrayToArrayOfArrays(rowDatas, csvParams);
        }
        return returnArray;
    }

    function getRealDeliminter(deliminter){
        switch (deliminter){
            case "Comma":
                return ",";
            case "Tab":
                return "\u0009";  //\t
                break;
            case "Semicolon":
                return ";";
                break;
            case "Space":
                return "\u0020";
                break;
            case "Pipe":
                return "\u007c";
                break;
            case "Other":
                return dijit.byId(constants.DELIMITER_INPUT).get("value");
                break;
            default:
               return ",";
        }
    }

    function _getMaxColumnNumber(rowDatas){
          if(rowDatas != null && rowDatas.length > 0){
              return rowDatas[0].length;
          }
          return 0;
    }

    function buildTable(){
        var container = dojo.byId(constants.PREVIEW_GRID);
        dojo.empty(container);
        return  dojo.create("table", {className: "alpineImportDataTable"} , container);
    }

    function buildTableHeader(tableDom, rowDatas, maxColumnNumber){
       clearWidgets();
       var hasHeader = dijit.byId(constants.INCLUDE_HEADER).get("checked");
       var tableHead = dojo.create("thead",null,tableDom);
       var trColumnNames = dojo.create("tr", {className: "alpineImportDataColName"} ,tableHead);
       var trColumnTypes = dojo.create("tr", {className: "alpineImportDataColType"} ,tableHead);
       var trColumnConf = dojo.create("tr", {className: "alpineImportDataColConf", style: "display: " + (isConfigShown == true ? "" : "none"), id: constants.COLUMN_CONF_AREA} ,tableHead);
       if(maxColumnNumber > 0){
           var selectOption = _buildSelectOption(dataTypes);
           for(var i = 0;i < maxColumnNumber;i++){
               var nameTd = dojo.create("td",{align:"center",valign:"middle",className:"alpineImportDataColName"},trColumnNames);
               var columnNameTextbox = new dijit.form.ValidationTextBox({
                   id: constants.COLUMN_NAME_PREFIX + i,
                   required: true,
                   baseClass: "alpineImportTextbox",
                   regExp: validationHelper.columnName
               }, dojo.create("div",{},nameTd));
               columnNameWidgets.push(columnNameTextbox);

               var theadSelectTd = dojo.create("td",{className:"alpineImportDataColType"},trColumnTypes);
               var columnSelect = new dijit.form.Select({
                   id:constants.COLUMN_TYPE_PREFIX + i,
                   options: dojo.clone(selectOption),
                   baseClass: "greyDropdownButton",
                   align:"center"
               }, dojo.create("div",{},theadSelectTd));
               selectImportWidgets.push(columnSelect);

               //_guessDataTypes(rowDatas, i, hasHeader);
               var typeCol = dataTypeUtils.getTypeOfArray(rowDatas, i, hasHeader, "db");
               dijit.byId(constants.COLUMN_TYPE_PREFIX + i).set('value', typeCol);
               
               var theadConfTd = dojo.create("td", {className: "alpineImportDataColType"}, trColumnConf);
               var includeCheckbox = new dijit.form.CheckBox({
                   id: constants.COLUMN_IS_INCLUDE_PREFIX + i,
                   checked: true
               }, dojo.create("div", {}, theadConfTd));
               selectImportWidgets.push(includeCheckbox);
               dojo.create("label", {innerHTML: alpine.nls.import_data_pane_format_label_include}, theadConfTd);

               dojo.create("br", {}, theadConfTd);

               var allowEmptyCheckbox = new dijit.form.CheckBox({
                   id: constants.COLUMN_ALLOW_EMPTY_PREFIX + i,
                   checked: true
               }, dojo.create("div", {}, theadConfTd));
               selectImportWidgets.push(allowEmptyCheckbox);
               dojo.create("label", {innerHTML: alpine.nls.import_data_pane_format_label_allow_empty}, theadConfTd);

           }
       }
    }

    function _buildSelectOption(dataTypes){
        var selectedOption = [];
        if(null!=dataTypes && dataTypes.length>0){
            for(var i=0;i<dataTypes.length;i++){
                selectedOption.push({label:dataTypes[i],value:dataTypes[i]});
            }
        }
        return selectedOption;
    }

    function buildTableBody(tableBody, rowDatas, maxColumnNumber){
        if(maxColumnNumber > 0 && rowDatas != null){
            if (dijit.byId(constants.INCLUDE_HEADER).get("checked")==true) {
                // fill headers, start data at 1
                _fillHeaders(rowDatas[0], maxColumnNumber);
                for(var i = 1;i < rowDatas.length;i++){
                    var tbodyTR = dojo.create("tr",{},tableBody);
                    if(null!=rowDatas[i] && ""!=rowDatas[i]){
                        _buildTbodyTDS(tbodyTR, rowDatas[i], maxColumnNumber);
                    }
                }
            } else {
                for(var i = 0;i < columnNameWidgets.length;i++){
                    columnNameWidgets[i].reset();
                }
                // no headers, start data at 0
                for(var i = 0;i < rowDatas.length;i++){
                    var tbodyTR = dojo.create("tr",{},tableBody);
                    if(null!=rowDatas[i] && ""!=rowDatas[i]){
                        _buildTbodyTDS(tbodyTR, rowDatas[i], maxColumnNumber);
                    }
                }
            }
        }
    }

    function _buildTbodyTDS(tbodyTR,columnData,maxColNum){
        for(var i=0;i<maxColNum;i++){
            var tdDivContainer = dojo.create('td',{},tbodyTR);
            dojo.create('div',{innerHTML:columnData[i]!=null?columnData[i]:""},tdDivContainer);
        }
    }

    function clearWidgets(){
        if(selectImportWidgets.length > 0){
            for(var i=0;i<selectImportWidgets.length;i++){
                selectImportWidgets[i].destroyRecursive();
            }
            selectImportWidgets = [];
        }
        if(columnNameWidgets.length > 0){
            for(var i = 0;i < columnNameWidgets.length;i++){
                columnNameWidgets[i].destroyRecursive();
            }
            columnNameWidgets = [];
        }
        if(configWidgets.length > 0){
            for(var i = 0;i < configWidgets.length;i++){
                configWidgets[i].destroyRecursive();
            }
            configWidgets = [];
        }
        var event = null;
        while((event = widgetsEvents.pop()) != undefined){
            dojo.disconnect(event);
        }
    }

    function _fillHeaders(headers, maxColumnNumber) {
        for(var j=0;j<maxColumnNumber;j++){
            //fill in column names...
            var col_text_id = constants.COLUMN_NAME_PREFIX + j;
            dijit.byId(col_text_id).set('value', dojo.trim(headers[j]));
        }
    }

    // refactored to DataTypeUtils
    /*function _guessDataTypes(dataRows, counter, hasHeader) {
        var i = (hasHeader || false) ? 1 : 0;
        var isFloat = true;
        var isInt = true;
        var needsBigInt = false;
        var hasNonNull = false;
        var num;
        var typeCol = "VARCHAR";
        for (i; i < dataRows.length; i++) {
            num = dataRows[i][counter];
            if (!num) {
                continue;
            }
            if (isNaN(num)){
                isFloat = false;
                isInt = false;
                break;
            } else if (num != parseInt(num) || /\./g.test(num)) {
                isInt = false;
                hasNonNull = true;
            } else {
                if (Math.abs(num) > 2147483647) {needsBigInt = true;}
                hasNonNull = true;
            }
        }
        if (isInt) {
            typeCol = needsBigInt ? "BIGINT" : "INTEGER";
        } else if (isFloat) {
            typeCol = "NUMERIC";
        }
        if (!hasNonNull) {
            typeCol = "VARCHAR";
        }
        var typeCol = dataTypeUtils.getTypeOfArray(dataRows, counter, hasHeader, "db");
        dijit.byId(constants.COLUMN_TYPE_PREFIX + counter).set('value', typeCol);
    }*/
    
    function buildColumnStructure(){
    	var columnStructureSet = new Array();
    	for(var i = 0;i < columnSize;i++){
    		columnStructureSet.push({
    			columnName: dijit.byId(constants.COLUMN_NAME_PREFIX + i).get("value"),
    			columnType: dijit.byId(constants.COLUMN_TYPE_PREFIX + i).get("value"),
    			isInclude: dijit.byId(constants.COLUMN_IS_INCLUDE_PREFIX + i).get("checked"),
    			allowEmpty: dijit.byId(constants.COLUMN_ALLOW_EMPTY_PREFIX + i).get("checked")
    		});
    	}
    	return columnStructureSet;
    }

    function importData(){
    	var databaseInfo = databaseHandler.getConnAndSchema();
    	var config = {
    		connectionName: databaseInfo.connection,
			schemaName: databaseInfo.schema,
			tableName: dijit.byId(constants.TABLE_NAME).get("value"),
			delimiter: getRealDeliminter(dijit.byId(constants.DELIMITER_SELECTOR).get("value")),
			quote: dijit.byId(constants.QUOTE_INPUT).get("value"),
			escape: dijit.byId(constants.ESCAPE_INPUT).get("value"),
			limitNum: limitSize,
			includeHeader: dijit.byId(constants.INCLUDE_HEADER).get("checked"),
			structure: buildColumnStructure()
    	};
    	switchImportButton(true);
    	importHandler.importData(config, function(data){
    		var msg;
    		var toasterType;
        	switchImportButton(false);
    		if(data.failureInfoSet.length > 0){
                if (data.type == "ERROR") {
        			toasterType = "error";
                    msg = "<a href=\"#\" onclick=\"alpine.import.DataFormatUIHelper.showErrMsgDlg();\">" + alpine.nls.import_data_message_failure + "</a>";
                }else {
                    if (data.failureInfoSet.length >= 100) {
                        msg = "<a href=\"#\" onclick=\"alpine.import.DataFormatUIHelper.showErrMsgDlg();\">" + lang.replace(alpine.nls.import_data_message_error100,{
                            hundrederrors: data.failureInfoSet.length
                        }) + "</a>";
                    } else {
                        msg = "<a href=\"#\" onclick=\"alpine.import.DataFormatUIHelper.showErrMsgDlg();\">" + lang.replace(alpine.nls.import_data_message_error,{
                            errCount: data.failureInfoSet.length
                        }) + "</a>";
                    }
        			toasterType = "warning";
                }
    			errorMsgList = data.failureInfoSet;
    		}else if(data.type == "ABORT"){
    			msg = alpine.nls.import_data_message_abort;
    			toasterType = "error";
    		}else{
    			msg = alpine.nls.import_data_message_success;
    			toasterType = "message";
    		}
    		dojo.publish("toasterMessage", [{
    			message: msg,
    			type: toasterType,
				duration: "10000"
    		}]);
            if (toasterType == "message"){
                importHandler.release(function(){
                    _release();
                    dijit.byId(constants.DIALOG).hide();
                });
            }

        	datasourceHandler.refreshCurrentLevel();//refresh data source explorer
    	}, function(){
        	switchImportButton(false);
    	});
    	importHandler.readProgress({
    		onMsg: function(rowNum){
    			dojo.publish("toasterMessage_ru", [{
        			message: "Import in progress: " + rowNum + " rows completed",
        			type: "message",
    				duration: "2000"
        		}]);
    		}
    	});
    }
    
    function showErrorMsgDialog(){
    	dijit.byId(constants.IMPORT_ERROR_DIALOG).show();
    	var errGrid = dijit.byId(constants.IMPORT_ERROR_GRID);
		var dataStore = new dojo.data.ItemFileReadStore({
			data: {					
				identifier: "rowNum",
				items: errorMsgList
			}
		});
    	if(!errGrid){
    		errGrid = new dojox.grid.DataGrid({
				store: dataStore,
				query: { "rowNum": "*" },
				structure: [
		           	[
			            {name: alpine.nls.import_data_error_grid_header_rownum, field: "rowNum", width: "20%"},
			            {name: alpine.nls.import_data_error_grid_header_message, field: "failureMessage", width: "80%"}
					]
				]
			},constants.IMPORT_ERROR_GRID);
    		errGrid.startup();
    	}else{
    		errGrid.setStore(dataStore);
    	}
    }
    
    function switchImportButton(isImporting){
    	dojo.style(dijit.byId(constants.FORMAT_SUBMIT).domNode, "display", (isImporting == true ? "none" : ""));
    	dojo.style(dijit.byId(constants.FORMAT_ABORT).domNode, "display", (isImporting == true ? "" : "none"));
    }

    function _release(){
        isConfigShown = false;
        limitSize = null;
        errorMsgList = null;
        dojo.byId(constants.CONFIG_SWITCHER).innerHTML = alpine.nls.import_data_pane_format_label_show_additional_option;
        dojo.style(dojo.byId(constants.CONFIG_CONTAINER), "display", "none");
        dojo.style(dojo.byId(constants.COLUMN_CONF_AREA), "display", "none");
        dijit.byId(constants.DELIMITER_SELECTOR).reset();
        dijit.byId(constants.ESCAPE_INPUT).reset();
        dijit.byId(constants.QUOTE_INPUT).reset();
    	switchImportButton(false);
    }

      return {
          startup: _startup,
          showErrMsgDlg: showErrorMsgDialog
      };
});
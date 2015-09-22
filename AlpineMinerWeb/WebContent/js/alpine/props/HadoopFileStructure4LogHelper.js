/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopFileStructure4LogHelper
 * Author: Will
 * Date: 12-10-24
 */
define(["dojo/dom-attr",
    "dojo/dom-style",
    "alpine/props/HadoopFileStructure4LogManager",
    "alpine/props/HadoopDataTypeUtil"
],
    function (domAttr, domStyle, fileLogMgmt, hdDataTypeUitl) {

        var DIALOG_ID = "hadoopFileStructureCfgDlg4Log";
        var SUB_TITLE = "hadoopPropery_Config_Colum_file_name4log";
        var Btn_Cancel = "hadoopFileStructure_4Log_Dlg_Btn_Cancel";
        var Btn_OK = "hadoopFileStructure_4Log_Dlg_Btn_OK";
        var Btn_Preview = "hadoop_prop_file_structure_log_preview";
        var TYPE_SELECT = "structure4log_fileType";
        var FORMAT_INPUT = "hadoop_prop_file_structure_Log_input_format";
        var FORMAT_INPUT_CONTAINER = "hadoop_prop_file_structure_Log_input_format_Container";
        var PREVIEW_GRID_CONTAINER = "log_preview_container";
        var PREVIEW_GRID_ID = "log_preview_grid";

        /*
         * logFormat
         * logType
         * columnNameList[]
         * columnTypeList[]
         * */
        var structureModel = null;
        var sourceButtonId = null;
        var currentProp = null;
        var onDlgLoad = true;
        var columnTypeWidgets = [];
        var columnNameWidgets = [];
        var previewFromButton = false;
        var MAX_GUESS_ROW = 100;

        dojo.ready(function () {
            dojo.connect(dijit.byId(Btn_Cancel), "onClick", cancelBtnClick);
            dojo.connect(dijit.byId(Btn_OK), "onClick", saveBtnClick);
            //dojo.connect(dijit.byId(),"Btn_Cancel",cancelBtnClick);
            //dojo.connect(dijit.byId(FORMAT_INPUT),"onChange",function(){});
            dojo.connect(dijit.byId(TYPE_SELECT), "onChange", updateFormatDefaultValue);

            dojo.connect(dijit.byId(FORMAT_INPUT), "onMouseOver", setOnLoadFalse);
            dojo.connect(dijit.byId(TYPE_SELECT), "onMouseOver", setOnLoadFalse);

            dojo.connect(dijit.byId(Btn_Preview), "onClick", function () {
                previewFromButton = true;
                previewData();
            });

        });

        function showFileStructure4LogDialog(subtitle, prop) {
            onDlgLoad = true;
            var dlg = dijit.byId(DIALOG_ID);
            if (null != dlg) {
                dlg.show();
                dojo.style(dlg.containerNode, {width:"700px", height:"550px", overflow:"hidden"});
                domStyle.set(dlg.titleBar, "display", "none");
                //domAttr.set(dojo.byId(SUB_TITLE), "innerHTML", subtitle);
                var tooltipValue = subtitle;
                if (subtitle != null && subtitle.length > 30) {
                    subtitle = subtitle.substring(0, 27) + "...";
                }
                domAttr.set(dojo.byId(SUB_TITLE), "innerHTML", subtitle);
                domAttr.set(dojo.byId(SUB_TITLE), "title", tooltipValue);
                sourceButtonId = getSourceButtonId(prop);
                currentProp = prop;
                if (prop.alpineLogFileStructureModel == null) {
                    prop.alpineLogFileStructureModel = getEmptyStructureModel();
                }
                structureModel = prop.alpineLogFileStructureModel
                //init ui status
                if (structureModel.logFormat == null) {
                    structureModel.logFormat = "";
                }
                if (structureModel.logType == null) {
                    structureModel.logType = "";
                }
                dijit.byId(FORMAT_INPUT).set("value", structureModel.logFormat);
                dijit.byId(TYPE_SELECT).set("value", structureModel.logType);

                //empty dom
                dojo.empty(PREVIEW_GRID_CONTAINER);
                _emptyWidgets();

                //build data grid
                if (dijit.byId(FORMAT_INPUT).get("value") != "" && dijit.byId(TYPE_SELECT).get("value") != "") {
                    //get datas
                    var hadoopConName = dijit.byId("connName" + ID_TAG).get("value");
                    var connectionKey = alpine.props.HadoopFileOperatorPropertyHelper.getConnectionKey(hadoopConName);
                    var path = dijit.byId("hadoopFileName" + ID_TAG).get("value");
                    var logFormat = dijit.byId(FORMAT_INPUT).get("value");
                    var logType = dijit.byId(TYPE_SELECT).get("value");
                    var eidtFlowInfo = alpine.flow.WorkFlowManager.getEditingFlow();
                    var flowInfoKey = eidtFlowInfo.key;
                    var selectOperator = alpine.flow.OperatorManagementUIHelper.getSelectedOperator(true);
                    var opUID = selectOperator.uid;
                    fileLogMgmt.getHadoopLogFileContents(connectionKey, path, flowInfoKey, opUID, logFormat, logType, 200, "preview", getLogFileContentsCallBack);
                }

            }
        }

        ;
        /*
         *  private CSVFileStructureModel csvFileStructureModel;
         private XMLFileStructureModel xmlFileStructureModel;
         private AlpineLogFileStructureModel alpineLogFileStructureModel;

         type=log,csv,xml,json
         */
        function clearStructueModel(prop, type) {
            if (type == "log") {
                prop.xmlFileStructureModel = undefined;
                //prop.alpineLogFileStructureModel=undefined;
                prop.csvFileStructureModel = undefined;
                prop.jsonFileStructureModel = undefined;
            } else if (type == "csv") {
                prop.xmlFileStructureModel = undefined;
                prop.alpineLogFileStructureModel = undefined;
                //prop.csvFileStructureModel = undefined;
                prop.jsonFileStructureModel = undefined;
            } else if (type == "xml") {
                // prop.xmlFileStructureModel=undefined;
                prop.alpineLogFileStructureModel = undefined;
                prop.csvFileStructureModel = undefined;
                prop.jsonFileStructureModel = undefined;
            } else if (type == "json") {
                prop.xmlFileStructureModel = undefined;
                prop.alpineLogFileStructureModel = undefined;
                prop.csvFileStructureModel = undefined;
                //prop.jsonFileStructureModel= undefined;
            }
        }

        ;

        function saveBtnClick() {
            if (saveLogStructureModel() == false) {
                return false;
            }

            hideDialog();
        }

        ;

        function saveLogStructureModel() {
            var format = dijit.byId(FORMAT_INPUT).get("value");
            var logType = dijit.byId(TYPE_SELECT).get("value");
            //validate model
            if ("" == format) {
                popupComponent.alert("Please input format value!");
                return false;
            }
            if (logType == "") {
                popupComponent.alert("Please select Log Type !");
                return false;
            }
            var columnNames = [];
            if (columnTypeWidgets != null) {
                for (var i = 0; i < columnTypeWidgets.length; i++) {
                    columnNames.push(columnNameWidgets[i].get("value"));
                }
            }
            if (hasEmptyColumnName(columnNames) == true) {
                return false;
            }
            if (hasSameColumnName(columnNames) == true) {
                return false;
            }

            if (isColumnValid(columnNames) == false) {
                return false;
            }
            if (hasHadoopKeyWord(columnNames) == true) {
                return false;
            }

            //save model
            //structureModel = {};
            structureModel.logFormat = format;
            structureModel.logType = logType;

            structureModel.columnNameList = [];
            structureModel.columnTypeList = [];
            //columnNames columnTypes
            if (columnTypeWidgets != null) {
                for (var i = 0; i < columnTypeWidgets.length; i++) {
                    structureModel.columnTypeList.push(columnTypeWidgets[i].get("value"));
                }
            }
            if (columnNameWidgets != null) {
                for (var i = 0; i < columnNameWidgets.length; i++) {
                    structureModel.columnNameList.push(columnNameWidgets[i].get("value"));
                }
            }

            clearStructueModel(currentProp, "log");

            setButtonBaseClassValid(sourceButtonId);
            return true;//same success
        }

        ;

        //--------------------validate begin---------------
        function hasEmptyColumnName(columnNames) {
            if (columnNames.length == 0) {
                popupComponent.alert(alpine.nls.hadoop_prop_file_structure_column_4json_define_tip);
                return true;
            }
            for (var i = 0; i < columnNames.length; i++) {
                if (columnNames[i] == "") {
                    popupComponent.alert(alpine.nls.hadoop_prop_file_structure_column_4json_empty_tip);
                    return true;
                }
            }
            return false;
        }

        ;

        function hasSameColumnName(columnNames) {
            columnNames.sort();
            for (i = 0; i < columnNames.length - 1; i++) {
                if (columnNames[i] == columnNames[i + 1]) {
                    var msg = alpine.nls.hadoop_prop_file_structure_xml_column_repeat_tip.replace("###", "'" + columnNames[i] + "'");
                    popupComponent.alert(msg);
                    return true;
                }
            }
            return false;
        }

        function isColumnValid(nameList) {
            //nameList.sort();
            for (i = 0; i < nameList.length; i++) {
                if (/^[a-zA-Z][a-zA-Z0-9_]{0,}$/.test(nameList[i]) == false) {
                    popupComponent.alert(alpine.nls.hadoop_prop_file_structure_tip_columnHeader_is_invalid.replace("##","\"" + nameList[i] + "\""));
                    return false;
                }
            }
            return true;
        }

        function hasHadoopKeyWord(dataItems) {
            if (dataItems != null && dataItems.length > 0) {
                for (var i = 0; i < dataItems.length; i++) {
                    if (hdDataTypeUitl.isHadoopKeyWord(dataItems[i]) == true) {
                        var tip = alpine.nls.hadoop_prop_file_structure_column_name_keyword_tip.replace("###", "'" + dataItems[i] + "'");
                        popupComponent.alert(tip);
                        return true;
                    }
                }
            }
            return false;
        }

        //--------------------validate end ----------------

        function cancelBtnClick() {
            hideDialog();
        }

        ;

        function getEmptyStructureModel() {
            structureModel = {};
            structureModel.logFormat = "";
            structureModel.logType = "";
            structureModel.columnNameList = [];
            structureModel.columnTypeList = [];
            return structureModel;
        }

        function hideDialog() {
            var dlg = dijit.byId(DIALOG_ID);
            if (null != dlg) {
                onDlgLoad = true;
                dlg.hide();

            }
        }

        ;

        function previewData(fromButton) {
            if (dijit.byId(FORMAT_INPUT).get("value") != "" && dijit.byId(TYPE_SELECT).get("value") != "") {
                _emptyModelNamesAndTypes();
                //get datas
                var hadoopConName = dijit.byId("connName" + ID_TAG).get("value");
                var connectionKey = alpine.props.HadoopFileOperatorPropertyHelper.getConnectionKey(hadoopConName);
                var path = dijit.byId("hadoopFileName" + ID_TAG).get("value");
                var logFormat = dijit.byId(FORMAT_INPUT).get("value");
                var logType = dijit.byId(TYPE_SELECT).get("value");
                var eidtFlowInfo = alpine.flow.WorkFlowManager.getEditingFlow();
                var flowInfoKey = eidtFlowInfo.key;
                var selectOperator = alpine.flow.OperatorManagementUIHelper.getSelectedOperator(true);
                var opUID = selectOperator.uid;
                fileLogMgmt.getHadoopLogFileContents(connectionKey, path, flowInfoKey, opUID, logFormat, logType, 200, "preview", getLogFileContentsCallBack);
            } else {
                dojo.empty(PREVIEW_GRID_CONTAINER);
                _emptyWidgets();
            }

        }

        ;

        function setOnLoadFalse() {
            onDlgLoad = false;
        }

        ;

        function getLogFileContentsCallBack(data) {
            if (data.error != null) {
                //popupComponent.alert("");
                setGridPreviwErrorInfo(data.error);
                return;
            }
            //
            dojo.empty(PREVIEW_GRID_CONTAINER);
            _emptyWidgets();
            var previewGrid = dojo.create("table", {className:"alpineImportDataTable", id:PREVIEW_GRID_ID}, PREVIEW_GRID_CONTAINER);
            buildPreviewTable(data, previewGrid);
        }

        ;

        function buildPreviewTable(data, previewGrid) {
            var columnNames = data.columnNames;
            var columnTypes = data.columnTypes;
            var modelColumnTypes = structureModel.columnTypeList;
            var contents = data.contents;

            var guessColumnTypes =  {
                guess:false,
                dataTypes:[]
            };

            if (previewFromButton == true) {
                previewFromButton = false;
                guessColumnTypes.guess = true;
                guessColumnTypes.dataTypes = _gessDataTypesfromContents(columnNames,contents);
            }

            var tableHead = dojo.create("thead", null, previewGrid);
            var trHeadeName = dojo.create("tr", {className:"alpineImportDataColName"}, tableHead);
            var trHeadeType = dojo.create("tr", {className:"alpineImportDataColName"}, tableHead);
            var tableBody = dojo.create("tbody", {className:"alpineImportDataTableBody"}, previewGrid);
            if (columnNames != null && columnTypes != null) {
                for (var i = 0; i < columnNames.length; i++) {
                    var tdName = dojo.create("td", {align:"center", valign:"middle", className:"alpineImportDataColName"}, trHeadeName);
                    var tdType = dojo.create("td", {align:"center", valign:"middle", className:"alpineImportDataColName"}, trHeadeType);
                    var headerNameBox = new dijit.form.ValidationTextBox({
                        baseClass:"alpineImportTextbox",
                        //id: "columnHeadTitle_" + i,
                        required:true,
                        isValid:function () {
                            var val = this.get('value');
                            var check = this.validator(val);
                            return check && /^[a-zA-Z][a-zA-Z0-9_]{0,}$/.test(val);
                        },
                        value:columnNames[i]
                    }, dojo.create("div", {}, tdName));
                    headerNameBox.startup();
                    columnNameWidgets.push(headerNameBox);
                    //structureModel.columnNameList
                    if (null != structureModel.columnNameList && structureModel.columnNameList.length == columnNames.length) {
                        headerNameBox.set("value", structureModel.columnNameList[i]);
                    }
                    var columnSelect = new dijit.form.Select({
                        //id:'columnHeadSelect_'+i,
                        //name: 'columnHeadSelect_'+i,
                        options:[
                            {label:"chararray", value:"chararray"},
                            {label:"bytearray", value:"bytearray"},
                            {label:"int", value:"int"},
                            {label:"long", value:"long"},
                            {label:"float", value:"float"},
                            {label:"double", value:"double"}
                        ],
                        baseClass:'greyDropdownButton',
                        align:"center"
                    }, dojo.create("div", {}, tdType));
                    //columnSelect.startup();
                    columnTypeWidgets.push(columnSelect);

                    //structureModel.columnTypeList
                    if(guessColumnTypes.guess==true && guessColumnTypes.dataTypes.length==columnNames.length){
                        columnSelect.set("value", guessColumnTypes.dataTypes[i]);
                    }else if (null != structureModel.columnTypeList && structureModel.columnTypeList.length == columnNames.length) {
                        columnSelect.set("value", structureModel.columnTypeList[i]);
                    }

                }
            }
            if (null != contents && columnNames != null && columnTypes != null && columnNames.length != 0 && columnTypes.length != 0) {
                //if contents.lengt != columnNames.length
                //else
                if (isAllEmpty(contents) == true) {
                    var tr = dojo.create("tr", {}, tableBody);
                    var tdDOM = dojo.create("td", {colspan:columnNames.length, style:"word-wrap:break-word; word-break:break-all;white-space:normal"}, tr);
                    dojo.create("span", {innerHTML:alpine.nls.hadoop_prop_file_structure_log_file_preview_error_tip}, tdDOM);
                    return;
                }
                for (var j = 0; j < contents.length; j++) {
                    var tr = dojo.create("tr", {}, tableBody);
                    for (var k = 0; k < columnNames.length; k++) {
                        var tdDOM = dojo.create("td", {style:"word-wrap:break-word; word-break:break-all;white-space:normal"}, tr);
                        var value = contents[j][k];
                        if (value == null) {
                            value = "N/A";
                        }
                        dojo.create("span", {innerHTML:value}, tdDOM);
                    }
                }
            }
        }

        ;

        function _gessDataTypesfromContents(columnNames,contents) {
            var columnTypes = [];
            if (null != columnNames && contents != null && isAllEmpty(contents) == false) {
                var columnLength = columnNames.length;
                if (columnLength == contents[0].length) {
                    var columnTypes4Judge = [];
                    for (var i = 0; i < columnLength; i++) { //column numbers
                        var columnType = [];
                        for (var j = 0; j < contents.length; j++) { //content rows
                            if(j>MAX_GUESS_ROW){
                                continue;
                            }
                            var contentType = _getDataTypeByValue(contents[j][i]);
                            columnType.push(contentType);
                        }
                        columnTypes4Judge.push(columnType);
                    }
                    //
                    columnTypes = [];
                    for (i = 0; i < columnLength; i++) {

                        if (columnTypes4Judge[i] == null || columnTypes4Judge[i].length == 0) {
                            columnTypes.push("chararray");
                            continue;
                        }

                        if (columnTypes4Judge[i].length > 0) {
                            if (dojo.indexOf(columnTypes4Judge[i], "chararray") != -1) {
                                columnTypes.push("chararray");
                                continue;
                            }
                            if (dojo.indexOf(columnTypes4Judge[i], "double") != -1) {
                                columnTypes.push("double");
                                continue;
                            }
                            columnTypes.push("long");
                        }
                    }

                }
            }
            return columnTypes;
        }

        ;

        function _getDataTypeByValue(value) {
            var isNumber = false;
            value = value + "";
            try {
                if (isNaN(value) == false) {
                    isNumber = true;
                }
            } catch (e) {
                isNumber = false;
            }
            var dataType = "chararray";
            if (isNumber == true && value.indexOf(".") == -1) {
                dataType = "long";
            }
            if (isNumber == true && value.indexOf(".") != -1) {
                dataType = "double";
            }
            var reg = /^((\d+.?\d+)[Ee]{1}(\d+))$/ig;
            if (reg.test(value) == true) {
                dataType = "double";
            }
            return dataType;
        }

        ;

        function isAllEmpty(contents) {
            for (var i = 0; i < contents.length; i++) {
                if (contents[i].length > 0) {
                    return false;
                }
            }
            return true;
        }

        function setGridPreviwErrorInfo(msg) {
            dojo.empty(PREVIEW_GRID_CONTAINER);
            dojo.create("div", {style:"heigth:50px;width:100%;text-align:center", innerHTML:msg}, dojo.byId(PREVIEW_GRID_CONTAINER));
        }

        ;

        function _emptyWidgets() {
            if (columnTypeWidgets != null) {
                for (var i = 0; i < columnTypeWidgets.length; i++) {
                    if (columnTypeWidgets[i].destroyRecursive) {
                        columnTypeWidgets[i].destroyRecursive();
                    }
                }
            }
            if (columnNameWidgets != null) {
                for (var i = 0; i < columnNameWidgets.length; i++) {
                    if (columnNameWidgets[i].destroyRecursive) {
                        columnNameWidgets[i].destroyRecursive();
                    }
                }
            }
            columnNameWidgets = [];
            columnTypeWidgets = [];
        }

        ;

        function _emptyModelNamesAndTypes() {
            structureModel.columnNameList = [];
            structureModel.columnTypeList = [];
        }

        ;

        function updateFormatDefaultValue() {
            var value = "";
            if (onDlgLoad == true) {
                value = structureModel.logFormat
            }
            structureModel.logFormat = "";

            var apacheLogServer = [
                {name:"%h %l %u %t \\\"%r\\\" %>s %b", id:"NCSA Common Log Format (CLF)"},
                {name:"%v %h %l %u %t \\\"%r\\\" %>s %b", id:"NCSA Common Log Format with Virtual Host"},
                {name:"%h %l %u %t \\\"%r\\\" %>s %b \\\"%{Referer}i\\\" \\\"%{User-agent}i\\\"", id:"NCSA extended/combined log format"},
                {name:"%v %h %l %u %t \\\"%r\\\" %>s %b \\\"%{Referer}i\\\" \\\"%{User-agent}i\\\"", id:"NCSA extended/combined log format with Virtual Host"}
            ];
            var log4J = [
                {name:"%d{yyyy-MM-dd HH:mm:ss.SSS Z} %5p [%t] %c{1}:%L - %m%n", id:"Log4J"}
            ]

            var formatInput = dijit.byId(FORMAT_INPUT);
            if (null != formatInput) {
                formatInput.destroyRecursive();
            }
            var dataStroe = null;
            if (this.get("value") == "") {
                dataStroe = new dojo.data.ItemFileReadStore({
                    data:{
                        dentifier:"id",
                        label:"id",
                        items:[]
                    }
                });
            } else if (this.get("value") == "Apache Log") {
                dataStroe = new dojo.data.ItemFileReadStore({
                    data:{
                        dentifier:"id",
                        label:"id",
                        items:apacheLogServer
                    }});
            } else if (this.get("value") == "Log4J") {
                dataStroe = new dojo.data.ItemFileReadStore({
                    data:{
                        dentifier:"id",
                        label:"id",
                        items:log4J
                    }});
            }
            formatInput = new dijit.form.ComboBox({
                id:FORMAT_INPUT,
                name:FORMAT_INPUT,
                value:value,
                baseClass:"greyDropdownButton",
                style:"width: 450px;margin-left:12px",
                store:dataStroe,
                searchAttr:"name"
            }, dojo.create("div", null, FORMAT_INPUT_CONTAINER));

            formatInput.startup();
        }

        return {
            showFileStructure4LogDialog:showFileStructure4LogDialog,
            clearStructueModel:clearStructueModel
        }
    })
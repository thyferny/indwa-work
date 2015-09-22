/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 *
 * HadoopFileOperatorPropertyHelper.js
 *
 * Author Will
 * Version 2.7
 * Date 2012-05-15
 */
define([
    "alpine/props/HadoopFileOperatorPropertyManager",
    "alpine/props/HadoopDataTypeUtil",
    "alpine/import/CSVUtil",
    "dojo/dom-attr",
    "dojo/dom-style",
    "alpine/props/HadoopFileStructure4LogHelper",
    "alpine/util/DataTypeUtils",
    "dojo/dom-class"
],function(hadoopMgmt,hdTypeUtil,csvUtil,domAttr,domStyle,logHelper,dataTypeUtils,domClass){

	var is_dlg_opening = false;
	var isInitialize = false;
    var fileStructureCfgDlg="hadoopFileStructureCfgDlg";
    var fileNameLabel = "hadoopPropery_Config_Colum_file_name";
    var fileStructureCfgDlg_OK = "hadoopFileStructure_Dlg_Btn_OK";
    var fileStructureCfgDlg_Cancel = "hadoopFileStructure_Dlg_Btn_Cancel";
    var includeHeader = "hadoopPropery_fileStructure_Include_Header";
    var tableContainerId = "hadoopPropery_Config_Colum_List_Container";
    var fileStructureTableId = "hadoopPropery_Config_Colum_List_Table";
    var otherDeliminterId="hadoopPropery_Config_Colum_OtherSeparator_Value";

    var quoteCharContainer = "hadoopPropery_Config_Colum_escape_quote_container";
    var delimiterContainer = "hadoopPropery_Config_Colum_Choose_Separator_Container";
    var commonFooterContainer="hadoopPropery_Config_Colum_Dlg_Btn_Container";

    var optionEscape = "hadoopPropery_Config_Colum_file_option_escape";
    var optionQuote = "hadoopPropery_Config_Colum_file_option_quote";

    var dataTypes = hdTypeUtil.getAllHadoopTypes(); //hadoop data type
    //var dataTypes = ["chararray","int","long","float","double","bytearray"]; //hadoop data type
    var allHadoopConntion = [];
    var selectWidgets = [];
    var fileStructureModel = null;
    var fileContent = null;
    var currentConnectionName; //passed by invoke showChooseHadoopFileDlg.

    var sourceButtonId = null;

    var dialogIsReady = false;

    var eventHandler = [];
    var submitCallback = null;

    var fileFormatType = "text"; //text,json,xml,apache

    var hadoopColumnType = null;

    var fileStructure_columntype_bk = null;

    function registEvents(){
    }

    dojo.ready(function(){
      dojo.connect(dijit.byId("hadoopPropery_Choose_HadoopFile_Dlg_Btn_Done"),"onClick",done_Choose_HadoopFile);
      dojo.connect(dijit.byId('hadoopPropery_Choose_HadoopFile_Dlg_Btn_Cancel'),"onClick",hideChooseHadoopFileDlg);
      dojo.connect(dijit.byId('hadoopPropery_Choose_HadoopFile_Dlg'),"onHide",destroy_Choose_HadoopFile_Variable);

      dojo.connect(dijit.byId(fileStructureCfgDlg_OK),"onClick",saveFileStructureCfg);
      dojo.connect(dijit.byId(fileStructureCfgDlg_Cancel),"onClick",cancelFileSturctureCfg);
      dojo.connect(dijit.byId(includeHeader),"onClick",changeIncludeHeader);
      dojo.connect(dijit.byId(fileStructureCfgDlg),"onHide",destroy_SelectWidgiet);
      dojo.query("#hadoopPropery_Config_Colum_Choose_Separator_Container input[type=radio]").forEach(function(itm,idx){
            dojo.connect(itm,"onclick",deliminterChange);
      });
      dojo.connect(dojo.byId(otherDeliminterId),"onChange",buildFileStructureContentGrid);


      dojo.connect(dijit.byId(optionEscape),"onChange",buildFileStructureContentGrid);
      dojo.connect(dijit.byId(optionQuote),"onChange",buildFileStructureContentGrid);

      dojo.connect(dijit.byId(optionEscape),"onKeyUp",validateEscape_Quote);
      dojo.connect(dijit.byId(optionQuote),"onKeyUp",validateEscape_Quote);
   });

   function initFileSturctureModelAndContent(prop, /*optional*/connectionKey, /*optional*/path){
       //init temp status currentFileStruceModel
       if(null!=prop && prop.csvFileStructureModel!=null){
           fileStructureModel = dojo.clone(prop.csvFileStructureModel);
           if(null==fileStructureModel.delimiter || fileStructureModel.delimiter==""){
               fileStructureModel.delimiter="Tab";
           }
           sourceButtonId = getSourceButtonId(prop);
       }
           //init hadoop file content
       getHadoopFilecontent(connectionKey, path);
   }

     // init hadoop file structure
    function initColumnConfigData(hadoopFileName,prop, callback){
        //show file structure dlg
        if(fileStructureModel==null){
            getCurrentFileStruceModel();
        }

        sourceButtonId = getSourceButtonId(prop);
        showHadoopFileStructCfgDlg(hadoopFileName);
        submitCallback = callback;
    }

    //get avaiable connections
    function initHadoopConnections(){
        hadoopMgmt.getAvailbleHadoopConnList(function(data){
            if(null!=data && data.length>0){
                allHadoopConntion = data;
            }
        });
    }
    function _getHadoopConnetionKeyByName(connName){
        if(null!=allHadoopConntion && allHadoopConntion.length>0 && null!=connName && ""!=connName){
            for(var i=0;i<allHadoopConntion.length;i++){
                if(allHadoopConntion[i].label!=null && allHadoopConntion[i].label==connName){
                    connName =allHadoopConntion[i].key;
                    break;
                }
            }
        }
        return connName;
    }

    function getHadoopFilecontent(connectionKey, path){
    	if(!(connectionKey && path)){
            var hadoopConName = dijit.byId("connName"+ID_TAG).get('value');
            connectionKey = _getHadoopConnetionKeyByName(hadoopConName);
            path = dijit.byId('hadoopFileName'+ID_TAG).get('value');
    	}
        if(null!=path && path != "" && null!=connectionKey && ""!=connectionKey){
            hadoopMgmt.gethadoopContent(connectionKey,path,getHadoopFileContentSuccessCallBack);
        }
    }

    function getHadoopFileContentSuccessCallBack(dataObj){
        if(null!=dataObj && dataObj.content!=null){
            //fileContent = buildFileContent(dataObj.content,fileStructureModel.delimiter);
            fileContent = dataObj.content;
        }
    }

    function showSomeDomNode(){
        domStyle.set(quoteCharContainer,"display","");
        domStyle.set(delimiterContainer,"display","");
        domStyle.set(commonFooterContainer,"display","");
    }



    function showHadoopFileStructCfgDlg(hadoopFileName){
       var dlg = dijit.byId(fileStructureCfgDlg);
        if(null!=dlg){
        	isInitialize = true;
        	is_dlg_opening = true;
            dlg.titleBar.style.display = 'none';
            dlg.show();

            //showSomeDomNode();

            dijit.byId(optionEscape).set("value",fileStructureModel.escapChar) ;
            dijit.byId(optionQuote).set("value",fileStructureModel.quoteChar) ;

            initFileSturctCfgDlgFileLabel(hadoopFileName,dlg);
            var dijitButton = dijit.byId("hadoopFileStructure"+ID_TAG);
            if(null==dijitButton){
                dijitButton = dijit.byId("pigExecuteFileStructure"+ID_TAG);
            }
            if (dijitButton != null && domClass.contains(dijitButton.domNode, "workflowButtonInvalid") == true) {
                if (null != fileContent && fileContent.split("\n").length > 0) {
                    var guessDelimiter = csvUtil.guessDelimiter(fileContent.split("\n")[0]);
                    if (dojo.indexOf(["Comma", "Tab", "Semicolon", "Space"], guessDelimiter) != -1) {
                        fileStructureModel.delimiter = guessDelimiter;
                    } else if (guessDelimiter == "Pipe") {
                        fileStructureModel.delimiter = "Other";
                        fileStructureModel.other = "|";
                    } else {
                        fileStructureModel.delimiter = "Comma";
                    }
                }
            }

            fileStructure_columntype_bk = dojo.clone(fileStructureModel.columnTypeList);

            initFileStructDelimiterSelectedStatus(fileStructureModel.delimiter);
            initFileSturctureIncludeHeaderCheckStatus();
            is_dlg_opening = false;
            buildFileStructureContentGrid();
        	isInitialize = false;

            dialogIsReady = true;

        }
    }

    function initFileSturctCfgDlgFileLabel(hadoopFileName,dlg){
        var configDataTitle = dojo.byId(fileNameLabel);
        if(null!=hadoopFileName && ""!=hadoopFileName){
            configDataTitle.title = hadoopFileName;
            var subtitle = hadoopFileName;
            if(subtitle.length>30){
                subtitle = subtitle.substring(0,27)+"...";
            }
            configDataTitle.innerHTML = subtitle;

        }
        dojo.style(dlg.containerNode,{height:"100%",overflow:"hidden"});
    }

   function initFileStructDelimiterSelectedStatus(deliminter){
        var radioButtons = dojo.query("#hadoopPropery_Config_Colum_Choose_Separator_Container input[type=radio]");
        if(radioButtons!=null){
            for(var i=0;i<radioButtons.length;i++){
                var radio = radioButtons[i];
                if(dojo.hasAttr(radio,"checked")==true){
                    dojo.removeAttr(radio,"checked");
                }
                if(deliminter==radio.value){
                    dojo.setAttr(radio, "checked", true);
                    deliminterChange.call(radio);
                }
            }
        }
    }

    function getFileStructDelimiter(){
        var delimiterValue = "";
        var radioButtons = dojo.query("#hadoopPropery_Config_Colum_Choose_Separator_Container input[type=radio]");
        if(radioButtons!=null){
            for(var i=0;i<radioButtons.length;i++){
                var radio = radioButtons[i];
                if(dojo.getAttr(radio, "checked")==true){
                    delimiterValue = radio.value;
                }
            }
        }
      return delimiterValue;
    }

    function initFileSturctureIncludeHeaderCheckStatus(){
         var checker = dijit.byId(includeHeader);
        if(checker!=null){
            if(null!=fileStructureModel.isFirstLineHeader && fileStructureModel.isFirstLineHeader.toLowerCase()=="true"){
                checker.set("checked",true);
            }else{
                checker.set("checked",false);
            }
        }
    }

   function changeIncludeHeader(){
//        if(this.get("checked")==true){
//            fileStructureModel.isFirstLineHeader="true";
//        }else{
//            fileStructureModel.isFirstLineHeader="false";
//        }
       //save header type
    //   if(null==fileStructureModel.columnTypeList){
           fileStructureModel.columnTypeList=[];
     //  }
//       if(null!=selectWidgets){
//           for(var i=0;i<selectWidgets.length;i++){
//               fileStructureModel.columnTypeList[i] = selectWidgets[i].get("value");
//               //selectWidgets[i].destroyRecursive();
//           }
//       }
       var len = selectWidgets.length;
       for(i=0;i<len;i++){
           selectWidgets[i].destroyRecursive();
       }
       selectWidgets = [];
       buildFileStructureContentGrid();
    }

    function buildFileContent(content,deliminter,escapChar,quoteChar){

        var realDeliminter = getRealDeliminter(deliminter);

        var csvParams = {
                separator: realDeliminter,
                quote: quoteChar,
                escaped: escapChar
            };
          
        var returnArray = [];
        if(content!=null){
            var rowDatas = content.split("\n");
            
            if(null!=rowDatas){
            	
            	returnArray = 	csvUtil.csvArrayToArrayOfArrays(rowDatas, csvParams) ;
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

    function getRealDeliminter(deliminter){
        switch (deliminter){
            case "Tab":
                return "\t";
                break;
            case "Comma":
                return ",";
                break;
            case "Semicolon":
                return ";";
                break;
            case "Space":
                return " ";
                break;
            case "Other":
               // if(null!=fileStructureModel.other){
                    if(dojo.byId(otherDeliminterId).value==null){
                        return "";
                    }else{
                       if(dojo.byId(otherDeliminterId).value.length>1){
                           dojo.byId(otherDeliminterId).value = dojo.byId(otherDeliminterId).value.substr(0,1);
                       }
                        return dojo.byId(otherDeliminterId).value;
                    }
               // }
                break;
            default:
               return "\t"
        }
    }

    function deliminterChange(){
       // fileStructureModel.delimiter = this.value;
        if(this.value=="Other"){
           dojo.byId(otherDeliminterId).disabled="";
            if(fileStructureModel.other==null){
                if(dojo.byId(otherDeliminterId).value==""){
                    dojo.byId(otherDeliminterId).value="";
                }
            }else{
                dojo.byId(otherDeliminterId).value=fileStructureModel.other;
            }
        }else{
            dojo.byId(otherDeliminterId).disabled="disabled";
        }
        buildFileStructureContentGrid();
    }

    function buildFileStructureContentGrid(){
    	if(is_dlg_opening==true){
    		return ;
    	}
        var fileStrutDelimiter = getFileStructDelimiter();
        var escChar=  dijit.byId(optionEscape).get("value")	;
        var quoteChar = dijit.byId(optionQuote).get("value")	;
        var rowDatas = buildFileContent(fileContent,fileStrutDelimiter, escChar,quoteChar);
        if(null!=rowDatas){

            var rowsColumnNum = _getRowsColumnNumber(rowDatas);
            if(_validateCanSplit(rowsColumnNum) == false){
                if(dojo.byId("hadoopPropery_Config_Colum_OtherSeparator_Radio").checked==true && dojo.byId(otherDeliminterId).value==""){
                    return false;
                }
                popupComponent.alert(alpine.nls.hadoop_prop_file_structure_split_error_tip);
                var container = dojo.byId(tableContainerId);
                dojo.empty(container);
                container.style.textAlign = "center";
                //container.innerHTML = "Split error.Try other delimiter.";
                dojo.create("div",{style:"padding-top:150px;",innerHTML:alpine.nls.hadoop_prop_file_structure_split_error_inner_tip},container);
                return false;
            }

            if(fileStructureModel.columnTypeList==null
                || fileStructureModel.columnTypeList.length==0
                || fileStructureModel.columnTypeList.length!=rowDatas[0].length
                || (dialogIsReady==true && _hasSameColumnTypes(rowDatas)==false)
                ){
                if(null!=dijit.byId("hadoopPropery_fileStructure_Include_Header").get("checked")
                    && dijit.byId("hadoopPropery_fileStructure_Include_Header").get("checked") ==true){
                    fileStructureModel.columnTypeList = assumeColumnType(rowDatas,true);
                }else{
                    fileStructureModel.columnTypeList = assumeColumnType(rowDatas,false);
                }
            }

            destroy_SelectWidgiet();
            var maxColumnNumber = _getMaxColumnNumber(rowDatas);
            var tableDom = buildTable();
            if(tableDom!=null){
                buildTableHeader(tableDom,rowDatas,maxColumnNumber);
                buildTableBody(tableDom,rowDatas,maxColumnNumber);
            }
        }
    }

    //assume Column type
    function _hasSameColumnTypes(rowDatas) {
        if(null!=fileStructureModel.columnTypeList && null!=rowDatas){
                         var newTypes = null;
            if(fileStructureModel.isFirstLineHeader.toUpperCase() == "TRUE"){
                newTypes = assumeColumnType(rowDatas,true);
            }else{
                newTypes = assumeColumnType(rowDatas,false);
            }
            if(newTypes==null){
                return false;
            }else{
                for(var i=0;i<newTypes.length;i++){
                    if(newTypes[i]!=fileStructureModel.columnTypeList[i]){
                        return false;
                    }
                }
              return true;
            }
        }else{
            return false;
        }

    }

   function assumeColumnType(rowData,includeHeader){
      var maxColumnNumber = _getMaxColumnNumber(rowData);
      var columnTypes = [];
      if(null!=rowData && rowData.length>0 && maxColumnNumber!=null){
         for(var j=0;j<maxColumnNumber;j++){
             columnTypes[j] = dataTypeUtils.getTypeOfArray(rowData,j,includeHeader,"hd");

             /*var i=0;
             if(includeHeader==true){
                 i=1
             }
             for(i;i<rowData.length;i++){
                 var type = "";
                 var reg = /^[-]?\d+(\.\d+)?$/;
                 var reg1 = /^((\d+.?\d+)[Ee]{1}(\d+))$/ig;

                 if(rowData[i][j]==null || rowData[i][j]==""){
                     continue;
                 }
                 if(reg.test(rowData[i][j])==true || reg1.test(rowData[i][j])==true){
                     type = "number";
                 }else{
                     type = "chararray";
                 }

                 if(type == "chararray"){
                     columnTypes[j] = "chararray";
                     break;
                 }else{
                     if(rowData[i][j].indexOf(".")!=-1 || rowData[i][j].indexOf("e")!=-1 || rowData[i][j].indexOf("E")!=-1){
                         columnTypes[j]="double";
                     }else if(rowData[i][j].indexOf(".")==-1 && rowData[i][j].indexOf("e")==-1 && rowData[i][j].indexOf("E")==-1){
                        if(columnTypes[j]!="double"){
                            columnTypes[j]="long";
                        }
                     }
                 }
             }
             if (columnTypes[j] == "") {
                 columnTypes[j] = "chararray";
             }*/
         }
      }
      return columnTypes;
   }

   function _getMaxColumnNumber(rowDatas){
          var columnNum = 1;
          var columnLengths = [];
          if(null!=rowDatas){
              for(var i=0;i<rowDatas.length;i++){
                  if(null!=rowDatas[i] && columnNum<rowDatas[i].length){
                      columnNum = rowDatas[i].length;
                  }
              }
          }
         return columnNum;
    }

   function _getRowsColumnNumber(rowDatas){
          var rowsColumnNum = [];
          if(null!=rowDatas){
              for(var i=0;i<rowDatas.length;i++){
                  if(null!=rowDatas[i]){
                      rowsColumnNum[i] = rowDatas[i].length;
                  }
              }
          }
         return rowsColumnNum;
    }

    function _validateCanSplit(rowsColumnNum){
        if(null!=rowsColumnNum && rowsColumnNum.length>0){
            var status = true;
            for(var i=0;i<rowsColumnNum.length-1;i++){
                status = status&(rowsColumnNum[i]==rowsColumnNum[i+1] || rowsColumnNum[i]==1 || rowsColumnNum[i+1]==1 || Math.abs(rowsColumnNum[i]-rowsColumnNum[i+1])==1);
            }
            return status;
        }else{
            return false;
        }
    }

    function  validateEscape_Quote(event){
       var inputValue = this.get("value");
        if(null!=inputValue && ""!=inputValue){
             var reg = /^[\W]$/;
            if(reg.test(inputValue)==false){
                this.set("value","");
            }
        }
        dojo.stopEvent(event);
    }

    function buildTable(){
        var container = dojo.byId(tableContainerId);
        dojo.empty(container);
        return  dojo.create("table",{className:"alpineImportDataTable",id:fileStructureTableId},container);
    }

   function buildTableHeader(tableDom,rowDatas,maxColumnNumber){
       var tableHead = dojo.create("thead",null,tableDom);
       var trColumnNames = dojo.create("tr",{className: "alpineImportDataColName"},tableHead);
       var trColumnTypes = dojo.create("tr",{className: "alpineImportDataColType"},tableHead);
       if(null!=maxColumnNumber && maxColumnNumber>0){
           var selectOption = _buildSelectOption(dataTypes);
           var columnNameList = fileStructureModel.columnNameList;
           var columnTypeList = fileStructureModel.columnTypeList;
           for(var i=0;i<maxColumnNumber;i++){
               var nameTd = dojo.create("td",{align:"center",valign:"middle",className:"alpineImportDataColName"},trColumnNames);
               var columnValue;
               if(dijit.byId(includeHeader).get("checked")==true){
            	   
            	  if(rowDatas[0]!=null && rowDatas[0][i]==null){
                      rowDatas[0][i] = "";
                  }
                   //var columnValue = rowDatas[0][i];
                  if(rowDatas[0]==null){continue;}
                   columnValue = rowDatas[0][i].replace(/[\s|\W]/g,"_");
                   if(null!=columnNameList && columnNameList[i]!=null && ""!=columnNameList[i] && columnNameList.length==maxColumnNumber && isInitialize){
                       columnValue = columnNameList[i];
                   }
//                   dojo.create("input",{type:"text",className:"tableHeaderInput",id:"columnHeadTitle_"+i,value:columnValue},nameTd);
               }else{
                   columnValue = "Column"+(i+1);
                   if(null!=columnNameList && columnNameList[i]!=null && ""!=columnNameList[i] && columnNameList.length==maxColumnNumber){
                       columnValue = columnNameList[i];
                   }
//                   dojo.create("input",{type:"text",className:"tableHeaderInput",id:"columnHeadTitle_"+i,value:columnValue},nameTd);
               }
               if(dijit.byId("columnHeadTitle_" + i)){
            	   dijit.byId("columnHeadTitle_" + i).destroyRecursive();
               }
               var headerTextBox = new dijit.form.ValidationTextBox({
                   baseClass: "alpineImportTextbox",
                   id: "columnHeadTitle_" + i,
            	   required: true,
            	   isValid: function(){
        			   this.invalidMessage = this.messages.invalidMessage;
            		   var val = this.get('value');
            		   var check = this.validator(val);
            		   var isKewWord = hdTypeUtil.isHadoopKeyWord(val);
            		   if(isKewWord){
            			   this.invalidMessage = alpine.nls.hadoop_prop_file_structure_column_name_keyword_tip.replace("###", "'" + val + "'");
            		   }
            		   
            		   return check && /^[a-zA-Z][a-zA-Z0-9_]{0,}$/.test(val) && !hdTypeUtil.isHadoopKeyWord(val);
            	   },
            	   value: columnValue
               }, dojo.create("div", {}, nameTd));
               headerTextBox.startup();

               var theadSelectTd = dojo.create("td",{className:"alpineImportDataColType",style:"text-align:center"},trColumnTypes);
               if(null!=columnTypeList){
                   for(var j=0;j<selectOption.length;j++){
                       if(selectOption[j].value==columnTypeList[i]){
                           selectOption[j].selected=true;
                       }else{
                           selectOption[j].selected=false;
                       }
                   }
               }
               var columnSelect = new dijit.form.Select({
                   id:'columnHeadSelect_'+i,
                   name: 'columnHeadSelect_'+i,
                   options: dojo.clone(selectOption),
                   baseClass:'greyDropdownButton',
                   align:"center"
               },dojo.create("div",{},theadSelectTd));
               selectWidgets.push(columnSelect);
               if(null!=columnTypeList && null!=columnTypeList[i]){
                   //columnSelect.setValue(selectedStatus[i]);
                   columnSelect.set('value',columnTypeList[i]);
               }
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

   function buildTableBody(tableDom,rowDatas,maxColumnNumber){
       if(null!=maxColumnNumber && maxColumnNumber>0 && rowDatas!=null){
           var tableBody = dojo.create("tbody",{className:"alpineImportDataTableBody"},tableDom);
           var i=0;
           if(dijit.byId(includeHeader).get("checked")==true){
               i=1;
           }
           for(;i<rowDatas.length;i++){
               var tbodyTR = dojo.create("tr",{},tableBody)
               if(null!=rowDatas[i] && ""!=rowDatas[i]){
                   _buildTbodyTDS(tbodyTR,rowDatas[i],maxColumnNumber);
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

    function saveFileStructureCfg(){
        var columnType=[];
        var columnHeader=[];
        dojo.forEach(dojo.query("#hadoopPropery_Config_Colum_List_Table .alpineImportTextbox"),function(itm,idx){
            columnHeader.push(dojo.trim(dijit.byNode(itm).get("value")));
        });
        dojo.forEach(dojo.query("#hadoopPropery_Config_Colum_List_Table .greyDropdownButton"),function(itm,idx){
            var selectWidget = dijit.byId(itm.id);
            if(selectWidget!=null){
                columnType.push(dojo.trim(selectWidget.get('value')));
            }
        });

        var validateColumnName = _validateColumnName(columnHeader);
        if(validateColumnName==false){
            return false;
        }

        //validate escapChar quoteChar
        var escapChar  =dijit.byId(optionEscape).get("value");
        var quoteChar = dijit.byId(optionQuote).get("value");
        if(!(escapChar==null && quoteChar==null)
            && !(escapChar=="" && quoteChar=="")
            && !(escapChar.length==1 && quoteChar.length==1)){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_escap_quote_char_tip);
            return false;
        }

        if((escapChar.length==1 && quoteChar.length==1) && escapChar==quoteChar){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_escap_quote_equal_tip);
            return false;
        }

        if(dojo.byId(otherDeliminterId).disabled==false && (null==dojo.byId(otherDeliminterId).value || dojo.byId(otherDeliminterId).value=="")){
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_tip_Other);
            return false;
        }else if(dojo.byId(otherDeliminterId).disabled==false && null!=dojo.byId(otherDeliminterId).value && dojo.byId(otherDeliminterId).value!=""){
            fileStructureModel.delimiter="Other";
            fileStructureModel.other = dojo.byId(otherDeliminterId).value;
        }else{
            var radioButtons = dojo.query("#hadoopPropery_Config_Colum_Choose_Separator_Container input[type=radio]");
            if(radioButtons!=null){
                for(var i=0;i<radioButtons.length;i++){
                    if(radioButtons[i].checked==true){
                        fileStructureModel.delimiter=radioButtons[i].value;
                        break;
                    }
                }
            }
            var otherValue = dojo.byId(otherDeliminterId).value;
            if(null==otherValue){
                fileStructureModel.other ="";
            }else{
                fileStructureModel.other = otherValue;
            }
        }

        if(dijit.byId(includeHeader).get("checked")==true){
            fileStructureModel.isFirstLineHeader="true";
        }else{
            fileStructureModel.isFirstLineHeader="false";
        }

        fileStructureModel.columnTypeList = columnType;
        fileStructureModel.columnNameList = columnHeader;
        
   
        fileStructureModel.escapChar = escapChar;
        fileStructureModel.quoteChar = quoteChar;
        setButtonBaseClassValid(sourceButtonId);
        if(submitCallback){
        	submitCallback.call(null, fileStructureModel);
        }
        var pList = CurrentOperatorDTO.propertyList;
        var currentProp = null;
        for(var i=0;i<pList.length;i++){
            if(pList[i].name=="hadoopFileStructure"){
                currentProp = pList[i];
                break;
            }
        }
        if(currentProp!=null){
            logHelper.clearStructueModel(currentProp,"csv");
        }
        hideFileStructureCfgDlg();
    }

    function cancelFileSturctureCfg(){
        dojo.byId(otherDeliminterId).value="";
        fileStructureModel.columnTypeList = fileStructure_columntype_bk;
        hideFileStructureCfgDlg();
    }

    function hideFileStructureCfgDlg(){
        destroy_SelectWidgiet();
        if(eventHandler.length>0){
            for(var i=0;i<eventHandler.length;i++){
                dojo.disconnect(eventHandler[i]);
            }
        }
        dialogIsReady = false;
        var dlg = dijit.byId(fileStructureCfgDlg);
        if(null!=dlg){
            dlg.hide();
            eventHandler = [];
        }
    }

   function clearSturctModelAndContent(){
       fileStructureModel = null;
       fileContent = null;
   }

    function getCurrentFileStruceModel(){
        if(fileStructureModel==null){
            fileStructureModel={}
        }
        if(fileStructureModel.isFirstLineHeader==null){
            fileStructureModel.isFirstLineHeader="";
        }
        if(null==fileStructureModel.delimiter || fileStructureModel.delimiter==""){
            fileStructureModel.delimiter="Tab";
        }
        if(null==fileStructureModel.columnNameList){
            fileStructureModel.columnNameList=[];
        }
        if(null==fileStructureModel.columnTypeList){
            fileStructureModel.columnTypeList = [];
        }
        return fileStructureModel;
    }

    function destroy_SelectWidgiet(){
        if(null!=selectWidgets && selectWidgets.length>0){
            for(var i=0;i<selectWidgets.length;i++){
                selectWidgets[i].destroyRecursive();
            }
            selectWidgets = [];
        }
    }


    function hadoopPropery_Config_Colum_ImportData(status){

        if(status==true){
            hadoopMgmt.gethadoopContent(connectionKey,path,hadoopPropery_Config_Colum_ImportData_Success_Callback);
        }else{
            if(columnData!=null){
                destroy_SelectWidgiet();
                buildConfig_Column_List_Table(columnData);
            }
        }
    }

    function hadoopPropery_Config_Colum_ImportData_Success_Callback(data){
         if(null!=data){
             columnData = data;
             destroy_SelectWidgiet();
             //new selected file should clear
             if(null!=currentFileStruceModel && columnData==null){
                 currentFileStruceModel.columnNameList=[];
             }

             buildConfig_Column_List_Table(data);
         }
    }

    function _validateColumnName(columnHeader){
        if(null!=columnHeader && columnHeader.length>0){
            //var s = columnHeader.join(",")+",";
            var hash = {};
            for(var i=0;i<columnHeader.length;i++){
                //fix /^[a-zA-Z0-9_]{1,}$/ can not number at the first
                dijit.byId("columnHeadTitle_"+i)._hasBeenBlurred = true;
                if(!dijit.byId("columnHeadTitle_"+i).validate()){
//                    popupComponent.alert(alpine.nls.hadoop_prop_file_structure_tip_columnHeader_is_invalid.replace("##",(i+1)));
                    
                    var titleHeader = dojo.byId("columnHeadTitle_"+i);
                    if(titleHeader!=null){
                        titleHeader.focus();
                    }
                    return false;
                }

                if(hash[columnHeader[i]]==true){
                    popupComponent.alert(alpine.nls.hadoop_prop_file_structure_tip_columnHeader_repleat.replace("##","\"" + columnHeader[i] + "\""));
                    return false;
                }
                hash[columnHeader[i]]=true;

            }
        }

    }


    //For property choose hadoop file
//    function showChooseHadoopFileDlg(callBackFunction, hadoopConName){
//        var dlg = dijit.byId('hadoopPropery_Choose_HadoopFile_Dlg');
//        if(null!=dlg){
//            dlg.titleBar.style.display = 'none';
//            dlg.show();
//            dojo.style(dlg.containerNode,{height:"100%",overflow:"hidden"});
//            connectionKey = _getConnectionKey(hadoopConName);
//            if(connectionKey!=null && connectionKey!=""){
//               // progressBar.showLoadingBar();
//                hadoopMgmt.getHadoopFilesByPath(connectionKey,"",function(data){
//                    var treeContainer = dojo.create("div",{},dojo.byId('hadoopPropery_Choose_HadoopFile_Tree_Container'));
//                    buildHadoopFileTree(data,treeContainer,callBackFunction);
//                }, "hadoopPropery_Choose_HadoopFile_Dlg" );
//                currentConnectionName = connectionKey;
//            }
//        }
//    }

    function _getConnectionKey(hadoopConName){
        var connectionKey = _getHadoopConnetionKeyByName(hadoopConName);
        return connectionKey;
    }

//    function buildHadoopFileTree(fileItems,treeContainer,callBackFunction){
//        //progressBar.closeLoadingBar();
//        var resourceStore = new dojo.data.ItemFileWriteStore({
//            data: {
//                identifier: "key",// refer to ResourceItem
//                label: "name",
//                items: fileItems
//            }
//        });
//
//        var hadoopFileTree = new dijit.Tree({
//            showRoot: false,
//            style : "height: 100%;",
//            id: "hadoop_File_Tree4_property_Hadoop_File_Name",
//            openOnDblClick: true,
//            persist: false,
//            model: new dijit.tree.ForestStoreModel({
//                store: resourceStore,
//                query: {
//                    "key": "*"
//                },
//                mayHaveChildren: function(item){
//                   if(getVal(item.isDir)==true){
//                       return true;
//                   }else{
//                       return false;
//                   }
//                },
//                getIdentity: function(item){
//                    return getVal(item.key);
//                }
//            }),
//            getIconClass: function(item, opened){
//               if(getVal(item.isDir)==true){
//                    return opened ? "dijitIconFolderOpen" : "dijitIconFolderClosed";
//                }else{
//                    return "dijitIconFile";
//                }
//            },
//            getLabel: function(item){
//                return item.name;
//            }
//        },treeContainer);
//
//        hadoopFileTree.model.getChildren =  function(parent, callback, onErr){
//            if(!parent.children){
//                hadoopMgmt.getHadoopFilesByPath(currentConnectionName,toolkit.getValue(parent.key),function(children){
//                    //_addTreeItems(hadoopFileTree.model,children,parent);
//                    parent.children = children;
//                    var parentNode = getVal(hadoopFileTree.getNodesByItem(getVal(parent.key)));
//                    parentNode.setChildItems(children);
//                    callback(parent['children']);
//                });
//               // resourceFetcher.loadTreeItems(parent, function(children){
//
//                    //resourceTree.addTreeItems(tree.model, children, parent);
//                    //var parentNode = getVal(tree.getNodesByItem(getVal(parent.key)));
//                    //parentNode.setChildItems(children);
//                   //callback(parent['children']);
//                //});
//            }else{
//                callback(parent['children']);
//            }
//        };
//        hadoopFileTree.startup();
//        dojo.connect(hadoopFileTree,"onClick",function(){
//            // hiddenValue = dojo.byId('hadoopPropery_Choose_HadoopFile_hidden').value;
//            if(arguments[0].isDir ==false){
//                dojo.byId('hadoopPropery_Choose_HadoopFile_hidden').value =arguments[0].key;
//            }else{
//                dojo.byId('hadoopPropery_Choose_HadoopFile_hidden').value = "";
//            }
//            //dojo.byId('hadoopPropery_Choose_HadoopFile_hidden').value;
//        });
//
//
//        dojo.connect(hadoopFileTree,"onDblClick",function(hadoopConn){
//            if(toolkit.getValue(arguments[0].isDir) ==false){
//                hideChooseHadoopFileDlg();
//                callBackFunction.call(null, hadoopConn);
//            }
//        });
//
//    }

    function _dbClick4SelectHadoopFile(){
//        dijit.byId('hadoopFileName'+ID_TAG).set("value",hadoopConnection.key);
//        dijit.byId('hadoopFileName'+ID_TAG).set("title",hadoopConnection.key);
        //
        //new selected file should clear
        resetFileStructModel(fileStructureModel);
        getHadoopFilecontent();
        _setHadoopFileFormat();
     }

    function getVal(val){
        return dojo.isArray(val) ? val[0] : val;
    }
    function hideChooseHadoopFileDlg(){
        var dlg = dijit.byId('hadoopPropery_Choose_HadoopFile_Dlg');
        if(null!=dlg){
            dlg.hide();
            //dojo.style(dlg.containerNode,{height:"100%",overflow:"hidden"});
        }
    }

    function destroy_Choose_HadoopFile_Variable(){
      var hadoopFileTree = dijit.byId('hadoop_File_Tree4_property_Hadoop_File_Name');
        if(null!=hadoopFileTree){
            hadoopFileTree.destroyRecursive();
        }
        currentConnectionName = null;
    }

    function done_Choose_HadoopFile(){
        if(""==dojo.byId('hadoopPropery_Choose_HadoopFile_hidden').value){
            popupComponent.alert(alpine.nls.hadoop_prop_choose_file_tip);
            return false;
        }
        dijit.byId('hadoopFileName'+ID_TAG).set("value",dojo.byId('hadoopPropery_Choose_HadoopFile_hidden').value);
        dijit.byId('hadoopFileName'+ID_TAG).set("title", dojo.byId('hadoopPropery_Choose_HadoopFile_hidden').value);
        //new selected file should clear
        resetFileStructModel(fileStructureModel);
        getHadoopFilecontent();
        hideChooseHadoopFileDlg();
        _setHadoopFileFormat();
    }

    function _setHadoopFileFormat(){
        var fileName = dijit.byId('hadoopFileName'+ID_TAG).get("value");
        var fsel = dijit.byId("hadoopFileFormat"+ID_TAG);
        if(fileName!=null){
            if(fileName.lastIndexOf(".")==-1){
                fsel.set("value","Text File");
                return
            }
            var suffix = dojo.trim(fileName.substring(fileName.lastIndexOf(".")+1));
            switch (suffix.toLowerCase()){
                case "json":
                    fsel.set("value","JSON");
                    clearPrevJsonMode();
                    break;
                case "xml":
                    fsel.set("value","XML");
                    break;
                case "csv":
                    fsel.set("value","Text File");
                    break;
                case "log":
                    fsel.set("value","Log File");
                    break;
            }
        }
    }
    function clearPrevJsonMode(){
        if(CurrentOperatorDTO!=null){
            for(var i=0;i<CurrentOperatorDTO.propertyList.length;i++){
                 var prop = CurrentOperatorDTO.propertyList[i];
                if(prop.name == "hadoopFileStructure"){
                      if(prop.jsonFileStructureModel !=null){
                          prop.jsonFileStructureModel = null;
//                          prop.jsonFileStructureModel.container = "";
//                          prop.jsonFileStructureModel.columnNameList=[];
//                          prop.jsonFileStructureModel.columnTypeList=[];
//                          prop.jsonFileStructureModel.jsonPathList = [];
                      }
                }
            }
        }
    }
    function resetFileStructModel(model){
        if(null!=model){
            model.delimiter="Tab";
            model.isFirstLineHeader="";
            model.other="";
            model.columnNameList=[];
            model.columnTypeList=[];
        }

        dijit.byId("hadoopFileStructure"+ID_TAG).set('baseClass', "workflowButtonInvalid");
        dijit.byId("hadoopFileStructure"+ID_TAG).focus();

        fileContent = null;

    }


    function getFileStructureColumnNameWithType(connKey,uid,callBackFunction){
        hadoopMgmt.getFileStructureColumnNameWithType(connKey,uid,callBackFunction);
    }

    function setFileFormatType(value){
        if(null!=value){
            switch (value){
                case "Text File":
                    fileFormatType = "text";
                break;
                case "Log File":
                    fileFormatType = "apache";
                break;
                case "JSON":
                    fileFormatType = "json";
                break;
                case "XML":
                    fileFormatType = "xml";
                break;
            }
        }else{
            fileFormatType = "text";
        }
    }

    function getFileFormatType(){
        return fileFormatType;
    }

    function getFileContent(){
        return fileContent;
    }

    function setFileContent(content){
        fileContent = content;
    }


    return {
        initColumnConfigData:initColumnConfigData,
//        showChooseHadoopFileDlg:showChooseHadoopFileDlg,
        initHadoopConnections:initHadoopConnections,
        dbClick4SelectHadoopFile:_dbClick4SelectHadoopFile,
        getFileStructureColumnNameWithType:getFileStructureColumnNameWithType,
        initFileSturctureModelAndContent:initFileSturctureModelAndContent,
        getCurrentFileStruceModel:getCurrentFileStruceModel,
        clearSturctModelAndContent:clearSturctModelAndContent,
        getConnectionKey:_getConnectionKey,
        buildFileContent:buildFileContent,
        resetFileStructModel:resetFileStructModel,
        getHadoopFilecontent:getHadoopFilecontent,
        setFileFormatType:setFileFormatType,
        getFileFormatType:getFileFormatType,
        getFileContent:getFileContent,
        setFileContent:setFileContent
    }
});
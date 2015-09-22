/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopOperatorsDataExplorerHelper
 * Author: Will
 * Date: 12-7-16
 */
define(["alpine/flow/HadoopOperatorsDataExplorerManager"],function(manager){

   // var storeResults = true;
    var isHadoopFileOperator = true;
    var CANVAS_ID = "FlowDisplayPanelPersonal";

    function show_hadoop_data_explorer(operator){
        var args = {};
        args.hadoopInfo = alpine.flow.HadoopResourceFetcher.getHadoopFileInfo(operator);
        args.connectionKey  = args.hadoopInfo.connectionName;
        args.path = args.hadoopInfo.outputFilePath;
        args.uid = operator.uid;
        args.isHDFileOperator = (operator.classname=="HadoopFileOperator"?true:false)

        manager.getCurrentOperatorProperties(operator,getCurrentOperatorPropertiesCallBack,CANVAS_ID, args);

    }

    function getCurrentOperatorPropertiesCallBack(data, args){
        var storeResults = true;
        if(data.classname == "HadoopFileOperator"){
            isHadoopFileOperator = true;
        }else{
            isHadoopFileOperator = false;
        }
        if(null!=data && null!=data.propertyList){
            for(var i=0;i<data.propertyList.length;i++){
                var prop = data.propertyList[i];
                if(prop.name=="storeResults" && prop.value=="true"){
                    storeResults = true;
                    break;
                }else if(prop.name=="storeResults" && prop.value=="false"){
                    storeResults = false;
                    break;
                }
            }
        }else{
            storeResults = false;
        }

        if(storeResults == true){
            var hadoopConnectKey = args.connectionKey;
            var hadoopFilePath = args.path;
            var operatorUID = args.uid;
            var eidtFlowInfo = alpine.flow.WorkFlowManager.getEditingFlow();
            var eidtFlowInfoKey = eidtFlowInfo.key;
            var isHDFileOperator = args.isHDFileOperator;
            //manager.show_hadoop_data_explorer(args.connectionKey,args.path,args.uid,show_hadoop_data_explorer_callback);
            showHadoopDataExplorer(hadoopConnectKey,hadoopFilePath,operatorUID,eidtFlowInfoKey,isHDFileOperator);
        }else{
            popupComponent.alert("Could not find properties for this file");
        }
    };

    function showHadoopDataExplorer(hadoopConnectKey,hadoopFilePath,operatorUID,eidtFlowInfoKey,isHDFileOperator){
        var window_id="hadoopFile_explorer"+new Date().getTime();
        var postForm = dojo.byId('dataExplorerForm');
        dojo.byId("dataExplorerForm_title_key").value = "hadoop_prop_right_menu_file_explorer";
        dojo.byId("dataExplorerForm_load_method").value = "gethadoopContent4DataExplorer";

        dojo.byId("dataExplorerForm_load_hadoopConnectKey").value = hadoopConnectKey;
        dojo.byId("dataExplorerForm_load_hadoopFilePath").value = hadoopFilePath;
        dojo.byId("dataExplorerForm_load_operatorUID").value = operatorUID;
        dojo.byId("dataExplorerForm_load_flowInfoKey").value = eidtFlowInfoKey;
        dojo.byId("dataExplorerForm_load_isHDFileOperator").value = isHDFileOperator;

        //var window_id="hadoopFile_explorer"+new Date().getTime();w
        postForm.target = "_blank";
        //window.open("about:blank", window_id, get_open_window_options());
        postForm.submit();
    }

    function show_hadoop_data_explorer_callback(data){
        if(null!=data && data.error!=null){
            popupComponent.alert(data.error);
            return false;
        }
        if(null!=data && null!=data.delimiter && null!=data.isFirstLineHeader && null!=data.content){
            //var url=baseURL + "/alpine/result/dataexplorer.jsp?load_method=gethadoopContent4DataExplorer&tableName=&schemaName=&dbConnectionName=&title_key=&"+alpine.nls.hadoop_prop_right_menu_file_explorer+"&dataContent="+data.content+"&dataDelimiter="+data.delimiter+"&dataIncludeHeader="+data.includeHeader+"&dataOther="+data.other;

            var dataContent = getDataContenttoJsonString(data.content);
            dojo.byId('dataExplorerForm4HadoopFileExplorer_dataContent').value=encodeURI(encodeURI(dataContent));
            dojo.byId('dataExplorerForm4HadoopFileExplorer_dataDelimiter').value=data.delimiter;
            dojo.byId('dataExplorerForm4HadoopFileExplorer_escapChar').value=data.escapChar;
            dojo.byId('dataExplorerForm4HadoopFileExplorer_quoteChar').value=data.quoteChar;
            if(isHadoopFileOperator == true){
                //"HadoopFileOperator"){
                dojo.byId('dataExplorerForm4HadoopFileExplorer_dataIncludeHeader').value=data.isFirstLineHeader;
            }else{
                dojo.byId('dataExplorerForm4HadoopFileExplorer_dataIncludeHeader').value=false;
            }
            dojo.byId('dataExplorerForm4HadoopFileExplorer_dataOther').value=data.other;

            if(null==data.columnNameList || data.columnNameList.length==0){
                dojo.byId('dataExplorerForm4HadoopFileExplorer_dataHeaderName').value="";
            }else{
                dojo.byId('dataExplorerForm4HadoopFileExplorer_dataHeaderName').value=data.columnNameList.join(",");
            }
            var postForm = dojo.byId('dataExplorerForm4HadoopFileExplorer');
            if(null!=postForm){
                var window_id="hadoopFile_explorer"+new Date().getTime();
                postForm.target = window_id;

                window.open("about:blank", window_id, get_open_window_options());
                postForm.submit();
            }
            //var resultWindow=window.open(encodeURI(url),	"_blank",   get_open_window_options());
            //resultWindow.focus();
        }else{
            popupComponent.alert(alpine.nls.hadoop_prop_file_structure_tip_file_load_error);
        }
    };

    function getDataContenttoJsonString(dataContent){
        var jsonStr = "[";
        if(null!=dataContent){
            var k=0;
             for(var prop in dataContent){
                 if(k==0){
                     jsonStr = jsonStr+"{tableKey:'"+prop+"',";
                     var rows = _splitContentByEnter(dataContent[prop]);
                     jsonStr = jsonStr+"tableContent:[";
                     for(var i=0;i<rows.length;i++){
                         rows[i] = rows[i].replace(/[']/g,"&#8217;");
                        // rows[i] = rows[i].replace(/["]/g,"&&quot;");
                         if(i==0){
                             jsonStr = jsonStr +"'"+ rows[i] +"'";
                         }else{
                             jsonStr = jsonStr +",'"+ rows[i] +"'";
                         }
                     }
                     jsonStr = jsonStr+"]}"
                 }else{
                     jsonStr = jsonStr+",{tableKey:'"+prop+"',";
                     var rows = _splitContentByEnter(dataContent[prop]);
                     jsonStr = jsonStr+"tableContent:[";
                     for(var i=0;i<rows.length;i++){
                         rows[i] = rows[i].replace(/[']/g,"&#8217;");
                        // rows[i] = rows[i].replace(/["]/g,"&&quot;");
                         if(i==0){
                             jsonStr = jsonStr +"'"+ rows[i] +"'";
                         }else{
                             jsonStr = jsonStr +",'"+ rows[i] +"'";
                         }
                     }
                     jsonStr = jsonStr+"]}"
                 }
                 k++;
             }
        }
        jsonStr = jsonStr+"]";
        return jsonStr;
    }

    function _splitContentByEnter(content){
        if(null!=content){
            return content.split("\n");
        }

    }

    return {
        show_hadoop_data_explorer:show_hadoop_data_explorer
    }

});
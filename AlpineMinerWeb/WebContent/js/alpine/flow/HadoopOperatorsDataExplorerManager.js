/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopOperatorsDataExplorerHelper
 * Author: Will
 * Date: 12-7-16
 */
define(["alpine/flow/WorkFlowManager"], function(workFlowHandler){
    function show_hadoop_data_explorer(connectionKey,hadooppath,opUID,flowInfoKey,isHDFileOperator,show_hadoop_data_explorer_callback){
        var urlFileType = baseURL+"/main/hadoopDataexplorer.do?method=getHadoopFileType4DataExplorer&operatorUID="+opUID+"&flowInfoKey="+flowInfoKey;
        ds.get(urlFileType,function(data){
            //file type
            if(null!=data.error){
                popupComponent.alert(data.error);
                return false;
            }
            switch (data.fileType){
                case "csv":
                    var url4HadoopFile_csv = baseURL+"/main/hadoopDataexplorer.do?method=getHadoopFileCsvTableData&connectionKey="
                    								+ connectionKey
                    								+ "&path=" + hadooppath
                    								+ "&operatorUID=" + opUID
                    								+ "&flowInfoKey=" + flowInfoKey
                    								+ "&isHDFileOperator=" + isHDFileOperator;
                    ds.get(url4HadoopFile_csv, show_hadoop_data_explorer_callback,null,true);
                    break;
                case "xml":
                    var url4HadoopFile_xml = baseURL+"/main/hadoopDataexplorer.do?method=getHadoopFileXmlDataTable&connectionKey="
                    								+ connectionKey
                    								+ "&path=" + hadooppath
                    								+ "&flowInfoKey=" + flowInfoKey;
                    ds.post(url4HadoopFile_xml,data.model,function(data){
                        var output= {};
                        output = data;
                        //callBack.call({},targetNode,output,"xml");
                        var dataViewContainerID = 'dataexplorer_id';
                        showVislualizationModel(output,dataViewContainerID);
                        //fillOutPaneDataTable(targetNode,output,"xml");
                    },null);
                    break;
                case "alpineLog":
                    var url4HadoopFile_json = baseURL + "/main/hadoopDataexplorer.do?method=getHadoopFileLogDataTable&connectionKey="
                        + connectionKey
                        + "&path=" + hadooppath
                        + "&flowInfoKey=" + flowInfoKey
                        + "&operatorUID=" + opUID
                        + "&logFormat=" + data.model.logFormat
                        + "&logType=" + data.model.logType
                        + "&size=" + 200
                        + "&viewType=dataexplorer";
                    ds.get(url4HadoopFile_json,function(data){
                        var output= {};
                        output = data;
                        //callBack.call({},targetNode,output,"xml");
                        var dataViewContainerID = 'dataexplorer_id';
                        showVislualizationModel(output,dataViewContainerID);
                        //fillOutPaneDataTable(targetNode,output,"xml");
                    },null);
                    break;
                case "json":
                    var url4HadoopFile_json = baseURL + "/main/hadoopDataexplorer.do?method=getHadoopFileJsonDataTable&connectionKey="
                                                        + connectionKey
                                                        + "&path=" + hadooppath
                                                        + "&flowInfoKey=" + flowInfoKey;
                    ds.post(url4HadoopFile_json,data.model,function(data){
                        var output= {};
                        output = data;
                        //callBack.call({},targetNode,output,"xml");
                        var dataViewContainerID = 'dataexplorer_id';
                        showVislualizationModel(output,dataViewContainerID);
                        //fillOutPaneDataTable(targetNode,output,"xml");
                    },null);
                    break;
            }
        },null,true);
        //return false;
       //
    };

    function getCurrentOperatorProperties(operator, callbackfunction, callbackPanelId, extraargs)
    {
        var actualCallback = callbackfunction;
        if (extraargs)
        {
            actualCallback = function(data)
            {
                callbackfunction(data,extraargs);
            }
        }

        var url = baseURL + "/main/property.do?method=getPropertyData" + "&uuid="
            + operator.uid + "&user=" + login;
        ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(),actualCallback,null, false,callbackPanelId);

    };

    function getCurrentOperatorPropertys4hdJoin_Set(operator,callbackfunction){
        var url = baseURL + "/main/property.do?method=getPropertyData" + "&uuid="
            + operator.uid + "&user=" + login;
        ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(),callbackfunction,null, true);
    }

    return {
        show_hadoop_data_explorer:show_hadoop_data_explorer,
        getCurrentOperatorProperties:getCurrentOperatorProperties,
        getCurrentOperatorPropertys4hdJoin_Set:getCurrentOperatorPropertys4hdJoin_Set
    };
});
define(["alpine/flow/WorkFlowManager"], function(workFlowHandler){
    function getHadoopFilesByPath(connectionKey,path,successCallback, callbackPanelId){
        var url4HadoopFile = baseURL+"/main/fileStructureManager.do?method=getHadoopFilesByPath";
        var parameters = {
        	connectionKey: connectionKey
        };
        if(null!=path && ""!=path){
        	parameters.path = path;
        }
        ds.getWithData(url4HadoopFile,parameters, successCallback, null, false, callbackPanelId);
    };

    function getAvailbleHadoopConnList(successCallBack){
        var url4HadoopFile = baseURL+"/main/dataSource/hadoop/manager.do?method=getAvailbleHadoopConnList";
        ds.get(url4HadoopFile,successCallBack,null,true);
    };

    function gethadoopContent(connectionKey,path,hadoopPropery_Config_Colum_ImportData_Success_Callback){
        var url4HadoopFile = baseURL+"/main/fileStructureManager.do?method=gethadoopContent";
        ds.getWithData(url4HadoopFile,{
        	connectionKey: connectionKey,
        	path: path,
        	size: 100
        },hadoopPropery_Config_Colum_ImportData_Success_Callback,function(){
            alpine.props.HadoopFileOperatorPropertyHelper.setFileContent("");
        },true);
    };

    function getFileStructureColumnNameWithType(connectionKey,opUID,callBackFunction){
        var url4HadoopFile = baseURL+"/main/fileStructureManager.do?method=getFileStructureColumnNameWithType&connectionKey="+connectionKey+"&operatorUID="+opUID;
        ds.post(url4HadoopFile,workFlowHandler.getEditingFlow(),callBackFunction,null,true);
    };

    return {
        getHadoopFilesByPath:getHadoopFilesByPath,
        getAvailbleHadoopConnList:getAvailbleHadoopConnList,
        gethadoopContent:gethadoopContent,
        getFileStructureColumnNameWithType:getFileStructureColumnNameWithType
    }
});
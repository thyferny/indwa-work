/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopFileStructure4LogManager.js
 * Author: Will
 * Date: 12-11-8
 */
define([],function(){
    function getHadoopLogFileContents(connectionKey,path,flowInfoKey,opUID,logFormat,logType,size,viewType,callback){
        var url =baseURL+"/main/fileStructureManager.do?method=getHadoopLogFileContent";
        ds.getWithData(url,{
        	connectionKey: connectionKey,
        	path: path,
            flowInfoKey:flowInfoKey,
            operatorUID:opUID,
        	logFormat: logFormat,
        	logType: logType,
        	size: 200,
            viewType:viewType
        },callback,null,false);
    }
    return {
        getHadoopLogFileContents:getHadoopLogFileContents
    };
});
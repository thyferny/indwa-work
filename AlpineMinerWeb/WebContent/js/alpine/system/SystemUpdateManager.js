/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: SystemUpdateManager
 * Author: Will
 * Date: 12-12-11
 */
define([],function(){
    function getUpdateFileInfos(okCallback,errorCallback, containerId){
        var url = baseURL+"/main/systemUpdate.do?method=getUpdateFileInfos";
        ds.get(url,okCallback,errorCallback, false, containerId);
    }

    function getCurrentRunFlowInfo(okCallback,errorCallback, containerId){
        var url = baseURL+"/main/systemUpdate.do?method=getCurrentRunFlowInfo";
        ds.get(url,okCallback,errorCallback, false, containerId);
    }

    function execuSystemUpdate(updateModel,okCallback,errorCallback, containerId){
        var url = baseURL+"/main/systemUpdate.do?method=execuSystemUpdate";
        ds.post(url,updateModel,okCallback,errorCallback, false, containerId);
    }
    function haveNewUpdateVersion(okCallback,errorCallback){
        var url = baseURL+"/main/systemUpdate.do?method=haveNewUpdateVersion";
        ds.get(url,okCallback,errorCallback);
    }
    

    return {
        getUpdateFileInfos:getUpdateFileInfos,
        getCurrentRunFlowInfo:getCurrentRunFlowInfo,
        execuSystemUpdate:execuSystemUpdate,
        haveNewUpdateVersion:haveNewUpdateVersion
    }
});
/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopFileStructure4XMLManager
 * Author: Will
 * Date: 12-10-26
 */
define([],function(){
    function getXMLContent(fileStructureModel,connectionKey,path,targetNode,callBack){
        var url = baseURL+"/main/fileStructureManager.do?method=getXMLFileContent&connectionKey="+connectionKey+"&path="+path;
        //ds.get(url,callBack,null,true);
        var str = dojox.json.ref.toJson(fileStructureModel);
        var xhrArgs = {
            url: url,
           // url:"sample1.xml",
            handleAs: "xml",
            sync:false,
            postData : str,
            preventCache:true,
            headers: {
                "Content-Type": "text/json",
                "Content-Encoding": "utf-8"
            },
            load:function(data){
                callBack(data);
            },
            error: function(error){
                //targetNode.innerHTML = "An unexpected error occurred: " + error;
                targetNode.innerHTML = "Load error";
            }
        };
        dojo.rawXhrPost(xhrArgs);
    };

    function getPreviewData4xml(fileStructureModel,connectionKey,path,flowInfoKey,targetNode,callBack,errorCallback){
        var url = baseURL+"/main/fileStructureManager.do?method=getXMLFileContentAsTable&connectionKey="+connectionKey+"&path="+path+"&flowInfoKey="+flowInfoKey;
        ds.post(url,fileStructureModel,function(data){
            dojo.byId("mask_xmlfileStruct_preview").style.display="none";
            var output= {};
             output.visualData = data;
            callBack.call({},targetNode,output,"xml");
        },function(error){
            //popupComponent.alert(error);
            dojo.byId("mask_xmlfileStruct_preview").style.display="none";
            if(null!=errorCallback){
                errorCallback(error);
            }
        });
    }

    return {
        getXMLContent:getXMLContent,
        getPreviewData4xml:getPreviewData4xml
    };
});
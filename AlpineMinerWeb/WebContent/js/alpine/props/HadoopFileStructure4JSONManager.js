/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * File: HadoopFileStructure4JSONManager
 * Author: Will
 * Date: 12-11-14
 */
define([],function(){

    function getJSONFileContent(fileStructureModel,connectionKey,path,callBack){
        var url = baseURL+"/main/fileStructureManager.do?method=getJSONContent&connectionKey="+connectionKey+"&path="+path;
        ds.post(url,fileStructureModel,callBack,function(){
            dojo.byId("mask_jsonfileStruct_column_define").style.display="none";
            dojo.byId("jsonDomTreeContainer").innerHTML=alpine.nls.hadoop_prop_file_structure_xml_load_structure_error_tip;
            dojo.byId("hadoop_file_struct_json_tree_node_select").value = "";
            dijit.byId("hadoop_prop_file_structure_json_tree_select_level").set("disabled",true);
        });
    }

    function getPreviewData4JSON(fileStructureModel,connectionKey,path,targetNode,callBack){
        var url = baseURL+"/main/fileStructureManager.do?method=getJSONFileContentAsTable&connectionKey="+connectionKey+"&path="+path;
        ds.post(url,fileStructureModel,function(data){
            dojo.byId("mask_JSONfileStruct_preview").style.display = "none";
            var output= {};
            output.visualData = data;
            callBack.call({},targetNode,output,"xml");
        },function(){
            dojo.byId("mask_JSONfileStruct_preview").style.display = "none";
        });
    }

    return {
        getJSONFileContent:getJSONFileContent,
        getPreviewData4JSON:getPreviewData4JSON
    }
});
/* COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * FlowVersionHistoryUIHelper
 * Author: Robbie
 */

define([
    "dojo/ready",
    "dojo/on",
    "dijit/registry",
    "alpine/flow/FlowVersionHistoryManager",
    "alpine/flow/WorkFlowManager"
], function(ready, on, registry, historyManager, workflowManager){

    var constants = {
        MENU: "btn_show_flow_history",
        CANVAS: "FlowDisplayPanelPersonal"
    };

    ready(function(){
        on(registry.byId(constants.MENU),"click",showFlowHistoryForOpenFlow);
    });

    function showFlowHistoryForOpenFlow() {
        var openedFlowInfo = [];
        openedFlowInfo.push(workflowManager.getEditingFlow());
        _showFlowHistoryDialog(openedFlowInfo, constants.CANVAS);
    }

    function _showFlowHistoryDialogFromTree(items) {
        //var items = flowCategoryHelper.getSelectedFlows();
        if (items||items.length&&items.length>0) {
            var flowInfos = new Array();
            for(var i = 0;i<items.length;i++){
                var item = items[i];
                var flowInfo = tree_item_toFlow_info(item);
                flowInfos.push(flowInfo) ;
            }

            //progressBar.showLoadingBar();
            //var  url =baseURL+"/main/flow/version.do?method=getFlowVersionInfos"  ;
            //ds.post(url, flowInfos, show_flow_history_callback, null,false, "flow_category_tree");
            _showFlowHistoryDialog(flowInfos, "flow_category_tree");

        }

    }

    function _showFlowHistoryDialog(flowInfo, panel) {
        historyManager.fetchFlowHistory(flowInfo, showFlowHistoryDialogCallback, panel);
    }

    function showFlowHistoryDialogCallback(flowHistoryList) {
        registry.byId("flowHistoryDialog").show();
        registry.byId("flowHistoryDialog").resize(600,300);
        var flowHistoryTable = registry.byId('flowHsitoryTable');
        initFlowHistoryListTable(flowHistoryList,flowHistoryTable);
    }

    function initFlowHistoryListTable(flowHistoryList, flowHistoryTable){

        //fill the flowversion info into the table
        if(!flowHistoryList ||flowHistoryList .length==0){
            popupComponent.alert(alpine.nls.history_not_found);
            return;
        }
        for(var i=0;i<flowHistoryList.length;i++){
            var info = flowHistoryList[i];
            if(info.modifiedTime){
                info.modifiedTime = alpine_format_date(new Date(info.modifiedTime));
            }
        }

        //for sorting ...
        for(var i=0;i<flowHistoryList.length;i++){
            if(flowHistoryList[i].version&&flowHistoryList[i].version!=""){
                flowHistoryList[i].version=flowHistoryList[i].version*1;
            }
        }

        var dataTable = {
            items : flowHistoryList
        };
        // our test data store for this example:
        var flowHistoryStore = new dojo.data.ItemFileWriteStore({
            data : dataTable
        });


        // this will make the edit ok
        flowHistoryTable.setStore(flowHistoryStore);



        if(flowHistoryStore._arrayOfTopLevelItems
            &&flowHistoryStore._arrayOfTopLevelItems.length>0){
            flowHistoryTable.selection.select(0);
            flowHistoryTable.updateRow(0);

        }else{
            flowHistoryTable.selection.deselectAll();

        }
        // Call startup, in order to render the grid:
        flowHistoryTable.render();

        //dojo.connect(flowHistoryTable, "onRowDblClick",  perform_open_flow_history );
    }

    function select_flow_history(){
        var flowHistoryTable = registry.byId('flowHsitoryTable');
        var items = flowHistoryTable.selection.getSelected();
        //only download one by one...
        if(items&&items.length==1){
            registry.byId("flow_history_download_id").set("disabled",false);
            registry.byId("replace_flow_by_history").set("disabled",false);
        }else{
            registry.byId("flow_history_download_id").set("disabled",true);
            registry.byId("replace_flow_by_history").set("disabled",true);
        }

    }

    function perform_download_flow_history(){
        // item is modelInfo
        var flowHistoryTable = registry.byId('flowHsitoryTable');
        var items = flowHistoryTable.selection.getSelected();
        if(!items||items.length==0){
            popupComponent.alert(alpine.nls.please_select_a_Flow);
        }else{
            var  url =baseURL+"/main/flow/version.do?method=downLoadFlowVersions"  ;
            for(var i=0;i<items.length;i++){

                var info =tree_item_toFlow_info(items[i]) ;
                //make sure the json work...
                info.modifiedTime =0;
                ds.post(url, info, downLoadFlowVersions_callback);

            }

        }

        function downLoadFlowVersions_callback(data){

            var download_url = baseURL + "/temp_flow/"+login+"/" + data;

            //modify by will begin
            var servlet_url = baseURL+"/CommonFileDownLoaderServlet?downloadFileName="+data+"&tempType=temp_flow&filePath=/"+login+"/";
            window.location.href = servlet_url;
            return false;
            //modify by will begin

//        var str = alpine.nls.save_as
//            + "<a href="
//            + download_url
//            + "><u><i><font color='blue'>" + data + "</font></i></u></a>";
//
//        dojo.html.set(dojo.byId("down_a_flow_label"), str);
        }
    }

    function replace_current_flow_by_history(){
        registry.byId('flowHistoryDialog').hide();

        var flowHistoryTable = registry.byId('flowHsitoryTable');
        var items=flowHistoryTable.selection.getSelected();
        if(items &&items.length>0){
            var item = items[0];
            var flow = tree_item_toFlow_info(item);


            flow.modifiedTime =0;


            var  url =baseURL+"/main/flow/version.do?method=replaceWithVersion"  ;
            ds.post(url, flow, make_newversion_callback);

        }




        //1 check the open status, if open, close it
        //2 call the service to make a history and use tge old content as the current version
        //3 re_show the tree, if opened, reopen it ...
    }

    function make_newversion_callback(result){
        //progressBar.closeLoadingBar();
        //result is the new flowInfo
        if(result&&result.version){
            var make_version_reopen=false;
            if(alpine.flow.WorkFlowManager.isEditing(result)){
                if (alpine.flow.WorkFlowManager.isDirty()) {
                    popupComponent.confirm(alpine.nls.update_not_saved,{
                        handle: function(){
                            var saveFlowCallback = function(){
                                alpine.flow.WorkFlowUIHelper.release();
                                alpine.flow.WorkFlowUIHelper.openWorkFlow(result);
                            };
                            //save_flow();
                            alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
                        }
                    },{
                        handle: function(){
                            alpine.flow.WorkFlowUIHelper.release();
                            alpine.flow.WorkFlowUIHelper.openWorkFlow(result);
                        }
                    });
                }else{
                    alpine.flow.WorkFlowUIHelper.release();
                    alpine.flow.WorkFlowUIHelper.openWorkFlow(result);
//	            open_flow("Personal", result);
                }
            }
            window.setTimeout(alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree, 2000);
        }else{
            popupComponent.alert(alpine.nls.error+":"+result.message);	}
    }

    /********************************************************************************/

    function select_flow_history_add(){
        var flowHistoryTable = registry.byId('flowHsitoryTable_Add');
        var items = flowHistoryTable.selection.getSelected();
        //only download one by one...
        if(items&&items.length==1){
            registry.byId("flow_history_open_id").set("disabled",false);
            //registry.byId("flow_history_open_menu").set("disabled",false);
        }else{
            registry.byId("flow_history_open_id").set("disabled",true);
            //registry.byId("flow_history_open_menu").set("disabled",true);
        }

    }

    function perform_flow_history_open_when_add(){

        registry.byId('flowHistoryDialog_Add').hide();
        var flowHistoryTable = registry.byId('flowHsitoryTable_Add');
        var items=flowHistoryTable.selection.getSelected();
        if(items &&items.length>0){
            var item = items[0];
            var flow = tree_item_toFlow_info(item);
            flow.modifiedTime =0;
            open_flow("Group", flow);

        }

    }

    function open_flow_history_dlg_foradd_for_addtopersonalflow(flowInfos){
        //progressBar.showLoadingBar();
        var  url =baseURL+"/main/flow/version.do?method=getFlowVersionInfos"  ;
        ds.post(url, flowInfos, show_flow_history_foradd_callback4_addtopersonal, null, false, "GroupContainer");
    }

    function show_flow_history_foradd_callback4_addtopersonal(flowHistoryList){

        registry.byId('flowHistoryDialog_Add').show();
        registry.byId('flowHistoryDialog_Add').resize(600, 300);

        var flowHistoryTable = registry.byId('flowHsitoryTable_Add');
        initFlowHistoryListTable(flowHistoryList,flowHistoryTable);

        //init the selectio1 ;

        var items = flowHistoryTable.store._arrayOfTopLevelItems;
        flowHistoryTable.selection.deselectAll();
        if(items &&items.length>0){
            flowHistoryTable.selection.select(items.length-1);
        }
        progressBar.closeLoadingBar();

    }

    function open_flow_history_dlg_foradd (){
        var flow_list = new Array();
        var idx = 0;
        var items = CurrentFlowTree['Group'].selectedItems;
        if (items||items.length&&items.length>0) {
            var flowInfos = new Array();
            //items.length always == 0
            for(var i = 0;i<items.length;i++){
                var item = items[i];
                var flowInfo = tree_item_toFlow_info(item) ;
                flowInfos.push(flowInfo) ;
                if(CurrentFlow_Group&&CurrentFlow_Group.key == item.key[0]){
                    //just the current opened item's version
                }else{
                    CurrentAddingFlow_Version = flowInfo.version;
                }
            }
            console.log("THINK THIS IS NOT BEING USED!!!!!!!!!!!!!!!!!!!");
            //progressBar.showLoadingBar();
            var  url =baseURL+"/main/flow/version.do?method=getFlowVersionInfos"  ;
            ds.post(url, flowInfos, show_flow_history_foradd_callback, null, false, "flow_category_tree");

        }

        function show_flow_history_foradd_callback(flowHistoryList){

            registry.byId('flowHistoryDialog_Add').show();
            registry.byId('flowHistoryDialog_Add').resize(600, 300);

            var flowHistoryTable = registry.byId('flowHsitoryTable_Add');
            initFlowHistoryListTable(flowHistoryList,flowHistoryTable);

            //init the selectio...


            var version = CurrentAddingFlow_Version*1 ;

            var items = flowHistoryTable.store._arrayOfTopLevelItems;
            flowHistoryTable.selection.deselectAll();
            if(items &&items.length>0){
                for(var i =0 ;i<items.length;i++){
                    var item = items[i];
                    if(item.version[0]==version){
                        flowHistoryTable.setStore(flowHistoryTable.store);
                        flowHistoryTable.updateRow(i);
                        flowHistoryTable.selection.select(i);
                        break;
                    }
                }
            }
            progressBar.closeLoadingBar();

        }

    }

    return {
        showFlowHistoryDialogFromTree: _showFlowHistoryDialogFromTree,
        select_flow_history: select_flow_history,
        perform_download_flow_history: perform_download_flow_history,
        replace_current_flow_by_history: replace_current_flow_by_history,
        select_flow_history_add: select_flow_history_add,
        perform_flow_history_open_when_add: perform_flow_history_open_when_add,
        open_flow_history_dlg_foradd_for_addtopersonalflow: open_flow_history_dlg_foradd_for_addtopersonalflow,
        open_flow_history_dlg_foradd: open_flow_history_dlg_foradd,
        showFlowHistoryForOpenFlow:showFlowHistoryForOpenFlow
    }

});
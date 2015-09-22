/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * FlowCategoryUIHelper
 * Author Gary
 */
define([
    "dojo/on",
    "dijit/registry",
    "alpine/flow/FlowCategoryManager",
    "alpine/flow/RecentlyHistoryFlowManager",
    "alpine/flow/WorkFlowManager",
    "alpine/flow/FlowDraftManager",
    "alpine/spinner",
    "alpine/layout/GuiHelper",
    "alpine/flow/FlowVersionHistoryUIHelper"
], function(on, registry, flowCategoryManager, recentlyHistoryFlowManager, workFlowHandler, flowDraftHander, spinner, guiHelper, flowVersionHistoryHelper){
    var constants = {
        TREE_ID: "flow_category_tree",
        TREE_CONTAINER_ID: "flow_category_tree_container",
        TREE_MENU_ID: "flow_category_tree_menu",
        CATEGORY_NAME_DIALOG: "flow_category_input",
        CATEGORY_NAME_INPUT: "flow_category_input_categoryName",
        CATEGORY_NAME_SUBMIT: "flow_category_input_submit",
        CATEGORY_NAME_CANCEL: "flow_category_input_cancel",
        FLOW_DISPLAY_PERSONAL:"FlowDisplayPanelPersonal",
        NEW_FLOW_DIALOG:"alpine_flow_newflow_dialog",
        NEW_FLOW_NAME: "alpine_flow_newflow_dialog_name",
        NEW_FLOW_DESC: "alpine_flow_newflow_dialog_description",
        NEW_FLOW_CAT: "alpine_flow_newflow_dialog_category",
        NEW_FLOW_DEFAULT_TABLE_VALUE:"alpine_flow_newflow_dialog_defaultvalue",
        WORKBENCH_PANEL: "personalFlowTree",
        RENAME_FLOW: "rename_flow_action_button"
    };
    var rootKey;
    var suffixForDeaultWorkFlow='lastWorkFlowForTheUserOf_';

    var userID="";
    var catPatrn = /^(\w|\s){1,20}$/;
    var flowPatrn =  /^[^~#%&*{}\/\\\:<>\?|\"\'\.]*$/;

    dojo.ready(function(){
        var treeData = flowCategoryManager.getTreeData(showErrorMsg, constants.WORKBENCH_PANEL);
        createFlowTree(constants.TREE_CONTAINER_ID, constants.TREE_ID, treeData);
        
        flowDraftHander.checkDraftExist(function(hasDraft){
        	if(hasDraft == true){
    			popupComponent.confirm(alpine.nls.flowdraft_message_opendraft, {
    				label: alpine.nls.flowdraft_button_opendraft_label,
    				handle: function(){
    	        		flowDraftHander.getDraftFlowDTO(function(result){
                    		alpine.flow.WorkFlowUIHelper.openWorkFlowAsWorkflowDTO(result.flowInfo, result.flowDTO);
                    		alpine.flow.WorkFlowUIHelper.setDirty(true);
    	        		});
    				}
    			}, {
    				//label: alpine.nls.flowdraft_button_reevert_label,
    				handle: function(){
    	                loadDefaultWorkFlowIfDefaultExist(alpine.USER);
    				}
    			}, {width: "400px"});
        	}else{
                loadDefaultWorkFlowIfDefaultExist(alpine.USER);
        	}
        });

        //new flow dialog
        dojo.connect(registry.byId('button_new_flow'), 'onClick', open_new_flow_dialog_no_cat);
        //  do new flow
        dojo.connect(registry.byId('alpine_flow_newflow_create_flow'), 'onClick',confirmThenCreateFlow );
        on(registry.byId(constants.RENAME_FLOW), 'click', renameOpenFlow);
    });




    /**
     * Functions for creating a new flow in the root directory
     */
    function open_new_flow_dialog_no_cat(event)
    {
        open_new_flow_dialog();
    }

    function open_new_flow_dialog(category)
    {
        console.log("opening new flow dialog");
        var catTextBox =  dojo.byId(constants.NEW_FLOW_CAT);
        if (category && catTextBox) catTextBox.value = category;
        else if ( catTextBox)   catTextBox.value = "";

        dijit.byId(constants.NEW_FLOW_DIALOG).titleBar.style.display='none';

        //reset the textboxes:
        dojo.byId(constants.NEW_FLOW_NAME).value = "";
        dojo.byId(constants.NEW_FLOW_DESC).value = "";
        dojo.byId(constants.NEW_FLOW_DEFAULT_TABLE_VALUE).value = "alp";


        dijit.byId(constants.NEW_FLOW_NAME).isValid = function(){
            var val = this.get("value");
            var patrn = flowPatrn;  //This filters to make sure we have no invalid characters.

            return patrn.test(val);
        };

        dijit.byId(constants.NEW_FLOW_DEFAULT_TABLE_VALUE).isValid = function() {
            var val = this.get("value");
            return validateTableName(val, true);
        };

        dijit.byId(constants.NEW_FLOW_DIALOG).show();
    }

    function confirmThenCreateFlow(){
        console.log("MADE IT!");

        var new_flow_comments = dojo.byId(constants.NEW_FLOW_DESC).value;
        var new_flow_name = dojo.byId(constants.NEW_FLOW_NAME).value.trim();
        var new_flow_category = dojo.byId(constants.NEW_FLOW_CAT).value.trim();
        var new_flow_default_prefix = dojo.byId(constants.NEW_FLOW_DEFAULT_TABLE_VALUE).value.trim();

        if (!validateAndConfirmNotEmptyFlowName(constants.NEW_FLOW_NAME,new_flow_name)) return;

        checkRes= dijit.byId(constants.NEW_FLOW_DEFAULT_TABLE_VALUE).validate();
        if(!checkRes){
            popupComponent.alert(alpine.nls.createflow_alert_invaliddefaultflow);
            return;
        }

        if (workFlowHandler.isEditing() && workFlowHandler.isDirty())
        {
            //there is another flow opened that needs to be saved potentially.
            dijit.byId("alpine_flow_newflow_dialog").hide();
            popupComponent.saveConfirm(alpine.nls.update_not_saved,{
                handle: function(){
                    var saveFlowCallback = function(){
                    	alpine.flow.WorkFlowUIHelper.release();
                        createTheFlow(new_flow_name, new_flow_comments,new_flow_category, new_flow_default_prefix);
                    };
                    alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
                }
            },{
                handle: function(){
                    // just clean screen, if click button with name is abort.
                	alpine.flow.WorkFlowUIHelper.release();
                    createTheFlow(new_flow_name, new_flow_comments,new_flow_category,new_flow_default_prefix);
                }
            });
        } else{
        	alpine.flow.WorkFlowUIHelper.release();
            createTheFlow(new_flow_name, new_flow_comments,new_flow_category,new_flow_default_prefix);
        }
    }

    function createTheFlow(name, comments, catkey, defaultPrefix) {
        dijit.byId("alpine_flow_newflow_dialog").hide();
        var  url =baseURL+"/main/flow/import_flow.do?method=newFlow" + "&user=" + login +"&flowName="+name+"&comments="+comments+"&catKey=" + catkey + "&defaultPrefix=" + defaultPrefix;
        ds.post(url, null, function(data) {
            //talking to the server went fine, but it's returning an error... need to handle it.
            if (data.message && data.error_code)
            {
                spinner.hideSpinner(constants.FLOW_DISPLAY_PERSONAL);
                popupComponent.alert(data.message);
                return;
            }
            rebuildCategoryTree();
            alpine.flow.WorkFlowUIHelper.openWorkFlow(data);
            var workflowpath = buildDefaultWorkFlowPath(data);
            var theArray = buildTheArrayForTheTreeNode(workflowpath);
            selectFlowByFlowInfoKey(theArray);
        }, null, false,constants.FLOW_DISPLAY_PERSONAL);
    }

    function validateAndConfirmNotEmptyFlowName(textfieldId)
    {
        var checkRes = dijit.byId(textfieldId).validate();
        if(!checkRes){
            popupComponent.alert(alpine.nls.createflow_alert_invalidcharacters);
            return false;
        }
        var new_flow_name = dojo.byId(textfieldId).value.trim();

        if (!new_flow_name || new_flow_name.length<1)
        {
            popupComponent.alert(alpine.nls.createflow_alert_flowname_empty);
            return false;
        }
        return true;
    }

    /*
    functions for renaming the flow
     */

    function renameFlowFromTree(treeItem) {
        var item = toolkit.getValue(treeItem.info);
        if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
            return;
        }
        var flowInfo = tree_item_toFlow_info(item);
        openRenameFlowDialog(flowInfo);
    }

    function renameOpenFlow() {
        var flowInfo = workFlowHandler.getEditingFlow();
        openRenameFlowDialog(flowInfo);
    }

    function openRenameFlowDialog(flowInfo) {
        guiHelper.showTextFieldEditorDialog(
            function (newName) {
                confirmThenRenameFlow(newName, flowInfo);
            },
            flowInfo.id,
            flowPatrn,
            alpine.nls.renameflow_dialog_title,
            alpine.nls.renameflow_dialog_okbutton,
            function(dijitid, value) {
                return validateAndConfirmNotEmptyFlowName(dijitid,value);
            }
        );
    }

    function confirmThenRenameFlow(newName, flowInfo)
    {
        //need current flow info
/*
        var item = toolkit.getValue(treeItem.info);
        if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
            return;
        }
        var flowInfo = tree_item_toFlow_info(item);
*/

        // if already working on this flow, then we need to save it first.
        if (workFlowHandler.isEditing(flowInfo) && workFlowHandler.isDirty())
        {
            //this flow is opened and dirty so we need to handle it.
            popupComponent.saveConfirm(alpine.nls.update_not_saved,{
                handle: function(){
                    var saveFlowCallback = function(){
                        flowInfo.version++;
                        alpine.flow.WorkFlowUIHelper.release();
                        renameFlow(newName, flowInfo, true);
                    };
                    alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
                }
            },{
                handle: function(){
                    alpine.flow.WorkFlowUIHelper.release();
                    renameFlow(newName, flowInfo,true);
                }
            });
        } else if (workFlowHandler.isEditing(flowInfo))    //if changing current flow, already saved
        {
            alpine.flow.WorkFlowUIHelper.release();
            renameFlow(newName, flowInfo,true);
        }
        else{    //renaming flow that isn't opened
            renameFlow(newName, flowInfo, false);
        }
    }

     function  renameFlow(newName, flowInfo, reloadFlow)
     {
         workFlowHandler.renameFlow(newName, flowInfo,
             function(data){
                 rebuildCategoryTree();
                 if (reloadFlow)
                 {
                     console.log("MUST RELOAD THE CURRENT FLOW!: ");
                     alpine.flow.WorkFlowUIHelper.openWorkFlow(data);
                 }
             });
     }

    function fetchTheWorkFlowForTheUser(){
        var lastWorkFlowKey=suffixForDeaultWorkFlow+userID;
        return dojox.storage.get(lastWorkFlowKey);
    }
    
    /*
     * this function should be called when close workflow and delete editing workflow and delete category which include editing workflow, to clean latest editing version.
     */
    function _removeWorkFlowEditingTrail(){
    	dojox.storage.remove(suffixForDeaultWorkFlow + userID);
    }

    function buildTheArrayForTheTreeNode(valueForTheKey){
        if (!valueForTheKey) valueForTheKey=fetchTheWorkFlowForTheUser();
        if(''==valueForTheKey||null==valueForTheKey){
            console.log("Nothing is there");
            return null;
        }
        var pathArray = new Array();
        pathArray = valueForTheKey.split("|");
//        var ccString='';
//        for(var i=0; i<pathArray.length; i++  ){
//            ccString = (i==0?pathArray[i]:(ccString+'/'+pathArray[i]));
//            pathArray[i]=ccString;
//        }

        return pathArray;
    }

    function selectFlowByFlowInfoKey(flowInfoKeyArray)
    {
        if (null==flowInfoKeyArray||0==flowInfoKeyArray.length)
        {
            console.log("Array is empty so we will just do nothing");
            return false;
        }
        var fct = dijit.byId("flow_category_tree");
        var evt = document.createEvent("HTMLEvents");

        fct.set('path',flowInfoKeyArray ).then(function(value){
                if (!value || (value[0] && !value[0][0])) return;
                fct._publish("execute", { item: fct.selectedNode.item, node: fct.selectedNode, evt: evt } );
                fct.onDblClick(fct.selectedNode.item, fct.selectedNode, evt);
                fct.focusNode(fct.selectedNode);
            },
            function(error){
                console.log("couldn't select");
                return false;
            }
        );
        return true;

    }
    function loadDefaultWorkFlowIfDefaultExist(user) {
        console.log("We are in loadDefaultWorkFlowIfDefaultExist");
        userID=user;
        var theArray= buildTheArrayForTheTreeNode();
        return selectFlowByFlowInfoKey(theArray);
      }

    function storeFlowPath(flowInfo){
        var lastWorkFlowKey=suffixForDeaultWorkFlow+userID;
    	var workFlowTitle = buildDefaultWorkFlowPath(flowInfo);
	   // var login = buildDefaultWorkFlowPath(flowInfo);
       // var lastWorkFlowKey='lastWorkFlowForTheUserOf_'+login;
        console.log("Will be storing for the key of:"+lastWorkFlowKey+",with the value of:"+workFlowTitle);
        dojox.storage.put(lastWorkFlowKey,workFlowTitle);
    }

    /**
     * create flow Tree
     */
    function createFlowTree(treeContainerID, treeID, rootData){
        var tree = createTree(treeContainerID, treeID, rootData),
            treeMenu = createTreeMenu(tree);
    }

    function createTree(treeContainerID, treeID, rootData){
        rootKey = rootData.key;
        var resourceStore = new dojo.data.ItemFileWriteStore({
            data: {
                identifier: "key",// refer to ResourceItem
                label: "name",
                items: [{
                    key: rootData.key,
                    name: "",
                    isCategory: rootData.isCategory,
                    subItems: rootData.subItems
                }]
            }
        });
        var treeContainer = dojo.create("div", {}, treeContainerID);
        var tree = new dijit.Tree({
            showRoot: false,
            id: treeID,
            style : "height: 100%;padding-top:5px;",
            /*openOnDblClick: true,*/
            openOnClick: true,
            persist: false,
            //autoExpand: true,
            model: new dijit.tree.TreeStoreModel({
                store: resourceStore,
                childrenAttrs : ["subItems"],
                query: {
                    "key": "*"
                }
            }),
            getIconClass: function(item, opened){
                if(toolkit.getValue(item.key) == rootKey){
                    return "dijitIconDatabase";
                }
                if(item.isCategory){
                    return opened ? "workFlowOpenedDirNodeIcon" : "workFlowClosedDirNodeIcon";
                }else{
                    return "workFlowIcon";
                }
            },
            getLabelClass: function(item){
                if(item.isCategory){
                    return "workflowFolderLabel";
                }else{
                    return "workflowLeafLabel";
                }
            },
            dndController: "dijit.tree.dndSource",
            mayHaveChildren: function(treeItem){
                return treeItem.isCategory != undefined;
            },
            onClick: function(treeItem){
                var selectedItems = this.get('selectedItems');
                var ableToShowButton = true;
                for(var i = 0;i < selectedItems.length;i++){
                    var isCategory = this.mayHaveChildren(selectedItems[i]);
                    if(isCategory || toolkit.getValue(selectedItems[i].key) == rootKey){
                        ableToShowButton = false;
                        break;
                    }
                }
//					dijit.byId("btn_tree_delete_flow").set("disabled",!ableToShowButton);
//					dijit.byId("btn_tree_history_flow").set("disabled",!ableToShowButton);
            },
            onDblClick: function(treeItem){
                if(this.mayHaveChildren(treeItem) || toolkit.getValue(treeItem.key) == rootKey){
                    return;//category
                }
                var item = toolkit.getValue(treeItem.info);
                if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
                    return;
                }
                var flow = tree_item_toFlow_info(item);
                //storeFlowPath(buildDefaultWorkFlowPath(item,userID));
                alpine.flow.WorkFlowUIHelper.openWorkFlow(flow);
            },
            //check selected items can be moved.
            checkAcceptance: function(source, nodes){
                if(this != source){
                    return;
                }
                var canMoved = true;
                for(var i = 0;i<nodes.length;i++){
                    var treeItem = dijit.getEnclosingWidget(nodes[i]).item;
                    canMoved &= (!dijit.byId(constants.TREE_ID).mayHaveChildren(treeItem) && toolkit.getValue(treeItem.key) != rootKey);
                }
                return canMoved;
            },
            //check selected items can be moved to current node
            checkItemAcceptance: function(node, source, position){
                if(this != source){
                    return;
                }
                this.movedNode = dijit.getEnclosingWidget(node);
                var treeItem = this.movedNode.item;
                var movedItems = dijit.byId(constants.TREE_ID).get('selectedItems');
                var canMoved = true;
                for(var i = 0;i < movedItems.length;i++){
                    canMoved &= toolkit.getValue(movedItems[i].parentKey) != toolkit.getValue(treeItem.key);
                }
                return canMoved && treeItem.key != rootKey && dijit.byId(constants.TREE_ID).mayHaveChildren(treeItem);
            },
            onDndDrop: function(source, nodes, copy){
                if(this != source){
                    return;
                }
                var treeWidget = dijit.byId(constants.TREE_ID);
                var flowBasisInfos = new Array().concat(treeWidget.get('selectedItems'));// Because maybe modify it at line 147
                var newParentNode = this.movedNode;
                if(newParentNode == null || !this.tree.mayHaveChildren(newParentNode.item)){
                    this.onDndCancel();
                    return;// return if target is not a category(appeared quickly moved a flow on other one in IE).
                }

                var newParentItem = newParentNode.item;
                var categoryKey = toolkit.getValue(newParentItem.key);
                for(var i = 0;i < flowBasisInfos.length;i++){
                    var flowBasisInfo = flowBasisInfos[i];
                    if(this.tree.mayHaveChildren(flowBasisInfo)){
                        this.onDndCancel();
                        return;// return if any selected items are category.
                    }
//                    var flowkey = buildFlowPath(toolkit.getValue(flowBasisInfo.info));
                    if(workFlowHandler.isEditing(toolkit.getValue(flowBasisInfo.info))){
                        popupComponent.alert(dojo.string.substitute(alpine.nls.flow_category_message_move_opened, {
                            flowName: workFlowHandler.getEditingFlow().id
                        }));
                        flowBasisInfos.splice(i, 1);
                        dijit.byId(constants.TREE_ID).set("selectedItems", flowBasisInfos);
                    }
                }

                //distinct flow
                checkDuplicateFlow(categoryKey, flowBasisInfos, function(moveFlowArray){
                    var flowBasisInfoArray = new Array();
                    for(var i = 0;i < moveFlowArray.length;i++){
                        flowBasisInfoArray.push({
                            key: toolkit.getValue(moveFlowArray[i].key),
                            name: toolkit.getValue(moveFlowArray[i].name),
                            parentKey: toolkit.getValue(moveFlowArray[i].parentKey),
                            path: toolkit.getValue(moveFlowArray[i].path)
                        });
                    }

                    flowCategoryManager.moveFlow({
                        key: categoryKey
                    }, flowBasisInfoArray, function(successMap){
                        //filter failed flow. and replace info.
                        var successFlowInfoList = dojo.filter(moveFlowArray, function(x){
                            var flowModel = successMap[toolkit.getValue(x.key)];
                            return flowModel != undefined;
                        });

                        for(var i = 0; i < successFlowInfoList.length;i++){
                            var newFlowInfo = successMap[toolkit.getValue(successFlowInfoList[i].key)];
                            var flowBasisInfo = successFlowInfoList[i];
//                            var flowkey = buildFlowPath(toolkit.getValue(flowBasisInfo.info));
                            moveFlowHandler({
                                flowInfo: flowBasisInfo,
                                newFlowInfo: newFlowInfo,
                                targetKey: categoryKey,
                                orignalKey: toolkit.getValue(flowBasisInfo.parentKey)
                            });
                            if(workFlowHandler.isEditing(flowBasisInfo.info)){
                                //update opened flow's category
                            	workFlowHandler.getEditingFlow().categories = [categoryKey];
                                //update opened flow's path title
                                var displayTitle = buildDisplayFlowPath(workFlowHandler.getEditingFlow());
//                                var title = buildFlowPath(CurrentFlow);
                                alpine.flow.WorkFlowUIHelper.setEditingFlowLabel(displayTitle);
//                                save_current_flow(title);
                            }
//				    			dijit.byId(constants.TREE_ID).set("selectedItems", []);
                        }
                    }, function(data){
                        showErrorMsg(data);
                    }, constants.WORKBENCH_PANEL);
                });
                if(!newParentNode.isExpanded){
                    dijit.byId(constants.TREE_ID)._onExpandoClick({
                        node: newParentNode
                    });
                }
                this.onDndCancel();
            }
        }, treeContainer);
        tree.startup();
        dojo.connect(tree, "onMouseMove", tree, function(){

        });
        return tree;
    }

    function createTreeMenu(tree){
        var treeMenu = new dijit.Menu({
            id: constants.TREE_MENU_ID
        });
        treeMenu.bindDomNode(tree.domNode);

        dojo.connect(tree.domNode, "oncontextmenu", tree, function(e){
            var tn = dijit.getEnclosingWidget(e.target);
            var treeItem = tn.item;
            var hidemodifyItem = false,// if a item is not a flow in selected items, then hide change menu.
                hideRenameCategory = false,// Allow to selected one record and it would be a category.
                hideRemoveCategory = false;// Same with canRenameCategory

            var items = this.get('selectedItems');

            if(!treeItem && items.length == 0){// no item was selected.
                hidemodifyItem = true;
                hideRenameCategory = true;
                hideRemoveCategory = true;
            }else{
                if(items.length < 2 && treeItem){// ensure right click on item without left click, will be selecte current item. items.length < 2 means switch select or first time select.
                    this.set('selectedItem', treeItem);
                    items = this.get('selectedItems');
                }

                if(items.length > 1 || (items.length == 1 && (!this.mayHaveChildren(items[0]) || toolkit.getValue(items[0].key) == rootKey))){
                    hideRenameCategory = true;
                    hideRemoveCategory = true;
                }
                for(var i = 0;i < items.length;i++){
                    var item = items[i];
                    if(this.mayHaveChildren(item) || toolkit.getValue(item.key) == rootKey){// if a item is category, then cannot be show change category menu.
                        hidemodifyItem = true;
                    }
                }
            }

//            treeMenu.getChildren()[0].set('style', 'display:block');
//
////		   		treeMenu.getChildren()[2].set('style', hideChangeCategory ? 'display:none' : 'display:block');
//            treeMenu.getChildren()[1].set('style', hidemodifyItem ? 'display:none' : 'display:block');
//            treeMenu.getChildren()[2].set('style', hidemodifyItem ? 'display:none' : 'display:block');
//
//            treeMenu.getChildren()[3].set('style', hideRenameCategory ? 'display:none' : 'display:block');
//            treeMenu.getChildren()[4].set('style', hideRemoveCategory ? 'display:none' : 'display:block');
//            treeMenu.getChildren()[5].set('style',  hideRenameCategory ? 'display:none' : 'display:block');
//            treeMenu.getChildren()[6].set('style',  hidemodifyItem ? 'display:none' : 'display:block');
//            treeMenu.getChildren()[7].set('style',  hidemodifyItem ? 'display:none' : 'display:block');
//            treeMenu.getChildren()[8].set('style',  hidemodifyItem ? 'display:none' : 'display:block');
            var removedItem = null;
            while((removedItem = treeMenu.getChildren().pop()) != null){
            	treeMenu.removeChild(removedItem);
            }
            treeMenu.addChild(new dijit.MenuItem({
                //iconClass:"dijitIconUndo",
                label: alpine.nls.flow_category_menu_refresh,
                onClick: function(event){
                    rebuildCategoryTree();
                }
            }));
            if(!hidemodifyItem){
                treeMenu.addChild(new dijit.MenuItem({
                    //iconClass:"dijitIconTable",
                    label: alpine.nls.Flow_History,
                    onClick: function(event){
//        					var treeWidget = dijit.byId(constants.TREE_ID);
//        					var treeItem = treeWidget.get('selectedItems')[0];
//        					renameCategory(treeItem);
                        //show_flow_history();
                        var items = getSelectedFlows();
                        flowVersionHistoryHelper.showFlowHistoryDialogFromTree(items);
                    }
                }));
                treeMenu.addChild(new dijit.MenuItem({
                    //iconClass:"dijitIconDelete",
                    label: alpine.nls.action_delete_open_flow,
                    onClick: function(event){
//        					var treeWidget = dijit.byId(constants.TREE_ID);
//        					var treeItem = treeWidget.get('selectedItems')[0];
//        					renameCategory(treeItem);
                        delete_flow_list('Personal');
                    }
                }));
            }
            if(!hideRenameCategory){
            	treeMenu.addChild(new dijit.MenuItem({
                    //iconClass:"dijitIconClear",
                    label: alpine.nls.flow_category_menu_rename,
                    onClick: function(event){
                        var treeWidget = dijit.byId(constants.TREE_ID);
                        var treeItem = treeWidget.get('selectedItems')[0];
                        renameCategory(treeItem);
                    }
                }));
            }
            if(!hideRemoveCategory){
            	treeMenu.addChild(new dijit.MenuItem({
                    //iconClass:"dijitIconDelete",
                    label: alpine.nls.flow_category_menu_remove,
                    onClick: function(event){
                        var treeWidget = dijit.byId(constants.TREE_ID);
                        var treeItem = treeWidget.get('selectedItems')[0];
                        popupComponent.confirm(alpine.nls.flow_category_message_remove_cagegory, {
                            handle: function(){
                                if(treeItem.subItems){
                                    //remove operator's history
                                    for (var i = 0; i < treeItem.subItems.length; i++) {
                                        var item = toolkit.getValue(treeItem.subItems[i].info);
                                        if (item !== null && item.id[0] != item.key[0]) {
                                            //remove flow opened record from Container of history
                                            recentlyHistoryFlowManager.removeFlowFromHistory(item);
                                        }
                                    }
                                    //if remove list include current opened flow, then close it.
                                    if (workFlowHandler.isEditing()) {
                                        for (var j = 0; j < treeItem.subItems.length; j++) {
//                                            var flowkey = buildFlowPath(toolkit.getValue(treeItem.subItems[j].info));
                                            if (workFlowHandler.isEditing(toolkit.getValue(treeItem.subItems[j].info))) {
                                            	alpine.flow.WorkFlowUIHelper.release();
//                                                dijit.byId("cancel_flow_button").set("disabled", true);
//                                                CurrentFlow = null;
//                                                save_current_flow(null);
                                                workFlowHandler.storeEditingFlow(null);
                                                _removeWorkFlowEditingTrail();
                                                break;
                                            }
                                        }
                                    }
                                }
                                flowCategoryManager.removeCategory(treeItem, function(){
                                    rebuildCategoryTree();
                                }, function(data){
                                    showErrorMsg(data);
                                }, constants.WORKBENCH_PANEL);
                            }
                        });
                    }
                }));
            }
            if(!hideRenameCategory){
            	treeMenu.addChild(new dijit.MenuItem({
                    //iconClass:"dijitIconClear",
                    label: alpine.nls.flow_category_menu_create_flow,
                    onClick: function(event){
//                        var treeWidget = dijit.byId(constants.TREE_ID);
                        var key = alpine.flow.FlowCategoryUIHelper.getFirstSelectedCategory();
                        open_new_flow_dialog(key);
                    }
                }));
            }
            if(!hidemodifyItem){
            	treeMenu.addChild(new dijit.MenuItem({
                    label: alpine.nls.flow_category_menu_rename_flow,
                    onClick: function(event){
                        var treeWidget = dijit.byId(constants.TREE_ID);
                        var treeItem = treeWidget.get('selectedItems')[0];
                        renameFlowFromTree(treeItem);
                    }
                }));
                 //export flow
                treeMenu.addChild(new dijit.MenuItem({
                    // iconClass:"dijitIconClear",
                    label: alpine.nls.export_flow_tip,
                    onClick: function(event){
                        var treeWidget = dijit.byId(constants.TREE_ID);
                        var treeItem = treeWidget.get('selectedItems');

                        var flowInfos = [];
                        for(var j=0;j<treeItem.length;j++){
                            flowInfos.push(tree_item_toFlow_info(treeItem[j].info[0]));
                        }
                        if(isSameNameInFlows(flowInfos)==true){
                            popupComponent.alert(alpine.nls.export_exec_flow_name_same_tip);
                            return false;
                        }

                        open_export_flow_dlg(flowInfos);
                    }
                }));

                //export execute flow
                treeMenu.addChild(new dijit.MenuItem({
                    // iconClass:"dijitIconClear",
                    label: alpine.nls.export_exec_flow_tip,
                    onClick: function(event){
                        var treeWidget = dijit.byId(constants.TREE_ID);
                        var treeItem = treeWidget.get('selectedItems');

                        var flowInfos = [];
                        for(var j=0;j<treeItem.length;j++){
                            flowInfos.push(tree_item_toFlow_info(treeItem[j].info[0]));
                        }
                        if(isSameNameInFlows(flowInfos)==true){
                            popupComponent.alert(alpine.nls.export_exec_flow_name_same_tip);
                            return false;
                        }
                        open_export_exe_flow_dlg(flowInfos);
                    }
                }));
            }

            treeMenu.startup();
            treeMenu._openMyself({
        		target: tree.domNode,
        		coords: {
        			x: e.pageX,
        			y: e.pageY
        		}
        	});
//				dijit.byId("btn_tree_delete_flow").set("disabled",ableToHideButton);
//				dijit.byId("btn_tree_history_flow").set("disabled",ableToHideButton);
        });
        return treeMenu;
    }

    function showErrorMsg(errorData){
        popupComponent.alert(errorData.message);
    }

    function rebuildCategoryTree(){
//		 	dijit.byId("btn_tree_delete_flow").set("disabled",true);
//			dijit.byId("btn_tree_history_flow").set("disabled",true);

        dijit.byId(constants.TREE_ID).destroyRecursive();
        dijit.byId(constants.TREE_MENU_ID).destroyRecursive();

        var treeData = flowCategoryManager.getTreeData(showErrorMsg, constants.WORKBENCH_PANEL);
        createFlowTree(constants.TREE_CONTAINER_ID, constants.TREE_ID, treeData);
        //expandRoot();
    }

//    function expandRoot(){
//        if(loadDefaultWorkFlowIfDefaultExist()){
//            console.log("There is no stored workflow");
//            return;
//        }
//        var firstNode = dijit.byId(constants.TREE_ID).rootNode.getChildren()[0];
//
//        dijit.byId(constants.TREE_ID)._onExpandoClick({
//            node: firstNode
//        });
//    }

    /**
     * invoked until complete invoke server.
     */
    function moveFlowHandler(args){
        var store = dijit.byId(constants.TREE_ID).model.store;
        // remove child from source item, and record the attribute that child occurred in
        store.fetchItemByIdentity({
            identity: args.orignalKey,
            onItem: function(oldParentItem){
                try{

                    var newPreviouschildren = dojo.filter(oldParentItem.subItems, function(x){
                        return x != args.flowInfo;
                    });
                }catch(e){
                    console.log(e);
                }
                try{
                    store.setValues(oldParentItem, "subItems", newPreviouschildren);
                    delete store._itemsByIdentity[toolkit.getValue(args.flowInfo.key)];//As update identity in item. Hard code in here, in order to update object, which key equals with moved's.
                }catch(e){
                    console.log(e);
                }
            }
        });

        //build new flow info, after rebuild original category
        for(var field in args.newFlowInfo){
            args.flowInfo[field] = [args.newFlowInfo[field]];
        }

        // modify target item's children attribute to include this item
        store.fetchItemByIdentity({
            identity: args.targetKey,
            onItem: function(newParentItem){
                var subItems = store.getValues(newParentItem, "subItems");
                var isReplace = false;
                for(var i = 0;i < subItems.length;i++){
                    var item = subItems[i];
                    if(toolkit.getValue(item.path) == toolkit.getValue(args.flowInfo.path)){//if category has same name of flow, need replacement.
                        isReplace = true;
                        var newInfo = toolkit.getValue(args.flowInfo.info);
                        var info = toolkit.getValue(item.info);
                        info.comments = newInfo.comments;
                        info.createTime = newInfo.createTime;
                        info.createUser = newInfo.createUser;
                        info.groupName = newInfo.groupName;
                        info.modifiedTime = newInfo.modifiedTime;
                        info.modifiedUser = newInfo.modifiedUser;
                        info.tmpPath = newInfo.tmpPath;
                        info.type = newInfo.type;
                        info.version = newInfo.version;
                    }
                }
                if(!isReplace){
                    //remove opened history of moved flow.
//						removeFlowFromHistory(toolkit.getValue(args.flowInfo.info));

                    toolkit.getValue(args.flowInfo.info).categories = [args.targetKey];// update flow's category

                    store.setValues(newParentItem, "subItems", store.getValues(newParentItem, "subItems").concat(args.flowInfo));
                    store._itemsByIdentity[toolkit.getValue(args.flowInfo.key)] = args.flowInfo;//Pair with line 443
                }
            }
        });
    }

    /**
     * init and open editor panel.
     */
    function createCategory(){
        guiHelper.showTextFieldEditorDialog(function(categoryName){
            flowCategoryManager.saveCategory({
                name: categoryName,
                parentKey: rootKey
            }, function(){
                rebuildCategoryTree();
            }, function(data){
                showErrorMsg(data);
            }, constants.WORKBENCH_PANEL);
        }, "", catPatrn,alpine.nls.newcat_dialog_title, alpine.nls.newcat_dialog_okbutton);
    }

    function renameCategory(category){
    	//make sure all of flow under category are not editing now
    	 //if remove list include current opened flow, then alert it out and terminate
        if (workFlowHandler.isEditing()) {
            for (var j = 0; j < category.subItems.length; j++) {
                if (workFlowHandler.isEditing(toolkit.getValue(category.subItems[j].info))) {
                    popupComponent.alert(dojo.string.substitute(alpine.nls.flow_category_message_move_opened, {
                        flowName: toolkit.getValue(category.subItems[j].info).id
                    }));
                    return;
                }
            }
        }
        guiHelper.showTextFieldEditorDialog(

            function(categoryName){
            flowCategoryManager.updateCategory(
                {
                key: category.key,
                name: categoryName,
                parentKey: category.parentKey
                },

                function(updatedCategory){
                rebuildCategoryTree();
                //update opened flow path
//                if(workFlowHandler.isEditing() && workFlowHandler.getEditingFlow().categories != undefined && workFlowHandler.getEditingFlow().categories[0] == category.key){
//                    //update opened flow's category
//                    CurrentFlow.categories = [updatedCategory.key];
//                    //update opened flow's path title
////                    var title = buildFlowPath(CurrentFlow);
//                    var displayTitle = buildDisplayFlowPath(CurrentFlow);
//                    alpine.flow.WorkFlowUIHelper.setEditingFlowLabel(displayTitle);
////                    save_current_flow(title);
//                }
                },
                function(data){
                showErrorMsg(data);
                }, constants.WORKBENCH_PANEL);
            }, toolkit.getValue(category.name), catPatrn  , alpine.nls.renamecat_dialog_title,  alpine.nls.renamecat_dialog_okbutton, null);
    }


    function fetchItemByName(name, fn, includeCategory){
        var isIncludeCategory = includeCategory == undefined ? true : includeCategory;//default is true
        var categoryStore = dijit.byId(constants.TREE_ID).model.store;
        var results = new Array();
        categoryStore.fetch({
            query: {
                name: name
            },
            queryOptions: {
                ignoreCase: true,
                deep: true
            },
            onItem: function(item){
                if(!(isIncludeCategory) && item.isCategory != undefined){
                    return;
                }
                results.push(item);
            },
            onComplete: function(){
                fn.call(null, results);
            }
        });
    }

    function buildFlowBasisBean(originalBean){
        return {
            key: toolkit.getValue(originalBean.key),
            name: toolkit.getValue(originalBean.name),
            path: toolkit.getValue(originalBean.path),
            parentKey: toolkit.getValue(originalBean.parentKey),
            info: tree_item_toFlow_info(toolkit.getValue(originalBean.info))
        };
    }

    /**
     * check whether exist same name flow in target category, and confirm.
     */
    function checkDuplicateFlow(targetCategoryKey, moveFlowArray, fn){
        var store = dijit.byId(constants.TREE_ID).model.store;
        var moveFlows = new Array();
        store.fetchItemByIdentity({
            identity: targetCategoryKey,
            onItem: function(targetCategory){
                var targetFlows = store.getValues(targetCategory, "subItems");
                next:
                    for(var i = 0;i < moveFlowArray.length;i++){
                        for(var j = 0;j < targetFlows.length;j++){
                            if(toolkit.getValue(moveFlowArray[i].name) == toolkit.getValue(targetFlows[j].name)){
                                var text = alpine.nls.flow_category_message_move_cover.replace("#", toolkit.getValue(moveFlowArray[i].name)).replace("@", toolkit.getValue(targetCategory.name));
                                if(confirm(text)){
                                    moveFlows.push(moveFlowArray[i]);
                                }
                                continue next;
                            }
                        }
                        moveFlows.push(moveFlowArray[i]);
                    }
                fn.call(null, moveFlows);
            }
        });
    }

    //--------------------------------------Public function---------------------------------------------

    function openSelectedFlow()
    {
        var focused = dijit.byId(constants.TREE_ID).get("lastFocused");
        if (!focused || focused.length ==0)
        {
            popupCompontent.alert(alpine.nls.mobile_msg_open_flow);
        } else
        {
            dijit.byId(constants.TREE_ID).onDblClick(focused.item);
            dijit.byId(constants.TREE_ID).set("selectedItems", [focused.item]);


        }
    }


    function buildDisplayFlowPath(flowInfo)
    {
        var category = flowInfo.categories ? toolkit.getValue(flowInfo.categories) : toolkit.getValue(flowInfo.modifiedUser);
        category = category.replace(/[\\|\/]/, " / ");
        var title = category + "  /  " + toolkit.getValue(flowInfo.id) + " [<a href='#' onclick='alpine.flow.FlowVersionHistoryUIHelper.showFlowHistoryForOpenFlow()'> " + toolkit.getValue(flowInfo.version) + "</a> ]";
        //don't want to show username in the front of the flow: pivotal 30143945
        var index = title.indexOf(" / ") ;
        if (index > -1)
        {
            title = title.substring(index + 3);
        }
        return title.trim();

    }

    function buildDefaultWorkFlowPath(flowInfo){
        if(null==flowInfo){
            console.log("Flow Info is null");
            return '';
        }
        var title = flowInfo.modifiedUser;
        if(flowInfo.categories){
            var category = toolkit.getValue(flowInfo.categories).replace(/[\\|\/]/, "/");
        	title += "|" + category;
        }
        title += "|" + toolkit.getValue(flowInfo.id + flowInfo.createTime);
        return title;
    }

    function buildFlowPath(flowInfo){
        if(null==flowInfo){
            console.log("FlowInfo is null");
            return '';
        }
        var category = flowInfo.categories ? toolkit.getValue(flowInfo.categories) : toolkit.getValue(flowInfo.modifiedUser);
        category = category.replace(/[\\|\/]/, " / ");
        var title = category + "  /  " + toolkit.getValue(flowInfo.id) + " [" + toolkit.getValue(flowInfo.version) + "]";

        return title;
//			var categoryStore = dijit.byId(constants.TREE_ID).model.store;
//			fetchItemByName(toolkit.getValue(flowInfo.id), function(results){
//				if(results.length < 1){
//					return;
//				}
//				categoryStore.fetchItemByIdentity({
//					identity: toolkit.getValue(results[0].parentKey),
//					onItem: function(parent){
//						var title = toolkit.getValue(parent.name) + "  /  " + toolkit.getValue(flowInfo.id) + " [" + toolkit.getValue(flowInfo.version) + "]";
//						fn.call(null, title);
//					}
//				});
//			}, false);
    }

    function getSelectedFlows(){
        var selectedItems = dijit.byId(constants.TREE_ID).get('selectedItems');
        var results = new Array();
        for(var i = 0;i < selectedItems.length;i++){
            if(selectedItems[i].isCategory || toolkit.getValue(selectedItems[i].key) == rootKey){
                continue;//filter category
            }
            results.push(toolkit.getValue(selectedItems[i].info));
        }
        return results;
    }

    function getFirstSelectedCategory()
    {
        var selectedItems = dijit.byId(constants.TREE_ID).get('selectedItems');
        for(var i = 0;i < selectedItems.length;i++){
            if(selectedItems[i].isCategory){
                return selectedItems[i].key;
            }
        }
        return null;
    }

    function fetchFlowInfoByName(name, fn){
        fetchItemByName(name, function(items){
            var results = new Array();
            for(var i = 0;i < items.length;i++){
                results.push(toolkit.getValue(items[i].info));
            }
            fn.call(null, results);
        }, false);
    }

    function setSelectedItem(flowInfo){
        var parentKey = flowInfo.categories == null ? alpine.USER : flowInfo.categories[0];
        parentKey = parentKey.replace(/[\\|\/]/, "/");
        var store = dijit.byId(constants.TREE_ID).model.store;
        store.fetchItemByIdentity({
            identity: parentKey,
            onItem: function(parentItem){
                var subItems = store.getValues(parentItem, "subItems");
                for(var i = 0;i < subItems.length;i++){
                    if(toolkit.getValue(subItems[i].name) == toolkit.getValue(flowInfo.id)){
                        dijit.byId(constants.TREE_ID).set("selectedItems", [subItems[i]]);
                    }
                }
            }
        });
    }
    function getBrothers(flowInfo, fn){
        var parentKey = flowInfo.categories == null ? alpine.USER : flowInfo.categories[0];
        parentKey = parentKey.replace(/[\\|\/]/, "/");
        var store = dijit.byId(constants.TREE_ID).model.store;
        store.fetchItemByIdentity({
            identity: parentKey,
            onItem: function(parentItem){
                var subItems = store.getValues(parentItem, "subItems");
                var childrenArray = new Array();
                for(var i = 0;i < subItems.length;i++){
                    if(subItems[i].isCategory || toolkit.getValue(subItems[i].name) == toolkit.getValue(flowInfo.id)){
                        continue;
                    }
                    childrenArray.push(buildFlowBasisBean(toolkit.getValue(subItems[i])));
                }
                fn.call(null, childrenArray);
            }
        });
    }
    
    function getFlowBasisInfo(flowInfo, fn){
        var parentKey = flowInfo.categories == null ? alpine.USER : flowInfo.categories[0];
        parentKey = parentKey.replace(/[\\|\/]/, "/");
        var store = dijit.byId(constants.TREE_ID).model.store;
        store.fetchItemByIdentity({
            identity: parentKey,
            onItem: function(parentItem){
                var subItems = store.getValues(parentItem, "subItems");
                var flowBasisInfo = null;
                for(var i = 0;i < subItems.length;i++){
                    if(!toolkit.getValue(subItems[i].isCategory) && toolkit.getValue(subItems[i].name) == toolkit.getValue(flowInfo.id)){
                    	flowBasisInfo = buildFlowBasisBean(toolkit.getValue(subItems[i]));
                    	break;
                    }
                }
                fn.call(null, flowBasisInfo);
            }
        });
    }

    return {
        buildFlowPath: buildFlowPath,
        buildDisplayFlowPath: buildDisplayFlowPath,
        rebuildCategoryTree: rebuildCategoryTree,
        getSelectedFlows: getSelectedFlows,
        getFirstSelectedCategory: getFirstSelectedCategory,
        fetchFlowInfoByName: fetchFlowInfoByName,
        setSelectedItem: setSelectedItem,
        getBrothers: getBrothers,
        storeFlowPath:storeFlowPath,
        buildDefaultWorkFlowPath:buildDefaultWorkFlowPath,
        createCategory:createCategory,
        openSelectedFlow:openSelectedFlow,
        removeWorkFlowEditingTrail: _removeWorkFlowEditingTrail
    };
});

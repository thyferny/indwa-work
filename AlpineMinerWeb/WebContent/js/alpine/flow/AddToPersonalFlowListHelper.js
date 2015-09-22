/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * AddToPersonalFlowListHelper.js
 * 
 * Author Will
 * Version 2.7
 * Date 2012-05-21
 */

define([
    "alpine/flow/AddToPersonalFlowListManager",
    "alpine/system/PermissionUtil",
    "alpine/flow/FlowVersionHistoryUIHelper"
],function(addToPersonalFlowMgmt, permissionUtil, flowVersionHistoryHelper){
	dojo.ready(function(){
		//open dailog
		dojo.connect(dijit.byId('button_open_public_group_flow'),'onClick',open_public_group_flow);
		//add open
		dojo.connect(dijit.byId('add_open_button_4public_group_flow'),'onClick',function(){ copy_flow_list(1);});
		//add 
		dojo.connect(dijit.byId('add_button_4public_group_flow'),'onClick',function(){return copy_flow_list(0);});
		//cancel
		dojo.connect(dijit.byId('cancel_button_4public_group_flow'),'onClick',cancel_open_public_gropu_flow_dlg);
	});
	
	//module use variable
//	var CurrentFlowTree = new Array();
	var flowTreeMenu =null;
	var flowTreeMenuHandlers = new Array();
    var copy_and_open_item;
	
	
	function open_public_group_flow(){
		addToPersonalFlowMgmt.load_flow_tree(flow_tree_callback);
	};
	
	function cancel_open_public_gropu_flow_dlg(){
		destroy_flow_tree('Group');
        clear_flow_display('Group');
        dijit.byId('PublicGroupFlowDialog').hide();
	};
	
	
	function _buildTreeModel(obj){
		var store = {identifier : "key",label : "id",items : obj};		
		var flowTreeStore = new dojo.data.ItemFileReadStore({data : store});
		var treeModel = new dijit.tree.ForestStoreModel({
			store : flowTreeStore,
			query : {tag : "TOP" },
			rootId : "root_Id",
			rootLabel : "",
			childrenAttrs : [ "children" ]
		});
		return treeModel;
	}
	
	function _buildTreeWidget(treeModel){
		var treeDomNode =dojo.create("div");
		dojo.place(treeDomNode,"Group","only");
	 	var treeId = "Group_generated_flow_tree_";
		var treeWidget = new dijit.Tree( {
			id : treeId,
			style : "height: 100%;",
			showRoot : false,
			model : treeModel
		},treeDomNode);
		//CurrentFlowTree[type].startup();
		treeWidget.startup();
		return treeWidget;
	}
	
	function _buildTreeItemClickHandler(TREEWEDGET){
		var clickerHandler = dojo.connect(TREEWEDGET, "onClick",   function(item,node,evt){ 
			//MINERWEB-306 Delete and show history should be disabled when there is no flow selected.
			if(item.type != "Personal" ){
				if(!CurrentFlow_Group){
					//refresh the comments,current no flow opened
					if(item.tag[0]!="TOP"&&item.comments&&item.comments[0]){
						dojo.byId("add_flow_comments").value=item.comments[0];
					}else{
						dojo.byId("add_flow_comments").value=" ";
					}
					if(item.tag[0]!="TOP"&&item.version&&item.version[0]){
						dojo.byId("add_flow_version").innerHTML=item.version[0];
					}else{
						dojo.byId("add_flow_version").innerHTML="";
					}
					 
					
					if(item.tag[0]!="TOP"&&item.modifiedUser&&item.modifiedUser[0]){
						dojo.byId("add_flow_publisher").innerHTML=item.modifiedUser[0];
					}else{
						dojo.byId("add_flow_publisher").innerHTML="";
					}
				 
					if(item.tag[0]!="TOP"&&item.modifiedTime&&item.modifiedTime[0]){
						var dateTime = new Date(item.modifiedTime[0]) ;//from time mills
//						dateTime=dateTime.toLocaleString();
						dateTime=alpine_format_date(dateTime);
						dojo.byId("add_flow_publish_time").innerHTML=dateTime;
					}else{
						dojo.byId("add_flow_publish_time").innerHTML="";
					}
					 
				}
			}
		});
		return clickerHandler;
	}
	
	function _buildTreeItemDBClickHandler(TREEWEDGET,type){
		var dbClickerHandler = dojo.connect(TREEWEDGET, "onDblClick",  function(item) {
			if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
				return null;
			}
			var flow = tree_item_toFlow_info(item);
//			if(item.type == "Personal"){
//				if(is_flow_loaded(alpine.flow.FlowCategoryUIHelper.buildFlowPath(item))){
//					return null;
//				}
//			}
			open_flow(type, flow);
		});
		
		return dbClickerHandler;
	}
	
	function _buildTreeMenu4AddToPersonalFlow(TREEWEDGET){
			var  flowTreeMenu = new dijit.Menu({
				targetNodeIds : [TREEWEDGET.id]
			});
			if(permissionUtil.checkPermission("EDIT_DATASOURCE_TO_PUBLIC")){ 
				flowTreeMenu.addChild(new dijit.MenuItem({
					label : alpine.nls.delete_flow_tip,
					onClick : function() {
						delete_flow_list("Group");
					}
				}));
			}
			flowTreeMenu.addChild(new dijit.MenuItem({
				label : alpine.nls.open_flow_tip,
				onClick : function(evt) {
				var items=CurrentFlowTree["Group"].selectedItems;
		 		
				if(items&&items.length>0){
					var item = items[0];
					if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
						return;
					}
					var flow = tree_item_toFlow_info(item);
	 
					open_flow("Group", flow);
				}
				}
			}));
			
			flowTreeMenu.addChild(new dijit.MenuItem({
				label : alpine.nls.open_flow_history_tip,
				onClick : function(evt) {
				var items=CurrentFlowTree["Group"].selectedItems;
		 		
				if(items&&items.length>0){
					var item = items[0];
					if(item.id[0] == "root_Id" || item.id[0] == item.key[0]) {
						return;
					}
					var flow = tree_item_toFlow_info(item);

                    flowVersionHistoryHelper.open_flow_history_dlg_foradd_for_addtopersonalflow([flow]);
				}
				}
			}));
			
			

			
			var flowTreeMenuHandler= dojo.connect(flowTreeMenu, "_openMyself", TREEWEDGET, function(e) {
				
				 var tn = dijit.getEnclosingWidget(e.target);
	 
		    	 //diable and enable menu
		    	 if(!tn||!tn.item||tn.item.children){//fix bug with IE, which bug name is MINERWEB-330 in JIRA
		      		 //if can enter here, must be click right click with any node
		      		 if(permissionUtil.checkPermission("EDIT_DATASOURCE_TO_PUBLIC")){
		      			flowTreeMenu.getChildren()[0].set('disabled', true);
		      			flowTreeMenu.getChildren()[1].set('disabled', true);
		      			flowTreeMenu.getChildren()[2].set('disabled', true);
		        		 
		      		 }else{
		      			flowTreeMenu.getChildren()[0].set('disabled', true);
		      			flowTreeMenu.getChildren()[1].set('disabled', true);
		      		 }
		      		return;
		      	 }
		    	 
		    	//make select
				 var items= this.get('selectedItems');
		    	 if(!items|| items.length==0){
		    		 this.set('selectedItem', tn.item);
		    	 }else{
		    		 if(dojo.indexOf(items, tn.item) == -1){
		    			 this.set('selectedItem', tn.item);
		    		 }
		    	 }
		    	 items= this.get('selectedItems');
		    	 
		    	 if(permissionUtil.checkPermission("EDIT_DATASOURCE_TO_PUBLIC")){
		    		 flowTreeMenu.getChildren()[0].set('disabled', false);
		    		 if(items.length>1){
		    		 flowTreeMenu.getChildren()[1].set('disabled', true);
		      			flowTreeMenu.getChildren()[2].set('disabled', true);
		    		 }else{
		      			flowTreeMenu.getChildren()[1].set('disabled', false);
		      			flowTreeMenu.getChildren()[2].set('disabled', false);
		      			}
		    	 }
		    	 else{
	    		 	if(items.length>1){
						flowTreeMenu.getChildren()[1].set('disabled', true);
						flowTreeMenu.getChildren()[0].set('disabled', true);
					 }else{
						flowTreeMenu.getChildren()[1].set('disabled', false);
						flowTreeMenu.getChildren()[0].set('disabled', false);
					}
		    	 }
		       	 
				 
			 });
			flowTreeMenuHandlers.push(flowTreeMenuHandler) ;
			 flowTreeMenu.startup();
	
	}
	
	function _showDialog(){
		CurrentFlow_Group=null;

		//init the dialog...
		dijit.byId("PublicGroupFlowDialog").titleBar.style.display = "none";
		dijit.byId("PublicGroupFlowDialog").show();
		dojo.byId("add_flow_comments").value="";
		
		dojo.byId("add_flow_version").innerHTML="";
		dojo.byId("add_flow_publisher").innerHTML="";
		dojo.byId("add_flow_publish_time").innerHTML="";
		
		if (dojo.isIE){
			dojo.byId("add_flow_comments").rows=2;
			dojo.byId("add_flow_comments").cols=40;
		}
	}
	
	function flow_tree_callback(obj) {
		var type = "Group";
		var flowTreeMenuHandlers =[];
		//obj is list of flow info
		if (obj.error_code ) {
			handle_error_result(obj) ; //in common.js
			return ;
		}
	    
		var treeModel = _buildTreeModel(obj);
		//make sure it is ok...
		destroy_flow_tree(type) ;
	 	
		CurrentFlowTree[type] = _buildTreeWidget(treeModel);
		
		flowTreeMenuHandlers.push(_buildTreeItemClickHandler(CurrentFlowTree[type]));
		
		var  dbClickerHandler = _buildTreeItemDBClickHandler(CurrentFlowTree[type],type);
		
		if(null!=dbClickerHandler){
			flowTreeMenuHandlers.push(dbClickerHandler) ;
		}else{
			return ;
		}
		
		_buildTreeMenu4AddToPersonalFlow(CurrentFlowTree[type]);
		
		//this is for file upload to get the flow name
	
		_showDialog();
		
	};
	
	function destroy_flow_tree( type) {
		var treeId = type + "_generated_flow_tree_";
		var t = dijit.byId(treeId);
		if (t) {
			dijit.registry.remove(treeId);
			t.destroyRecursive();
		 
			if(type=="Group"){
				flowTreeMenu= null;
			}
			
			if(flowTreeMenuHandlers!=null){
				for(var i=0;i<flowTreeMenuHandlers.length;i++){
					if(flowTreeMenuHandlers[i]!=null){
						dojo.disconnect(flowTreeMenuHandlers[i]);
						flowTreeMenuHandlers[i]=null;
					}
				}
				flowTreeMenuHandlers=new Array();
			}
		}
		
		var parent = dojo.byId(type);
		if (parent && parent.firstChild){		
			parent.removeChild(parent.firstChild);
			parent.innerHTML="";
		}	
	};
	function copy_flow_list(type) {
		if (type != 0) {//add and open
			if (alpine.flow.WorkFlowManager.isDirty()==true){
				popupComponent.saveConfirm(alpine.nls.update_not_saved,{
					handle: function(){
						var save_flow_addition_callback = function(){
							copyFlowListHandle(type);
						};
                        alpine.flow.WorkFlowUIHelper.saveWorkFlow(save_flow_addition_callback);
						//save_flow();
					}
				},{
					handle: function(){
                        alpine.flow.WorkFlowUIHelper.release();
                        copyFlowListHandle(type);
					}
				});
			}else{
				copyFlowListHandle(type);

			}
		}else{
			copyFlowListHandle(type);
		}
		
}
	
	function copyFlowListHandle(type){
		var copy_flow_list = new Array(); 
		var idx = 0;
		var items = CurrentFlowTree["Group"].selectedItems;
		if (items.length) {
			for (var i = 0; i < items.length; i++) {
				var item = items[i];				
				if (item !== null && item.id[0] != item.key[0]) {
					
					copy_flow_list[idx] = tree_item_toFlow_info(item);
					//get the opened version
					//added check whether CurrentFlow_Group is null or not, to fix JIRA MINERWEB-568
					if(CurrentFlow_Group && copy_flow_list[idx].key==CurrentFlow_Group.key){
						copy_flow_list[idx].version = CurrentAddingFlow_Version;
					}
					copy_flow_list[idx].comments =dojo.byId("add_flow_comments").value;
					if(!copy_flow_list[idx].comments||copy_flow_list[idx].comments==""){
						copy_flow_list[idx].comments= " ";
					}
					idx=idx+1;
					
				}				
			}				
		}
			
		if (copy_flow_list.length > 0) {
			var canContinue = true;
			
			error_msg = alpine.nls.copy_flow_error;
			
			for(var i =0 ;i<copy_flow_list.length;i++){
				if (alpine.flow.WorkFlowManager.isEditing(copy_flow_list[i])){
					if(alpine.flow.WorkFlowManager.isDirty()==true){
						canContinue = false;
						popupComponent.saveConfirm(alpine.nls.update_not_saved,{
							handle: function(){
								var saveFlowCallback = function(){
									alpine.flow.WorkFlowUIHelper.release();
									
									//copied flow is current flow with edit, then replace latest version to screen.
                                    copy_and_open_item = null;
                                    var flow = copy_flow_list[0];
                                    flow.type = "Personal";
                                    flow.modifiedUser = login;
                                    copy_and_open_item = flow;
                                    addToPersonalFlowMgmt.copyFlowListHandle(type,copy_flow_list, copy_flow_list_callback, error_callback);
                                    //ds.post(url, copy_flow_list, copy_flow_list_callback, error_callback);

//									CurrentFlow = null;
								};
								//save_flow();
                                alpine.flow.WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
							}
						},{
							handle: function(){
								//if abort current flow with edit, just clean screen.
								alpine.flow.WorkFlowUIHelper.release();
                                copy_and_open_item = null;
                                var flow = copy_flow_list[0];
                                flow.type = "Personal";
                                flow.modifiedUser = login;
                                copy_and_open_item = flow;
                                addToPersonalFlowMgmt.copyFlowListHandle(type,copy_flow_list, copy_flow_list_callback, error_callback);
								//ds.post(url, copy_flow_list, copy_flow_list_callback, error_callback);
							}
						});
					}else{
						alpine.flow.WorkFlowUIHelper.release();
					}
				}
			}
			if(canContinue){
				addToPersonalFlowMgmt.copyFlowListHandle(type,copy_flow_list,function (obj){
					copy_and_open_item = null;
					if (type != 0) {
						var flow = copy_flow_list[copy_flow_list.length -1];
						flow.type = "Personal";
						flow.modifiedUser = login;
						copy_and_open_item = flow;	
//						CurrentFlow = null;				
					}	
					copy_flow_list_callback(obj);
				}, error_callback);
			}
		}	
	}

	function copy_flow_list_callback(obj) {
		if (obj.error_code&&obj.error_code!=0) {
			if (obj.error_code == -1) {
				popupComponent.alert(alpine.nls.no_login, function(){
					window.top.location.pathname = loginURL;	
				});
				return;
			}
			else if (obj.error_code == 3) {
				popupComponent.alert(alpine.nls.flow_exist_error);
				return;
			}		
			else {
				var msg = alpine.nls.flow_not_found;
				if (obj.message) {
					msg = obj.message;
				}
				popupComponent.alert(msg);
				return;
			}			
		}
		clear_flow_display("Group");
		destroy_flow_tree('Group');
		dijit.byId('PublicGroupFlowDialog').hide();
	 
		
		if (copy_and_open_item != null) { 
			if(obj.newFlowVersion){
				copy_and_open_item.version = obj.newFlowVersion;
			}
			//open the crroct version ...
			alpine.flow.WorkFlowUIHelper.release();
			alpine.flow.WorkFlowUIHelper.openWorkFlow(copy_and_open_item);
//			open_flow("Personal", copy_and_open_item);
			 
		}
		window.setTimeout(alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree, 2000);
	}
	
	// delete flow list
	function delete_flow_list(type) {
		var items = type == "Personal" ? alpine.flow.FlowCategoryUIHelper.getSelectedFlows() : CurrentFlowTree[type].selectedItems;
		popupComponent.confirm(alpine.nls.delete_flow_confirm_tip, {
			handle: function(){
				var flow_list = new Array();
				var idx = 0;
			
				if (items.length) {
					for (var i = 0; i < items.length; i++) {
						var item = items[i];				
						if (item !== null && item.id[0] != item.key[0]) {
							flow_list[idx++] = tree_item_toFlow_info(item);
							//remove flow opened record from Container of history
//							alpine.flow.RecentlyHistoryFlowManager.removeFlowFromHistory(item);
						}				
					}				
				}
				if (flow_list.length) {
					var callback = null;
//					if (type == "Personal") {
////					 	dijit.byId("btn_tree_delete_flow").set("disabled",true);
////						dijit.byId("btn_tree_history_flow").set("disabled",true);
////						callback = flow_tree_personal_cb;
//						
//						callback = alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree;
//					}
//					else {
//						callback = flow_tree_group_cb;
//					}
					callback = flow_tree_group_cb;
					/*
					  var url = flowBaseURL + "?method=deleteFlowList"
						+ "&user=" + login
						+ "&type=" + type;
					*/
					error_msg = alpine.nls.delete_flow_error;
					//
					addToPersonalFlowMgmt.delete_flow_list(type,flow_list, callback, error_callback);
					//ds.post(url, flow_list, callback, error_callback);
//					if (type == "Personal" && CurrentFlow) {
//						for (var j = 0; j < flow_list.length; j++) {
//							if (flow_list[j].id == CurrentFlow.id) {
//								clear_flow_display("Personal");
//								dijit.byId("cancel_flow_button").set("disabled", true);
//								
//								CurrentFlow = null;
//								save_current_flow(null);
//								break;
//							}
//						}
//					}
				}	
			}
		});	
	}
	return {
		destroy_flow_tree: destroy_flow_tree
	};
});
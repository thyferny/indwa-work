/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * HadoopConnUIHelper
 * Author Gary
 */
define(["alpine/datasourcemgr/DataSourceManager", 
        "alpine/datasourcemgr/DataSourceHadoopUIHelper", 
        "alpine/datasourcemgr/DataSourceDBUIHelper",
        "alpine/system/PermissionUtil"], function(persistence, hadoopHelper, dbHelper, permissionUtil){
	var handlers = [dbHelper, hadoopHelper];
	
	var constants = {
		DIALOG_ID: "alpine_datasource_config_Dialog",
		CONFIG_CONTAINER: "alpine_datasource_config_container",
		PUBLIC_SECTION_ID: "alpine_datasource_config_public",
		GROUP_SECTION_ID: "alpine_datasource_config_group",
		PERSONAL_SECTION_ID: "alpine_datasource_config_personal",
		PUBLIC_SECTION_TREE_ID: "alpine_datasource_config_public_tree",
		GROUP_SECTION_TREE_ID: "alpine_datasource_config_group_tree",
		PERSONAL_SECTION_TREE_ID: "alpine_datasource_config_personal_tree",
		
		MENU_ID: "datasourceConnections_button",
		
		EDITOR_DIALOG_ID: "alpine_datasource_config_editor_Dialog",
		EDITOR_MAIN_CONTENT: "alpine_datasource_config_editor_mainContainer",
		
		EDITOR_SWITCHER: "alpine_datasource_config_editor_datasourceType",
		EDITOR_CONTENT_CONTAINER: "alpine_datasource_config_editor_contentSwitcher",
		EDITOR_DB_CONTENT: "alpine_datasource_config_editor_db_content",
		EDITOR_HADOOP_CONTENT: "alpine_datasource_config_editor_hadoop_content",

		EDITOR_INPUT_TYPE_ID: "alpine_datasource_config_editor_type",
		EDITOR_INPUT_GROUP_ID: "alpine_datasource_config_editor_group",
		EDITOR_INPUT_CONN_NAME: "alpine_datasource_config_editor_connection_name",
		
		EDITOR_INPUT_CREATE_USER_ID: "alpine_datasource_config_editor_createUser",
		
		EDITOR_BUTTON_TEST_ID: "alpine_datasource_config_editor_button_test",	
		EDITOR_BUTTON_SAVE_ID: "alpine_datasource_config_editor_button_save",	
		EDITOR_BUTTON_CANCEL_ID: "alpine_datasource_config_editor_button_cancel",
		
		CREATE_BUTTON_ID: "alpine_datasource_config_button_create",
		UPDATE_BUTTON_ID: "alpine_datasource_config_button_update",
		DELETE_BUTTON_ID: "alpine_datasource_config_button_delete",
		DUPLICATE_BUTTON_ID: "alpine_datasource_config_button_duplicate",
		CLOSE_BUTTON_ID: "alpine_datasource_config_button_close"
	};
	
	
	var editorEventHandler = [];
	var currentSection;
	var categoryTreeMapping = {
		Public: {
			treeID: constants.PUBLIC_SECTION_TREE_ID,
			sectionID: constants.PUBLIC_SECTION_ID
		},
		Group: {
			treeID: constants.GROUP_SECTION_TREE_ID,
			sectionID: constants.GROUP_SECTION_ID
		},
		Personal: {
			treeID: constants.PERSONAL_SECTION_TREE_ID,
			sectionID: constants.PERSONAL_SECTION_ID
		}
	};
	
	function getEditorHandler(dataSourceType){
		if(dataSourceType.toUpperCase() == "HADOOP"){
			return hadoopHelper;
		}else{
			return dbHelper;
		}
	}
	
	function startup(){
		//default open personal panel.
		dijit.byId(constants.DIALOG_ID).titleBar.style.display = "none";
		currentSection = "Personal";
		dijit.byId(constants.CONFIG_CONTAINER).selectChild(dijit.byId(categoryTreeMapping.Personal.sectionID));
		resetMgrButtons();
		
		dijit.byId(constants.DIALOG_ID).show();
		var categories = persistence.loadAllCategories(constants.CONFIG_CONTAINER);
		for(var type in categoryTreeMapping){
			createTree(categoryTreeMapping[type].sectionID, categoryTreeMapping[type].treeID, categories[type]);
		}
		var groups = persistence.getGroups(constants.CONFIG_CONTAINER);
		var groupSel = dijit.byId(constants.EDITOR_INPUT_GROUP_ID);
		groupSel.removeOption(groupSel.getOptions());
		dojo.forEach(groups,function(item){
			groupSel.addOption({
				label: item,
				value: item
			});
		});	
	}
	
	function release(){
		for(var type in categoryTreeMapping){
			dijit.byId(categoryTreeMapping[type].treeID).destroyRecursive();	
		}
	}
	
	function resetMgrButtons(){
		dijit.byId(constants.UPDATE_BUTTON_ID).set("disabled", true);
		dijit.byId(constants.DUPLICATE_BUTTON_ID).set("disabled", true);
		dijit.byId(constants.DELETE_BUTTON_ID).set("disabled", true);
	}
	
	function showEditor(fillFn, submitFn){
        dijit.byId(constants.EDITOR_DIALOG_ID).titleBar.style.display = "none";
		dijit.byId(constants.EDITOR_DIALOG_ID).show();
		editorEventHandler.push(dojo.connect(dijit.byId(constants.EDITOR_INPUT_TYPE_ID), "onChange", function(val){
			var groupSel = dijit.byId(constants.EDITOR_INPUT_GROUP_ID);
			if(val == "Group"){
				groupSel.set("disabled", false);
			}else{
				groupSel.set("disabled", true);
			}
		}));
		editorEventHandler.push(dojo.connect(dijit.byId(constants.EDITOR_BUTTON_SAVE_ID), "onClick", submitFn));
		fillFn.call(null);
		_changeEditDialog(dijit.byId(constants.EDITOR_SWITCHER).get("value"));
	}
	
	function releaseEditor(){
		var item = null;
		while((item = editorEventHandler.pop()) != undefined){
			dojo.disconnect(item);
		}
	}
	
	function resetInputbox(){
		var switcher = dijit.byId(constants.EDITOR_SWITCHER);
		switcher.set("disabled", false);
		switcher.reset();
		dijit.byId(constants.EDITOR_INPUT_CONN_NAME).set("disabled", false);
		dijit.byId(constants.EDITOR_INPUT_CONN_NAME).reset();
		for(var i = 0;i < handlers.length; i++){
			handlers[i].resetEditorWidgets.call();
		}
	}
	
	function addConnFillHandler(){
		resetInputbox();
		changeInputBoxByRole();
	}
	
	function changeInputBoxByRole(){
		if(!permissionUtil.checkPermission("EDIT_DATASOURCE_TO_PUBLIC")){
			dijit.byId(constants.EDITOR_INPUT_TYPE_ID).set("value", "Personal");
			dijit.byId(constants.EDITOR_INPUT_TYPE_ID).set("disabled", true);
			dijit.byId(constants.EDITOR_INPUT_GROUP_ID).set("disabled", true);
		}else{
			dijit.byId(constants.EDITOR_INPUT_TYPE_ID).set("value", currentSection);
			//admin able to create or duplicate connection to everywhere
			dijit.byId(constants.EDITOR_INPUT_TYPE_ID).set("disabled", false);
		}
	}
	
	function updateConnFillHandler(){
		var selectedItems = dijit.byId(categoryTreeMapping[currentSection].treeID).get('selectedItems');
		if(!selectedItems || selectedItems.length == 0){
			return;
		}
		var connectionConfig = getEditorHandler(getVal(selectedItems[0].configType)).loadDataSourceConfig(getVal(selectedItems[0].key), constants.EDITOR_DIALOG_ID);
		
		dijit.byId(constants.EDITOR_INPUT_TYPE_ID).set("disabled", true);
		dijit.byId(constants.EDITOR_INPUT_GROUP_ID).set("disabled", true);
		dijit.byId(constants.EDITOR_INPUT_CONN_NAME).set("disabled", true);
		fillFieldToEditor(connectionConfig);
	}
	
	function fillFieldToEditor(connectionConfig){
		dijit.byId(constants.EDITOR_SWITCHER).set("disabled", true);
		dijit.byId(constants.EDITOR_SWITCHER).set("value", connectionConfig.datasourceType);
		dijit.byId(constants.EDITOR_INPUT_TYPE_ID).set("value", connectionConfig.type);
		dijit.byId(constants.EDITOR_INPUT_GROUP_ID).set("value", connectionConfig.groupName);
		dojo.byId(constants.EDITOR_INPUT_CREATE_USER_ID).value = connectionConfig.createUser;
		dijit.byId(constants.EDITOR_INPUT_CONN_NAME).set("value", connectionConfig.connection.connName);
		getEditorHandler(connectionConfig.datasourceType).fillDataToWidgets.call(null, connectionConfig);
	}
	
	function duplicateFillHandler(){
		var selectedItems = dijit.byId(categoryTreeMapping[currentSection].treeID).get('selectedItems');
		if(!selectedItems || selectedItems.length == 0){
			return;
		}
		var connectionConfig = getEditorHandler(getVal(selectedItems[0].configType)).loadDataSourceConfig(getVal(selectedItems[0].key), constants.EDITOR_DIALOG_ID);
		dijit.byId(constants.EDITOR_INPUT_CONN_NAME).set("disabled", false);
		fillFieldToEditor(connectionConfig);
		changeInputBoxByRole();
	}
	
	function createTree(treeContainerID, treeID, rootData){
		var resourceStore = new dojo.data.ItemFileWriteStore({
			data: {
				identifier: "key",// refer to ResourceItem
				label: "label",
				items: rootData.subItems
			}
		});
		var treeContainer = dojo.create("div", {}, treeContainerID);
		var tree = new dijit.Tree({
			id: treeID,
			showRoot: false,
			style : "height: 100%;",
			openOnDblClick: true,
			persist: false,
//	        autoExpand: true,
			model: new dijit.tree.ForestStoreModel({
		        store: resourceStore,
				childrenAttrs : ["subItems"],
		        query: {
		            "key": "*"
		        }
		    }),
		    getIconClass: function(item, opened){
	        	if(getVal(item.isCategory)){
	            	return opened ? "dijitIconFolderOpen" : "dijitIconFolderClosed";
	        	}else{
	            	return "dijitIconFile";
	        	}
	        },
//		    dndController: "dijit.tree.dndSource",
		    mayHaveChildren: function(treeItem){
				return getVal(treeItem.isCategory);
			},
			onClick: function(treeItem){
				if((currentSection != "Personal" && !permissionUtil.checkPermission("EDIT_DATASOURCE_TO_PUBLIC")) || this.mayHaveChildren(treeItem)){
					dijit.byId(constants.DELETE_BUTTON_ID).set("disabled", true);
				}else{
					dijit.byId(constants.DELETE_BUTTON_ID).set("disabled", false);
				}
				
				if(this.get('selectedItems').length != 1 || (currentSection != "Personal" && !permissionUtil.checkPermission("EDIT_DATASOURCE_TO_PUBLIC")) || this.mayHaveChildren(treeItem)){
					dijit.byId(constants.UPDATE_BUTTON_ID).set("disabled", true);
					
				}else{
					dijit.byId(constants.UPDATE_BUTTON_ID).set("disabled", false);
				}
				
				if(this.get('selectedItems').length != 1 || this.mayHaveChildren(treeItem)){
					dijit.byId(constants.DUPLICATE_BUTTON_ID).set("disabled", true);
				}else{
					dijit.byId(constants.DUPLICATE_BUTTON_ID).set("disabled", false);
				}
			}
		}, treeContainer);
		tree.startup();	
		return tree;
	}
	
	function buildResourceInfo(){
		var configInfo = getEditorHandler(dijit.byId(constants.EDITOR_SWITCHER).get("value")).buildConfigInfo();
		configInfo.connName = dijit.byId(constants.EDITOR_INPUT_CONN_NAME).get("value");
		return {
			id: configInfo.connName,
			type: dijit.byId(constants.EDITOR_INPUT_TYPE_ID).get("value"),
			connection: configInfo,
			groupName: dijit.byId(constants.EDITOR_INPUT_GROUP_ID).get("value"),
			createUser: dojo.byId(constants.EDITOR_INPUT_CREATE_USER_ID).value
		};
	}
	
	function saveConnSubmitHandler(){
		if(!validateEditFileds()){
			return;
		}
		var invoker = getEditorHandler(dijit.byId(constants.EDITOR_SWITCHER).get("value"));
		var connResourceInfo = buildResourceInfo();
		invoker.saveResourceInfo(connResourceInfo, function(data){
			if(data.error_code){
				popupComponent.alert(data.message);
			}else{
				dijit.byId(constants.EDITOR_DIALOG_ID).hide();
				rebuildTree(connResourceInfo.type);
			}
		}, constants.EDITOR_DIALOG_ID);
	}
	
	function updateConnSubmitHandler(){
		if(!validateEditFileds()){
			return;
		}
		var invoker = getEditorHandler(dijit.byId(constants.EDITOR_SWITCHER).get("value"));
		var connResourceInfo = buildResourceInfo();
		invoker.updateResourceInfo(connResourceInfo, function(data){
			dijit.byId(constants.EDITOR_DIALOG_ID).hide();
		}, constants.EDITOR_DIALOG_ID);
	}
	
	function deleteConfigInfo(){
		var selectedItems = dijit.byId(categoryTreeMapping[currentSection].treeID).get('selectedItems');
		if(!selectedItems || selectedItems.length == 0){
			return;
		}
		popupComponent.confirm(alpine.nls.datasource_config_msg_delete, {
			handle: function(){
				persistence.removeConfigInfo(selectedItems, function(){
					resetMgrButtons();
					rebuildTree(currentSection);
				},constants.CONFIG_CONTAINER);
			}
		});
	}
	
	function rebuildTree(type){
		var mapping = categoryTreeMapping[type];
		var categories = persistence.loadAllCategories(constants.CONFIG_CONTAINER);
		dijit.byId(mapping.treeID).destroyRecursive();
		createTree(mapping.sectionID, mapping.treeID, categories[type]);
		dijit.byId(constants.CONFIG_CONTAINER).selectChild(dijit.byId(mapping.sectionID));	
	}
	
	function getVal(val){
		return toolkit.getValue(val);
	}
	
	function resetSectionItems(sectionID){
		var tree = dijit.byId(categoryTreeMapping[sectionID].treeID);
		if(tree == null){
			return;
		}
		resetMgrButtons();
		tree.set('selectedItems', []);
	}
	
	function validateEditFileds(){
		dijit.byId(constants.EDITOR_INPUT_CONN_NAME)._hasBeenBlurred = true;
		var isValidate = dijit.byId(constants.EDITOR_INPUT_CONN_NAME).validate();
		isValidate &= getEditorHandler(dijit.byId(constants.EDITOR_SWITCHER).get("value")).validate();
		return isValidate;
	}
	
	function _changeEditDialog(value){
		var dialogSize = {
			w: 350
		};
		var height = 350;
		switch(value){
		case "HADOOP": 
			dijit.byId(constants.EDITOR_CONTENT_CONTAINER).selectChild(constants.EDITOR_HADOOP_CONTENT);
			if(alpine.hadoopVersion == "Apache Hadoop 1.0.2" 
				|| alpine.hadoopVersion == "Apache Hadoop 1.0.4"){
				height = 490;
			}
			break;
		case "DATABASE": 
			dijit.byId(constants.EDITOR_CONTENT_CONTAINER).selectChild(constants.EDITOR_DB_CONTENT);
			height = 350;
			break;
		}
		dialogSize.h = height;
		dijit.byId(constants.EDITOR_MAIN_CONTENT).resize(dialogSize);
	}
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.MENU_ID), "onClick", function(){
			startup();
		});
		
		dojo.connect(dijit.byId(constants.CLOSE_BUTTON_ID), "onClick", function(){
			dijit.byId(constants.DIALOG_ID).hide();
		});
		dojo.connect(dijit.byId(constants.CREATE_BUTTON_ID), "onClick", function(){
			showEditor.call(null, addConnFillHandler, saveConnSubmitHandler);
		});
		dojo.connect(dijit.byId(constants.UPDATE_BUTTON_ID), "onClick", function(){
			showEditor.call(null, updateConnFillHandler, updateConnSubmitHandler);
		});
		dojo.connect(dijit.byId(constants.DELETE_BUTTON_ID), "onClick", deleteConfigInfo);
		dojo.connect(dijit.byId(constants.DUPLICATE_BUTTON_ID), "onClick", function(){
			showEditor.call(null, duplicateFillHandler, saveConnSubmitHandler);
		});
		

		dojo.connect(dijit.byId(constants.EDITOR_BUTTON_CANCEL_ID), "onClick", function(){
			dijit.byId(constants.EDITOR_DIALOG_ID).hide();
		});
		dojo.connect(dijit.byId(constants.EDITOR_BUTTON_TEST_ID), "onClick", function(){
			if(!validateEditFileds()){
				return;
			}
			getEditorHandler(dijit.byId(constants.EDITOR_SWITCHER).get("value")).testConnect();
		});
		dojo.connect(dijit.byId(constants.EDITOR_DIALOG_ID), "onHide", releaseEditor);
		dojo.connect(dijit.byId(constants.DIALOG_ID), "onHide", release);
		
		dojo.connect(dijit.byId(constants.PUBLIC_SECTION_ID), "onSelected", function(){
			currentSection = "Public";
		});
		dojo.connect(dijit.byId(constants.PUBLIC_SECTION_ID), "onHide", function(){
			resetSectionItems(currentSection);
		});
		
		dojo.connect(dijit.byId(constants.GROUP_SECTION_ID), "onSelected", function(){
			currentSection = "Group";
		});
		dojo.connect(dijit.byId(constants.GROUP_SECTION_ID), "onHide", function(){
			resetSectionItems(currentSection);
		});
		
		dojo.connect(dijit.byId(constants.PERSONAL_SECTION_ID), "onSelected", function(){
			currentSection = "Personal";
		});
		dojo.connect(dijit.byId(constants.PERSONAL_SECTION_ID), "onHide", function(){
			resetSectionItems(currentSection);
		});
		
		
		dojo.connect(dijit.byId(constants.EDITOR_SWITCHER), "onChange", function(val){
			_changeEditDialog(val);
		});
	});
});

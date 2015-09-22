define(["alpine/props/HadoopCommonPropertyManager"], function(remoteHandler){

	var constants = {
		HADOOP_FILE_EXPLORER_TREE_CONTAINER: "alpine_props_hadoopcommonproperty_fileexplorer_tree_container",
		HADOOP_FILE_EXPLORER_TREE_ID: "alpine_props_hadoopcommonproperty_fileexplorer_tree",
		HADOOP_FILE_EXPLORER_BUTTON_OK: "alpine_props_hadoopcommonproperty_fileexplorer_submit",
		HADOOP_FILE_EXPLORER_DIALOG: "alpine_props_hadoopcommonproperty_fileexplorer_dialog"
	};
	var eventHandlers = [];
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.HADOOP_FILE_EXPLORER_DIALOG), "onHide", function(){
			var event = null;
			while((event = eventHandlers.pop()) != undefined){
				dojo.disconnect(event);
			}
		});
	});
	
	function _startup(connectionKey, fn, /*boolean*/includeFile){
		if(dijit.byId(constants.HADOOP_FILE_EXPLORER_TREE_ID)){
			dijit.byId(constants.HADOOP_FILE_EXPLORER_TREE_ID).destroyRecursive();
		}
		var rootFileItems = remoteHandler.getChildHadoopFileByPath(connectionKey, includeFile, null);
        var resourceStore = new dojo.data.ItemFileWriteStore({
            data: {
                identifier: "key",
                label: "name",
                items: rootFileItems
            }
        });
        dijit.byId(constants.HADOOP_FILE_EXPLORER_DIALOG).titleBar.style.display = "none";
        dijit.byId(constants.HADOOP_FILE_EXPLORER_DIALOG).show();
		var hadoopFileTree = new dijit.Tree({
            showRoot: false,
            style : "height: 100%;",
            id: constants.HADOOP_FILE_EXPLORER_TREE_ID,
            openOnDblClick: true,
            persist: false,
            model: new dijit.tree.ForestStoreModel({
                store: resourceStore,
                query: {
                    "key": "*"
                },
                mayHaveChildren: function(item){
                   return toolkit.getValue(item.isDir);
                },
                getIdentity: function(item){
                    return toolkit.getValue(item.key);
                }
            }),
            getIconClass: function(item, opened){
               if(toolkit.getValue(item.isDir) == true){
                    return opened ? "dijitIconFolderOpen" : "dijitIconFolderClosed";
                }else{
                    return "dijitIconFile";
                }
            },
            getLabel: function(item){
                return item.name;
            }
        }, dojo.create("div", {}, constants.HADOOP_FILE_EXPLORER_TREE_CONTAINER));

        hadoopFileTree.model.getChildren = function(parent, callback, onErr){
            if(!parent.children){
            	var subFileItems = remoteHandler.getChildHadoopFileByPath(connectionKey, includeFile, toolkit.getValue(parent.key));
                parent.children = subFileItems;
                var parentNode = hadoopFileTree.getNodesByItem(toolkit.getValue(parent.key))[0];
                parentNode.setChildItems(subFileItems);
                callback(parent['children']);
            }else{
                callback(parent['children']);
            }
        };
        
        hadoopFileTree.startup();
        
        var submitHandler = dojo.connect(dijit.byId(constants.HADOOP_FILE_EXPLORER_BUTTON_OK), "onClick", function(){
        	var selectedItems = hadoopFileTree.get("selectedItems");
        	if(selectedItems == null || selectedItems.length == 0){
        		popupComponent.alert(
        				includeFile ? 
        				alpine.nls.hadoop_props_file_explorer_message_no_select_file : 
        					alpine.nls.hadoop_props_file_explorer_message_no_select_folder);
        		return;
        	}
        	if(selectedItems.length > 1){
        		popupComponent.alert(alpine.nls.hadoop_props_file_explorer_message_select_too_much);
        		return;
        	}
        	if(includeFile == true && hadoopFileTree.model.mayHaveChildren(selectedItems[0])){
        		popupComponent.alert(alpine.nls.hadoop_props_file_explorer_message_select_file);
        		return;
        	}
        	var canClose = fn.call(null, selectedItems[0]);
        	if(canClose == true){
            	dijit.byId(constants.HADOOP_FILE_EXPLORER_DIALOG).hide();
        	}
        });
        eventHandlers.push(submitHandler);
	}
	
	return {
		startup: _startup
	};
});
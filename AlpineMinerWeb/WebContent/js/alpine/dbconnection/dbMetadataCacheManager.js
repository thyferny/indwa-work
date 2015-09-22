define([
    "dijit/Tree"],

    function(Tree){
	var constant = {
		DIALOG_ID: "alpine_flow_dbSourceUpdater_Dialog",
		TREE_CONTAINER: "alpine_flow_dbSourceUpdater_resourcesTree_container",
		TREE_ID: "alpine_flow_dbSourceUpdater_resourcesTree",
		TREE_MENU_ID: "alpine_flow_dbSourceUpdater_resourcesTree_menu",
		REQUEST_URL: baseURL + "/main/dataSource/db/manager.do",
		RESOURCE_TYPE: {
			DB: "db",
			SCHEMA: "sc",
			VIEWCONTAINER: "vc",
			TABLECONTAINER: "tc",
			TABLE: "tb",
			VIEW: "vi"
		},
		TREE_CHILDE_ATTR: "subItems"
	};
	
	var resourceFetcher = new ResourceFetcher(),
		resourceTree = new ResourceTree(),
		resourceCache = new ResourceCache();
	
	// must be released when dialog is close.
	var eventConnectQueue = new Array();
	
	this.updater = new ResourceUpdater();
	
	
	/**
	 * Declare ResourceUpdater Class
	 */
	function ResourceUpdater(){
		this.dialogId = constant.DIALOG_ID;
	};
	
	ResourceUpdater.prototype.startup = function(){
		var dialog = dijit.byId(constant.DIALOG_ID);
        dialog.titleBar.style.display = "none";
        dialog.show();
		resourceFetcher.loadRootItems(resourceTree.startup, resourceTree, constant.DIALOG_ID);// load root items and fill them to tree.
//		eventConnectQueue.push(dojo.connect(dialog, "onHide", dialog, ResourceUpdater.prototype.releaseResources));
	};
	
	ResourceUpdater.prototype.close = function(){
		dijit.byId(constant.DIALOG_ID).hide();
	};

	ResourceUpdater.prototype.releaseResources = function(){
		//clear event connects
		var item = null;
		while((item = eventConnectQueue.pop()) != undefined){
			dojo.disconnect(item);
		}
		//destroy instance
		dijit.byId(constant.TREE_ID).destroyRecursive();
		dijit.byId(constant.TREE_MENU_ID).destroyRecursive();
	};

	/**
	 * Declare ResourceTree Class
	 */
	function ResourceTree(){
		
	};
	
	ResourceTree.prototype.startup = function(rootItems){
		var treeWidget = dijit.byId(constant.TREE_ID);

		if(!treeWidget){
			this.create(rootItems);
		}else{
			//this.recreate(rootItems, treeWidget);
			//destory tree
			dijit.byId(constant.TREE_ID).destroyRecursive();
			dijit.byId(constant.TREE_MENU_ID).destroyRecursive();
			
			this.create(rootItems);
		}
	};
	
	ResourceTree.prototype.recreate = function(currentRootItems, treeWidget){
	  	var rootItems = treeWidget.model.store._arrayOfTopLevelItems;
		// first delete rootItem from rootItems which is removed currently.
		removeLoop:
		for(var i = 0;i < rootItems.length;i++){
			var rootItem = rootItems[i];
			for(var j = 0;j < currentRootItems.length;j++){
				var currentRootItem = currentRootItems[j];
				if(getVal(rootItem.key) == currentRootItem.key){
					continue removeLoop;
				}
			}
			// already removed currently. So delete it from rootItem
			treeWidget.model.store.deleteItem(rootItem);
			i--;
		}
		
		
		
		// next add unsaved rootItem from currentRootItems to tree.
		
		insertLoop:
		for(var i = 0;i < currentRootItems.length;i++){
			var currentRootItem = currentRootItems[i];
			for(var j = 0;j < rootItems.length;j++){
				var rootItem = rootItems[j];
				if(currentRootItem.key == getVal(rootItem.key)){
					continue insertLoop;
				}
			}
			console.log(currentRootItem);
			// new root item, insert it to tree.
			treeWidget.model.store.newItem(currentRootItem);
		}
		
	};
	
	/**
	 * initialize Tree
	 * @param rootItems
	 * @return	void
	 */
	ResourceTree.prototype.create = function(rootItems){
		var tree = null,
			treeMenu = null;
		var resourceStore = new dojo.data.ItemFileWriteStore({
			data: {
				identifier: "key",// refer to ResourceItem
				label: "label",
				items: rootItems
			}
		});
		
		var treeContainer = dojo.create("div", {}, constant.TREE_CONTAINER);
		
		tree = new dijit.Tree({
			showRoot: false,
			style : "height: 100%;",
	        id: constant.TREE_ID,
			openOnClick: true,
			persist: false,
			model: new dijit.tree.ForestStoreModel({
		        store: resourceStore,
				childrenAttrs : [ constant.TREE_CHILDE_ATTR ],
		        query: {
		            "type": "*"
		        },
		        mayHaveChildren: function(item){
		        	if(parent == this.root){
		        		true;
		        	}
		        	switch(resourceCache.load(item).type){
		        	case constant.RESOURCE_TYPE.DB:
		        	case constant.RESOURCE_TYPE.SCHEMA:
		        	case constant.RESOURCE_TYPE.TABLECONTAINER:
		        	case constant.RESOURCE_TYPE.VIEWCONTAINER:
		        		return true;
		    		default :
		    			return false;
		        	}
		        },
		        getIdentity: function(item){
		        	return getVal(item.key);
		        }
		    }),
		    getIconClass: function(item, opened){
	        	var style;
	        	if(item == this.model.root){
	        		return null;
	        	}
	        	switch(resourceCache.load(item).type){
	        	case constant.RESOURCE_TYPE.DB:
	            	return "resource_db";
	        	case constant.RESOURCE_TYPE.SCHEMA:
	            	return "resource_schema";
	        	case constant.RESOURCE_TYPE.TABLECONTAINER:
	            	return "resource_table_container";
	        	case constant.RESOURCE_TYPE.VIEWCONTAINER:
	            	return "resource_view_container";
	        	case constant.RESOURCE_TYPE.TABLE :
	            	return "resource_table";
	        	case constant.RESOURCE_TYPE.VIEW :
	            	return "resource_view";
	        	}
	        },
	        getLabel: function(item){
	            return item.label;
	        }
		}, treeContainer);
		// As initialize top level node will be crash if put getChildren method in constuction method to override. So put it here to avoid initialize top level node invoked.
		tree.model.getChildren =  function(parent, callback, onErr){
        	if(!parent.children){
        		resourceFetcher.loadTreeItems(parent, function(children){
        			resourceTree.addTreeItems(tree.model, children, parent);
        			var parentNode = getVal(tree.getNodesByItem(getVal(parent.key)));
        			parentNode.setChildItems(children);
        			callback(parent[constant.TREE_CHILDE_ATTR]);
        		});
        	}else{
        		callback(parent[constant.TREE_CHILDE_ATTR]);
        	}
        };
		tree.startup();

		treeMenu = new dijit.Menu({
			id: constant.TREE_MENU_ID
		});
		treeMenu.bindDomNode(tree.domNode);
		treeMenu.addChild(new dijit.MenuItem({
			iconClass:"dijitIconUndo",
			label: alpine.nls.dbresource_tree_menu_refresh,
			onClick: function(){
				var selectedItemArray = dijit.byId(constant.TREE_ID).get("selectedItems");
				if(selectedItemArray){
					for(var i = 0;i < selectedItemArray.length;i++){
						resourceFetcher.refreshTreeItems(selectedItemArray[i], resourceTree.refresh);
					}
				}
			}
		}));
		eventConnectQueue.push(dojo.connect(treeMenu, "_openMyself", tree, function(e){
			var tn = dijit.getEnclosingWidget(e.target);
			var treeItem = tn.item;
			var treeWidget = dijit.byId(constant.TREE_ID);
			if(treeItem){//avoid right click out of tree node.
		    	 var items = treeWidget.get('selectedItems');
		    	 if(!items|| items.length==0 || items.indexOf(tn.item) < 0){
						treeWidget.set('selectedItem', tn.item);
		    	 }
		    	 if(getVal(treeItem.type) == constant.RESOURCE_TYPE.SCHEMA){
		    		 treeMenu.getChildren()[0].set('disabled', true);
		    		 treeMenu.getChildren()[0].set('style','display:none');
		    	 }else{
		    		 treeMenu.getChildren()[0].set('disabled', false);
		    		 treeMenu.getChildren()[0].set('style','display:block');
		    	 }
			}
        }));
		treeMenu.startup();
	};
	
	ResourceTree.prototype.addTreeItems = function(treeModel, children, parent){
		//sync children to data node.
		parent.children = children;
		var parentChildrenArray = parent[constant.TREE_CHILDE_ATTR];
		if(!parentChildrenArray){
			parentChildrenArray = new Array();
			parent[constant.TREE_CHILDE_ATTR] = parentChildrenArray;
		}
		for(var i = 0;i < children.length;i++){
			var childrenItem = children[i];
			parentChildrenArray.push(childrenItem);
			//make sure children of table container or view container will be stored.
			if(childrenItem.children){
				var childrenLength = childrenItem.children.length,
					childrenArray = parentChildrenArray[i][constant.TREE_CHILDE_ATTR];
				if(!childrenArray){
					childrenArray = new Array();
					parentChildrenArray[i][constant.TREE_CHILDE_ATTR] = childrenArray;
				}
				for(var j = 0;j < childrenLength;j++){
					childrenArray.push(childrenItem.children[j]);
				}
			}
		}
	};
	
	ResourceTree.prototype.removeTreeItem = function(store, item){
		while(item[constant.TREE_CHILDE_ATTR] && item[constant.TREE_CHILDE_ATTR].length > 0){
			arguments.callee(store, item[constant.TREE_CHILDE_ATTR][0]);
		}
		store.deleteItem(item);
	};
	
	/**
	 * rebuild tree item with its children.
	 * @param childrenItems		the list of item of tree item.
	 * @return
	 */
	ResourceTree.prototype.refresh = function(parentItem, childrenItems){

		var treeWidget = dijit.byId(constant.TREE_ID);
		var itemNode = getVal(treeWidget.getNodesByItem(getVal(parentItem.key)));
		parentItem[constant.TREE_CHILDE_ATTR] = childrenItems;
		itemNode.setChildItems(childrenItems);
	};
	
	/**
	 * Declare ResourceFetcher Class
	 * to load all of resources from server, must be through this class.
	 */
	function ResourceFetcher(){
		
	};
	
	ResourceFetcher.prototype._adapterItems = function(items){
		var result = new Array();
		for(var i = 0;i < items.length;i++){
			result.push(new ResourceItem(items[i]));
		}

		result.sort(sortHandler);
		return result;
	};
	
	/**
	 * load root items from Server side.
	 * @param fn call back function, when server return data. 
	 */
	ResourceFetcher.prototype.loadRootItems = function(fn, scope,callbackPanelId){
        var url = constant.REQUEST_URL + "?method=getAllDBConnectionMetadata";
		//progressBar.showLoadingBar();
		ds.get(url, function(rootItems){
			var adaptedItems = resourceFetcher._adapterItems(rootItems);
		//	progressBar.closeLoadingBar();
			fn.call(scope, adaptedItems);
		}, null, false,callbackPanelId );
	};
	
	/**
	 * load children data from Server side
	 * @param parentItem
	 * @param fn call back function, when server return data. 
	 * @return a list of ResourceItem
	 */
	ResourceFetcher.prototype.loadTreeItems = function(parentItem, fn){
		var url = constant.REQUEST_URL + "?method=getDBMetaDataChildren";
		ds.post(url, resourceCache.load(parentItem),  function(children){
			var adaptedItems = resourceFetcher._adapterItems(children);
			fn.call(null, adaptedItems);
		});
	};

	/**
	 * load children data from Server side
	 * @param parentItem
	 * @param fn call back function, when server return data. 
	 * @return a list of ResourceItem
	 */
	ResourceFetcher.prototype.refreshTreeItems = function(parentItem, fn){
		var url = constant.REQUEST_URL + "?method=refreshDBMetaData";
		ds.post(url, resourceCache.load(parentItem),  function(children){
			var adaptedItems = resourceFetcher._adapterItems(children);
			fn.call(null, parentItem, adaptedItems);
		});
	};
	
	
	
	/**
	 * Declare ResourceItem Class
	 * Model of Resource for UI
	 * argument resource is Model of server side.
	 */
	function ResourceItem(resource){
		this.key = this.buildKey(resource),
		this.label = resource.name;
		this.type = resource.type;
		this.connectionName = resource.connectionName;
		this.schemaName = resource.schemaName;
		this.name = resource.name;
		if(resource.children){
			this.children = resourceFetcher._adapterItems(resource.children);
		}
//		resourceCache.put(resource);
	};

	
	ResourceItem.prototype.buildKey = function(resource){
		var pathKey,
			_type = getVal(resource.type);
		switch(_type){
    	case constant.RESOURCE_TYPE.DB:
    		pathKey = getVal(resource.connectionName);
    		break;
    	case constant.RESOURCE_TYPE.SCHEMA:
    		pathKey = getVal(resource.connectionName) + "-" + getVal(resource.schemaName);
    		break;
    	case constant.RESOURCE_TYPE.TABLECONTAINER:
    	case constant.RESOURCE_TYPE.VIEWCONTAINER:
    		pathKey = getVal(resource.connectionName) + "-" + getVal(resource.schemaName) + "-" + getVal(resource.type);
    		break;
    	case constant.RESOURCE_TYPE.TABLE:
    	case constant.RESOURCE_TYPE.VIEW:
    	default:
    		pathKey = getVal(resource.connectionName) + "-" + getVal(resource.schemaName) + "-" + getVal(resource.type) + "-" + getVal(resource.name);
    	}
		return pathKey;
		
	};
	
	function ResourceCache(){
//		this.cache = new Array();
	}
	
//	ResourceCache.prototype.put = function(resource){
//		this.cache[ResourceItem.prototype.buildKey(resource)] = resource;
//	};
	
	ResourceCache.prototype.load = function(resource){
//		return this.cache[getVal(resource.key)];
		var res = {};
		switch(getVal(resource.type)){
    	case constant.RESOURCE_TYPE.TABLE:
    	case constant.RESOURCE_TYPE.VIEW:
    	case constant.RESOURCE_TYPE.TABLECONTAINER:
    	case constant.RESOURCE_TYPE.VIEWCONTAINER:
    	case constant.RESOURCE_TYPE.SCHEMA:
    		res.schemaName = getVal(resource.schemaName);
    	case constant.RESOURCE_TYPE.DB:
    		res.connectionName = getVal(resource.connectionName);
			res.type = getVal(resource.type);
			res.name = getVal(resource.name);
		};
		return res;
	};
	
	function getVal(val){
		return dojo.isArray(val) ? val[0] : val;
	};
	
	function sortHandler(item1, item2){
		var label1 = item1.label,
			label2 = item2.label;
		return label1 < label2 ? -1 : label1 > label2 ? 1 : 0;
	};


        return {
             startup: this.updater.startup,
                close: this.updater.close
        }

});
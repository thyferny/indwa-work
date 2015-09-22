/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * HadoopExplorerUIHelper
 * Author Gary & Robbie
 */
define(["alpine/datasourceexplorer/DataSourceExplorerManager",
        "alpine/layout/GuiHelper",
        "alpine/datasourceexplorer/InspectHadoopFileProperty",
        "alpine/datasourceexplorer/HadoopFileDownloadConfigUIHelper"], function(remoteHandler, guiHelper, hadoopFilePropertyInspecter, hadoopFileDownload){
	
	var currentConnName = null;
	var path = null;
	
	var constants = {
		CREATE_BTN: "alpine_datasourceexplorer_create_btn"	
	};
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.CREATE_BTN), "onClick", createSubFolder);
	});
	
	function _getNextDisplayInfo(currentItem, callbackfunction){
		if(currentConnName == null){// if true, means first time to call this.
			currentConnName = toolkit.getValue(currentItem.key);
		}else{
			path = toolkit.getValue(currentItem.key);//Because connection has key attribute also.
		}
		remoteHandler.getNextFileInHadoop(currentConnName, path,callbackfunction, _callbackOnbuildChildArgs, function(){
			//if retrieve failed, assume connect failed, then clear currentConnName
			currentConnName = null;
		});
	}

    function _callbackOnbuildChildArgs(callbackfunction, result){
        var gridArgs = _buildNextGridArgs(result);
        callbackfunction(gridArgs) ;
    }
	
	function _buildNextGridArgs(data){
		return {
			gridData: data,
			structure: [
			    {
			    	name: "",
			    	field: "isDir", 
			    	width: "35px",
			    	formatter: function(value) {
             			//we need isDir in here in order to switch icon.
			    		var src = "";
			    		if(value == true){
			    			src = baseURL + "/images/workbench_icons/directory.png";
			    		}else{
			    			src = baseURL + "/images/workbench_icons/datasource.png";
			    		}
                        return "<img class=\"listIcon\" src=\"" + src + "\" />";
                    }
			    },{
			    	name: "",
			    	field: "label", width: "100%",
             		formatter: function(value, idx) {
             			var isDir = toolkit.getValue(this.grid.getItem(idx).isDir);
                        return "<div class=\"" + (isDir? "selectablelistValue" : "listValue")  + "\" title=\"" + value + "\">" + value + "</div>";
                    }
             	},{
             		name: "",
             		field: "key",
             		width: "12px",
             		formatter: function(value, idx){
             			var isDir = toolkit.getValue(this.grid.getItem(idx).isDir);
             			if(isDir == true){
             				return "";
             			}
             			var downloadLink = dojo.create("a", {
             				className: "infoIcon",
             				style: "display: none; cursor: pointer", 
             				id: _replaceSpecialChar(value) + "_propIcon"
             			});
             			return downloadLink.outerHTML;
             		}
             	},{
             		name: "",
             		field: "key",
             		width: "14px",
             		formatter: function(value, idx){
             			var isDir = toolkit.getValue(this.grid.getItem(idx).isDir);
             			if(isDir == true){
             				return "";
             			}
             			var downloadLink = dojo.create("a", {
             				className: "downloadIcon",
             				style: "display: none; cursor: pointer", 
             				id: _replaceSpecialChar(value) + "_downloadIcon"
             			});
             			return downloadLink.outerHTML;
             		}
             	},{
             		name: "",
             		field: "key",
             		width: "12px",
             		formatter: function(value){
             			var deleteLink = dojo.create("a", {
             				className: "deleteIcon", 
             				style: "display: none; cursor: pointer", 
             				id: _replaceSpecialChar(value) + "_delIcon"
             			});
             			return deleteLink.outerHTML;
             		}
             	}
			],
			buildDisplayInfoFn: _getNextDisplayInfo,
			isOpenable: function(currentItem){
				return toolkit.getValue(currentItem.isDir);
			},
			ableToImport: function(){
				return true;
			},
			ableToCreateSubFolder: function(){
				return true;
			},
            ableToCreateConnection:function(){
                return false;
            },
			saveCurrentKey: function(key){
				if(currentConnName == key){
					path = null;
				}else{
					path = key;
				}
			},
			onRowMouseOver: function(e){
				if(e.rowIndex == undefined){
	                return;
	            }
	            var currentItem = this.getItem(e.rowIndex);
	            if(!currentItem){
	            	return;// to avoid the exception after delete last item.
	            }
	            var downloadLink = dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_downloadIcon");
	            var propLink = dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_propIcon");
	            dojo.style(dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_delIcon"), "display", "block");
	            if(downloadLink){
	            	dojo.style(downloadLink, "display", "block");
	            }
	            if(propLink){
	            	dojo.style(propLink, "display", "block");
	            }
			}, 
			onRowMouseOut: function(e){
				if(e.rowIndex == undefined){
	                return;
	            }
	            var currentItem = this.getItem(e.rowIndex);
	            var downloadLink = dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_downloadIcon");
	            var propLink = dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_propIcon");
	            dojo.style(dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_delIcon"), "display", "none");
	            if(downloadLink){
	            	dojo.style(downloadLink, "display", "none");
	            }
	            if(propLink){
	            	dojo.style(propLink, "display", "none");
	            }
			},
			onCellClick: function(e){
				if(e.rowIndex == undefined){
	                return;
	            }
	            var currentItem = this.getItem(e.rowIndex);
	            if(e.cell.index == 2 && dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_propIcon")){//inspect property
					remoteHandler.loadHadoopProperty(_getCurrentConnKey(), toolkit.getValue(currentItem.key), function(data){
						hadoopFilePropertyInspecter.openHadoopFileProperty(data);
					});
				}else if(e.cell.index == 3 && dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_downloadIcon")){// download file
					var filePath = toolkit.getValue(currentItem.key);
					remoteHandler.loadHadoopProperty(_getCurrentConnKey(), toolkit.getValue(currentItem.key), function(data){
						hadoopFileDownload.openHadoopFileDownloadConfig(data, filePath);
					});
				}else if(e.cell.index == 4 && dojo.byId(_replaceSpecialChar(toolkit.getValue(currentItem.key)) + "_delIcon")){//delete row
					var grid = this;
					var isDir = toolkit.getValue(currentItem.isDir);
					var confirmMsg = isDir ? alpine.nls.hadoop_data_mgr_confirm_delete_directory : alpine.nls.hadoop_data_mgr_confirm_delete_file;
					popupComponent.confirm(confirmMsg, {
						handle: function(){
							remoteHandler.deleteResource(_getCurrentConnKey(), toolkit.getValue(currentItem.key), function(){
								grid.store.deleteItem(currentItem);
							});
						}
					});
				}
			},
			dragging: true
		};
	}
	
	function _getCurrentConnKey(){
		return currentConnName;
	}
	
	function _getCurrentFolderPath(){
		return path;
	}
	
	function createSubFolder(){
		 guiHelper.showTextFieldEditorDialog(
            function (newName){
            	remoteHandler.createSubFolder(_getCurrentConnKey(), _getCurrentFolderPath(), newName, function(){
            		guiHelper.hideTextFieldEditorDialog();
            		alpine.datasourceexplorer.DataSourceExplorerUIHelper.refreshCurrentLevel();
            	});
            }, null, /^[\w]+$/, alpine.nls.hadoop_data_mgr_create_folder_title, alpine.nls.OK, null, true);
	}
	
	function _release(){
		currentConnName = null;
		path = null;
	}
	
	function _replaceSpecialChar(text){
		return text.replace(/&amp;/g, "_").replace(/&/g, "_");
	}
	
	return {
		getCurrentConnKey: _getCurrentConnKey,
		getCurrentFolderPath: _getCurrentFolderPath,
		getNextDisplayInfo: _getNextDisplayInfo,
		release: _release
	};
});
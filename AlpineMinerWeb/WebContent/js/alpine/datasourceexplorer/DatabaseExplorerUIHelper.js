/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DatabaseExplorerUIHelper
 * Author Gary & Robbie
 */
define(["alpine/datasourceexplorer/DataSourceExplorerManager", "alpine/spinner"], function(remoteHandler, spinner){
	
	var currentConnName;
    var currentSchemaName;

	function _getSchemaDisplayInfo(currentItem, outercallbackfunction){
		currentConnName = toolkit.getValue(currentItem.label);
		remoteHandler.getSchemasByConnection(currentConnName, outercallbackfunction, _callbackOnbuildSchemaGridArgs);
	}


    function _callbackOnbuildSchemaGridArgs(callbackfunction, result){
       var gridArgs = _buildSchemaGridArgs(result);
        callbackfunction(gridArgs) ;
    }

	function _buildSchemaGridArgs(data){
		return {
			gridData: data,
			structure: [
             	{name: "",field: "key", width: "35px",
             		formatter: function(value) {
                         var src = baseURL + "/images/workbench_icons/directory.png";
                         return "<img class=\"listIcon\" src=\"" + src + "\" />";
                    }
             	},
             	{name: "",field: "label", width: "100%",
             		formatter: function(value) {
                         return "<div class=\"selectablelistValue\" title=\"" + value + "\">" + value + "</div>";
                    }
             	}
			],
			buildDisplayInfoFn: _getTableViewDisplayInfo,
			isOpenable: function(currentItem){
				return true;
			},
			ableToImport: function(){
				return false;
			},
			ableToCreateSubFolder: function(){
				return false;
			},
			saveCurrentKey: function(key){
				//nothing to do. this function just for hadoop return previous level to save path.
			},
            ableToCreateConnection:function(){
                return false;
            }
		};
	}
	
	function _getTableViewDisplayInfo(currentItem, outercallbackfunction){
		currentSchemaName = toolkit.getValue(currentItem.label);
        var tableViewArray = remoteHandler.getTableViewBySchema(currentConnName, currentSchemaName, outercallbackfunction, _callbackOnBuildTableViewGridArgs);
	}

    function _callbackOnBuildTableViewGridArgs(callbackfunction, result)
    {
        var gridArgs = _buildTableViewGridArgs(result);
        callbackfunction(gridArgs) ;

    }

	function _buildTableViewGridArgs(data){
		return {
			gridData: data,
			structure: [
             	{name: "",field: "key", width: "35px",
             		formatter: function(value) {
                         var src = baseURL + "/images/workbench_icons/datasource.png";
                         return "<img class=\"listIcon\" src=\"" + src + "\" />";
                    }
             	},
             	{name: "",field: "label", width: "100%",
             		formatter: function(value) {
                         return "<div class=\"listValue\" title=\"" + value + "\">" + value + "</div>";
                    }
             	}
			],
			buildDisplayInfoFn: null,
			isOpenable: function(currentItem){
				return false;
			},
			ableToImport: function(){
				return true;
			},
			ableToCreateSubFolder: function(){
				return false;
			},
            ableToCreateConnection:function(){
                return false;
            },
			dragging: true
		};
	}
	
	function _release(){
		currentConnName = null;
        currentSchemaName = null;
	}

    function _getConnAndSchema() {
        if (currentConnName != null && currentSchemaName != null) {
            return {
                connection: currentConnName,
                schema: currentSchemaName
            };
        } else {
            return null;
        }

    }
	
	return {
		getSchemaDisplayInfo: _getSchemaDisplayInfo,
        getConnAndSchema: _getConnAndSchema,
		release: _release
	};
});
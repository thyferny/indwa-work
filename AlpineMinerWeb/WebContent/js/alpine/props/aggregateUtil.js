var aggregateColumnChecker = (function(){
	var allofColumnNamesStore = {};
	var currentReferColumns = new Array();
	var currentType;
	
	function _storeReferColumns(type, columns){
		switch(type){
		case "PT_CUSTOM_AGG_COLUMN":
			allofColumnNamesStore.PT_CUSTOM_AGG_COLUMN = _buildColumnStore(columns, function(field){
				return field.alias;
			});
			break;
		case "PT_CUSTOM_AGG_WINDOW":
			allofColumnNamesStore.PT_CUSTOM_AGG_WINDOW = _buildColumnStore(columns, function(field){
				return field.resultColumn;
			});
			break;
		case "PT_CUSTOM_AGG_GROUPBY":
			allofColumnNamesStore.PT_CUSTOM_AGG_GROUPBY = _buildColumnStore(columns, function(field){
				return field;
			});
			break;
		}
	}
	
	function _buildColumnStore(sourceColumns, fn){
		var columns = new Array();
		for(var i = 0; i < sourceColumns.length; i++){
			columns.push(fn(sourceColumns[i]));
		}
		return columns;
	}
	
	//variable of currentReferColumns and currentType is used to avoid wrongs if user click Cancel button after do some add and delete.
	function initialize(type){
		currentType = type;
		if(allofColumnNamesStore[type]){
			currentReferColumns = dojo.clone(allofColumnNamesStore[type]);
		} else {
            currentReferColumns = [];
        }
	}
	
	// store refer columns when leave current dialog.
	function _storeCurrentReferColumn(referColumns){
		_storeReferColumns(currentType, referColumns);
	}
	
	function _addColumnToCurrent(referColumn){
		currentReferColumns.push(referColumn);
	}
	function _removeColumnFromCurrent(referColumn){
		var newArray = new Array();
		for(var i = 0;i < currentReferColumns.length;i++){
			if(referColumn != currentReferColumns[i]){
				newArray.push(currentReferColumns[i]);
			}
		}
		currentReferColumns = newArray;
	}
	
	// Check argument in scope of all types, except current edit.
	function _checkColumnIsExists(columnName){
		for(var typeStore in allofColumnNamesStore){
			if(typeStore == undefined){
				continue;
			}
			var referColumns = typeStore == currentType ? currentReferColumns : allofColumnNamesStore[typeStore];
			for(var i = 0;i < referColumns.length;i++){
				if(columnName == referColumns[i]){
					return true;
				}
			}
		}
		//store columnName, if can not found it in all of refer colmns.
		_addColumnToCurrent(columnName);
		return false;
	}

    function _release(type) {
        switch(type){
            case "PT_CUSTOM_AGG_COLUMN":
                allofColumnNamesStore.PT_CUSTOM_AGG_COLUMN = [];
                break;
            case "PT_CUSTOM_AGG_WINDOW":
                allofColumnNamesStore.PT_CUSTOM_AGG_WINDOW = [];
                break;
            case "PT_CUSTOM_AGG_GROUPBY":
                allofColumnNamesStore.PT_CUSTOM_AGG_GROUPBY = [];
                break;
        }
    };
	
	return {
		isExists: _checkColumnIsExists,
		storeCurrentColumns: _storeCurrentReferColumn,
		remove: _removeColumnFromCurrent,
		initializeItems: _storeReferColumns,
		initialize: initialize,
        release: _release
	};
})();


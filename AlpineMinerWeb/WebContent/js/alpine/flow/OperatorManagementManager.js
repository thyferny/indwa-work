/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * OperatorManagementManager.js 
 * Author Gary
 * Jul 5, 2012
 */
define(["alpine/operatorexplorer/OperatorUtil",
        "alpine/flow/OperatorLinkManager"], function(OperatorUtil, linkHandler){
	
	var constants = {
		REQUEST_URL: baseURL + "/main/operatorManagement.do"	
	};
	
	/**
	 * paire of key/value
	 * the key is Operator's uid,
	 * the value is Operator basic information.
	 * value: {
	 * 		String uid;
	 * 		String name;
	 * 		String classname;
	 * 		boolean isValid;
	 * 		String connectionName;
	 * 		String operatorType;
	 * 		int x;
	 * 		int y;
	 * //********DB**********
	 * 		String outputSchema;
	 * 		String outputTable;
	 * 		String outputType;
	 * 		boolean hasDbTableInfo;
	 * 		Array interTableList;// For details see com.alpine.miner.impls.controller.FlowDTO.IntermediateTableInfo.class
	 * 		String modelType;
	 * //*********Hadoop**********
	 * 		String outputHadoopFilePath;
	 * }
	 */
	var operatorMap = [];
	
	/**
	 * parie of key(string)/value(Array)
	 * store operator label and its number series.
	 * in order to generate operator label if it have conflict with other operator.
	 */
	var operatorLabelPool = [];
	
	/**
	 * @return {
	 * 		uid,
	 * 		key: the operator class name,
	 * 		icon: normal status icon,
	 * 		icons: selected status icon
	 * 		optype: operator category,
	 * }
	 */
	function getOperatorBasicInfo(operatorClassName, callback, errCallback){
		var operatorInfo = OperatorUtil.getOperatorObjectByKey(operatorClassName);
		var operatorInfoCopy = {
				uid: new Date().getTime()
		};
		dojo.safeMixin(operatorInfoCopy, operatorInfo);
		callback.call(null, operatorInfoCopy);
	}
	
	function generateOperatorLabel(arglabel){
		var seriesPattern = /-[\d]+$/,
			label = arglabel.replace(seriesPattern, "");
		if(operatorLabelPool[arglabel]){// first try to find original label(include order number)
			var labelSeries = operatorLabelPool[arglabel];
			label = raiseSeries(arglabel, labelSeries);
		}else if(operatorLabelPool[label]){//second try to find label(except order number)
			var labelSeries = operatorLabelPool[label];
			label = raiseSeries(label, labelSeries);
		}else{
			label = arglabel;
			if(!operatorLabelPool[label]){
				operatorLabelPool[label] = [];
			}
			operatorLabelPool[label][0] = true;
		}
		return label;
		
		function raiseSeries(key, labelSeries){
			if(labelSeries.length > 0 && !labelSeries[0]){
				labelSeries[0] = true;
				return key;
			}
			for(var i = 1;i < labelSeries.length;i++){
				if(!labelSeries[i]){
					key += "-";
					key += i;
					labelSeries[i] = true;
					return key;
				}
			}
			key = key + "-" + labelSeries.length;
			labelSeries.push(true);
			return key;
		}
	}
	
	function _pushLabelToPool(label){
		//if label exist in pool, we do nothing. because it already pushed in the pool by generateOperatorLabel
		// e.g. drag a dataset/operator to workflow.
		if(operatorLabelPool[label]){
			return;
		}
		//label already have order number. Like Histogram-2
		//or the label's name include -xxx like 'part-r-0000'
		if(/^.*-[\d]+$/.test(label)){
			var prefix = label.replace(/-[\d]+$/, "");
			var orderNum = parseInt(label.replace(/^.*-/, ""));
			if(operatorLabelPool[prefix]){
				operatorLabelPool[prefix][orderNum] = true;
				return;
			}
		}
		operatorLabelPool[label] = [];
		operatorLabelPool[label][0] = true;
	}
	
	function _removeLabelFromPool(label){
		var seriesPattern = /-[\d]+$/;
		var prefix = label.replace(seriesPattern, "");
		if(operatorLabelPool[label]){
			delete operatorLabelPool[label][0];
		}else if(operatorLabelPool[prefix]){
			var orderNum = parseInt(label.replace(/^.*-/, ""));
			delete operatorLabelPool[prefix][orderNum];
		}
	}
	
	/**
	 * operatorInfo = {
	 * 		uuid,
	 * 		name,
	 * 		operatorClass,
	 * 		hasDefaultVal,
	 * 		x,
	 * 		y,
	 * 		data source property..
	 * }
	 */
	function storeOperator(operatorInfo, callback, errCallback, callbackpanelid){
 		ds.post(constants.REQUEST_URL + "?method=addOperator", {
			flowInfo: alpine.flow.WorkFlowManager.getEditingFlow(),
			operatorParam: operatorInfo
		}, function(operatorPrimaryInfo){
			storeOperatorPrimaryInfo(operatorPrimaryInfo);
			callback.call(null, operatorPrimaryInfo);
		}, errCallback, false, callbackpanelid);
	}
	
	/**
	 * to remove operators and them connect info from work flow.
	 */
	function removeOperator(flowInfo, operatorUids, callback, errCallback,callbackpanelid){
        ds.post(constants.REQUEST_URL + "?method=removeOperator", {
			flowInfo: flowInfo,
			operatorUids: operatorUids
		}, function (operatorInfoSet){
			for(var i = 0;i < operatorUids.length;i++){
				_removeLabelFromPool(getOperatorPrimaryInfo(operatorUids[i]).name);
				_removeOperatorPrimaryInfo(operatorUids[i]);
			}
			_deleteInvalidOperatorInfo(operatorInfoSet);
			
			updateOperatorPrimaryInfo(operatorInfoSet);
			callback.call(null, operatorInfoSet);
		}, errCallback, false, callbackpanelid);
	}
	
	/**
	 * to rename operator 
	 */
	function _renameOperator(flowInfo, operatorUid, newName, callback){
		ds.post(constants.REQUEST_URL + "?method=renameOperator", {
			flowInfo: flowInfo,
			operatorParam: {
				uuid: operatorUid,
				name: newName
			}
		}, function(){
			_removeLabelFromPool(getOperatorPrimaryInfo(operatorUid).name);
			_pushLabelToPool(newName);
			callback.call(null);
		}, false);
	}
	
	function _deleteInvalidOperatorInfo(operatorInfoSet){
		// delete invalid position info.
		for(var i = 0;i < operatorInfoSet.length;i++){
			delete operatorInfoSet[i].x;
			delete operatorInfoSet[i].y;
		}
	}
	
	/**
	 * operatorPrimaryInfo: {
	 * 		String uid;
	 * 		String name;
	 * 		String classname;
	 * 		boolean isValid;
	 * 		String connectionName;
	 * 		String operatorType;
	 * 		int x;
	 * 		int y;
	 * //********DB**********
	 * 		String outputSchema;
	 * 		String outputTable;
	 * 		String outputType;
	 * 		boolean hasDbTableInfo;
	 * 		Array interTableList;// For details see com.alpine.miner.impls.controller.FlowDTO.IntermediateTableInfo.class
	 * 		String modelType;
	 * //*********Hadoop**********
	 * 		String outputHadoopFilePath;
	 * }
	 */
	function storeOperatorPrimaryInfo(operatorPrimaryInfo){
		operatorMap[operatorPrimaryInfo.uid] = operatorPrimaryInfo;
	}
	
	/**
	 * get Operator information by uid
	 */
	function getOperatorPrimaryInfo(operatorUid){
		return operatorMap[operatorUid];
	}
	
	function _removeOperatorPrimaryInfo(operatorUid){
		delete operatorMap[operatorUid];
	}
	
	function updateOperatorPrimaryInfo(operatorPrimaryArray){
		for(var i = 0;i < operatorPrimaryArray.length;i++){
			var newOperatorInfo = operatorPrimaryArray[i];
			delete newOperatorInfo.x;
			delete newOperatorInfo.y;
			var oldOperatorInfo = getOperatorPrimaryInfo(newOperatorInfo.uid);
			dojo.safeMixin(oldOperatorInfo, newOperatorInfo);
		}
	}
	
	function _getOperatorUidByName(name){
		var uid = null;
		forEachOperatorInfo(function (operatorInfo){
			if(operatorInfo.name == name){
				uid = operatorInfo.uid;
			}
		});
		return uid;
	}
	
	function forEachOperatorInfo(fn){
		for(var uid in operatorMap){
			if(typeof operatorMap[uid] == "function"){
				continue;
			}
			fn.call(operatorMap, operatorMap[uid]);
		}
	}
	
	/**
	 * get all of previous Operators(only straight operator) for giving Operator's uid.
	 */
	function getPreviousOperators(operatorUid){
		var previousOperatorArray = [];
		linkHandler.forEachLink(function(link){
			if(link.targetId == operatorUid){
				previousOperatorArray.push(getOperatorPrimaryInfo(link.sourceId));
			}
		});
		return previousOperatorArray;
	}
	
	/**
	 * get next level operators(only straight operator) for giving Operator's uid.
	 */
	function getNextOperators(operatorUid){
		var nextOperatorArray = new Array();
		linkHandler.forEachLink(function(link){
			if(link.sourceId == operatorUid){
				nextOperatorArray.push(getOperatorPrimaryInfo(link.targetId));
			}
		});
		return nextOperatorArray;
	}
	
	/**
	 * get all of parent Operators for giving Operator's uid.
	 * 
	 * the sequence start from first operator in flow.
	 */
	function getAllParentOperators(operatorUid, /*boolean*/inculdeSelf){
		var parentOperatorArray = [];
		_recursiveGetParent(parentOperatorArray, operatorUid);
		if(inculdeSelf == true){
			parentOperatorArray.push(getOperatorPrimaryInfo(operatorUid));
		}
		return parentOperatorArray;
		function _recursiveGetParent(container, currentUid){
			linkHandler.forEachLink(function(link){
				if(link.targetId == currentUid){
					_recursiveGetParent(container, link.sourceId);
					container.push(getOperatorPrimaryInfo(link.sourceId));
				}
			});
		}
	}
	
	/**
	 * assert whether operator has child operator or not
	 * @param adjustFn is a additional function for help adjust. 
	 */
	function hasChildrenOperator(operatorUid, adjustFn){
		var adjuster = adjustFn || function(){
			return true;
		};
		var hasChild = false;
		linkHandler.forEachLink(function(link){
			if(link.sourceid == operatorUid && adjuster(operatorUid, link)){
				hasChild = true;
				return;
			}
		});
		return hasChild;
	}
	
	function release(){
		operatorMap = [];
		operatorLinkSet = [];
		operatorLabelPool = [];
		linkHandler.release();
	}
	
	function _hasOperator(){
        var hasOperator = false;
        for(var key in operatorMap){
            if(key){
                hasOperator = true;
                break;
            }
        }
        return operatorMap && hasOperator;
    }
	
	return {
		getOperatorBasicInfo: getOperatorBasicInfo,
		storeOperator: storeOperator,
		storeOperatorPrimaryInfo: storeOperatorPrimaryInfo,
		getOperatorPrimaryInfo: getOperatorPrimaryInfo,
		updateOperatorPrimaryInfo: updateOperatorPrimaryInfo,
		getOperatorUidByName: _getOperatorUidByName,
		getPreviousOperators: getPreviousOperators,
		getNextOperators: getNextOperators,
		getAllParentOperators: getAllParentOperators,
		hasChildrenOperator: hasChildrenOperator,
		forEachOperatorInfo: forEachOperatorInfo,
		generateOperatorLabel: generateOperatorLabel,
		pushLabelToPool: _pushLabelToPool,
		removeOperator: removeOperator,
		renameOperator: _renameOperator,
		hasOperator: _hasOperator,
		release: release
	};
});
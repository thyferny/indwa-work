/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowVariableManager.js 
 * Author Gary
 * Dec 25, 2012
 */
define(function(){
	
	function _getFlowVariable(flowInfo, currentFlowSign, callback, loadingPane){
		ds.post(alpine.baseURL + "/main/flowVariable.do?method=getFlowVariables", flowInfo, function(data){
			//progressBar.closeLoadingBar();
			callback.call(null, _rebuildData(data, currentFlowSign));
        }, null, false, loadingPane);
	}
	
	function _saveFlowVariables(variableData, flowInfo, callback, loadingPane){
		ds.post(alpine.baseURL + "/main/flowVariable.do?method=saveVariable", {
			flowInfo: flowInfo,
			flowVariableSet: _revertData(variableData)
		}, callback, null, false, loadingPane);
	}
	
	// make data from one level to origin.
	function _revertData(data){
		var flowVariableMap = [];//key = flowName, value = variables
		var flowVariableSet = [];
		for(var i = 0;i < data.length;i++){
			var flowVariable = flowVariableMap[data[i].flowName];
			if(flowVariable == null){
				flowVariable = {
					flowName: data[i].flowName,
					variables: []
				};
				flowVariableMap[data[i].flowName] = flowVariable;
			}
			flowVariable.variables.push({
				name: data[i].name,
				value: data[i].value
			});
		}
		for(var key in flowVariableMap){
			flowVariableSet.push(flowVariableMap[key]);
		}
		return flowVariableSet;
	}
	
	//make data to one level
	function _rebuildData(data, currentFlowSign){
		var result = [];
		// the first element of data is the variable list of current flow.
		data[0].flowName = currentFlowSign;
		result = result.concat(_generateVariablesForSubFlow(data[0]));
		for(var i = 1;i < data.length;i++){
			var subFlowVariables = _generateVariablesForSubFlow(data[i]);
			result = result.concat(subFlowVariables);
		}
		return result;
	}
	
	function _generateVariablesForSubFlow(subflowVariable){
		var subflowVarList = [];
		var flowName = subflowVariable.flowName;
		for(var i = 0;i < subflowVariable.variables.length;i++){
			subflowVarList.push({
				flowName: flowName,
				name: subflowVariable.variables[i].name,
				value: subflowVariable.variables[i].value
			});
		}
		return subflowVarList;
	}
	return {
		getFlowVariable: _getFlowVariable,
		saveFlowVariables: _saveFlowVariables
	};
});
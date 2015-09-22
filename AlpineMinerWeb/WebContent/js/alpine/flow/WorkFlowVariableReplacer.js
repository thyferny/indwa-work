/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowVariableReplacer.js 
 * Author Gary
 * Dec 27, 2012
 */
define(function(){
	var variableNameList = new Array();
	var variableMap = {};

	var PREFIX = "@";
	var ESCAPE_PREFIX = "\\\\" + PREFIX;
	var ESCAPE_PREFIX_TMP = "\\\\" + PREFIX + "*";
	
	function _init(){
		ds.post(alpine.baseURL + "/main/flowVariable.do?method=getFlowVariables", alpine.flow.WorkFlowManager.getEditingFlow(), function(data){
			var varNameList = new Array();
			for(var i = 0;i < data[0].variables.length;i++){
				var variable = data[0].variables[i];
				variableMap[variable.name] = variable.value;
				
				varNameList.push(variable.name);
			}
			variableNameList = _sortVariable(varNameList);
		}, null, true);
	}
	
	function _finalize(){
		variableMap = {};
		variableNameList = new Array();
	}
	
	function _sortVariable(variableNameList){
		return variableNameList.sort(function(v1, v2){
			if(v1.length > v2.length){
				return -1;
			}else if(v1.length < v2.length){
				return 1;
			}else{
				if(v1 > v2){
					return -1;
				}else if(v1 < v2){
					return 1;
				}else{
					return 0;
				}
			}
		});
	}
	
	function _replaceVariable(value){
		if(typeof value != "string" || value == null || value.indexOf(PREFIX) == -1){
			return value;
		}
		value = value.replace(new RegExp(ESCAPE_PREFIX,"gm"), ESCAPE_PREFIX_TMP);//replace all escape words to tmp escape words.
		for(var i = 0;i < variableNameList.length;i++){
			value = value.replace(new RegExp(variableNameList[i],"gm"), variableMap[variableNameList[i]]);//replace all variable to value.
		}
		value = value.replace(new RegExp(ESCAPE_PREFIX_TMP,"gm"), PREFIX);//replace all tmp escape words to prefix words.
		return value;
	}
	
	return {
		init: _init,
		replaceVariable: _replaceVariable,
		finalize: _finalize
	};
});
/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * findandreplaceparameterHelper
 * 
 * Author Will
 * Version 2.7
 * Date 2012-05-15
 */
define(["alpine/flow/FindandreplaceparameterManager",
        "alpine/flow/OperatorManagementManager",
        "alpine/flow/WorkFlowManager"],function(parameterManager, operatorManagement, workFlowManager){
//Const for connecttion schema table maps
var FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ = null;
var findParamDisplay=null;
dojo.ready(function(){
	
	dojo.connect(dijit.byId('find_replace_parameter_value_btn'),"onClick",showFindAndReplaceParameterDlg);
	dojo.connect(dijit.byId('findandreplaceparameter_Parameter_Name'),"onChange",parameter_name_change);
	dojo.connect(dijit.byId('findandreplaceparameter_Current_flow'),"onChange",changeScope);
	dojo.connect(dijit.byId('findandreplaceparameter_All_flows'),"onChange",changeScope);
	dojo.connect(dijit.byId('findandreplaceparameter_btn_search'),"onClick",execFindAndReplaceQuery);
	dojo.connect(dijit.byId('findandreplaceparameter_btn_replace'),"onClick",execReplaceSelectedFindResults);
	dojo.connect(dijit.byId('findandreplaceparameter_btn_cancel'),"onClick",hideFindAndReplaceDlg);
});

function showFindAndReplaceParameterDlg(){
	var dlg = dijit.byId("findandreplaceparameterDlg");
    dlg.titleBar.style.display = "none";
	dlg.show();

    findParamDisplay={
        dbConnectionName:alpine.nls.find_and_replace_param_dbConnectionName,
        schemaName:alpine.nls.find_and_replace_param_schemaName,
        tableName:alpine.nls.find_and_replace_param_tableName,
        outputSchema:alpine.nls.find_and_replace_param_outputSchema
    };

	initGridLayout();
	//console.log(findandreplaceparameter_PARAMETERNAME);
	initRadioSelect();
	
	initParameterNameList();
	setInputBoxValidate();
	_clearResultToolTip();
	// init all connections and connection`s schemas
	parameterManager.init4FindAndReplaceConst(init4FindAndReplaceConst_CallBack); 
	
	//regist onhide event
	clearHistoryValue();


}

function init4FindAndReplaceConst_CallBack(data){
	if(null!=data){
		FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ = data;
	}
}

function initGridLayout(){//init data grid layout
	var gridStore = new dojo.data.ItemFileReadStore({
				    	data:{
						  	  identifier:"flowNameoperName",
						  	  items:[]
						      }
				         }); 
	var gridLayout =  [
     	            	{type: "dojox.grid._CheckBoxSelector"},
    	            	[
    	            	 	{name:alpine.nls.Flow_Name, field: "flowName", width: "40%"},
    	            	 	{name:alpine.nls.Oprator_Name, field: "operName", width: "20%"},
    	            	 	{name:alpine.nls.Parameter_Name, field: "parameterName", width: "20%"},
    	            	 	{name:alpine.nls.Parameter_Value, field: "parameterValue", width: "20%"}
    		            ]
				      ];
	 var grid = dijit.byId("findandreplaceparameter_Search_Result_grid");
	 if(null==grid){
		 var grid = new dojox.grid.DataGrid({
				id:'findandreplaceparameter_Search_Result_grid',
				store:gridStore,
				structure:gridLayout,
				style:"heigth:240px;width:100%;",
				query: {"flowNameoperName": "*"}, //??
				onRowClick : function(event){
	 				   //this.selection.toggleSelect(event.rowIndex);
	 				   var inIndex = event.rowIndex;
	 				   // this.edit.rowClick(event);
	 				    this.selection._beginUpdate();
	 					this.selection.toggleSelect(inIndex);
	 					this.selection._endUpdate();
	 				   //var statusArray = grid.selection.selected;
				},
				canSort: function(){return false;}
				},dojo.create("div",null,dojo.byId("findandreplaceparameter_Search_Result_container")));
		   grid.startup();
	 }else{
		 
		 grid.render();
	 }
	 
}


function initParameterNameList(){
	var parameter_Name = dijit.byId("findandreplaceparameter_Parameter_Name");
	if(null!=parameter_Name && parameter_Name.options.length==0){
		var firstVal;
		if(findParamDisplay!=null){
			var parameterOptions = [] ;
            var i=0;
			for ( var prop in findParamDisplay) {
				var opt = {};
				opt.label = findParamDisplay[prop];
				opt.value = prop;
				if(i==0){
					firstVal = opt.value;
				}
				parameterOptions.push(opt);
                i++;
			}
			parameter_Name.options = parameterOptions;
			parameter_Name.set("value", firstVal);
		}
	
	}

}

function parameter_name_change(){
	clearHistoryValue();
	_clearResultToolTip();
}

function setInputBoxValidate(){
	var findValue = dijit.byId('findandreplaceparameter_Find_Value');
	var replaceWidth = dijit.byId('findandreplaceparameter_Replace_with');
	if(null!=findValue){
		findValue.isValid=function(){}; //inputBoxValidate;
	}
	if(null!=replaceWidth){
		replaceWidth.isValid = function(){};//inputBoxValidate;
	}
	
}

function inputBoxValidate(){
	var inputValue = this.get('value');
	if(null==inputValue || ""==inputValue){
	  return false;
	}
	var pattern = /^[\w-\s]+$/;
	if(pattern.test(inputValue)==false){
		return false;
	}
	
	return true;
}
//regist dlg on hide event
function clearHistoryValue() {
	//var dlg = dijit.byId("findandreplaceparameterDlg");
 
	//var onHideEventHandler = dojo.connect(dlg, 'onHide', function() {
	//	alert("ttt");
		
		var findValue = dijit.byId('findandreplaceparameter_Find_Value');
		var replaceWidth = dijit.byId('findandreplaceparameter_Replace_with');
		var resultGrid = dijit.byId('findandreplaceparameter_Search_Result_grid');
		if(null!=findValue){
			findValue.set('value','');
		}
		if(null!=replaceWidth){
			replaceWidth.set('value','');
		}
		if(null!=resultGrid){
			 resultGrid.setStore(new dojo.data.ItemFileReadStore({
			    	data:{
				  	  identifier:"flowNameoperName",
				  	  items:[]
				      }
		     }));
			 if(resultGrid.selection !=null){
				 resultGrid.selection.deselectAll();
			 }
		}
		   
		  // dojo.disconnect(onHideEventHandler);
	//});

}

function hideFindAndReplaceDlg(){
	var dlg = dijit.byId("findandreplaceparameterDlg");
	if(null!=dlg){
	   dlg.hide();
	}
}
/*Find results*/
function execFindAndReplaceQuery(){
	
	if(validateFindAndReplaceQuery("query")==false){
	   return ;
	}
	
	var findValue = dijit.byId('findandreplaceparameter_Find_Value');
	var parameter = dijit.byId('findandreplaceparameter_Parameter_Name');
	var searchParamObj = {};
	
	searchParamObj.paramterName = parameter.get("value");
	searchParamObj.parameterValue = findValue.get("value");
	searchParamObj.flowInfo = workFlowManager.getEditingFlow();
	//findandreplaceparameter_Current_flow
	//findandreplaceparameter_All_flows
	if(dojo.byId("findandreplaceparameter_Current_flow").checked==true){
		searchParamObj.searchScope = dojo.byId("findandreplaceparameter_Current_flow").value;
	}
	if(dojo.byId("findandreplaceparameter_All_flows").checked==true){
		searchParamObj.searchScope =dojo.byId("findandreplaceparameter_All_flows").value;
	}
	searchParamObj.ignoreCase = !dijit.byId("findandreplaceparameter_IGNORE_CASE").get("checked");
	
	parameterManager.getFindAndReplaceQueryResult(searchParamObj,getFindAndReplaceQueryResult_SUCCESS_CALLBACK);
	
	
}
/*Validate find and replace value whether validate return boolean*/
function validateFindAndReplaceQuery(type){
	var findValue = dijit.byId('findandreplaceparameter_Find_Value');
	var parameter = dijit.byId('findandreplaceparameter_Parameter_Name');
	if(null==parameter){
	   return false;
	}
	if(findValue==null){
	  return false;	
	}
	if(parameter.get('value') ==null || parameter.get('value')==""){
		popupComponent.alert(alpine.nls.Parameter_Name_alert_msg );
		return false;
	}
	var pattern = /^[@|a-zA-Z_]\w*/;
	if(findValue.get('value')==null || findValue.get('value')==""){
		popupComponent.alert(alpine.nls.Find_Value_alert_msg);
		return false;
	}else{
		if(findValue.get('value')!="*" && pattern.test(findValue.get('value'))==false){
			popupComponent.alert(alpine.nls.Find_Value_alert_msg);
			return false;
		}
	}
	if(type!=null && type=="replace"){
		var replaceWidth = dijit.byId('findandreplaceparameter_Replace_with');
		if(replaceWidth==null){
			return false;
		}
		if(replaceWidth.get('value')==null || replaceWidth.get('value')=="" ){
			popupComponent.alert(alpine.nls.Replace_with_alert_msg);
			return false;
		}else{
			if(pattern.test(replaceWidth.get('value'))==false){
				popupComponent.alert(alpine.nls.Replace_with_alert_msg);
				return false;
			}
		}
		var resultGrid = dijit.byId('findandreplaceparameter_Search_Result_grid');
		if(null==resultGrid){
		   return false;
		}
		  var selectedArray =  resultGrid.selection.getSelected();
		  //MINERWEB-1057
		 // var selectedStatusArray = resultGrid.selection.selected;
		 // var storeArray  = resultGrid.store._arrayOfAllItems;
		if(null==selectedArray || selectedArray.length==0){
			popupComponent.alert(alpine.nls.Parameter_replace_alert_tip);
			return false;
		}
		var storeArray  = resultGrid.store._arrayOfAllItems;
	  
		if(null==storeArray){
			  popupComponent.alert(alpine.nls.Parameter_replace_alert_tip);
			  return false;
		}
		if(null!=storeArray && storeArray.length==0){
			  popupComponent.alert(alpine.nls.Parameter_replace_alert_tip);
			  return false;
		}
	}
	return true;
}

function getFindAndReplaceQueryResult_SUCCESS_CALLBACK(data){
   var resultGrid = dijit.byId('findandreplaceparameter_Search_Result_grid');
  if(null!=data && data.length>0){
    if(null!=resultGrid){
    	var gridStore = resultGrid.store;
    	var newDataItem = [];
        dojo.forEach(data,function(itm,idx){
        	if(null!=itm){
        		newDataItem.push({flowNameoperName:itm.flowName+"_"+itm.operatorName+idx,flowName:itm.flowName,operName:itm.operatorName,parameterName:itm.parameterName,parameterValue:itm.parameterValue});
        	}
        });
        resultGrid.setStore(new dojo.data.ItemFileReadStore({
	    	data:{
		  	  identifier:"flowNameoperName",
		  	  items:newDataItem
		      }
       }));
        if(resultGrid.selection!=null){
        	resultGrid.selection.deselectAll();
        }
        
        _setResultToolTip(data.length);

    	resultGrid.render();
    }
  }else{
	  resultGrid.setStore(new dojo.data.ItemFileReadStore({
	    	data:{
		  	  identifier:"flowNameoperName",
		  	  items:[]
		      }
     }));
	  resultGrid.render();
	  _setResultToolTip(0);
	  //popupComponent.alert(alpine.nls.Search_Result_NORESULT);
  }
}

function _setResultToolTip(number){
    var resultTip = dojo.byId("findandreplaceparameter_Search_Result_Tip");
    if(resultTip!=null){
    	popupComponent.alert(alpine.nls.Search_Result_Tip.replace("##",number));
    	resultTip.innerHTML =alpine.nls.Search_Result_Lable_Tip.replace("##",number) ;
    }
}

function _clearResultToolTip(){
	   var resultTip = dojo.byId("findandreplaceparameter_Search_Result_Tip");
	    if(resultTip!=null){
	    	resultTip.innerHTML ="";
	    }
}
/*Replace value*/
function execReplaceSelectedFindResults(){
	if(validateFindAndReplaceQuery("replace")==false){
		return ;
	}
	// validate replace value is exist
	var replaceWidth = dijit.byId('findandreplaceparameter_Replace_with');
	var parameter = dijit.byId('findandreplaceparameter_Parameter_Name');
	var searchParamObj = {};
	
	var replaceValue = replaceWidth.get("value");
	var findValue = dijit.byId('findandreplaceparameter_Find_Value').get("value");
	
	searchParamObj.paramterName = parameter.get("value");
	searchParamObj.parameterValue = replaceWidth.get("value");
	searchParamObj.flowInfo = workFlowManager.getEditingFlow();
	if(dojo.byId("findandreplaceparameter_Current_flow").checked==true){
		searchParamObj.searchScope = dojo.byId("findandreplaceparameter_Current_flow").value;
	}
	if(dojo.byId("findandreplaceparameter_All_flows").checked==true){
		searchParamObj.searchScope =dojo.byId("findandreplaceparameter_All_flows").value;
	}
	searchParamObj.ignoreCase = !dijit.byId("findandreplaceparameter_IGNORE_CASE").get("checked");
	
	
	
	var canReplace = false;
	// validate replace value is exist for connection  
	if(searchParamObj.searchScope == "all"){
		//scope=all
		if (null != searchParamObj.paramterName
				&& searchParamObj.paramterName == "dbConnectionName") {
			if (null != FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
				canReplace = _propertyInObject(
						FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ,
						replaceValue);
			}
		}
			
		//validate replace value is exist for schema
		 if (null != searchParamObj.paramterName
				&& searchParamObj.paramterName == "schemaName") {
			if (null != FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
				for ( var connName in FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
					if (null != connName && FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName]!=null) {
                         if(_propertyInObject(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName],replaceValue)==true){
                        	 canReplace = true;
                        	 break;
                         }
					} else {
						continue;
					}

				}
			}
		}
		
		//validate replace for outputschema
		 if (null != searchParamObj.paramterName
				&& searchParamObj.paramterName == "outputSchema"
				&& replaceValue == "@default_schema") {
			canReplace = true;
		} else if (null != searchParamObj.paramterName
				&& searchParamObj.paramterName == "outputSchema"
				&& replaceValue != "@default_schema") {
			
			if (null != FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
				for ( var connName in FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
					if (null != connName && FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName]!=null) {
                         if(_propertyInObject(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName],replaceValue)==true){
                        	 canReplace = true;
                        	 break;
                         }
					} else {
						continue;
					}

				}
			}
		}
		 
		//validate replace for tablename
		 if (null != searchParamObj.paramterName
				&& searchParamObj.paramterName == "tableName") {
			if (null != FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
				var breakConn = false;
				for ( var connName in FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
					if(breakConn==true){
						break;
					}
					if (null != connName
							&& FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName] != null) {
						for(var schemaName in FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName]){
							if(schemaName!=null && FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName][schemaName]!=null){
								if(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName][schemaName].length>0){
									if(_tableNameIN(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[connName][schemaName],replaceValue)==true){
										canReplace = true;
										breakConn = true;
										break;
									}
								}
							}
						}
						
					} 
				}
			}
		}
		
	}else{
		// current flow validate
		//get current grid select value
		var selectedOPNames = getSelectedOperatorNames();
		
		//get this info according to(find result info operator name) 
		var selectedOperatorsInfos= [];
		var selectedOperatorsDBInfs = [];
		
		if(null!=selectedOPNames && selectedOPNames.length>0){
			for ( var j = 0; j < selectedOPNames.length; j++) {
				selectedOperatorsInfos.push(getOperInfoByOperatorName(selectedOPNames[j]));
			}
			for ( var k = 0; k < selectedOperatorsInfos.length; k++) {
				selectedOperatorsDBInfs.push(getDBInfo(selectedOperatorsInfos[k])); //getDBInfo() in dataexplorer.js 
			}
			
		}
		
		// connection validate
		if (null != searchParamObj.paramterName
				&& searchParamObj.paramterName == "dbConnectionName") {
			if (null != FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ) {
				canReplace = _propertyInObject(
						FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ,
						replaceValue);
			}
		}
		
	   // schema validate
		 if (null != searchParamObj.paramterName
					&& searchParamObj.paramterName == "schemaName") {
			 if(null!=selectedOperatorsDBInfs && selectedOperatorsDBInfs.length>0){
				 var schemaInConn = true;
				 for ( var i = 0; i < selectedOperatorsDBInfs.length; i++) {
					var dbinfo = selectedOperatorsDBInfs[i];
					if(null!=dbinfo && dbinfo.connection!=null){
						var db_conn = dbinfo.connection;
						schemaInConn = schemaInConn && _propertyInObject(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[db_conn],replaceValue);
					}
					
				}
				 canReplace = schemaInConn;
			 }
		 }
	   // output schema validate
		 if (null != searchParamObj.paramterName
					&& searchParamObj.paramterName == "outputSchema"
					&& replaceValue == "@default_schema") {
				canReplace = true;
			} else if (null != searchParamObj.paramterName
					&& searchParamObj.paramterName == "outputSchema"
					&& replaceValue != "@default_schema") {
				
				 if(null!=selectedOperatorsDBInfs && selectedOperatorsDBInfs.length>0){
					 var schemaInConn = true;
					 for ( var i = 0; i < selectedOperatorsDBInfs.length; i++) {
						var dbinfo = selectedOperatorsDBInfs[i];
						if(null!=dbinfo && dbinfo.connection!=null){
							var db_conn = dbinfo.connection;
							schemaInConn = schemaInConn && _propertyInObject(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[db_conn],replaceValue);
						}
						
					}
					 canReplace = schemaInConn;
				 }
			}
	   //tablename validate
		 if (null != searchParamObj.paramterName
					&& searchParamObj.paramterName == "tableName") {
			 if(null!=selectedOperatorsDBInfs && selectedOperatorsDBInfs.length>0){
				 //connection schema
				 var tableInSchema = true;
				 for ( var i = 0; i < selectedOperatorsDBInfs.length; i++) {
					 var dbinfo = selectedOperatorsDBInfs[i];
						if(null!=dbinfo && dbinfo.connection!=null && dbinfo.schema!=null){
							var db_conn = dbinfo.connection;
							var db_schema = dbinfo.schema;
							if(null!=FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[db_conn] && null!=FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[db_conn][db_schema]){
								tableInSchema =tableInSchema && _tableNameIN(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[db_conn][db_schema],replaceValue);
								if(tableInSchema==false){
									break;
								}
							}else{
								tableInSchema = false;
								break;
							}
							//schemaInConn = schemaInConn && _propertyInObject(FINEANDREPLACE_CONNECTION_SCHEMA_TABLE_MAP_OBJ[db_conn],replaceValue);
						} 
				 }
				 canReplace = tableInSchema;
			 }
		 }
		
	}

	

	if(canReplace==true){
		replaceValueCanReplace();
	}else{
		   popupComponent.alert(alpine.nls.Parameter_replaceValue_not_exist);
	}
}
/*For dbconnection schema tablelist map*/
function _propertyInObject(obj,propName){
	var status = false;
	if(null!=obj){
		for(var pName in obj){
			if(null!=pName && pName==propName){
				status = true;
				break;
			}else{
				continue;
			}
		}
		
	}
	return status;
}

/*Table name whether in tableNmaeList(array)*/
function _tableNameIN(tableNameList,tableName){
	var status = false;
	if(null!=tableNameList && tableNameList.length>0){
		for ( var i = 0; i < tableNameList.length; i++) {
			var array_element = tableNameList[i];
			if(array_element==tableName){
				status = true;
				break;
			}
		}
		
	}
	return status;
}


/*If Validate success then replace value*/
function replaceValueCanReplace(){
	//
	var grid = dijit.byId("findandreplaceparameter_Search_Result_grid");
	if(grid!=null){
		var replaceWidth = dijit.byId('findandreplaceparameter_Replace_with');
		var selectedStatusArray = grid.selection.selected;
		var storeArray  = grid.store._arrayOfAllItems;
		//struct same as java object FindAndReplaceParamSearchObj
		var replaceParamObj = {};
       	replaceParamObj.replaceList = [];
       	//replaceParamObj.replacePathMap = {};
    	replaceParamObj.replaceValue = replaceWidth.get("value");
    	replaceParamObj.flowInfo = workFlowManager.getEditingFlow();
    	
    	if(dojo.byId("findandreplaceparameter_Current_flow").checked==true){
    		replaceParamObj.searchScope = dojo.byId("findandreplaceparameter_Current_flow").value;
    	}
    	if(dojo.byId("findandreplaceparameter_All_flows").checked==true){
    		replaceParamObj.searchScope =dojo.byId("findandreplaceparameter_All_flows").value;
    	}
    	
    	//
    	//var selectedItem = grid.selection.getSelected();
     	
		for(var i=0;i<selectedStatusArray.length;i++){
		        if(selectedStatusArray[i]==true){
		        	var pathValue = toolkit.getValue(storeArray[i].flowName);
		        	var kValue = toolkit.getValue(storeArray[i].operName);
		        	var vValue = toolkit.getValue(storeArray[i].parameterName);
		        	var replaceObj = {};
		        	replaceObj.flowPath = pathValue;
		        	replaceObj.operName = kValue;
		        	replaceObj.parameterName = vValue;
		        	
		        	replaceParamObj.replaceList.push(replaceObj);
		        	
		        	//replaceParamObj.replacePathMap[pathValue] = kValue;
		        	//replaceParamObj.replaceMap[kValue]=vValue;
		        }
		}
		
		parameterManager.replaceValueCanReplace_done(replaceParamObj,replaceValueCanReplace_SuccessCallBack);
          
	}
}

function replaceValueCanReplace_SuccessCallBack(msg){
	popupComponent.alert(msg.message);
    //clear query result
	var resultGrid = dijit.byId("findandreplaceparameter_Search_Result_grid");
	if(null!=resultGrid){
		resultGrid.setStore(new dojo.data.ItemFileReadStore({
			data:{
			identifier:"flowNameoperName",
			items:[]
		}
		}));
		resultGrid.render();
		
		//clearHistoryValue();
		
		_clearResultToolTip();
		if(workFlowManager.isEditing()){
			validateReplaceOperator(msg.replaceNum);
		}
	}
}

/*Validate current open flow whether need update status*/
function validateReplaceOperator(updateNumber){
	var url = alpine.baseURL+"/main/findAndReplace.do?method=validateReplaceValueOperator";
	ds.post(url,workFlowManager.getEditingFlow(),
				function(data){
		           	for(var key in data){
		           		operatorManagement.forEachOperatorInfo(function(operatorInfo){
		           			if(operatorInfo.uid == key){
		           				operatorInfo.isValid = data[key];
		           			}
		           		});
		           		alpine.flow.OperatorManagementUIHelper.validateOperators([{
		           			uid: key,
		           			isValid: data[key]
		           		}]);
					}
		           	if(updateNumber>0){
		           		alpine.flow.WorkFlowUIHelper.setDirty(true);
		           	}
	            });
}
/*Select the value of operator name from search result grid list*/
function getSelectedOperatorNames(){
	var operNames = [];
	var grid = dijit.byId("findandreplaceparameter_Search_Result_grid");
	if(grid!=null && grid.store!=null){
		var selectedStatusArray = grid.selection.selected;
		var storeArray  = grid.store._arrayOfAllItems;
		for(var i=0;i<selectedStatusArray.length;i++){
	        if(selectedStatusArray[i]==true){
	        	operNames.push(storeArray[i].operName);
	        }
	    }
	}
	
	return operNames;
}
/*Get operator infos when operator name equals opName*/
function getOperInfoByOperatorName(opName){
	var operatorUid = operatorManagement.getOperatorUidByName(opName);
	return operatorManagement.getOperatorPrimaryInfo(operatorUid);
}

/*Clean search condition for scope changed*/
function changeScope(){
	var scopeAll = dijit.byId('findandreplaceparameter_All_flows');
	var scopeCurrent = dijit.byId('findandreplaceparameter_Current_flow');
	clearHistoryValue();
	dojo.byId('findandreplaceparameter_Search_Result_Tip').innerHTML = "";
}
/*Initialization scope radio button status*/
function initRadioSelect(){
	var current = dijit.byId('findandreplaceparameter_Current_flow');
	var all = dijit.byId('findandreplaceparameter_All_flows');
	if(!workFlowManager.isEditing()){
		//checked="checked"
		current.set('disabled',true);
		all.set('checked',true);
		current.set('checked',false);
	}else{
		current.set('disabled',false);
		current.set('checked',true);
		all.set('checked',false);
	}
}
});
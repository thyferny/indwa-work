/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * plda.js
 * 
 * Author Will
 * Version 3.0
 * Date 2012-3-29
 */

//Add by Will needShowDisplayProperty(prop)  whether to display dicIndexColumn
function needShowDisplayProperty(prop){
	if(CurrentOperatorDTO!=null && CurrentOperatorDTO.inputTableInfos!=null 
		&& CurrentOperatorDTO.inputTableInfos.length==1 
		&& prop!=null 
		&& prop.name!=null 
		&& prop.name=='dicIndexColumn'){
	   var currentDBType = CurrentOperatorDTO.inputTableInfos[0].system;
	   if(null!=currentDBType 
			   && (currentDBType==alpine.DATABASETYPE_ORACLE 
					   || currentDBType==alpine.DATABASETYPE_GREENPLUM
					   || currentDBType==alpine.DATABASETYPE_POSTGRESQL)){
			// alpine.DATABASETYPE_ORACLE = "Oracle";alpine.DATABASETYPE_GREENPLUM = "Greenplum";alpine.DATABASETYPE_POSTGRESQL = "PostgreSQL";
		   return false;
	   }
	}
	return true;
}

function update_table_list4PLDA(schema,conn,callBackFunc){
	if(!schema || schema == null || schema == ""){
		return;
	}
	var url = baseURL + "/main/dataSource/explorer.do?method=getTableViewNames"
	 + "&user=" + login
	 + "&conn=" + conn
	 + "&schema=" + schema
	 + "&resourceType=" + alpine.flow.WorkFlowManager.getEditingFlow().type;
	//progressBar.showLoadingBar();
	ds.get(url, callBackFunc, property_error_callback, false, DIALOG_ID);
}

function update_table_list4PLDA_callback(list){
	//progressBar.closeLoadingBar();
	//console.log(list);
	var id= "dictionaryTable"+ID_TAG;
	var fsel = dijit.byId(id);
	if (fsel == null) {
		return;
	}
	var store = make_store(list); //In property.js 
	update_single_select_store4PLDA(id, store);
}
function update_single_select_store4PLDA(id, store) {
	var fsel = dijit.byId(id);
	fsel.set("value", "");
	fsel.set("store", store);
	fsel.startup();
	var dicIndexColumn = dijit.byId("dicIndexColumn"+ID_TAG);
	if(null!=dicIndexColumn){
		dicIndexColumn.set("value","");
	}
	var dicContentColumn = dijit.byId("dicContentColumn"+ID_TAG);
	if(null!=dicContentColumn){
		dicContentColumn.set("value","");
	}
}
function update_columns4PLDADicIndexColumn(connnectName,schemaName,tableName,update_columns4PLDA_callBack){
	update_columns4PLDA(connnectName,schemaName,tableName,update_columns4PLDA_callBack ,"int");	
}
function update_columns4PLDADicContentColumn(connnectName,schemaName,tableName,update_columns4PLDA_callBack){
	 var currentDBType = CurrentOperatorDTO.inputTableInfos[0].system;
	   if(null!=currentDBType 
			   && (currentDBType==alpine.DATABASETYPE_ORACLE 
					   || currentDBType==alpine.DATABASETYPE_GREENPLUM
					   || currentDBType==alpine.DATABASETYPE_POSTGRESQL)){
		   update_columns4PLDA(connnectName,schemaName,tableName,update_columns4PLDA_callBack ,"array");
	   }else{
		   update_columns4PLDA(connnectName,schemaName,tableName,update_columns4PLDA_callBack ,"all");
	   }
}

function update_columns4PLDA(connName,schemaName,tableName,callBackFunc,dataType){
	
	if(null==connName || ""==connName){
	  return;
	}
	if(null==schemaName || ""==schemaName){
		return;
	}
	if(null==tableName || ""==tableName){
		return;
	}	
    // number cate all 
	//1  dict index ->int
	//2 doc content -> (pG GP oracle :array) else int
	//3 contect docindex cxolumn  (can notbe array,double float ) // int_cate_time
	var url = baseURL + "/main/dataexplorer.do?method=getColumnNamesWithType"
	+ "&dbConnName="+connName
	+ "&dbSchemaName="+schemaName 
	+ "&dbTableName="+tableName
	+"&columnType="+dataType
	+"&resourceType="+alpine.flow.WorkFlowManager.getEditingFlow().type+"&isGeneratedTable=false";
	//progressBar.showLoadingBar();
	ds.get(url,callBackFunc,property_error_callback, false, DIALOG_ID);
}

function update_columns4PLDA_callBack(columns){
	//progressBar.closeLoadingBar();
	
	if(!columns){
		return;
	}
	if (columns.error_code&&columns.error_code!=0) {
		if (columns.error_code == -1) {
			popupComponent.alert(alpine.nls.no_login, function(){
				window.top.location.pathname = loginURL;	
			});
			return;
		}
		else {
			var msg = "";
			if (list.message) {
				msg = columns.message;
			}
			popupComponent.alert(msg);
			 
			return;
		}			
	}
	//dicContentColumn PLDAModelOutputSchema
	var pLDAModelOutputSchema =dijit.byId("dicIndexColumn"+ID_TAG);
	var dicContentColumn =dijit.byId("dicContentColumn"+ID_TAG);
	
	var store = make_store(columns);
	if(null!=pLDAModelOutputSchema){
		update_single_select_store("dicIndexColumn"+ID_TAG, store); //Function in property.js
	}
	if(null!=dicContentColumn){
		update_single_select_store("dicContentColumn"+ID_TAG, store);
	}
	
}

function validatePLDARelationship(propertyList){
	
	if(dijit.byId('contentDocIndexColumn'+ID_TAG)!=null && dijit.byId('contentWordColumn'+ID_TAG)!=null){
	  if(dijit.byId('contentDocIndexColumn'+ID_TAG).value==dijit.byId('contentWordColumn'+ID_TAG).value){
		  popupComponent.alert(alpine.nls.plda_column_same);
		  return false;
	  }
	}
	 
	  if(dijit.byId('dicIndexColumn'+ID_TAG)!=null && dijit.byId('dicContentColumn'+ID_TAG)!=null){
		  if(dijit.byId('dicIndexColumn'+ID_TAG).value==dijit.byId('dicContentColumn'+ID_TAG).value){
			  popupComponent.alert(alpine.nls.plda_column_same);
			  return false;
		  }
	  }
	  //PLDAModelOutputTable topicOutTable docTopicOutTable  LDAModelOutputSchema  topicOutSchema docTopicOutSchema
	  if(dijit.byId('PLDAModelOutputSchema'+ID_TAG).value==dijit.byId('topicOutSchema'+ID_TAG).value){
		  if(dijit.byId("PLDAModelOutputTable"+ID_TAG).value == dijit.byId("topicOutTable"+ID_TAG).value){
			  popupComponent.alert(alpine.nls.plda_output_table_same);
			  return false;
		  }
	  }
	  if(dijit.byId('PLDAModelOutputSchema'+ID_TAG).value==dijit.byId('docTopicOutSchema'+ID_TAG).value){
		  if(dijit.byId("PLDAModelOutputTable"+ID_TAG).value == dijit.byId("docTopicOutTable"+ID_TAG).value){
			  popupComponent.alert(alpine.nls.plda_output_table_same);
			  return false;
		  }
	  }
	  if(dijit.byId('topicOutSchema'+ID_TAG).value==dijit.byId('docTopicOutSchema'+ID_TAG).value){
		  if(dijit.byId("topicOutTable"+ID_TAG).value == dijit.byId("docTopicOutTable"+ID_TAG).value){
			  popupComponent.alert(alpine.nls.plda_output_table_same);
			  return false;
		  }
	  }
	  	  
	return true;
}
function validatePLDAPredictRelationship(){
	 if(dijit.byId('outputSchema'+ID_TAG).value==dijit.byId('PLDADocTopicOutputSchema'+ID_TAG).value){
		  if(dijit.byId("outputTable"+ID_TAG).value == dijit.byId("PLDADocTopicOutputTable"+ID_TAG).value){
			  popupComponent.alert(alpine.nls.plda_output_table_same);
			  return false;
		  }
	  }
	return true;
}
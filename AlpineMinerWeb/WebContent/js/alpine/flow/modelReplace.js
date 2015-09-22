

var modelListTable = null; 
var modelListStore;
 
var crrentFlowName;
var crrentModelName;
var crrentModelType;

var modelListDialog_ID="modelReplaceDialog"; 
var modelListTable_ID ="modelListTable" ;
var canvas_ID = "FlowDisplayPanelPersonal";

// modelList is from REST service...
function initModelListTable( modelList ) {
	if(!modelList||modelList.lengt==0){
		popupComponent.alert(alpine.nls.model_not_found);
	}
	if(modelList.error_code){
		handle_error_result(modelList);
		return;
	}
	var fitlerOptions = new Array();
	var flowNames= new Array();
	// use a more friend data format
	for(var i=0;i<modelList.length;i++){
		var model=modelList[i];
		var timemills=model.createTime;
		model.createTime = alpine_format_date(new Date(timemills));
		var flowName = model.flowName;
		if(flowNames.indexOf(flowName)<0){
			flowNames.push(flowName);
		}		
	}
	for(var i=0;i<flowNames.length;i++){
		var flowName = flowNames[i];
		fitlerOptions.push(
				{
			        label: flowName,
			        value: flowName
			    }
					);
	}
	if(fitlerOptions.length>0){
		//show all 
		fitlerOptions.push(
				{
			        label: "*",
			        value: "*"
			    }
					);
	}
	dijit.byId("model_flowName_filter").set("options",fitlerOptions);
	dijit.byId("model_flowName_filter").startup();
	dijit.byId("model_flowName_filter").attr("value","*");
	openModelReplaceDialog();
 
	var dataTable = {
		items : modelList
	};
	// our test data store for this example:
	modelListStore = new dojo.data.ItemFileWriteStore({
		data : dataTable
	});

	
	if(modelListTable==null){		
		modelListTable = dijit.byId(modelListTable_ID);
		dojo.connect(modelListTable, "onRowClick", select_flow_model); 
	}
	
	 
	// this will make the edit ok
	modelListTable.setStore(modelListStore);
	
	// Call startup, in order to render the grid:
	modelListTable.render();
	
	if(modelListStore._arrayOfTopLevelItems.length >0){
		modelListTable.selection.select(0);	
		modelListTable.updateRow(0);
	}else{
		modelListTable.selection.deselectAll();
	}
	select_flow_model();
	
	filter_model_table("*");
	current_model_filter ="*" ;
	


	
//	options: [{
//        label: 'TN',
//        value: 'Tennessee'
//    },
    
	
	//progressBar.closeLoadingBar();

}

function table_item_to_model_info(item ){
	var info = {};
	info.version=item.version[0];
	if(item.comments){
		info.comments=item.comments[0];
	}
	info.id = item.id[0];
	
	info.createUser = item.createUser[0];
	info.modifiedUser = item.modifiedUser[0];
	if(item.groupName) {
		info.groupName = item.groupName[0];
	}
	info.type = item.type[0];
 
	//avoid the json parse problem...
	info.createTime = 0;
	info.modifiedTime = 0;

	
	info.modelName = item.modelName[0];	
	info.algorithmName = item.algorithmName[0];
	info.flowName = item.flowName[0];
	
	return info;
}
function perform_delete_model( ){
	// item is modelInfo
		var items = modelListTable.selection.getSelected();
		if(!items||items.length==0){
			popupComponent.alert(alpine.nls.please_select_model);
		}else{
			var modelInfos = new Array();
			for ( var x = 0; x < items.length; x++) {
				var modelInfo= items[x];
				modelListStore.deleteItem(modelInfo);
				modelInfos.push(table_item_to_model_info(modelInfo));
	
			}
			var requestUrl = baseURL + "/main/model.do?method=deleteModelList";
			ds.post(requestUrl,modelInfos, deleteModelCallBack,null, false, canvas_ID);
			
			
		}
  } 
  
function deleteModelCallBack(obj){
	if(obj.error_code){
		handle_error_result(obj) ;
		return;
	}
	//progressBar.closeLoadingBar();
	modelListTable.setStore(modelListStore);
	if(current_model_filter){
		modelListTable.filter({    flowName: current_model_filter });
	}
	
	if(modelListStore._arrayOfTopLevelItems.length >0){
		modelListTable.selection.select(0);	
		modelListTable.updateRow(0);
	}else{
		modelListTable.selection.deselectAll();
	}
	select_flow_model();
 
}

var need_mode_replace =false ;

function showModelReplaceDialog(categories, flowName, modelName,needReplace,modelType) {
	//progressBar.showLoadingBar();
	need_mode_replace=needReplace;
	var requestUrl = baseURL + "/main/model.do?method=getModelList"
			+ "&flowName=" + flowName + "&categories=" + categories + "&modelName=" + modelName;
	crrentFlowName=flowName;
	crrentModelName=modelName;
	crrentModelType = modelType ;
	ds.get(requestUrl, initModelListTable, null, false, canvas_ID);

}

function openModelReplaceDialog(  ){

	dijit.byId(modelListDialog_ID).show();
	dojo.html.set(dojo.byId("download_model_label"), "");	
//	dijit.byId(modelListDialog_ID).resize(300, 301);
}
//var need_reopen_flow_after_replace =false;
function replaceModelCallback(data){ 
	 
	//progressBar.closeLoadingBar();
	
	if(data.error_code){
		handle_error_result(data);
		return ;
	}
	dijit.byId(modelListDialog_ID).hide();
	//make sure the server will reload it when run.
	alpine.flow.WorkFlowManager.getEditingFlow().tmpPath="";
	//data is a flow info
	var modelOperator = 	alpine.flow.OperatorManagementUIHelper.getSelectedOperator();
	modelOperator.isValid = true;
	
	alpine.flow.OperatorManagementUIHelper.validateOperators([modelOperator]);
	
}

function perform_replace_model() {
 
	// item is modelInfo
	var items = modelListTable.selection.getSelected();
	if(!items||items.length==0){
		popupComponent.alert(alpine.nls.please_select_model);
	}else{
		var modelInfo= items[0];
 
		 
//		var createTime=modelInfo.createTime[0];
//		modelInfo.createTime[0]=createTime.getTime();
		modelInfo = table_item_to_model_info(modelInfo) ;
		var flowInfo = alpine.flow.WorkFlowManager.getEditingFlow();
		var requestUrl = baseURL + "/main/model.do?method=replaceModel" + "&modelName=" + crrentModelName;
		
		//progressBar.showLoadingBar();
		ds.post(requestUrl,{
			flowInfo: flowInfo,
			modelInfo: modelInfo
		}, replaceModelCallback,null, false, canvas_ID);
		
	}
	 
	
}
 

function select_flow_model(event){
	//add by Will begin
	var items=[];
	if(event&&event.rowIndex==undefined){
		var currentRowIndex = modelListTable.focus.rowIndex;		
		items[0] = modelListTable.getItem(currentRowIndex);
		modelListTable.selection.select(currentRowIndex);
	}else{
		//event is no use now
		items = modelListTable.selection.getSelected();
	}
	// add by Will end
	//only download one by one...
	if(items&&items.length==1){
		 
		 dijit.byId("model_download_id").set("disabled",false);
		 dijit.byId("model_view_id").set("disabled",false);
		 var item = items[0];
		 var modelType = item.algorithmName[0];
		 if(isModelTypeSame(modelType)==true){
			 dijit.byId("model_replace_ok_id").set("disabled",false);
		 }else{ //difference model ,can not replaced ,will cause error
			 dijit.byId("model_replace_ok_id").set("disabled",true);
		 }
		 
		 
	}else{
		dijit.byId("model_download_id").set("disabled",true);
		 dijit.byId("model_replace_ok_id").set("disabled",true);
		 dijit.byId("model_view_id").set("disabled",true);
		 
	}

	//multiple selected
	if(items&&items.length>0){
		 dijit.byId("model_replace_delete_id").set("disabled",false);
	 }else{
		 dijit.byId("model_replace_delete_id").set("disabled",true);
	 }
	//model can not be replaced...
	if(need_mode_replace ==false){
		 dijit.byId("model_replace_ok_id").set("disabled",true);
	}
	
	function isModelTypeSame(modelType){
		if(!crrentModelType){
			return true;
		}else{
			return (modelType==crrentModelType);
		}
	}
 
}

function perform_download_model() {
	 
	// item is modelInfo
	var items = modelListTable.selection.getSelected();
	if(!items||items.length==0){
		popupComponent.alert(alpine.nls.please_select_model);
	}else{
		var item = items[0];

		var modelInfo = table_item_to_model_info (item) ;
		var requestUrl = baseURL + "/main/model.do?method=exportModel" ;
		//progressBar.showLoadingBar();
		ds.post(requestUrl,modelInfo, downLoadModel_callback,null, false, canvas_ID);
		
	}
	 
	
}


var modelInfoForView = null;
function perform_view_model() {
	 
	// item is modelInfo
	var items = modelListTable.selection.getSelected();
	if(!items||items.length==0){
		popupComponent.alert(alpine.nls.please_select_model);
	}else{
		var item = items[0];

		var modelInfo = table_item_to_model_info (item) ;
		modelInfoForView = modelInfo;
		var resultWindow = window.open(baseURL + "/alpine/result/viewModel.jsp", '_blank', get_open_window_options());
		resultWindow.focus();
		resultWindow=null;
	 
	}
}

var current_model_filter = null;
function filter_model_table(newValue){
	current_model_filter=newValue;
	
	
	
	
	modelListTable.filter({    flowName: newValue });
	//clear the select status...
	
	modelListTable.selection.deselectAll();
	select_flow_model();
}

function downLoadModel_callback(url){
	//progressBar.closeLoadingBar();
	if(url.error_code){
		handle_error_result(url);
		return ;
	}
	//Add by Will
	var download_url = baseURL + "/temp_model/"+login+"/" + url;
	var servlet_url = baseURL+"/CommonFileDownLoaderServlet?downloadFileName="+url+"&tempType=temp_model&filePath=/"+login+"/";
	window.location.href = servlet_url;
}
 
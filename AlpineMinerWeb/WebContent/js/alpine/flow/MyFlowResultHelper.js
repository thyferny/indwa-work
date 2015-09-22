define(["alpine/flow/MyFlowResultManager",
        "alpine/flow/WorkFlowManager"],function(myFlowResultMgmt, workflowManager){
	
	var resultListTable; 
	var resultListStore;
	
	var flow_ResultListDialog_ID="flowResultsDialog";  
	var flow_ResultListTable_ID ="flowResultsTable" ; 
	
	dojo.ready(function(){
		dojo.connect(dijit.byId("flowResultsTable"), "onRowClick", dijit.byId("flowResultsTable"), select_flow_result);
	    dojo.connect(dijit.byId('my_result_button'),"onClick",showFlowResultsDialog);
	    dojo.connect(dijit.byId('flow_result_delete_id'),"onClick",perform_delete_flow_result);
	    dojo.connect(dijit.byId('model_myflowResult_name_filter'),"onChange",function(){filterModelMyflowResultByName(this.value);});
	    dojo.connect(dijit.byId('flow_result_open_id'),"onClick",function(){
	    	return perform_open_flow_result();
	    });
	});
	
	function _buildInitFilter(){
		var editingFlow = workflowManager.getEditingFlow();
		return editingFlow == null ? "*" : editingFlow.id;
	}
	
// modelList is from REST service...
	function initResultListTable( resultList ) {
		var initFilter = _buildInitFilter();
		
		if(resultList&&resultList.error_code){
			handle_error_result(obj);
			return ;
		}
		
		if(!resultList||resultList.length==0){
			popupComponent.alert(alpine.nls.result_not_found);
			return ;
		}
		
		openFlowResultsDialog();

		var myFlowResultfilterOptions =  [];
		var myFlowNames = [];

		myFlowResultfilterOptions.push({
			label: "*",
			value: "*"
		});
		resultList.sort(function(objA,objB){
            if(objA.flowName>objB.flowName){
                return 1;
            }else if(objA.flowName<objB.flowName){
                return -1;
            }else{
                if(objA.version>objB.version){
                    return 1;
                }else if(objA.version<objB.version){
                    return -1;
                }else{
                    return 0;
                }
            }

        });
		for(var i=0;i<resultList.length;i++){
			var resultInfo=resultList[i];
			var timemills=resultInfo.startTime;
			resultInfo.startTime = alpine_format_date(new Date(timemills));
			timemills=resultInfo.endTime;
			resultInfo.endTime = alpine_format_date(new Date(timemills));
			if(myFlowNames.indexOf(resultList[i].flowName)<0){
				myFlowNames.push(resultList[i].flowName);
				myFlowResultfilterOptions.push({
					label:resultList[i].flowName,
					value:resultList[i].flowName
				});
			}
		}
		dijit.byId("model_myflowResult_name_filter").set("options",myFlowResultfilterOptions);
        dijit.byId("model_myflowResult_name_filter").set("maxHeight",100);
		dijit.byId("model_myflowResult_name_filter").startup();
		dijit.byId("model_myflowResult_name_filter").set("value",initFilter);
		
		var dataTable = {
				items : resultList
		};
		// our test data store for this example:
		resultListStore = new dojo.data.ItemFileWriteStore({
			data : dataTable
		});
		
		resultListTable = dijit.byId(flow_ResultListTable_ID);
		// this will make the edit ok
		resultListTable.setStore(resultListStore);
		
		// Call startup, in order to render the grid:
		resultListTable.render();
		if(resultListStore._arrayOfTopLevelItems.length >0){
			resultListTable.selection.select(0);	
		}else{
			resultListTable.selection.deselectAll();
		}
		//make sure the item is disabled
		filterModelMyflowResultByName(initFilter); 
		select_flow_result();
		
		
	}
	
//Add by will begin
	function filterModelMyflowResultByName(currentmyFlowName){
		
		resultListTable.selection.deselectAll();
		resultListTable.setStore(resultListStore);
		resultListTable.filter({flowName: dijit.byId("model_myflowResult_name_filter").getOptions(currentmyFlowName) == null ? "*" : currentmyFlowName });
		//disable show result btn
		dijit.byId("flow_result_open_id").set("disabled",true);
	}
//Add by will end
	
	
	function perform_delete_flow_result( ){
		// item is modelInfo
		var items = resultListTable.selection.getSelected();
		if(!items||items.length==0){
			popupComponent.alert(alpine.nls.select_rsult_first);
		}else{
			for ( var x = 0; x < items.length; x++) {
				var deleteFlowArray = new Array();
				for ( var x = 0; x < items.length; x++) {
					var resultInfo= items[x];
					//avoid json error
					resultInfo.startTime=0;
					resultInfo.endTime=0;
					
					resultListStore.deleteItem(resultInfo);
					deleteFlowArray.push({
						key: resultInfo.flowName,
						id: resultInfo.id
					});
				}
				myFlowResultMgmt.perform_delete_flow_result(deleteFlowArray,deleteFlowResultCallBack);
			}
		}
	} 
	
	function deleteFlowResultCallBack(obj){
		if(obj&&obj.error_code){
			handle_error_result(obj);
			return ;
		}
		//make sure the item is disabled
		resultListTable.setStore(resultListStore);
		if(resultListStore._arrayOfTopLevelItems.length >0){
			resultListTable.selection.select(0);	
		}else{
			resultListTable.selection.deselectAll();
		}
		var myflowName = dijit.byId("model_myflowResult_name_filter").get("value");
		filterModelMyflowResultByName(myflowName?myflowName:"*"); 
		select_flow_result();
	}
	
	function showFlowResultsDialog() { 
		myFlowResultMgmt.showFlowResultsDialog(initResultListTable);
	}
	
	function openFlowResultsDialog(){
        dijit.byId(flow_ResultListDialog_ID).titleBar.style.display = "none";
		dijit.byId(flow_ResultListDialog_ID).show();
		dijit.byId(flow_ResultListDialog_ID).resize(400, 321);
	}
	
//show a result page...
	function perform_open_flow_result() {
		
		// item is modelInfo
		var items = resultListTable.selection.getSelected();
		if(!items||items.length==0){
			popupComponent.alert(alpine.nls.select_rsult_first);
		}else{
			for(var i=0;i<items.length;i++){
				var resultInfo= items[i];
				//date, not sure why it becomes an array
				//use _blank to open a new window...
				if(resultInfo){
					var url = baseURL + "/alpine/result/flowResultView.jsp?uuid=" + resultInfo.id
					+"&flowName="+resultInfo.flowName	;
					url= encodeURI (url) ;
					var resultWindow = window.open(url	,'_blank', get_open_window_options());
					resultWindow.focus();
					resultWindow = null;
					
				}	
			}
			
		}
		
	}
	
	
	function select_flow_result(e){
		// add by Will begin
		var items = [];
		if(e && e.rowIndex==undefined){
			var currentRowIndex = resultListTable.focus.rowIndex;		
			items[0] = resultListTable.getItem(currentRowIndex);
			resultListTable.selection.select(currentRowIndex);
		}else{
			items = resultListTable.selection.getSelected();
		}
		// add by Will end
		if(items && items[0] && items.length==1){//&&items.length==1
			dijit.byId("flow_result_delete_id").set("disabled",false);
			dijit.byId("flow_result_open_id").set("disabled",false);
			
		}
		
		else{
			dijit.byId("flow_result_delete_id").set("disabled",false);
			dijit.byId("flow_result_open_id").set("disabled",true);
			
			
		}
		
		
	}
});


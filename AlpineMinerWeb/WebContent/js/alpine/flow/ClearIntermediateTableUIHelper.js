/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * ClearIntermediateTableUIHelper
 * Author Gary
 */
define(["alpine/flow/ClearIntermediateTableManager", 
        "alpine/system/PreferenceManager",
        "alpine/flow/OperatorManagementManager",
        "alpine/flow/WorkFlowManager", 
        "alpine/flow/WorkFlowVariableReplacer",
        "alpine/datasourcemgr/DataSourceHadoopManager",
        "alpine/spinner"], function(clearIntermediateTableManager, preferenceManager, operatorManagement, workFlowManager, variableReplacer, hadoopDsMgr, spinner){

	var constants = {
		DIALOG: "alpine_flow_clearintermediatetable_dialog",
		GRID: "alpine_flow_clearintermediatetable_grid",
		BUTTON_SUBMIT: "alpine_flow_clearintermediatetable_submit",
        CANVAS: "FlowDisplayPanelPersonal"
	};
	
	dojo.ready(function(){
		dojo.connect(dijit.byId(constants.BUTTON_SUBMIT), "onClick", submitClearTable);
	});
	
	function showClearTableDialog(operator){
        spinner.showSpinner(constants.CANVAS);
		buildClearTableGrid(refineOperatorArray());
        spinner.hideSpinner(constants.CANVAS);
	 
	}
	
	function buildClearTableGrid(optArray){
		var clearTableGrid = dijit.byId(constants.GRID);
		var dataStore = new dojo.data.ItemFileWriteStore({
					data: {					
					identifier: "id",
					items: optArray
				}
			});
		dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
		if(!clearTableGrid){
			clearTableGrid = new dojox.grid.DataGrid({
				store: dataStore,
				query: { "id": "*" },
				structure: [
		           	{type: "dojox.grid._CheckBoxSelector"},
		           	[
			            {name: alpine.nls.table_clean_operatorName, field: "operatorName",width: "25%"},
			            {name: alpine.nls.table_clean_connectionName, field: "connectionName",width: "20%"},
			            {name: alpine.nls.table_clean_tableName, field: "tableName4Display" ,width: "40%"},
			            {name: alpine.nls.table_clean_outputType, field: "outputType" ,width: "15%"}
					]
				],
				onRowClick: function(){
					//nothing to do..
				}
			},constants.GRID);
			clearTableGrid.startup();
		}else{
			dojo.forEach(dijit.byId(constants.GRID).selection.getSelected(),function(item){
				dijit.byId(constants.GRID).selection.remove(item);
			});
			clearTableGrid.setStore(dataStore);
		}
	}

	function refineOperatorArray(){
		var refineOptArray = new Array();
		var hdMapping = null;
		variableReplacer.init();
		operatorManagement.forEachOperatorInfo(function(opt){
			if(opt.operatorType == "DB"){
				_fillDBOperatorInfo(opt, refineOptArray);
			}else{
				if(hdMapping == null){//to lazy load
					hdMapping = createHDConnNamingMapping();
				}
				_fillHadoopOperatorInfo(opt, refineOptArray, hdMapping);
			}
		});
		variableReplacer.finalize();
		return refineOptArray;
	}
	
	//return a map that key is conn's name and value is conn's label
	function createHDConnNamingMapping(){
		var mapping = {};
		var result = hadoopDsMgr.getPersonalConnections();
		for(var i = 0;i < result.length;i++){
			mapping[result[i].key] = result[i].label;
		}
		return mapping;
	}
	
	function _fillDBOperatorInfo(opt, resultArray){
		var idContainer = [];
		var tablePrefix = preferenceManager.getPrefix();
		if (!opt.interTableList
				|| opt.classname == "DbTableOperator" 
				|| opt.classname == "RandomSamplingOperator" 
					|| opt.classname == "StratifiedSamplingOperator") {
			return;
		}
		for(var i = 0; i < opt.interTableList.length; i++){//get each table/view info
			var id = opt.connectionName + "." + opt.interTableList[i].schemaName + "." + opt.interTableList[i].tableName;//opt.uid + opt.interTableList[i].tableName;
			if(idContainer[id] != null){
				continue;
			}
			idContainer[id] = true;
			var outType =opt.interTableList[i].outputType;
			if(!outType){
				outType="TABLE" ;
			}
			var refineOpt = {
				"id": opt.uid + opt.interTableList[i].tableName,
				"uuid": opt.uid ,
				"operatorName": opt.name,
				"connectionName": opt.connectionName,
				"schemaName": opt.interTableList[i].schemaName,
				"tableName4Display": opt.interTableList[i].schemaName + "." + tablePrefix + opt.interTableList[i].tableName,
				"tableName": opt.interTableList[i].tableName,
				"outputType":outType,
				"resourceType": workFlowManager.getEditingFlow().type
			};
			resultArray.push(refineOpt);
		}
	}
	
	function _fillHadoopOperatorInfo(opt, resultArray, connLabelMapping){
		if(opt.storeResult == null 
				||opt.storeResult == false){
			return;
		}
		var filePath = variableReplacer.replaceVariable(opt.outputHadoopFilePath);
		var refineOpt = {
			"id": opt.uid,
			"uuid": opt.uid ,
			"operatorName": opt.name,
			"connectionName": connLabelMapping[opt.connectionName],
			"tableName": filePath,
			"tableName4Display": filePath,
			"outputType": "FILE",
			"resourceType": workFlowManager.getEditingFlow().type
		};
		resultArray.push(refineOpt);
	}

	function submitClearTable(){
		var clearTableArray = dijit.byId(constants.GRID).selection.getSelected();
		if(clearTableArray.length < 1){
			popupComponent.alert(alpine.nls.table_clean_nochoise);
			return;
		}
		popupComponent.confirm(alpine.nls.table_clean_confirm, {
			handle: function(){
				//progressBar.showLoadingBar();
				var result = clearIntermediateTableManager.dropIntermediateTables(clearTableArray,
                    function(result)
                    {
                        if(result.error_code == 333){
                            handle_error_result(result);
                            return;
                        }
                        dijit.byId(constants.DIALOG).hide();

                    }, null,
                constants.DIALOG);
				//progressBar.closeLoadingBar();
			}
		});
	}
	
	return{
		showClearTableDialog: showClearTableDialog
	};
});
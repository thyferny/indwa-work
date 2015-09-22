/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * 
 * subflow.js
 * 
 * Author Will Version 3.0 Date 2012-04-06
 */
var SUBFLOWTABLES = null;
var SUBFLOWVARIABLE = null;
var INPUTTABLEMAPPING = null;
var EVENT_HANDLER4SUBFLOWPROPERTY = [];
function buildSubFlowPathSelectList(pNode, prop, currentFlowInfo, callBackFunc) {

	alpine.flow.FlowCategoryUIHelper.getBrothers(currentFlowInfo,
			function(list) {

				var storeItems = [];
				if (null != list && list.length > 0) {
					dojo.forEach(list, function(itm, idx) {
						// var path =
						// alpine.flow.FlowCategoryUIHelper.buildFlowPath(itm.info);
						var flowPathObj = {};
						// tmpObj.name = path;
						flowPathObj.name = itm.name;
						flowPathObj.id = itm.path;
						storeItems.push(flowPathObj);
					});
				}
				var nameStore = new dojo.data.ItemFileReadStore({
					data : {
						identifier : "name",
						items : storeItems,
						label : "name"
					}

				});

				var id = prop.name + ID_TAG;
				while (pNode.firstChild) {
					pNode.removeChild(pNode.firstChild);
				}
				var fselDomNode = dojo.create("div");
				pNode.appendChild(fselDomNode);
				if (dijit.byId(id) == null) {
					var fsel = new dijit.form.FilteringSelect({
						id : id,
						name : id,
						store : nameStore,
						sort : true,
						searchAttr : "name",
						baseClass : "greyDropdownButton"
					// value:
					}, fselDomNode);
					property_widgets.push(fsel);

					var subflowPathDropDownHandler = dojo
							.connect(fsel, "onChange",
									function() {
										var itemId = null;
										var itemName = null;
										if (null != this.item
												&& null != this.item.id) {
											itemId = this.item.id;
										}
										if (null != this.item
												&& null != this.item.name) {
											itemName = this.item.name;
										}
										subflowOnChange(prop.value, itemId,
												itemName);
										//
										getSubflowTableList(itemId);
									});

					EVENT_HANDLER4SUBFLOWPROPERTY
							.push(subflowPathDropDownHandler);
				}

				if (null != prop && prop.value != null && prop.value != ""
						&& isPropValueInStoreItems(storeItems, prop.value)) {
					dijit.byId(id).set("value", prop.value);
				}
			});

}

function isPropValueInStoreItems(storeItems, pValue) {
	if (null != storeItems && storeItems.length != null
			&& storeItems.length > 0) {
		for ( var i = 0; i < storeItems.length; i++) {
			if (storeItems[i].name == pValue) {
				return true;
			}
		}
		return false;
	} else {
		return false;
	}

}

function subflowOnChange(preValue, subflowName, subFlowValue) {
	if (null == subflowName || 0 == subflowName.length) {
		return;
	}
	var parentFlowName = subflowName[0].replace(subFlowValue[0],
			alpine.flow.WorkFlowManager.getEditingFlow().id);
	var url = baseURL
			+ "/main/operator/subflowHandle.do?method=getSubFlowExitOperatorInfo&subflowRealName="
			+ subFlowValue + "&newSubflowName=" + subflowName
			+ "&parentFlowName=" + parentFlowName + "&subflowOperatorUUID="
			+ alpine.flow.OperatorManagementUIHelper.getSelectedOperator().uid;
	ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(),
			subflowOnChangeCallback);

}
function subflowOnChangeCallback(data) {
	//
	if (null != data && null != data.errorId && null != data.errorMessage) {
		popupComponent.alert(data.errorMessage); // Recursive subflow found
		// clear value
		dijit.byId("subflowPath" + ID_TAG).set("value", "");
		return;
	}
	buildExitOperatorDropDownList(data);
	//
	updateSubFlowVariable(data);
}

function buildExitOperatorDropDownList(data) {
	var storeItems = [];
	if (null != data && data.exitOperatorMap != null) {
		for ( var name in data.exitOperatorMap) {
			if (name != null) {
				var tmpObj = {};
				tmpObj.name = name;
				tmpObj.id = data[name];
				storeItems.push(tmpObj);
			}
		}
	}

	var nameStore = new dojo.data.ItemFileReadStore({
		data : {
			identifier : "name",
			items : storeItems,
			label : "name"
		}

	});
	var id = "exitOperator" + ID_TAG;
	var exitOperator = dijit.byId(id);

	if (alpine.flow.OperatorManagementManager
			.hasChildrenOperator(
					alpine.flow.OperatorManagementUIHelper
							.getSelectedOperator().uid,
					function(operatorUid, link) {
						return "SQLExecuteOperator" != alpine.flow.OperatorManagementManager
								.getOperatorPrimaryInfo(operatorUid).classname;
					}) == true) {
		// alert("validate");
		exitOperator.required = true;
	} else {
		// alert("no validate");
		exitOperator.required = false;
	}
	exitOperator.set("store", nameStore);
	// CurrentOperatorDTO
	var exitOperatorValue = _getExitOperatorValue();
	if (exitOperatorValue != null
			&& _isInDataList(data.exitOperatorMap, exitOperatorValue)) {
		exitOperator.set("value", exitOperatorValue);
	} else {
		exitOperator.set("value", "");
	}

	exitOperator.startup();
}

function updateSubFlowVariable(data) {
	// console.log(data.subFlowVariableMap);
	SUBFLOWVARIABLE = [];
	if (data != null && data.subFlowVariableMap != null) {
		// SUBFLOWVARIABLE.push({name:allItem[i].paramName,value:allItem[i].paramValue});
		for ( var item in data.subFlowVariableMap) {
			SUBFLOWVARIABLE.push({
				name : item,
				value : data.subFlowVariableMap[item]
			});
		}
	}
}

function _isInDataList(data, exitOperatorValue) {
	for ( var name in data) {
		if (exitOperatorValue == name) {
			return true;
		}
	}
	return false;
}

function _getExitOperatorValue() {
	var exitOperatorValue = null;
	var propertyList = CurrentOperatorDTO.propertyList;
	for ( var i = 0; i < propertyList.length; i++) {
		if (propertyList[i].name == "exitOperator") {
			exitOperatorValue = propertyList[i].value;
			break;
		}
	}
	return exitOperatorValue;
}

/* init ExitOperator property */
function buildExitOperatorSelectList(pNode, prop, callBackFunc) {

	var id = prop.name + ID_TAG;
	while (pNode.firstChild) {
		pNode.removeChild(pNode.firstChild);
	}
	var fselDomNode = dojo.create("div");
	pNode.appendChild(fselDomNode);
	if (dijit.byId(id) == null) {
		var fsel = new dijit.form.FilteringSelect({
			id : id,
			name : id,
			store : new dojo.data.ItemFileReadStore({
				data : {
					identifier : "name",
					items : [],
					label : "name"
				}
			}),
			sort : true,
			searchAttr : "name",
			baseClass : "greyDropdownButton"
		// value:
		}, fselDomNode);
		property_widgets.push(fsel);
	}

	var exitOperator = dijit.byId(id);

	if (null != prop && prop.value != null && prop.value != "") {
		dijit.byId(id).setValue(prop.value);
	}

}

function buildTableMappingDlg(props) {
	var subFlowPropertyTableMappingDialog = dijit
			.byId("subFlowPropertyTableMappingDialog");
	if (null != subFlowPropertyTableMappingDialog) {
		//subFlowPropertyTableMappingDialog.set("title", props.displayName);
        subFlowPropertyTableMappingDialog.titleBar.style.display = "none";
        //subFlowPropertyTableMappingDialog.containerNode.heigth = "410px";
		// build table mapping grid

        var currentOperatorInputTables = [];
        if(CurrentOperatorDTO.inputTableInfos!=null){
            currentOperatorInputTables  = dojo.clone(CurrentOperatorDTO.inputTableInfos);
        }
        var currentFileInfos = [];
        if(CurrentOperatorDTO.inputFileInfos!=null){
            currentFileInfos = dojo.clone(CurrentOperatorDTO.inputFileInfos);
        }
        var currentFileToTables = [];
        if(currentFileInfos!=null){
            for(var i=0;i<currentFileInfos.length;i++){
                currentFileToTables.push({
                    connectionName:currentFileInfos[i].connectionName,
                    fieldColumns:buildFieldColumns(currentFileInfos[i].columnInfo),
                    operatorUUID:currentFileInfos[i].operatorUUID,
                    table:currentFileInfos[i].hadoopFileName,
                    schema:"",
                    system:"hadoop"
                });
            }
        }

        currentOperatorInputTables = currentOperatorInputTables.concat(currentFileToTables);
		var tablemappingRowContainer = dojo.byId("tablemappingRowContainer");
		// get subFlow tables;
		var subflowPath = dijit.byId("subflowPath" + ID_TAG);

		if (null != subflowPath && null != subflowPath.item
				&& null != subflowPath.item.id) {

			if (null != tablemappingRowContainer) {
				dojo.empty("tablemappingRowContainer");
					buildTableMappingRows(tablemappingRowContainer,
							currentOperatorInputTables, SUBFLOWTABLES,
							INPUTTABLEMAPPING);

			}
			subFlowPropertyTableMappingDialog.show();
		} else {
			popupComponent.alert(alpine.nls.subflow_need_select_alert);
			return false;
		}

	}
}

function getSubflowTableList(flowBasisKey) {
	if (null == flowBasisKey) {
		return;
	}
	var tableList = null;
	var url = baseURL
			+ "/main/operator/subflowHandle.do?method=getSubFlowTablesInfo&flowBasisKey="
			+ flowBasisKey;
	ds.get(url, function(data) {
		// console.log(data);
		SUBFLOWTABLES = data;
	});
}

function buildTableMappingRows(parentNode, currentOperatorInputTables,
		subflowInputtables, mappings) {
	// currentFlowDataTables subFlowDataTables maps
	if (null != currentOperatorInputTables
			&& currentOperatorInputTables.length > 0) {
		for ( var i = 0; i < currentOperatorInputTables.length; i++) {
			if (!currentOperatorInputTables[i].schema ) {
				var title = currentOperatorInputTables[i].table;
				var tableValue = currentOperatorInputTables[i].table;

			} else {
				var title = currentOperatorInputTables[i].schema + "."
						+ currentOperatorInputTables[i].table;
				var tableValue = currentOperatorInputTables[i].schema + ","
						+ currentOperatorInputTables[i].table;

			}
			createMappingRow(parentNode, title, tableValue, subflowInputtables,
					mappings);
		}
	}
}

function createMappingRow(parentNode, title, parentTableValue,
		subflowTableList, mappings) {
	var selectedValue = "";
	if (null != mappings) {
		for ( var j = 0; j < mappings.length; j++) {
			if (mappings[j].inputSchema  && mappings[j].inputSchema!="") {
				if (mappings[j].inputSchema == parentTableValue.split(",")[0]
						&& mappings[j].inputTable == parentTableValue
								.split(",")[1]) {
					selectedValue = mappings[j].subFlowSchema + "."
							+ mappings[j].subFlowTable;
				}
			} else {
				if (mappings[j].inputTable == parentTableValue) {
					selectedValue = mappings[j].subFlowTable;
				}
			}
		}
	}

	var row = dojo.create("div", {
		className : "tablemapping-row"
	}, parentNode);
	var displayTitle = title;
	if (null != displayTitle && displayTitle.length > 15) {
		displayTitle = displayTitle.substring(0, 15) + "...";
	}
	dojo.create("div", {
		className : "tablemapping-row-title",
		innerHTML : displayTitle,
		title : title
	}, row);
	dojo.create("div", {
		className : "tablemapping-row-map-to",
		innerHTML : "maps to sub-flow table"
	}, row);
	var subflowListContainer = dojo.create("div", {
		className : "tablemapping-row-subflow-table"
	}, row);
	dojo.create("input", {
		className : "parentTableName",
		type : "hidden",
		value : parentTableValue
	}, subflowListContainer);

	var subflowTableSelect = dojo.create("select", {
		id : "table_list_" + new Date().getTime() + "_" + Math.random(),
		name : "table_list_" + new Date().getTime(),
		className : 'tablemapping-row-subflow-table-list',
		style : "width:140px",
		parentInputTable : parentTableValue
	}, subflowListContainer);

	/*
	 * var subflowList = new dijit.form.Select({ name: "table_list_"+new
	 * Date().getTime(), className:'tablemapping-row-subflow-table-list',
	 * style:"width:130px", options: listOptions,
	 * parentInputTable:parentTableValue }).placeAt(subflowListContainer);
	 */

	var emptyOption = dojo.create("option", {
		value : "",
		title : "",
		innerHTML : "&nbsp;&nbsp;"
	}, subflowTableSelect);

	var listOptions = [];
	var emptyOptionSelected = true;
	if (null != subflowTableList) {
		for ( var i = 0; i < subflowTableList.length; i++) {
			var tmpObj = {};
			if (null != subflowTableList[i]) {
				if (subflowTableList[i].schema == null) {
					tmpObj.label = subflowTableList[i].table;
					tmpObj.value = subflowTableList[i].table;
				} else {
					tmpObj.label = subflowTableList[i].schema + "."
							+ subflowTableList[i].table;
					tmpObj.value = subflowTableList[i].schema + ","
							+ subflowTableList[i].table;
				}
				var pre_label_value = tmpObj.label;
				if (null != tmpObj.label && tmpObj.label.length > 15) {
					tmpObj.label = tmpObj.label.substring(0, 12) + "...";
				}
				if (selectedValue == pre_label_value) {
					// tmpObj.selected = true;
					dojo.create("option", {
						value : tmpObj.value,
						title : pre_label_value,
						innerHTML : tmpObj.label,
						selected : true
					}, subflowTableSelect);
					emptyOptionSelected = false;
				} else {
					// emptyOption.selected=true;
					dojo.create("option", {
						value : tmpObj.value,
						title : pre_label_value,
						innerHTML : tmpObj.label
					}, subflowTableSelect);
				}
			}
		}
		if (emptyOptionSelected == true) {
			emptyOption.selected = true;
		}
	}

}

function saveTableMappingSetting() {
    //validate input table number same as subflow input table number
    var inputTables = dojo.query("#tablemappingRowContainer .tablemapping-row");
    var dropdownList = dojo.query("#tablemappingRowContainer .tablemapping-row-subflow-table-list");
    if(inputTables!=null && dropdownList!=null && dropdownList.length>0){
        var subFlowInpus = 0;
        if(dropdownList[0].options!=null){
            for(var i=0;i<dropdownList[0].options.length;i++){
                if(dropdownList[0].options[i].value!=null && dropdownList[0].options[i].value!=""){
                    subFlowInpus++;
                }
            }
        }
        if(inputTables.length!=subFlowInpus){
             popupComponent.alert(alpine.nls.subflow_tableMapping_count_invalidte);
             return false;
        }
    }
	var currentTableMappingSetting = buidClickOKTableMappingList();
	var mappingValidateMsg = validateCurrentTableMappingSet(currentTableMappingSetting);
	if (mappingValidateMsg) {
	 	popupComponent.alert(alpine.nls.subflow_tableMapping_invalidte +mappingValidateMsg);
		return false;
	}

	saveTempTableMappingList();

	hideTableMappingDlog();

}

// For click ok(saveTableMappingSetting)
function buidClickOKTableMappingList() {
	var subflowTableListDOM = dojo
			.query(".tablemapping-row-subflow-table-list");
	var currentTableMappingSetting = [];
	if (null != subflowTableListDOM && subflowTableListDOM.length > 0) {
		var domIds = [];
		for ( var i = 0; i < subflowTableListDOM.length; i++) {
			domIds.push(subflowTableListDOM[i].id);
		}
		if (domIds.length > 0) {
			dojo
					.forEach(
							domIds,
							function(itm, idx) {
								// for(var itm=0;i<domIds.length;itm++){
								var selectList = dojo.byId(itm);
								if (null != selectList
										&& selectList.value != null
										&& selectList.value != "") {
									var tmpMapingObj = {};
									var parentTableString = selectList
											.getAttribute("parentInputTable");
									var subFlowTableString = selectList.value;
									if (null != parentTableString
											&& null != subFlowTableString) {
                                        if(parentTableString.split(",").length==2){
                                            tmpMapingObj.parentSchema = parentTableString
                                                .split(",")[0];
                                            tmpMapingObj.parentTable = parentTableString
                                                .split(",")[1];
                                        }else{
                                            tmpMapingObj.parentSchema = "";
                                            tmpMapingObj.parentTable = parentTableString;
                                        }
                                        if(subFlowTableString.split(",").length==2){
                                            tmpMapingObj.subFlowSchema = subFlowTableString
                                                .split(",")[0];
                                            tmpMapingObj.subFlowTable = subFlowTableString
                                                .split(",")[1];
                                        }else{
                                            tmpMapingObj.subFlowSchema = "";
                                            tmpMapingObj.subFlowTable = subFlowTableString;
                                        }

										tmpMapingObj.domId = itm;
										currentTableMappingSetting
												.push(tmpMapingObj);
									}
								}
							});
		}
	}
	return currentTableMappingSetting;
}

function saveTempTableMappingList() {
	var subflowTableListDOM = dojo
			.query(".tablemapping-row-subflow-table-list");
	var currentTableMappingSetting = [];
	if (null != subflowTableListDOM && subflowTableListDOM.length > 0) {
		var domIds = [];
		for ( var i = 0; i < subflowTableListDOM.length; i++) {
			domIds.push(subflowTableListDOM[i].id);
		}
		if (domIds.length > 0) {
			dojo
					.forEach(
							domIds,
							function(itm, idx) {
								// for(var itm=0;itm<domIds.length;itm++){
								var selectList = dojo.byId(itm);
								if (null != selectList
										&& selectList.value != null
										&& selectList.value != "") {
									var tmpMapingObj = {};
									var parentTableString = selectList
											.getAttribute("parentInputTable");
									var subFlowTableString = selectList.value;
									if (null != parentTableString
											&& null != subFlowTableString) {
                                        if(parentTableString.split(",").length==1){
                                            tmpMapingObj.inputSchema = "";
                                            tmpMapingObj.inputTable = parentTableString;

                                        }
                                        if(subFlowTableString.split(",").length==1){
                                            tmpMapingObj.subFlowSchema = "";
                                            tmpMapingObj.subFlowTable = subFlowTableString;
                                        }
                                        if(parentTableString.split(",").length>1){
                                            tmpMapingObj.inputSchema = toolkit
                                                .getValue(parentTableString
                                                .split(",")[0]);
                                            tmpMapingObj.inputTable = toolkit
                                                .getValue(parentTableString
                                                .split(",")[1]);

                                        }
                                        if(subFlowTableString.split(",").length>1){
                                            tmpMapingObj.subFlowSchema = toolkit
                                                .getValue(subFlowTableString
                                                .split(",")[0]);
                                            tmpMapingObj.subFlowTable = toolkit
                                                .getValue(subFlowTableString
                                                .split(",")[1]);
                                        }
                                        currentTableMappingSetting
                                            .push(tmpMapingObj);
										INPUTTABLEMAPPING = currentTableMappingSetting;
									}
								}
							});
		}
		/*
		 * for(var j=0;j<CurrentOperatorDTO.propertyList.length;j++){ var
		 * currentProperty = CurrentOperatorDTO.propertyList[j];
		 * if(currentProperty.name=="tableMapping"){
		 * if(currentProperty.subflowTableMappingModel==null){
		 * currentProperty.subflowTableMappingModel={}; }
		 * currentProperty.subflowTableMappingModel.mappingItems =
		 * INPUTTABLEMAPPING; break;
		 *  } }
		 */
	}
}

function validateCurrentTableMappingSet(currentTableMappingSetting) {
 
	if (null != currentTableMappingSetting
			&& currentTableMappingSetting.length > 0) {
		//
		var mappingRows = dojo
				.query("#tablemappingRowContainer>.tablemapping-row");
		if (null != mappingRows
				&& mappingRows.length != currentTableMappingSetting.length) {
			return false;
		}
		//
		if (isSameInputTableSchema(currentTableMappingSetting) == true) {
			popupComponent.alert(alpine.nls.subflow_inputtable_same);
			return false;
		}

		for ( var i = 0; i < currentTableMappingSetting.length; i++) {

			var parentTableInfo = getParentInputTableInfo(
					currentTableMappingSetting[i].parentSchema,
					currentTableMappingSetting[i].parentTable);
			var subFlowTableInfo = getSubFlowInputTableInfo(
					currentTableMappingSetting[i].subFlowSchema,
					currentTableMappingSetting[i].subFlowTable);
			var parentColumns = parentTableInfo.fieldColumns == null ? null
					: parentTableInfo.fieldColumns;
			var subflowColumns = (!subFlowTableInfo||subFlowTableInfo.fieldColumns == null) ? null
					: subFlowTableInfo.fieldColumns;
			if (null != parentColumns && null != subflowColumns
					&& parentColumns.length > 0 && subflowColumns.length > 0) {
				// if(isColumnListEqual(parentColumns,subflowColumns) &&
				// isColumnListEqual(subflowColumns,parentColumns)){
				var validateMsg =isColumnListEqual(subflowColumns, parentColumns);
				if (validateMsg) {
					return validateMsg;
				}  
			}

			// }

		}
	}

	return null;
}

function isSameInputTableSchema(currentTableMappingSetting) {
	if (null != currentTableMappingSetting
			&& currentTableMappingSetting.length > 0) {

		for ( var i = 0; i < currentTableMappingSetting.length - 1; i++) {
			var tempSchemaName = currentTableMappingSetting[i].parentSchema;
			var tempTableName = currentTableMappingSetting[i].parentTable;
			for ( var j = i + 1; j < currentTableMappingSetting.length; j++) {
				if (tempSchemaName == currentTableMappingSetting[j].parentSchema
						&& tempTableName == currentTableMappingSetting[j].parentTable) {
					// popupComponent.alert(alpine.nls.subflow_inputtable_same);
					return true;
				}
			}
		}
	}
	return false;

}
 
function isColumnListEqual(columnListA, columnListB) {
	if (null != columnListA && null != columnListB) {
		for ( var i = 0; i < columnListA.length; i++) {
			if (isColumnInColumnList(columnListA[i], columnListB) == false) {
				return "\""+columnListA[i][0]+":"+columnListA[i][1] +"\" not found in target dataset.";
			}
		}
	}
	return null;
}

function isColumnInColumnList(column, columnList) {
	if (null != column && null != columnList && columnList.length != null
			&& columnList.length > 0) {
		for ( var i = 0; i < columnList.length; i++) {
			if (column[0] == columnList[i][0]
					&& isType(column[1]) == isType(columnList[i][1])) {
				return true;
			}
		}
	}
	return false;
}

function isType(columeType) {
	if (null != columeType) {
		columeType = columeType.toUpperCase();
		switch (columeType) {
		// db2
		case "INT":
		case "INTEGER":
		case "SMALLINT":
		case "BIGINT":
			// gp
		case "INT2":
		case "INT4":
		case "INT8":
			// case "INTEGER":
			// case "SMALLINT":
			// case "BIGINT":

			// oracle
			// case "INTEGER":
			// case "BIGINT":
			// case "INT":
			// case "SMALLINT":

		case "BINARY_INTEGER":
		case "simple_integer":
		case "NUMERIC":

			// float number
			// DB2
		case "DOUBLE":
		case "DECIMAL":
		case "DEC":
		case "REAL":
		case "NUM":
		case "DECFLOAT":

			// case "DECFLOAT":
		case "FLOAT":
		case "FLOAT4":
		case "FLOAT8":
		case "DOUBLE PRECISION":

			// case "DOUBLE_PRECISION":
			return "number";
			break;
		default:
			return columeType;
		}
	}
}

function getParentInputTableInfo(pSchema, pTable) {
	if (null != CurrentOperatorDTO) {
		// hadoop
		if (CurrentOperatorDTO.inputFileInfos != null
				&& CurrentOperatorDTO.inputFileInfos.length > 0) {
			var fileInfos = transformToTableInfo(CurrentOperatorDTO.inputFileInfos);
			for ( var i = 0; i < fileInfos.length; i++) {
				if (fileInfos[i].table == pTable) {
					return fileInfos[i];
				}
			}
        }
		if (CurrentOperatorDTO.inputTableInfos != null
				&& CurrentOperatorDTO.inputTableInfos.length > 0) { // db
			var tableInfos = CurrentOperatorDTO.inputTableInfos;
			for ( var i = 0; i < tableInfos.length; i++) {
				if (tableInfos[i].schema == pSchema
						&& tableInfos[i].table == pTable) {
					return tableInfos[i];
				}
			}
		}
	}
	return null;
}
function getSubFlowInputTableInfo(sSchema, sTable) {
	if (null != SUBFLOWTABLES && SUBFLOWTABLES.length > 0) {
		for ( var i = 0; i < SUBFLOWTABLES.length; i++) {
			if (null != SUBFLOWTABLES[i]) {
				if ((sSchema == SUBFLOWTABLES[i].schema || (sSchema=="" && SUBFLOWTABLES[i].schema==null))
						&& sTable == SUBFLOWTABLES[i].table) {
					return SUBFLOWTABLES[i];
				}
			}
		}
	}
	return null;
}

function hideTableMappingDlog() {
	var subFlowPropertyTableMappingDialog = dijit
			.byId("subFlowPropertyTableMappingDialog");
	if (null != subFlowPropertyTableMappingDialog) {
		subFlowPropertyTableMappingDialog.hide();
	}
}
function hideVariableDlg() {
	var variableDlg = dijit.byId("subFlowPropertyVariableDialog");
	if (variableDlg != null) {
		setVariableSettingDisenable();
		variableDlg.hide();

	}
}

function buildSubFlowVariableDlg(props) {

	var variableDlg = dijit.byId("subFlowPropertyVariableDialog");
	if (variableDlg != null) {
		//variableDlg.set("title", props.displayName);
        variableDlg.titleBar.style.display = "none"
		variableDlg.show();
        //dojo.byId("subFlowPropertyVariableDialog_title").innerHTML = props.displayName;
    } else {
		return;
	}

	buildSubflowVariableList(dojo.byId("subflow_variablelist_container"),
			SUBFLOWVARIABLE);

}
function buildSubflowVariableList(pNode, dataItems) {

	if (dataItems == null) {
		return;
	}
	//
	var variableDataItem = [];
	if (null != dataItems.length && dataItems.length > 0) {
		for ( var i = 0; i < dataItems.length; i++) {
			var tempObj = {};
			tempObj.paramName = dataItems[i].name;
			tempObj.paramValue = dataItems[i].value;
			variableDataItem.push(tempObj);
		}
	}

	storeData = {
		identifier : "paramName",
		items : variableDataItem
	};

	var layout = [ [ {
		'name' : alpine.nls.subflow_parameter_name,
		'field' : 'paramName',
		'width' : '50%'
	}, {
		'name' : alpine.nls.subflow_parameter_value,
		'field' : 'paramValue',
		'width' : '50%'
	} ] ];
	var store = new dojo.data.ItemFileWriteStore({
		data : storeData
	});
	var grid = dijit.byId("subflowVariableList");
	if (null == grid) {
		grid = new dojox.grid.DataGrid({
			id : 'subflowVariableList',
			store : store,
			structure : layout,
			query : {
				paramName : "*"
			},
			selectionMode : "single",
			canSort : function() {
				return false;
			},
			onSelected : subflowVariableList_OnSelected,
			style : "width:100%;height:100%"
		}, dojo.create("div", null, pNode));
		grid.startup();
		// regist tooltip
		dojo.connect(grid, "onCellMouseOver", function(e) {
			toggleSubflowVariableTooltip(e, true);
		});
		dojo.connect(grid, "onCellMouseOut", function(e) {
			toggleSubflowVariableTooltip(e, false);
		});
	} else {
		grid.setStore(store);
		grid.render();
	}

}

function toggleSubflowVariableTooltip(e, status) {
	if (null == e.rowIndex) {
		return;
	}
	var grid = dijit.byId("subflowVariableList");
	if (null != grid) {
		var currentRow = grid.getItem(e.rowIndex);
		if (null != currentRow && null != currentRow.paramName
				&& null != currentRow.paramValue) {

			var msg = dojo.string.substitute(
					alpine.nls.flowvariable_message_conflict, {
						parentVar : currentRow.paramValue
					});

			if (status == true) {
				dijit.showTooltip(msg, e.cellNode);
			} else {
				dijit.hideTooltip(e.cellNode);
			}
		}

	}
}

function subflowVariableList_OnSelected(indexRow) {
	if (-1 != indexRow) {
		setVariableSettingEnable();
		var parameter_name = dijit.byId("subflow_parameter_name_Input");
		var parameter_value = dijit.byId("subflow_parameter_value_Input");

		var rowItems = this.getItem(indexRow);

		if (null != rowItems && rowItems.paramName != null
				&& null != rowItems.paramValue) {
			parameter_name.set("value", rowItems.paramName);
			parameter_value.set("value", rowItems.paramValue);
		}

	}
}

function setVariableSettingDisenable() {
	var parameter_name = dijit.byId("subflow_parameter_name_Input");
	var parameter_value = dijit.byId("subflow_parameter_value_Input");
	var variable_Save_button = dijit.byId("subflow_variable_Save_button");

	parameter_name.set("value", "");
	parameter_value.set("value", "");
	variable_Save_button.set("value", "");

	parameter_name.set("disabled", true);
	parameter_value.set("disabled", true);
	variable_Save_button.set("disabled", true);
}
function setVariableSettingEnable() {
	// var parameter_name = dijit.byId("subflow_parameter_name_Input");
	var parameter_value = dijit.byId("subflow_parameter_value_Input");
	var variable_Save_button = dijit.byId("subflow_variable_Save_button");

	// parameter_name.set("disabled",false);
	parameter_value.set("disabled", false);
	variable_Save_button.set("disabled", false);
}

function saveVariableValue() {
	var parameter_value = dijit.byId("subflow_parameter_value_Input");
	var parameter_name = dijit.byId("subflow_parameter_name_Input");
	if (parameter_value.validate() == false) {
		return false;
	}
	var grid = dijit.byId("subflowVariableList");

	if (null != grid) {
		var gridStore = grid.store;
		if (null != gridStore) {
			var pName = parameter_name.get("value");
			var pValue = parameter_value.get("value");
			gridStore.fetchItemByIdentity({
				identity : pName,
				onItem : function(item) {
					if (null != item) {
						// item.paramValue = pValue;
						gridStore.setValue(item, "paramValue", pValue);
						gridStore.save(pName);

						// save current Variable setting
						var allItem = gridStore._arrayOfAllItems;
						SUBFLOWVARIABLE = [];
						for ( var i = 0; i < allItem.length; i++) {
							SUBFLOWVARIABLE.push({
								name : toolkit.getValue(allItem[i].paramName),
								value : toolkit.getValue(allItem[i].paramValue)
							});
						}
						/*
						 * for(var j=0;j<CurrentOperatorDTO.propertyList.length;j++){
						 * var currentProperty =
						 * CurrentOperatorDTO.propertyList[j];
						 * if(currentProperty.name=="subflowVariable"){
						 * if(currentProperty.flowVariable==null){
						 * currentProperty.flowVariable = {}; }
						 * currentProperty.flowVariable.variables =
						 * SUBFLOWVARIABLE; }
						 *  }
						 */

					}
				}
			});
		}
	}
}

function rightMenuEditSubFlow(operator) {
	var flowCategory = null;
	var editingFlow = alpine.flow.WorkFlowManager.getEditingFlow();
	if (null != editingFlow && editingFlow.categories != null
			&& editingFlow.categories.length > 0) {
		flowCategory = editingFlow.categories[0];
	} else {
		flowCategory = alpine.USER;
	}
	console.log(operator);
	var getPropertyURL = baseURL + "/main/property.do?method=getPropertyData&user="
			+ alpine.USER + "&uuid=" + operator.uid;
	var currentFlowObj = editingFlow;
	ds.post(getPropertyURL, currentFlowObj, function(data) {
		var subflowPathValue = "";
		if (null != data && null != data.propertyList) {
			var proplist = data.propertyList;
			for ( var i = 0; i < proplist.length; i++) {
				if ("subflowPath" == proplist[i].name) {
					subflowPathValue = proplist[i].value;
					break;
				}
			}
			if ("" != subflowPathValue) {
				var url = baseURL
						+ "/main/operator/subflowHandle.do?method=getSubFlowInfo&flowCategory="
						+ editingFlow.type + "/" + flowCategory
						+ "&subflowPathValue=" + subflowPathValue;
				ds.get(url, function(obj) {
					if (null != obj) {
						alpine.flow.WorkFlowUIHelper.openWorkFlow(obj);
						alpine.flow.FlowCategoryUIHelper.setSelectedItem(obj);
					}
				});
			}
		}
	});
	// var subflowName =
	// var url = baseURL +
	// "/main/flow.do?method=getSubFlowInfo&flowCategory=&subflowName=";
	// open_flow(getVal(item.type), flow);
}

function transformToTableInfo(inputFileInfos) {
	var result = [];
	if(inputFileInfos) {
		var size = inputFileInfos.length;
		var index = 0;
		for ( var i = 0; i < size; i++) {
			fileInfo = inputFileInfos[i];
			var res = toTableInfo(fileInfo);
			if(res) {
				result[index] = res;
				index =index+1
				}
		}
	}
	return result;
}

function toTableInfo(fileInfo) {
	var tableInfo = {};
	tableInfo.fieldColumns = buildFieldColumns(fileInfo.columnInfo);
	tableInfo.table = fileInfo.hadoopFileName;
	return tableInfo;
}

function buildFieldColumns(columnInfo) {
	var fieldColumns = [];
	if(!columnInfo){
		return fieldColumns;
	}
	var nameList = columnInfo.columnNameList;
	var typeList = columnInfo.columnTypeList;
	for ( var i = 0; i < nameList.length; i++) {
		fieldColumns[i] = [ nameList[i], typeList[i] ];
	}
	return fieldColumns;
}

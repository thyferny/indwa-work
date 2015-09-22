dojo.ready(function(){
	//consistence width for button
	var width = "60px";
	dojo.style("woe_grouping_add", "width", width);
	dojo.style("woe_grouping_delete", "width", width);
	dojo.style("woe_grouping_autoCalculate", "width", width);
	dojo.style("woe_grouping_calculate", "width", width);
});

 
var WOE_Operator = {
		
	//merge select columns into woe setting column.
	mergeColumn: function(/* String[] */selectColumns, woeEditColumns){
		var mergedColumnArray = new Array();
		for(var i = 0;i < selectColumns.length;i++){//for each selected column
			mergedColumnArray.push(mergnItem(selectColumns[i], woeEditColumns));
		}
		return mergedColumnArray;
		// 
		function mergnItem(selectedColumn, woeEditColumns){
			for(var i = 0;i < woeEditColumns.length;i++){//for each woe setting column
				if(selectedColumn == woeEditColumns[i].columnName){// check if woe setting column array include selected column
					return dojo.clone(woeEditColumns[i]);
				}
			}
			// not found selected column in woeEditColumns
			return {
				columnName : selectedColumn
			};
		}
	},
	
	getValueFromGrid: function(val){
		return val == undefined ? undefined : val[0];
	}
};

//WOE setting dialog
var WOE_Setting = {
	/*-----------------------private fields-----------------------*/
	_currentWOESettigData: null,
	
	//reflected fields from WOEInforList.class
	_WOEInfoList: {
		columnName: "columnName",
		gini: "gini",
		infoValue: "inforValue",
		dataType: "dataType",// this field is not in WOEInforList.class, but WoeCalculateElement.
		infoList: "InforList"
	},
	
	//reflected fields from WOENumericNode.class
	_WOENumericNode: {
		id: "groupInfo",
		woeVal: "WOEValue",
		upper: "upper",
		bottom: "bottom"
	},
	//reflected fields from WOENominalNode.class
	_WOENominalNode: {
		id: "groupInfo",
		woeVal: "WOEValue",
		optionalVal: "choosedList"
	},
	//reflected Enumeration from WOEModelUI.dataType
	_WOEColumnType: {
		numeric: "NUMERIC",
		text: "TEXT"
	},
	
	/*
	 * Numeric grouping component.
	 * include 
	 * 1.grid
	 * 2.add Numeric row
	 * 3.delete Numeric row
	 * 4. return current Numeric datas.
	 */
	numericComponent: {
		releaseHandle: {},
		
		initialize: function(){
			var groupingAddBtn = dijit.byId("woe_grouping_add"),
				groupingDelBtn = dijit.byId("woe_grouping_delete"),
				groupingCalculateBtn = dijit.byId("woe_grouping_calculate"),
				releaseHandle = WOE_Setting.numericComponent.releaseHandle;
			releaseHandle.groupingAddBtn = dojo.connect(groupingAddBtn, "onClick", function(){
				WOE_Setting.numericComponent.addRecord();
			});
			releaseHandle.groupingDelBtn = dojo.connect(groupingDelBtn, "onClick", function(){
				WOE_Setting.numericComponent.delRecord();
			});
			releaseHandle.currentEditColumn = dijit.byId("columnsGrid").selection.getSelected()[0];
			
			releaseHandle.groupingCalculateBtn = dojo.connect(groupingCalculateBtn, "onClick", function(){
				var isAllright = WOE_Setting.numericComponent._validateRecord();
				// get edited data array back to current column.
				WOE_Setting.numericComponent._syncNumericEditDatas(dijit.byId("columnsGrid").selection.getSelected()[0]);
				WOE_Setting.calculate(isAllright);
			});
		},
		
		finalize: function(isSyncData){
			if(isSyncData){
				// get edited data array back to current column.
				WOE_Setting.numericComponent._syncNumericEditDatas(WOE_Setting.numericComponent.releaseHandle.currentEditColumn);
			}
			
			dojo.disconnect(WOE_Setting.numericComponent.releaseHandle.groupingAddBtn);
			dojo.disconnect(WOE_Setting.numericComponent.releaseHandle.groupingDelBtn);
			dojo.disconnect(WOE_Setting.numericComponent.releaseHandle.groupingCalculateBtn);
			delete WOE_Setting.numericComponent.releaseHandle.groupingAddBtn;
			delete WOE_Setting.numericComponent.releaseHandle.groupingDelBtn;
			delete WOE_Setting.numericComponent.releaseHandle.groupingCalculateBtn;
			delete WOE_Setting.numericComponent.releaseHandle.currentEditColumn;
		},
		
		/**
		 * sync numeric edit datas into current selected column info.
		 */
		_syncNumericEditDatas: function(currentColumnRecorder){
			if(!currentColumnRecorder){
				//never opend section. So nothing to sync.
				return;
			}
			var numericGrid = dijit.byId("numericEditGrid"),
				rowCount = numericGrid.rowCount,
				numericDatas = new Array();
			for(var i = 0;i < rowCount;i++){
				var numericItem = numericGrid.getItem(i),
					pureItem = {};
				pureItem[WOE_Setting._WOENumericNode.id] = numericItem[WOE_Setting._WOENumericNode.id];
				pureItem[WOE_Setting._WOENumericNode.bottom] = numericItem[WOE_Setting._WOENumericNode.bottom];
				pureItem[WOE_Setting._WOENumericNode.upper] = numericItem[WOE_Setting._WOENumericNode.upper];
				//fixed MINERWEB-662	some IE explore can be filter undefined value. So manually check it.
				pureItem[WOE_Setting._WOENumericNode.woeVal] = numericItem[WOE_Setting._WOENumericNode.woeVal] ? numericItem[WOE_Setting._WOENumericNode.woeVal] : "0";
				numericDatas.push(pureItem);
			}
			currentColumnRecorder.InforList = numericDatas;
		},
		
		/**
		 * private method.
		 * return true if records is allright, false if any upper value was NaN.
		 */
		_validateRecord: function(){
			var numericGrid = dijit.byId("numericEditGrid"),
				rowLength = numericGrid.rowCount;
			if(rowLength < 1){
				return false;
			}
			for(var i = 0;i < rowLength;i++){
				 var record = numericGrid.getItem(i);
				 if(!WOE_Setting.numericComponent.validateNumericItem(record, numericGrid.store._arrayOfAllItems, i)){
					return false;
				 }
			}
			return true;
		},
		
		validateNumericItem: function(numericRecord, recordList, i){
			if(recordList && i != undefined){
				var previousItem = recordList[i - 1];
				if(!previousItem){
					return true;//first row 
				}
				var previousValue = alpine.flow.WorkFlowVariableReplacer.replaceVariable(WOE_Setting.getVal(previousItem[WOE_Setting._WOENumericNode.upper]));
				var currentValue = alpine.flow.WorkFlowVariableReplacer.replaceVariable(WOE_Setting.getVal(numericRecord[WOE_Setting._WOENumericNode.upper]));
				return parseFloat(previousValue) < parseFloat(currentValue);
			}else{
				var currentValue = alpine.flow.WorkFlowVariableReplacer.replaceVariable(WOE_Setting.getVal(numericRecord[WOE_Setting._WOENumericNode.upper]));
				return currentValue.toString() != Number.NaN.toString();
			}
		},
		
		/**
		 * create or rebuild data grid for numeric
		 */
		buildNumericGrid: function(numericRange){
			var numericStore = new dojo.data.ItemFileWriteStore({
				data: {
					items: numericRange
				}
			});
			var grid = dijit.byId("numericEditGrid");
			if(!grid){
				grid = new dojox.grid.DataGrid({
					store: numericStore,
					query: {"groupInfo": "*"},
					structure: [
						{name: alpine.nls.woe_setting_numeric_grid_id,field: WOE_Setting._WOENumericNode.id,width: "20%"},
						{name: alpine.nls.woe_setting_numeric_grid_bottomVal,field: WOE_Setting._WOENumericNode.bottom,width: "30%"},
						{
							name: alpine.nls.woe_setting_numeric_grid_upperVal,
							field: WOE_Setting._WOENumericNode.upper,
							width: "30%",
							editable: true,
							type: dojox.grid.cells._Widget,
							widgetClass: dijit.form.ValidationTextBox,
							widgetProps: {
								id: "woeNumericTextBox",
								isValid: function(){
									var value = this.get("value");
									if(!this.validator(value)){
										return false;
									}
									value = alpine.flow.WorkFlowVariableReplacer.replaceVariable(value);
									return !isNaN(value);
								},
								//on show the box, then disabled all buttons
								onFocus: function(){
									// last row is infinity record
									if((grid.rowCount - 1) == grid.focus.rowIndex){
										grid.edit.cancel();
										return;
									}
									//if the value return from validate is false, them means the all of buttons have still disabled. So nothing to do.
									if(!this.validate()){
										return;
									}
									WOE_Setting.changeButtonsState(false);
								}
							}
						},
						{name: alpine.nls.woe_setting_numeric_grid_woeVal,field: WOE_Setting._WOENumericNode.woeVal,width: "20%"}
					]
				},"numericEditGrid");
				grid.startup();
				dojo.connect(grid, "onApplyCellEdit", grid, function(val, rowIdx){
					//on hide the box, then disabled all buttons
					if(dijit.byId("woeNumericTextBox").validate()){
						WOE_Setting.changeButtonsState(true);
					}
					this.store.setValue(this.getItem(rowIdx + 1), WOE_Setting._WOENumericNode.bottom, val);//sync value to next row's bottom.
					//avoid data can not sync to column record if click current record after excute add/del. 
					WOE_Setting.numericComponent._syncNumericEditDatas(WOE_Setting.numericComponent.releaseHandle.currentEditColumn);
				});
			}else{
				grid.setStore(numericStore);
			}
		},
		
		/**
		 * fired by click add button in Numeric Panel.
		 */
		addRecord: function(){
			var grid = dijit.byId("numericEditGrid"),
				lastId = grid.rowCount;
			switch(grid.rowCount){
			case 1://bad quality data. just grouping one row by automatic.
				//then delete the row and execute same no row.
				grid.store.deleteItem(grid.getItem(--lastId));
			case 0:
				//added two ranges to make sense for completely section, if there is no range in grid.
				var origin = {}, finis = {};
				origin[WOE_Setting._WOENumericNode.id] = ++lastId;
				origin[WOE_Setting._WOENumericNode.bottom] = Number.NEGATIVE_INFINITY;
				origin[WOE_Setting._WOENumericNode.upper] = 0;
				grid.store.newItem(origin);
				
				finis[WOE_Setting._WOENumericNode.id] = ++lastId;
				finis[WOE_Setting._WOENumericNode.bottom] = 0;
				finis[WOE_Setting._WOENumericNode.upper] = Number.POSITIVE_INFINITY;
				grid.store.newItem(finis);
				break;
			default: 
				var finis = grid.getItem(--lastId), //because in Array index is start with zero.
					newRange = {},
					previousRange = grid.getItem(lastId - 1);
				
				grid.store.deleteItem(finis);//remove finis range from grid
				
				newRange[WOE_Setting._WOENumericNode.id] = ++lastId;
				newRange[WOE_Setting._WOENumericNode.bottom] = grid.store.getValue(previousRange, WOE_Setting._WOENumericNode.upper);
				newRange[WOE_Setting._WOENumericNode.upper] = Number.NaN;
				grid.store.newItem(newRange);
	
				// create finis range into grid after added new range.
				finis = {};
				finis[WOE_Setting._WOENumericNode.id] = ++lastId;
				finis[WOE_Setting._WOENumericNode.bottom] = Number.NaN;
				finis[WOE_Setting._WOENumericNode.upper] = Number.POSITIVE_INFINITY;
				grid.store.newItem(finis);
			}
			grid.render();
			//avoid data can not sync to column record if click current record after excute add/del. 
			WOE_Setting.numericComponent._syncNumericEditDatas(WOE_Setting.numericComponent.releaseHandle.currentEditColumn);
		},
		
		/**
		 * fired by click delete button in Numeric Panel.
		 */
		delRecord: function(){
			var numericGrid = dijit.byId("numericEditGrid"),
				deleteNumericRecords;

			deleteNumericRecords = numericGrid.selection.getSelected();
			if(deleteNumericRecords.length < 1){
				popupComponent.alert(alpine.nls.woe_setting_alert_delete_norecord);
				return;
			}
			popupComponent.confirm(alpine.nls.woe_setting_confirm_delete, {
				handle: function(){
					var numericStore = numericGrid.store;
					
					if(numericGrid.rowCount < 3){//must be only two rows of -infinity and infinity in the grid. delete them if selected both of them. 
						numericGrid.selection.selectRange(0, numericGrid.rowCount - 1);
						numericGrid.removeSelectedRows();
						numericGrid.render();
					}else{
						// remove the first and last range select.
						numericGrid.selection.deselect(0);
						numericGrid.selection.deselect(numericGrid.rowCount - 1);
						numericGrid.removeSelectedRows();
						numericGrid.render();
						
						//update last range id and sync bottom field to previous range.
						var rowLength = numericGrid.rowCount;
						var finis = numericGrid.getItem(numericGrid.rowCount - 1);
						var previous = numericGrid.getItem(numericGrid.rowCount - 2);
						numericGrid.store.setValue(finis, WOE_Setting._WOENumericNode.id, rowLength);
						numericGrid.store.setValue(finis, WOE_Setting._WOENumericNode.bottom, numericGrid.store.getValue(previous, WOE_Setting._WOENumericNode.upper));
						WOE_Setting.realignRecordId("numericEditGrid");
					}
					//avoid data can not sync to column record if click current record after excute add/del. 
					WOE_Setting.numericComponent._syncNumericEditDatas(WOE_Setting.numericComponent.releaseHandle.currentEditColumn);
				}
			}, {//fired on click cancel buttons
				handle: function(){
					numericGrid.selection.deselectAll();
				}
			});
		}
	},

	/*
	 * nominal grouping component.
	 * include 
	 * 1.grid.
	 * 2.add Nominal row.
	 * 3.delete Nominal row.
	 * 4.return current Nominal datas.
	 * 5.attach values to nominal Column.
	 */
	nominalComponent: {
		releaseHandle: {},
		
		initialize: function(){
			var groupingAddBtn = dijit.byId("woe_grouping_add"),
				groupingDelBtn = dijit.byId("woe_grouping_delete"),
				groupingCalculateBtn = dijit.byId("woe_grouping_calculate");
				submitOpValBtn = dijit.byId("submitOptionalVal"),
				cancelOpValBtn = dijit.byId("cancelOptionalVal"),
			
				releaseHandle = WOE_Setting.nominalComponent.releaseHandle;
			releaseHandle.groupingAddBtn = dojo.connect(groupingAddBtn, "onClick", function(){
				WOE_Setting.nominalComponent.addRecord();
			});
			releaseHandle.groupingDelBtn = dojo.connect(groupingDelBtn, "onClick", function(){
				WOE_Setting.nominalComponent.delRecord();
			});
			
			releaseHandle.groupingCalculateBtn = dojo.connect(groupingCalculateBtn, "onClick", function(){
				var isAllright = WOE_Setting.nominalComponent._validateRecord();
				// get edited data array back to current column.
				WOE_Setting.nominalComponent._syncNominalEditDatas(dijit.byId("columnsGrid").selection.getSelected()[0]);
				WOE_Setting.calculate(isAllright);
			});

			releaseHandle.currentEditColumn = dijit.byId("columnsGrid").selection.getSelected()[0];
			
			releaseHandle.submitOpValBtn = dojo.connect(submitOpValBtn, "onClick", function(){
				var optionalValSel = dijit.byId("optionalValSel");
//				optionalValSel.required = true;
//				if(!optionalValSel.validate()){
//					return;
//				}
//				optionalValSel.required = false;
				var nominalGrid = dijit.byId("nominalEditGrid");
				var editRow = nominalGrid.selection.getSelected()[0];
				nominalGrid.store.setValue(editRow, WOE_Setting._WOENominalNode.optionalVal, optionalValSel.getValue().toString());
				dijit.byId("editWOEOptionalVal").hide();
				nominalGrid.render();
				//avoid data can not sync to column record if click current record after excute add/del. 
				WOE_Setting.nominalComponent._syncNominalEditDatas(WOE_Setting.nominalComponent.releaseHandle.currentEditColumn);
			});
			
			releaseHandle.cancelOpValBtn = dojo.connect(cancelOpValBtn, "onClick", function(){
				dijit.byId("optionalValSel").reset();
				dijit.byId("editWOEOptionalVal").hide();
			});
		},
		
		
		
		finalize: function(isSyncData){
			if(isSyncData){
				// get edited data array back to current column.
				WOE_Setting.nominalComponent._syncNominalEditDatas(WOE_Setting.nominalComponent.releaseHandle.currentEditColumn);
			}
			var nominalGrid = dijit.byId("nominalEditGrid");
			if(nominalGrid && nominalGrid.edit.isEditing()){
				nominalGrid.edit.apply();//for IE cannot be apply by itself.
			}
			dojo.disconnect(WOE_Setting.nominalComponent.releaseHandle.groupingAddBtn);
			dojo.disconnect(WOE_Setting.nominalComponent.releaseHandle.groupingDelBtn);
			dojo.disconnect(WOE_Setting.nominalComponent.releaseHandle.groupingCalculateBtn);
			dojo.disconnect(WOE_Setting.nominalComponent.releaseHandle.submitOpValBtn);
			dojo.disconnect(WOE_Setting.nominalComponent.releaseHandle.cancelOpValBtn);
			delete WOE_Setting.nominalComponent.releaseHandle.groupingAddBtn;
			delete WOE_Setting.nominalComponent.releaseHandle.groupingDelBtn;
			delete WOE_Setting.nominalComponent.releaseHandle.groupingCalculateBtn;
			delete WOE_Setting.nominalComponent.releaseHandle.submitOpValBtn;
			delete WOE_Setting.nominalComponent.releaseHandle.cancelOpValBtn;
			delete WOE_Setting.nominalComponent.releaseHandle.currentEditColumn;
		},

		/**
		 * sync nominal edit datas into current selected column info.
		 */
		_syncNominalEditDatas: function(editColumn){
			if(!editColumn){
				//never opend section. So nothing to sync.
				return;
			}
			var nominalGrid = dijit.byId("nominalEditGrid"),
				rowCount = nominalGrid.rowCount,
				nominalDatas = new Array();
			for(var i = 0;i < rowCount;i++){
				var nominalItem = nominalGrid.getItem(i),
					pureItem = {};
				pureItem[WOE_Setting._WOENominalNode.id] = nominalItem[WOE_Setting._WOENominalNode.id];
				pureItem[WOE_Setting._WOENominalNode.optionalVal] = nominalItem[WOE_Setting._WOENominalNode.optionalVal][0] == undefined ? [] : nominalItem[WOE_Setting._WOENominalNode.optionalVal][0].split(",");
				//fixed MINERWEB-662	some IE explore can be filter undefined value. So manually check it.
				pureItem[WOE_Setting._WOENominalNode.woeVal] = nominalItem[WOE_Setting._WOENominalNode.woeVal] ? nominalItem[WOE_Setting._WOENominalNode.woeVal] : "0";
				nominalDatas.push(pureItem);
			}
			editColumn.InforList = nominalDatas;
		},
		
		/**
		 * private method.
		 * return true if records are allright, false if any optional values was null string e.g.("").
		 */
		_validateRecord: function(){
			var nominalGrid = dijit.byId("nominalEditGrid"),
				rowLength = nominalGrid.rowCount;
			if(rowLength < 1){
				return false;
			}
			for(var i = 0;i < rowLength;i++){
				 var record = nominalGrid.getItem(i);
				 if(!WOE_Setting.nominalComponent.validateNominalItem(record)){
					return false;
				 }
			}
			return true;
		},
		
		validateNominalItem: function(nominalItem){
			var nominalVal = WOE_Setting.getVal(nominalItem[WOE_Setting._WOENominalNode.optionalVal]);
			return nominalVal != "";
		},
		
		//build all of available values in current column.
		_buildAvailableOptionalVals: function(currentNominalItem){
			var columnGrid = dijit.byId("columnsGrid"),
				columnStore = columnGrid.store,
				currentColumn = columnGrid.selection.getSelected(),
				allOfOpVals = dojo.clone(currentColumn[0].columnValues),
				currentColumnNominalGrid = dijit.byId("nominalEditGrid"),
				currentColumnNominalGridStore = currentColumnNominalGrid.store;
			for(var i = 0;i < currentColumnNominalGrid.rowCount;i++){
				var nominalItem = currentColumnNominalGrid.getItem(i),
					optionalVals = currentColumnNominalGridStore.getValue(nominalItem, WOE_Setting._WOENominalNode.optionalVal).split(",");
				if(nominalItem == currentNominalItem){
					continue;
				}
				for(var j = 0;j < optionalVals.length;j++){
					var idx = dojo.indexOf(allOfOpVals, optionalVals[j]);
					if(idx == -1){// avoid '[""]' of new record. 
						continue;
					}
					allOfOpVals.splice(idx, 1);
				}
			}
			return allOfOpVals;
		},
		
		//attach values to column information, which type is nominal
		attachNominalValues: function(nominalColumnValues, woeEditColumns){
			for(var i = 0;i < woeEditColumns.length;i++){//for each woe setting column
				var values = nominalColumnValues[woeEditColumns[i].columnName];
				if(values){
					woeEditColumns[i].columnValues = values;
				}
			}
		},
		
		/**
		 * create or rebuild data grid for nominal
		 */
		buildNominalGrid: function(nominalRange){
			var nominalStore = new dojo.data.ItemFileWriteStore({
				data: {
					items: nominalRange
				}
			});
			var grid = dijit.byId("nominalEditGrid");
			if(!grid){
				grid = new dojox.grid.DataGrid({
					store: nominalStore,
					query: {"groupInfo": "*"},
					structure: [
						{name: alpine.nls.woe_setting_nominal_grid_id,field: WOE_Setting._WOENominalNode.id,width: "20%"},
						{
							name: alpine.nls.woe_setting_nominal_grid_optionalVal,
							field: WOE_Setting._WOENominalNode.optionalVal,
							width: "50%",
							editable: true,
							type: dojox.grid.cells._Widget,
							widgetClass: dijit.form.Button,
							widgetProps: {
								id: "editOptionValBtn",
								label: alpine.nls.woe_setting_nominal_button_editOpVal,
								baseClass: "workflowButton",
								onClick: function(){
									
									var row = grid.selection.getSelected()[0],
										currentOpVals = grid.store.getValue(row, WOE_Setting._WOENominalNode.optionalVal).split(","),
										optionalValSel = dijit.byId("optionalValSel"),
										availableVals = WOE_Setting.nominalComponent._buildAvailableOptionalVals(row);
		
									optionalValSel.removeOption(optionalValSel.getOptions());
									for(var i = 0;i < availableVals.length;i++){
										var op = {
											label: availableVals[i],
											value: availableVals[i]
										};
										if(dojo.indexOf(currentOpVals, availableVals[i]) != -1){
											op.selected = true;
										}
										optionalValSel.addOption(op);
									}
							
							
									var options = optionalValSel.getOptions();
									if(options.length < 1){
										popupComponent.alert(alpine.nls.woe_setting_nominal_alert_noAvailableValue, function(){
											dijit.byId("nominalEditGrid").removeSelectedRows();
										});
										return;
									}
									dijit.byId("editWOEOptionalVal").show();
								} 
							}
						},
						{name: alpine.nls.woe_setting_nominal_grid_woeVal,field: WOE_Setting._WOENominalNode.woeVal,width: "30%"}
					]
				},"nominalEditGrid");
				dojo.connect(grid, "onStartEdit", grid, function(cell, rowIdx){
					//avoid multiple select row
					this.selection.deselectAll();
					this.selection.select(rowIdx);
				});
				grid.startup();
			}else{
				grid.setStore(nominalStore);
				grid.render();
			}
		},
		
		/**
		 * create new row for nominal record.
		 * fired on click add button in nominal panel.
		 */
		addRecord: function(){
			var grid = dijit.byId("nominalEditGrid"),
				lastId = grid.rowCount;
				newRecord = {},
				availableOptVals = WOE_Setting.nominalComponent._buildAvailableOptionalVals(null);
			if(grid && grid.edit.isEditing()){
				grid.edit.apply();//for IE cannot be apply by itself.
			}
			if(availableOptVals.length < 1){
				popupComponent.alert(alpine.nls.woe_setting_nominal_alert_noAvailableValue);
				return;
			}
			newRecord[WOE_Setting._WOENominalNode.id] = ++lastId;
			newRecord[WOE_Setting._WOENominalNode.optionalVal] = "";
			grid.store.newItem(newRecord);
			grid.render();

			//avoid data can not sync to column record if click current record after excute add/del. 
			WOE_Setting.nominalComponent._syncNominalEditDatas(WOE_Setting.nominalComponent.releaseHandle.currentEditColumn);
		},
		
		/**
		 * fired on click delete button in nominal panel.
		 */
		delRecord: function(){
			var nominalGrid = dijit.byId("nominalEditGrid"),
				deleteNominalRecords;
	
			deleteNominalRecords = nominalGrid.selection.getSelected();
			if(deleteNominalRecords.length < 1){
				popupComponent.alert(alpine.nls.woe_setting_alert_delete_norecord);
				return;
			}
			popupComponent.confirm(alpine.nls.woe_setting_confirm_delete, {
				handle: function(){
					nominalGrid.removeSelectedRows();
					WOE_Setting.realignRecordId("nominalEditGrid");
					nominalGrid.render();
					//avoid data can not sync to column record if click current record after excute add/del. 
					WOE_Setting.nominalComponent._syncNominalEditDatas(WOE_Setting.nominalComponent.releaseHandle.currentEditColumn);
				}
			}, {
				handle: function(){
					nominalGrid.selection.deselectAll();
				}
			});
		}
	},
		
	/*-----------------------Methods-----------------------*/
	showWoeSetting: function(prop){
		var dataList;
		// get selected columns
		var propList = WOE_Setting._getOperatorDTO().propertyList;
		var selectedColumns,dependentColumn;
		for(var pli = 0;pli < propList.length;pli++){
			if(propList[pli].name == "columnNames"){
				if (!propList[pli].valid)
                {
                    popupComponent.alert(alpine.nls.woe_setting_alert_invalidcolumns);
                    return;
                }

                selectedColumns = propList[pli].selected;
			}
		}
		if(selectedColumns.length < 1){
			popupComponent.alert(alpine.nls.woe_setting_alert_nocolumn);
			return;
		}
		//delete selected column from selected list if it is dependent column.
		var idx = dojo.indexOf(selectedColumns, CurrentDependentColumn);
		if(idx != -1){
			selectedColumns.splice(idx, 1);
		}
		
		WOE_Setting._currentWOESettigData = prop;
		dijit.byId("woeSettingWindow").titleBar.style.display = "none";
		dijit.byId("woeSettingWindow").show();
		dataList = prop.woeModel.calculateElements;
		
		//merge selected columns and woe setting columns
		var editColumns = WOE_Operator.mergeColumn(selectedColumns, dataList);
		
		WOE_Setting._adaptWOEInforListArray(editColumns);
		WOE_Setting.nominalComponent.attachNominalValues(prop.woeModel.nominalColumnValues, editColumns);
		WOE_Setting.startup(editColumns);
	},
	
	submitWoeSetting: function(){
		//following is force synchronize edit data to grid.
//		dijit.byId("editType").selectChild("woeDefaultEditGrid", true);
		WOE_Setting.numericComponent._syncNumericEditDatas(WOE_Setting.numericComponent.releaseHandle.currentEditColumn);
		WOE_Setting.nominalComponent._syncNominalEditDatas(WOE_Setting.nominalComponent.releaseHandle.currentEditColumn);
		
		var columnGrid = dijit.byId("columnsGrid"),
			rowCount = columnGrid.rowCount,
			columnList = new Array();
		
		for(var i = 0;i < rowCount;i++){
			var originalColumn = columnGrid.getItem(i);
			if(WOE_Setting.getVal(originalColumn[WOE_Setting._WOEInfoList.dataType]) == WOE_Setting._WOEColumnType.numeric){
				for(var j = 0;j < originalColumn.InforList.length;j++){
					var isPass = WOE_Setting.numericComponent.validateNumericItem(originalColumn.InforList[j], originalColumn.InforList, j);
					if(!isPass){
						popupComponent.alert(alpine.nls.woe_setting_grid_validate_false);
						return;
					}
				}
			}else{
				for(var j = 0;j < originalColumn.InforList.length;j++){
					var isPass = WOE_Setting.nominalComponent.validateNominalItem(originalColumn.InforList[j]);
					if(!isPass){
						popupComponent.alert(alpine.nls.woe_setting_grid_validate_false);
						return;
					}
				}
			}
			columnList.push(WOE_Setting._restructureColumnInfoFromGrid(originalColumn));
		}
		WOE_Setting._currentWOESettigData.woeModel.calculateElements = columnList;

        var sourceButtonId = getSourceButtonId(WOE_Setting._currentWOESettigData);
        setButtonBaseClassValid(sourceButtonId) ;

		WOE_Setting.closeWoeSetting();
	},
	
	closeWoeSetting: function(){
		WOE_Setting.numericComponent.finalize(false);
		WOE_Setting.nominalComponent.finalize(false);
		dijit.byId("woeSettingWindow").hide();
	},
	
	/**
	 * initialize Setting dialog.
	 */
	startup: function(settingData){
		this.buildColumnGrid(settingData);
		if(settingData.length > 0){//select the first row
			var columnGrid = dijit.byId("columnsGrid"),
				rowIdx = 0;
			columnGrid.selection.select(rowIdx);
			WOE_Setting.openEditor(columnGrid.getItem(rowIdx));
		}
		//binding event on button
	},
	
	/**
	 * create or rebuild columns information.
	 */
	buildColumnGrid: function(calculatedColumns){
		var columnStore = new dojo.data.ItemFileWriteStore({
			data: {
				items: calculatedColumns
			}
		});
		var grid = dijit.byId("columnsGrid");
		if(!grid){
			grid = new dojox.grid.DataGrid({
				store: columnStore,
				query: {"columnName": "*"},
				selectionMode: "single",
				structure: [
					{name: "Column Name",field: this._WOEInfoList.columnName,width: "30%"},
					{name: "Gini",field: this._WOEInfoList.gini,width: "35%"},
					{name: "Info Value",field: this._WOEInfoList.infoValue,width: "35%"}
				]
			},"columnsGrid");
			grid.startup();
			dojo.connect(grid,"onRowClick", grid,function(e){
				WOE_Setting.openEditor(this.getItem(this.focus.rowIndex));
			});
		}else{
			grid.setStore(columnStore);
		}
	},
	
	//realign id of records after delete operation.
	realignRecordId: function(gridWidgetID){
		var grid = dijit.byId(gridWidgetID);
		if(!grid){
			return;
		}
		var rowCount = grid.rowCount;
		if(grid.store._arrayOfTopLevelItems.length == 0){
			return;//if delete all of records. then not need to realign id.
		}
		for(var i = 0;i < rowCount;i++){
			var item = grid.getItem(i);
			if(item == null){//grid can not be refresh row immediately, so check if item was deleted, ignore it.
				continue;
			}
			grid.store.setValue(item, "groupInfo", i + 1);
		}
	},
	
	openEditor: function(row){
		var group = row.InforList,
			grid = dijit.byId("columnsGrid");
			columnStore = grid.store;
		var editTypeContainer = dijit.byId("editType"),
			editPanelArray = editTypeContainer.getChildren();
		//following is enable to invoke finalize function, which is current render panel.
//		editTypeContainer.selectChild("woeDefaultEditGrid", true);
		WOE_Setting.numericComponent.finalize(false);
		WOE_Setting.nominalComponent.finalize(false);
		
		switch(grid.store.getValue(row,WOE_Setting._WOEInfoList.dataType)){
		case WOE_Setting._WOEColumnType.numeric:
			dijit.byId("editType").selectChild("numericType",true);
			WOE_Setting.numericComponent.buildNumericGrid(refineGroup(group, WOE_Setting._WOENumericNode));
			WOE_Setting.numericComponent.initialize();
			break;
		case WOE_Setting._WOEColumnType.text:
			dijit.byId("editType").selectChild("nominalType",true);
			
			WOE_Setting.nominalComponent.buildNominalGrid(refineGroup(group, WOE_Setting._WOENominalNode, function(attr, item){
				if(WOE_Setting._WOENominalNode[attr] == WOE_Setting._WOENominalNode.optionalVal){// check if optional values, then make it into string
					return item[WOE_Setting._WOENominalNode[attr]].toString();
				}else{
					return WOE_Operator.getValueFromGrid(item[WOE_Setting._WOENominalNode[attr]]);
				}
			}));
			//As onEditCell depend on grid, so initialize function must be follow buildGrid function.
			WOE_Setting.nominalComponent.initialize();
			break;
		}
		//remove fields which create by dojo grid
		function refineGroup(items, originalBean, fn){
			var refinedArray = new Array();
			fn = fn || function(attr, item){
				return WOE_Operator.getValueFromGrid(item[originalBean[attr]]);
			};
			for(var i = 0; i < items.length; i++){
				var item = items[i];
				var res = {};
				for(var attr in originalBean){
					res[originalBean[attr]] = fn(attr, item);
				}
				refinedArray.push(res);
			}
			return refinedArray;
		}
	},
	
	/**
	 * fired by click auto group button
	 */
	autoGroup: function(){
		WOE_Setting._requestAutoCalculate(dijit.byId("columnsGrid").store._arrayOfTopLevelItems, WOE_Setting._fillGroupData);
	},
	
	/**
	 * fired by auto calculate for single column
	 */
	autoCalculate: function(){
		var selectedColumns = dijit.byId("columnsGrid").selection.getSelected();
		if(selectedColumns.length < 1){
			popupComponent.alert(alpine.nls.woe_setting_column_noselected);
			return;
		}
		WOE_Setting._requestAutoCalculate(selectedColumns, WOE_Setting._fillElementGroupData);
	},
	
	/**
	 * fired by click calculate button to calculate column's WOE value.
	 * argument is validat result for numeric or nominal Grid.
	 */
	calculate: function(dataIsAllright){
		if(!dataIsAllright){
			popupComponent.alert(alpine.nls.woe_setting_grid_validate_false);
			return;
		}
		var selectedColumns = dijit.byId("columnsGrid").selection.getSelected();
		if(selectedColumns.length < 1){
			popupComponent.alert(alpine.nls.woe_setting_column_noselected);
			return;
		}
		
		WOE_Setting._requestCalculate(selectedColumns[0], function(calculatedColumn){
			WOE_Setting._fillElementGroupData([calculatedColumn]);
		});
	},
	
	_requestCalculate: function(column, fn){
		//progressBar.showLoadingBar();
		var parameters = WOE_Setting._buildWOECalculateParam([column]);
		ds.post(
			baseURL + "/main/flow/woeOperator.do?method=calculate",
			parameters,
			fn , null, false, "woeSettingWindow"
		);
	},
	
	_requestAutoCalculate: function(columns, fn){
		//progressBar.showLoadingBar();
		var parameters = WOE_Setting._buildWOECalculateParam(columns);
		ds.post(
			baseURL + "/main/flow/woeOperator.do?method=autoCalculate",
			parameters,
			fn, null, false, "woeSettingWindow"
		);
	},
	
	_adaptWOEInforListArray: function(settingDatas){
		for(var i = 0; i < settingDatas.length; i++){
			var infoList;
			//make sure the infoList ready
			if(settingDatas[i].InforList == undefined){
				settingDatas[i].InforList = [];
			}
			infoList = settingDatas[i].InforList;
			//synchronize column's data type
			settingDatas[i][WOE_Setting._WOEInfoList.dataType] = WOE_Setting._currentWOESettigData.woeModel.columnTypeInfo[settingDatas[i].columnName];
			for(var j = 0;j < infoList.length;){
				infoList[j].groupInfo = ++j;
			}
		}
		return settingDatas;
	},
	
	/**
	 * for all of columns calculate
	 */
	_fillGroupData: function(settingDatas){
		if(settingDatas.error_code){
			//progressBar.closeLoadingBar();
			popupComponent.alert(settingDatas.message);
		}else{
			WOE_Setting._adaptWOEInforListArray(settingDatas);
			WOE_Setting.nominalComponent.attachNominalValues(WOE_Setting._currentWOESettigData.woeModel.nominalColumnValues, settingDatas);
			WOE_Setting.startup(settingDatas);
			//progressBar.closeLoadingBar();
		}
	},
	
	/**
	 * for single columns calculate
	 */
	_fillElementGroupData: function(settingDatas){
		if(settingDatas.error_code){
			//progressBar.closeLoadingBar();
			if (settingDatas.error_code == -1) {
				popupComponent.alert(alpine.nls.no_login, "",function() {
					window.top.location.pathname = loginURL;
				});
			}
			else if (settingDatas.error_code == -2) {
				popupComponent.alert(alpine.nls.session_ended, "",function() {
					window.top.location.pathname = loginURL;
				});
			}
			else if(settingDatas.message){
				popupComponent.alert(settingDatas.message);
			}
			return;	
		}
		var columnGrid = dijit.byId("columnsGrid"),
			selectedColumn = columnGrid.selection.getSelected()[0],
			columnStore = columnGrid.store;
		WOE_Setting._adaptWOEInforListArray(settingDatas);
		WOE_Setting.nominalComponent.attachNominalValues(WOE_Setting._currentWOESettigData.woeModel.nominalColumnValues, settingDatas);
		//fill column grid and refresh.
		columnStore.setValue(selectedColumn, WOE_Setting._WOEInfoList.gini, settingDatas[0][WOE_Setting._WOEInfoList.gini]);
		columnStore.setValue(selectedColumn, WOE_Setting._WOEInfoList.infoValue, settingDatas[0][WOE_Setting._WOEInfoList.infoValue]);
		var calulatedInfoList = settingDatas[0][WOE_Setting._WOEInfoList.infoList];
		for(var i = 0;i < calulatedInfoList.length;i++){
			for(var item in WOE_Setting._WOENumericNode){
				calulatedInfoList[i][WOE_Setting._WOENumericNode[item]] = [calulatedInfoList[i][WOE_Setting._WOENumericNode[item]]];
			}
		}
		
		selectedColumn[WOE_Setting._WOEInfoList.infoList] = calulatedInfoList;
		columnGrid.render();
		
		//re-build info list grid.
		WOE_Setting.openEditor(selectedColumn);
		//progressBar.closeLoadingBar();
	},
	
	/**
	 * create calculate parameters for server side.
	 */
	_buildWOECalculateParam: function(columns){
		var opDTO = WOE_Setting._getOperatorDTO(),
			columnList = new Array(),
			propertyList = WOE_Setting._getOperatorDTO().propertyList,
			dependentColumn,
			goodValue,
			columnNames;
		
		for(var i = 0;i < columns.length;i++){
			var originalColumn = columns[i];
			columnList.push(WOE_Setting._restructureColumnInfoFromGrid(originalColumn));
		}
		
		get_current_operator_data(propertyList,false);
		for(var idx = 0;idx < propertyList.length;idx++){
			switch(propertyList[idx].name){
			case "dependentColumn":
				dependentColumn = propertyList[idx].value
				break;
			case "goodValue":
				goodValue = propertyList[idx].value
				break;
			case "columnNames":
				columnNames = propertyList[idx].selected.toString();
				break;
			}
		}
		return {
			flowInfo: opDTO.flowInfo,
			calculateElements: columnList,
			dependentColumn: dependentColumn,
			goodValue: goodValue,
			columnNames: columnNames,
			operatorUUID: WOE_Setting._getOperatorDTO().uuid
		};
	},
	
	_restructureColumnInfoFromGrid: function(original){
		var columnInfo = {};
		columnInfo.columnName = WOE_Operator.getValueFromGrid(original[WOE_Setting._WOEInfoList.columnName]);
		columnInfo.dataType = WOE_Operator.getValueFromGrid(original[WOE_Setting._WOEInfoList.dataType]);
		columnInfo.gini = WOE_Operator.getValueFromGrid(original[WOE_Setting._WOEInfoList.gini]) ? WOE_Operator.getValueFromGrid(original[WOE_Setting._WOEInfoList.gini]) : "0";
		columnInfo.inforValue = WOE_Operator.getValueFromGrid(original[WOE_Setting._WOEInfoList.infoValue]) ? WOE_Operator.getValueFromGrid(original[WOE_Setting._WOEInfoList.infoValue]) : "0";
		columnInfo.InforList = _restructureInfoList(original[WOE_Setting._WOEInfoList.infoList], original[WOE_Setting._WOEInfoList.dataType][0]);
		
		return columnInfo;
		//restructure info list by data type
		function _restructureInfoList(infoList, dataType){
			var templateClass,
				resInfoList = new Array(),
				getValHandle;
			switch(dataType){
			case WOE_Setting._WOEColumnType.numeric:
				templateClass = WOE_Setting._WOENumericNode;
				getValHandle = function(item, attr){
					return WOE_Operator.getValueFromGrid(item[attr]);//alpine.flow.WorkFlowVariableReplacer.replaceVariable(WOE_Operator.getValueFromGrid(item[attr]));
				};
				break;
			case WOE_Setting._WOEColumnType.text:
				templateClass = WOE_Setting._WOENominalNode;
				getValHandle = function(item, attr){
					if(attr == WOE_Setting._WOENominalNode.optionalVal){// check if optional values then just return it. Because it already a Array
						return item[attr];
					}else{
						return WOE_Operator.getValueFromGrid(item[attr]);
					}
				};
				break;
			}
			for(var i = 0;i < infoList.length;i++){
				var info = infoList[i],
					resInfo = {};
				for(var fieldNameItem in templateClass){
					var fieldName = templateClass[fieldNameItem];
					resInfo[fieldName] = getValHandle(info, fieldName);
				}
				resInfoList.push(resInfo);
			}
			return resInfoList;
		}
	},
	
	/**
	 * change auto group, add, delete, auto calculate, calculate of buttons state.
	 */
	changeButtonsState: function(activity){
		dijit.byId("woe_grouping_add").set("disabled", !activity);
		dijit.byId("woe_grouping_delete").set("disabled", !activity);
		dijit.byId("woe_grouping_autoCalculate").set("disabled", !activity);
		dijit.byId("woe_grouping_calculate").set("disabled", !activity);
		dijit.byId("woe_autoGrouping").set("disabled", !activity);
		dijit.byId("woe_submit_prop").set("disabled", !activity);
		dijit.byId("woe_cancel_prop").set("disabled", !activity);
	},
	
	_releaseResources: function(){
		WOE_Setting._currentWOESettigData = null;
	},
	
	_getOperatorDTO: function(){
		return CurrentOperatorDTO;
	},
	
	getVal: function(val){
		return dojo.isArray(val) ? val[0] : val;
	}
};
/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * var_derived.js
 * 
 * Author sam_zang
 * Version 3.0
 * Date Dec 4, 2011
 */

 
var VDCurrentModel = null;
var VarColumnList = null;
var var_derivedGrid = null;
var var_derivedStore = null;
var sourceButtonId = null;
var myCodeMirror = null;

var derivedGridRowNum = 1;

var derivedEditingRowNum;

dojo.ready(function(){
    dojo.connect(var_derivedGrid,"onBlur",function(){
        resetVarDerivedForm();
    });

    myCodeMirror = CodeMirror.fromTextArea(dojo.byId("var_sql_spec"), {
        lineNumbers: true,
        matchBrackets: true,
        indentUnit: 4,
        mode: "text/x-plsql",
        extraKeys: {"Ctrl-Space": "autocomplete","Shift-Ctrl-Space": "autocomplete"}
    });

});

function showVariableDerivedEditDialog(prop) {
	if (prop.derivedFieldsModel == null) {
		var model = {};
		model.derivedFieldsList = new Array();
		model.selectedFieldList = new Array();
		prop.derivedFieldsModel = model;
	}
	derivedGridRowNum = 1;//reset row index to 1
	VDCurrentModel = prop.derivedFieldsModel;
	create_var_derivedGrid(prop);

	var fulllist = prop.fullSelection;
	var selected = VDCurrentModel.selectedFieldList;
	var tbl = dojo.byId("var_derived_list_table");
	VarColumnList = fulllist;
	
//	disable_var_derived_buttons();
	enable_var_derived_create();
	clear_var_derived_from();
	var colName = dijit.byId("var_dev_result_column");
//	var handler = dojo.connect(colName, "onKeyUp", function() {
//		var_derived_button_mode(colName.get('value'));
//	});
//	sp_property_event_handlers.push(handler);

    sourceButtonId = getSourceButtonId(prop);
    dijit.byId("variableDerivedConfigEditDialog").titleBar.style.display = 'none';
	dijit.byId("variableDerivedConfigEditDialog").show();
	

	var grid = dijit.byId("colNamesItemGrid4Property");
	if(grid!=null){
        //destroy colNamesItemGrid4Property
		grid.destroyRecursive();
    	grid = null;
    	//
    	//dijit.byId("columnSelectionDialogBtn_OK").set("disabled",false);
	}

    new_add_check_button_panel(tbl,fulllist, selected,alpine.nls.var_dev_available_column);

    //update var_dev_data_type options
    if(varIsHadoop()){
        var varDevDataSelect = dijit.byId('var_dev_data_type');
        //Text-->chararray Numeric-->double Int-->int Float-->
        var dataTypes = alpine.props.HadoopDataTypeUtil.getAllHadoopTypes();
        //var dataTypes = ["int","long","float","double","chararray","bytearray"];
        var items = [];
        for(var i=0;i<dataTypes.length;i++){
            items.push({label:dataTypes[i],value:dataTypes[i]});
        }
        varDevDataSelect.options = items;
        varDevDataSelect.startup();
        varDevDataSelect.set('value',dataTypes[1]);
        dojo.byId('sqlExpression_title').innerHTML = alpine.nls.sqlExpression_title_java;
        dijit.byId('pigSyntaxHelperButton').set('style',"display:block;");
        dijit.byId('pigSyntaxHelperDialog').titleBar.style.display = 'none';
        myCodeMirror.setOption("mode", "text/x-pig");

        CodeMirror.commands.autocomplete = function(cm) {
            CodeMirror.simpleHint(cm, CodeMirror.pigHint);
        }
    }else{
        var varDevDataSelect = dijit.byId('var_dev_data_type');
        var dataTypes = ["BIGINT","BOOLEAN","BIT","BIT VARYING","CHAR","DATE","DOUBLE PRECISION","NUMERIC","INTEGER","VARCHAR"];
        var items = [];
        for(var i=0;i<dataTypes.length;i++){
            items.push({label:dataTypes[i],value:dataTypes[i]});
        }
        varDevDataSelect.options = items;
        varDevDataSelect.startup();
        dojo.byId('sqlExpression_title').innerHTML = alpine.nls.sqlExpression_title_sql;
        myCodeMirror.setOption("mode", "text/x-plsql");

        CodeMirror.commands.autocomplete = function(cm) {
            CodeMirror.simpleHint(cm, CodeMirror.plsqlHint);
        }
    }
    myCodeMirror.setValue("");
}
function varIsHadoop(){
       return ("HadoopVariableOperator"==CurrentOperatorDTO.classname);
}

function new_add_check_button_panel(parent,list, selected, gridTitle){
    if(parent!=null){
        parent.innerHTML ="";
        var listObj = _buildListObject(list);
        var colNamesItemStore = new dojo.data.ItemFileReadStore({
            data:{
                identifier:"colName",
                label:"colName",
                items:listObj
            }
        });
        var colNamesGridLayout = [
            {type: "dojox.grid._CheckBoxSelector"},
            [
                {name:gridTitle, field: "colName", width: "100%"}
            ]
        ];

        var grid = dijit.byId("varDerivedColNamesGrid");
        //MINERWEB-1012
        if(grid!=null){
            //destroy colNamesItemGrid4Property
            grid.destroyRecursive();
            grid = null;
        }

        if(!grid){
            var grid = new dojox.grid.DataGrid({
                id:'varDerivedColNamesGrid',
                store:colNamesItemStore,
                structure:colNamesGridLayout,
                style:"heigth:100%;width:100%;",
                query: {"colName": "*"},
                //rowsPerPage:alpine.GridMaxRowsPerPage,
                canSort: function(){return false;},
                onRowClick: function(){
                	//nothing to do.
                },
                onDblClick : function(e){
                    if(e.rowIndex == undefined){
    					return;
    				}
    				var record = this.getItem(e.rowIndex);
                    var value = toolkit.getValue(record.colName);
                    if (!varIsHadoop())
                    {
                        value = "\"" + value + "\" ";
                    }
                    myCodeMirror.replaceSelection(value.trim()  + " ", "end");
                    myCodeMirror.focus();
                    //dojo.byId("var_sql_spec").value += toolkit.getValue(record.colName);
                }
            },dojo.create("div",null,parent));
            //dojo.place(grid.domNode,parent,"only");
            grid.startup();
        }
        //checkbox select
        _buildGrideSelectStatus(grid,selected);
    }
}

function _buildGrideSelectStatus(grid,selected){
    if(grid!=null && null!=selected){
       var selectedStatus = grid.selection.selected;
       var gridItems = grid.store._arrayOfAllItems;
        if(null!=gridItems){
            for(var i=0;i<gridItems.length;i++){
                if(dojo.indexOf(selected,gridItems[i].colName[0])!=-1){
                    grid.selection.setSelected(i,true);
                }
            }
        }
    }

}


function close_quantile_derived_dialog(){
    _validateVariableBtn();
    dijit.byId('pigSyntaxHelperButton').set('style', "display:none;");
	dijit.byId('variableDerivedConfigEditDialog').hide();
    //dojo.byId("var_dev_result_column").disabled = "";
}

function _validateVariableBtn(){
    if(CurrentOperatorDTO.classname == "VariableOperator"){
        var variableNum = 0;
        var quantileVariableNum = 0;
        quantileVariableNum =_getQuqntileVariableNumFromPropList();
        variableNum = _getVariableNumFromGrid();
        if(variableNum>0 || quantileVariableNum>0){
            _setDefVariableBtnValid();
            _setQuantileVariableBtnValid();
        }else{
            _setDefVariableBtnInvalid();
            _setQuantileVariableBtnInalid();
        }
    }
}

function _getVariableNumFromGrid(){
    var num = 0;
    var varGrid = var_derivedGrid;
    if(null!=varGrid){
        var items = varGrid.store._arrayOfAllItems;
        if(null!=items){
            for(var i=0;i<items.length;i++){
                if(null!=items[i]){
                    num++;
                }
            }
        }
    }
    return num;
}

function validateSameNameWithColumn(){
    var varGrid = var_derivedGrid;
    var colGrid = dijit.byId("varDerivedColNamesGrid");
    if(null!=varGrid && null!=colGrid){
       var  varItems = varGrid.store._arrayOfAllItems;
       var resultCols = [];
        for(var j=0;j<varItems.length;j++){
            if(varItems[j]==null){
                continue;
            }
            resultCols.push(varItems[j].resultColumnName[0]);
        }
       var colItems = colGrid.store._arrayOfAllItems;
       var colStatus = colGrid.selection.selected;
       if(colStatus!=null){
           for(var i=0;i<colStatus.length;i++){
               if(colStatus[i]==true){
                  if(dojo.indexOf(resultCols,colItems[i].colName[0])!=-1){
                      return true;
                  }
               }
           }
       }
    }

    return false
}

function update_var_derived_data() {
    //save data tip
   var varName = dijit.byId("var_dev_result_column").get("value");
   var expressionValue = myCodeMirror.getValue();
   var btnCreate = dijit.byId("var_derived_create_button");
   var btnUpdate = dijit.byId("var_derived_update_button");

   if(dojo.trim(varName)!=""
    && dojo.trim(expressionValue)!=""){
      var rows = var_derivedGrid.store._arrayOfAllItems;
      if(null!=rows && rows.length>0){
         var selectIndex = -1;
          for(var i=0;i<rows.length;i++){
              if(rows[i].resultColumnName[0]==varName){
                  derivedEditingRowNum = rows[i].rowNum[0];
                  selectIndex = i;
                  break;
              }
          }
          if(-1!=selectIndex){
              var_derivedGrid.selection.select(selectIndex);

              btnCreate.set("disabled",true);
              btnUpdate.set("disabled",false);
          }
      }
   }

   if(btnCreate.get("disabled")==false
       && btnUpdate.get("disabled")==true
       && dojo.trim(varName)!=""
       && dojo.trim(expressionValue)!=""){
       popupComponent.confirm(alpine.nls.var_dev_column_create_tip,alpine.nls.var_dev_column_create_title,{label:alpine.nls.var_dev_column_btn_create,handle:function(){
           if(create_var_derived()!=false){
               real_execute_update_var_derived_data();
           }
       }},{label:alpine.nls.var_dev_column_btn_cancel,handle:function(){
           real_execute_update_var_derived_data();
       }});

   }

   if(btnCreate.get("disabled")==true
       && btnUpdate.get("disabled")==false
       && dojo.trim(varName)!=""
       && dojo.trim(expressionValue)!=""
       && valueDiffWithSelected(varName,expressionValue,dijit.byId("var_dev_data_type").get("value"))==true){
       popupComponent.confirm(alpine.nls.var_dev_column_update_tip,alpine.nls.var_dev_column_update_title,{label:alpine.nls.var_dev_column_btn_update,handle:function(){
           if(update_var_derived()!=false){
               real_execute_update_var_derived_data();
           }
       }},{label:alpine.nls.var_dev_column_btn_cancel,handle:function(){
           //real_execute_update_var_derived_data();
       }});
   }

  if(!(btnCreate.get("disabled")==false
      && btnUpdate.get("disabled")==true
      && dojo.trim(varName)!=""
      && dojo.trim(expressionValue)!="")
      &&
      !(btnCreate.get("disabled")==true
      && btnUpdate.get("disabled")==false
      && dojo.trim(varName)!=""
      && dojo.trim(expressionValue)!=""
      && valueDiffWithSelected(varName,expressionValue,dijit.byId("var_dev_data_type").get("value"))==true)){
      real_execute_update_var_derived_data();
  }

}



function real_execute_update_var_derived_data(){
    if(validateSameNameWithColumn()==true){
        popupComponent.alert(alpine.nls.var_dev_column_exist);
        return false;
    }

    VDCurrentModel.selectedFieldList = new Array();
    //var len = colmun_checkbox_list.length;
    var idx = 0;
    //Will begin
    var grid = dijit.byId('varDerivedColNamesGrid');
    if(grid!=null){
        // var item = grid.selection.getSelected();
        // if(item && item.length!=null && item.length>0){
        // for ( var i = 0; i < item.length; i++) {
        // VDCurrentModel.selectedFieldList.push(item[i].colName[0]);
        // }
        //
        //}
        // Fix lazy load
        var item = grid.selection.selected;
        var storeItem = grid.store._arrayOfAllItems;
        if(item && item.length!=null && item.length>0 && storeItem!=null && storeItem.length!=null && storeItem.length>0){
            for ( var i = 0; i < item.length; i++) {
                if(item[i]==true){
                    VDCurrentModel.selectedFieldList.push(storeItem[i].colName[0]);
                }
            }
        }
    }
    //Will end

    var updateModel = function(items, request) {
        var model = VDCurrentModel;
        model.derivedFieldsList = new Array();
        for ( var i = 0; i < items.length; i++) {
            var val = {};
            val.resultColumnName = items[i].resultColumnName[0];
            val.sqlExpression = items[i].sqlExpression[0];
            val.dataType = items[i].dataType[0];
            model.derivedFieldsList[i] = val;
        }
    };

    var request = var_derivedStore.fetch({
        query : {
            resultColumnName : "*"
        },
        onComplete : updateModel
    });

    var stroeNum = 0;
    var length = var_derivedGrid.store._arrayOfAllItems.length;
    for(var j=0;j<length;j++){
        if(var_derivedGrid.store._arrayOfAllItems[j]!=null){
            stroeNum++;
        }
    }
    if(stroeNum>0){
        setButtonBaseClassValid("fieldList"+ID_TAG);
    }else{
        setButtonBaseClassInvalid("fieldList"+ID_TAG);
    }

    close_quantile_derived_dialog();
}

function valueDiffWithSelected(nameValue,expressionValue,selectValue){
    if(var_derivedGrid!=null){
        var gridSelected = var_derivedGrid.selection.selected;
        var rows = var_derivedGrid.store._arrayOfAllItems;
        if(gridSelected!=null && rows != null){
            var selectIndex =-1;
            for(var i=0;i<gridSelected.length;i++){
                if(gridSelected[i]==true){
                    selectIndex = i;
                    break;
                }
            }
            if(selectIndex!=-1){
                if(rows[selectIndex].resultColumnName[0]!=nameValue
                    || rows[selectIndex].sqlExpression[0]!=expressionValue
                    || rows[selectIndex].dataType[0]!=selectValue){
                    return true;
                }
            }
        }
    }
    return false;
}

function create_var_derivedGrid(prop) {

	var model = VDCurrentModel;
	var var_derivedData = dojo.clone(model.derivedFieldsList);
	
	for(var i = 0;i < var_derivedData.length;i++){
		var_derivedData[i].rowNum = derivedGridRowNum++;
	}

	var store = {
		identifier : 'rowNum',
		items : var_derivedData
	};
	var_derivedStore = new dojo.data.ItemFileWriteStore({
		data : store
	});

    var sqlExpressionLabel = alpine.nls.sqlExpression_title_sql;
    if("HadoopVariableOperator"==CurrentOperatorDTO.classname){
        sqlExpressionLabel = alpine.nls.sqlExpression_title_java;
    }
	if(var_derivedGrid!=null){
        var_derivedGrid.destroyRecursive();
        var_derivedGrid = null;
    }
	// create a new var_derived grid:
	if(var_derivedGrid==null){
		// set the layout structure:
		var layout = [ {
			field : 'resultColumnName',
			name : alpine.nls.var_dev_result_column,
			width : '100px'

		}, {
			field : 'dataType',
			name : alpine.nls.var_dev_data_type,
			width : '120px'
		}, {
			field : 'sqlExpression',
			name : sqlExpressionLabel,
			width : 'auto'
		} ];
		var panel = dojo.byId("var_derived_gridpane");
		if (panel.firstChild) {
			panel.removeChild(panel.firstChild);
		}
		gridDomNode =document.createElement('div');
		panel.appendChild(gridDomNode);
		var_derivedGrid = new dojox.grid.DataGrid({
			query : {
				rowNum : '*'
			},
			store : var_derivedStore,
			clientSort : true,
			selectionMode: "single",
			rowSelector : '10px',
			structure : layout,
            onRowDblClick:function(event){
                return false;
            }
		}, gridDomNode);
	 
		 dojo.connect(var_derivedGrid, "onRowClick", click_var_derived);
         dojo.connect(var_derivedGrid,"onHeaderCellClick",function(){
             click_var_derived();
         });
	 
		var_derivedGrid.startup();
	}
    /*else{
		var_derivedGrid.setStore(var_derivedStore);
		var_derivedGrid.render();
	}*/
}

function click_var_derived(event) {
   var items = [];
   if(event && event.rowIndex==undefined){
		var currentRowIndex = var_derivedGrid.focus.rowIndex;
		items[0] = var_derivedGrid.getItem(currentRowIndex);
		var_derivedGrid.selection.select(currentRowIndex);
	}else{
		items = var_derivedGrid.selection.getSelected();
	}

	var val = var_derivedGrid.getItem(var_derivedGrid.focus.rowIndex);

	if(!items||!items[0]) {//no selected or cancel select
		dojo.byId("var_dev_result_column").value = "";
       // dojo.byId("var_dev_result_column").disabled = "";
        myCodeMirror.setValue("");
		//dojo.byId("var_sql_spec").value = "";
		dijit.byId("var_dev_data_type").set('value',"");

		disable_var_derived_buttons();
		return;
	}else{
		if(dojo.indexOf(items,val)<0){
			//ctrl to cacel the select
			val = items[0];
		}
		dojo.byId("var_dev_result_column").value = val.resultColumnName[0];
        //dojo.byId("var_dev_result_column").disabled = "disabled";
		//dojo.byId("var_sql_spec").value = val.sqlExpression[0];
        myCodeMirror.setValue(val.sqlExpression[0]);
		dijit.byId("var_dev_data_type").set('value',val.dataType[0]);

		derivedEditingRowNum = val.rowNum[0];

		enable_var_derived_update();
        enable_var_derived_delete();
	}
}

function clear_var_derived_from() {
	dojo.byId("var_dev_result_column").value = "";
	//dojo.byId("var_sql_spec").value = "";
    myCodeMirror.setValue("");
}

function create_var_derived() {
	if (!var_derivedForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
        return false;
	}

	var val = get_var_derived_data();
	var selectedAvailable = new Array();
	var selectedCheckBox = dijit.byId("colNamesItemGrid4Property");
	if(selectedCheckBox!=null){
		var selectItem = selectedCheckBox.selection.getSelected();
		if(selectItem!=null && selectItem.length!=null){
		   for ( var i = 0; i < selectItem.length; i++) {
			   if(selectItem[i].colName == val.resultColumnName){
					popupComponent.alert(alpine.nls.agg_win_column_exist);
                    return false;
			   }
			
		  }
		}
	}



	var doCreate = function(items, request) {
		if (items.length == 0) {
			val.rowNum = derivedGridRowNum++;
			var_derivedStore.newItem(val);
			var_derivedStore.save();
			var_derivedGrid.startup();
			var_derivedGrid.render();
			resetVarDerivedForm();
		} else {
			popupComponent.alert(alpine.nls.var_dev_column_exist);
		}
	};

	var request = var_derivedStore.fetch({
		query : {
			resultColumnName : val.resultColumnName
		},
		onComplete : doCreate
	});
}

function update_var_derived() {
	if (!var_derivedForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return false;
	}
	var item = get_var_derived_data();
    /* Look through existing vars by name to see if we should delete & recreate */
//	var items = var_derivedGrid.store._arrayOfAllItems;
//	if (null!=items && items.length>0) {
//        for (var i=0; i<items.length; i++) {
//            if(items[i]==null) {continue;}
//            if(items[i].resultColumnName[0]==item.resultColumnName){
//                var_derivedStore.deleteItem(items[i]);
//            }
//        }
//	}
//	var_derivedStore.save();
//	var_derivedStore.newItem(item);
//	var_derivedStore.save();
//
//	var_derivedGrid.startup();
//	var_derivedGrid.render();
	var_derivedStore.fetch({
		query : {
			resultColumnName : item.resultColumnName
		},
		onComplete : function(items){
			if(items.length > 0 && items[0].rowNum[0] != derivedEditingRowNum){
				popupComponent.alert(alpine.nls.agg_win_column_exist);
			}else{
				var_derivedGrid.store.fetchItemByIdentity({
					identity: derivedEditingRowNum,
					onItem: function(editingRow){
						var_derivedGrid.store.setValue(editingRow, "resultColumnName", item.resultColumnName);
						var_derivedGrid.store.setValue(editingRow, "dataType", item.dataType);
						var_derivedGrid.store.setValue(editingRow, "sqlExpression", item.sqlExpression);
						resetVarDerivedForm();
					}
				});
			}
			
		}
	});
}

function delete_var_derived() {
	if (!var_derivedForm.validate()) {
		popupComponent.alert(alpine.nls.ConnectionValid);
		return;
	}
	var d = get_var_derived_data();
	var items = var_derivedGrid.selection.getSelected();
	if (items.length) {
		dojo.forEach(items, function(selectedItem) {
			if (selectedItem !== null) {
				var_derivedStore.deleteItem(selectedItem);
			}
		});
	}
	var_derivedStore.save();
	resetVarDerivedForm();
    //dojo.byId("var_dev_result_column").disabled = "";
}
// String resultColumnName = null; 
// String dataType = null;
// String sqlExpression = null;
function get_var_derived_data() {
	var val = {};
	val.resultColumnName = dojo.byId("var_dev_result_column").value;
    val.sqlExpression = myCodeMirror.getValue();
    //val.sqlExpression = dojo.byId("var_sql_spec").value;
//    myCodeMirror.setValue("");
    val.dataType = dijit.byId("var_dev_data_type").get("value");
	return val;
}
function resetVarDerivedForm(){
	enable_var_derived_create();
	clear_var_derived_from();
    //dojo.byId("var_dev_result_column").disabled = "";
    var items = var_derivedGrid.selection.clear();
    
}


function enable_var_derived_create() {
	dijit.byId("var_derived_create_button").set("disabled", false);
	dijit.byId("var_derived_update_button").set("disabled", true);
	dijit.byId("var_derived_delete_button").set("disabled", true);
}

function enable_var_derived_update() {
	dijit.byId("var_derived_create_button").set("disabled", true);
	dijit.byId("var_derived_update_button").set("disabled", false);
	//dijit.byId("var_derived_delete_button").set("disabled", false);
}

function enable_var_derived_delete() {
    dijit.byId("var_derived_delete_button").set("disabled", false);
}

function disable_var_derived_buttons() {
	dijit.byId("var_derived_create_button").set("disabled", true);
	dijit.byId("var_derived_update_button").set("disabled", true);
	dijit.byId("var_derived_delete_button").set("disabled", true);
}

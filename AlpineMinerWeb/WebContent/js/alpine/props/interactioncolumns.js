
var interactionColumnsTable =null;  
var interactionColumnsStore;
var current_ic_item = null;
var interactionColumnsDialog_ID="interactionColumnsDialog";
var interactionColumns_ID ="interactionColumnsTable" ; 

function initInteractionColumnsTable(interactionItems ,avaliableColumns) { 
	var interactionArray = new Array(); 
	if(interactionItems){
		for ( var i = 0; i < interactionItems.length; i++) {
			interactionArray[i] = {
					firstColumn : interactionItems[i].firstColumn ,
					interactionType : interactionItems[i].interactionType ,
					secondColumn:interactionItems[i].secondColumn
			};
		}
	}
	var dataTable = {
		items : interactionArray
	};
	// our test data store for ts example:
	interactionColumnsStore = new dojo.data.ItemFileWriteStore({
		data : dataTable
	});

	 
	if(interactionColumnsTable==null){
	 	interactionColumnsTable = dijit.byId(interactionColumns_ID);
		dojo.connect(interactionColumnsTable, "onRowClick", select_ic_item); 
	}
	

	//this will make the edit ok
	interactionColumnsTable.setStore(interactionColumnsStore);
	var options = new Array();
	var options2 = new Array();
	if(avaliableColumns){
			for ( var x = 0; x < avaliableColumns.length; x++) {
				var column = avaliableColumns[x] ;
				var option ={ label: column, value: column};
				//MINERWEB601 --avoid use the same option will cause error
				options.push(option);
				options2.push({ label: column, value: column});
//				dijit.byId("edit_ic_column1").addOption(option);
//				dijit.byId("edit_ic_column2").addOption(option);
				
				
			}
	}
	
	
	
	dijit.byId("edit_ic_interactive").set("options",[{ label: "*", value: "*"},
	                                                 { label: ":", value: ":"}]);
	dijit.byId("edit_ic_column2").set("options",options);
	dijit.byId("edit_ic_column1").set("options",options2);
	dijit.byId("edit_ic_column1").startup();
	dijit.byId("edit_ic_column2").startup();
	
	// Call startup, in order to render the grid:
	interactionColumnsTable.render();
	
	if(interactionColumnsStore._arrayOfTopLevelItems
			&&interactionColumnsStore._arrayOfTopLevelItems.length>0){
		interactionColumnsTable.selection.select(0);
		interactionColumnsTable.updateRow(0);
		 
	}else{
		interactionColumnsTable.selection.deselectAll();
		
	}
 
	
	
	select_ic_item();
}

function addInteractiveColumn(){
	current_ic_item=null;
//	var index=1;
//	if(interactionColumnsStore&&interactionColumnsStore._arrayOfTopLevelItems){
//		index=interactionColumnsStore._arrayOfTopLevelItems.length+1;
//	}
//	 
	var interactionType =null ;
	var firstColumn =null ;
	var secondColumn =null;

	
	if(dijit.byId("edit_ic_interactive").get("value")){
		interactionType=dijit.byId("edit_ic_interactive").get("value");
	}		
	if(dijit.byId("edit_ic_column1").get("value")){
		firstColumn=dijit.byId("edit_ic_column1").get("value");
	}
	if(dijit.byId("edit_ic_column2").get("value")){
		secondColumn=dijit.byId("edit_ic_column2").get("value");
	}
	
	var message=validate_ic_items(firstColumn,interactionType,secondColumn);
	if(message){
		popupComponent.alert(message);
		select_ic_item();
		return;
	}
	
	var myNewItem = {
			firstColumn : firstColumn,
			interactionType :interactionType,
			secondColumn : secondColumn

	};
	//interactionColumnsStore.newItem(myNewItem);
	//interactionColumnsTable.setStore(interactionColumnsStore);
    interactionColumnsTable.store.newItem(myNewItem);
    interactionColumnsTable.render();
    interactionColumnsTable.selection.deselectAll();
	var index = interactionColumnsStore._arrayOfTopLevelItems.length-1;
	interactionColumnsTable.selection.select(index);
	interactionColumnsTable.updateRow(index);

	select_ic_item();

	dijit.byId("removeInteractiveColumnBtn").set("disabled",false);
//	dijit.byId("interactin_columns_ok_id").setAttribute("disabled",true);
}
 
function removeInteractiveColumn(){
	// Get all selected items from the Grid:
	var items = interactionColumnsTable.selection.getSelected();
	if (items.length) {
		dojo.forEach(items, function(selectedItem) {
			if (selectedItem != null) {
                interactionColumnsTable.store.deleteItem(selectedItem);
		 
				
			}
		});
		
		//interactionColumnsTable.setStore(interactionColumnsStore);
        interactionColumnsTable.render();
		if(interactionColumnsStore._arrayOfTopLevelItems&&
				interactionColumnsStore._arrayOfTopLevelItems.length>0){
			interactionColumnsTable.selection.select(0);	
			interactionColumnsTable.updateRow(0);
 
		}else{
			interactionColumnsTable.selection.deselectAll();
		}
	 
		select_ic_item();	
	 
		
	} // end if
}
dojo.addOnLoad(function() {

	dijit.byId("removeInteractiveColumnBtn").onClick = function() {
		removeInteractiveColumn();
	};

	dijit.byId("addInteractiveColumnBtn").onClick = function() {
		addInteractiveColumn();
	};
	
	dijit.byId("updateInteractiveColumnBtn").onClick = function() {
		updateInteractiveColumn();
	};
	

});


function validate_ic_items( firstColumn,interactionType,secondColumn) {
//	if(!firstColumn||!interactionType||!secondColumn){
//		return "Please complete the input!";
//	}
	
	if(firstColumn==secondColumn){
		return(alpine.nls.same_interaction_column);
		
	}
//	if(!firstColumn||firstColumn.replace(/\ /g,"").length==0
//			||!secondColumn||secondColumn.replace(/\ /g,"").length==0){
//		return "Interaction column can not be empty!";
//	}
	//make sure all columns are set
	var items=interactionColumnsStore._arrayOfTopLevelItems;
 
	var same_column = false;
	for(var i =0 ;i<items.length;i++){
		var column1=	items[i].firstColumn[0];
		var collumn2=	items[i].secondColumn[0];
  	 	if(items[i]==current_ic_item){
  	 		continue;
  	 	}
			 
				if(firstColumn==column1&&secondColumn==collumn2){
					same_column=true;
				}
				if(secondColumn==column1&&firstColumn==collumn2){
					same_column=true;
				}
 			
			if(same_column==true){
				return (alpine.nls.interaction_column_already_exists);
			}
 
	}
	return null;
//	if(error){
//		dijit.byId("interactin_columns_ok_id").setAttribute("disabled",true);
//	}
//	else{
//		dijit.byId("interactin_columns_ok_id").setAttribute("disabled",false);
//	}
 
}



function showInterActionColumnsDialog() {

    var dependColumn = dijit.byId("dependentColumn"+ID_TAG);
    if(null!=dependColumn && (dependColumn.get("value")==null || dependColumn.get("value")=="")){
        popupComponent.alert(alpine.nls.select_dep_column_first);
        return false;
    }

	dijit.byId(interactionColumnsDialog_ID).show();
	dijit.byId(interactionColumnsDialog_ID).resize(400, 301);

    if(CurrentOperatorDTO.classname == "HadoopLinearRegressionOperator" || CurrentOperatorDTO.classname == "HadoopLogisticRegressionOperator"){
        var inputTableInfos=CurrentOperatorDTO.inputFileInfos;


        var dependentColumn=  dojo.byId("dependentColumn" + ID_TAG).value;
        var propertyList = CurrentOperatorDTO.propertyList;
//	for(var i =0 ; i<propertyList.length; i++){
//		var prop = propertyList[i];
//		if(prop.name=="dependentColumn"){
//			dependentColumn = prop.value;
//		}
//	}
//
        var avaliableColumns=new Array();
        var inputTable=inputTableInfos[0];

        var index = 0;
        for(var i=0;i<inputTable.columnInfo.columnNameList.length;i++){
            var columnName=inputTable.columnInfo.columnNameList[i];
            if(columnName!=dependentColumn && alpine.props.HadoopDataTypeUtil.isNumberType(inputTable.columnInfo.columnTypeList[i])==true){
                avaliableColumns[index]=columnName;
                index=index+1;
            }

        }
        var interactionItems=null;
        if(current_interaction_columns.interActionModel){
            interactionItems=  current_interaction_columns.interActionModel.interActionItems;
        }
        initInteractionColumnsTable(interactionItems,avaliableColumns);
    }else{
        var inputTableInfos=CurrentOperatorDTO.inputTableInfos;


        var dependentColumn=  dojo.byId("dependentColumn" + ID_TAG).value;
        var propertyList = CurrentOperatorDTO.propertyList;
//	for(var i =0 ; i<propertyList.length; i++){
//		var prop = propertyList[i];
//		if(prop.name=="dependentColumn"){
//			dependentColumn = prop.value;
//		}
//	}
//
        var avaliableColumns=new Array();
        var inputTable=inputTableInfos[0];

        var index = 0;
        for(var i=0;i<inputTable.fieldColumns.length;i++){
            var columnName=inputTable.fieldColumns[i][0];
            if(columnName!=dependentColumn){
                avaliableColumns[index]=columnName;
                index=index+1;
            }

        }
        var interactionItems=null;
        if(current_interaction_columns.interActionModel){
            interactionItems=  current_interaction_columns.interActionModel.interActionItems;
        }
        initInteractionColumnsTable(interactionItems,avaliableColumns);
    }
}

function update_interactive_columns_data() {
  	
	    //modify later
	    var interActionItems=new Array();

		if(interactionColumnsStore&&interactionColumnsStore._arrayOfTopLevelItems){
			var items=interactionColumnsStore._arrayOfTopLevelItems;
			for(var i =0 ;i<items.length;i++){ 
				interActionItems[i]={id:i,firstColumn:items[i].firstColumn[0],
						interactionType:items[i].interactionType[0],
						secondColumn:items[i].secondColumn[0]
						};
			
			}
		}
		//update the operator parameter...
		var model={};
		model.interActionItems=interActionItems; 
		current_interaction_columns.interActionModel= model; 
	
	//current_tableJoin.objectValue = newTableJoinModel;

	close_ic_dialog();
}


//
function _setInteractionBtnValid(){
    setButtonBaseClassValid(getSourceButtonId(current_interaction_columns));
    dijit.byId(getSourceButtonId(current_interaction_columns)).focus();
}
function _setInteractionBtnInvalid(){
    dijit.byId(getSourceButtonId(current_interaction_columns)).set('baseClass', "workflowButtonInvalid");
    dijit.byId(getSourceButtonId(current_interaction_columns)).focus();
}

function _setColumnBtnValid4Regression(){
    if((CurrentOperatorDTO.classname == "LinearRegressionOperator"
        || CurrentOperatorDTO.classname == "LogisticRegressionOperator"
        || CurrentOperatorDTO.classname == "HadoopLinearRegressionOperator"
       || CurrentOperatorDTO.classname == "HadoopLogisticRegressionOperator"
        )){
        var columnNames = dijit.byId('columnNames'+ID_TAG);
        if(null!=columnNames){
            setButtonBaseClassValid('columnNames'+ID_TAG);
            dijit.byId('columnNames'+ID_TAG).focus();
        }
    }
}

function _setColumnBtnInvalid4Regression(){
    if((CurrentOperatorDTO.classname == "LinearRegressionOperator"
        || CurrentOperatorDTO.classname == "LogisticRegressionOperator"
        || CurrentOperatorDTO.classname == "HadoopLinearRegressionOperator"
        || CurrentOperatorDTO.classname == "HadoopLogisticRegressionOperator"
        )){
        var columnNames = dijit.byId('columnNames'+ID_TAG);
        if(null!=columnNames){
            dijit.byId('columnNames'+ID_TAG).set('baseClass', "workflowButtonInvalid");
            dijit.byId('columnNames'+ID_TAG).focus();
        }
    }
}
//
function updateInteractiveColumn(){
	if(interactionColumnsStore._arrayOfTopLevelItems&&
			interactionColumnsStore._arrayOfTopLevelItems.length>0
			&&current_ic_item){
		//validate 
		//1 null
		//2 same left and right
		//3 exists...
		var 	interactionType=dijit.byId("edit_ic_interactive").get("value");
		var	firstColumn=dijit.byId("edit_ic_column1").get("value");
		var	secondColumn=dijit.byId("edit_ic_column2").get("value");
		
		var message=validate_ic_items(firstColumn,interactionType,secondColumn);
		if(message){
			popupComponent.alert (message);
			select_ic_item();
			return ;
		}
		current_ic_item.firstColumn[0] = firstColumn;
		current_ic_item.interactionType[0] =interactionType;
		current_ic_item.secondColumn[0] = secondColumn;
		interactionColumnsTable.setStore(interactionColumnsStore);
 		interactionColumnsTable.selection.deselectAll();
//
 		var index = interactionColumnsStore._arrayOfTopLevelItems.indexOf(current_ic_item);
 		
 		interactionColumnsTable.selection.select(index);	
		interactionColumnsTable.updateRow(index);
 
 		select_ic_item();
	}else{
		popupComponent.alert(alpine.nls.ic_not_selected);
	}
	 
	
}



function select_ic_item(event ){
	//event is no use now
	var items = [];
	 
	if(event && event.rowIndex==undefined){
		var currentRowIndex = interactionColumnsTable.focus.rowIndex;		
		items[0] = interactionColumnsTable.getItem(currentRowIndex);
		interactionColumnsTable.selection.select(currentRowIndex);
	}else{
		items = interactionColumnsTable.selection.getSelected();
	}
	if(items&&items[0]){
 
		dijit.byId("updateInteractiveColumnBtn").set("disabled",false);
		dijit.byId("removeInteractiveColumnBtn").set("disabled",false);
	 
		if(event){//make sure last one
			current_ic_item =  interactionColumnsTable.getItem(event.rowIndex);
			if(dojo.indexOf(items,current_ic_item)<0){
				//ctrl to cacel the select
				current_ic_item = items[0];
			}
		}else 	if(items&&items[0]){
			current_ic_item = items[0];
		} 
		
		dijit.byId("edit_ic_column1").set("value", current_ic_item.firstColumn[0]);
		dijit.byId("edit_ic_column2").set("value", current_ic_item.secondColumn[0]);
		 
		dijit.byId("edit_ic_interactive").set("value", current_ic_item.interactionType[0]);
		 
	}else{
		dijit.byId("updateInteractiveColumnBtn").set("disabled",true);
		dijit.byId("removeInteractiveColumnBtn").set("disabled",true);
		current_ic_item= null;
		dijit.byId("edit_ic_column1").set("value", "");
		dijit.byId("edit_ic_column2").set("value", "");
		 
		dijit.byId("edit_ic_interactive").set("value", "");
	
	}
 	

}

function close_ic_dialog(){
    if(interactionColumnsStore!=null && interactionColumnsStore._arrayOfTopLevelItems!=null){
       var interactionNum = 0;
       var selectColumnNum = 0;
       if(null!=current_interaction_columns
           && null!=current_interaction_columns.interActionModel
           && null!=current_interaction_columns.interActionModel.interActionItems){
           interactionNum = current_interaction_columns.interActionModel.interActionItems.length;
       }
        selectColumnNum = _getSelectColumnNum();
        var isStepWise_false = dijit.byId("isStepWise"+ID_TAG+"false")
        //var isStepWise_true = dijit.byId("isStepWise"+ID_TAG+"true")
        if(null==isStepWise_false){
            //for hadoop operator
            if(interactionNum>0 || selectColumnNum>0){
                _setInteractionBtnValid();
                _setColumnBtnValid4Regression();
            }else{
                _setInteractionBtnInvalid();
                _setColumnBtnInvalid4Regression();
            }
        }else{
            if(null!=isStepWise_false && isStepWise_false.get("checked")==true){
                if(interactionNum>0 || selectColumnNum>0){
                    _setInteractionBtnValid();
                    _setColumnBtnValid4Regression();
                }else{
                    _setInteractionBtnInvalid();
                    _setColumnBtnInvalid4Regression();
                }
            }else{
                if(interactionNum==0){
                    _setInteractionBtnInvalid();
                }else{
                    _setInteractionBtnValid();
                }

                if(selectColumnNum==0){
                    _setColumnBtnInvalid4Regression();
                }else{
                    _setColumnBtnValid4Regression();
                    _setInteractionBtnValid();
                }
            }
        }


    }
	current_ic_item = null;
	dijit.byId(interactionColumnsDialog_ID).hide();
}

function _getSelectColumnNum(){
    var columnNum = 0;
   if(null!=CurrentOperatorDTO && CurrentOperatorDTO.propertyList!=null){
       var pList = CurrentOperatorDTO.propertyList;
       for(var i=0;i<pList.length;i++){
           if(pList[i].name=="columnNames"){
               if(pList[i].selected!=null){
                   columnNum = pList[i].selected.length;
               }
               break;
           }
       }

   }


    return columnNum;
}
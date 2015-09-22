var current_hiddenlayer_item = null;
var hiddenLayerTable = null;; 
 
var hiddenLayerStore;
 
var hiddenLayerDialog_ID="hiddenLayerDialog";
var hiddenLayerTable_ID ="hiddenLayerTable" ;

function initHinddenLayerTable(hiddenLayers ) {
	var hiddenLayerArray = new Array();
	if(hiddenLayers){
		for ( var i = 0; i < hiddenLayers.length; i++) {
			hiddenLayerArray[i] = {
					layerName : hiddenLayers[i].layerName ,
					layerSize:hiddenLayers[i].layerSize
			};
		}
	}
	var dataTable = {
		items : hiddenLayerArray
	};
	// our test data store for this example:
	hiddenLayerStore = new dojo.data.ItemFileWriteStore({
		data : dataTable
	});
 
	if(hiddenLayerTable==null){
		hiddenLayerTable = dijit.byId(hiddenLayerTable_ID);
		dojo.connect(hiddenLayerTable, "onRowClick", select_hiddenlayer_item); 
	}
	
	//this will make the edit ok
	hiddenLayerTable.setStore(hiddenLayerStore);
	
	// Call startup, in order to render the grid:
	hiddenLayerTable.render();

}
 
dojo.addOnLoad(function() {

	dijit.byId("removeHiddenLayerBtn").onClick = function() {
		removeHiddenLayer();
	};

	dijit.byId("addHidenLayerBtn").onClick = function() {
		addHiddenLayer();
	};
	
	
	var grid= dijit.byId(hiddenLayerTable_ID);
 
	dojo.connect(grid, "onApplyCellEdit", onApplyHLCellEditHandler);  

});

function removeHiddenLayer(){
	// Get all selected items from the Grid:
	var items = hiddenLayerTable.selection.getSelected();
	if (items.length) {
		dojo.forEach(items, function(selectedItem) {
			if (selectedItem != null) {
                hiddenLayerTable.store.deleteItem(selectedItem);
				
				if(hiddenLayerTable.store&&hiddenLayerTable.store._arrayOfTopLevelItems){
					var layers=hiddenLayerTable.store._arrayOfTopLevelItems;
					for(var i =0 ;i<layers.length;i++){
                        hiddenLayerTable.store.setValue(layers[i],"layerName","hiddenlayer"+(i+1));
					}
				}
				 
				
			}
		});
		//hiddenLayerTable.setStore(hiddenLayerStore);
        hiddenLayerTable.render();
		hiddenLayerTable.selection.deselectAll();
		if(hiddenLayerTable.store._arrayOfTopLevelItems&&
            hiddenLayerTable.store._arrayOfTopLevelItems.length>0){
			 var index = 0;
			 hiddenLayerTable.selection.select(index);	
			hiddenLayerTable.updateRow(index);
 
		} 
		
		select_hiddenlayer_item();
		
//		validate_all_hidden_layer();//move validate to click submit
		
		
	} // end if
}

function validate_all_hidden_layer(){
	var validate = true;
	var validateFailedNames = new Array();
	if (hiddenLayerStore && hiddenLayerStore._arrayOfTopLevelItems) {
		var layers = hiddenLayerStore._arrayOfTopLevelItems;
		for ( var i = 0; i < layers.length; i++) {
			var inValue = toolkit.getValue(layers[i].layerSize);
			inValue = alpine.flow.WorkFlowVariableReplacer.replaceVariable(inValue);
			if(isNaN(inValue) || !(parseInt(inValue) >= 1 && parseInt(inValue) <= 999)){
				validate = false;
				validateFailedNames.push(toolkit.getValue(layers[i].layerName));
			}
		}
	}
 
	   if(!validate){
		   var failedNames = validateFailedNames.join();
		   var msg = dojo.string.substitute(alpine.nls.hidden_layer_msg_validateFailed, {
			   hiddenLayer: failedNames
			});
		   popupComponent.alert(msg);
	   }
	return validate;
}

function addHiddenLayer(){
	var index=1;
	if(hiddenLayerTable.store&&hiddenLayerTable.store._arrayOfTopLevelItems){
		index=hiddenLayerTable.store._arrayOfTopLevelItems.length+1;
	}
	 
	var myNewItem = {
			layerName : "hiddenlayer"+index,
			layerSize : "1"
		 
	};
    hiddenLayerTable.store.newItem(myNewItem);
	hiddenLayerTable.render();
	
	hiddenLayerTable.selection.deselectAll();
	 var index = hiddenLayerTable.store._arrayOfTopLevelItems.length-1;
	 hiddenLayerTable.selection.select(index);	
	hiddenLayerTable.updateRow(index);
	select_hiddenlayer_item();
	
	
}

function onApplyHLCellEditHandler( inValue, inRowIndex, inFieldIndex) {
//	inValue = alpine.flow.WorkFlowVariableReplacer.replaceVariable(inValue);
//   if(!isNaN(inValue) && inValue >= 1 && inValue <= 999){
//	   //nothing to do  
//		dijit.byId("hidden_layer_ok_id").setAttribute("disabled",false);
//   }else{
//	 
//		dijit.byId("hidden_layer_ok_id").setAttribute("disabled",true);
//		popupComponent.alert(alpine.nls.hidden_layer_size_error );
//   }
//	validate_all_hidden_layer(); move validate to click submit
}


function showHiddenLayerDialog() {
	var hiddenLyaers=null;
	if(current_hiddenLayer.hiddenLayersModel){
		hiddenLyaers=current_hiddenLayer.hiddenLayersModel.hiddenLayers;
	}
	
	initHinddenLayerTable(hiddenLyaers);
	dijit.byId(hiddenLayerDialog_ID).titleBar.style.display = "none";
	dijit.byId(hiddenLayerDialog_ID).show();
	dijit.byId(hiddenLayerDialog_ID).resize(300, 301);
    //button size
    dojo.query("#addHidenLayerBtn>span").style("width","42px");
    dojo.query("#removeHiddenLayerBtn>span").style("width","42px");
}

function update_hidden_layer_data() {
	//validate the hidden layer size
	if(!validate_all_hidden_layer()){
		return;
	}
	 
	
	//modify later
	var hiddenLayers=new Array();
	 
		if(hiddenLayerStore&&hiddenLayerStore._arrayOfTopLevelItems){
			var layers=hiddenLayerStore._arrayOfTopLevelItems;
			for(var i =0 ;i<layers.length;i++){
				hiddenLayers[i]={layerName:layers[i].layerName,layerSize:layers[i].layerSize};
			
			}
		}
		//update the operator parameter...
		var hiddenLayersModel={};
		hiddenLayersModel.hiddenLayers=hiddenLayers;
		current_hiddenLayer.hiddenLayersModel= hiddenLayersModel;
    setButtonBaseClassValid(getSourceButtonId(current_hiddenLayer)) ;

    close_hidden_layer_dialog();
	
}

function close_hidden_layer_dialog(){
	  hiddenLayerStore  = null;

	current_hiddenlayer_item = null;
 
	dijit.byId(hiddenLayerDialog_ID).hide();
}

function select_hiddenlayer_item(event){
	
	var items = [];
	var currentRowIndex = -1;
	if(event && event.rowIndex==undefined){
		currentRowIndex = hiddenLayerTable.focus.rowIndex;		
		items[0] = hiddenLayerTable.getItem(currentRowIndex);
		hiddenLayerTable.selection.select(currentRowIndex);
	}else{
		items = hiddenLayerTable.selection.getSelected();
	}
	
	
	//event is no use now
	if(items&&items[0]){
		dijit.byId("removeHiddenLayerBtn").set("disabled",false);
		 
		if(event){//make sure last one
			current_hiddenlayer_item =  hiddenLayerTable.getItem(currentRowIndex);
			if(dojo.indexOf(items,current_hiddenlayer_item)<0){
				//ctrl to cacel the select
				current_hiddenlayer_item = items[0];
			}
		}else 	if(items&&items[0]){
			current_hiddenlayer_item = items[0];
		} 
		current_hiddenlayer_item= items[items.length-1] ;
	
	}else{
		dijit.byId("removeHiddenLayerBtn").set("disabled",true);
		current_hiddenlayer_item= null;
	
	}

}


 

var udf_store = null;
var udf_table = null;
var UDF_DIALOG_ID = "udf_dialog" ;
var MAIN_ID = "FlowDisplayPanelPersonal";

function init_udf_table(parentPanelId){
	var requestUrl = udfBaseURL + "?method=getUDFModels";
	//progressBar.showLoadingBar();
	ds.get(requestUrl, _initUDFListTable, null, false, parentPanelId);
}


function show_udf_dialog() {
	
	init_udf_table(MAIN_ID);
	upload_udf_error("");
	dijit.byId(UDF_DIALOG_ID).titleBar.style.display = "none";
	dijit.byId(UDF_DIALOG_ID).show();
	dijit.byId(UDF_DIALOG_ID).resize(400, 321);

    dojo.byId("fFile_udf").value="";
 

}
 

// modelList is from REST service...
function _initUDFListTable(udfList) {
	//progressBar.closeLoadingBar();
	
	if (!udfList || udfList.length == 0) {
		return ;
	}
	for ( var i = 0; i < udfList.length; i++) {
		var udf = udfList[i];
		var longName = udf.operatorName;
		udf.operatorName_long = longName;
		udf.operatorName = longName.substring(0,longName.lastIndexOf("_"));

	}
	var dataTable = {
		items : udfList
	};
	// our test data store for this example:
	udf_store = new dojo.data.ItemFileWriteStore( {
		data : dataTable
	});

	udf_table = dijit.byId("udf_table");
	// this will make the edit ok
	udf_table.setStore(udf_store);

	// Call startup, in order to render the grid:
	udf_table.render();
	
	if(udf_store._arrayOfTopLevelItems.length >0){
		udf_table.selection.setSelected([udf_store._arrayOfTopLevelItems[0]]);	
	}else{
		udf_table.selection.setSelected(new Array());
	}
	//make sure the item is disabled
    dojo.byId("fFile_udf").value="";
	select_udf();
	

}

function perform_delete_udf() {
	// item is modelInfo
	var items = udf_table.selection.getSelected();
	if (!items || items.length == 0) {
		popupComponent.alert(alpine.nls.select_rsult_first);
	} else {
		for ( var x = 0; x < items.length; x++) {
			var udf = items[x];

            udf_table.store.deleteItem(udf);

			var requestUrl = udfBaseURL + "?method=deleteUDFModel&operatorName="+udf.operatorName_long;

			ds.post(requestUrl, udf, delete_udf_call_back, null, false, UDF_DIALOG_ID);
		}
 
		//udf_table.setStore(udf_store);
		if (udf_store._arrayOfTopLevelItems.length > 0) {
			udf_table.selection
					.setSelected( [ udf_store._arrayOfTopLevelItems[0] ]);
		} else {
			udf_table.selection.setSelected(new Array());
		}
		// make sure the item is disabled

		select_udf();

	}
}
//not used now
function delete_udf_call_back(){

    if(dijit.byId("alpine_layout_navigation_operator_pane").selected){
    	alpine.operatorexplorer.OperatorExplorerUIHelper.refreshPane();
    }
}
 
    
function is_udf_file_name_OK(udf_file_name) {
	var hasError = false;
	if (!udf_file_name || udf_file_name.length == 0) {
		upload_udf_error(alpine.nls.MSG_Please_select_file);
		hasError = true;
	} else {// special case for IE
		if (dojo.isIE && udf_file_name.lastIndexOf("\\") > -1) {
			udf_file_name = udf_file_name.substring(udf_file_name
					.lastIndexOf("\\") + 1);
		}
		// .afm lengt =4
		if (udf_file_name.length < 5
				|| udf_file_name.substring(udf_file_name.length - 4) != ".xml") {

			upload_udf_error(alpine.nls.MSG_Please_select_udf_file);
			hasError = true;
		}
	}
	return hasError;
}
 

function upload_udf_error(errorMsg) {
//	if(dojo.isIE){
		if(errorMsg&&errorMsg!=""&&errorMsg.length>0){
			popupComponent.alert(errorMsg) ;
		}
//	}else{
//		dojo.byId("upload_udf_error").color = "red";
//		dojo.byId("upload_udf_error").innerHTML = errorMsg;
//	}
}

function do_udf_upload(x) {

	// clear error
	upload_udf_error("");
  
	var hasError = false;
 
	var uploadFileName = dojo.byId("fFile_udf").value;
	hasError = is_udf_file_name_OK(uploadFileName);

	
	if (hasError == false) {
        var url = udfBaseURL+ "?method=uploadUDFModels";
        ds.upload(url,"frmIO_udf", function(data)
        {
            if(data.error_code){
                if (data.error_code == -1) {
                   popupComponent.alert(alpine.nls.no_login, function(){
                        window.top.location.pathname = loginURL;
                    });
                }
                else {
                    upload_udf_error(alpine.nls.MSG_Upload_Error
                        + data.message);
                }
            }
            else {
                init_udf_table(UDF_DIALOG_ID);
                if(dijit.byId("alpine_layout_navigation_operator_pane").selected){
                	alpine.operatorexplorer.OperatorExplorerUIHelper.refreshPane();
                }
            }
        }, function (data)
        {
//            popupComponent.alert(data);
        }, UDF_DIALOG_ID);

//		progressBar.showLoadingBar();
//		var url = udfBaseURL+ "?method=uploadUDFModels";
//		var td = dojo.io.iframe.send( {
//			url : encodeURI(url),
//			form : "frmIO_udf",
//			method : "post",
//			content : {
//				fnx : x
//			},
//			timeoutSeconds : 60,
//			preventCache : true,
//			handleAs : "html",
//			handle : function(res, ioArgs) {
//
//
//
//				if (res && res.body && res.body.innerHTML) {
//
//					var data = res.body.innerHTML.evalJSON();
//
//
//				}
//
//			},
//			error : function(res, ioArgs) {
//				progressBar.closeLoadingBar();
//				popupComponent.alert(res);
//			}
//		});
	}
}

function select_udf() {
	var items = udf_table.selection.getSelected();
	if (items && items[0]) {// &&items.length==1
		dijit.byId("btn_udf_delete").set("disabled", false);
		dijit.byId("menu_udf_delete").set("disabled", false);
	}

	else {
		dijit.byId("btn_udf_delete").set("disabled", true);
		dijit.byId("menu_udf_delete").set("disabled", true);

	}

}
 
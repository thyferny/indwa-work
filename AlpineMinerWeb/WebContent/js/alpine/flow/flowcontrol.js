/**
 * COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * 
 * flowcontrol.js
 * 
 * Author sam_zang
 * 
 * Version 2.0
 * 
 * Date Aug 19, 2011
 */

// flow control functions.

//var is_running = false;
//var uuid = null;
//var hasCopied = false;

	function get_flow_tree_public() {
		load_flow_tree("Public");
	}

	function get_flow_tree_group() {
		load_flow_tree("Group");
	}
	
	function get_flow_tree_personal() {
		load_flow_tree("Personal");
	}

	function flow_tree_public_cb(obj) {
		flow_tree_callback(obj, "Public");
	}

	function flow_tree_group_cb(obj) {
		flow_tree_callback(obj, "Group");
	}
	
	function flow_tree_personal_cb(obj) {
		flow_tree_callback(obj, "Personal");
	}
	
	
	function load_flow_tree(type) {
		
		var url = flowBaseURL + "?method=getFlows"
		+ "&user=" + login
		+ "&type=" + type;

		var callback = null;
		switch (type) {
		case "Public": callback = flow_tree_public_cb;
			break;
		//now public will go here... this is for add	
		case "Group": callback = flow_tree_group_cb;
			break;
		case "Personal": callback = flow_tree_personal_cb;
		break;		
		}
		
		ds.get(url, callback);
	}



	// run flow
//	function run_flow() {
//		var isAllValid = alpine.flow.OperatorManagementUIHelper.validateOperators();
//		if (isAllValid == false) {
//			popupComponent.alert(alpine.nls.invalid_flow);
//			return;
//		}
//		disable_run_flow();
//		if (!alpine.flow.WorkFlowManager.isEditing()) {
//			return;
//		}
//		uuid = Math.random();
//		var url = flowBaseURL + "?method=runFlow" + "&uuid=" + uuid + "&user=" + login;
//
//		var callbackFn;
//		if(dojo.isSafari){
//			dojo.publish('/opener/callOpen');
//			
//		}else{
//			callbackFn = run_flow_callback;
//		}
//		
//		ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(), callbackFn, null);
// 		          
//	}
//	dojo.subscribe('/opener/callOpen',function() {
////		setImgs(operatorList[0].name);
//		var url = baseURL + "/alpine/result/result.jsp?uuid=" + uuid+"&flowName="+alpine.flow.WorkFlowManager.getEditingFlow().id;
//		window.open(encodeURI(url),'_blank', get_open_window_options());
//	});

//	function run_flow_callback(obj){
//		
//		if (obj.error_code&&obj.error_code!=0) {
//			if (obj.error_code == -1) {
//				popupComponent.alert(alpine.nls.no_login, function(){
//					window.top.location.pathname = loginURL;	
//				});
//				return;
//			}
//			else {
//				var msg = alpine.nls.flow_not_found;
//				if (obj.message) {
//					msg = obj.message;
//				}
//				popupComponent.alert(msg);
////				clear_flow_display("Personal");
//				alpine.flow.WorkFlowUIHelper.release();
//				get_flow_tree_personal();
//				return;
//			}			
//		}
//		
////		setImgs(operatorList[0].name);	
//		var url = baseURL + "/alpine/result/result.jsp?uuid=" + uuid+"&flowName="+alpine.flow.WorkFlowManager.getEditingFlow().id;
//		window.open(encodeURI(url),'_blank', get_open_window_options());
//	}
	
 

	// run flow
//	function step_run_flow() { 
//		
//		 
//		var current_op= alpine.flow.OperatorManagementUIHelper.getSelectedOperator();// getOperatorByUid(CurrentOperator_uuid);
//		step_run_to_operator(current_op);
//	
//		
//
//
//	}

    function open_operator_properties() {
        var current_op = alpine.flow.OperatorManagementUIHelper.getSelectedOperator();//getOperatorByUid(CurrentOperator_uuid);
        open_property_dialog(current_op);

    }


	// stop flow
//	function stop_flow() {
//	
//		if (uuid == null) {
//			return;
//		}
//		run_flow_finished(uuid) ;
//		var url = flowBaseURL + "?method=stopFlow" + "&uuid=" + uuid + "&user=" + login;
//		ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(), stop_flow_callback);
//		uuid = null;
//	}
//
//	function stop_flow_callback(obj){
//		if (obj && obj.error_code) {
//			handle_error_result(obj);
//			return;
//		 			
//		}
//	}

	// copy flow
	function copy_flow() {
		if (!alpine.flow.WorkFlowManager.isEditing()) {
			return;
		}

		if (alpine.flow.WorkFlowManager.getEditingFlow().type == "Personal") {
			popupComponent.alert(alpine.nls.already_in_personal);
			return;
		}
		
		var url = flowBaseURL + "?method=copyFlow" + "&user=" + login;
		error_msg = alpine.nls.flow_exist_error;
		ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(), copy_flow_callback, error_callback);
	}

	function copy_flow_callback(obj) {
		if(obj&obj.error_code){
			handle_error_result(obj);
			return ;
		}
		flow_tree_personal_cb(obj);
	}

	// export flow
	function export_flow() {
		if (!alpine.flow.WorkFlowManager.isEditing()) {
			return;
		}
		
		var url = baseURL+"/main/flow/im_export_flow.do?method=exportFlow" + "&user=" + login;
		ds.post(url, alpine.flow.WorkFlowManager.getEditingFlow(), export_flow_callback, null);
	}

	function open_export_flow_dlg(flowInfos){
        var url = flowBaseURL + "?method=exportFlow" + "&user=" + login;
		if(flowInfos==null){
            if (!alpine.flow.WorkFlowManager.isEditing()) {
                return;
            }
            ds.post(url, [alpine.flow.WorkFlowManager.getEditingFlow()], export_flow_callback, null);
        }else{
            ds.post(url, flowInfos, export_flow_callback, null);
        }




	}	


	function open_export_exe_flow_dlg(selectFlows){
		if(selectFlows==null){
            if (!alpine.flow.WorkFlowManager.isEditing()) {
                return;
            }
        }
        if(selectFlows!=null){
          dijit.byId("export_exec_flow_btn_ok").onClick=function(){
              do_export_exec_flow(selectFlows);
          };
        }else{
            dijit.byId("export_exec_flow_btn_ok").onClick=function(){
                do_export_exec_flow();
            };
        }
        var dlg = dijit.byId("export_exec_flow_dlg");
        dlg.titleBar.style.display = "none";
        dlg.show();

	}

    function hidden_export_exec_flow(){
        var dlg = dijit.byId("export_exec_flow_dlg");
        if(null!=dlg){
            dlg.hide();
        }
    }

    function do_export_exec_flow(selectFlows){
        var adapter = "";
       if(dijit.byId("export_exec_flow_adapter_linux").get("checked")==true){
           adapter = dijit.byId("export_exec_flow_adapter_linux").get("value");
       }else if(dijit.byId("export_exec_flow_adapter_window").get("checked")==true){
           adapter = dijit.byId("export_exec_flow_adapter_window").get("value");
       }else{
           adapter = dijit.byId("export_exec_flow_adapter_mac").get("value");
       }
        //
        //validate flow info same name

        var url = flowBaseURL + "?method=exportExecFlow" + "&user=" + login+"&adapter="+adapter;
        if(selectFlows==null){
            ds.post(url, [alpine.flow.WorkFlowManager.getEditingFlow()], export_flow_callback, null,false,"export_exec_flow_dlg");
        }else{
            ds.post(url,selectFlows, export_flow_callback, null,false,"export_exec_flow_dlg");
        }

    }

   function isSameNameInFlows(selectFlows){
       var flowNames = [];
      if(selectFlows!=null && selectFlows.length>0){
          for(var i=0;i<selectFlows.length;i++){
              flowNames.push(selectFlows[i].id);
          }
          flowNames.sort();
          for(i=0;i<flowNames.length-1;i++){
              if(flowNames[i]==flowNames[i+1]){
                  return true;
              }
          }
      }
       return false
   }

  function export_flow_callback(obj){
        dijit.byId("export_exec_flow_dlg").hide();
		if(obj&obj.error_code){
			handle_error_result(obj);
			return ;
		}
		var download_url = baseURL + "/temp_flow/"+login+"/" + obj;
		var servlet_url = baseURL+"/CommonFileDownLoaderServlet?downloadFileName="+obj+"&tempType=temp_flow&filePath=/"+login+"/";
		window.location.href = servlet_url;
		return false;
//		var str = alpine.nls.save_as
//			+ "<a href="
//			+ download_url
//			+ "><u><i><font color='blue'>" + obj + "</font></i></u></a>";
//
//		dojo.html.set(dojo.byId("export_flow_label"), str);
//		dijit.byId('export_flow_dlg').show();
	}

	function clear_export_label() {
		dojo.html.set(dojo.byId("export_flow_label"), "");
	}
	
	function error_callback(text, args){
		progressBar.closeLoadingBar();
//		if (error_msg) {
//			popupComponent.alert(error_msg);
//			error_msg = null;
//		}else{
//			popupComponent.alert(text);  
//		}
	}

	// open flow
//	var CurrentFlow = null;
	var flowDisplayPanel = null;
//	var operatorList = null;
//	var operatorLinkList = null;
	
//	function disable_all() {
//		dijit.byId("run_flow").set("disabled", true);
//		dijit.byId("stop_flow").set("disabled", true);
//		dijit.byId("copy_flow").set("disabled", true);
//		dijit.byId("step_run_flow").set("disabled", true);
//        dijit.byId("export_flow_dropdown").set("disabled", true);
//		dijit.byId("share_flow_dropdown").set("disabled", true);
//		dijit.byId("publish_flow_dropdown").set("disabled", true);
//		dijit.byId("delete_flow_dropdown").set("disabled", true);
//        dijit.byId("edit_operator").set("disabled", true);
//        disable_open_properties();
//	}
	
//	function run_flow_finished(runFlowUUID) {
//		
////		if (runFlowUUID = uuid) {// wrong expression... never work.
//
////			for(var i = 0;i < operatorList.length;i++){
////				var operatorInfo = operatorList[i];
////				alpine.flow.OperatorManagementUIHelper.resetOperator(operatorInfo.uid, true);
////			}
//			alpine.flow.OperatorManagementManager.forEachOperatorInfo(function(operatorInfo){
//				alpine.flow.OperatorManagementUIHelper.resetOperator(operatorInfo.uid, true);
//			});
////			if (ShapeList) {
////				for ( var i = 0; i < ShapeList.length; i++) {
////					var current_op = ShapeList[i];
////					// toggle image
////					if (current_op != null) {
////						current_op.icon_current = current_op.icon;
////						
////						alpine.flow.WorkFlowPainter.fineOperatorUnit("FlowDisplayPanelPersonal", current_op.uuid, function(operatorUnit){
////				        	operatorUnit.icon.setShape({
////				        		src: current_op.icon_current
////				        	});
////				    	});
//////						current_op.img.setShape( {
//////							src : current_op.icon_current
//////						});
////	
////					}
////				}
////			}
//			enable_run_flow();
////		}
//	 	
//	}
	
//	function enable_run_flow() {
//		is_running = false;
//		dijit.byId("run_flow").set("disabled", false);
//		dijit.byId("stop_flow").set("disabled", true);
//
//	}

//	function disable_run_flow() {
//		is_running = true;
//		dijit.byId("run_flow").set("disabled", true);
//		dijit.byId("step_run_flow").set("disabled", true);
//		dijit.byId("stop_flow").set("disabled", false);
//        disable_open_properties();
//	}
	
//	function enable_step_run_flow() {
//		dijit.byId("run_flow").set("disabled", false);
//		dijit.byId("step_run_flow").set("disabled", false);
//		dijit.byId("stop_flow").set("disabled", true);
//        enable_open_properties();
//	}

//	function disable_step_run_flow() {
//		dijit.byId("run_flow").set("disabled", true);
//		dijit.byId("step_run_flow").set("disabled", true);
//		dijit.byId("stop_flow").set("disabled", false);
//        disable_open_properties();
//	}

//    function enable_open_properties()
//    {
//        if (dijit.byId("open_sel_operator_prop")) dijit.byId("open_sel_operator_prop").set("disabled", false);
//        dijit.byId("edit_operator").set("disabled", false);
//    	if(dijit.byId("alpine_flow_operator_delete_btn"))
//    		dijit.byId("alpine_flow_operator_delete_btn").set("disabled", false);
//    }

//    function disable_open_properties()
//    {
//        if (dijit.byId("open_sel_operator_prop")) 
//        	dijit.byId("open_sel_operator_prop").set("disabled", true);
//        dijit.byId("edit_operator").set("disabled", true);
//
//    	if(dijit.byId("alpine_flow_operator_delete_btn"))
//    		dijit.byId("alpine_flow_operator_delete_btn").set("disabled", true);
//    }


	function getGroupList() {
		var url = baseURL + "/main/admin.do?method=getGroupsForUser" + "&user=" + login;
		ds.get(url, generate_select_list);
	}
	
	
	function upload_error(errorMsg){		
		popupComponent.alert(errorMsg);
	}

	var dynamic_import_flow_files=new Array(); 
 

	
	function clear_import_flow_Selection(){

		for(var i=0;i<dynamic_import_flow_files.length;i++){
			if(dynamic_import_flow_files[i]){
				document.getElementById("div_import_flow_files").removeChild(dynamic_import_flow_files[i]);
			}
			
		}
	dynamic_import_flow_files=new Array(); 
	}
	
//	function add_more_import_flow() {
//
//		var trElement = document.createElement("tr");
//
//		dynamic_import_flow_files[dynamic_import_flow_files.length ] = trElement;
//		var tdElement = document.createElement("td");
//		tdElement.innerHTML ="<input type = \"checkbox\" onClick = \"sync_import_flow_checkbox_status()\" ></input>";
//		trElement.appendChild(tdElement);
//
//		tdElement = document.createElement("td");
//		trElement.appendChild(tdElement);
//		//timemills
//		var import_flow_index = Date.parse(new Date());
//		tdElement.innerHTML = "<label>" +alpine.nls.file_name+
//		" " //+ (import_index + 1)
//			+ ": </label><input type=\"file\" name=\"fFile_flow_"
//			+ import_flow_index + "\" id=\"fFile_flow_" + import_flow_index
//			+ "\" size= \"" + getFileInputSize() + "\" onChange=\"hasSameUploadFileName(this)\"/>";
//
//
////		tdElement.innerHTML = "<label>File " //+ (import_index + 1)
////				+ ": </label><input type=\"file\"  size= \"33\" onChange=\"hasSameUploadFileName(this)\"/>";
////
//		// var input = document.createElement("div");
//
//
//		// input.innerHTML="<label>File "+(import_index+1)+": </label><input
//		// type=\"file\" name=\"fFile_flow_"+import_index+
//		// "\" id=\"fFile_flow_"+import_index+"\" style=\"width: 380px\"/>";
//
//		document.getElementById("div_import_flow_files").appendChild(trElement);
//		// max 6 files
//		sync_import_flow_btnstatus();
//
//		function getFileInputSize(){
//			if(dojo.isIE){
//				getFileInputSize = function(){
//					return "35";
//				};
//			}else{
//				getFileInputSize = function(){
//					return "35";
//				};
//			}
//			return getFileInputSize();
//		}
//
//	}
	function sync_import_flow_btnstatus ()
	{
		if (dynamic_import_flow_files.length > 5) {
			dijit.byId("add_more_import_flow").set("disabled", true);
		}else{
			dijit.byId("add_more_import_flow").set("disabled", false);
		}
	
		if (dynamic_import_flow_files.length == 0) {
			dijit.byId("remove_last_import_flow").set("disabled", true);
		} else {
			dijit.byId("remove_last_import_flow").set("disabled", false);
		}
		sync_import_flow_checkbox_status();
	}
	
	function sync_import_flow_checkbox_status(){
		var hasChecked= false;
		for(var i =0;i<dynamic_import_flow_files.length;i++){
			var tr = dynamic_import_flow_files[i];
			var checkbox=tr.children[0].children[0] ; 		
			if(checkbox&&checkbox.checked==true){
				hasChecked = true ;
			}
		}
	 
		if (hasChecked == false) {
			dijit.byId("remove_last_import_flow").set("disabled", true);
		} else {
			dijit.byId("remove_last_import_flow").set("disabled", false);
		}
	}
	
	function remove_import_flows(){
		var new_dynamic_import_flow_files = new Array();
		
 		for(var i =0;i<dynamic_import_flow_files.length;i++){
 			var tr =dynamic_import_flow_files[i];
 			var checkbox=tr.children[0].children[0] ; 			                                                          	;
 			if(checkbox){
 				if(checkbox.checked==true){
 					document.getElementById("div_import_flow_files").removeChild(tr);
 				}else{
 					new_dynamic_import_flow_files[new_dynamic_import_flow_files.length]=tr ;
 				}
 			}
 		}
 		dynamic_import_flow_files= new_dynamic_import_flow_files;
 		
 		sync_import_flow_btnstatus();
		
	}

	/*
	function isUploadFileNameOK(uploadFileName){
		var hasError= false;
		if(!uploadFileName||uploadFileName.length==0){
			upload_error(alpine.nls.MSG_Please_select_file);
			hasError = true;
		}
		else {//special case for IE
			if(dojo.isIE&&uploadFileName.lastIndexOf("\\")>-1){
				uploadFileName=uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
			}
			//.afm lengt =4
			if(uploadFileName.length<5
			   ||uploadFileName.substring(uploadFileName.length-4)!=".afm"){
				   
			upload_error(alpine.nls.MSG_Please_selectFlow_file);
			hasError = true;
			}
			
			var flowName =uploadFileName.substring(0,uploadFileName.length-4);
		}
		
		return hasError;
	}
	
	function hasSameUploadFileName(uploadFileInput){
		var haveSameFile = dojo.query("input[type='file']").some(function(item){
			return uploadFileInput != item && uploadFileInput.value == item.value;
		});
		if(haveSameFile){
		 
			popupComponent.alert(alpine.nls.same_name_exists);
		 
			if(dojo.isIE){//IE
				uploadFileInput.outerHTML = uploadFileInput.outerHTML;
			}else{//FF
				uploadFileInput.value = "";
			}
		}
		
 
	}
 */

	
	function flow_tree_personal_cb_add(obj){
	
	
		
//		flow_tree_callback(obj, "Personal");
		
	
//		dijit.byId("open_flow_button").openDropDown();
		alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree();
		
		//find the flow info
		if(dojo.isIE&&uploadFileName.lastIndexOf("\\")>-1){
			uploadFileName = uploadFileName.substring(uploadFileName.lastIndexOf("\\")+1);
		}
		var flowName =uploadFileName.substring(0,uploadFileName.length-4);
		//replace the blank..
		flowName=flowName.replace(/\ /g,"_");
		
		CurrentFlowTree['Personal'].model.store._arrayOfTopLevelItems;
	}
	
//	function show_import_flow_progress_bar(){
//		progressBar.showLoadingBar();
//	//	var	progress_bar=  new dijit.ProgressBar({indeterminate  : true,style:"width: 340px; "},document.createElement("div"));
//	//	dijit.byId("db_progress_import_flow").setContent("<img src=\""+alpine.progressImage+"\" width =100%>");
//	//	dijit.byId("db_progress_import_flow").setContent(progress_bar.domNode);
//
//	}
//
//	function hide_import_flow_progress_bar(){
//		progressBar.closeLoadingBar();
//	}



	

	function share_a_flow_clear_user_group_list() {
		var sel = dojo.byId("share_with_user_group");
		while (sel.firstChild) {
			sel.removeChild(sel.firstChild);
		}		
	}
	
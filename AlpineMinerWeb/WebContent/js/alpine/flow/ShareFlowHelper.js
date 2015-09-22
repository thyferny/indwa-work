define(["alpine/flow/ShareFlowManager",
        "alpine/flow/WorkFlowManager"],function(shareFlowMgmt, workFlowManager){
    var constants = {
        CANVAS: "FlowDisplayPanelPersonal"

    };
	dojo.ready(function(){
        dijit.byId('share_flow_dlg').titleBar.style.display = "none";
		dojo.connect(dijit.byId('share_flow_button'),"onClick",open_share_flow_dlg);
		dojo.connect(dijit.byId('share_flow_OK_btn'),"onClick",function(){return share_a_flow();});
	});

	function open_share_flow_dlg(){
		if (!workFlowManager.isEditing()) {
			return;
		}
		getGroupList();
		if (dojo.isIE){
			dojo.byId("share_flow_comments").rows=4;
			dojo.byId("share_flow_comments").cols=58;
		}
		dijit.byId('share_flow_dlg').show();
		dojo.byId("share_flow_comments").value="";
	}
	// share flow
	function share_a_flow() {
		dijit.byId('share_flow_dlg').hide();
		//show progress bar
		//progressBar.showLoadingBar();
		
		if (!workFlowManager.isEditing()) {
			return;
		}
		var login = alpine.USER;
		var id = "share_flow_radio_button_table";
		var type = "Public";
		var name = null;
		
		var list = ShareNameList;
		var currSelectItem = dijit.byId(id).get("value");
		for (j = 0; j < list.length; j++) {
//			var button_id = id + ID_TAG + list[j];
//			var btn = dojo.byId(button_id);
			if (currSelectItem == list[j]) {
				name = currSelectItem;
				if (j > 0) {
					type = "Group";
				}
				break;
			}
		}
		
		var url = flowBaseURL + "?method=shareFlow" 
			+ "&user=" + login
			+ "&type=" + type + "&name=" + name;
		error_msg = alpine.nls.flow_exist_error;
		
		workFlowManager.getEditingFlow().comments= dojo.byId("share_flow_comments").value;
//		if(!workFlowManager.getEditingFlow().comments||workFlowManager.getEditingFlow().comments==""){
//			workFlowManager.getEditingFlow().comments =" ";
//		}
		shareFlowMgmt.share_a_flow(url,workFlowManager.getEditingFlow(),share_a_flow_callback_Success,share_a_flow_callback_Error, constants.CANVAS);
		
	}
	

	function share_a_flow_callback_Success(obj){
		//finally hide progress bar
		//progressBar.closeLoadingBar();
		if(obj && obj.error_code && obj.error_code!=0){
			if (obj.error_code == -1) {
				popupComponent.alert(alpine.nls.no_login, "", function(){
					window.top.location.pathname = loginURL;	
				});
			}else if(obj.error_code == 3){
				popupComponent.alert(alpine.nls.share_alert_already_exist);
			}else{
				popupComponent.alert(obj.messgae);
			}
		}
	
	}
	
	function share_a_flow_callback_Error(text, args){
		//add hide progress bar
		//progressBar.closeLoadingBar();
		error_callback(text, args);
	}
});
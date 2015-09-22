/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.
 * DuplicateFlowUIHelper
 * Author Gary
 */
define(["alpine/flow/DuplicateFlowManager",
        "alpine/flow/WorkFlowManager"], function(duplicateManager, workflowManager){
	var constants = {
        CANVAS: "FlowDisplayPanelPersonal",
        MENU: "btn_tree_duplicate_flow",
		DIALOG: "alpine_flow_duplicateflow_dialog",
		NEW_FLOW_NAME: "alpine_flow_duplicateflow_newFlowName",
		BUTTON_DUPLICATE: "alpine_flow_duplicateflow_doDuplicate"
	};
	
	dojo.ready(function(){
		dijit.byId(constants.NEW_FLOW_NAME).isValid = isFlowNameValidate;
		dojo.connect(dijit.byId(constants.BUTTON_DUPLICATE), "onClick", submitDuplicateFlow);
		dojo.connect(dijit.byId(constants.MENU), "onClick", showDuplicateDialog);
	});
	
	function showDuplicateDialog() {
		if(!workflowManager.isEditing()) {
			return;
		}
		dijit.byId(constants.NEW_FLOW_NAME).reset();
        dijit.byId(constants.DIALOG).titleBar.style.display = "none";
		dijit.byId(constants.DIALOG).show();
	}

	function isFlowNameValidate (){
		var input = dijit.byId(constants.NEW_FLOW_NAME);
	 
		var val = input.get('value');
		
//		if(!val||val.length==0){
//			if(alpine.nls&&alpine.nls.err_empty_name){
//				input.invalidMessage = alpine.nls.err_empty_name;
//			}
//			if(dijit.byId("duplicate_flow_ok")){
//				dijit.byId("duplicate_flow_ok").set("disabled",true);
//			}
//			return false;
//		}
		var patrn = /^[^~#%&*{}\/\\\:<>\?|\"\'\.]*$/;
		if(!patrn.test(val)){
			return false;
		};
//		if(invalidateResourceNames){
//			for(var i =0;i<invalidateResourceNames.length;i++){
//				var resName = invalidateResourceNames[i] ;
//				if(val.indexOf(resName)>-1){
//					if(alpine.nls&&alpine.nls.err_invalid_char_inname){
//						input.invalidMessage = alpine.nls.err_invalid_char_inname+" \'"+resName+"\'";
//					}
//					if(dijit.byId("duplicate_flow_ok")){
//						dijit.byId("duplicate_flow_ok").set("disabled",true);
//				 	}
//					return false;
//				}
//			}
//		} 
		if(dijit.byId(constants.BUTTON_DUPLICATE)){
			dijit.byId(constants.BUTTON_DUPLICATE).set("disabled",false);
		}
		return true;
		
	 
	}

	function submitDuplicateFlow() {
		if(!dijit.byId(constants.NEW_FLOW_NAME).validate()){
			return;
		}
		
		var newFlowName = dijit.byId(constants.NEW_FLOW_NAME).get("value");
		if (newFlowName == null || newFlowName == "") {
			popupComponent.alert(alpine.nls.duplicateflow_alert_noNewName);
			return;
		}
//		if (newFlowName == CurrentFlow.id) {
//			popupComponent.alert(alpine.nls.duplicateflow_alert_newflowisexist);
//			return;
//		}
		//progressBar.showLoadingBar();
		duplicateManager.duplicate(newFlowName, workflowManager.getEditingFlow(), function(){
		//	progressBar.closeLoadingBar();
			dijit.byId(constants.DIALOG).hide();
			alpine.flow.FlowCategoryUIHelper.rebuildCategoryTree();
		}, constants.CANVAS);
	}
});

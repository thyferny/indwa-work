/* COPYRIGHT 2011 (c) Alpine Data Labs. All Rights Reserved.    
 * WorkFlowEditToolbarHelper.js 
 * Author Gary
 * Dec 6, 2012
 */
define([
    "dojo/dom",
    "dojo/html",
    "dojo/on",
    "dijit/registry",
    "alpine/operatorexplorer/OperatorUtil"
], function(dom, html, on, registry, operatorUtil){
	
	var constants = {
		CANVAS: "FlowDisplayPanelPersonal",
        CURRENT_OP_IMAGE: "current_operator_img",
        EDIT_PROPERTY_BTN: "edit_operator",
		DELETE_OPERATOR_BTN: "alpine_flow_operator_delete_btn",
		COPY_OPERATOR_BTN: "alpine_flow_operator_copy_btn",
		PASTE_OPERATOR_BTN: "alpine_flow_operator_paste_btn",
		RENAME_OPERATOR_BTN: "alpine_flow_operator_rename_btn",
        STEPRUN_OPERATOR_BTN: "step_run_flow",
        EXPLORE_OPERATOR_BTN: "operator_explore_button"
	};

	dojo.ready(function(){
        if (registry.byId(constants.DELETE_OPERATOR_BTN)) {
            on(registry.byId(constants.DELETE_OPERATOR_BTN), "click", function(){
                alpine.flow.OperatorManagementUIHelper.deleteSelectedOperators();// delete selected operators.
                alpine.flow.OperatorLinkUIHelper.deleteSelectedLink(constants.CANVAS);//delete selected link
            });
        }
        if (registry.byId(constants.RENAME_OPERATOR_BTN)){
            on(registry.byId(constants.RENAME_OPERATOR_BTN), "click", _renameOperatorFromButton);
        }
        registry.byId(constants.EDIT_PROPERTY_BTN).set("disabled", true);
		if(registry.byId(constants.DELETE_OPERATOR_BTN))
			registry.byId(constants.DELETE_OPERATOR_BTN).set("disabled", true);
        if (registry.byId(constants.EXPLORE_OPERATOR_BTN)) 
        	registry.byId(constants.EXPLORE_OPERATOR_BTN).set("disabled", true);
        if (registry.byId(constants.RENAME_OPERATOR_BTN))
        	registry.byId(constants.RENAME_OPERATOR_BTN).set("disabled", true);
        if (registry.byId(constants.COPY_OPERATOR_BTN))
        	registry.byId(constants.COPY_OPERATOR_BTN).set("disabled", true);
        if (registry.byId(constants.PASTE_OPERATOR_BTN))
        	registry.byId(constants.PASTE_OPERATOR_BTN).set("disabled", true);
	});
	
    function _setupOperatorToolbar(operatorHashInfo, operatorInfo) {
        opName = operatorInfo.name;
        opIcon = operatorUtil.getImageSourceByImageType(operatorHashInfo.imgtype);
        html.set(dom.byId("current_operator_label"), opName);
        dom.byId(constants.CURRENT_OP_IMAGE).src = opIcon;
        dom.byId(constants.CURRENT_OP_IMAGE).title = operatorHashInfo.label;
        
        registry.byId(constants.EDIT_PROPERTY_BTN).set("disabled", false);

    	if(registry.byId(constants.DELETE_OPERATOR_BTN))
    		registry.byId(constants.DELETE_OPERATOR_BTN).set("disabled", false);
        if (registry.byId(constants.COPY_OPERATOR_BTN))
            registry.byId(constants.COPY_OPERATOR_BTN).set("disabled", false);
        if (registry.byId(constants.RENAME_OPERATOR_BTN))
            registry.byId(constants.RENAME_OPERATOR_BTN).set("disabled", false);
        registry.byId(constants.STEPRUN_OPERATOR_BTN).set("disabled", false);
        if (alpine.flow.OperatorMenuHelper.genericNeedsDataExplorer(operatorInfo)) {
            if (registry.byId(constants.EXPLORE_OPERATOR_BTN))
            	registry.byId(constants.EXPLORE_OPERATOR_BTN).set("disabled", false);
        }
    }
    
    function _setupMultipleOperatorToolbar(){
    	_resetOperatorToolBar();
    	if(registry.byId(constants.DELETE_OPERATOR_BTN))
    		registry.byId(constants.DELETE_OPERATOR_BTN).set("disabled", false);
        if (registry.byId(constants.COPY_OPERATOR_BTN))
            registry.byId(constants.COPY_OPERATOR_BTN).set("disabled", false);
    }

    function _resetOperatorToolBar() {
        clearIcon =  baseURL + "/images/workbench_icons/blank.png";
        html.set(dom.byId("current_operator_label"), "");
        dom.byId(constants.CURRENT_OP_IMAGE).src = clearIcon;
        dom.byId(constants.CURRENT_OP_IMAGE).title = "";
        registry.byId(constants.EDIT_PROPERTY_BTN).set("disabled", true);
        _resetCommonButtons();
        if (registry.byId(constants.COPY_OPERATOR_BTN))
            registry.byId(constants.COPY_OPERATOR_BTN).set("disabled", true);
        if (registry.byId(constants.PASTE_OPERATOR_BTN))
            registry.byId(constants.PASTE_OPERATOR_BTN).set("disabled", !alpine.flow.CopyManager.hasCopied());
        if (registry.byId(constants.RENAME_OPERATOR_BTN))
            registry.byId(constants.RENAME_OPERATOR_BTN).set("disabled", true);
        registry.byId(constants.STEPRUN_OPERATOR_BTN).set("disabled", true);
        if (registry.byId(constants.EXPLORE_OPERATOR_BTN))
        	registry.byId(constants.EXPLORE_OPERATOR_BTN).set("disabled", true);
    }
    
    function _setupLinkToolbar(){
    	if(registry.byId(constants.DELETE_OPERATOR_BTN))
    		registry.byId(constants.DELETE_OPERATOR_BTN).set("disabled", false);
    }
    
    function _resetLinkToolbar(){
    	_resetCommonButtons();
    }
    
    /**
     * to reset all buttons of shared between Operator and Link.
     */
    function _resetCommonButtons(){
    	var anyOperatorSelected = alpine.flow.OperatorManagementUIHelper.getSelectedOperator(true) != null;
    	var anyLinkSelected = alpine.flow.OperatorLinkUIHelper.getSelectedLink() != null;
    	if(registry.byId(constants.DELETE_OPERATOR_BTN)
    			&& !anyOperatorSelected 
    			&& !anyLinkSelected){
    		registry.byId(constants.DELETE_OPERATOR_BTN).set("disabled", true);
    	}
    }

    function _renameOperatorFromButton() {
        alpine.flow.OperatorManagementUIHelper.renameSelectedOperator();
    }
    
    function _release(){
    	_resetOperatorToolBar();
        if (registry.byId(constants.PASTE_OPERATOR_BTN)) {
            registry.byId(constants.PASTE_OPERATOR_BTN).set("disabled", true);
        }
    	if(registry.byId(constants.DELETE_OPERATOR_BTN))
    		registry.byId(constants.DELETE_OPERATOR_BTN).set("disabled", true);
    }
    
    return {
    	setupOperatorToolbar: _setupOperatorToolbar,
    	setupMultipleOperatorToolbar: _setupMultipleOperatorToolbar,
    	resetOperatorToolbar: _resetOperatorToolBar,
    	setupLinkToolbar: _setupLinkToolbar,
    	resetLinkToolbar: _resetLinkToolbar,
    	release: _release
    };
});
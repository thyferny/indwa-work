/* COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * DeleteFlowUIHelper
 * Author: Robbie
 */

define([
    "dojo/ready",
    "dojo/on",
    "dojo/_base/array",
    "dijit/registry",
    "alpine/flow/DeleteFlowManager",
    "alpine/flow/WorkFlowManager",
    "alpine/flow/WorkFlowUIHelper",
    "alpine/flow/FlowCategoryUIHelper",
    "alpine/flow/RecentlyHistoryFlowManager"
], function(ready, on, array, registry, deleteFlowManager, workflowManager, workflowHelper, flowCategoryHelper, recentHistoryManager){

    var constants = {
        MENU: "delete_flow_action_button"
    };

    ready(function(){
        on(registry.byId(constants.MENU),"click",deleteOpenFlow);
    });

    function deleteOpenFlow() {
        var openedFlowInfo = [];
        openedFlowInfo.push(workflowManager.getEditingFlow());
        _deleteFlows(openedFlowInfo, "Personal", alpine.nls.delete_open_flow_prompt);
    }

    function _deleteFlows(flowInfo, type, message) {
        popupComponent.confirm(message, {
            handle: function(){
                if (flowInfo.length) {
                    var callback = null;
                    if (type == "Personal") {
                        callback = flowCategoryHelper.rebuildCategoryTree;
                    }
                    else {
                        callback = flow_tree_group_cb;
                    }
                    array.forEach(flowInfo, function(flow){
                        recentHistoryManager.removeFlowFromHistory(flow);
                    });
                    deleteFlowManager.deleteFlow(flowInfo, callback, type);
                    if (workflowManager.isEditing()) {
                        for (var j = 0; j < flowInfo.length; j++) {
                            if (workflowManager.isEditing(flowInfo[j])) {
                                workflowHelper.release();
                                flowCategoryHelper.removeWorkFlowEditingTrail();
                                break;
                            }
                        }
                    }
                }
            },
            label: alpine.nls.action_delete_open_flow
        });

    }

    return {};
});
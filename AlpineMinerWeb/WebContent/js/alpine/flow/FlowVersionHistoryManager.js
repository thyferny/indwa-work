/* COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * FlowVersionHistoryManager
 * Author: Robbie
 */

define([],function(){

    var constants = {
        FLOW_HISTORY_URL: baseURL + "/main/flow/version.do?method=getFlowVersionInfos"
    };

    function _fetchFlowHistory(flowArray, callback, panelID) {
        ds.post(constants.FLOW_HISTORY_URL, flowArray, callback, null, false, panelID);
    }


    return {
        fetchFlowHistory: _fetchFlowHistory
    };

});
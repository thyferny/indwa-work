/* COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * DeleteFlowManager
 * Author: Robbie
 */

define([],function(){

    var constants = {
        URL_BASE_DEL: flowBaseURL + "?method=deleteFlowList"
    };

    function _deleteFlow(flowInfo, callback, type) {
        var url = constants.URL_BASE_DEL + "&user=" + login + "&type=" + type;
        error_msg = alpine.nls.delete_flow_error;
        ds.post(url, flowInfo, callback, error_callback);
    }

    return {
        deleteFlow: _deleteFlow
    };

});
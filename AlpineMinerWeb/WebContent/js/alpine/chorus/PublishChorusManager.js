/**
 * User: robbie
 * Date: 1/15/13
 */


define([], function () {

    var chorusDO = baseURL + "/main/chorus.do";

    var constants = {
        URL_GET_WS: chorusDO + "?method=getActiveWorkSpaces",
        URL_PUBLISH: chorusDO + "?method=publishFlow",
        URL_UPDATE_KEY: chorusDO + "?method=updateUserAPIKey"
    };

    function _getActiveWorkSpaces(callback, errorCallback, callbackPaneId) {
        ds.get(constants.URL_GET_WS, callback, errorCallback, false, callbackPaneId);
    }

    function _publishFlow(flowArray, workspace, desc, callback, errorCallback, callbackPaneId) {
        var url = constants.URL_PUBLISH + "&workspace=" + encodeURIComponent(workspace) + "&description=" + encodeURIComponent(desc);
        ds.post(url, flowArray, callback, errorCallback, false, callbackPaneId);
    }

    function _updateAPIKey(key, callback, errorCallback, callbackPaneId) {
        var url = constants.URL_UPDATE_KEY + "&api_key=" + encodeURIComponent(key);
        ds.get(url, callback, errorCallback, false, callbackPaneId);
    }

    return {
        getActiveWorkSpaces: _getActiveWorkSpaces,
        publishFlow: _publishFlow,
        updateAPIKey: _updateAPIKey
    }

});
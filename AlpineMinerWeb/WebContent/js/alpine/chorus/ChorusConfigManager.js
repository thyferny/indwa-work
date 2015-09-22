/**
 * ChorusConfigManager.js
 * @author robbie
 * 01/14/2013
 */

define(function () {

    var chorusDO = baseURL + "/main/chorus.do";

    var constants = {
        URL_GET_CONFIG: chorusDO + "?method=getChorusConfig",
        URL_UPDATE_CONFIG: chorusDO + "?method=updateChorusConfig",
        URL_TEST_CONN: chorusDO + "?method=testChorusConnection"
    };

    function _getChorusConfig(callback, callbackPanelId) {
        ds.get(constants.URL_GET_CONFIG, callback, null, false, callbackPanelId);
    }

    function _updateChorusConfig(chorusConfig, callback, errorCallback, callbackPaneId) {
        ds.post(constants.URL_UPDATE_CONFIG, chorusConfig, callback, errorCallback, false, callbackPaneId);
    }

    function _testConnection(config, callback, errorCallback, callbackPaneId) {
        ds.post(constants.URL_TEST_CONN, config, callback, errorCallback, false, callbackPaneId);
    }

    return {
        getChorusConfig: _getChorusConfig,
        updateChorusConfig: _updateChorusConfig,
        testConnection: _testConnection
    };

});
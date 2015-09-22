/**
 * ChorusConfigUIHelper.js
 * @author robbie
 * 01/14/2013
 */

define([
    "dojo/ready",
    "dojo/on",
    "dojo/dom",
    "dijit/registry",
    "alpine/chorus/ChorusConfigManager"
], function (ready, on, dom, registry, chorusManager) {

    var constants = {
        MENU_BTN: "chorusConfigurationMenuItem",
        DIALOG: "chorusConfigurationDialog",
        HOST: "host",
        PORT: "port",
        KEY: "apiKey",
        BTN_CANCEL: "chorusConfigurationDialog_cancel",
        BTN_OK: "chorusConfigurationDialog_ok",
        BTN_TEST: "chorusConfigurationDialog_test",
        HOST_TB: "chorusHostTB",
        PORT_TB: "chorusPortTB",
        API_KEY_LABEL: "chorusAlpineAPIKey"
    };

    var listeners = [];
    var apiKey;

    ready(function () {
        if (registry.byId(constants.MENU_BTN)) {
            // only for admin users
            on(registry.byId(constants.MENU_BTN), "click", showDialog);
        }
    });

    function showDialog() {
        listeners.push(on(registry.byId(constants.BTN_CANCEL), "click", hideDialog));
        listeners.push(on(registry.byId(constants.BTN_TEST), "click", testConnection));
        listeners.push(on(registry.byId(constants.BTN_OK), "click", saveAndClose));
        retrieveConfig();
    }

    function hideDialog() {
        registry.byId(constants.DIALOG).hide();
    }

    function saveAndClose() {
        //set the new connection info
        var newConfig = _getConfigFromUI();
        chorusManager.updateChorusConfig(newConfig, updateConfigCallback, null, constants.DIALOG);
    }

    function clean() {
        listeners.forEach(function (handle) {
            handle.remove();
        });
        dom.byId(constants.API_KEY_LABEL).innerHTML = '';
        apiKey = '';
    }

    function getChorusConfigCallback(chorusConfig) {
        registry.byId(constants.HOST_TB).set('value', chorusConfig[constants.HOST]);
        registry.byId(constants.PORT_TB).set('value', chorusConfig[constants.PORT]);
        apiKey = chorusConfig[constants.KEY];
        dom.byId(constants.API_KEY_LABEL).innerHTML = apiKey;
        registry.byId(constants.DIALOG).show();
        on.once(registry.byId(constants.DIALOG), "hide", clean);
    }

    function updateConfigCallback(obj) {
        if (obj.error_code) {
            handle_error_result(obj);
            return;
        }
        hideDialog();
    }

    function retrieveConfig() {
        chorusManager.getChorusConfig(getChorusConfigCallback, "FlowDisplayPanelPersonal");
    }

    function testConnection() {
        var newConfig = _getConfigFromUI();
        chorusManager.testConnection(newConfig, testConnectionCallback, null, constants.DIALOG);
    }

    function testConnectionCallback(result) {
        popupComponent.alert(alpine.nls.MSG_TEST_Connenction_OK, "Test Result");
    }

    function _getConfigFromUI() {
        var newConfig = {};
        newConfig[constants.HOST] = registry.byId(constants.HOST_TB).get('value');
        newConfig[constants.PORT] = registry.byId(constants.PORT_TB).get('value');
        newConfig[constants.KEY] = apiKey;
        return newConfig;
    }

});
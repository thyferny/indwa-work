/**
 * User: robbie
 * Date: 1/15/13
 */

define([
    "dojo/ready",
    "dojo/dom",
    "dojo/dom-construct",
    "dojo/dom-class",
    "dojo/query",
    "dojo/dom-style",
    "dojo/on",
    "dojo/_base/array",
    "dijit/registry",
    "dijit/form/Select",
    "dijit/form/TextBox",
    "alpine/chorus/PublishChorusManager",
    "alpine/flow/WorkFlowManager"
], function (ready, dom, domConstruct, domClass, query, domStyle, on, array, registry, Select, TextBox, publishChorusManager, workflowManager) {

    var constants = {
        CANVAS: "FlowDisplayPanelPersonal",
        MENU_BTN: "publish_chorus_button",
        DIALOG: "publishChorusDialog",
        BTN_OK: "publishChorus_ok",
        BTN_CANCEL: "publishChorus_cancel",
        DLG_CONTENT_TOP: "chorusPublishContentTop",
        DLG_CONTENT: "chorusPublishContent",
        SPACES_SEL: "chorusWorkSpaceSelect",
        SPACES_TB: "chorusWorkSpaceTA",
        KEY: {
            DIALOG: "chorusAPIKeyDialog",
            BTN_OK: "chorusAPIKey_ok",
            BTN_CANCEL: "chorusAPIKey_cancel",
            DLG_CONTENT_TOP: "chorusAPIKeyContentTop",
            DLG_CONTENT: "chorusAPIKeyContent",
            KEY_TB: "chorusKeyTB"
        }
    };

    var listeners = [];
    var tracker = [];
    var selectedFlows = null;

    ready(function () {
        on(registry.byId(constants.MENU_BTN), "click", showDialog);
    });

    function showDialog() {
        listeners.push(on(registry.byId(constants.BTN_CANCEL), "click", hideDialog));
        listeners.push(on(registry.byId(constants.BTN_OK), "click", _publish));
        _getWorkSpaces();
    }

    function _getWorkSpaces() {
        if (alpine.userInfo.chorusKey && alpine.userInfo.chorusKey != "null") {
            publishChorusManager.getActiveWorkSpaces(_getWorkSpacesCallback, _getWorkSpacesErrorCallback, "FlowDisplayPanelPersonal");
        } else {
            _openUpdateKeyDialog()
        }
    }

    function _getWorkSpacesErrorCallback(obj) {
        // TODO: if unauthorized but not if timeout
        _openUpdateKeyDialog()

    }

    function _getWorkSpacesCallback(obj) {
        var holder = domConstruct.create("div", {}, dom.byId(constants.DLG_CONTENT_TOP));
        domConstruct.create("label", {class: "valueLabel", innerHTML: "Select a workspace: "}, holder);
        var spaces = [];
        array.forEach(obj.response, function (ws) {
            spaces.push({
                value: ws.id,
                label: ws.name
            });
        });
        var spacesSelect = new Select({
            id: constants.SPACES_SEL,
            baseClass: "greyDropdownButton",
            options: spaces
        }, domConstruct.create("div", {}, holder));
        tracker.push(spacesSelect);
        domConstruct.create("label", {class: "valueLabel", innerHTML: "Description:"}, dom.byId(constants.DLG_CONTENT));
        holder = domConstruct.create("div", {style: "margin:auto;"}, dom.byId(constants.DLG_CONTENT));
        var spaceTextbox = new TextBox({
            id: constants.SPACES_TB,
            style: "width:95%",
            baseClass: "inlineTextbox"
        }, domConstruct.create("div", {}, holder));
        tracker.push(spaceTextbox);
        registry.byId(constants.DIALOG).show();
        on.once(registry.byId(constants.DIALOG), "hide", clean);
    }

    function _publish() {
        var flows = selectedFlows;
        if (flows == null) {
            flows = [workflowManager.getEditingFlow()];
        }
        var workspace = registry.byId(constants.SPACES_SEL).get('value');
        var desc = registry.byId(constants.SPACES_TB).get('value');
        hideDialog();
        publishChorusManager.publishFlow(flows, workspace, desc, _publishCallback, null, constants.DIALOG);
    }

    function _publishCallback(obj) {
        console.log("successful publish");
    }

    function hideDialog() {
        registry.byId(constants.DIALOG).hide();
    }

    function clean() {
        listeners.forEach(function (handle) {
            handle.remove();
        });
        tracker.forEach(function (widget) {
            widget.destroyRecursive();
        });
        domConstruct.empty(constants.DLG_CONTENT_TOP);
        domConstruct.empty(constants.DLG_CONTENT);
        domConstruct.empty(constants.KEY.DLG_CONTENT_TOP);
        domConstruct.empty(constants.KEY.DLG_CONTENT);
        selectedFlows = null;
    }

    function _openUpdateKeyDialog() {
        listeners.push(on(registry.byId(constants.KEY.BTN_CANCEL), "click", hideKeyDialog));
        listeners.push(on(registry.byId(constants.KEY.BTN_OK), "click", _updateKey));
        var holder = domConstruct.create("div", {}, dom.byId(constants.KEY.DLG_CONTENT_TOP));
        domConstruct.create("label", {class: "valueLabel", innerHTML: "Enter your Chorus API key: "}, holder);
        holder = domConstruct.create("div", {style: "margin:auto;"}, dom.byId(constants.KEY.DLG_CONTENT));
        var spaceTextbox = new TextBox({
            id: constants.KEY.KEY_TB,
            style: "width:95%",
            baseClass: "inlineTextbox"
        }, domConstruct.create("div", {}, holder));
        tracker.push(spaceTextbox);
        registry.byId(constants.KEY.DIALOG).show();
        on.once(registry.byId(constants.KEY.DIALOG), "hide", clean);
    }

    function hideKeyDialog() {
        registry.byId(constants.KEY.DIALOG).hide();
    }

    function _updateKey() {
        var key = registry.byId(constants.KEY.KEY_TB).get('value');
        publishChorusManager.updateAPIKey(key, _updateKeyCallback, null, constants.CANVAS);
        registry.byId(constants.KEY.DIALOG).hide();
    }

    function _updateKeyCallback(obj) {
        showDialog();
    }


});
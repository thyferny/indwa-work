/**
 * User: robbie
 * Date: 1/15/13
 */


define([
    "dojo/ready",
    "dojo/dom",
    "dojo/dom-construct",
    "dojo/on",
    "dijit/registry",
    "dijit/form/TextBox",
    "dijit/form/CheckBox",
    "alpine/chorus/ChorusCommentManager"
], function (ready, dom, domConstruct, on, registry, TextBox, CheckBox, chorusCommentManager) {

    var constants = {
        DIALOG: "chorusCommentDialog",
        BTN_OK: "chorusComment_ok",
        BTN_CANCEL: "chorusComment_cancel",
        DLG_CONTENT_TOP: "chorusCommentContentTop",
        DLG_CONTENT: "chorusCommentContent",
        TB: "chorusCommentTB",
        CB: "chorusCommentCB"
    };

    var listeners = [];
    var tracker = [];

    function showCommentDialog() {
        listeners.push(on(registry.byId(constants.BTN_OK), 'click', postComment));
        listeners.push(on(registry.byId(constants.BTN_CANCEL), 'click', hideDialog));

        var holder = domConstruct.create("div", {}, dom.byId(constants.DLG_CONTENT_TOP));
        domConstruct.create("label", {class: "valueLabel", innerHTML: "Comment: "}, holder);
        holder = domConstruct.create("div", {style: "margin:auto;"}, dom.byId(constants.DLG_CONTENT));
        var tb = new TextBox({
            id: constants.TB,
            style: "width:95%",
            baseClass: "inlineTextbox"
        }, domConstruct.create("div", {}, holder));
        tracker.push(tb);

        holder = domConstruct.create("div", {style: "margin:auto; padding-top: 10px;"}, dom.byId(constants.DLG_CONTENT));
        var cb = new CheckBox({
            id: constants.CB
        }, domConstruct.create("div", {}, holder));
        tracker.push(cb);
        domConstruct.create("label", {class: "valueLabel", innerHTML: "Is insight?"}, holder);

        registry.byId(constants.DIALOG).show();
        on.once(registry.byId(constants.DIALOG), 'hide', clean)
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
    }

    function hideDialog() {
        registry.byId(constants.DIALOG).hide();
    }

    function postComment() {
        var info = {};
        info['comment'] = registry.byId(constants.TB).get('value');
        info['checked'] = registry.byId(constants.CB).get('checked');
        if (chorusWorkfileID) { info['entity_id'] = chorusWorkfileID; }
        if (chorusWorkfileType) { info['entity_type'] = chorusWorkfileType; }
        hideDialog();
        chorusCommentManager.postBack(info, callback, errorCallback, constants.DIALOG);
    }

    function callback() {
        console.log("comment successful");
    }

    function errorCallback() {
        console.log("comment errorCallback");
    }

    return {
        showCommentDialog: showCommentDialog
    }

});
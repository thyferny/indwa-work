/**
 * User: robbie
 * Date: 9/24/12
 * Time: 8:02 PM
 */


define([
    "dojo/_base/declare",
    "alpine/layout/ExtendedTextBox"],

    function(
        declare,
        ExtendedTextBox) {


declare("alpine.layout.ClearableTextBox", [ExtendedTextBox],{

    deleteText: "Delete",
    intermediateChanges: true,

    postCreate: function() {
        this.inherited(arguments);
        var domNode = this.domNode;
        dojo.addClass(domNode, "clearableTextBox");
        this.clearLink = dojo.create("a", {
            className: "textClear",
            innerHTML: this.deleteText
        }, domNode, "first");
        var startWidth = dojo.style(domNode, "width"),
            pad = dojo.style(this.domNode,"paddingRight");
        dojo.style(domNode, "width", (startWidth - pad) + "px");
        this.connect(this.clearLink, "onclick", function(){
            this.set("value", "");
            this.textbox.focus();
        });
        this.connect(this, "onChange", "checkValue");
        this.checkValue();
    },
    checkValue: function(value) {
        dojo[(value != "" && value != undefined ? "remove" : "add") + "Class"](this.clearLink, "dijitHidden");
    }
});

        });

/**
 * User: robbie
 * Date: 10/15/12
 * (c) Alpine Data Labs 2012
 */
define([
    'dojo/_base/declare',
    'dijit/form/DropDownButton'
], function(declare, DropDownButton){

    return declare([DropDownButton],{

        postCreate: function() {
            this.inherited(arguments);
            this._setCustomLabel();
        },

        validate: function(/*Boolean*/ isFocused) {
            return this.dropDown.content.validateContent()
        },

        onBlur: function() {
            var isValid = this.validate();
            this.focusNode.setAttribute("aria-invalid", isValid ? "false" : "true");
            this._set('state',isValid ? "" : "Error");
            this._setCustomLabel();
            this.inherited(arguments);
        },

        _setCustomLabel: function() {
            var customWidget = this.dropDown.content;
            this.set('label',customWidget.getCustomLabel());
        }
    });

});
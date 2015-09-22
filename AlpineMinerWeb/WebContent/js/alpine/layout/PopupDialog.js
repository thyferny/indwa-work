/**
 * User: sasher
 * Date: 10/2/12
 * Time: 10:23 AM
 *
 * see http://codewut.de/content/disable-close-button-dojo-dijitdialog
 */

define([
    "dojo/_base/declare",
    "dijit/Dialog",
    "dojo/dom-class",
    "dojo/dom-style"],

    function(
        declare,
        Dialog,
        domClass,
        domStyle){

        return declare(
        "alpine.layout.PopupDialog",
        [Dialog],
        {
            // summary:
            //    extended version of the dojo Dialog widget with the option to disable
            //    the close button and supress the escape key.

            disableCloseButton: true,
            baseClass: "alpineDialog",

            /* *********************************************************** postCreate */
            postCreate: function()
            {
                this.inherited(arguments);
                this._updateCloseButtonState();
                if (this.titleBar)
                {
                    domClass.add(this.titleBar, "alpineDialog");
                }
            },

            /* *************************************************************** _onKey */
            _onKey: function(evt)
            {
                //no close button but we want ESCAPE to hide
                //if(this.disableCloseButton && evt.charOrCode == dojo.keys.ESCAPE) return;
                this.inherited(arguments);
            },

            /* ************************************************ setCloseButtonDisabled*/
            setCloseButtonDisabled: function(flag)
            {
                this.disableCloseButton = flag;
                this._updateCloseButtonState();
            },

            /* ********************************************** _updateCloseButtonState */
            _updateCloseButtonState: function()
            {
                domStyle.set(this.closeButtonNode,
                    "display",this.disableCloseButton ? "none" : "block");
            }
        }
    )}
);
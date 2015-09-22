/**
 * User: sasher
 * Date: 6/28/12
 * Time: 4:01 PM
 */


define([
    "dojo/_base/declare",
    "dojox/widget/Standby"],

function(
    declare,
    Standby) {


return declare('alpine.layout.StandbySpinner', [Standby], {
    templateString:
        "<div>" +
            "<div style=\"display: none; opacity: 0; z-index: 9999; " +
            "position: absolute; \" dojoAttachPoint=\"_underlayNode\"></div>" +
            "<img src=\"${image}\" style=\"opacity: 1; display: none; z-index: -10000; " +
            "position: absolute; top: 0px; left: 0px; cursor:wait;\" "+
            "dojoAttachPoint=\"_imageNode\">" +
            "<div style=\"opacity: 1; display: none; z-index: -10000; position: absolute; " +
            "top: 0px;\" dojoAttachPoint=\"_textNode\"></div>" +
            "</div>",

    postCreate: function(){
        this.inherited(arguments);
    },

    _fadeIn: function(){
        // summary:
        //		Internal function that does the opacity style fade in animation.
        // tags:
        //		private
        var self = this;
        self._underlayNode.opacity=.75;
        self._centerNode.opacity = 1;
        self.onShow();
    },

    _fadeOut: function(){
        // summary:
        //		Internal function that does the opacity style fade out animation.
        // tags:
        //		private
        var self = this;
        self._underlayNode.opacity=0;
        self._centerNode.opacity = 0;
        self.onHide();
        self._enableOverflow();
    }



});
});
/**
 * User: sasher
 * Date: 8/2/12
 * Time: 3:20 PM
 */

define([
    "dojo/_base/declare", // declare
    "dijit/form/TextBox"
], function(declare, TextBox){

    return declare('alpine.layout.ExtendedTextBox', [TextBox], {

        _onInput: function(e){
            this.inherited(arguments);
            if(this.intermediateChanges){ // _TextBoxMixin uses onInput
                var _this = this;
                // the setTimeout allows the key to post to the widget input box
                setTimeout(function(){ _this._handleOnChange(_this.get('value'), false); }, 0);
            }
            this._updatePlaceHolder();
        },
        _updatePlaceHolder: function(){
            if(this._phspan){
                this._phspan.style.display=(this.placeHolder&&(!this.textbox.value||this.textbox.value.length ==0))?"":"none";
            }
        }
    });

});

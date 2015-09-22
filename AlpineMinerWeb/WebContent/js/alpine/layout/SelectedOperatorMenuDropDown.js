/**
 * User: robbie and sara
 * Date: 7/30/12
 * Time: 12:58 PM
 */

define(["dojo/_base/declare","alpine/flow/OperatorMenuHelper","dijit/form/DropDownButton"], function(declare,menuHelper,DropDownButton){
    declare('alpine.layout.SelectedOperatorMenuDropDown', [DropDownButton], {

        isLoaded: function(){
                return false;
        },

        loadDropDown: function(){
                var dropDown = menuHelper.getSelectedOperatorMenu();
                this.dropDown = dropDown;
                if(!dropDown){ return; }
                this.openDropDown();
                return;
        }


    });

});
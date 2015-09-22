/**
 * User: sasher
 * Date: 8/16/12
 * Time: 11:47 AM
 */

define(function(){
    var constants = {
        TEXTFIELD_DIALOG: "alpine_textfield_dialog",
        TEXTFIELD_DIALOG_TITLE: "alpine_textfield_title",
        TEXTFIELD_DIALOG_VALUE: "alpine_textfield_value",
        TEXTFIELD_DIALOG_ACTION_BUTTON: "alpine_textfield_actionbutton",
        TEXTFIELD_DIALOG_CANCEL_BUTTON: "alpine_textfield_cancelbutton"
    };
function _showTextFieldEditorDialog(callback, initialValue, validatorString, dialogTitle, okbuttonLabel, validationHandler, keepOpenWindow)
{
    var eventHandlers = new Array();
    openDialog();
    function openDialog(){
        dijit.byId(constants.TEXTFIELD_DIALOG).show();
        dijit.byId(constants.TEXTFIELD_DIALOG).titleBar.style.display='none';
        dojo.byId(constants.TEXTFIELD_DIALOG_TITLE).innerHTML = dialogTitle;
        dojo.byId(constants.TEXTFIELD_DIALOG_ACTION_BUTTON).innerHTML = okbuttonLabel.toUpperCase();
        if(initialValue != undefined && initialValue != null){
            dijit.byId(constants.TEXTFIELD_DIALOG_VALUE).set("value", initialValue);
        }

        dijit.byId(constants.TEXTFIELD_DIALOG_VALUE).isValid = function(){
            var val = this.get("value");
            if (validatorString)
            {
                return validatorString.test(val);
            }
            else if (validationHandler)
            {
                return validationHandler.call(null,constants.TEXTFIELD_DIALOG_VALUE, val);
            };
        }


        dijit.byId(constants.TEXTFIELD_DIALOG_ACTION_BUTTON).onClick = function (){
            dijit.byId(constants.TEXTFIELD_DIALOG_VALUE)._hasBeenBlurred = true;
            //if passed in special validation function, do that.
            if (validationHandler)
            {
                var valueToTest =  dijit.byId(constants.TEXTFIELD_DIALOG_VALUE).value;
                if (!validationHandler.call(null, constants.TEXTFIELD_DIALOG_VALUE, valueToTest))
                {
                    return;
                }
            } else  // do default validation behavior
            {
                var checkRes = dijit.byId(constants.TEXTFIELD_DIALOG_VALUE).validate();
                if(!checkRes){
                    console.log("didn't pass validation");
                    return;
                }
            }

            //passes validation - do callback
            var val = dijit.byId(constants.TEXTFIELD_DIALOG_VALUE).get("value");
            if (!keepOpenWindow) _hideTextFieldEditorDialog();
            callback.call(null, val);
        };

        eventHandlers.push(dojo.connect(dijit.byId(constants.TEXTFIELD_DIALOG_CANCEL_BUTTON), "onClick", function (){
            dijit.byId(constants.TEXTFIELD_DIALOG).hide();
        }));
        eventHandlers.push(dojo.connect(dijit.byId(constants.TEXTFIELD_DIALOG), "onHide", finalize));
    }
    function finalize(){
        dijit.byId(constants.TEXTFIELD_DIALOG_VALUE).reset();
        var event = null;
        while((event = eventHandlers.pop()) != undefined){
            dojo.disconnect(event);
        }
    }
}

    function _hideTextFieldEditorDialog()
    {
        dijit.byId(constants.TEXTFIELD_DIALOG_VALUE).reset();
        if (dijit.byId(constants.TEXTFIELD_DIALOG)) dijit.byId(constants.TEXTFIELD_DIALOG).hide();

    }

   return {
       showTextFieldEditorDialog: _showTextFieldEditorDialog,
       hideTextFieldEditorDialog: _hideTextFieldEditorDialog
   }


});
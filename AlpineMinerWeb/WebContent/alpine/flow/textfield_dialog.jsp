<fmt:bundle basename="app">
    <div id="alpine_textfield_dialog" dojoType="dijit.Dialog" draggable="false" style="width: 320px;">
        <div id="alpine_textfield_title" class="titleBar"></div>
        <div class="innerPadding" >
            <!--<div class="errorDiv"><label id="alpine_textfield_error" class="error">  </label>  </div> -->
            <div>
                <input id="alpine_textfield_value" style="margin-bottom:3px;padding-top: 3px;padding-bottom: 3px;" required="true" dojoType="dijit.form.ValidationTextBox"/>
            </div>
        </div> <!-- end contentpane -->
        <div class="whiteDialogFooter">
            <button baseClass="cancelButton" valign = "bottom" dojoType="dijit.form.Button" type="button"  id = "alpine_textfield_cancelbutton"><fmt:message key="Cancel"/></button>
            <button baseClass="primaryButton" valign = "bottom" dojoType="dijit.form.Button" type="button" id = "alpine_textfield_actionbutton"></button>
        </div>  <!--end dialogFooter-->

    </div>
</fmt:bundle>
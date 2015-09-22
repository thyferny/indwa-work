<script type="text/javascript">
    <!--
    dojo.require("alpine.flow.DuplicateFlowUIHelper");
    dojo.require("alpine.flow.ImportFlowHelper");
    //-->
</script>
<fmt:bundle basename="app">
    <div id="alpine_flow_newflow_dialog" dojoType="dijit.Dialog" draggable="false" title="<fmt:message key='newflow_dialog_title'/>" style="width: 320px;">
        <div class="titleBar">
            <fmt:message key='createflow_dialog_title'/>
        </div>
        <div class="innerPadding" >
            <!--<div class="errorDiv"><label id="login_error" class="error">  </label>  </div> -->
            <div>
                <input id="alpine_flow_newflow_dialog_category" type="hidden" />
                <label class="form" style="margin-top:20px"><fmt:message key='createflow_dialog_workflow_name'/></label> <br/>
                <input id="alpine_flow_newflow_dialog_name" style="margin-bottom:3px;padding-top: 3px;padding-bottom: 3px;" required="true" dojoType="dijit.form.ValidationTextBox"/>
            </div>
            <div class="paddedTopDiv">
                <label class="form" ><fmt:message key='createflow_dialog_workflow_description'/></label> <br/>
                <input id="alpine_flow_newflow_dialog_description" style="margin-bottom:3px;padding-top: 3px;padding-bottom: 3px;" dojoType="dijit.form.SimpleTextarea" rows=5/>
            </div>
            <div class="paddedTopDiv">
                <label class="form" ><fmt:message key='createflow_dialog_workflow_defaultValue'/></label> <br/>
                <input id="alpine_flow_newflow_dialog_defaultvalue" style="margin-bottom:3px;padding-top: 3px;padding-bottom: 3px;" dojoType="dijit.form.ValidationTextBox" value="alp"/>
            </div>
        </div> <!-- end contentpane -->
        <div class="whiteDialogFooter">
            <button   baseClass="cancelButton" valign = "bottom" onclick="dijit.byId('alpine_flow_newflow_dialog').hide();" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
            <button  baseClass="primaryButton" valign = "bottom" dojoType="dijit.form.Button" type="button" id = "alpine_flow_newflow_create_flow"><fmt:message key="createflow_dialog_okbutton"/></button>
        </div>  <!--end dialogFooter-->

    </div>
</fmt:bundle>
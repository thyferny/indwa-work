<script type="text/javascript">
<!--
dojo.require("alpine.flow.DuplicateFlowUIHelper");
//-->
</script>
<fmt:bundle basename="app">
	<div id="alpine_flow_duplicateflow_dialog" dojoType="dijit.Dialog" draggable="false" title="<fmt:message key='duplicateflow_dialog_title'/>" style="width: 320px;">
    <div class="titleBar">
        <fmt:message key='duplicateflow_dialog_title'/>
    </div>
	<div class="innerPadding" >
        <b><fmt:message key="duplicateflow_label_newname"/> </b>
        <input dojoType="dijit.form.ValidationTextBox" required="true" id="alpine_flow_duplicateflow_newFlowName"
        trim="true">
	</div> <!-- end contentpane -->
        <div class="whiteDialogFooter">
            <button   baseClass="cancelButton" valign = "buttom" onclick="dijit.byId('alpine_flow_duplicateflow_dialog').hide();" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
            <button  baseClass="primaryButton" valign = "buttom" dojoType="dijit.form.Button" type="button" id = "alpine_flow_duplicateflow_doDuplicate"><fmt:message key="OK"/></button>
        </div>  <!--end dialogFooter-->

    </div>
</fmt:bundle>
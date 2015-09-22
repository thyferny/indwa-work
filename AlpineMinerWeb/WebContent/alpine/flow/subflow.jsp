<script type="text/javascript">
    dojo.require("alpine.props.SubflowVariablePropHelper");
</script>
<fmt:bundle basename="app">
<%--subflow mapping dialog--%>
<div dojoType="dijit.Dialog" draggable="false"
		id="subFlowPropertyTableMappingDialog" style="width: 540px;height: 415px;">
    <div class="titleBar">
        <fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>
    </div>
    <div id="tablemappingRowContainer" style="margin:4px;height:330px;width:100%;background:white;overflow-y:auto;"></div>
    <div class="whiteDialogFooter">
       <button dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
                                    id="subflow_tablemapping_cancel_button" onClick="hideTableMappingDlog()">
                                    <fmt:message key="Cancel" />
                                </button>
        <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
                id="subflow_tablemapping_ok_button" onClick="saveTableMappingSetting()">
            <fmt:message key="OK" />
        </button>
    </div>
</div>
<%--subflow variable dialog--%>
    <div dojoType="dijit.Dialog" id="subFlowPropertyVariableDialog" draggable="false" title="<fmt:message key='subflowVariable'/>">
        <div class="titleBar">
            <fmt:message key='subflowVariable'/>
        </div>
        <div dojoType="dijit.layout.LayoutContainer" style="width: 400px; height: 350px">
            <div dojoType="dijit.layout.ContentPane" region="top">
                <div style="float: right;padding-right: 15px;">
                    <button type="Button" dojoType="dijit.form.Button" baseClass="linkButton" id="subFlowPropertyVariable_button_add"><fmt:message key="create_button"/></button>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div dojoType="dijit.form.Form" id="subFlowPropertyVariable_variableForm" style="overflow: auto;">
                    <div id="subFlowPropertyVariable_variableGrid_container"></div>
                </div>
            </div>
        </div>
        <div class="whiteDialogFooter">
            <table width="100%">
                <tr>
                    <td align="right">
                        <button type="Button" dojoType="dijit.form.Button" baseClass="cancelButton" id="subFlowPropertyVariable_button_cancel"><fmt:message key="Cancel"/></button>
                        <button type="Button" dojoType="dijit.form.Button" baseClass="primaryButton" id="subFlowPropertyVariable_button_submit"><fmt:message key="OK"/></button>
                    </td>
                </tr>
            </table>
        </div>
    </div>
</fmt:bundle>
 
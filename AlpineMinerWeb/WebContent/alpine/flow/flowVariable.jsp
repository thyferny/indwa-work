<script type="text/javascript">
    dojo.require("alpine.flow.WorkFlowVariableHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" id="flow_flowvariable_dialog" draggable="false" title="<fmt:message key='flowvariable_dialog_title'/>">
		<div class="titleBar">
            <fmt:message key='flowvariable_dialog_title'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 500px; height: 350px">
			<div dojoType="dijit.layout.ContentPane" region="top">
				<div style="float: right;padding-right: 15px;">
					<button type="Button" dojoType="dijit.form.Button" baseClass="linkButton" id="flow_flowvariable_button_add"><fmt:message key="create_button"/></button>
				</div>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="center">
				<div dojoType="dijit.form.Form" id="flow_flowvariable_variableForm" style="overflow: auto;">
					<div id="flow_flowvariable_variableGrid_container"></div>
				</div>
			</div>
		</div>
		<div class="whiteDialogFooter">
			<table width="100%">
				<tr>
					<td align="right">
						<button type="Button" dojoType="dijit.form.Button" baseClass="cancelButton" id="flow_flowvariable_button_cancel"><fmt:message key="Cancel"/></button>
						<button type="Button" dojoType="dijit.form.Button" baseClass="primaryButton" id="flow_flowvariable_button_submit"><fmt:message key="OK"/></button>
					</td>
				</tr>
			</table>
		</div>
	</div>
</fmt:bundle>
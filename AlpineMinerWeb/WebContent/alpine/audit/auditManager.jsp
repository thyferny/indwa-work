<script type="text/javascript">
	dojo.require("alpine.system.AuditUIHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" title="<fmt:message key="audit_dialog_title"/>" id="alpine_system_auditmanager_Dialog">
		<div class="titleBar">
            <fmt:message key='audit_dialog_title'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 600px; height: 400px">
			<div dojoType="dijit.layout.ContentPane" region="top" style="width: 100%; height: 30px">
				<table width="100%">
					<tr>
						<td>
							<fmt:message key="audit_grid_title_category"/>
							<select dojoType="dijit.form.Select" id="alpine_system_auditmanager_query_category" baseClass="greyDropdownButton" style="width:auto;"></select>
						</td>
						 
					</tr>
				</table>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%;">
				<div id="alpine_system_auditmanager_grid"></div>
			</div>
		</div>
		<div class="whiteDialogFooter">
			<button dojoType="dijit.form.Button" baseclass="cancelButton" onclick="dijit.byId('alpine_system_auditmanager_Dialog').hide()"><fmt:message key="Done"/></button>
			<button dojoType="dijit.form.Button" baseclass="primaryButton" id="alpine_system_auditmanager_button_clear" type="button" style="margin-right:5px;"><fmt:message key="clear_log_info_btn"/></button>
		</div>
	</div>
</fmt:bundle>
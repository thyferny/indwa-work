<script type="text/javascript">
	dojo.require("alpine.system.CleanupSessionUIHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" id="alpine_system_sessionMgr_dialg" title="<fmt:message key='session_manager_title'/>">
		<div class="titleBar">
            <fmt:message key='session_manager_title'/>
        </div>
        <br/>
		<div dojoType="dijit.layout.BorderContainer" style="width: 400px; height: 300px">
			<div dojoType="dijit.layout.ContentPane" region="center">
				<div id="alpine_system_sessionMgr_grid" style="height: 100%"></div>
			</div>
		</div>
		<div class="whiteDialogFooter">
			<button baseClass="cancelButton" type="button" dojoType="dijit.form.Button" onClick="dijit.byId('alpine_system_sessionMgr_dialg').hide()"><fmt:message key='Done'/></button>
			<button baseClass="primaryButton" type="button" dojoType="dijit.form.Button" id="alpine_system_sessionMgr_clean"><fmt:message key='session_manager_button_kill'/></button>
		</div>
	</div>
</fmt:bundle>
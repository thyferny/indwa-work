<script type="text/javascript">
<!--
	dojo.require("alpine.flow.ClearIntermediateTableUIHelper");
//-->
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="alpine_flow_clearintermediatetable_dialog" 
		title="<fmt:message key='table_clean_title'/>">
		<div class="titleBar">
            <fmt:message key='table_clean_title'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width:600px;height:300px">
			<div dojoType="dijit.layout.ContentPane" region="center">
				<div id="alpine_flow_clearintermediatetable_grid"></div>
			</div>
		</div>
		<div class="whiteDialogFooter">
			<button dojoType="dijit.form.Button" type="button" baseClass="cancelButton" onclick="dijit.byId('alpine_flow_clearintermediatetable_dialog').hide()"><fmt:message key='Cancel'/></button>
			<button dojoType="dijit.form.Button" type="button" baseClass="primaryButton" id="alpine_flow_clearintermediatetable_submit"><fmt:message key='OK'/></button>
		</div>
	</div>
</fmt:bundle>
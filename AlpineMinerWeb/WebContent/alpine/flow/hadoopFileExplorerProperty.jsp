<script type="text/javascript">
	dojo.require("alpine.props.HadoopFileExplorerHelper");
</script>
<%-- Hadoop file explorer widget --%>
<div dojoType="dijit.Dialog" draggable="false" title="<fmt:message key='hadoop_props_file_explorer_dialog_title'/>" id="alpine_props_hadoopcommonproperty_fileexplorer_dialog">
	<div class="titleBar">
        <fmt:message key='hadoop_props_file_explorer_dialog_title'/>
    </div>
	<div dojoType="dijit.layout.BorderContainer" style="width: 300px; height: 400px">
		<div dojoType="dijit.layout.ContentPane" region="center">
			<div id="alpine_props_hadoopcommonproperty_fileexplorer_tree_container"></div>
		</div>
	</div>
	<div class="whiteDialogFooter">
		<button type="button" dojoType="dijit.form.Button" onClick="dijit.byId('alpine_props_hadoopcommonproperty_fileexplorer_dialog').hide()" baseClass="cancelButton">
			<fmt:message key="Cancel"/>
		</button>
		<button type="button" dojoType="dijit.form.Button" id="alpine_props_hadoopcommonproperty_fileexplorer_submit" baseClass="primaryButton">
			<fmt:message key="OK"/>
		</button>
	</div>
</div>
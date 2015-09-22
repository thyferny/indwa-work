<script type="text/javascript">
    dojo.require("alpine.dbconnection.dbMetadataCacheManager");
</script>

<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="alpine_flow_dbSourceUpdater_Dialog" 
		title="<fmt:message key='dbresource_title'/>">
        <div class="titleBar">
            <fmt:message key='dbresource_title'/>
        </div>
        <div class="innerPadding">
            <div dojoType="dijit.layout.BorderContainer" style="width: 400px; height: 500px">
                <div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%; border:none;" id="alpine_flow_dbSourceUpdater_resourcesTree_container">
                </div>
            </div>
        </div>
		<div class="whiteDialogFooter">
			<button type="button" dojoType="dijit.form.Button" baseClass="primaryButton" onclick="alpine.dbconnection.dbMetadataCacheManager.close()"><fmt:message key="Done"/></button>
		</div>
	</div>
	
	<div id="alpine_flow_dbSourceUpdater_resourcesTree_menu"></div>
</fmt:bundle>
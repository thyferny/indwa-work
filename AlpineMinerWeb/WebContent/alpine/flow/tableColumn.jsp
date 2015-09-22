<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="tableColumnDialog" title="<fmt:message key='table_column_title'/>">
		<div class="titleBar">
            <fmt:message key='table_column_title'/>
        </div>
		<div dojoType="dijit.layout.ContentPane" style="width: 420px; height: 320px">
			<div id="tableColumnGrid"></div>
			
		</div>
		<div class="whiteDialogFooter"> 
			<button type="button" baseClass="primaryButton" dojoType="dijit.form.Button"  align = "right"
								onClick="dijit.byId('tableColumnDialog').hide();">
								<fmt:message key='Done'/></button>
		</div>
	</div>
</fmt:bundle>
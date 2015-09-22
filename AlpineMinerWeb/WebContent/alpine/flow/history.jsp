 
<fmt:bundle basename="app">
 
<div dojoType="alpine.layout.PopupDialog" draggable="false"
		id="flowHistoryDialog" title="<fmt:message key='Flow_History' />">
	 <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 690px; height: 300px">
	 	<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%; height: 80%">
	 		<table id="flowHsitoryTable" dojoType="dojox.grid.DataGrid"
				query="{ flowName: '*' }" clientSort="true" 
	 			 onSelected= "alpine.flow.FlowVersionHistoryUIHelper.select_flow_history" >
				<thead>
					<tr>
						<th width="200px" field="id"><fmt:message key="Flow_Name" /></th>
						<th width="80px" field="version"><fmt:message key="Flow_Version"/></th>
						<th width="120px" field="modifiedTime"><fmt:message key="Modified_Time" /></th>
						<th width="250px" field="comments"><fmt:message key="COMMENTS" /></th>
					</tr>
				</thead>
			</table>
	 	</div>
	 	<div dojoType="dijit.layout.ContentPane" region="bottom" style="width: 100%;" class="whiteDialogFooter">
			<table width="100%" height="100%">
				<tr >
					<td>
						<div dojoType="dijit.form.Button" baseClass="secondaryButton"  id="flow_history_download_id" type="button"
							disabled = true
							onclick="alpine.flow.FlowVersionHistoryUIHelper.perform_download_flow_history()">

							<fmt:message key="Download" />
						</div>
						<div dojoType="dijit.form.Button" baseClass="secondaryButton" id="replace_flow_by_history" type="button"
							disabled = true
							onclick="alpine.flow.FlowVersionHistoryUIHelper.replace_current_flow_by_history()">

							<fmt:message key="Replace_Current" />
						</div>
						<div dojoType="dijit.form.Button" baseClass="primaryButton" type="button"
							onclick="dijit.byId('flowHistoryDialog').hide();">
							<fmt:message key="Done" />
						</div>
					</td>
				</tr>
				<tr>
					<td width = 100%>
						<label witdh="100%" id="down_a_flow_label"> </label>
					</td>
				</tr>
			</table>
	 	</div>
	 </div>
</div>



<div dojoType="alpine.layout.PopupDialog" draggable="false"
		id="flowHistoryDialog_Add" title="<fmt:message key='Flow_History' />">
	 <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 690px; height: 320px">
	 	<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%; height: 90%">
	 		<table id="flowHsitoryTable_Add" dojoType="dojox.grid.DataGrid"
				query="{ flowName: '*' }" clientSort="true" 
	 			 onSelected= "alpine.flow.FlowVersionHistoryUIHelper.select_flow_history_add" >
				<thead>
					<tr>
						<th width="180px" field="id"><fmt:message key="Flow_Name" /></th>
						<th width="60px" field="version"><fmt:message key="Flow_Version"/></th>
						<th width="60px" field="modifiedUser"><fmt:message key="Publisher" /></th>
						<th width="120px" field="modifiedTime"><fmt:message key="Publish_Time" /></th>
						<th width="220px" field="comments"><fmt:message key="COMMENTS" /></th>
					</tr>
				</thead>
			</table>
	 	</div>
	 	<div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter">
             <div dojoType="dijit.form.Button" baseClass="cancelButton"
                  type="button"
                  onclick="dijit.byId('flowHistoryDialog_Add').hide();">
                 <fmt:message key="Cancel"/>
             </div>
             <div dojoType="dijit.form.Button" id="flow_history_open_id" baseClass="primaryButton"
                  type="button"
                  disabled=true
                  onclick="alpine.flow.FlowVersionHistoryUIHelper.perform_flow_history_open_when_add()">
                 <fmt:message key="Open"/>
             </div>

	 	</div>
	 </div>
</div>

 
</fmt:bundle>
 
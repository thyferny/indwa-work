
<script type="text/javascript"
	src="../../js/alpine/props/hiddenlayer.js" charset="utf-8"></script>
<fmt:bundle basename="app">
<div dojoType="dijit.Dialog" draggable="false" height=300 width=400 id="hiddenLayerDialog"
	closeNode="hidehiddenlayer" title='<fmt:message key="Edit_Hidden_Layer" />'>
	<div class="titleBar"><fmt:message key="Edit_Hidden_Layer" /></div>
	<table>
		<tr>
			<td colspan=2 height=260 width=400>
				<fieldset style="margin: auto;border:1px solid #cecdcd; width: 95%; height: 95%; padding-left:2px;">
					<legend style="margin-left:8px;"><fmt:message key='Hidden_Layers'/></legend>
					<table height=220 width=370>
						<tr  >
							<td valign="top" >
								<table id="hiddenLayerTable" dojoType="dojox.grid.DataGrid"
									query="{ layerName: '*' }"   clientSort="true"
								 
									
									style="width: 240px; height: 200px;">
									<thead>
										<tr>
											<th width="50%" field="layerName"><label class="valueLabel"><fmt:message key="Hidden_Layers" /></label></th>
				  
											<th width="50%" field="layerSize" editable="true"><label class="valueLabel"><fmt:message key="Hidden_Layer_Size" /></label></th>
										</tr>
									</thead>
								</table></td>
							<td valign="top"><span dojoType="dijit.form.Button" type="button"    baseClass="workflowButton"
								id="addHidenLayerBtn"> <fmt:message key="Add" />   </span> <span
								dojoType="dijit.form.Button" id="removeHiddenLayerBtn" type="button" baseClass="workflowButton">
									 <fmt:message key="Remove" />  </span></td>
						</tr>

					</table>
				</fieldset>
			</td>
		</tr>
	</table>
	<div class="whiteDialogFooter">
		<button	dojoType="dijit.form.Button" type="button" id="hidehiddenlayer" onclick="close_hidden_layer_dialog();" baseclass="cancelButton"><fmt:message key="Cancel" /> </button>
		<button id="hidden_layer_ok_id" dojoType="dijit.form.Button" type="button" onclick="return update_hidden_layer_data()" baseclass="primaryButton"><fmt:message key="OK" /> </button> 
	</div>
</div>
</fmt:bundle>
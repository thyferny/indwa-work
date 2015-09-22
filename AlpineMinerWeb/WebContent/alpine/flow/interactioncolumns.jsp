
<script type="text/javascript"
	src="../../js/alpine/props/interactioncolumns.js" charset="utf-8"></script>
<fmt:bundle basename="app">
<div dojoType="alpine.layout.PopupDialog" draggable="false" height="400px" id="interactionColumnsDialog"
	closeNode="hideinteractioncolumns" title='<fmt:message key="Edit_Interaction_Columns" />'>
	<table>
		<tr>
			<td colspan=2 height="360px" width="600px">
				<div dojoType="dijit.layout.ContentPane" style="width: 590px; height: 360px"   >
					<fieldset style="border:1px solid #cecdcd; height: 95%; padding-left:2px;">
						<legend style="margin-left:8px;"><fmt:message key='Interaction_Columns'/></legend>
						<table style="height: 320px ;width: 560px ">
							<tr height=200  width="530px">
								<td  width="510px"  height="200px" colspan =2>
									<table id="interactionColumnsTable" dojoType="dojox.grid.DataGrid"
										query="{ firstColumn: '*' }"   clientSort="true"
									
										style="width: 100%;height: 200px">
										<thead>
											<tr>
											<th width="210px" field="firstColumn"    >
											<fmt:message key="First_Column" /></th>
												<th width="100%" field="interactionType"   >
												<fmt:message key="Interaction" /></th>
												<th width="210px" field="secondColumn"   >
												<fmt:message key="Second_Column" /></th>
											</tr>
										</thead>
									</table>
								</td>
							</tr>
							<tr colspan =2 >	<td>
									<table>
									<tr>
										<td>
										 	<label class="valueLabel" for="edit_ic_column1">
												<fmt:message key="First_Column" />
											</label>
										</td>
										<td>
										<select  style="align: left; width: 124px; height: 18px"
													dojoType="dijit.form.Select" id="edit_ic_column1" maxHeight="200"> </select>
										</td>
									</tr>
									<tr >
										<td   valign="bottom">
											<label class="valueLabel" for="edit_ic_interactive">
												<fmt:message key="Interaction" />
											</label>
										</td>
										<td>
											<input   style="align: left; width: 124px; height: 18px"
														dojoType="dijit.form.Select" id="edit_ic_interactive"></input>
										</td>
									</tr>
									<tr >
										<td   valign="bottom">
											<label class="valueLabel" for="edit_ic_column2">
												<fmt:message key="Second_Column" />
											</label>
										</td>
										<td>	
										<select  width= "124px" style="align: left; width: 124px; height: 18px"
													dojoType="dijit.form.Select" id="edit_ic_column2" maxHeight="200"  > </select>
										</td>	
									</tr>
							
									</table>
									</td>
							</tr>
									
							<tr colspan =2>
								<td width= 280  valign="bottom">
									<button dojoType="dijit.form.Button" type="button" baseClass="workflowButton"
										id="addInteractiveColumnBtn"> <fmt:message key="create_button" />   
									</button>
									<button dojoType="dijit.form.Button" id="updateInteractiveColumnBtn" baseClass="workflowButton"
											 disabled="true" type="button">
										 <fmt:message key="update_button" />
									</button>
									<button dojoType="dijit.form.Button" id="removeInteractiveColumnBtn" baseClass="workflowButton" 
											disabled="true" type="button">
										 <fmt:message key="delete_button" />  
									 </button>
								 </td>
							</tr>
						</table>
					</fieldset>
				</div>
			</td>
		</tr>
	</table>
	<div class="whiteDialogFooter">
        <button dojoType="dijit.form.Button" type="button" id="hideinteractioncolumns" onclick="close_ic_dialog();" baseClass="cancelButton"><fmt:message key="Cancel" /> </button>
        <button id="interactin_columns_ok_id" dojoType="dijit.form.Button" type="button" onclick="return update_interactive_columns_data()" baseClass="primaryButton"><fmt:message key="OK" /> </button>
	</div>
</div>
</fmt:bundle>
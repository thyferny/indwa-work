
<script type="text/javascript" src="../../js/alpine/props/storageparameters.js"></script>

<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" id="props_storageParameter_dialog" draggable="false" title="<fmt:message key='output_creation_param_title'/>">
        <div class="titleBar">
            <fmt:message key='output_creation_param_title'/>
        </div>
		<!--<div dojoType="dijit.layout.BorderContainer" style="width: 450px; height: 300px">-->
			<div dojoType="dijit.layout.ContentPane"  style="width: 500px; height: 300px; padding: 10px;" region="center">
				<div dojoType="dijit.layout.StackContainer" id="props_storageParameter_database_section">
					<div dojoType="dijit.layout.ContentPane" id="props_storageParameter_database_section_PostgreSQL">
						<table width="100%" cellspacing="5">
							<tr>
								<td>
									<b><fmt:message key="output_creation_param_Append_Only"/>  </b>
								</td>
								<td>
									<input dojoType="dijit.form.RadioButton" name="appendOnly" id="props_storageParameter_pg_appendOnly_y"/><fmt:message key="Yes"/>
									<input dojoType="dijit.form.RadioButton" name="appendOnly" id="props_storageParameter_pg_appendOnly_n" checked="true"/><fmt:message key="No"/>
								</td>
							</tr>
							<tr>
								<td>
                                    <b><fmt:message key="output_creation_param_Columnar_Storage"/> </b>
								</td>
								<td>
									<input dojoType="dijit.form.RadioButton" name="columnarStorage" id="props_storageParameter_pg_columnarStorage_y" disabled="true"/><fmt:message key="Yes"/>
									<input dojoType="dijit.form.RadioButton" name="columnarStorage" id="props_storageParameter_pg_columnarStorage_n" checked="true" disabled="true"/><fmt:message key="No"/>
								</td>
							</tr>
							<tr>
								<td>
                                    <b><fmt:message key="output_creation_param_Compression"/> </b>
								</td>
								<td>
									<input dojoType="dijit.form.RadioButton" name="compression" id="props_storageParameter_pg_compression_y" disabled="true"/><fmt:message key="Yes"/>
									<input dojoType="dijit.form.RadioButton" name="compression" id="props_storageParameter_pg_compression_n" checked="true" disabled="true"/><fmt:message key="No"/>
									<fmt:message key="output_creation_param_Compression_Level"/>
									<select dojoType="dijit.form.Select" id="props_storageParameter_pg_compression_level" disabled="true"
                                            baseClass="greyDropdownButton" style="width:auto;">
										<option value="1">1</option>
										<option value="2">2</option>
										<option value="3">3</option>
										<option value="4">4</option>
										<option value="5">5</option>
										<option value="6">6</option>
										<option value="7">7</option>
										<option value="8">8</option>
										<option value="9">9</option>
									</select>
								</td>
							</tr>
							<tr>
								<td>
                                    <b><fmt:message key="output_creation_param_Destribution"/>  </b>
								</td>
								<td>
									<input dojoType="dijit.form.RadioButton" name="distribution" id="props_storageParameter_pg_distribution_random" checked="true"></input><fmt:message key="output_creation_param_Distributed_randomly"/>
								</td>
							</tr>
							<tr>
								<td>
									
								</td>
								<td>
									<input dojoType="dijit.form.RadioButton" name="distribution" id="props_storageParameter_pg_distribution_assigment"></input><fmt:message key="output_creation_param_Distributed_by_columns"/>
								</td>
							</tr>
							<tr>
								<td>
									
								</td>
								<td>
									<input dojoType="dijit.form.ValidationTextBox" id="props_storageParameter_pg_distribution_assigment_columns" required="true" disabled="true">
								</td>
							</tr>
						</table>
					</div>
					<div dojoType="dijit.layout.ContentPane" id="props_storageParameter_database_section_Greenplum">
						
					</div>
					<div dojoType="dijit.layout.ContentPane" id="props_storageParameter_database_section_DB2">
						
					</div>
					<div dojoType="dijit.layout.ContentPane" id="props_storageParameter_database_section_Netezza">
						
					</div>
					<div dojoType="dijit.layout.ContentPane" id="props_storageParameter_database_section_Oracle">
						
					</div>
				</div>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="bottom">
                <div class="whiteDialogFooter">
                    <button type="Button" baseClass="cancelButton" dojoType="dijit.form.Button" id="props_storageParameter_button_cancel"><fmt:message key="Cancel"/></button>
                    <button type="Button" baseClass="primaryButton" dojoType="dijit.form.Button" id="props_storageParameter_button_save"><fmt:message key="OK"/></button>
                </div>
			</div>
		<!--</div>-->
	</div>
</fmt:bundle>
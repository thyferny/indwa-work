<%@ page pageEncoding="UTF-8"%>


<fmt:bundle basename="app">

<script type="text/javascript">
var prefNameMap=new Array(); 
prefNameMap["alg"] = "<fmt:message key='Algorithm_Profiles'/>" ;
prefNameMap["db"] =  "<fmt:message key='Database_Profiles'/>" ; 
prefNameMap["ui"] = "<fmt:message key='UI_Profiles'/>" ;
prefNameMap["sys"] = "<fmt:message key='System_Profiles'/>" ;
dojo.require("alpine.system.PreferenceHelper");
	
</script>

<div dojoType="dijit.Dialog" draggable="false" id="preferenceEditDialog" 
			title="<fmt:message key='preference_edit_title'/>" >
			
			<div dojoType="dijit.layout.BorderContainer" 
				style="width: 590px; height: 320px;" 
				id="perfborderContainer">
						<div dojoType="dijit.layout.ContentPane" region="top">
							<div class="titleBar">
					            <fmt:message key='preference_edit_title'/>
					        </div>
						</div>
			 		 	<div dojoType="dijit.layout.ContentPane" region="leading" style="width: 30%;">
			 				<div dojoType="dijit.layout.AccordionContainer" minSize="20"
								style="width: 340px;" id="prefleftConnAccordion" region="leading"
								splitter="true">
								<div dojoType="dijit.layout.ContentPane"
									title='<fmt:message key="system_preference" />'
									  id="Preference_Tree_Pane"></div>
							</div>
					 	</div>
					 	<div dojoType="dijit.layout.ContentPane" region="center">
							<div id="preference_container" dojoType="dijit.layout.StackContainer"
							height ="100%" width = "100%" region="top" 
							tabPosition="top" tabStrip="false">     
			        	
			        			<div width="100%" height="100%" dojoType="dijit.layout.ContentPane" 
			        				id="preferenceEditPanel_alg"  
			        				title="<fmt:message key='Algorithm_Profiles'/>"> 
									<table>
										<tr valign = "top">
											<td ><label class="valueLabel" for="distinct_value_count">
											<fmt:message key="Distinct_Value_Count" /> </label></td><td style="width:4px"></td>
											
											<td><input style="width:100px" dojoType="dijit.form.ValidationTextBox"
											required="true" trim="true" type="text" regExp="^[0-9]*$"
											name="distinct_value_count" id="distinct_value_count"
											invalidMessage="<fmt:message key="NumberTextValid"/>" />
											</td>
										</tr>
										<tr>
											<td  ><label class="valueLabel" for="va_distinct_value_count">
											<fmt:message key="Value_Analysis_Distinct_Value_Count" /> </label></td><td style="width:4px"></td>
											<td>
											<input  style="width:100px" dojoType="dijit.form.ValidationTextBox"
											required="true" trim="true" type="text" regExp="^[0-9]*$"
											name="va_distinct_value_count" id="va_distinct_value_count"
											invalidMessage="<fmt:message key="NumberTextValid"/>" />
			 
											</td>
										</tr>
										<tr>
											<td  ><label class="valueLabel" for="decimal_precision">
											<fmt:message key="Decimal_precision_digits" /> </label></td><td style="width:4px"></td>
											<td><select 
											id="decimal_precision" name="decimal_precision">
											    <option value ="3">     3 	    </option>
											    <option value ="4">     4 	    </option>
											    <option value ="5">     5 	    </option>
											    <option value ="6">     6 	    </option>
											    <option value ="7">     7 	    </option>
											</select>
											</td>
										</tr>
						 
										 
									</table>
								<button id="preference_alg_save_data_btn"    dojoType="dijit.form.Button" baseClass="workflowButton"  type="button">
									<fmt:message key="SAVE" /> 
								 </button>
								<button id="preference_alg_restore_data_btn"  dojoType="dijit.form.Button" baseClass="workflowButton"  type="button">
								    <fmt:message key="Restore" />
								</button>  
								</div>
								
								<div width="100%" dojoType="dijit.layout.ContentPane" 
			        				id="preferenceEditPanel_sys" title="<fmt:message key='System_Profiles'/>"> 
									<table  >
				 
										<tr valign = "top">
											<td  ><label class="valueLabel" for="debug_level">
											<fmt:message key="debug_level" /> </label></td><td style="width:4px"></td>
											<td><select  style="width:100px" 
											id="debug_level" name="debug_level">
											    <option value="Info">     Info 	    </option>
											    <option value ="Debug">     Debug	    </option>
											   
											</select>
											</td>
										</tr>
									</table>
									<button id="preference_sys_save_data_btn" dojoType="dijit.form.Button"  baseClass="workflowButton" type="button">
									<fmt:message key="SAVE" /> 
									</button>
									<button id="preference_sys_restore_data_btn"  dojoType="dijit.form.Button" baseClass="workflowButton"  type="button">
									 <fmt:message key="Restore" />
									 </button>  
								</div>
								
								<div width="100%" height="100%" dojoType="dijit.layout.ContentPane" 
									 id="preferenceEditPanel_db"  
									title="<fmt:message key='Database_Profiles'/>"> 
								 <table   >
										<tr valign = "top"><td>
							 		<label class="valueLabel" for="connection_timeout">
							 			<fmt:message key="Database_connection_timeout" /> </label></td><td style="width:4px"> 
							 		</td><td>
							 		<input style="width:100px" dojoType="dijit.form.ValidationTextBox" 
											required="true" trim="true" type="text" regExp="^[0-9]*$"
											name="connection_timeout" id="connection_timeout"
											invalidMessage="<fmt:message key="NumberTextValid"/>" />
											
					  			</td></tr>
					  			
										<tr>
											<td><label class="valueLabel" for="add_outputtable_prefix">
											<fmt:message key="Add_output_table_prefix" /></label></td><td style="width:4px"></td>
											<td width="100px"><input  dojoType ="dijit.form.CheckBox"    
												 name="add_outputtable_prefix" 
												 id="add_outputtable_prefix"  />
											</td>
										</tr>
										<tr>
											<td  ><label class="valueLabel" for="hd_local_data_size_threshold">
											<fmt:message key="hadoop_local_data_size_threshold" /> </label></td><td style="width:4px"></td>
											<td>
											<input  style="width:70px" dojoType="dijit.form.ValidationTextBox"
											required="true" trim="true" type="text" regExp="^[\d]*$"
											id="hd_local_data_size_threshold"
											invalidMessage="<fmt:message key="NumberTextValid"/>" />MB
											</td>
										</tr>
										</table>
									 
								 <button id="preference_db_save_data_btn" dojoType="dijit.form.Button"  type="button" baseClass="workflowButton">	<fmt:message key="SAVE" /> </button>
								<button id="preference_db_restore_data_btn"  dojoType="dijit.form.Button"  type="button" baseClass="workflowButton">
												<fmt:message key="Restore" /></button>  
											 
								</div>
							
								<div width="100%" height="100%" dojoType="dijit.layout.ContentPane" 
									id="preferenceEditPanel_ui"   
									title="<fmt:message key='UI_Profiles'/>"> 
								 <table   >
										<tr valign = "top">
											<td  ><label class="valueLabel" for="max_table_lines">
											<fmt:message key="Show_max_lines" /> </label></td><td style="width:4px"></td>
											<td width="100px">
											 
										<input style="width:100px"  dojoType="dijit.form.NumberTextBox"                            
			                            constraints="{max:2000,min:10}"
			                            name="max_table_lines"
			                            id="max_table_lines" rangeMessage="<fmt:message key="NumberTextValid"/>:[10,2000]"
			                            required="true"/>
								 
											</td>
										</tr>
										
										
												<tr valign = "top">
											<td  ><label  class="valueLabel" for="max_scatter_points">
											<fmt:message key="max_scatter_points" /> </label></td><td style="width:4px"></td>
											<td width="100px">
											
														<input style="width:100px" dojoType="dijit.form.NumberTextBox"                            
			                            constraints="{max:200,min:20}"
			                            name="max_scatter_points"
			                            id="max_scatter_points"
			                            rangeMessage="<fmt:message key="NumberTextValid"/>:[20,200]"
			                            required="true"/>
								 
											</td>
										</tr>
											<tr valign = "top" style="display: none;">
											<td  ><label class="valueLabel" for="max_timeseries_points">
											<fmt:message key="max_timeseries_points" /> </label></td><td style="width:4px"></td>
											<td width="100px">
											
										<input style="width:100px" dojoType="dijit.form.NumberTextBox"                            
			                            constraints="{max:100,min:20}"
			                            name="max_timeseries_points"
			                            id="max_timeseries_points"
			                            rangeMessage="<fmt:message key="NumberTextValid"/>:[20,100]"
			                            required="true"/>
								 
											</td>
										</tr>
										
														<tr valign = "top">
											<td  ><label class="valueLabel" for="max_cluster_points">
											<fmt:message key="max_cluster_points" /> </label></td><td style="width:4px"></td>
											<td width="100px">
											
											  <input style="width:100px" dojoType="dijit.form.NumberTextBox"                            
							                            constraints="{max:100,min:20}"
							                            name="max_cluster_points"
							                            id="max_cluster_points"
							                            rangeMessage="<fmt:message key="NumberTextValid"/>:[20,100]"
							                            required="true"/>
											</td>
										</tr>
										<tr><td></td></tr>
							  				 
									</table>
								<button id="preference_ui_save_data_btn"  dojoType="dijit.form.Button" baseClass="workflowButton"  type="button">	<fmt:message key="SAVE" /> </button>
								<button id="preference_ui_restore_data_btn"  dojoType="dijit.form.Button"  baseClass="workflowButton" type="button"><fmt:message key="Restore" /></button>
						 
								</div>
							</div>
					 	</div>
		 		</div>
            <div class="whiteDialogFooter">
                <button id="perference_dlg_done_button" dojoType="dijit.form.Button" baseClass="primaryButton" type="button">
                    <fmt:message key="Done" />
                </button>
            </div>
</div>
</fmt:bundle>
<script type="text/javascript">
<!--
	dojo.require("alpine.flow.SchedulerManagerUIHelper");
//-->
</script>
<fmt:bundle basename="app">
	<div id="alpine_flow_scheduler_dialog" draggable="false" dojoType="dijit.Dialog" title="<fmt:message key="scheduler_edit_dialog_title"/>">
		<div dojoType="dijit.layout.LayoutContainer" style="width: 660px;height: 600px">
			<div dojoType="dijit.layout.ContentPane" region="top">
				<div class="titleBar">
		            <fmt:message key='scheduler_edit_dialog_title'/>
		        </div>
			</div>
            <div class="innerPadding">
                <div dojoType="dijit.layout.BorderContainer" region="center">
                	<div dojoType="dijit.layout.ContentPane" region="left" style="width: 35%;">
                		<table width="100%">
                			<tr>
                				<td>
                					<fmt:message key="scheduler_edit_title_task_list"/>
                				</td>
                				<td align="right">
                					<a href="#" id="alpine_flow_scheduler_addTaskBtn"><fmt:message key="scheduler_edit_button_add"/></a>
                				</td>
                			</tr>
                		</table>
             			<div id="alpine_flow_scheduler_task_dataGrid" style="height: 90%; background: #FFFFFF"></div>
                	</div>
                	<div dojoType="dijit.layout.ContentPane" region="center" style="width: 65%">
                		<div dojoType="dijit.form.Form" id="alpine_flow_scheduler_editor_form">
	                		<table width="100%" height="100%">
	                			<tr height="20px">
	                				<td>
	                					<input dojoType="dijit.form.ValidationTextBox" baseClass="alpineImportTextbox largeSubtitle" id="alpine_flow_scheduler_editor_task_name" regExp="^[\w]{1,20}$" placeHolder="<fmt:message key='scheduler_edit_task_name_tip'/>" required="true" style="width: 300px">
	                				</td>
	                			</tr>
	                            <tr height="20px">
	                                <td valign="top">
	                                	<div id="alpine_flow_scheduler_editor_workflowSelectorPane" dojoType="dijit.TitlePane">
	                                		<table width="100%" height="100%">
	                                			<tr>
					                                <td width="40%">
					                                    <select id="alpine_flow_scheduler_editor_flow" style="width:100%;" multiple size="15"></select>
					                                </td>
					                                <td width="20%" align="center">
					                                    <button type="button" baseClass="workflowButton" id="alpine_flow_scheduler_editor_flow_moveup" dojoType="dijit.form.Button">^</button>
					                                    <br/>
					                                    <button type="button" baseClass="workflowButton" id="alpine_flow_scheduler_editor_flow_moveleft" dojoType="dijit.form.Button">&lt;</button>
					                                    <br>
					                                    <br>
					                                    <button type="button" baseClass="workflowButton" id="alpine_flow_scheduler_editor_flow_moveright" dojoType="dijit.form.Button">&gt;</button>
					                                    <br/>
					                                    <button type="button" baseClass="workflowButton" id="alpine_flow_scheduler_editor_flow_movedown" dojoType="dijit.form.Button">v</button>
					                                </td>
					                                <td width="40%">
					                                    <select id="alpine_flow_scheduler_editor_flow_orignal" style="width:100%;" multiple size="15"></select>
					                                </td>
	                                			</tr>
	                                		</table>
	                                	</div>
	                                </td>
	                            </tr>
	                            <tr height="20px">
	                            	<td>
	                            		<div style="float: left;"><fmt:message key="scheduler_edit_title_scheduler"/></div>
	                            		<div id="alpine_flow_scheduler_editor_status_area" style="display: none;">
	                            			&nbsp;<fmt:message key="scheduler_edit_title_schedule_disabled"/>&emsp;
	                            			<%--<a id="alpine_flow_scheduler_editor_status_switch" href="#">Turn schedule on</a> --%>
	                            		</div>
	                            	</td>
	                            </tr>
	                            <tr>
	                            	<td valign="top" align="right">
	                            		<table width="95%">
	                            			<tr height="25px">
	                            				<td nowrap>
	                            					<fmt:message key="scheduler_edit_starttime"/>
	                            				</td>
	                            				<td>
	                            					<input id="alpine_flow_scheduler_editor_execute_startdate" 
	                            							placeHolder="<fmt:message key='scheduler_edit_starttime_placeholder'/>" 
	                            							baseClass="greyDropdownButton" 
	                            							dojoType="dijit.form.DateTextBox" 
	                            							style="width: 120px"/>
	                            				</td>
	                            			</tr>
	                            			<tr height="25px">
	                            				<td nowrap>
	                            					<fmt:message key="scheduler_edit_repeat_type"/>
	                            				</td>
	                            				<td>
	                            					<select id="alpine_flow_scheduler_editor_repeat_type" dojoType="dijit.form.Select" style="width: 60px; float: left;" baseClass="greyDropdownButton">
	                            						<option value="interval"><fmt:message key="scheduler_edit_repeat_option_interval"/></option>
	                            						<option value="cron-day"><fmt:message key="scheduler_edit_repeat_option_daily"/></option>
	                            						<option value="cron-week"><fmt:message key="scheduler_edit_repeat_option_weekly"/></option>
	                            						<option value="cron-month"><fmt:message key="scheduler_edit_repeat_option_monthly"/></option>
	                            					</select>
	                            					<div id="alpine_flow_scheduler_editor_repeat_interval_area">
	                            						&nbsp;<fmt:message key="scheduler_edit_repeat_interval_every"/>
	                            						<input dojoType="dijit.form.NumberTextBox" constraints="{min: 1, places: 0}"  id="alpine_flow_scheduler_editor_repeat_interval_val" style="width: 50px" required="true" baseClass="inlineTextbox">
		                            					<select id="alpine_flow_scheduler_editor_repeat_interval_unit" dojoType="dijit.form.Select" style="width: 60px" baseClass="greyDropdownButton">
		                            						<option value="MINUTE"><fmt:message key="scheduler_edit_repeat_interval_unit_minutes"/></option>
		                            						<option value="HOUR"><fmt:message key="scheduler_edit_repeat_interval_unit_hours"/></option>
		                            						<option value="DAY"><fmt:message key="scheduler_edit_repeat_interval_unit_days"/></option>
		                            						<option value="WEEK"><fmt:message key="scheduler_edit_repeat_interval_unit_weeks"/></option>
		                            					</select>
	                            					</div>
	                            					<div id="alpine_flow_scheduler_editor_repeat_scheduler_area" style="display: none;">
	                            						&nbsp;<fmt:message key="scheduler_edit_repeat_schedule_at"/>
		                            					<input dojoType="dijit.form.NumberTextBox" 
		                            							constraints="{min: 1,max: 31,places: 0}" 
		                            							required="true"
		                            							id="alpine_flow_scheduler_editor_repeat_scheduler_month" 
		                            							class="cron-interval cron-monthly" 
		                            							style="width: 80px; 
		                            							display:none"  
		                            							baseClass="inlineTextbox"
	                            								placeHolder="<fmt:message key='scheduler_edit_repeat_schedule_monthly_placeholder'/>" >
		                                                <select id="alpine_flow_scheduler_editor_repeat_scheduler_week" style="width: 60px; display:none;" dojoType="dijit.form.Select" class="cron-interval cron-weekly" baseClass="greyDropdownButton">
		                                                    <option value="2"><fmt:message key="scheduler_edit_repeat_schedule_week_monday"/></option>
		                                                    <option value="3"><fmt:message key="scheduler_edit_repeat_schedule_week_tuesday"/></option>
		                                                    <option value="4"><fmt:message key="scheduler_edit_repeat_schedule_week_wednesday"/></option>
		                                                    <option value="5"><fmt:message key="scheduler_edit_repeat_schedule_week_Thursday"/></option>
		                                                    <option value="6"><fmt:message key="scheduler_edit_repeat_schedule_week_Friday"/></option>
		                                                    <option value="7"><fmt:message key="scheduler_edit_repeat_schedule_week_Saturday"/></option>
		                                                    <option value="1"><fmt:message key="scheduler_edit_repeat_schedule_week_Sunday"/></option>
		                                                </select>
		                            					<select id="alpine_flow_scheduler_editor_repeat_scheduler_time" dojoType="dijit.form.FilteringSelect" style="width: 70px" class="cron-interval cron-daily cron-weekly cron-monthly" baseClass="greyDropdownButton">
		                            						<option value="1">1 AM</option>
		                            						<option value="2">2 AM</option>
		                            						<option value="3">3 AM</option>
		                            						<option value="4">4 AM</option>
		                            						<option value="5">5 AM</option>
		                            						<option value="6">6 AM</option>
		                            						<option value="7">7 AM</option>
		                            						<option value="8">8 AM</option>
		                            						<option value="9">9 AM</option>
		                            						<option value="10">10AM</option>
		                            						<option value="11">11AM</option>
		                            						<option value="12">12PM</option>
		                            						<option value="13">1 PM</option>
		                            						<option value="14">2 PM</option>
		                            						<option value="15">3 PM</option>
		                            						<option value="16">4 PM</option>
		                            						<option value="17">5 PM</option>
		                            						<option value="18">6 PM</option>
		                            						<option value="19">7 PM</option>
		                            						<option value="20">8 PM</option>
		                            						<option value="21">9 PM</option>
		                            						<option value="22">10PM</option>
		                            						<option value="23">11PM</option>
		                            						<option value="0">12AM</option>
		                            					</select>
	                            					</div>
	                            				</td>
	                            			</tr>
	                            			<tr height="25px">
	                            				<td rowspan="3" valign="top" nowrap>
	                            					<fmt:message key="scheduler_edit_endtime"/>
	                            				</td>
	                            				<td>
	                            					<input dojoType="dijit.form.RadioButton" id="alpine_flow_scheduler_editor_terminal_never" name="terminateCond" checked="true">
	                            					<label for="alpine_flow_scheduler_editor_terminal_never"><fmt:message key="scheduler_edit_endtime_option_never"/></label>
	                            				</td>
	                            			</tr>
	                            			<tr height="25px">
	                            				<td>
	                            					<input dojoType="dijit.form.RadioButton" id="alpine_flow_scheduler_editor_terminal_times" name="terminateCond">
	                            					<label for="alpine_flow_scheduler_editor_terminal_times"><fmt:message key="scheduler_edit_endtime_option_times_after"/> </label>
	                            					<input dojoType="dijit.form.NumberTextBox" disabled constraints="{min: 1, places: 0}" required="true" id="alpine_flow_scheduler_editor_terminal_times_val" style="width: 50px" baseClass="inlineTextbox">
	                            					<label for="alpine_flow_scheduler_editor_terminal_times"> <fmt:message key="scheduler_edit_endtime_option_times_occurrences"/></label>
	                            				</td>
	                            			</tr>
	                            			<tr height="25px">
	                            				<td>
	                            					<input dojoType="dijit.form.RadioButton" id="alpine_flow_scheduler_editor_terminal_date" name="terminateCond">
	                            					<label for="alpine_flow_scheduler_editor_terminal_date"><fmt:message key="scheduler_edit_endtime_option_date_on"/>&nbsp;&nbsp;</label>
	                            					<input id="alpine_flow_scheduler_editor_terminal_date_val" 
	                            							placeHolder="<fmt:message key='scheduler_edit_endtime_placeholder'/>" 
	                            						 	required="true"
	                            							baseClass="greyDropdownButton" 
	                            							dojoType="dijit.form.DateTextBox" 
	                            							style="width: 120px"
	                            							disabled/>
	                            				</td>
	                            			</tr>
	                            		</table>
	                            	</td>
	                            </tr>
	                        </table>
                        </div>
                	</div>
                </div>
            </div>
			<div dojoType="dijit.layout.ContentPane" region="bottom">
				<div class="whiteDialogFooter">
					<button dojoType="dijit.form.Button" type="button" baseClass="cancelButton" onClick="dijit.byId('alpine_flow_scheduler_dialog').hide()"><fmt:message key="Cancel"/></button>
					<button dojoType="dijit.form.Button" type="button" baseClass="primaryButton" id="alpine_flow_scheduler_btn_submit"><fmt:message key="OK"/></button>
				</div>
			</div>
		</div>
	</div>
</fmt:bundle>
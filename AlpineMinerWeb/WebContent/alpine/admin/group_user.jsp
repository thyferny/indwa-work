	<script type="text/javascript">
       dojo.require("alpine.system.ProfileHelper");
       dojo.require("alpine.system.UserGroupManagementUIHelper");
	</script>
	
		<div dojoType="dijit.Dialog" draggable="false" id="groupUserEditDialog"
			title="<fmt:message key='group_user_edit_title'/>">
		<div class="titleBar">
            <fmt:message key='group_user_edit_title'/>
        </div>
		<div dojoType="dijit.layout.ContentPane" 
			style="width: 680px; height: 620px;">
			<div dojoType="dijit.layout.TabContainer" id="sysmain"
				style="width: 100%; height: 93%;">
				<div dojoType="dijit.layout.ContentPane" title="<fmt:message key="admin_user" />">
					<div dojoType="dijit.layout.LayoutContainer">
						<div dojoType="dijit.layout.ContentPane" region="top">
                            <div style="float: left;"><img src="<%=path%>/images/user-icon.png" width="40" height="40" border="1" id="group" /> </div>
							<div style="float: left;vertical-align:top;margin-top:10px;"><label style="font-size: 18pt;">
								<fmt:message key="admin_user" />
							</label>   </div>
						</div>
	
						<div dojoType="dijit.layout.ContentPane" id="usergridpane"
							region="left" style="width: 40%; height: 100%"></div>
	
						<div dojoType="dijit.layout.ContentPane" id="userformpane"
							region="center" style="width: 60%; height: 100%">
	
							<div dojoType="dijit.form.Form" id="userForm" jsId="userForm" action="" method="" style="overflow:auto;">
	
								<table cellspacing="6" width="100%" height="100%">
									<tr>
										<td width="25%" align="right" nowrap><label class="valueLabel" for="login">
												<fmt:message key="user_name" /></label>
										</td>
										<td><input type="text" id="user_login" name="login"
											onClick="alpine.system.UserGroupManagementUIHelper.enable_user_create()" required="true" trim="true"
											dojoType="dijit.form.ValidationTextBox"  
											data-dojo-props='regExp:"[\\w]+"' 
											/>
										</td>
									</tr>
									<tr>
										<td width="25%" align="right" nowrap><label class="valueLabel" for="email">
												<fmt:message key="user_email" /></label>
										</td>
										<td><input type="email" id="user_email" name="email"
											required="true" trim="true"
											dojoType="dijit.form.ValidationTextBox" 
											regExpGen="dojox.validate.regexp.emailAddress"/>
										</td>
									</tr>
									<tr>
										<td width="25%" align="right" nowrap><label class="valueLabel" for="password"><fmt:message
													key="user_password" />
										</label>
										</td>
										<td><input dojoType="dijit.form.ValidationTextBox"
											required="true" trim="true" type="password" name="password"
											id="user_password" pwType="new">
										</td>
									</tr>
									<tr>
										<td width="25%" align="right" nowrap><label class="valueLabel" for="password"><fmt:message
													key="user_password2" />
										</label>
										</td>
										<td><input dojoType="dijit.form.ValidationTextBox"
											pwType="verify" required="true" trim="true" type="password"
											id="user_password2">
										</td>
									</tr>
									<tr>
										<td width="25%" align="right" nowrap><label class="valueLabel" for="firstName"><fmt:message
													key="user_first" />
										</label>
										</td>
										<td><input dojoType="dijit.form.ValidationTextBox"
											trim="true" type="text" name="firstName" id="user_first">
										</td>
									</tr>
									<tr>
										<td width="25%" align="right" nowrap><label class="valueLabel" for="lastName"><fmt:message
													key="user_last" />
										</label>
										</td>
										<td><input dojoType="dijit.form.ValidationTextBox"
											trim="true" type="text" name="lastName" id="user_last">
										</td>
									</tr>
                                    <tr>
                                        <td width="25%" align="right" nowrap><label class="valueLabel" for="roles"><fmt:message
                                                key="user_roles" />
                                        </label>
                                        </td>
                                        <td>
                                            <input id="roles_admin" name="roles_admin_ck" dojoType="dijit.form.CheckBox" >
                                            <label for="roles_admin"><fmt:message key='roles_admin'/></label>

                                            <input id="roles_modeler" name="roles_modeler_ck" dojoType="dijit.form.CheckBox" >
                                            <label for="roles_modeler"><fmt:message key='roles_modeler'/></label>
                                        </td>
                                    </tr>
									<tr>
										<td width="25%" align="right" nowrap><label class="valueLabel" for="groups"><fmt:message
													key="user_groups" />
										</label>
										</td>
										<td>
	
											<div id="dynamicGroupSel" name="groups"
												dojoType="dijit.form.MultiSelect"></div></td>
									</tr>
									<tr><td></td>
									<td>
									<input id="user_notify" name="user_notify_ck" dojoType="dijit.form.CheckBox" >
									<label for="user_notify_ck"><fmt:message key="receive_notification" /></label>
										
									</td>
									</tr>
									<tr>
										<td width="25%" align="right"><label class="valueLabel" for="description">
												<fmt:message key="user_desc" /></label>
										</td>
										<td><textarea id="user_desc" name="description"
												dojoType="dijit.form.SimpleTextarea" rows="2" cols="24"
												style="width: auto;"></textarea>
										</td>
									</tr>
									<tr>
										<td width="25%"></td>
										<td align="left" colspan="2">
											<button dojoType="dijit.form.Button" type="button"  baseClass="workflowButton"
												id="user_create_button" jsId="user_create_button"
												onClick="alpine.system.UserGroupManagementUIHelper.create_user()">
												<fmt:message key="create_button" />
											</button>
											<button dojoType="dijit.form.Button" type="button"    baseClass="workflowButton"
												id="user_update_button" jsId="user_update_button"
												onClick="alpine.system.UserGroupManagementUIHelper.update_user()">
												<fmt:message key="update_button" />
											</button>
											<button dojoType="dijit.form.Button" id="user_delete_button"  baseClass="workflowButton"
												jsId="user_delete_button" onClick="alpine.system.UserGroupManagementUIHelper.delete_user()"
												type="reset">
												<fmt:message key="delete_button" />
											</button></td>
									</tr>
								</table>
							</div>
						</div>
					</div>
				</div>
				
				<div dojoType="dijit.layout.ContentPane" 
					title="<fmt:message key="admin_group" />" style="width: 100%; height: 100%">
					<div dojoType="dijit.layout.LayoutContainer">
	
						<div dojoType="dijit.layout.ContentPane" region="top" style="height: 45px">

                            <div style="float: left;"><img
										src="<%=path%>/images/user-group-icon.png" width="40"
										height="40" border="1" id="groupicon" /> </div>
                            <div style="float: left;vertical-align:top;margin-top:10px;"><label style="font-size: 18pt;">
											<fmt:message key="admin_group" />
										</label></div>
						</div>
	
						<div dojoType="dijit.layout.ContentPane" id="groupgridpane"
							region="center" style="width: 40%"></div>
	
						<div dojoType="dijit.layout.ContentPane" id="groupformpane"
							region="right" style="width: 60%">
	
							<div dojoType="dijit.form.Form" id="groupForm" jsId="groupForm"
								encType="multipart/form-data" action="" method="">
	
								<table cellspacing="10" width="100%">
									<tr>
										<td width="25%" align="right"><label class="valueLabel" for="id"> <fmt:message
													key="group_name" /></label>
										</td>
										<td><input type="text" id="group_name" name="id"
											required="true" trim="true" 
											data-dojo-props='regExp:"[\\w|\\s]+"' 
											dojoType="dijit.form.ValidationTextBox"
											onClick="alpine.system.UserGroupManagementUIHelper.enable_group_create()" />
										</td>
									</tr>
									<tr>
										<td width="25%" align="right"><label class="valueLabel" for="description">
												<fmt:message key="group_desc" /></label>
										</td>
										<td><textarea id="group_desc" name="description"
												dojoType="dijit.form.SimpleTextarea" rows="4" cols="24"
												style="width: auto;"></textarea>
										</td>
									</tr>
									<tr>
										<td width="25%"></td>
										<td align="left" colspan="2">
											<button dojoType="dijit.form.Button" type="button"  baseClass="workflowButton"
												id="group_create_button" jsId="group_create_button"
												onClick="alpine.system.UserGroupManagementUIHelper.create_group()">
												<fmt:message key="create_button" />
											</button>
											<button dojoType="dijit.form.Button" type="button"  baseClass="workflowButton"
												id="group_update_button" jsId="group_update_button"
												onClick="alpine.system.UserGroupManagementUIHelper.update_group()">
												<fmt:message key="update_button" />
											</button>
											<button dojoType="dijit.form.Button" id="group_delete_button" baseClass="workflowButton"
												jsId="group_delete_button" onClick="alpine.system.UserGroupManagementUIHelper.delete_group()"
												type="reset">
												<fmt:message key="delete_button" />
											</button></td>
									</tr>
								</table>
							</div>
						</div>
					</div>
	
				</div>
				</div>
			
				<div dojoType="dijit.layout.ContentPane" region="bottom">
                    <div class="whiteDialogFooter">
								<button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
									onClick="dijit.byId('groupUserEditDialog').hide();">
									<fmt:message key="Done" />
								</button></td>
                        </div>
				</div>

		</div>
		</div>

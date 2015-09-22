<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<fmt:bundle basename="app">
	<script type="text/javascript">
       dojo.require("alpine.system.ProfileHelper");
       dojo.require("alpine.system.UserGroupManagementUIHelper");
	</script>
	

		<div dojoType="dijit.Dialog" draggable="false" id="reset_pass_dlg"
			title="<fmt:message key='reset_password_title'/>">
			<div class="titleBar">
	            <fmt:message key='reset_password_title'/>
	        </div>
			<div dojoType="dijit.layout.ContentPane" id="user_profileformpane"
				layoutAlign="right" style="width: 400px; height: 100%" scrolling="auto">

				<div dojoType="dijit.form.Form" id="user_profileForm" jsId="user_profileForm"
					encType="multipart/form-data" action="" method=""
					style="width: 100%; display: block;">

					<table cellspacing="6" width="100%">
						<tr>
							<td width="25%" align="right"><label class="valueLabel" for="login"> <fmt:message
										key="user_name" />
							</label></td>
							<td><input type="text" id="user_profile_login" name="login"
								disabled
								dojoType="dijit.form.ValidationTextBox" /></td>
						</tr>
						
						<tr>
							<td width="25%" align="right"><label class="valueLabel" for="password"><fmt:message
										key="user_password" /> </label></td>
							<td><input dojoType="dijit.form.ValidationTextBox"
								required="true" trim="true" type="password" name="password"
								id="user_profile_password" pwType="new"></td>
						</tr>
						<tr>
							<td width="25%" align="right"><label class="valueLabel" for="password"><fmt:message
										key="user_password2" /> </label></td>
							<td><input dojoType="dijit.form.ValidationTextBox"
								pwType="verify" required="true" trim="true" type="password"
								id="user_profile_password2"></td>
						</tr>
						<tr>
							<td width="25%" align="right"><label class="valueLabel" for="email"> <fmt:message
										key="user_email" />
							</label></td>
							<td><input type="email" id="user_profile_email" name="email"
								required="true" trim="true"
								dojoType="dijit.form.ValidationTextBox"
								regExpGen="dojox.validate.regexp.emailAddress" /></td>
						</tr>
						<tr>
							<td></td>
							<td><input id="user_profile_notify" name="notification_ck"
								dojoType="dijit.form.CheckBox" checked> <label
								for="notification_ck"><fmt:message key="receive_notification" /></label></td>
						</tr>
						<tr>
							<td width="25%" align="right"><label class="valueLabel" for="firstName"><fmt:message
										key="user_first" /> </label></td>
							<td><input dojoType="dijit.form.ValidationTextBox"
								trim="true" type="text" name="firstName" id="user_profile_first">
							</td>
						</tr>
						<tr>
							<td width="25%" align="right"><label class="valueLabel" for="lastName"><fmt:message
										key="user_last" /> </label></td>
							<td><input dojoType="dijit.form.ValidationTextBox"
								trim="true" type="text" name="lastName" id="user_profile_last">
							</td>
						</tr>
						<!-- 
						<tr>
							<td width="25%" align="right"><label for="groups"><fmt:message
										key="user_groups" /> </label></td>
							<td>

								<div id="dynamicGroupSel" name="groups"
									dojoType="dijit.form.MultiSelect"></div>
							</td>
						</tr>
						 -->
						
						<tr>
							<td width="25%" align="right"><label class="valueLabel" for="description">
									<fmt:message key="user_desc" />
							</label></td>
							<td><textarea id="user_profile_desc" name="description"
									dojoType="dijit.form.SimpleTextarea" rows="2" cols="24"
									style="width: auto;"></textarea></td>
						</tr>
					</table>
				</div>
                <div class="whiteDialogFooter">
                    <button dojoType=dijit.form.Button type="Reset" name="reset" baseclass="cancelButton"
                            onClick="dijit.byId('reset_pass_dlg').hide();">
                        <fmt:message key="Cancel" />
                    </button>
                    
                    <button dojoType=dijit.form.Button type="button" name="submit" baseclass="primaryButton"
                            id="update_user_profile_button"
                            onClick="">
                        <fmt:message key="OK" />
                    </button>
                </div>
			</div>
		</div>

</fmt:bundle>

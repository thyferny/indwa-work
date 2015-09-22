<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@page import="com.alpine.miner.security.impl.LDAPConfiguration"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <script type="text/javascript">
        dojo.require("alpine.system.security_config");
    </script>
</head>
<fmt:bundle basename="app">
	<body>
		<div dojoType="dijit.Dialog" draggable="false" id="security_config"
			title="<fmt:message key='security'/>">
			<div dojoType="dijit.layout.LayoutContainer" style="width: 800px; height: 460px;">

				<div dojoType="dijit.layout.ContentPane" 
					style="width: 100%"
					region="top">
					<div class="titleBar">
			            <fmt:message key='security'/>
			        </div>
					<form id="auth_provider_form">
						<table cellspacing="10" width="100%" height="100%">		
						<%--		
							<tr>
								<td width="100px"><label style="font-size: 14pt" for="auth_type"> 
								<fmt:message key="auth_provider_type" /> 
								</label></td>
							</tr>
							 --%>		
							<tr>
								<td width="600px">
									<div style="width: 100%" id="auth_provider_radio_button_table"></div>
									<input dojoType="dijit.form.RadioButton" id="auth_provider_radio_button_table_LocalProvider" name="authentication_provider" value="LocalProvider" onclick="alpine.system.security_config.check_sec_cfg_auth_mode()"/>
									<fmt:message key='security_LocalProvider'/>
									<input dojoType="dijit.form.RadioButton" id="auth_provider_radio_button_table_LDAPProvider" name="authentication_provider" value="LDAPProvider" onclick="alpine.system.security_config.check_sec_cfg_auth_mode()"/>
									<fmt:message key='security_LDAPProvider'/>
									<input dojoType="dijit.form.RadioButton" id="auth_provider_radio_button_table_ADProvider" name="authentication_provider" value="ADProvider" onclick="alpine.system.security_config.check_sec_cfg_auth_mode()"/>
									<fmt:message key='security_ADProvider'/>
									<input dojoType="dijit.form.RadioButton" id="auth_provider_radio_button_table_CustomProvider" name="authentication_provider" value="CustomProvider" onclick="alpine.system.security_config.check_sec_cfg_auth_mode()"/>
									<fmt:message key='security_CustomProvider'/>
								</td>
							</tr>	
													
						</table>
					</form>
				</div>
				<div dojoType="dijit.layout.ContentPane" 
					style="width: 100%;"
					region="center">
					
					<div id="sec_cfg_tab_container" 
						dojoType="dijit.layout.StackContainer" 
						height ="100%" width = "100%">
						
			        	<div width="100%" height="100%" 
							dojoType="dijit.layout.ContentPane" 	
							id="LDAPProvider_tab"			    			> 
			        		<div dojoType="dijit.form.Form" 
						id="ldapConfigForm" 
						jsId="ldapConfigForm" action="" 
						method="" style="overflow:auto;">
						<table cellspacing="2" width="100%" height="100%">
						
						<tr>
						<td>
						<table cellspacing="6" >
							<tr>
								<td colspan=2>
									<label style="font-size: 14pt" for="auth_type">
									<fmt:message key="ldap_config" /></label>									
								</td>
							</tr>
							<tr><td height="40px"></td></tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="host"> <fmt:message
											key="LDAP_HOST" />
								</label></td>
								<td><input type="text" name="host"
									 required="true" trim="true"
									dojoType="dijit.form.ValidationTextBox"
									id="sec_cfg_ldap_1"
									 /></td>								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="port"> <fmt:message
											key="LDAP_PORT" />
								</label></td>
								<td><input name="port"
									required="true" trim="true"
									id="sec_cfg_ldap_2"
									constraints="{min: 1, max: 99999, places: 0}"
									invalidMessage="<fmt:message key="NumberTextValid"/>"
									dojoType="dijit.form.NumberTextBox" /></td>
								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="ldapVersion">
									<fmt:message key="LDAP_VERSION" /></label></td>
								<td><select
									dojoType="dijit.form.Select" id="sec_cfg_ldap_3"
										name="ldapVersion">
										<option value="LDAP v3">
											<fmt:message key="LDAP_V3" />
										</option>
										<option value=LDAP v2>
											<fmt:message key="LDAP_V2" />
										</option>
								</select>
								</td>
								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="principal"><fmt:message
											key="LDAP_PRINCIPAL" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_4"
									required="true" trim="true" type="text" name="principal">
								</td>
								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="credential"><fmt:message
											key="LDAP_PASSWORD" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_5"
									required="true" trim="true" type="password" name="credential"
									></td>
								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="level">
									<fmt:message key="LDAP_LEVEL" /></label></td>
								<td><select
									dojoType="dijit.form.Select" id="sec_cfg_ldap_6"
										name="level">
										<option value="simple">
											<fmt:message key="LDAP_user_password" />
										</option>
										<option value="anonymous">
											<fmt:message key="LDAP_user_only" />
										</option>
								</select>
								</td>
								
							</tr>

							
						</table>
						</td>
						<td>
						<table cellspacing="6" >
							
							<tr>
								
								<td  align="right"><label class="valueLabel" for="groupDN"><fmt:message
											key="LDAP_GROUP_DN" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_7"
									required="true" trim="true" type="text" name="groupDN">
								</td>
							</tr>
							<tr>
								
								<td  align="right"><label class="valueLabel" for="groupNameAddr"><fmt:message
											key="LDAP_GROUP_ADDR" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_8"
									required="true" trim="true" type="text" name="groupNameAddr">
								</td>
							</tr>
							 
							
							<tr>
								
								<td  align="right"><label class="valueLabel" for="userDN"><fmt:message
											key="LDAP_USER_DN" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_9"
									required="true" trim="true" type="text" name="userDN">
								</td>
							</tr>
							<tr>
								
								<td  align="right"><label class="valueLabel" for="userNameAddr"><fmt:message
											key="LDAP_USER_ADDR" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_10"
									required="true" trim="true" type="text" name="userNameAddr">
								</td>
							</tr>
							<tr>
								
								<td  align="right"><label class="valueLabel" for="userPassowrdAddr"><fmt:message
											key="LDAP_USER_PADDWORD" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_11"
									required="true" trim="true" type="text" name="userPassowrdAddr">
								</td>
							</tr>
							<tr>							
								<td  align="right"><label class="valueLabel" for="userEmailAddr"><fmt:message
											key="LDAP_USER_EMAIL" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_12"
									required="true" trim="true" type="text" name="userEmailAddr">
								</td>
							</tr>
							<tr>							
								<td  align="right"><label class="valueLabel" for="userNotifyAddr"><fmt:message
											key="LDAP_USER_Notify" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_13"
									trim="true" type="text" name="userNotifyAddr">
								</td>
							</tr>
							<tr>							
								<td  align="right"><label class="valueLabel" for="userFirstNameAddr"><fmt:message
											key="LDAP_USER_Firstname" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_14"
									trim="true" type="text" name="userFirstNameAddr">
								</td>
							</tr>
							<tr>							
								<td  align="right"><label class="valueLabel" for="userLastNameAddr"><fmt:message
											key="LDAP_USER_Lastname" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_15"
									trim="true" type="text" name="userLastNameAddr">
								</td>
							</tr>
							<tr>							
								<td  align="right"><label class="valueLabel" for="userDescriptionAddr"><fmt:message
											key="LDAP_USER_Desc" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_16"
									trim="true" type="text" name="userDescriptionAddr">
								</td>
							</tr>
							<tr>							
								<td  align="right"><label class="valueLabel" for="roleAddr"><fmt:message
											key="LDAP_ROLE" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_17"
									trim="true" type="text" name="roleAddr">
								</td>
							</tr>
							<tr>							
								<td  align="right"><label class="valueLabel" for="chorusKey"><fmt:message
											key="LDAP_CHORUS_KEY" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_ldap_18"
									trim="true" type="text" name="chorusKey">
								</td>
							</tr>

							
							<tr>
								<td align="right" colspan="2">
									<button dojoType="dijit.form.Button" type="button" baseClass="secondaryButton"
										jsId="sec_ldap_test_button"
										onClick="return alpine.system.security_config.test_security_config()">
										<fmt:message key="TEST_CONNECTION" />
									</button>
									
								</td>
								
							</tr>
						</table>
						</td>
						<td>
						</td>
						</tr>
						</table>
						
					</div>
			        	</div>
			        	
			        	<div width="100%" height="100%" 
							dojoType="dijit.layout.ContentPane" 	
							id="ADProvider_tab"			    			> 
			        		<div dojoType="dijit.form.Form" 
						id="adConfigForm" 
						jsId="adConfigForm" action="" 
						method="" style="overflow:auto;">
						<table cellspacing="2" width="100%" height="100%">
						
						<tr>
						<td>
						<table cellspacing="6" >
							<tr>
								<td colspan=2>
									<label style="font-size: 14pt" for="auth_type">
									<fmt:message key="ad_config" /></label>									
								</td>
							</tr>
							<tr><td height="40px"></td></tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="host"> <fmt:message
											key="LDAP_HOST" />
								</label></td>
								<td><input type="text" name="host"
									 required="true" trim="true"
									dojoType="dijit.form.ValidationTextBox"
									id="sec_cfg_AD_1"
									 /></td>								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="port"> <fmt:message
											key="LDAP_PORT" />
								</label></td>
								<td><input name="port"
									required="true" trim="true"
									id="sec_cfg_AD_2"
									constraints="{min: 1, max: 99999, places: 0}"
									invalidMessage="<fmt:message key="NumberTextValid"/>"
									dojoType="dijit.form.NumberTextBox" /></td>
								
							</tr>
							
							<tr>
								<td  align="right"><label class="valueLabel" for="principal"><fmt:message
											key="LDAP_PRINCIPAL" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_AD_3"
									required="true" trim="true" type="text" name="principal">
								</td>
								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="credential"><fmt:message
											key="LDAP_PASSWORD" /> </label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_AD_4"
									required="true" trim="true" type="password" name="credential"
									></td>
								
							</tr>
							<tr>
								<td  align="right"><label class="valueLabel" for="level">
									<fmt:message key="AD_DOMAIN_NAME"/></label></td>
								<td><input dojoType="dijit.form.ValidationTextBox"
								id="sec_cfg_AD_5"
									required="true" trim="true" type="text" name="domain">
								</td>
								
							</tr>
<tr>
<td></td>
								<td align="right" colspan="2">
									<button dojoType="dijit.form.Button" type="button"  baseClass="secondaryButton"
										id="sec_cfg_AD_6" jsId="sec_AD_test_button"
										onClick="return alpine.system.security_config.test_security_config()">
										<fmt:message key="TEST_CONNECTION" />
									</button>
									
								</td>
								
							</tr>
							
						</table>
						</td>
						<td>
						
							
						
						</td>
						<td>
						</td>
						</tr>
						</table>
						
					</div>
			       </div>
			        	<div width="100%" height="100%" 
							dojoType="dijit.layout.ContentPane" 	
							id="CustomProvider_tab"			    			> 
			        		<div dojoType="dijit.form.Form" id="customForm" jsId="customForm"
						action="" enctype="multipart/form-data" method="POST" style="overflow: auto;">

						<table cellspacing="6" width="100%" >
							<tr>
								<td colspan=2 width="100px">
									<label style="font-size: 14pt" for="auth_type"> 
									<fmt:message key="custom_config" /> 
									</label></td>
							</tr>
							<tr>
								<td  align="right"><label for="jarFile"> <fmt:message
											key="custom_jar_file" />
								</label></td>
								<td width="400px"><input type="file" id="sec_custom_jarFile" 
									name="jarFile"
									required="true" trim="true"
									size="48" /></td>
							</tr>
							<tr>
								<td  align="right"><label for="className"> <fmt:message
											key="custom_class_name" />
								</label></td>
								<td><input type="text" id="sec_custom_className" 
									name="className"
									required="true" trim="true"
									dojoType="dijit.form.ValidationTextBox"/></td>
							</tr>
							<tr>
								<td align="right" colspan="4">
									<button dojoType="dijit.form.Button" type="button" baseClass="secondaryButton"
										id="sec_custom_test_button" jsId="sec_custom_test_button"
										onClick="return alpine.system.security_config.test_security_config()">
										<fmt:message key="TEST_CONNECTION" />
									</button>
									
								</td>
								
							</tr>
							
						</table>
					</div>
			        	</div>
					</div>
					</div>
				
			</div>
			
			<div dojoType="dijit.layout.ContentPane" region="bottom">
                <div class="whiteDialogFooter">
					<button dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
						onClick="alpine.system.security_config.close_security_config();">
						<fmt:message key="Cancel" />
					</button>
					<button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
						onClick="return alpine.system.security_config.save_security_config();">
						<fmt:message key="OK" />
					</button>
                </div>
			</div>
		</div>
			
	</body>
</fmt:bundle>
</html>
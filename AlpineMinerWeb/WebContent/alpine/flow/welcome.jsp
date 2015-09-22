<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/alpine/commons/config.jsp" %>
<%@page import="com.alpine.miner.impls.mail.MailConfiguration"%>
<%
    String host = request.getRemoteHost();
    String port = String.valueOf(request.getServerPort());
    String baseURL = request.getContextPath();//"http://" + request.getHeader("Host")+ request.getContextPath();
    String progressImage = baseURL + "/images/progressBar.gif";

%>
<html xmlns="http://www.w3.org/1999/xhtml">
<fmt:bundle basename="app">

<script type="text/javascript">
var invalidateResourceNames = ['\\', '/', ':', '*', '?', '"', '<', '>', '|','/', '\0'];

var baseURL = "<%=baseURL%>";
var progressImage = "<%=progressImage%>" ;
var iconpath = baseURL + "/images/icons/";
var flowBaseURL = baseURL + "/main/flow.do";
var preferenceBaseURL = baseURL + "/main/preference.do";
var connBaseURL = baseURL + "/main/dbconnection.do";
var udfBaseURL = baseURL + "/main/udf.do";
var loginURL = alpine.WEB_APP_NAME + "/index.jsp";
var logoutURL = alpine.WEB_APP_NAME + "/alpine/login/weblogout.jsp";

var ds = new httpService();
var login = null;
var error_msg = null;
var y_offset = 40;
var x_offset = 80;
var y_link_off = 0;
var current_flow_id;
</script>

<head>
    <script type="text/javascript">
        dojo.require("alpine.flow.WelcomeHelper");
        dojo.require("alpine.system.security_config");

    </script>

</head>


<body class="soria">
<div dojoType="dijit.layout.LayoutContainer" style="width:100%; height: 100%;">
    <div dojoType="dijit.layout.ContentPane" region="top">
        <table id="banner" width="100%">
            <tr>
                <td>
                    <img style="padding: 5px;"
                         src="../../images/interface/banner_logo.png" />
                </td>
                <td align="right">
                    <div class="userPanelButtonPanel">
                        <div dojoType="dijit.form.DropDownButton" id="settings_button" baseClass="userPanelDropdownButton">
                            <span><fmt:message key='helloUser' /></span>
                            <div dojoType="dijit.Menu" baseClass="userPanelDropdownButton">
                                <div dojoType="dijit.MenuItem" id="datasourceConnections_button">
                                    <fmt:message key='datasource_config_menu_title' />
                                </div>
                                <div dojoType="dijit.MenuItem" id="scheduler_button">
                                    <fmt:message key='scheduler_configuration_button_title' />
                                </div>
                                <div dojoType="dijit.MenuSeparator"></div>

                                <alpine:permissionChecker permission="UPDATE_PREFERENCE,
					                                        				SECURITY_UPDATE,
					                                        				USER_MANAGEMENT_EDIT,
					                                        				GROUP_MANAGEMENT_EDIT,
					                                        				MAIL_SERVER_CONFIG_UPDATE,
					                                        				UDF_MANAGEMENT_EDIT,
					                                        				SESSION_MANAGEMENT_KILL">
                                    <alpine:permissionChecker permission="UPDATE_PREFERENCE">
                                        <div dojoType="dijit.MenuItem" id="prefrenece_button">
                                            <fmt:message key='prefrenece' />
                                        </div>
                                    </alpine:permissionChecker>
                                    <alpine:permissionChecker permission="SECURITY_UPDATE">
                                        <div dojoType="dijit.MenuItem" id="security_button" onClick="alpine.system.security_config.showSecurityDialog();">
                                            <fmt:message key='security' />
                                        </div>
                                    </alpine:permissionChecker>
                                    <alpine:permissionChecker permission="USER_MANAGEMENT_EDIT,GROUP_MANAGEMENT_EDIT">
                                        <div dojoType="dijit.MenuItem" id="groupusers_button" onClick="alpine.system.UserGroupManagementUIHelper.showGroupUserDialog();">
                                            <fmt:message key='groupusers' />
                                        </div>
                                    </alpine:permissionChecker>
                                    <alpine:permissionChecker permission="MAIL_SERVER_CONFIG_UPDATE">
                                        <div dojoType="dijit.MenuItem" id="btn_edit_mail_config">
                                            <fmt:message key='Mail_Configuration' />
                                        </div>
                                    </alpine:permissionChecker>
                                    <alpine:permissionChecker permission="UDF_MANAGEMENT_EDIT">
                                        <div dojoType="dijit.MenuItem" id="udf_button" onClick="show_udf_dialog();">
                                            <fmt:message key='UDF' />
                                        </div>
                                    </alpine:permissionChecker>
                                    <alpine:permissionChecker permission="SESSION_MANAGEMENT_KILL">
                                        <div dojoType="dijit.MenuItem" id="alpine_system_sessionMgr_menu">
                                            <fmt:message key='session_manager_title' />
                                        </div>
                                    </alpine:permissionChecker>
                                    <div dojoType="dijit.MenuSeparator"></div>
                                </alpine:permissionChecker>
                                <div dojoType="dijit.MenuItem" id="reset_passworod_button">
                                    <fmt:message key='restpassword' />
                                </div>
                                <div dojoType="dijit.MenuItem" id="user_logs_button">
                                    <fmt:message key='audit_button_entrance' />
                                </div>
                                <div dojoType="dijit.MenuSeparator"></div>
                                <div dojoType="dijit.MenuItem" id="alpine_system_licenseInfo_menu">
                                    <fmt:message key='license_displayer_about' />
                                </div>
                            </div>
                        </div>
                        <button id="logout_button" data-dojo-type="dijit.form.Button" baseClass="userPanelButton" type="button">
                            <fmt:message key='signout' />
                        </button>
                        <button id="general_help_button" data-dojo-type="dijit.form.Button" baseClass="userPanelButton" type="button">
                            <fmt:message key='outputtitlebar_help'/>
                        </button>
                    </div>
                </td>
            </tr>
        </table>
    </div>
</div>

</body>

</fmt:bundle>
</html>
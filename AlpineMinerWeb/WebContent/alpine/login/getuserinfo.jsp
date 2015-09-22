
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<%@ include file="/alpine/commons/jstl.jsp"%>
<html>
<fmt:bundle basename="app">
<head>

<%
	String path = request.getContextPath();
%>
 <link rel="shortcut icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<link rel="icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<%@ include file="/alpine/commons/dojoinclude.jsp"%>

<script type="text/javascript">
		dojo.require("dojox.json.ref");
		dojo.require("dijit.form.ValidationTextBox");
		dojo.require("dijit.Dialog");
		dojo.require("dijit.form.Button");
		dojo.require("dijit.form.Form");
        dojo.require("dijit.layout.LayoutContainer");
        dojo.require("dijit.layout.ContentPane");
        dojo.require("dojox.validate.regexp");
        dojo.require("alpine.onlinehelp.BasicOnlineHelp");
        dojo.require("alpine.layout.PopupDialog");
</script>

    <%@ include file="/alpine/admin/licenseUpdate.jsp"%>


    <script type="text/javascript" src="../../js/alpine/common.js"
	charset="utf-8"></script>
 
 
<script type="text/javascript" src="../../js/alpine/httpService.js" 
	charset="utf-8"></script>
 <!--
<script type="text/javascript" src="../../js/alpine/prototype.js"
	charset="utf-8"></script>
  -->


<script type="text/javascript"
	src="../../js/alpine/popupComponent.js" charset="utf-8">
</script>

<title>Alpine Data Labs</title>




</head>
	<SCRIPT LANGUAGE="javascript">
        var loginURL = "<%=path%>"+ "/index.jsp";
        var ds = new httpService();

	var loginURL = "<%=path%>"+ "/index.jsp";

		dojo.addOnLoad(function() {
			alpine = {
					nls: {
						OK: "<fmt:message key='OK'/>"
					},
                 baseURL: "<%=request.getContextPath()%>"
				};
			var bt = dijit.byId("user_email_address");

			dojo.connect(bt, 'onKeyPress', function(evt) {
				key = evt.keyCode;
				if (key == dojo.keys.ENTER) {
					get_user_info();
				}
			});
		});

		function get_user_info() {
			if (alpine_sendmail_form.validate() == false) {
				popupComponent.alert("Email address error!");
				return;
			}
			var user = {};
			user.email = dojo.byId("user_email_address").value;
			var ds = new httpService();
			var url = "<%=path%>" + "/main/admin.do?method=getUserInfo";
			ds.post(url, user, sendmail_callback, sendmail_error);

		}

		function sendmail_callback(obj) {
			if(obj&&obj.error_code){
				handle_error_result(obj);
				return ;
			}
			window.location.pathname = "<%=path%>/alpine/login/login.jsp";
		}

		function sendmail_error(text, args) {
			var str = dojox.json.ref.toJson(text);
			popupComponent.alert(str);
		}

		function reset() {
			dojo.byId("user_email_address").value = "";
		}

	</SCRIPT>

	<body class="soria">
		<!-- Banner -->
		<div>
			<img style="padding: 5px; margin-top: 5px;"
			src="../../images/interface/banner_logo.png" />
		</div>

		<div id="container2">


			<div dojoType="dijit.layout.ContentPane" layoutAlign="client">
				<div dojoType="dijit.form.Form" id="alpine_sendmail_form"
					jsId="alpine_sendmail_form" encType="multipart/form-data" action=""
					method="">

					<table cellspacing="10" id="alpine_sendmail_form_table">
						<tr>
							<td colspan="2">
								<h3>
								<fmt:message key="System_Status" />
								</h3></td>
						</tr>
						<tr>
							<td colspan="2">
								<div>
								<fmt:message key="All_System_Are_Functional" />
								</div></td>
						</tr>
						<tr>
							<td colspan="2">
								<h3><fmt:message key="Forget_Your_Password" /></h3></td>
						</tr>
						<tr>
							<td colspan="2">
								<div>
								<fmt:message key="Input_Your_EMail" /></div></td>
						</tr>

						<tr>
							<td width="25%" align="right"><label for="email"> <fmt:message
										key="user_email" />
							</label></td>
							<td><input type="email" id="user_email_address" name="email"
								required="true" trim="true"
								dojoType="dijit.form.ValidationTextBox"
								regExpGen="dojox.validate.regexp.emailAddress" /></td>
						</tr>


						<tr>

						</tr>
						<tr>
							<td colspan="2" align="right">
								<button dojoType="dijit.form.Button" type="button" baseClass="dialogButton"
								     onClick="get_user_info()">
								<fmt:message key="Send_Now" />
							</td>
						</tr>
                        <tr>
                            <td colspan=2 align="left">
                                    <fmt:message key="license_update_text" />
                                    <a href="javascript:alpine_system_licenseUpdate.startup();"><fmt:message key="user_signin_here" /></a>
                            </td>
                        </tr>
					</table>

				</div>
			</div>
		</div>

	</body>
</fmt:bundle>
</html>
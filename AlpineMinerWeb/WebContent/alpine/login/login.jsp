<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<%
	String path = request.getContextPath();
    String baseURL = request.getContextPath();
%>
 <link rel="shortcut icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<link rel="icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">

<%@ include file="/alpine/commons/jstl.jsp"%>
<%@ include file="/alpine/commons/dojoinclude.jsp"%>


    <script type="text/javascript" src="../../js/alpine/common.js"
	charset="utf-8"></script>
  <!-- 
<script type="text/javascript" src="../../js/alpine/prototype.js"
	charset="utf-8"></script>
  -->
 
<script type="text/javascript" src="../../js/alpine/alpineConstants.js" charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/httpService.js" charset="utf-8"></script>


<%--<script type="text/javascript"--%>
	<%--src="../../js/alpine/flow/OnlineHelp.js" charset="utf-8">--%>
<%--</script>--%>

<script type="text/javascript"
	src="../../js/alpine/popupComponent.js" charset="utf-8">
</script>

    <!--[if IE]>
    <script type="text/javascript"
            src="http://ajax.googleapis.com/ajax/libs/chrome-frame/1/CFInstall.min.js"></script>
    <![endif]-->


    <title>Alpine Data Labs</title>

 

</head>
<fmt:bundle basename="app">
	<script type="text/javascript">
        var baseURL = "<%=baseURL%>";
        var loginURL = "<%=path%>"+ "/index.jsp";
		var ds = new httpService();
		var alpine = {
			nls: {
				OK: "<fmt:message key='OK'/>",
				Cancel: "<fmt:message key='Cancel'/>",
				can_not_connect_server: "<fmt:message key='can_not_connect_server'/>",
				message_unknow_error: "<fmt:message key='message_unknow_error'/>",
				system_update_tip_need_clean_script_cache: "<fmt:message key='system_update_tip_need_clean_script_cache'/>"
			},
			TS: "",
			USER: "",
			baseURL: "<%=request.getContextPath()%>"
		};
		dojo.addOnLoad(function() {
	
			var bt = dijit.byId("password");

			dojo.connect(bt, 'onKeyPress', function(evt) {
				key = evt.keyCode;
				if (key == dojo.keys.ENTER) {
					check();
				}
			});
            dojo.attr(dijit.byId('login').textbox, "autocomplete", "on");
            dojo.attr(dijit.byId('password').textbox, "autocomplete", "on");
            document.getElementById("login").focus();
            
		    setTimeout("hideLoader()",100);
		    
		    _checkScriptRelVersion();
		});

		var user = {};
		function check() {			
			user.login = document.getElementById("login").value;
			user.password = document.getElementById("password").value;
            document.getElementById("login_error").innerHTML=" ";
            document.getElementById("login_error_div").style.display = 'none';
			var url = "<%=path%>" + "/main/admin.do?method=login";
			ds.post(url, user, login_callback);
		}
		
		function forceLogin() {			
			user.login = document.getElementById("login").value;
			user.password = document.getElementById("password").value;

			var url = "<%=path%>" + "/main/admin.do?method=forceLogin";
			ds.post(url, user, login_callback);
		}
		
		function login_callback(msg) {
			if(msg){
				switch (msg.error_code) {
				case 0:
					window.location.href = "<%=path%>"
						+ "/alpine/flow/webflowmain.jsp?user="
						+ user.login;
					break;
				case 1:
					var str = '<fmt:message key="BAD_USER_PASSWORD_COMBO"/>';
                    document.getElementById("login_error").innerHTML=str;
                    document.getElementById("login_error_div").style.display = 'block';
                    break;
				case 2:
					var str = '<fmt:message key="BAD_USER_PASSWORD_COMBO"/>';
                    document.getElementById("login_error").innerHTML=str;
                    document.getElementById("login_error_div").style.display = 'block';
                    break;
				case 3:
					var str = '<fmt:message key="ALREADY_LOGGED_IN"/>';
					var takeover = {};
					takeover.handle = forceLogin;
					popupComponent.confirm(str, takeover);
					break;
				case 4:
					var str = '<fmt:message key="BROWSER_HAS_LOGGED_BY_OTHER_USER"/>';
					var takeover = {};
					takeover.handle = forceLogin;
					popupComponent.confirm(str, takeover);
					break;
				case 5:
					alpine.system.AgreementHelper.showAgreement();
					break;
				default:
                    document.getElementById("login_error").innerHTML=str;
                    document.getElementById("login_error_div").style.display = 'block';
				}
				return;
			}
		}

		function reset() {
			document.getElementById("login").value = "";
			document.getElementById("password").value = "";
		}


		function hideLoader(){
	   		var loader = dojo.byId('loader');
	   		dojo.fadeOut({ node: loader, duration:100,
	    	onEnd: function()
	    	{ 
	     		loader.style.display = "none";

                if (dojo.isIE)
                {
                CFInstall.check({
                    mode: "overlay"
                });
                }
            }
	   		}).play();
	  	}

	    function _checkScriptRelVersion(){
	        var url = baseURL+"/main/systemUpdate.do?method=checkScriptVersion&scriptVersion=" + alpine_script_rel_version;
	        ds.get(url,function(isLatest){
	    		if(isLatest == false){
	    			popupComponent.alert(alpine.nls.system_update_tip_need_clean_script_cache);
	    		}
	    	});
	    }
	</script>
	
<script type="text/javascript">
	dojo.require("dojox.json.ref");
	dojo.require("dijit.form.ValidationTextBox");
	dojo.require("dijit.Dialog");
	dojo.require("dijit.form.Button");
	dojo.require("dijit.form.RadioButton");
	dojo.require("dijit.form.Form");
	dojo.require("dijit.layout.BorderContainer");
	dojo.require("dijit.layout.ContentPane");
	dojo.require("dijit.layout.LayoutContainer");
	dojo.require("dojox.validate.regexp");
    dojo.require("alpine.layout.ExtendedTextBox");
    dojo.require("alpine.onlinehelp.BasicOnlineHelp");
    dojo.require("alpine.system.SystemUpdateManager");
</script>
<%@ include file="./agreementManager.jsp"%>

<body class="soria">
<div id="loader"><div id="loaderInner"><fmt:message key="Loading"/>  </div></div>
		<!-- Banner -->
<!--		<div id="banner">
			<img style="padding: 5px;" src="../../images/interface/banner_logo.png" />
		</div>
-->
		<!-- Content container -->
		<div id="container">

            <!-- Login Panel -->
            <!-- Login title -->
            <div class="dialogPanel">
                <div id="loginLogo">
                    <img src="../../images/interface/logo_horiz.png"  />
                </div>
                <div id="loginBody">
                    <div class="bigHeader"><fmt:message key="user_signin" /></div>
                    <div class="errorDiv" style="padding-bottom: 8px;padding-left: 3px;"><div id="login_error_div" style="display:none; vertical-align: middle;"><img src="../../images/interface/icon_alert_generic.png" alt="alert" style="vertical-align: middle; padding-right: 5px"/><label id="login_error" class="error"></label></div></div>
                    <div>
                        <input id="login" placeHolder="<fmt:message key="user_name" />" style="margin-bottom:8px; padding-top:4px;padding-bottom:4px; padding-left:5px" dojoType="alpine.layout.ExtendedTextBox"/>
                    </div>
                    <div class="paddedTopDiv">
                        <input id="password" placeHolder="<fmt:message key="password" />" style="margin-bottom:3px; padding-top:4px;padding-bottom:4px; padding-left:5px"  type="password" dojoType="alpine.layout.ExtendedTextBox"/>
                    </div>
                    <div class="loginLink">
                        <!--<a href="javascript:showHelp('<%=path %>', 'Overview');"><fmt:message key="user_signin_here" /></a>  -->
                        <a href="<%=path%>/alpine/login/getuserinfo.jsp"> <fmt:message key="user_signin_message" />
                        </a>
                    </div>
                    <div class="paddedTopDiv" align="right" >
                        <button onClick="check" type="Button" dojoType="dijit.form.Button" baseClass="primaryButton"><fmt:message key="user_signin"/></button>
                    </div>
                    <div class="footer">
                        <div class="copyright left"><label>&copy; 2012 Alpine Data Labs</label></div><div class="privacy right"><a href="http://www.alpinedatalabs.com/company/contact.html" target="_blank"><fmt:message key="contact_us"/></a></div>
                    </div>
                </div>
            </div>
			<!-- Login Text -->
		</div>

		<!-- Login Watermark -->
		<!--<div id="loginWatermark">
			<img src="../../images/interface/alpine_design.png" />
		</div>-->


	</body>
</fmt:bundle>
</html>


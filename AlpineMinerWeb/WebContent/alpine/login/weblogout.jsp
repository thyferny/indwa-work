<%@ page pageEncoding="UTF-8" %>

<%
	String path = request.getContextPath();
	if(session.getAttribute(Resources.SESSION_USER) != null){
		String loginName = ((UserInfo)session.getAttribute(Resources.SESSION_USER)).getLogin();
		AuditManager.INSTANCE.appendUserAuditItem(loginName,new AuditItem(loginName,ActionType.LOGOUT));
	 	session.removeAttribute(Resources.SESSION_USER);
	 	session.removeAttribute(Resources.SESSION_PERMISSION);
	}
%>

<%@page import="com.alpine.miner.impls.Resources"%>

<%@page import="com.alpine.miner.impls.audit.AuditManager"%>
<%@page import="com.alpine.miner.security.UserInfo"%>
<%@page import="com.alpine.miner.impls.audit.ActionType"%>
<%@page import="com.alpine.miner.impls.audit.AuditItem"%>
<html>
<head> 
 <link rel="shortcut icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<link rel="icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<title>Alpine Data Labs</title>
<script type="text/javascript">
window.location.href = "<%=path%>/index.jsp";
</script>
</html>
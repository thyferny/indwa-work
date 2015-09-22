<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@page import="com.alpine.miner.impls.onlinehelp.OperatorHelpConvertion"%>
<%
	String operatorName = request.getParameter("operatorName");
	String path = request.getContextPath();
	String initPage = "";
	if(operatorName != null && !"".equals(operatorName)){
		initPage = "operators/" + OperatorHelpConvertion.getInstance(request.getLocale()).getHelpVal(operatorName);
	}
%>

<HTML>
<HEAD> 
<link rel="shortcut icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<link rel="icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
</HEAD>
<frameset rows="100,*">
	<frame src="header.html" >		
	<frameset cols="30%,*"> 
		<frame src="menu.jsp" >
		<frame name="content" src="<%=initPage %>">
	</frameset>
</frameset>
</HTML>

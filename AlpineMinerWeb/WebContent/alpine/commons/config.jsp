<%@page import="com.alpine.utility.hadoop.HadoopConnection"%>
<%@page import="com.alpine.miner.security.permission.Permission"%>
<%@page import="java.util.Set"%>
<%@page import="com.alpine.miner.impls.Resources"%>
<%@page import="com.alpine.miner.security.UserInfo"%>
<%@page import="com.alpine.miner.util.SearchReplaceUtil" %>
<%@ page pageEncoding="UTF-8" %>

<%
	String path = request.getContextPath();
	String user = request.getParameter("user");
	String baseURL2 = request.getContextPath();//"http://" + request.getHeader("Host") + request.getContextPath();
	
	UserInfo userInfo = (UserInfo) session.getAttribute(Resources.SESSION_USER);
	Set<Permission> blacklist = (Set<Permission>)session.getAttribute(Resources.SESSION_PERMISSION);
	String ts = (String) session.getAttribute(Resources.SESSION_TIME_STAMP);
	String auth_type = (String) session.getAttribute(Resources.AUTH_TYPE);
	String language = request.getLocale().getLanguage();

	if(userInfo == null){// session already expired. So we forward to login page.
		response.sendRedirect(path + "/index.jsp");
		return;
	}
%>

<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="alpine" uri="/WEB-INF/alpine.tld" %>
<fmt:setLocale value="<%=language%>" />

<%@ include file="/alpine/commons/dojoinclude.jsp"%>

<fmt:bundle basename="app">

<script type="text/javascript"
	src="../../js/alpine/namespace.js" charset="utf-8">
</script>

<script type="text/javascript">

		dojo.require("dijit.Dialog");
		dojo.require("dijit.Editor");
		dojo.require("dijit.form.Button");
		dojo.require("dijit.form.CheckBox");
		dojo.require("dijit.form.ComboBox");
		dojo.require("dijit.form.DateTextBox");
		dojo.require("dijit.form.DropDownButton");
		dojo.require("dijit.form.FilteringSelect");
		dojo.require("dijit.form.Form");
		dojo.require("dijit.form.MultiSelect");
		dojo.require("dijit.form.NumberSpinner");
		dojo.require("dijit.form.NumberTextBox");
		dojo.require("dijit.form.Select");
		dojo.require("dijit.form.SimpleTextarea");
		dojo.require("dijit.form.TextBox");
		dojo.require("dijit.form.TimeTextBox");
		dojo.require("dijit.form.ValidationTextBox");
		dojo.require("dijit.layout.AccordionContainer");
		dojo.require("dijit.layout.AccordionPane");
		dojo.require("dijit.layout.BorderContainer");
		dojo.require("dijit.layout.ContentPane");
		dojo.require("dijit.layout.LayoutContainer");
		dojo.require("dijit.layout.LinkPane");
		dojo.require("dijit.layout.SplitContainer");
		dojo.require("dijit.layout.TabContainer");
		dojo.require("dijit.Menu");
		dojo.require("dijit.MenuItem");
		dojo.require("dijit.ProgressBar");
		dojo.require("dijit.TitlePane");
		dojo.require("dijit.Toolbar");
		dojo.require("dijit.ToolbarSeparator");//
		dojo.require("dijit.Tooltip");
		dojo.require("dijit.TooltipDialog");//
		dojo.require("dijit.Tree");
		dojo.require("dijit.tree.dndSource");
		dojo.require("dijit.tree.ForestStoreModel");
 
		dojo.require("dojo.data.ItemFileReadStore");
		dojo.require("dojo.data.ItemFileWriteStore");
	    dojo.require("dojo.fx.easing");
		dojo.require("dojo.io.iframe");
		dojo.require("dojo.window");
        dojo.require("dojo.require");
        dojo.require("dojo.dnd.Target");
        dojo.require("dojo.dnd.AutoSource");
        dojo.require("dojo.fx.Toggler");
		
		dojo.require("dojox.form.CheckedMultiSelect");
		dojo.require("dojox.form.FileInput");
		dojo.require("dojox.form.ListInput");
	    dojo.require("dojox.form.Uploader");
        dojo.require("dojox.form.uploader.plugins.HTML5");
	    dojo.require("dojox.form.uploader.plugins.IFrame");
		dojo.require("dojox.fx");
		dojo.require("dojox.fx.flip");
		dojo.require("dojox.gfx");
		dojo.require("dojox.gfx.arc");  
		//dojo.require("dojox.gfx._base");  
		dojo.require("dojox.gfx.fx");
		dojo.require("dojox.gfx.Moveable");
		dojo.require("dojox.gfx.path");  
		dojo.require("dojox.gfx.shape");  
		dojo.require("dojox.grid.cells.dijit");// this makes dijit validation work in grid cell.
		dojo.require("dojox.grid._CheckBoxSelector");
		dojo.require("dojox.grid.EnhancedGrid");
		dojo.require("dojox.grid.enhanced.plugins.GridSource");
        dojo.require("dojox.grid.enhanced.plugins.IndirectSelection");
        dojo.require("dojox.grid.DataGrid");
		dojo.require("dojox.grid.TreeGrid");
		dojo.require("dojox.json.ref");
		dojo.require("dojox.layout.ExpandoPane");
		dojo.require("dojox.layout.ResizeHandle");
		dojo.require("dojox.layout.ScrollPane");
		dojo.require("dojox.layout.TableContainer");
		dojo.require("dojox.validate.regexp");
		dojo.require("dojox.widget.Standby");
		dojo.require("dojox.widget.Toaster");
		dojo.require("dojox.socket");
        dojo.require("dojox.storage");
        dojo.require("dojox.storage.manager");

		dojo.require("alpine.layout.OperatorDnD");
        dojo.require("alpine.layout.ExtendedTextBox");
        dojo.require("alpine.layout.PopupDialog");
        dojo.require("alpine.spinner");
        dojo.require("alpine.system.PermissionUtil");


</script>

<script type="text/javascript" src="../../js/alpine/common.js"
	charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/dataStore.js"
	charset="utf-8"></script>
 <!-- 
<script type="text/javascript" src="../../js/alpine/prototype.js"
	charset="utf-8"></script>
  -->
 
<script type="text/javascript" src="../../js/alpine/httpService.js"
	charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/visual/dataexplorer.js"
	charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/alpineConstants.js"
	charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/popupComponent.js"
	charset="utf-8"></script>		
<script type="text/javascript" src="../../js/alpine/utility.js"
	charset="utf-8"></script>

<script src="../../js/codemirror/codemirror-uncompressed.js"></script>
<script src="../../js/codemirror/pig-features.js"></script><%-- the codemirror is dependence on this file --%>



<style>.CodeMirror {border: 1px inset #dee;}</style>
<style>.CodeMirror-completions {z-index: 1000;}</style>

<div id="loader"><div id="loaderInner"><fmt:message key="Loading"/>  </div></div>
<script type="text/javascript">


	var alpine;
	if(!alpine){
		alpine = {};		
	}
	
	alpine.WEB_APP_NAME = "<%=path%>";
	alpine.USER = "<%=user%>";
	alpine.TS = "<%=ts%>";
	alpine.auth_type = "<%=auth_type%>";

	alpine.userInfo = buildUserInfo();
	alpine.ResultEvent = 1;
	alpine.FaultEvent = 0;
	//this is for progress, sometime the js  can not get the URL
	//alpine.progressImage = "<%=baseURL2%>" 	+ "/images/progressBar.gif";
	alpine.baseURL = "<%=baseURL2%>";


	alpine.DATABASETYPE_ORACLE = "Oracle";
	alpine.DATABASETYPE_GREENPLUM = "Greenplum";
	alpine.DATABASETYPE_POSTGRESQL = "PostgreSQL";

	alpine.hadoopVersion = "<%= HadoopConnection.CURRENT_HADOOP_VERSION%>";// it's used at Datasource function to decide whether change dialog size or not.

	function hideLoader()
    {
   		var loader = dojo.byId('loader'); 
   		dojo.fadeOut({ node: loader, duration:100,
    	onEnd: function()
    	{ 
     		loader.style.display = "none"; 
    	}
   		}).play();
  	}
  	
  	dojo.addOnLoad(function() {
	    setTimeout("hideLoader()",100);
  	});


    function buildUserInfo(){
        <%
        	if(userInfo == null){
        		%>
        			return null;
        		<%
        	}else{
        %>
        
        var groups = new Array();
        <%
        	if(userInfo.getGroups() != null){
            	for(String group : userInfo.getGroups()){
            		%>
    					groups.push("<%=group%>");
            		<%
            	}
        	}
        %>
        
        var blacklist = [];
        <%
        	for(Permission blackItem : blacklist){
        		%>
        			blacklist["<%=blackItem.toString()%>"] = true;
        		<%
        	}
	    %>
        
        
        var user = {
        		"<%=UserInfo.LOGIN%>": "<%=userInfo.getLogin()%>",
         		"<%=UserInfo.GROUPS%>": groups,
        		"<%=UserInfo.FIRSTNAME%>": "<%=userInfo.getFirstName()%>",
        		"<%=UserInfo.LASTNAME%>": "<%=userInfo.getLastName()%>",
        		"<%=UserInfo.EMAIL%>": "<%=userInfo.getEmail()%>",
        		"<%=UserInfo.CHORUS_KEY%>": "<%=userInfo.getChorusKey()%>",
        		blacklist: blacklist
       	};
       	return user;

       	<%}%>
    }

</script>

</fmt:bundle >

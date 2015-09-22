<%@ include file="../../alpine/commons/jstl.jsp"%>
<fmt:bundle basename="app">
<html>
<head>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<style type="text/css">
	#progressBarInstance img{width:100%!important;}
</style>
<%
 
String baseURL=request.getContextPath();//"http://"+request.getHeader("Host")+request.getContextPath();
 
String path = request.getContextPath();

 
String progressImage =  baseURL + "/images/progressBar.gif";
%>
 <link rel="shortcut icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<link rel="icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
<title>View Model Detail</title>
<%@ include file="/alpine/commons/dojoinclude.jsp"%>
<%@ include file="/alpine/commons/graphicsinclude.jsp"%>
 
 
 <script type="text/javascript">
      var djConfig = { modulePaths: {alpine: "../../js/alpine"},
                       parseOnLoad: true};
    </script>


<script type="text/javascript" src="../../js/alpine/d3.v2.min.js" charset="utf-8"></script>
<script type="text/javascript" src= "../../js/alpine/alpineConstants.js" charset="utf-8"></script>
<script type="text/javascript" src= "../../js/alpine/logView.js" charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/httpService.js" charset="utf-8"></script>


<script type="text/javascript" src= "../../js/alpine/result.js" charset="utf-8"></script>

<script type="text/javascript" src="../../js/alpine/common.js"
	charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/dataStore.js"
	charset="utf-8"></script>
	 <!-- 
<script type="text/javascript" src="../../js/alpine/prototype.js"
	charset="utf-8"></script>
  -->
<script type="text/javascript" src="../../js/alpine/popupComponent.js"
	charset="utf-8"></script>		
  

<script type="text/javascript">
	var baseURL = "<%=baseURL%>";
	//var progressImage = "<%=progressImage%>";
	var loginURL = "<%=path%>"+ "/index.jsp";
    if (!alpine) alpine = {};
    alpine.nls = {
				error : "<fmt:message key='Error'/>",
				no_login : "<fmt:message key='no_login'/>",
				result_not_found : "<fmt:message key='result_not_found'/>",
				OK: "<fmt:message key='OK'/>",
				Yes: "<fmt:message key='Yes'/>",
				No: "<fmt:message key='No'/>",
				Cancel: "<fmt:message key='Cancel'/>",
				message_unknow_error: "<fmt:message key='message_unknow_error'/>",
				model_not_found:"<fmt:message key='model_not_found'/>",
				can_not_connect_server : "<fmt:message key='can_not_connect_server'/>",
                scatter_plot_legend_SCATTERMATRIX_LINE :  "<fmt:message key='SCATTERMATRIX_LINE'/>",
                scatter_plot_legend_SCATTERMATRIX_VALUE : "<fmt:message key='SCATTERMATRIX_VALUE'/>",
                Logistic_Regression_Group_By_Value:"<fmt:message key='Logistic_Regression_Group_By_Value'/>",
                Logistic_Regression_Group_By_Result_Select_Tip:"<fmt:message key='Logistic_Regression_Group_By_Result_Select_Tip'/>",
                download_result_groupby_tip:"<fmt:message key='download_result_groupby_tip'/>",
                hadoop_prop_right_menu_file_explorer:"<fmt:message key='hadoop_prop_right_menu_file_explorer'/>"
		};
	dojo.require("dijit.layout.ContentPane");
	dojo.require("dijit.layout.TabContainer");
	dojo.require("dojo.string");
	dojo.require("dojo.i18n");
	//for http service..
 	dojo.require("dojox.json.ref");
 	dojo.require("dijit.Tooltip"); 
 
 	dojo.require("dojo._base.xhr");
	dojo.require("dojo._base.json");
	dojo.require("dijit.layout.TabContainer");
	dojo.require("dijit.layout.ContentPane");
	dojo.require("dijit.layout.LayoutContainer");
	dojo.require("dojox.gfx.Moveable");
	dojo.require("dojox.grid.DataGrid"); 
	dojo.require("dojo.data.ItemFileReadStore");   
	dojo.require("dojo.data.ItemFileWriteStore"); 
	
	dojo.require("dijit.Dialog");
	dojo.require("dijit.TitlePane");
    dojo.require("dijit.form.ComboBox");
    
    
    dojo.require("dojo.string");
    dojo.require("dojo.i18n");
  

    dojo.require("dojox.charting.Chart");
    dojo.require("dojox.charting.DataSeries");

    dojo.require("dojox.charting.widget.Legend");
    dojo.require("dojox.charting.widget.SelectableLegend");
    dojo.require("dojox.charting.action2d.Highlight");
    dojo.require("dojox.charting.axis2d.Default");

    dojo.require("dojox.charting.plot2d.Markers");
    dojo.require("dojox.charting.plot2d.Columns");
    dojo.require("dojox.charting.plot2d.Pie");
    dojo.require("dojox.charting.plot2d.Lines");
    dojo.require("dojox.charting.plot2d.Scatter");
    dojo.require("dijit.form.Select");
    dojo.require("dojox.charting.action2d.Tooltip");
    dojo.require("dojox.charting.action2d.MoveSlice"); 
    dojo.require("dojox.charting.action2d.Magnify");
    dojo.require("dojox.charting.action2d.Highlight");
    
    dojo.require("dojox.charting.plot2d.ClusteredColumns");
    dojo.require("dojox.charting.themes.Bahamation");
 //   dojo.require("dojox.charting.themes.PlotKit.orange");
 //   dojo.require("dojox.charting.themes.PlotKit.blue");
    dojo.require("dojo.data.ItemFileWriteStore");

    dojo.require("dijit.form.NumberSpinner");
    dojo.require("dijit.form.Textarea"); 


    dojo.require("dojox.charting.plot2d.Grid");
     //this is to save svg ...
    dojo.require("dojox.gfx.utils");

    dojo.require("dojox.widget.Standby");
    dojo.require("alpine.layout.StandbySpinner");
    dojo.require("alpine.spinner");

    dojo.require("alpine.props.HadoopFileOperatorPropertyHelper");
    dojo.require("alpine.flow.HadoopOperatorsDataExplorerManager");
    dojo.require("alpine.props.HadoopDataTypeUtil");

    var  model_output_for_view =null;

   function show_Model_Result() {
  		var ds = new httpService();
  		model_output_for_view = window.opener.modelInfoForView;
  		window.opener.modelInfoForView=null;
		var requestUrl = baseURL + "/main/model.do?method=getModelVisualization" ;
		//progressBar.showLoadingBar();
		ds.post(requestUrl,model_output_for_view, function (output){
			if(output.error_code){
				handle_error_result(output);
				return ;
			}
	  		showVislualizationModel(output,"view_model_tabRoot");
			//progressBar.closeLoadingBar();
		},function(text,arg){
			//progressBar.closeLoadingBar();
			popupComponent.alert("Error :" + text);
		}, false, "view_model_tabRoot");
  	} ;

</script>
<script type="text/javascript"
            charset="utf-8">
        var ds_result = new httpService();
</script>
</head>
<body class="soria" id="coverScopeId" onload = "show_Model_Result()" onunload = "releaseResultTab('view_model_tabRoot')">
<div id="view_model_tabRoot"  width="100%" height ="100%">
 
 
</div>

 
</body>
</html>
</fmt:bundle>
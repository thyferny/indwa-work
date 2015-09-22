<html>

<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="com.alpine.miner.utils.SysConfigManager" %>
<%@page import="com.alpine.miner.impls.web.resource.ResourceManager" %>
<%@page import="com.alpine.miner.impls.web.resource.PreferenceInfo" %>
<%@ page import="com.alpine.miner.impls.controller.ChorusController" %>
<%@ include file="../../alpine/commons/jstl.jsp" %>
<fmt:bundle basename="app">
<head>
    <style type="text/css">
        .scatter-matric-text {
        }

        .scatter-matric-text .bigger {
            font-weight: bolder;
            height: 30px;
            font-size: 20px;
        }

        .scatter-matric-text .smaller {
            font-weight: bolder;
            height: 10px;
            font-size: 10px;
        }

        #progressBarInstance img {
            width: 100% !important;
        }

        #scattermatrixchartdlg div {
            overflow: hidden;
        }
    </style>

    <%
        String path = request.getContextPath();
        String uuid = request.getParameter("uuid");
        String flowName = request.getParameter("flowName");
        String chorusWorkfileID = request.getParameter(ChorusController.CHORUS_WORKFILE_ID);
        String chorusWorkfileType = request.getParameter(ChorusController.CHORUS_WORKFILE_TYPE);
        String chorusWorkfileName = request.getParameter(ChorusController.CHORUS_WORKFILE_NAME);
        flowName = new String(flowName.getBytes(SysConfigManager.INSTANCE.getServerEncoding()), "UTF-8");
        String baseURL = request.getContextPath();//"http://"+request.getHeader("Host")+request.getContextPath();
        String progressImage = baseURL + "/images/progressBar.gif";
//scatter plot matrix accuracy
        String scatterMatrixAccuracy = ResourceManager.getInstance().getPreferenceProp(PreferenceInfo.GROUP_ALG, PreferenceInfo.KEY_MAX_SCATTER_POINTS);
    %>


    <link rel="shortcut icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
    <link rel="icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
    <title><%=flowName %> - <fmt:message key="workflow_result_title"/></title>
    <%@ include file="/alpine/commons/dojoinclude.jsp" %>
    <%@ include file="/alpine/commons/graphicsinclude.jsp" %>


    <script type="text/javascript">
        var flowUUID = "<%=uuid%>";
        var baseURL = "<%=baseURL%>";
        //var progressImage = "<%=progressImage%>";
        var flowName = "<%=flowName%>";
        var chorusWorkfileID = "<%=chorusWorkfileID%>";
        var chorusWorkfileType = "<%=chorusWorkfileType%>";
        var chorusWorkfileName = "<%=chorusWorkfileName%>";
        var isChorusRun = <%= chorusWorkfileID != null%>;
        var loginURL = "<%=path%>" + "/index.jsp";
        var CONST_scatterMatrixAccuracy = <%=scatterMatrixAccuracy%>;

        if (!alpine) alpine = {};
        alpine.nls = {
            error:"<fmt:message key='Error'/>",
            no_login:"<fmt:message key='no_login'/>",
            result_not_found:"<fmt:message key='result_not_found'/>",
            OK:"<fmt:message key='OK'/>",
            Yes:"<fmt:message key='Yes'/>",
            No:"<fmt:message key='No'/>",
            Cancel:"<fmt:message key='Cancel'/>",
            message_unknow_error:"<fmt:message key='message_unknow_error'/>",
            save_html_report:"<fmt:message key='save_html_report'/>",
            save_as:"<fmt:message key='save_as'/>",
            Result_Log:"<fmt:message key='Result_Log'/>",
            can_not_connect_server:"<fmt:message key='can_not_connect_server'/>",
            chart_barchart_direction_horizontal:"<fmt:message key='chart_barchart_direction_horizontal'/>",
            chart_barchart_direction_vertical:"<fmt:message key='chart_barchart_direction_vertical'/>",
            chart_barchart_direction_title:"<fmt:message key='chart_barchart_direction_title'/>",
            resultdownloadtip:"<fmt:message key='DirectDownloadTip'/>",
            scatter_plot_matrix_img_click_tooltip:"<fmt:message key='scatter_plot_matrix_img_click_tooltip'/>",
            scatter_plot_matrix_img_tooltip:"<fmt:message key='scatter_plot_matrix_img_tooltip'/>",
            scatter_plot_matrix_txt_tooltip:"<fmt:message key='scatter_plot_matrix_txt_tooltip'/>",
            scatter_plot_legend_SCATTERMATRIX_LINE:"<fmt:message key='SCATTERMATRIX_LINE'/>",
            scatter_plot_legend_SCATTERMATRIX_VALUE:"<fmt:message key='SCATTERMATRIX_VALUE'/>",
            Logistic_Regression_Group_By_Value:"<fmt:message key='Logistic_Regression_Group_By_Value'/>",
            Logistic_Regression_Group_By_Result_Select_Tip:"<fmt:message key='Logistic_Regression_Group_By_Result_Select_Tip'/>",
            download_result_groupby_tip:"<fmt:message key='download_result_groupby_tip'/>",
            hadoop_prop_right_menu_file_explorer:"<fmt:message key='hadoop_prop_right_menu_file_explorer'/>",
            box_plot_min:"<fmt:message key='box_plot_min'/>",
            box_plot_25:"<fmt:message key='box_plot_25'/>",
            box_plot_median:"<fmt:message key='box_plot_median'/>",
            box_plot_75:"<fmt:message key='box_plot_75'/>",
            box_plot_max:"<fmt:message key='box_plot_max'/>",
            box_plot_mean:"<fmt:message key='box_plot_mean'/>",
            series:"<fmt:message key="Scope_Domain" />"

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
        dojo.require("dojox.charting.plot2d.ClusteredBars");
        dojo.require("dojox.charting.themes.Bahamation");
        //   dojo.require("dojox.charting.themes.PlotKit.orange");
        //   dojo.require("dojox.charting.themes.PlotKit.blue");
        dojo.require("dojo.data.ItemFileWriteStore");

        dojo.require("dijit.form.NumberSpinner");
        dojo.require("dijit.form.Textarea");


        dojo.require("dojox.charting.plot2d.Grid");
        //this is to save svg ...
        dojo.require("dojox.gfx.utils");

        dojo.require("dijit._Widget");
        dojo.require("dijit._TemplatedMixin");
        dojo.require("dojox.widget.Standby");
        dojo.require("alpine.layout.StandbySpinner");
        dojo.require("alpine.spinner");

        dojo.require("alpine.props.HadoopFileOperatorPropertyHelper");
        dojo.require("alpine.flow.HadoopOperatorsDataExplorerManager");
        dojo.require("alpine.props.HadoopDataTypeUtil");
        dojo.require("alpine.layout.PopupDialog");

        //this does not work in IE
        //dojo.addOnLoad(result);

        //MINERWEB-877
        try {
            window.moveTo(0, 0);
            window.resizeTo(screen.availWidth, screen.availHeight);
            window.outerWidth = screen.availWidth;
            window.outerHeight = screen.availHeight;
        } catch (e) {
        }

    </script>

    <script type="text/javascript" src="../../js/alpine/alpineConstants.js" charset="utf-8"></script>
    <script type="text/javascript" src="../../js/alpine/logView.js" charset="utf-8"></script>
    <script type="text/javascript" src="../../js/alpine/httpService.js" charset="utf-8"></script>
    <script type="text/javascript" src="../../js/alpine/result.js" charset="utf-8"></script>

    <script type="text/javascript" src="../../js/alpine/common.js"
            charset="utf-8"></script>
    <script type="text/javascript" src="../../js/alpine/dataStore.js"
            charset="utf-8"></script>
    <script type="text/javascript"
            charset="utf-8">
        var ds_result = new httpService();
    </script>
    <script type="text/javascript" src="../../js/alpine/popupComponent.js"></script>
</head>
<body class="soria" onunload="releaseResultTab('tabRoot')" onload="result()" id="coverScopeId">
<div id="tabRoot" width="100%" height="100%">


</div>


<div dojoType="dijit.Dialog" width="400px" height="160px" draggable="false"
     id="dlg_download_report" title="<fmt:message key='Report_Download'/>">

    <table style="width: 390px; height: 100px">
        <tr width="380px">
            <td width="370px">
                <div>
                    <label witdh="100%" id="download_report_label"> </label>
                </div>
            </td>

    </table>

</div>

<div id="scattermatrixchartdlg" dojoType="dijit.Dialog" title="<fmt:message key='Scat_Plot_Martix'/>"
     style="background: #ffffff;overflow:hidden;width:820px;height:600px">
    <div dojoType="dijit.layout.ContentPane" region="center" id="scattermatrixchartContainer"
         style="width:800px;height:500px;overflow:visible;"></div>
    <div dojoType="dijit.layout.ContentPane" region="bottom" style="width:800px">
        <div style="overflow:hidden;width:100%;text-align: right;">
            <button dojoType=dijit.form.Button type="button" baseClass="primaryButton" id="scattermatrixchartdlg_OK"
                    onClick="dijit.byId('scattermatrixchartdlg').hide();">
                <fmt:message key='Done'/>
            </button>
        </div>
    </div>

</div>

<div id="resultchartContainer"></div>

<div dojoType="alpine.layout.PopupDialog" draggable="false" id="chorusCommentDialog" title="Post Comment to Chorus">
    <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 350px; height: 150px">
        <div dojoType="dijit.layout.ContentPane" region="top">
            <div id="chorusCommentContentTop" style="padding-bottom: 15px;"></div>
        </div>
        <div dojoType="dijit.layout.ContentPane" region="center">
            <div id="chorusCommentContent"></div>
        </div>
        <div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter" >
            <button baseClass="cancelButton" valign="bottom" id="chorusComment_cancel" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
            <button baseClass="primaryButton" valign="bottom" id="chorusComment_ok" dojoType="dijit.form.Button" type="button">Post</button>
        </div>
    </div>
</div>
</body>
</fmt:bundle>
</html>
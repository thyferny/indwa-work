<%@ page pageEncoding="UTF-8" %>
<head> <meta http-equiv="X-UA-Compatible" content="chrome=1">

    <link rel="shortcut icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
    <link rel="icon" href="<%=path%>/favicon.ico" type="image/vnd.microsoft.icon">
</head>
<style type="text/css">
	@import "../../js/dojox/grid/resources/soriaGrid.css";
	@import "../../js/dojox/layout/resources/ExpandoPane.css";
	@import "../../js/dojox/form/resources/UploaderFileList.css";
	@import "../../js/dojo/resources/dojo.css";
	@import "../../js/dijit/themes/soria/soria.css";
	@import "../../js/dijit/icons/editorIcons.css";
	@import "../../js/dojox/form/resources/CheckedMultiSelect.css";
	@import "../../js/dojox/grid/enhanced/resources/EnhancedGrid.css";
	@import "../../js/dojox/form/resources/FileInput.css"; 
	@import "../../js/dojox/widget/Toaster/Toaster.css"; 
	@import "../../js/dojox/form/resources/ListInput.css";
	@import "../../js/dojox/layout/resources/ResizeHandle.css";
    @import "../../css/tabs.css" 	;
    @import "../../css/grid.css"     ;
    @import "../../css/bordercontainer.css";
    @import "../../css/accordiancontainer.css";
    @import "../../css/menu.css";
    @import "../../css/login.css";
    @import "../../css/common.css";
    @import "../../css/workBenchStyle.css";
    @import "../../css/breadcrumb.css";
    @import "../../css/ui_update.css";
    @import "../../css/hadoopOperatorPropertysSet.css";
    @import "../../css/d3css.css" 	;
    @import "../../js/codemirror/codemirror.css" 	;
    @import "../../js/codemirror/simple-hint.css" 	;
</style>
<script type="text/javascript">
var dojoConfig = {
	isDebug: true,
	parseOnLoad: true,
	locale: "<%=request.getLocale().getLanguage()%>",
	baseUrl: "../../js/",
    tlmSiblingOfDojo: false,
    packages: [
        { name: "dojo", location: "dojo" },
        { name: "dijit", location: "dijit" },
        { name: "dojox", location: "dojox" },
        { name: "alpine", location: "alpine" }
    ]
};
</script>

<script type="text/javascript" src="../../js/dojo/dojo.js" charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/alpine.js" charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/alpineDijit.js" charset="utf-8"></script>

 
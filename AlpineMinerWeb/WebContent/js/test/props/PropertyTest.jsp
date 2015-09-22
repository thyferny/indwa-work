<html>
<head>
<title>Property Test with Dojo DOH</title>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<style type="text/css">
@import "../../dojo/resources/dojo.css";
</style>
<!-- required for Tooltip: a default dijit theme: -->


<%
	String host = request.getRemoteHost();
	String port = String.valueOf(request.getServerPort());
	String baseURL = "http://" + request.getHeader("Host")+ request.getContextPath();
	String path = request.getContextPath();
	 
	String ts = (String) session.getAttribute("TIME_STAMP");
	String user ="admin" ;
	 	String baseURL2 = "http://" + request.getHeader("Host")
	+ request.getContextPath();
%>
<script type="text/javascript"
	djConfig="parseOnLoad: true ,isDebug: true" src="../../dojo/dojo.js"></script>



<script type="text/javascript" src="../../alpine/common.js"
	charset="utf-8"></script>
<script type="text/javascript" src="../../alpine/dataStore.js"
	charset="utf-8"></script>
	 <!-- 
<script type="text/javascript" src="../../js/alpine/prototype.js"
	charset="utf-8"></script>
  -->
<script type="text/javascript" src="../../alpine/httpService.js"
	charset="utf-8"></script>
<script type="text/javascript" src="./stepwise_test.js" charset="utf-8"></script>

<script type="text/javascript">
  	dojo.require("doh.runner");
	dojo.require("dojo.data.ItemFileReadStore");
	dojo.require("dojo.data.ItemFileWriteStore");
	dojo.require("dojo.io.iframe");
	dojo.require("dojo.window");
 
	dojo.require("dojox.json.ref");
 
	dojo.require("dojox.validate.regexp");
 
 
  	var login ="admin";
  	var baseURL = "<%=baseURL%>";
	var flowBaseURL = baseURL + "/main/flow.do";

	var ds = new httpService(); 
	var alpine;
	if(!alpine){
		alpine = {};		
	}
	
	alpine.WEB_APP_NAME = "<%=path%>";
	alpine.USER = "<%=user%>";
	alpine.TS = "<%=ts%>";
 
	alpine.ResultEvent = 1;
	alpine.FaultEvent = 0;
	//this is for progress, sometime the js  can not get the URL
	alpine.baseURL = "<%=baseURL2%>";
	

	property_test = function() {
		doh.register("stepwise_test", [ {
			name : "stepwise_test",
			timeout : 4000,
			runTest : function() {
				stepwise_test ();
			}
		} ]);

		doh.run();
	};

	dojo.addOnLoad(property_test);
</script>

</head>
<body class="tundra">



</body>

</html>

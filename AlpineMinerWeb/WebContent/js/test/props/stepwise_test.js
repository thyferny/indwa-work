stepwise_test = function() {

	var CurrentFlow = {
		createTime : 1325130581156,
		createUser : alpine.USER,
		groupName : "",
		id : "sample_linear_regression",
		modifiedUser : alpine.USER,
		type : "Personal",
		version : "1"
	};

	var url = baseURL + "/main/property.do?method=getPropertyData" + "&uuid="
			+ "1303205901393" + "&user=" + login;

	var str = dojox.json.ref.toJson(CurrentFlow);
	var opDTO = null;
	dojo.rawXhrPost( {
		url : encodeURI(url),
		postData : str,
		sync : true,
		headers : {
			"Content-Type" : "plain/text",
			"Accept" : "plain/text",
			"TIME_STAMP" : alpine.TS,
			"USER_INFO" : alpine.USER
		},
		load : function(data) {
			opDTO = eval("(" + data + ")");
		},
		error : function(error) {
			alert("Message error.");

		}

	});

	//,

	var classname = "LinearRegressionOperator";

	doh.is(classname, opDTO.classname);
	var props = opDTO.propertyList;
	for ( var i = 0; i < props.length; i++) {
		var prop = props[i];
		if (prop.name == "isStepWise") {
			doh.is(prop.value, "false");
			prop.value = "true";
		}

	}

	str = dojox.json.ref.toJson(opDTO);
	var url = baseURL + "/main/property.do?method=updatePropertyData" + "&user="
			+ alpine.USER;
	dojo.rawXhrPost( {
		url : encodeURI(url),
		postData : str,
		sync : true,
		headers : {
			"Content-Type" : "plain/text",
			"Accept" : "plain/text",
			"TIME_STAMP" : alpine.TS,
			"USER_INFO" : alpine.USER
		},
		load : function(data) {
			CurrentFlow = eval("(" + data + ")").flowInfo;
		}

	});

	var url = baseURL + "/main/property.do?method=getPropertyData" + "&uuid="
			+ "1303205901393" + "&user=" + login;

	str = dojox.json.ref.toJson(CurrentFlow);
	var opDTO = null;
	dojo.rawXhrPost( {
		url : encodeURI(url),
		postData : str,
		sync : true,
		headers : {
			"Content-Type" : "plain/text",
			"Accept" : "plain/text",
			"TIME_STAMP" : alpine.TS,
			"USER_INFO" : alpine.USER
		},
		load : function(data) {
			opDTO = eval("(" + data + ")");
		},
		error : function(error) {
			alert("Message error.");

		}

	});
	var props = opDTO.propertyList;
	for ( var i = 0; i < props.length; i++) {
		var prop = props[i];
		if (prop.name == "isStepWise") {
			doh.is(prop.value, "true");
			prop.value = "false";
		}

	}

	var url = baseURL + "/main/property.do?method=updatePropertyData" + "&user="
			+ alpine.USER;
	str = dojox.json.ref.toJson(opDTO);
	dojo.rawXhrPost( {
		url : encodeURI(url),
		postData : str,
		sync : true,
		headers : {
			"Content-Type" : "plain/text",
			"Accept" : "plain/text",
			"TIME_STAMP" : alpine.TS,
			"USER_INFO" : alpine.USER
		},
		load : function(data) {

		}

	});

};
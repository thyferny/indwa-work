dojo.declare("httpService",null,{
	constructor:function(){
		this.sync = false;
	},

	setSync:function(bool){
		this.sync = bool;
	},


    //called if get/post/postWithLogin completes successfully
    success:function (text, args, callbackpanelid, callback, errorcallback, logincallback) {
        if (args && args.xhr && args.xhr.status != 200) {
        	jstestdriver.console.log("communication error.");
        	fail();
            return;
        }
        if (text && text != "") {
            var obj = eval("(" + text + ")");
            if (obj.error_code == -1) {
                if (logincallback) logincallback(obj);
                else {
                	jstestdriver.console.log("user login off.");
                	fail();
                }
            }
            else if (obj.error_code == -2) {
                if (logincallback) logincallback(obj);
                else {
                	jstestdriver.console.log("user's session was expired.");
                	fail();
                }
            }
            else if (obj.error_code == 9999) {
            	jstestdriver.console.log(obj.message);
            	assertTrue(false);
            } else {
                if (callback) {
                    callback(obj);
                }
            }
        } else {
            if (callback) {
                callback(null);
            }
        }
    },

    //called if get/post/postWithLogin fails
    failure:function(text,args, callbackpanelid,errorcallback)
    {
        if(args&&args.xhr&&args.xhr.status!=200){
        	jstestdriver.console.log("cannot connect to server.");
            if (errorcallback != null) {
                errorcallback(text, args);
            }
        	fail();
            return;
        }
        text = buildErrorMsg(text);
        if (errorcallback != null) {
            errorcallback(text, args);
        } else {
        	jstestdriver.console.log(text);
        }
    	fail();
    } ,

        //add errorcallback to make it more user-friendly
	get : function(url, callback, errorcallback, isSync, callbackpanelid){
        var success = this.success;
        var failure = this.failure;
        url += "&Alpine_Special_Key=ff1335bea314baa82e";
        dojo.xhrGet({
			url : encodeURI(url),
			sync : isSync || false,
			preventCache : true,
			headers : {
				"Content-Type" : "plain/text; charset=utf-8",
				"Accept" : "plain/text",
				"TIME_STAMP": alpine.TS,
				"USER_INFO": alpine.USER
			},
			load : function(text, args)
            {
                success(text,args,callbackpanelid, callback,errorcallback);
            },
			error : function(text, args) {
               failure(text,args,callbackpanelid,errorcallback);
			}
		});
	},
	// use this function to pass special char(&, = etc.) to server side.
	getWithData: function(url, data, callback, errorcallback, isSync, callbackpanelid){
        var success = this.success;
        var failure = this.failure;
        url += "&Alpine_Special_Key=ff1335bea314baa82e";
        dojo.xhrGet({
			url : encodeURI(url),
			sync : isSync || false,
			preventCache : true,
			content: data,
			headers : {
				"Content-Type" : "plain/text; charset=utf-8",
				"Accept" : "plain/text",
				"TIME_STAMP": alpine.TS,
				"USER_INFO": alpine.USER
			},
			load : function(text, args)
            {
                success(text,args,callbackpanelid, callback,errorcallback);
            },
			error : function(text, args) {
               failure(text,args,callbackpanelid,errorcallback);
			}
		});
	},
	
	postWithLogin : function(url, data, callback, errorcallback,logincallback, isSync, callbackpanelid){
        var success = this.success;
        var failure = this.failure;
        var str = dojox.json.ref.toJson(data);
		dojo.rawXhrPost({
			url : encodeURI(url),
			sync : isSync || false,
			postData : str,
			headers : {
				"Content-Type" : "plain/text; charset=utf-8",
				"Accept" : "plain/text",
				"TIME_STAMP": alpine.TS,
				"USER_INFO": alpine.USER
			},
			load : function(text, args) {
                success(text,args,callbackpanelid, callback,errorcallback,logincallback);
            },
			error : function(text, args) {
                failure(text,args,callbackpanelid,errorcallback);
            }
		});
	},
	
	post : function(url, data, callback, errorcallback, isSync, callbackpanelid){
        var success = this.success;
        var failure = this.failure;

        var str = dojox.json.ref.toJson(data);
        url += "&Alpine_Special_Key=ff1335bea314baa82e";
		dojo.rawXhrPost({
			url : encodeURI(url),
			sync : isSync || false,
			postData : str,
			headers : {
				"Content-Type" : "plain/text; charset=utf-8",
				"Accept" : "plain/text",
				"TIME_STAMP": alpine.TS,
				"USER_INFO": alpine.USER
			},
			load : function(text, args) {
                success(text,args,callbackpanelid, callback,errorcallback);
            },
			error : function(text, args) {
                failure(text,args,callbackpanelid,errorcallback);
            }
		});
	},
	upload : function(url, form_name, callback, errorcallback, callbackpanelid, contentfnx){
        if (!contentfnx) contentfnx = 1;
        dojo.io.iframe.send( {
			url : encodeURI(url),
			form : form_name,
			method : "post",
			content : {
				fnx : contentfnx
			},
			timeoutSeconds : 60,
			preventCache : true,
			handleAs : "html",
			handle : function(res, ioArgs) {
                if (res && res.body && res.body.innerHTML) {
					if (callback) {
						var data = eval("(" + res.body.innerHTML + ")");//res.body.innerHTML.evalJSON();
						callback(data);
					}
				}
			},
			error : function(res, ioArgs) {
                if (errorcallback) {
					var data = eval("(" + res.body.innerHTML + ")"); //res.body.innerHTML.evalJSON();
					errorcallback(data);
				}
            	fail();
			}
		});
	}
});

function buildErrorMsg(text) {
	if (dojo.isString(text)) {
		return text;
	}
	try {
		return text.message;
	} catch (e) {
		return alpine.nls.message_unknow_error;
	}
}
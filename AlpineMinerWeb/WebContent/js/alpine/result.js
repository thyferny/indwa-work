/**
 * result.js
 *  This is the js file for result tab container of the process result.
 * @author John zhao
 *
 * Version  Ver 3.0
 *
 * Date     2011-7-4
 *
 * COPYRIGHT   2010 - 2011 Alpine Solutions. All Rights Reserved.
 * */

//this is the global varaible for the i18n use
var nlsStrings = null;
var timerID;
var count = 0;
var LastOperator = null;
var flow_outputs = new Array();
var flow_meta_info = null;//report use
var ds = null;
var isExportingReport = false;
var process_bar_dialog = null;
var result_event_handlers = new Array();
//For  Logistic Regression double click to get  tabcontainer id
var LOGISTIC_REGRESSION_DB_CLICK_TAB_CONTAINER = null;
var LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER = null;
var LinearRegressionGroup_IMPORT_IMG_COUNT = 0;
var LinearRegressionGroup_IMPORT_IMG_MAX_COUNT = 60; //30 set

function addResultTab(id, header, data) {

    if (!dijit.byId(ID_RESULT_TAB)) {

        var container = new dijit.layout.TabContainer({
            style:"width: 100%; height: 100%;"
        }, ID_RESULT_TAB);//" >
        container.startup();
    }

    var div = document.createElement("div");
    div.id = id;
    dojo.byId(ID_RESULT_TAB).appendChild(div);
    closable = false;
    //user can not close the tab...
//	if (id == ID_LOG_VIEW) {
//		closable = false;
//	}
    var tab = new dijit.layout.ContentPane({
        title:header,
        closable:closable
    }, div);

    dijit.byId(ID_RESULT_TAB).addChild(tab);
    //fill the hori...
    tab.style.width = "100%";
    tab.style.height = "100%";
    tab.preload = false;
    //log tab has no data
    if (data) {
        data.isRendered = false;
        data.contentPaneId = tab.id;
    }
    tab.chartData = data;

    var handler = dojo.connect(tab, "onShow", tab, function () {
        if (!isExportingReport) {
            renderOnShow(tab, true);
        }
    });
    result_event_handlers.push(handler);
    return tab;

}


function result() {

    count = 0;
    //show_progress_bar_dilog();

    //clear the last result
    //log can be closed by user
    if (!dijit.byId(ID_LOG_VIEW)) {
        addResultTab(ID_LOG_VIEW, alpine.nls.Result_Log);
    }
    alpine.spinner.showSpinner(ID_LOG_VIEW);
    dojo.byId(ID_LOG_VIEW).innerHTML = "";

    init_export_html_btn();

    //call the server to get the running status each 1 second

    LastOperator = null;
    timerID = setInterval("getMessage()", 1000);
}

function init_export_html_btn() {
    var div = document.createElement("DIV");
    dojo.byId(ID_LOG_VIEW).appendChild(div);
    div.style.top = 2;
    div.align = "left";

    var createHTMLReportBtn = new dijit.form.Button({
        id:'btn_export_html_report',
        disabled:'true',
        baseClass:'primaryButton',
        label:alpine.nls.save_html_report,
        onClick:function () {
            export_HTML_report('tabRoot', false)
        }
    }, dojo.create('div', {}, div));

    require(["alpine/chorus/ChorusCommentUIHelper"], function (chorusCommentHelper){
        if (isChorusRun) {
            var createChorusCommentBtn = new dijit.form.Button({
                id:'btn_post_chorus_comment',
                disabled:'true',
                baseClass:'primaryButton',
                label:'Post Comment/Insight to Chorus',
                onClick:function () {
                    chorusCommentHelper.showCommentDialog();
                }
            }, dojo.create('div', {}, div));
        }

    });



}

function getMessage() {
    var requestUrl = baseURL + "/main/flowRunner.do?method=getMessage&uuid="
        + flowUUID;
    getHttpService().get(requestUrl, function (data) {
        if (!data) {
            if (LastOperator != null) {
                if (!isChorusRun && window.opener && window.opener.alpine) {
                    //keep the animation(switching the icon)...
                    if (typeof window.opener.alpine.flow.WorkFlowRunnerUIHelper.flashOperator == "function") window.opener.alpine.flow.WorkFlowRunnerUIHelper.flashOperator(LastOperator);
                }
            }
            return;
        }
        if (data == "Result not found!") {
            clearInterval(timerID);
            alpine.spinner.hideSpinner(ID_LOG_VIEW);
            enable_export_report_btn(false);
            return;
        }
        if (data.error_code) {
            handle_error_result(data);
            return;
        }
        var result = data.result;
        data = null;

        try {
            if (result) {
                setLog(result);
            }
        } catch (e) {
            popupComponent.alert(alpine.nls.error + ":" + e.description, function () {
                enable_export_report_btn(false);
                clearInterval(timerID);
                alpine.spinner.hideSpinner(ID_LOG_VIEW);
            });
        }
    }, function (data) {
        clearInterval(timerID);
        alpine.spinner.hideSpinner(ID_LOG_VIEW);
    }, false, "");
//	dojo.xhrGet( {
//		url : requestUrl,
//		sync : false,
//		preventCache : true,
//		headers : {
//		"Content-Type" : "plain/text; charset=utf-8",
//			"TIME_STAMP": alpine.TS,
//			"USER_INFO": alpine.USER
//		},
//		load : function(text, args) {
//			if(args&&args.xhr&&args.xhr.status!=200){
//				popupComponent.alert(alpine.nls.can_not_connect_server);
//				return;
//			}
//			if (text && text.length > 0 && text != "") {
//				if (text == "Result not found!") {
//
//					clearInterval(timerID);
//                    alpine.spinner.hideSpinner(ID_LOG_VIEW);
//					//hide_progress_bar_dilog();
//					enable_export_report_btn(false);
//					return;
//				}
//
//				var jsonObj = eval("(" + text + ")");
//				text=null;
//				if(jsonObj.error_code){
//					handle_error_result(jsonObj);
//					return ;
//				}
//
//				var data = jsonObj.result;
//				jsonObj=null;
//
//				try {
//					if (data) {
//						setLog(data);
//					}
//				} catch (e) {
//					popupComponent.alert(alpine.nls.error+":" + e.description, function(){
//						enable_export_report_btn( false);
//						clearInterval(timerID);
//						//hide_progress_bar_dilog();
//                        alpine.spinner.hideSpinner(ID_LOG_VIEW);
//					});
//				}
//				//handle exception
//
//			} else {
//				if (LastOperator != null) {
//					if(window.opener){
//                        //keep the animation(switching the icon)...
//                        if (typeof window.opener.setImgs == "function") window.opener.setImgs(LastOperator);
// 			}
//				}
//			}
//		},
//		error : function(text, args) {
//			if(args&&args.xhr&&args.xhr.status!=200){
//				popupComponent.alert(alpine.nls.can_not_connect_server);
//				return;
//			}
//			console.error(text);
//			clearInterval(timerID);
//			//hide_progress_bar_dilog();
//            alpine.spinner.hideSpinner(ID_LOG_VIEW);
//        }
//	});
}

function enable_export_report_btn(enable) {
    if (dijit.byId("btn_export_html_report")) {
        dijit.byId("btn_export_html_report").set('disabled', '');
        if (dijit.byId("btn_export_chorus_report")) dijit.byId("btn_export_chorus_report").set('disabled', '');

    }
    if (dijit.byId("btn_post_chorus_comment")) {
        dijit.byId("btn_post_chorus_comment").set('disabled', '');
    }
}

function setLog(data) {
    //data is an array or operators output, each operator can have only one output(but can be composited)
    if (!data) {
        return;
    }
    for (var i = 0; i < data.length; i++) {
        var outputData = data[i];
        if (!outputData || outputData == "null" || outputData.uuid != flowUUID) {
            continue;
        }

        if (outputData.output && outputData.output.node_meta_info) {
            flow_outputs[flow_outputs.length] = outputData;//chart,suface,
        }

        if ((outputData.message == MSG_OPERATOR_START || outputData.message == MSG_PROCESS_FINISHED)
            || outputData.message == MSG_OPERATOR_FINISHED
            || outputData.message == MSG_PROCESS_STOP) {
            LastOperator = outputData.operatorname;
            if (!isChorusRun && window.opener && window.opener.alpine) {
                if (typeof window.opener.alpine.flow.WorkFlowRunnerUIHelper.switchFlashOperator == "function") window.opener.alpine.flow.WorkFlowRunnerUIHelper.switchFlashOperator(LastOperator);
            }
            if (outputData.message == MSG_PROCESS_FINISHED
                || outputData.message == MSG_PROCESS_STOP) {

                //switch to the first tab..   this is impotant to show the chart in IE
                if (dijit.byId(ID_LOG_VIEW)) {
                    dijit.byId(ID_RESULT_TAB).selectChild(
                        dijit.byId(ID_LOG_VIEW));
                }
                //stop the running
                clearInterval(timerID);
                timerID = null;

                enable_export_report_btn(false);
                alpine.spinner.hideSpinner(ID_LOG_VIEW);

                //hide_progress_bar_dilog();
                flow_meta_info = outputData.output;
            }
        }
        if (outputData.message == MSG_PROCESS_FINISHED
            || outputData.message == MSG_PROCESS_STOP) {
            if (!isChorusRun && window.opener && window.opener.alpine) {
                if (typeof window.opener.alpine.flow.WorkFlowRunnerUIHelper.afterRun == "function") window.opener.alpine.flow.WorkFlowRunnerUIHelper.afterRun(flowUUID);
            }

            window.opener = null;
        }

        setLogContent(outputData, count + 2);


        //this is for vusualizatio use
        if (outputData.output && outputData.output != "null"
            && outputData.output.out_id && outputData.output.visualData) {
            //get the output pane and set the result
            var outpane = addResultTab(outputData.output.out_id, outputData.output.out_title, outputData.output);

//			fillOutPutPan(outpane, outputData.output);

            //dijit.byId(ID_RESULT_TAB).selectChild(outpane);
            outpane.startup();
            outpane = null;

        }
        if (outputData.message == MSG_PROCESS_ERROR) {
            count++;
        }
        outputData = null;
        count++;
        //get some time ...

    }
    data = null;

}

//will add more visulization here
function fillOutPutPan(outpane, output) {
    // store render status
    output.isRendered = true;
    var panelid = (output.parentOutPut == null || output.parentOutPut.contentPaneId == null) ? output.out_id : output.parentOutPut.contentPaneId;
    if (output.visualType == VISUAL_TYPE_DATATABLE) {
        fillOutPaneDataTable(outpane, output);
    } else if (output.visualType == VISUAL_TYPE_BARCHART) {
        outpane.domNode.style.overflow = "hidden";
        if (output.visualData.height > outpane.domNode.offsetHeight) {
            outpane.domNode.style.height = (output.visualData.height + 150);
        } else {
            outpane.domNode.style.height = "auto";
        }
        if (output.visualData.width > outpane.domNode.offsetWidth) {
            outpane.domNode.style.width = (output.visualData.width + 50);
            outpane.domNode.parentNode.style.overflow = "auto";
        } else {
            outpane.domNode.style.width = "100%";
            outpane.domNode.parentNode.style.overflow = "auto";
        }
        var container = dojo.create("div", {style:"overflow:hidden"}, outpane.domNode);
        chartComponent = initializeSwitch(container, (output.parentOutPut == null || output.parentOutPut.contentPaneId == null) ? output.out_id : output.parentOutPut.contentPaneId, output);//include plot, x axis, y axis.
        var chartContainer = dojo.create("div", {style:"overflow:hidden;"}, container);
        fillOutPaneBarChart(chartContainer, output, chartComponent);
    } else if (output.visualType == VISUAL_TYPE_COMPOSITE) {// already initialized if Exporting
        fillOutPaneComposite(outpane, output);
    } else if (output.visualType == VISUAL_TYPE_TEXT) {
        fillOutPaneText(outpane, output);
    }
    else if (output.visualType == VISUAL_TYPE_CLUSRTER_CHART) {
        fillOutPaneLayeredClusterChart(outpane, output);
    }
    else if (output.visualType == VISUAL_TYPE_LAYERED) {
        fillOutPaneLayered(outpane, output);
    } else if (output.visualType == TYPE_LINE_CHART) {
        fillOutPaneLine(outpane, output, panelid);
    } else if (output.visualType == TYPE_SCATTER_CHART) {
        fillOutPaneScatter(outpane, output);
    } else if (output.visualType == 11) {
        fillOutPaneScatter4ScatterPlotMatrix(outpane, output, panelid);
    } else if (output.visualType == 12) {
        scatterPlotChart4OperatorMenu(outpane, output, panelid);
    } else if (output.visualType == VISUAL_TYPE_TREE) {
        fillOutPaneTreeChart(outpane, output);
    } else if (output.visualType == VISUAL_TYPE_NETWORK) {
        fillOutPaneNetWorkChart(outpane, output);
    } else if (output.visualType == TYPE_TABLE_GROUPED) {
        fillOutPaneClusterProfileChart(outpane, output);
    } else if (output.visualType == TYPE_BOXANDWHISKER) {
        //fillOutPaneBoxAndWhisker(outpane, output, panelid);
        alpine.visual.BoxPlotGraph.fillOutPaneBoxAndWhisker(outpane, output, panelid);
    }
    else if (output.visualType == TYPE_SCATTER_MATRIX) {
        fillOutPaneScatterMatrix(outpane, output, panelid);
    }
    else if (output.visualType == VISUAL_TYPE_SCATT_PREVIEW) {
        // change to gart's new method later..
        fillOutPaneScatterPreview(outpane, output);
        // fillOutPaneScatter(outpane, output);
    } else if (output.visualType == 199 || output.visualType == 200) {
        //LogisticRegression group by is not null or LinearRegression group by is not null
        //Data grid can double click
        //fillOutPaneDataTable(outpane, output);
        fillOutPaneDataTable4GridCanDBClick(outpane, output);
    }


}

function createSubTab4CompositeOut(tabContainer, id, title, output) {
    var div = document.createElement(TAG_DIV);
    div.id = id;
    tabContainer.domNode.appendChild(div);
    var content = new dijit.layout.ContentPane({
        //id:id,
        title:title
        //,    content: title
    }, div);
    //this is so important .otherwise the domnode is created but display =none, can not see
    content.preload = true;
    content.chartData = output;


    output.isRendered = false;
    output.contentPaneId = content.id;

    var handler = dojo.connect(content, "onShow", content, function () {
        if (!isExportingReport) {
            renderOnShow(content, true);
        }
    });
    result_event_handlers.push(handler);
    return content;

}

function fillOutPaneText(outpane, output) {

    if (output.visualData && output.visualData.text) {
        var text = output.visualData.text;

        outpane.setContent("<textarea rows=\"36\" cols=\"120\">" + text
            + "</textarea>");

        outpane.startup();
    }

}

function fillOutPaneLayered(outpane, output) {

    var keyLable = output.keyLabel;
    var keys = output.keys;
    var div = document.createElement(TAG_DIV);

    //div.style.width = "100%";
    //div.style.height = "100%";
    var comboDiv = document.createElement(TAG_DIV);
    var contentDiv = document.createElement(TAG_DIV);
    contentDiv.style.width = "100%";
    contentDiv.style.height = "100%";
    //div.appendChild(comboDiv);
    //div.appendChild(contentDiv);
    outpane.containerNode.appendChild(comboDiv);
    outpane.containerNode.appendChild(contentDiv);
    //outpane.setContent(div);
    var label = document.createElement("label");
    label.innerHTML = ("<a>" + keyLable + "</a>");
    comboDiv.appendChild(label);


    var mySelect = new dijit.form.Select({
        maxHeight:"400"
    });
    comboDiv.appendChild(mySelect.domNode);
    outpane.layered_select = mySelect;


    var options = new Array();
    for (var i = 0; i < keys.length; i++) {
        options.push({
            label:keys[i],
            value:keys[i]
        });

    }
    mySelect.set("options", options);
    mySelect.layered_keys = keys;
    mySelect.layered_backebd_triggered = false;

    var contentDivPane = new dijit.layout.ContentPane({}, contentDiv);
    contentDivPane.style.width = "100%";
    contentDivPane.style.height = "100%";
    contentDivPane.preload = true;


    //this is for reportgeneration use ...
    output.contentDivPane = contentDivPane;
    output.contentDiv = contentDiv;
    mySelect.outpane = outpane;
    outpane.is_initing = true;
    var visualDatas = output.visualData;
    if (visualDatas && visualDatas.length > 0 && keys.length > 0) {

        mySelect.contentDiv = contentDiv;
        mySelect.contentDivPane = contentDivPane;
        mySelect.visualDatas = visualDatas;

        mySelect.output = output;


        var vData = visualDatas[0][keys[0]];
        //	fillOutPutPan(contentDivPane,vData);
        output.currentLayeredData = vData;

        //	contentDivPane.startup();


        var selectOutKey = function () {
            if (!this.outpane || !this.outpane.chartData || !this.outpane.chartData.isRendered || (this.outpane.is_initing == true)) {
                return;
            }


            var value = this.get("value");
            var selectedIndex = dojo.indexOf(this.layered_keys, value);
            var vData = this.visualDatas[selectedIndex][value];


            if (isExportingReport == false) {

                var contentDiv = this.contentDiv;
                var contentDivPane = this.contentDivPane;

                //find the brother...
                var outpane = this.outpane;
                //only manually select will cause the progress bar
                if (this.layered_backebd_triggered == false) {
                    var tabContainer = outpane.getParent();
                    tabContainer.init_layzred_key = value;
                    //show_progress_bar_dilog();

                    vData.parentOutPut = output;
                    //	window.setTimeout(function(){
                    fillLayeredContent(contentDiv, contentDivPane, vData);
                    //find the brother...
                    //	if(outpane.layered_select&&outpane.layered_select.layered_backebd_triggered==false){
                    //	 hide_progress_bar_dilog();
                    //	}
                    //	window.setTimeout(function(){

                    if (tabContainer) {
                        var brothers = tabContainer.getChildren();
                        for (var i = 0; i < brothers.length; i++) {
                            var bPane = brothers[i];
                            if (this.outpane != bPane && bPane.chartData && bPane.chartData.visualType == VISUAL_TYPE_LAYERED) {
                                refreshLayeredPaneWithNewKey(bPane, value);
                            }
                        }
                    }
                    //	}, 10);

                    //	}, 10);
                } else { //now is called by other's select's onchange...

                    vData.parentOutPut = output;
                    fillLayeredContent(contentDiv, contentDivPane, vData);

                    //restre the default value
                    if (outpane.layered_select) {
                        outpane.layered_select.layered_backebd_triggered = false;
                    }
                }

            }

            //this is for reportgeneration use ...
            this.output.currentLayeredData = vData;

        };
        var handler = dojo.connect(mySelect, "onChange", mySelect, selectOutKey);
        result_event_handlers.push(handler);
        var tabContainer = outpane.getParent();
        if (tabContainer.init_layzred_key && dojo.indexOf(keys, tabContainer.init_layzred_key) > -1) {
            mySelect.set("value", tabContainer.init_layzred_key);
        }
        else {
            tabContainer.init_layzred_key = keys[0];
            mySelect.set("value", keys[0]);
        }


    }
    contentDivPane.startup();
    outpane.is_initing = false;
}

function fillOutPaneLayeredClusterChart(outpane, output) {
    var keyLable = output.keyLabel;
    var keys = output.keys;
    var div = document.createElement(TAG_DIV);
    div.style.width = "100%";
    div.style.height = "100%";
    var comboDiv = document.createElement(TAG_DIV);
    var contentDiv = document.createElement(TAG_DIV);
//	contentDiv.style.width = "100%";
    contentDiv.style.height = "100%";
    contentDiv.style.overflow = "hidden";
    div.appendChild(comboDiv);

    div.appendChild(contentDiv);
    var label = document.createElement("label");
    label.innerHTML = ("<a>" + keyLable + "</a>");
    comboDiv.appendChild(label);

    var mySelect = document.createElement("select");

    for (var i = 0; i < keys.length; i++) {
        var option = document.createElement("option");
        option.value = keys[i];
        option.appendChild(document.createTextNode(keys[i]));
        mySelect.appendChild(option);
    }

    comboDiv.appendChild(mySelect);

    var contentDivPane = new dijit.layout.ContentPane({}, contentDiv);
    contentDivPane.style.width = "100%";
    contentDivPane.style.height = "100%";
    contentDivPane.preload = true;
    outpane.setContent(div);

    //this is for reportgeneration use ...
    output.contentDivPane = contentDivPane;
    output.contentDiv = contentDiv;

    var visualDatas = output.visualData;
    if (visualDatas && visualDatas.length > 0 && keys.length > 0) {
        var vData = visualDatas[0][keys[0]];
        fillOutPutPan(contentDivPane, vData);
        output.currentLayeredData = vData;

        //	contentDivPane.startup();

        var selectOutKey = function () {
            var value = mySelect.value;
            var vData = visualDatas[mySelect.selectedIndex][value];


            if (isExportingReport == false) {

//				show_progress_bar_dilog();
                window.setTimeout(function () {
                    fillLayeredContent(contentDiv, contentDivPane, vData);
//					 hide_progress_bar_dilog();

                }, 300);
            }


            //this is for reportgeneration use ...
            output.currentLayeredData = vData;

        };
        if (window.addEventListener) // Mozilla, Netscape, Firefox
        {
            mySelect.addEventListener('change', selectOutKey, false);
        } else// IE
        {
            mySelect.attachEvent('onchange', selectOutKey);
        }
        //make it refresh !!!  ? not working
        mySelect.options[0].selected = true;
    }
    contentDivPane.startup();

}

function refreshLayeredPaneWithNewKey(newPane, newKey) {
    if (newKey && newPane.chartData && newPane.chartData.visualType == VISUAL_TYPE_LAYERED) {
        var select = newPane.layered_select;
        //in dataexplorer .jsp select could be undefined...
        if (select && select.layered_keys && (dojo.indexOf(select.layered_keys, newKey) > -1)) {

            if (select.get("value") && select.get("value") != newKey) {
                select.layered_backebd_triggered = true;
                select.set("value", newKey);//will triger the selectOutKey method...
            }
        }

    }

}
//for report use ,because it use the lazy load...
function fillLayeredContent(contentDiv, contentDivPane, vData) {
    contentDivPane.destroyRecursive(true);

    contentDivPane = null;
    contentDiv.innerHTML = "";
    contentDivPane = new dijit.layout.ContentPane({id:new Date().getTime()}, contentDiv);
    contentDivPane.preload = true;
    //user interaction

    fillOutPutPan(contentDivPane, vData);


}

function fillOutPaneComposite(outpane, output) {
    var subOutPut = output.visualData;
    // new tab container in outpane

    var tabContainer = new dijit.layout.TabContainer({
        id:TAG_PREFIX_OUTPUT_CHILD + output.out_id + Math.random(),
        style:"height: 100%; width: 100%;"
    }, document.createElement(TAG_DIV));

    var firstPane = null;
    for (var i = 0; i < subOutPut.length; i++) {
        var output = subOutPut[i];

        //add by Will
        if (output.visualType == 199) {
            LOGISTIC_REGRESSION_DB_CLICK_TAB_CONTAINER = tabContainer.get('id');
        }
        if (output.visualType == 200) {
            LINEAR_REGRESSION_DB_CLICK_TAB_CONTAINER = tabContainer.get('id');
        }

        var subpane = createSubTab4CompositeOut(tabContainer, output.out_id, output.out_title, output);
        tabContainer.addChild(subpane);

//		fillOutPutPan(subpane, output);
//		if (int == 0) {
//			firstPane = subpane;
//		}
//		tabContainer.selectChild(subpane);

    }
//	tabContainer.selectChild(firstPane);
    tabContainer.startup();
    outpane.setContent(tabContainer.domNode);

}

function fillVisualErrorMessages(div, messages) {
    fillVisualMessages(div, messages, "DD0000");
}

function fillVisualMessages(div, messages, colorStr) {
    if (messages && div) {
        for (var i = 0; i < messages.length; i++) {
            var div1 = document.createElement("DIV");
            div1.style.top = (i + 1) * 14;

            // red is error
            div1.style.color = colorStr;
            div1.style.left = 8;

            div1.innerHTML = messages[i];
            div.appendChild(div1);

        }
    }
}

function newNameValueArray(name, value) {
    var array = new Array();
    array.push(name);
    array.push(value);
    return array;
}

// FlowResult (AnalyticFlowMetaInfo,OperatorResult (AnalyticNodeMetaInfo
// ,OperatorOutput))
var flowResult = null;
var publishToChorus = false;

function export_HTML_report(tabid, shouldPublishToChorus) {
    isExportingReport = true;
    alpine.spinner.showSpinner("tabRoot");
    //show_progress_bar_dilog();
    allSVGoutput = null;
    allLegendOutput = null;
    allLegendLength = null;
    svgCountArray = null;
    //first time
//	  if(!flowResult){
    flowResult = null;
    initializeReportTabs(tabid);
    flowResult = generateReportResult();
    if (shouldPublishToChorus) publishToChorus = true;
    else publishToChorus = false;

    do_export_report();
//	  }else{
//			//all svg is ready...
//			var requestUrl = baseURL + "/main/flow.do?method=exportHTMLResult"
//			+ "&flowName=" + flowName;
//
//			getHttpService().post(requestUrl, flowResult, exportHTMLReportCallBack, null);
//	  }

}
var allSVGoutput = null;
var allLegendOutput = null;
var allLegendLength = null;
var svgCountArray = null;
function do_export_report() {
    if (allSVGoutput) {
        for (var i = 0; i < allSVGoutput.length; i++) {
            if (!allSVGoutput[i].svg) {
                return;
            }
        }
    }
    if (allLegendOutput) {
        for (var i = 0; i < allLegendOutput.length; i++) {
            if (allLegendOutput.length == 0 || (allLegendOutput[i] && allLegendOutput[i].length != allLegendLength[i])) {
                return;
            }
        }
    }
    //if layered ...
    if (svgCountArray) {
        for (var i = 0; i < svgCountArray.length; i++) {
            if (svgCountArray.length == 0 || (svgCountArray[i] && svgCountArray[i] != 0)) {
                return;
            }
        }
    }
    if (!flowResult) {
        return;
    }
    var isIE = false;
    if (dojo.isIE) {
        isIE = true;
    }
    //all svg is ready...
    var requestUrl = baseURL + "/main/flow.do?method=exportHTMLResult"
        + "&flowName=" + flowName
        + "&isIE=" + isIE
        + "&forChorus=" + publishToChorus;
    flowResult.isIE = isIE;

    getHttpService().post(requestUrl, flowResult, exportHTMLReportCallBack, null);
}
function getHttpService() {
    if (!ds) {
        ds = new httpService();
    }
    return ds;
}

function initializeReportTabs(tabid) {
    var firstLevelTabs = dijit.byId(tabid).getChildren();
    var deferred = new dojo.Deferred();
    initializeTabContainer(deferred, firstLevelTabs, 1);
    deferred.resolve();

    function initializeTabContainer(deferred, levelTabs, start) {
        for (var i = start; i < levelTabs.length; i++) {
            if (levelTabs[i].chartData.visualType == VISUAL_TYPE_COMPOSITE) {
                if (levelTabs[i].chartData && !levelTabs[i].chartData.isRendered) {
                    fillOutPutPan(levelTabs[i], levelTabs[i].chartData);//make tab initialized by force.
                }
                deferred.then(dojo.hitch(levelTabs[i], function () {
                    this.getParent().forward();// just move the tab container pointer to current tab.
                }));
                initializeTabContainer(deferred, levelTabs[i].getChildren()[0].getChildren(), 0);

                continue;
            }
            var scope = {
                index:i,
                tabs:levelTabs,
                current:levelTabs[i]
            };

            deferred.then(dojo.hitch(scope, function () {
                if (this.index != 0) {
                    this.current.getParent().forward();//move the tab container pointer to current tab.
                }
                renderOnShow(this.current, false);
            }));
        }
    }
}

function initializeTab(parentTab, tab) {
    return parentTab.selectChild(tab, false);
}

function generateReportResult() {
    var flowResult = {};

    if (flow_meta_info) {// [][]

        flowResult.flowMetaInfo = flow_meta_info;
    }

    var operatorResults = new Array();
    if (!flow_outputs) {
        return;
    }

    var length = flow_outputs.length;
    for (var i = 0; i < length; i++) {
        var operatorResult = {};

        var output = flow_outputs[i].output;
        if (!output) {
            continue;
        }
        var node_metaInfo = output.node_meta_info;
        if (node_metaInfo) {
            var new_node_metaInfo = new Array();
            operatorResult.nodeMetaInfo = node_metaInfo;


        }
        var operatorOutput = {};
        fillOperatorResult(operatorOutput, output);
        operatorResult.operatorOutput = operatorOutput;
        operatorResult.name = output.out_title;

        if (output.operator_input) {
            operatorResult.operatorInput = output.operator_input;
        }
        operatorResults[i] = operatorResult;

        // dojo_cluster_table , dojo_surface this 2 need save svg pic...
        // var svg_text = ;
    }
    flowResult.operatorResults = operatorResults;
    return flowResult;
}
function exportHTMLReportCallBack(reportFileURL) {


    if (reportFileURL.error_code) {
        handle_error_result(reportFileURL);
        return;
    }

    if (publishToChorus === true) {
        publish_chorus_path = reportFileURL;
        dijit.byId("publish_insight_to_chorus").show();
        return;
    }

//	if(dojo.isIE&&first_time_report){//IE will generate the svg in the second call, don't know why
//		first_time_report = false;
//		export_HTML_report();
//	}
//	else{
    if (reportFileURL.indexOf(".zip") < 0) {
        popupComponent.alert(alpine.nls.error + ":" + reportFileURL);
        return;
    }
    //form linux or windows server is always use diff /

    if (reportFileURL.lastIndexOf("/") > 0) {
        var fileName = reportFileURL.substring(reportFileURL.lastIndexOf("/") + 1, reportFileURL.length);
    } else {
        fileName = reportFileURL.substring(reportFileURL.lastIndexOf("\\") + 1, reportFileURL.length);
    }
    //hide_progress_bar_dilog();
    alpine.spinner.hideSpinner("tabRoot");
    isExportingReport = false;

    var filePath = reportFileURL.replace('/temp_report/', '');

    filePath = filePath.replace(fileName, '');

    var servlet_url = baseURL + "/CommonFileDownLoaderServlet?downloadFileName=" + fileName + "&tempType=temp_report&filePath=/" + filePath;
    if (dojo.isIE) {
        //window.open("downLoadResult.jsp?fileName="+fileName+"&filePath="+filePath);
        var openHandler = window.open("", "", "height=250, width=250,toolbar=no,menubar=no");
        openHandler.document.write("<html>");
        openHandler.document.write("<head>");
        openHandler.document.write("<script type='text/javascript'>");
        openHandler.document.write("function doLoad(){");
        openHandler.document.write("var servlet_url = '" + servlet_url + "';");
        openHandler.document.write("location.href = servlet_url");
        openHandler.document.write("}");
        openHandler.document.write("</script>");
        openHandler.document.write("<head>");

        openHandler.document.write("</head>");
        openHandler.document.write("<body onload=doLoad()>");
        openHandler.document.write("<a href='" + servlet_url + "'>" + alpine.nls.resultdownloadtip + "</a>");
        openHandler.document.write("</body>");
        openHandler.document.write("</html>");
        openHandler.document.close();
        openHandler.location.href = servlet_url;

    } else {
        window.location.href = servlet_url;
    }
    return false;

//		var download_url = baseURL  + reportFileURL;
//		var str =alpine.nls.save_as+"\n"
//				+ "<a href="
//			+ download_url
//			+ "><u><i><font color='blue'>" +fileName + "</font></i></u></a>";
//	
//	 		dojo.html.set(dojo.byId("download_report_label"), str);
//			dijit.byId("dlg_download_report").show();
//	}

}

//special for result
function fillOperatorResult(operatorOutput, output) {
    if (output.isRendered == false) {
        fillOutPutPan(dijit.byId(output.contentPaneId), output);
    }

    var visualData = output.visualData;
    // some opeator have no output!!!
    if (!visualData) {
        return;
    }
    operatorOutput.name = output.out_title;
    var visualData = output.visualData;

    if (output.visualType == VISUAL_TYPE_TREE) {
        rectList = output.rectList;
        open_all_tree_node(rectList);
    }

    if (output.d3_svg) {
        console.log("!!!!!!!!!!!!!!!!!!!!!!!!!!!: this is a d3_svg!");
        operatorOutput.type = TYPE_CHART;// let server to know what's kind of output.
        operatorOutput.svg = cleanSVG((new XMLSerializer()).serializeToString(output.d3_svg));
        if (output.d3_legend) {
            operatorOutput.svg = operatorOutput.svg;
            if (output.d3_legend) {
                var items = [];
                var exportLegend = [],
                    exportLegendLabels = [];
                if (output.visualType == VISUAL_TYPE_BARCHART) {
                    items = output.visualData.series;
                } else if (output.visualType == TYPE_LINE_CHART) {
                    items = output.visualData.lines;
                } else if (output.visualType == TYPE_BOXANDWHISKER) {
                    items = output.visualData.boxWhiskers;
                }
                for (var i = 0; i < items.length; i++) {
                    if (items[i].active == true) {
                        exportLegend.push(output.d3_legend[i]);
                        exportLegendLabels.push(output.d3_legendlabel[i]);
                    }
                }

                operatorOutput.svg_legend = exportLegend;
                operatorOutput.svg_legend_labels = exportLegendLabels;
            }
        }
        do_export_report();
        return;
    }

    if (output.dojo_surface) {
        operatorOutput.type = TYPE_CHART;

        var deferred = dojox.gfx.utils.toSvg(output.dojo_surface);
        if (!allSVGoutput) {
            allSVGoutput = new Array();
        }
        allSVGoutput.push(operatorOutput);

        deferred.addCallback(function (svg) {
            operatorOutput.svg = svg;
            do_export_report();

        });


        deferred.addErrback(function (error) {
            popupComponent.alert(alpine.nls.error + ":" + error);
        });

        //dojo charting have seperated legend for charting...
        if (output.dojo_surface_legend) {
            operatorOutput.svg_legend = new Array();
            if (!allLegendOutput) {
                allLegendOutput = new Array();
                allLegendLength = new Array();
            }
            allLegendOutput.push(operatorOutput.svg_legend);

            allLegendLength.push(output.dojo_surface_legend.length);
            for (var i = 0; i < output.dojo_surface_legend.length; i++) {

                var deferred = dojox.gfx.utils.toSvg(output.dojo_surface_legend[i]);
                deferred.addCallback(function (svg) {

                    operatorOutput.svg_legend.push(svg);
                    do_export_report();

                });


            }


            operatorOutput.svg_legend_labels = output.dojo_legend_labels;
        }
    } else if (output.visualType == TYPE_SCATTER_CHART || output.visualType == 11) { //only linearRegression group by use
        if (LinearRegressionGroup_IMPORT_IMG_COUNT < LinearRegressionGroup_IMPORT_IMG_MAX_COUNT) {
            var contentPane = new dijit.layout.ContentPane({}, dojo.create("div"));
            dojo.place(contentPane.domNode, "resultchartContainer", "only");
            //var contentPane = dojo.create("div",{style:"display:none"});
            if (output.visualType != 11) {
                fillOutPaneScatter(contentPane, output);
                operatorOutput.type = TYPE_CHART;
                var deferred = dojox.gfx.utils.toSvg(output.dojo_surface);
                var deferred = new Deferred();
                if (!allSVGoutput) {
                    allSVGoutput = new Array();
                }
                allSVGoutput.push(operatorOutput);

                deferred.addCallback(function (svg) {
                    operatorOutput.svg = svg;
                    do_export_report();

                });


                deferred.addErrback(function (error) {
                    popupComponent.alert(alpine.nls.error + ":" + error);
                });

                //dojo charting have seperated legend for charting...
                if (output.dojo_surface_legend) {
                    operatorOutput.svg_legend = new Array();
                    if (!allLegendOutput) {
                        allLegendOutput = new Array();
                        allLegendLength = new Array();
                    }
                    allLegendOutput.push(operatorOutput.svg_legend);

                    allLegendLength.push(output.dojo_surface_legend.length);
                    for (var i = 0; i < output.dojo_surface_legend.length; i++) {

                        var deferred = dojox.gfx.utils.toSvg(output.dojo_surface_legend[i]);
                        deferred.addCallback(function (svg) {

                            operatorOutput.svg_legend.push(svg);
                            do_export_report();

                        });
                    }
                    operatorOutput.svg_legend_labels = output.dojo_legend_labels;
                }

            } else {
                fillOutPaneScatter4ScatterPlotMatrix(contentPane, output, LinearRegressionGroup_IMPORT_IMG_COUNT);
                operatorOutput.type = TYPE_CHART;
                if (output.d3_svg) {
                    operatorOutput.svg = cleanSVG((new XMLSerializer()).serializeToString(output.d3_svg));
                }
            }
            //destroy contentpane widget
            contentPane.destroyRecursive();
            LinearRegressionGroup_IMPORT_IMG_COUNT = LinearRegressionGroup_IMPORT_IMG_COUNT + 1;
        } else {
            operatorOutput.type = VISUAL_TYPE_TEXT;
            //operatorOutput.text = visualData.text;
            operatorOutput['text'] = '<span style="font-size:10px; font-weight:bolder; color:#FF0066">' + alpine.nls.download_result_groupby_tip + '</span>';
        }
    } else if (output.visualType == TYPE_SCATTER_MATRIX) {

        operatorOutput.type = TYPE_SCATTER_MATRIX;
        var tableData = output.tableData;
        //make surface to svg...
        var svgCount = 0;

        for (var i = 0; i < tableData.length; i++) {
            var rowData = tableData[i];
            for (var j = 0; j < rowData.length; j++) {
                if (output.tableGroupCellType[i][j] == "svg") {
                    var surface = rowData[j];
                    //avoid duplicated  changed

                    if (dojo.isString(surface) == false) {
                        svgCount = svgCount + 1;

                    }


                }
            }


        }

        if (svgCount > 0) {
            if (!svgCountArray) {
                svgCountArray = new Array();
            }

            svgCountArray.push(svgCount);

            var index = svgCountArray.length - 1;
            for (var i = 0; i < tableData.length; i++) {
                var rowData = tableData[i];
                for (var j = 0; j < rowData.length; j++) {
                    if (output.tableGroupCellType[i][j] == "svg") {
                        var surface = rowData[j];
                        //avoid duplicated  changed

                        if (dojo.isString(surface) == false) {

                            var deferred = dojox.gfx.utils.toSvg(surface);
                            var callBackScope = {tableData:tableData, i:i, j:j, index:index};

                            deferred.addCallback(dojo.hitch(callBackScope, function (svg) {

                                this.tableData[this.i][this.j] = svg;
                                if (svgCountArray && this.index > -1) {
                                    svgCountArray[this.index] = svgCountArray[this.index] - 1;
                                }
                                do_export_report();


                            })
                            );


                        }


                    }
                }


            }
        }
        operatorOutput.tableGroupCellType = output.tableGroupCellType;//text,svg
        operatorOutput.tableData = tableData;

        // ???
    }

    else if (output.dojo_cluster_table) {

        operatorOutput.type = TYPE_TABLE_GROUPED;
        var tableData = output.tableData;
        //make surface to svg...
        var svgCount = 0;

        for (var i = 0; i < tableData.length; i++) {
            var rowData = tableData[i];
            for (var j = 0; j < rowData.length; j++) {
                if (output.tableGroupColumnType[j] == "svg") {
                    var surface = rowData[j];
                    //avoid duplicated  changed

                    if (dojo.isString(surface) == false) {
                        svgCount = svgCount + 1;

                    }


                }
            }


        }

        if (svgCount > 0) {
            if (!svgCountArray) {
                svgCountArray = new Array();
            }

            svgCountArray.push(svgCount);

            var index = svgCountArray.length - 1;
            for (var i = 0; i < tableData.length; i++) {
                var rowData = tableData[i];
                for (var j = 0; j < rowData.length; j++) {
                    if (output.tableGroupColumnType[j] == "svg") {
                        var surface = rowData[j];
                        //avoid duplicated  changed

                        if (dojo.isString(surface) == false) {

                            var deferred = dojox.gfx.utils.toSvg(surface);
                            var callBackScope = {tableData:tableData, i:i, j:j, index:index};

                            deferred.addCallback(dojo.hitch(callBackScope, function (svg) {

                                this.tableData[this.i][this.j] = svg;
                                if (svgCountArray && this.index > -1) {
                                    svgCountArray[this.index] = svgCountArray[this.index] - 1;
                                }
                                do_export_report();


                            })
                            );


                        }


                    }
                }


            }
        }
        operatorOutput.tableGroupHeader = output.tableGroupHeader;
        operatorOutput.tableGroupColumnType = output.tableGroupColumnType;//text,svg
        operatorOutput.tableData = tableData;

        // ???
    } else if (output.visualType == VISUAL_TYPE_TEXT) {
        operatorOutput.type = VISUAL_TYPE_TEXT;
        operatorOutput.text = visualData.text;
    } else if (output.visualType == VISUAL_TYPE_DATATABLE || output.visualType == 199 || output.visualType == 200) {
        if (visualData.tableName) {
            operatorOutput.tableName = visualData.tableName;
        }
        var items = visualData.items;
        var columns = visualData.columns;

        var tableData = new Array();
        //table head
        tableData.push(columns);

        if (items) {
            for (var i = 0; i < items.length; i++) {
                var item = items[i];
                var rowData = new Array();
                for (var j = 0; j < columns.length; j++) {
                    rowData.push(item[columns[j]]);
                }
                tableData.push(rowData);
            }
        }
        operatorOutput.tableData = tableData;
        operatorOutput.type = VISUAL_TYPE_DATATABLE;
        //tableType: table ,view ,fakeTable (this will show in the report!!!)
    }

    else if (output.visualType == VISUAL_TYPE_COMPOSITE) {

        operatorOutput.type = VISUAL_TYPE_COMPOSITE;
        var subOutPut = output.visualData;
        if (subOutPut) {
            var subResultoutPuts = new Array();
            for (var i = 0; i < subOutPut.length; i++) {
                var output = subOutPut[i];
                if (output.isGenerateReport
                    && output.isGenerateReport == true) {
                    if (output.isRendered == false) {
                        fillOutPutPan(dijit.byId(output.contentPaneId), output);
                    }
                    var subResultOutPut = {};
                    fillOperatorResult(subResultOutPut, output);
                    subResultoutPuts[i] = subResultOutPut;
                }
            }
            operatorOutput.outPuts = subResultoutPuts;
        }

    }

    else if (output.visualType == VISUAL_TYPE_LAYERED
        && output.isGenerateReport
        && output.isGenerateReport == true) {
        operatorOutput.type = VISUAL_TYPE_COMPOSITE;
        var subOutPut = output.visualData;
        var keys = output.keys;
        if (subOutPut) {
            var subResultoutPuts = new Array();
            var old_currentLayeredData = output.currentLayeredData;
            for (var i = 0; i < subOutPut.length; i++) {

                var vData = subOutPut[i][keys[i]];
                var subResultOutPut = {};
                if (vData.visualType != VISUAL_TYPE_DATATABLE
                    && vData.visualType != VISUAL_TYPE_TEXT) {
                    fillLayeredContent(output.contentDiv, output.contentDivPane, vData);

                }

                fillOperatorResult(subResultOutPut, vData);
                subResultoutPuts[i] = subResultOutPut;


            }
            //restore the current...
            //	if(vData.visualType!=VISUAL_TYPE_DATATABLE
            //		&&vData.visualType!=VISUAL_TYPE_TEXT){
            if (old_currentLayeredData) {
                fillLayeredContent(output.contentDiv, output.contentDivPane, old_currentLayeredData);
            }
            //	}
            operatorOutput.outPuts = subResultoutPuts;
        }

    }
    //else if()
    //if array...
    //operatorOutput.
}


function setOutputLegendLabels(output, legend) {
    var originLegends = legend.legends;
    if (originLegends) {
        var legendLables = new Array();
        for (var i = 0; i < originLegends.length; i++) {
            var td = originLegends[i];
            if (dojo.isIE) {
                legendLables.push(td.outerText);
            } else {
                legendLables.push(td.textContent);
            }
        }
        output.dojo_legend_labels = legendLables;

    }
}


//function show_progress_bar_dilog(containerID){
//    if (containerID) console.log ("should show the spinner for: " + containerID) ;
//	progressBar.showLoadingBar();
//}

//function hide_progress_bar_dilog(){
//	progressBar.closeLoadingBar();
//}

//this is used for vire saved result
function show_flow_result() {
    var requestUrl = baseURL
        + "/main/flow.do?method=getFlowResultInfoData&flowName="
        + flowName + "&uuid=" + flowUUID;

    getHttpService().get(requestUrl, function (data) {
        if (data && data.error_code) {
            handle_error_result(data);
            return;
        } else if (!data) {
            popupComponent.alert(alpine.nls.result_not_found);
        }
        else {
            var logs = data.logs;
            var logPane = fill_flow_log(logs);
            var outputs = data.outputs;
            //MINERWEB-825 add by Will begin
            flow_meta_info = data.flowMetaInfo;
            // end
            for (var i = 0; i < outputs.length; i++) {
                if (outputs[i]) {
                    //MINERWEB-825 add by Will begin
                    flow_outputs.push({"output":data.outputs[i]});
                    // end
                    fill_flow_result(outputs[i]);
                }
            }
            if (logPane) {
                dijit.byId(ID_FLOW_RESULT_TAB).selectChild(logPane);
            }


        }
    }, null, true, "tabRoot");

//	dojo.xhrGet({
//		url : encodeURI(requestUrl),
//		sync : false,
//		preventCache : true,
//		headers : {
//		"Content-Type" : "plain/text; charset=utf-8",
//			"TIME_STAMP": alpine.TS,
//			"USER_INFO": alpine.USER
//		},
//		load : function(text, args) {
//			if(args&&args.xhr&&args.xhr.status!=200){
//				popupComponent.alert(alpine.nls.can_not_connect_server);
//				return;
//			}
//			if (text && text.length > 0 && text != "") {
//
//				var jsonObj =  eval("(" + text + ")");
//				if(jsonObj&&jsonObj.error_code ){
//					handle_error_result(jsonObj);
//					return;
//				}else if (!jsonObj){
//					popupComponent.alert(alpine.nls.result_not_found);
//					}
//				else{
//					var logs = jsonObj.logs;
//					var logPane=fill_flow_log(logs);
//					var outputs = jsonObj.outputs;
//					//MINERWEB-825 add by Will begin
//					flow_meta_info = jsonObj.flowMetaInfo;
//					// end
//					for ( var i = 0; i < outputs.length; i++) {
//						if (outputs[i]) {
//							//MINERWEB-825 add by Will begin
//							flow_outputs.push({"output":jsonObj.outputs[i]});
//							// end
//							fill_flow_result(outputs[i]);
//						}
//					}
//					if(logPane){
//						dijit.byId(ID_FLOW_RESULT_TAB).selectChild(logPane);
//					}
//
//
//
//				}
//			}
//		},
//		error : function(text, args) {
//			if(args&&args.xhr&&args.xhr.status!=200){
//				popupComponent.alert(alpine.nls.can_not_connect_server);
//				return;
//			}
//			popupComponent.alert(alpine.nls.error+":" + text);
//
//		}
//	});


    function add_flow_result_tab(id, header) {

        if (!dijit.byId(ID_FLOW_RESULT_TAB)) {

            var container = new dijit.layout.TabContainer({style:"width: 100%; height: 100%;"}, ID_FLOW_RESULT_TAB);//" >
            container.startup();
        }

        var div = document.createElement("div");
        div.id = id;
        dojo.byId(ID_FLOW_RESULT_TAB).appendChild(div);
        //user can not close
        var closable = false;

        var tab = new dijit.layout.ContentPane({title:header, closable:closable}, div);

        dijit.byId(ID_FLOW_RESULT_TAB).addChild(tab);
        //fill the hori...
        tab.style.width = "100%";
        tab.style.height = "100%";
        tab.preload = true;
        return tab;

    }

    function fill_flow_log(logs) {
        if (!logs || logs.length == 0) {
            return null;
        }
        var outpane = add_flow_result_tab("flow_log_tab", alpine.nls.Result_Log);
        outpane.innerHTML = "";
        outpane.startup();
        // add save button by will  MINERWEB-825 begin
        /*
         var myFlowResult_saveButton = new dijit.form.Button({id:"myFlowResult_saveButton",label: alpine.nls.save_html_report, onClick:function(){
         export_HTML_report(ID_FLOW_RESULT_TAB, false);
         }},dojo.create("div",{id:"myFlowResult_saveButton_container"},outpane.domNode));
         */
        //for new css style modify by Will
//        var myFlowResult_saveButton = dojo.create("button",{id:"myFlowResult_saveButton",dojoType:"dijit.form.Button",type:"button",innerHTML: alpine.nls.save_html_report,baseClass:"primaryButton",style:"cursor:pointer;",onclick:function(){
//            LinearRegressionGroup_IMPORT_IMG_COUNT = 0;
//            export_HTML_report(ID_FLOW_RESULT_TAB, false);
//        }},dojo.create("div",{id:"myFlowResult_saveButton_container"},outpane.domNode));
        // add save button by will  MINERWEB-825 end
        new dijit.form.Button({
            id:'myFlowResult_saveButton',
            //disabled: 'true',
            baseClass:'primaryButton',
            label:alpine.nls.save_html_report,
            onClick:function () {
                LinearRegressionGroup_IMPORT_IMG_COUNT = 0;
                export_HTML_report(ID_FLOW_RESULT_TAB, false);
                //  export_HTML_report('tabRoot', false)
            }
        }, dojo.create('div', {}, outpane.domNode));

        var index = 0;
        for (var i = 0; i < logs.length; i++) {
            var log = logs[i];
            if (!log) {
                continue;
            }
            var div = document.createElement("DIV");
            div.id = log.nodeName + "_logdiv";
//			div.style.position=STYLE_POSITION_ABS;
//			div.style.top=index * 14;
//			div.style.left=8;
            div.align = STYLE_ALIGN_LEFT;
            var name = log.nodeName == "null" ? "" : log.nodeName;
            var message = log.logmessage;


            div.innerHTML = " [" + log.dateTime + "] " + "  " + name + "  " + message;
            //appendChild is a html dom method ,can not ivoke a dojo object...
            dojo.byId("flow_log_tab").appendChild(div);
            //append error message
            if (log.message == MSG_PROCESS_ERROR) {
                var div1 = document.createElement("DIV");
                div1.id = name + "_errdiv";
//				div1.style.position=STYLE_POSITION_ABS;
//				index = index+1 ;
//				div1.style.top=index * 14;

                //red is error
                div1.style.color = "FF0000";
//				div1.style.left=8;
                div1.align = STYLE_ALIGN_LEFT;

                div1.innerHTML = log.errMessage;
                dojo.byId("flow_log_tab").appendChild(div1);
            }
            index = index + 1;
        }


        return outpane;
    }

    function fill_flow_result(outputData) {
        //data is an array or operators output, each operator can have only one output(but can be composited)
        if (!outputData) {
            return;
        }

        //this is for vusualizatio use
        if (outputData != "null" && outputData.out_id && outputData.visualData) {
            //get the output pane and set the result
            var outpane = add_flow_result_tab(outputData.out_id, outputData.out_title);
            outpane.chartData = outputData;
            outpane.preload = true;
            dijit.byId(ID_FLOW_RESULT_TAB).selectChild(outpane);

            fillOutPutPan(outpane, outputData);

            outpane.startup();


        }

    }
}

function renderOnShow(contentPane, showProgress) {
    if (contentPane.chartData && !contentPane.chartData.isRendered) {
        if (!contentPane.chartData.is_firt_comp_tab) { //manully click the tab and not a popup menu or first page.

            if (showProgress && contentPane.chartData.visualType != VISUAL_TYPE_CLUSRTER_CHART) {
                alpine.spinner.showSpinner(contentPane.id);
                //show_progress_bar_dilog();

                window.setTimeout(function () {
                    fillOutPutPan(contentPane, contentPane.chartData);
                    //hide_progress_bar_dilog();
                    alpine.spinner.hideSpinner(contentPane.id);
                }, 1000);
            } else {
                fillOutPutPan(contentPane, contentPane.chartData);
            }
        }
    }
}


function releaseResultTab(id) {
    alpine = null;
    window.opener = null;
    if (dijit.byId(id) == null) {
        return;
    }
    var tabs = dijit.byId(id).getChildren();

    //make sure release the event handler first!!
    if (result_event_handlers) {
        for (var i = 0; i < result_event_handlers.length; i++) {
            if (result_event_handlers[i]) {
                dojo.disconnect(result_event_handlers[i]);
                result_event_handlers[i] = null;
            }
        }
    }

    if (tabs) {
        for (var i = 0; i < tabs.length; i++) {
            release_output_resource(tabs[i].chartData);
            tabs[i].chartData = null;
        }
    }


    result_event_handlers = null;
    clearInterval(timerID);
    count = 0;
    LastOperator = null;
    var length = flow_outputs.length;
    for (var i = 0; i < length; i++) {

        flow_outputs[i] = null;
    }
    flow_outputs = null;
    flow_meta_info = null;//report use
    ds = null;
    isExportingReport = false;
    process_bar_dialog = null;
    flowResult = null;
    allSVGoutput = null;
    allLegendOutput = null;
    allLegendLength = null;
    svgCountArray = null;
    if (dijit.byId(id) && dijit.byId(id).destroyRecursive) {
        dijit.byId(id).destroyRecursive();
    }
    purge(document.body);
}

function release_output_resource(output) {
    if (!output) {
        return;
    }
    if (output.dojo_widget) {
        output.dojo_widget.destroyRecursive();
    }
    else if (output.dojo_surface) {
        output.dojo_surface.clear();
        output.dojo_surface.destroy();
        output.dojo_surface = null;

        if (output.dojo_surface_legend) {
            for (var i = 0; i < output.dojo_surface_legend.length; i++) {
                output.dojo_surface_legend[i].clear();
                output.dojo_surface_legend[i].destroy();
                output.dojo_surface_legend[i] = null;
            }
            output.dojo_surface_legend = null;
        }


    } else if (output.dojo_cluster_table) {
        var tableData = output.tableData;
        //make surface to svg...
        var svgCount = 0;

        for (var i = 0; i < tableData.length; i++) {
            var rowData = tableData[i];
            for (var j = 0; j < rowData.length; j++) {
                if (output.tableGroupColumnType[j] == "svg") {
                    var surface = rowData[j];
                    //avoid duplicated  changed

                    if (dojo.isString(surface) == false) {
                        surface.destroy();
                        rowData[j] = null;
                    }


                }
            }


        }


    } else if (output.visualType == VISUAL_TYPE_COMPOSITE) {

        var subOutPut = output.visualData;
        if (subOutPut) {
            var subResultoutPuts = new Array();
            for (var i = 0; i < subOutPut.length; i++) {
                var output = subOutPut[i];
                if (output) {
                    release_output_resource(subOutPut[i]);
                    subOutPut[i] = null;
                }
            }
        }
    } else if (output.visualType == VISUAL_TYPE_LAYERED) {

        var subOutPut = output.visualData;
        var keys = output.keys;
        if (subOutPut) {
            var subResultoutPuts = new Array();
            var old_currentLayeredData = output.currentLayeredData;
            for (var i = 0; i < subOutPut.length; i++) {

                var vData = subOutPut[i][keys[i]];
                if (vData) {
                    release_output_resource(vData);
                    subOutPut[i][keys[i]] = null;
                }
            }
        }
    }

}


function fillTabledGroupChart(outpane, output) {
    var keyLable = output.keyLabel;
    var keys = output.keys;
    var div = document.createElement(TAG_DIV);
    div.style.width = "100%";
    div.style.height = "100%";
    var comboDiv = document.createElement(TAG_DIV);
    var contentDiv = document.createElement(TAG_DIV);
    contentDiv.style.width = "100%";
    contentDiv.style.height = "100%";
    div.appendChild(comboDiv);

    div.appendChild(contentDiv);
    var label = document.createElement("label");
    label.innerHTML = ("<a>" + keyLable + "</a>");
    comboDiv.appendChild(label);

    var mySelect = document.createElement("select");

    for (var i = 0; i < keys.length; i++) {
        var option = document.createElement("option");
        option.value = keys[i];
        option.appendChild(document.createTextNode(keys[i]));
        mySelect.appendChild(option);
    }

    comboDiv.appendChild(mySelect);

    var contentDivPane = new dijit.layout.ContentPane({}, contentDiv);
    contentDivPane.style.width = "100%";
    contentDivPane.style.height = "100%";
    contentDivPane.preload = true;
    outpane.setContent(div);

    //this is for reportgeneration use ...
    output.contentDivPane = contentDivPane;
    output.contentDiv = contentDiv;

    var visualDatas = output.visualData;
    if (visualDatas && visualDatas.length > 0 && keys.length > 0) {
        var vData = visualDatas[0][keys[0]];
        fillOutPutPan(contentDivPane, vData);
        output.currentLayeredData = vData;

        //	contentDivPane.startup();

        var selectOutKey = function () {
            var value = mySelect.value;
            var vData = visualDatas[mySelect.selectedIndex][value];


            if (isExportingReport == false) {

//				show_progress_bar_dilog();
                window.setTimeout(function () {
                    fillLayeredContent(contentDiv, contentDivPane, vData);
//					 hide_progress_bar_dilog();

                }, 300);
            }


            //this is for reportgeneration use ...
            output.currentLayeredData = vData;

        };
        if (window.addEventListener) // Mozilla, Netscape, Firefox
        {
            mySelect.addEventListener('change', selectOutKey, false);
        } else// IE
        {
            mySelect.attachEvent('onchange', selectOutKey);
        }
        //make it refresh !!!  ? not working
        mySelect.options[0].selected = true;
    }
    contentDivPane.startup();

}

function cleanSVG(svg) {
    //Make sure the namespace is set.
    if (svg.indexOf("xmlns=\"http://www.w3.org/2000/svg\"") == -1) {
        svg = svg.substring(4, svg.length);
        svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"" + svg;
    }
    //Same for xmlns:xlink (missing in Chrome and Safari)
    if (svg.indexOf("xmlns:xlink=\"http://www.w3.org/1999/xlink\"") == -1) {
        svg = svg.substring(4, svg.length);
        svg = "<svg xmlns:xlink=\"http://www.w3.org/1999/xlink\"" + svg;
    }
    var endfirst = (svg.indexOf(">"));
    if (endfirst > -1) {
        //svg = svg.substring(0,endfirst + 1) + "<style type=\"text/css\" ><![CDATA[.axis path, .axis line {fill: none;stroke: #000;shape-rendering: crispEdges;} .axis text {font-size: 10px;}]]></style>" + svg.substring(endfirst + 1, svg.length);
        svg = svg.substring(0, endfirst + 1) + " <defs id=\"cssdefs\"> <style type=\"text/css\" id=\"cssstyle\"> .axis path, .axis line  {fill: none;stroke: #000;shape-rendering: crispEdges;} .axis text {font-size: 10px;} </style> </defs>" + svg.substring(endfirst + 1, svg.length);

    }

    //and add namespace to href attribute if not done yet
    //(FF 5+ adds xlink:href but not the xmlns def)
    if (svg.indexOf("xlink:href") === -1) {
        svg = svg.replace(/href\s*=/g, "xlink:href=");
    }
    //Do some other cleanup, like stripping out the
    //dojoGfx attributes and quoting ids.
    svg = svg.replace(/\bdojoGfx\w*\s*=\s*(['"])\w*\1/g, "");
    svg = svg.replace(/\b__gfxObject__\s*=\s*(['"])\w*\1/g, "");
    svg = svg.replace(/[=]([^"']+?)(\s|>)/g, '="$1"$2');
    return svg;
}
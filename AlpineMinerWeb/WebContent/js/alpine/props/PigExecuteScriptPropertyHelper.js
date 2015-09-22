/**
 * Date: 10/19/2012
 * User: sasher
 * (C) 2012 Alpine Data Labs
 */

define([
    "dojo/ready",
    "dojo/dom",
    "dojo/dom-construct",
    "dojo/dom-class",
    "dojo/query",
    "dojo/on",
    "dojo/_base/lang",
    "dojo/_base/array",
    "dojo/store/Memory",
    "dijit/registry",
    "dijit/form/Button",
    "alpine/flow/OperatorManagementManager"
],
    function(ready, dom, domConstruct, domClass, query, on, lang, array, Memory, registry, Button, operatorManager) {

		var columnPaneShowing = true;//initially the panel is open.
	
        var rowFilterCodeMirror = null;
        
        var callback = null;
        
        var currentProp = null;
        
        var scriptModel = null;
        
        var eventTracker = [];

        var constants =
        {
            DIALOG_ID:              "alpine_props_pigexecuteScript_dialog",
            BTN_CANCEL:             "alpine_props_pigexecuteScript_btn_cancel",
            BTN_OK:                 "alpine_props_pigexecuteScript_btn_save",
            EDITOR_ID:              "alpine_props_pigexecuteScript_editor",
            TOOLBAR:                "alpine_props_pigexecuteScript_toolbar",
            COLUMN_SEL:             "alpine_props_pigexecuteScript_columnList",
            COLUMN_GRID_ID:			"alpine_props_pigexecuteScript_columnList_grid",
            BTN_PIG_HELPER:         "alpine_props_pigexecuteScript_btn_help",
            PIG_HELPER_DIALOG_ID:   "pigSyntaxHelperDialog",
            PIG_INPUT_LABEL: 		"alpine_props_pigexecuteScript_input_label",
            COLUMN_GRID_PANE:       "alpine_props_pigexecuteScript_columnList_pane",
            SQL_HELPER_WORDS:       ["AND","OR","NOT","||","BETWEEN","IN","LIKE","||","=","<>",">","<",">=","<="],
            PIG_HELPER_WORDS:       ["AND","OR","NOT","||","MATCHES","IS NULL","IS NOT NULL","||","==","!=",">","<",">=","<="]

        };

        ready(function(){

            on(registry.byId(constants.BTN_OK), "click", update_where_clause_data);
            on(registry.byId(constants.BTN_CANCEL), "click", function(){
            	registry.byId(constants.DIALOG_ID).hide();
            });
            on(registry.byId(constants.BTN_PIG_HELPER), "click",_showPigHelper);
            on(registry.byId(constants.DIALOG_ID), "hide", close_cancel_where_clause_dialog);


            rowFilterCodeMirror = CodeMirror.fromTextArea(dom.byId(constants.EDITOR_ID), {
                lineNumbers: true,
                matchBrackets: true,
                indentUnit: 4,
                mode: "text/x-plsql",
                extraKeys: {"Ctrl-Space": "autocomplete","Shift-Ctrl-Space": "autocomplete"}
            });

        });
        
        /**
         * the arguments what will be passed to submitCallback:
         * arguments[0] = /String/ scriptContent
         * arguments[1] = /String/ inputFileName
         */
        function _showPigScriptDialog(prop, submitCallback)
        {
            scriptModel =  prop.hadoopPigExecuteScriptModel;
            console.log("prop value = " + scriptModel);
            callback = submitCallback;
            currentProp = prop;


            _buildToolButtons(CurrentOperatorDTO.classname,constants.TOOLBAR);
            dialogTitle = alpine.nls.hadoop_pig_execute_pigScript_dialog_title;
            registry.byId(constants.BTN_PIG_HELPER).set('style',"display:block;");

            rowFilterCodeMirror.setOption("mode", "text/x-pig");

            CodeMirror.commands.autocomplete = function(cm) {
                CodeMirror.simpleHint(cm, CodeMirror.pigHint);
            };
            
//            dom.byId(constants.TITLE_ID).innerHTML =  dialogTitle;

            rowFilterCodeMirror.setValue(scriptModel.pigScript) ;

            registry.byId(constants.DIALOG_ID).show();
            registry.byId(constants.DIALOG_ID).titleBar.style.display = "none";
            rowFilterCodeMirror.refresh();

            if(CurrentOperatorDTO.inputFileInfos){// means whether operator is connected by other operator.
            	if(!columnPaneShowing){
            		dijit.byId(constants.COLUMN_GRID_PANE).toggle();
            		columnPaneShowing = true;
            	}
            	_buildAvailableColumnGrid(constants.COLUMN_SEL);
            	if(scriptModel.pigScript == ""){
            		rowFilterCodeMirror.setValue(_generateSampleScript());
            	}
            }else if(columnPaneShowing){
        		dijit.byId(constants.COLUMN_GRID_PANE).toggle();
        		columnPaneShowing = false;
        	}
        }

        function _buildToolButtons(operatorType,id){
            toolBtn = constants.PIG_HELPER_WORDS;
            domConstruct.empty(id);
            toolBtn.forEach(function(itm,idx){
                var buttonWidget = null;
                if(itm=="||"){
                    buttonWidget =  new dijit.ToolbarSeparator();
                }else{
                    buttonWidget = new dijit.form.Button({type:"button",innerHTML:itm+"&nbsp;",onClick:function(){
                        insert_text(itm.toLowerCase());
                    }});
                }
                domConstruct.place(buttonWidget.domNode,id,"last");
            });


            registry.byId(id).startup();
        }

        function insert_text_with_quot(value) {
            var newval;
            if(CurrentOperatorDTO.inputTableInfos){//db table
                newval = "\"" + value + "\" ";
            }else{//hadoop
                newval = toolkit.getValue(value);
            }

            insert_text(newval);
        }
        function insert_text(value) {
            rowFilterCodeMirror.replaceSelection(value.trim() + " ", "end");
            rowFilterCodeMirror.focus();

        }

//        function removeHTMLTags(str) {
//            str = str.replace(/<\/?[^>]+(>|$)/g, "");
//
//            str = str.replace(/&lt;/gi, '<');
//            str = str.replace(/&gt;/gi, '>');
//            str = str.replace(/&quot;/gi, '"');
//            str = str.replace(/&amp;/gi, '&');
//            str = str.replace(/\s{2,}/gi, " ");//replace all successive spaces to single space, to avoid encoding problem. MINERWEB-945
//            str = encodeURI(str);
//            str = str.replace(new RegExp("%C2%A0","gm"), "%20");
//            return decodeURI(str);
//        }

        function update_where_clause_data() {
//            var str = rowFilterCodeMirror.getValue();
//            var data = removeHTMLTags(str);

            var data = rowFilterCodeMirror.getValue();
            if(data!=""){
                //Nested foreach operations validate
               var reg1 = /\bFOREACH\b[\s]+[\w]+[\s]*{/ig;
               var reg2 = /\bforeach\b[\s]+[\w]*[\s]*{/ig;
                if(reg1.test(data)==true || reg2.test(data)==true){
                    popupComponent.alert(alpine.nls.hadoop_pig_execute_pigscript_invalid_tip);
                    return false;
                }

            }
            if(null==data || dojo.trim(data)==""){
                setButtonBaseClassInvalid(getSourceButtonId(currentProp));
            }else{
                setButtonBaseClassValid(getSourceButtonId(currentProp));
            }
            
            scriptModel.pigScript = data;
            
        	callback.call(null, scriptModel);
        	registry.byId(constants.DIALOG_ID).hide();
        }

        function close_cancel_where_clause_dialog() {
        	_clear();
            registry.byId(constants.BTN_PIG_HELPER).set('style','display:none;');
        }

        function _showPigHelper()
        {
            registry.byId(constants.PIG_HELPER_DIALOG_ID).titleBar.style.display = 'none';
            registry.byId(constants.PIG_HELPER_DIALOG_ID).show();
        }
        
        function _clear(){
        	callback = null;
        	currentProp = null;
    		var event = null;
    		if(dijit.byId(constants.COLUMN_GRID_ID)){
        		dijit.byId(constants.COLUMN_GRID_ID).destroyRecursive();
    		}
    		while((event = eventTracker.pop()) != undefined){
    			dojo.disconnect(event);
    		}
        }
        
        function _buildAvailableColumnGrid(containerID){
			var grid = dijit.byId(constants.COLUMN_GRID_ID);
        	var data = _adaptDataToTreeGrid(scriptModel.inputFileList);
			var store = new dojo.data.ItemFileWriteStore({
				data: {
					identifier: "id",
					label: "name",
					items: data
				}
			});
        	grid = new dojox.grid.TreeGrid({
				id: constants.COLUMN_GRID_ID,
				store: store,
				query: {id: "R-*"},
				queryOptions: {deep: true},
				structure: [{
					cells: [
						[
						 {name: "Alias", field: "name", width: "60%", formatter: function(value, rowId){
							 var record = this.grid.getItem(rowId);
							 var operatorUUID = toolkit.getValue(record.uid);
							 var operatorPrimaryInfo = operatorManager.getOperatorPrimaryInfo(operatorUUID);
							 return operatorPrimaryInfo.name + "<br/>(" + value + ")";
						 }},
						 {
						 	field: "columns",
						 	children: [
						 	    {name: "Column name", field: "name", width: "40%", formatter: formatterNullSummary}
						 	]
						 }
						]
					]
				}],
				canSort: function(){
					return false;
				}
			}, dojo.create("div", {}, containerID));
			grid.startup();
			eventTracker.push(dojo.connect(grid, "onRowDblClick", function(e){
        		if(e.rowIndex == undefined
        				|| e.target.className == "dojoxGridExpandoNode"){
					return;
				}
				var record = this.getItem(e.rowIndex);
				if(record  == null){
					return;
				}
        		insert_text_with_quot(toolkit.getValue(record.name));
			}));
        }
        
        function _generateSampleScript(){
        	var folder = null, 
        		file = null,
        		outputPath = null,
        		scriptTemplate = "${varName} = FOREACH ${alias} GENERATE ${columns};\n",
        		storageTemplate = "Result = ...\nSTORE Result INTO '${ouputFile}';",
        		scriptSampleText = "",
        		wordChar = 65,//65 = A
        		seriesNum = 0,
        		varName = "";
        	if(!scriptModel.inputFileList){
        		return "";
        	}
        	for(var i = 0;i < scriptModel.inputFileList.length;i++){
        		if(i != 0 && i % 26 == 0){
        			wordChar = 65;
        			seriesNum++;
        		}
        		if(seriesNum > 0){
        			varName = String.fromCharCode(wordChar++) + seriesNum;
        		}else{
        			varName = String.fromCharCode(wordChar++);
        		}
            	var fileScriptSlice = dojo.string.substitute(scriptTemplate, {
            		varName: varName,
            		alias: scriptModel.inputFileList[i].alias,
            		columns: scriptModel.inputFileList[i].columnNames.join(",")
            	});
            	scriptSampleText += fileScriptSlice;
        	}
        	
			folder = dijit.byId("resultsLocation" + ID_TAG).get("value");
			file = dijit.byId("resultsName" + ID_TAG).get("value");
        	outputPath = folder + "/" + file;
        	return scriptSampleText += dojo.string.substitute(storageTemplate, {
        		ouputFile: outputPath
        	});
        }
        
    	function formatterNullSummary(value, rowIdx){
    		if(rowIdx >= 0){
    			return value;
    		}
    		return "";
    	}
        
        function _adaptDataToTreeGrid(data){
        	var adapterData = [];
        	for(var i = 0; i < data.length;i++){
        		var fileData = {
        			id: "R-" + data[i].operatorUUID,
        			uid: data[i].operatorUUID,
        			name: data[i].alias,
        			columns: []
        		};
        		if(data[i].columnNames){
            		for(var j = 0;j < data[i].columnNames.length;j++){
            			fileData.columns.push({
            				id: "C-" + data[i].operatorUUID + "-" + data[i].columnNames[j],
            				name: data[i].columnNames[j]
            			});
            		}
        		}
        		adapterData.push(fileData);
        	}
        	return adapterData;
        }
        
        return {
            showPigScriptDialog: _showPigScriptDialog
        };
    });
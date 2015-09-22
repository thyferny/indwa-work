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
    "dijit/registry"
],
    function(ready, dom, domConstruct, domClass, query, on, lang, array, Memory, registry) {

        var WhereClauseColumnListGrid = null;
        var rowFilterCodeMirror = null;

        var constants =
        {
            DIALOG_ID:              "whereClauseEditDialog",
            BTN_CANCEL:             "cancelRowFilterButton",
            BTN_OK:                 "saveRowFilterButton",
            EDITOR_ID:              "where_clause_editor",
            TOOLBAR:                "where_clause_toolbar",
            COLUMN_SEL:             "where_clause_column_list",
            BTN_PIG_HELPER:         "pigSyntaxHelperButtonFilter",
            PIG_HELPER_DIALOG_ID:   "pigSyntaxHelperDialog",
            TITLE_ID:               "where_clause_edit_title_container",
            SQL_HELPER_WORDS:       ["AND","OR","NOT","||","BETWEEN","IN","LIKE","||","=","<>",">","<",">=","<="],
            PIG_HELPER_WORDS:       ["AND","OR","NOT","||","MATCHES","IS NULL","IS NOT NULL","||","==","!=",">","<",">=","<="]

        }


        ready(function(){

            on(registry.byId(constants.BTN_OK), "click", update_where_clause_data);
            on(registry.byId(constants.BTN_CANCEL), "click", close_cancel_where_clause_dialog);
            on(registry.byId(constants.BTN_PIG_HELPER), "click",_showPigHelper)


            rowFilterCodeMirror = CodeMirror.fromTextArea(dom.byId(constants.EDITOR_ID), {
                lineNumbers: true,
                matchBrackets: true,
                indentUnit: 4,
                mode: "text/x-plsql",
                extraKeys: {"Ctrl-Space": "autocomplete","Shift-Ctrl-Space": "autocomplete"}
            });

        });




        function _showRowFilterDialog(prop)
        {
            var value =  prop.value == null ? "" : prop.value;
            console.log("prop value = " + value);

            var grid = create_default_column_list(prop.store, constants.COLUMN_SEL, whereclause_click_column_list);
            WhereClauseColumnListGrid = grid;

            var isHadoop = false;
            if (CurrentOperatorDTO.classname == "HadoopRowFilterOperator")
            {
                isHadoop = true;
            }

            _buildToolButtons(CurrentOperatorDTO.classname,constants.TOOLBAR, isHadoop);
            dialogTitle = alpine.nls.row_filter_title;
            if(isHadoop){
                registry.byId(constants.BTN_PIG_HELPER).set('style',"display:block;");

                rowFilterCodeMirror.setOption("mode", "text/x-pig");

                CodeMirror.commands.autocomplete = function(cm) {
                    CodeMirror.simpleHint(cm, CodeMirror.pigHint);
                };
            }    else
            {
                if("SQLExecuteOperator" == CurrentOperatorDTO.classname){
                    dialogTitle = alpine.nls.sql_editor_title;
                }
                rowFilterCodeMirror.setOption("mode", "text/x-plsql");

                CodeMirror.commands.autocomplete = function(cm) {
                    CodeMirror.simpleHint(cm, CodeMirror.plsqlHint);
                };
            }

            dom.byId(constants.TITLE_ID).innerHTML =  dialogTitle;

            rowFilterCodeMirror.setValue(value) ;
            registry.byId(constants.DIALOG_ID).titleBar.style.display = "none";
            registry.byId(constants.DIALOG_ID).show();
            rowFilterCodeMirror.refresh();
        }

        function _buildToolButtons(operatorType,id,isHadoop){
            var toolBtn = constants.SQL_HELPER_WORDS;
            if(isHadoop){
                toolBtn = constants.PIG_HELPER_WORDS;
            }
            domConstruct.empty(id);
            toolBtn.forEach(function(itm,idx){
                var buttonWidget = null;
                if(itm=="||"){
                    buttonWidget =  new dijit.ToolbarSeparator();
                }else{
                    buttonWidget = new dijit.form.Button({type:"button",innerHTML:itm+"&nbsp;",onClick:function(){
                        if(isHadoop){
                            insert_text(itm.toLowerCase());
                        }else{
                            insert_text(itm);
                        }
                    }});
                }
                domConstruct.place(buttonWidget.domNode,id,"last");
            });


            registry.byId(id).startup();
        }

        function whereclause_click_column_list(event) {
            var c = WhereClauseColumnListGrid.getItem(event.rowIndex);
            insert_text_with_quot(c.name);
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
            var str = rowFilterCodeMirror.getValue();
           // var data = removeHTMLTags(str);
            CurrentWhereClause.value = str;

            if(null==CurrentWhereClause.value || dojo.trim(CurrentWhereClause.value)==""){
                setButtonBaseClassInvalid(getSourceButtonId(CurrentWhereClause));
            }else{
                setButtonBaseClassValid(getSourceButtonId(CurrentWhereClause));
            }
            registry.byId(constants.DIALOG_ID).hide();
            registry.byId(constants.BTN_PIG_HELPER).set('style',"display:none;");
        }

        function close_cancel_where_clause_dialog() {
            registry.byId(constants.DIALOG_ID).hide();
            registry.byId(constants.BTN_PIG_HELPER).set('style','display:none;');
        }

        function _showPigHelper()
        {
            registry.byId(constants.PIG_HELPER_DIALOG_ID).titleBar.style.display = 'none';
            registry.byId(constants.PIG_HELPER_DIALOG_ID).show();
        }


        return {
            showRowFilterDialog: _showRowFilterDialog
        }
    });
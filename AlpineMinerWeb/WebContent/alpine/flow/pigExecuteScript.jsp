<script type="text/javascript">
    dojo.require("alpine.props.PigExecuteScriptPropertyHelper");
</script>
<style>
	.rowFilterCodeMirror .CodeMirror-scroll {height: 380px}
	#alpine_props_pigexecuteScript_columnList_pane .dojoxExpandoTitle{
		display: none;
	}	
</style>

<div dojoType="dijit.Dialog" id="alpine_props_pigexecuteScript_dialog" draggable="false"
     title="<fmt:message key='where_clause_edit'/>">
    <div class="titleBar">
        <fmt:message key='pig_edit_scirpt_button'/>
    </div>
    <div dojoType="dijit.layout.BorderContainer" style="width: 900px; height: 490px;" design="sidebar" gutters="true" liveSplitters="true">
        <div dojoType="dijit.layout.ContentPane" region="center">
            <div id="alpine_props_pigexecuteScript_toolbar" dojoType="dijit.Toolbar">
            </div>
            <div class="rowFilterCodeMirror"><textarea id="alpine_props_pigexecuteScript_editor" name="rowsqlExpression"
                                  dojoType="dijit.form.SimpleTextarea"></textarea> </div>
            <div style="height:15px; float:right">
                <button dojoType="dijit.form.Button" baseClass="workflowButton"
                        id="alpine_props_pigexecuteScript_btn_help"
                        style="display:none;">
                    <fmt:message key="pig_syntax_button" />
                </button>
            </div>
        </div>

        <div dojoType="dojox.layout.ExpandoPane" id="alpine_props_pigexecuteScript_columnList_pane" region="right" style="width: 300px">
        	<div id="alpine_props_pigexecuteScript_columnList" style="width: 98%; height: 95%"></div>
        </div>
    </div>
    <div class="whiteDialogFooter">
        <button dojoType="dijit.form.Button" type="button" baseClass="cancelButton" id="alpine_props_pigexecuteScript_btn_cancel">
            <fmt:message key="Cancel" />
        </button>
        <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton" id="alpine_props_pigexecuteScript_btn_save">
            <fmt:message key="OK" />
        </button>

    </div>
</div>
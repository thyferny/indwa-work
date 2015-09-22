<html>
<head>
    <script type="text/javascript">
        dojo.require("alpine.props.RowFilterPropertyHelper");
    </script>
    <style>.rowFilterCodeMirror .CodeMirror-scroll {height: 380px}</style>

</head>
<body>

<div dojoType="dijit.Dialog" id="whereClauseEditDialog" draggable="false">
	<div class="titleBar" id="where_clause_edit_title_container"></div>
    <div dojoType="dijit.layout.BorderContainer" design="sidebar" style="width: 900px; height: 440px;" gutters="true" liveSplitters="true" id="whereClauseBorderContainer">
        <div dojoType="dijit.layout.ContentPane" splitter="true"
             region="center">
            <div id="where_clause_toolbar" dojoType="dijit.Toolbar">
            </div>
            <div class="rowFilterCodeMirror"><textarea id="where_clause_editor" name="rowsqlExpression"
                                  dojoType="dijit.form.SimpleTextarea"></textarea> </div>
            <div style="height:15px; float:right">
                <button dojoType="dijit.form.Button" baseClass="workflowButton"
                        id="pigSyntaxHelperButtonFilter"
                        style="display:none;">
                    <fmt:message key="pig_syntax_button" />
                </button>
            </div>
        </div>

        <div dojoType="dijit.layout.ContentPane" splitter="true"
             region="right" style="width: 300px;" id="where_clause_column_list">
            add column list grid here.</div>
    </div>
    <div class="whiteDialogFooter">
        <button dojoType="dijit.form.Button" type="button" baseClass="cancelButton" id="cancelRowFilterButton">
            <fmt:message key="Cancel" />
        </button>
        <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton" id="saveRowFilterButton">
            <fmt:message key="OK" />
        </button>

    </div>
</div>
</body>
</html>

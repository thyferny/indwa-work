<%--
  Created by IntelliJ IDEA.
  User: Will
  Date: 12-8-9
  Time: 上午11:00
--%>
<script type="text/javascript">
dojo.require("alpine.props.ColumnNamePropertySelectHelper");
</script>

<fmt:bundle basename="app">
<div dojoType="alpine.layout.PopupDialog" id="columnSelectionDialog" draggable="false"
     title="<fmt:message key='select_columns'/>">
    <div dojoType="dijit.layout.LayoutContainer"
         style="width: 400px; height: 550px;margin-left: 8px; margin-right:8px;" id="columnSelectionDialog_container">
        <div dojoType="dijit.layout.ContentPane" region="top" style="padding:5px 0 5px 0;">
            <span style="float: left">
                <div class="textBoxFancyBorder" style="margin-top:0;"><div dojoType="alpine.layout.ClearableTextBox" id="dataexplorer_columns_filter_for_property" style="width:240px;margin-top:-8px;border:none;background:none;font-size:12px;" placeHolder="<fmt:message key='workbench_filterbox_placeholder'/>"></div></div>
            </span>
            <span style="float:right;width:80px;padding-top:4px;">
            <button id="column_selectall" dojoType=dijit.form.Button baseClass="linkButton" name="All">
                    <fmt:message key="Table_Join_All_Button" />
                </button><span class="tablejoinsubfield" style="padding-right:0; color:#949599;">|</span><button id="column_selectnone" dojoType=dijit.form.Button baseClass="linkButton" name="None">
                    <fmt:message key="Table_Join_None_Button" />
                </button>
            </span>
        </div>
        <div dojoType="dijit.layout.ContentPane" region="center" >
            <div id="columnDialogTableHolder" style="height:100%;"></div>
        </div>
        <div dojoType="dijit.layout.ContentPane" layoutAlign="bottom">
            <div class="whiteDialogFooter">
                <button id="btn_cancel_4_property_columnName_select" dojoType=dijit.form.Button type="Reset" baseClass="cancelButton" name="Reset">
                    <fmt:message key="Cancel" />
                </button>
                <button id="btn_ok_4_property_columnName_select" dojoType=dijit.form.Button type="button" baseClass="primaryButton" name="submit">
                    <fmt:message key="OK" />
                </button>
            </div>
        </div>
    </div>
    <div dojoType="dojox.layout.ResizeHandle" targetId="columnSelectionDialog_container" constrainMax="true" maxWidth="600" maxHeight="600" minWidth="400" minHeight="300"></div>
</div>
</fmt:bundle>
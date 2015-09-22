<%--
  User: Will
  File:NullValueReplacePropertyConfig
  Date: 12-8-10
  Time: 上午11:54
  
--%>
<script type="text/javascript">
    dojo.require("alpine.props.NullValueReplacementPropertyHelper");
</script>
<fmt:bundle basename="app">
    <div dojoType="alpine.layout.PopupDialog" id="nullValueReplaceColumnSelectionDialog" draggable="false"
         title="<fmt:message key='prop_null_value_replace_dlg_title'/>">
        <div dojoType="dijit.layout.LayoutContainer" id="nullValueReplaceColumnSelectionDialog_container"
             style="width: 500px; height: 480px">
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div dojoType="dijit.form.Form" id="nullValueReplaceColumnForm"  jsId="nullValueReplaceColumnForm">
                 <div id="nullValueReplaceGridContainer" style="height:100%;width:100%;overflow:auto;"></div>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom">
                <div class="whiteDialogFooter">
                    <button id="btn_cancel_4_property_nullvaluereplace_columnName_select" dojoType=dijit.form.Button type="Reset"  baseClass="cancelButton" name="Reset">
                        <fmt:message key="Cancel" />
                    </button></td>
                    <button id="btn_ok_4_property_nullvaluereplace_columnName_select" dojoType=dijit.form.Button type="button"  baseClass="primaryButton" name="submit">
                        <fmt:message key="OK" />
                    </button>
                </div>
            </div>
        </div>
        <div dojoType="dojox.layout.ResizeHandle" targetId="nullValueReplaceColumnSelectionDialog_container" constrainMax="true" maxWidth="600" maxHeight="500" minWidth="400" minHeight="480"></div>
    </div>
</fmt:bundle>
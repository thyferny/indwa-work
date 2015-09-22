<script type="text/javascript">
    dojo.require("alpine.props.setOperatorPropertyHelper");
</script>

<fmt:bundle basename="app">

    <div dojoType="alpine.layout.PopupDialog" draggable="false" id="setOperatorDialog" title="<fmt:message key='tableset_config_title'/>" >
        <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 800px; height: 1000px;">
            <div dojoType="dijit.layout.ContentPane" region="top" >
                <span style="padding: 0 15px 5px 15px;" id="setOperatorUnionTypeSelectCont"></span>
                <label class="operatorSubText" id="setOpTypeDescription" ><fmt:message key='set_op_union_msg'/></label>
                <div>
                    <span style="float: right;padding-right: 15px;"><span id="setOperatorMagicBtn" ></span></span>
                    <span style="float: right;padding-right: 15px;"><button type="button" dojoType="dijit.form.Button" baseClass="linkButton" id="setOperatorDialogNewBtn"><fmt:message key='set_op_new'/></button></span>
                    <div style="font-size: 13px; font-style: italic; padding: 5px 15px 0 18px; visibility: hidden;" id="setOperatorFirstTableSelectCont"><span style="padding-right: 5px;"><fmt:message key='set_op_primary_dataset'/></span></div>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center" >
                <div dojoType="dijit.form.Form" id="setOperatorDialogForm" style="overflow: auto;">
                    <div id="setOpInlineEdit"></div>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter" >
                <button baseClass="cancelButton" valign="bottom" id="setOperatorDialog_cancel" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
                <button baseClass="primaryButton" valign="bottom" id="setOperatorDialog_ok" dojoType="dijit.form.Button" type="button"><fmt:message key="OK"/></button>
            </div>
        </div>
    </div>

</fmt:bundle>

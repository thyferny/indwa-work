<script type="text/javascript">
    dojo.require("alpine.props.nullValueReplacementHelper");
</script>

<fmt:bundle basename="app">

    <div dojoType="alpine.layout.PopupDialog" draggable="false" id="nvrConfigurationDialog" title="<fmt:message key='prop_null_value_replace_dlg_title'/>" >
        <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 500px; height: 600px">
            <div dojoType="dijit.layout.ContentPane" region="top" >
                <div style="float: right;padding-right: 15px;">
                    <button type="button" dojoType="dijit.form.Button" baseClass="linkButton" id="nvrConfigurationAllBtn">All</button>
                    <span class="tablejoinsubfield" style="padding-right:0; color:#949599;">|</span>
                    <button type="button" dojoType="dijit.form.Button" baseClass="linkButton" id="nvrConfigurationNoneBtn">None</button>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div dojoType="dijit.form.Form" id="nvrConfigurationForm" style="overflow: auto;">
                    <div id="nvrInlineEdit"></div>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter" >
                <div style="text-align:center;padding:6px;">
                    <fmt:message key='nvr_aggregate_by'/> <div dojoType="dijit.form.FilteringSelect" id="nvrGroupBySelect" baseClass="greyDropdownButton" style="width:220px;" placeHolder="group by column" required="false"></div>
                </div>
                <div>
                    <button baseClass="cancelButton" valign="bottom" id="nvrConfigurationDialog_cancel" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
                    <button baseClass="primaryButton" valign="bottom" id="nvrConfigurationDialog_ok" dojoType="dijit.form.Button" type="button"><fmt:message key="OK"/></button>
                </div>
                </div>
        </div>
    </div>

</fmt:bundle>
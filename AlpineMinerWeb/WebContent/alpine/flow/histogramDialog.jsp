<script type="text/javascript">
    dojo.require("alpine.props.histogramPropertyHelper");
</script>

<fmt:bundle basename="app">

    <div dojoType="alpine.layout.PopupDialog" draggable="false" id="histogramConfigurationDialog" title="<fmt:message key='histogram_dialog_title'/>" >
        <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 700px; height: 450px">
            <div dojoType="dijit.layout.ContentPane" region="top" >
                <div style="float: right;padding-right: 15px;">
                    <button type="button" dojoType="dijit.form.Button" baseClass="linkButton" id="histogramConfigurationNewBtn"><fmt:message key='histogram_new_histogram'/></button>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div dojoType="dijit.form.Form" id="histogramConfigurationForm" style="overflow: auto;">
                    <div id="histogramInlineEdit"></div>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter" >
                <button baseClass="cancelButton" valign="bottom" id="histogramConfigurationDialog_cancel" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
                <button baseClass="primaryButton" valign="bottom" id="histogramConfigurationDialog_ok" dojoType="dijit.form.Button" type="button"><fmt:message key="OK"/></button>
            </div>
        </div>
    </div>

</fmt:bundle>
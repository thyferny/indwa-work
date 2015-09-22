<script type="text/javascript">
    dojo.require("alpine.chorus.ChorusConfigUIHelper");
</script>

<fmt:bundle basename="app">

    <div dojoType="alpine.layout.PopupDialog" draggable="false" id="chorusConfigurationDialog"
         title="Chorus Configuration">
        <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 350px; height: 250px">
            <div dojoType="dijit.layout.ContentPane" region="top"></div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div dojoType="dijit.form.Form" id="chorusConfigurationForm" style="overflow: auto;">
                    <table>
                        <tr>
                            <td><label class="valueLabel">Enter the host address of the Chorus server: </label></td>
                        </tr>
                        <tr>
                            <td>
                                <span class="right">
                                    <input
                                            dojoType="dijit.form.ValidationTextBox"
                                            id="chorusHostTB"
                                            baseClass="inlineTextbox"
                                            style="font-size:11pt;">
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td><label class="valueLabel">Enter the port number of the Chorus server: </label></td>
                        </tr>
                        <tr>
                            <td>
                                <span class="right">
                                    <input
                                            dojoType="dijit.form.ValidationTextBox"
                                            id="chorusPortTB"
                                            baseClass="inlineTextbox"
                                            style="font-size:11pt;">
                                </span>
                            </td>
                        </tr>
                        <tr>
                            <td><label class="valueLabel">Alpine API Key: </label></td>
                        </tr>
                        <tr>
                            <td><label id="chorusAlpineAPIKey" class="right"></label></td>
                        </tr>
                    </table>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter">
                <button baseClass="cancelButton" valign="bottom" id="chorusConfigurationDialog_cancel"
                        dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
                <button baseClass="secondaryButton" valign="bottom" id="chorusConfigurationDialog_test"
                        dojoType="dijit.form.Button" type="button"><fmt:message
                        key="datasource_config_button_test_connect"/></button>
                <button baseClass="primaryButton" valign="bottom" id="chorusConfigurationDialog_ok"
                        dojoType="dijit.form.Button" type="button"><fmt:message key="OK"/></button>
            </div>
        </div>
    </div>

</fmt:bundle>
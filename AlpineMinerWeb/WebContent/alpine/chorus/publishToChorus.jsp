<script type="text/javascript">
    dojo.require("alpine.chorus.PublishChorusUIHelper");
</script>

<fmt:bundle basename="app">

    <div dojoType="alpine.layout.PopupDialog" draggable="false" id="publishChorusDialog" title="Publish to Chorus">
        <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 350px; height: 200px">
            <div dojoType="dijit.layout.ContentPane" region="top">
                <div id="chorusPublishContentTop" style="padding-bottom: 15px;"></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div id="chorusPublishContent"></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter">
                <button baseClass="cancelButton" valign="bottom" id="publishChorus_cancel" dojoType="dijit.form.Button"
                        type="button"><fmt:message key="Cancel"/></button>
                <button baseClass="primaryButton" valign="bottom" id="publishChorus_ok" dojoType="dijit.form.Button"
                        type="button">PUBLISH
                </button>
            </div>
        </div>
    </div>

    <div dojoType="alpine.layout.PopupDialog" draggable="false" id="chorusAPIKeyDialog" title="Chorus API key">
        <div class="innerPadding" dojoType="dijit.layout.LayoutContainer" style="width: 350px; height: 150px">
            <div dojoType="dijit.layout.ContentPane" region="top">
                <div id="chorusAPIKeyContentTop" style="padding-bottom: 15px;"></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="center">
                <div id="chorusAPIKeyContent"></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" class="whiteDialogFooter">
                <button baseClass="cancelButton" valign="bottom" id="chorusAPIKey_cancel" dojoType="dijit.form.Button"
                        type="button"><fmt:message key="Cancel"/></button>
                <button baseClass="primaryButton" valign="bottom" id="chorusAPIKey_ok" dojoType="dijit.form.Button"
                        type="button">SAVE
                </button>
            </div>
        </div>
    </div>

</fmt:bundle>
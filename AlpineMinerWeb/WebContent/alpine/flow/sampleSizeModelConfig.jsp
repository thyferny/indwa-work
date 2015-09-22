<script type="text/javascript">
dojo.require("alpine.flow.SampleSizeModelConfigHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="sampleSizeModelConfig"
		title="<fmt:message key='sample_size_btn4_define'/>" style="overflow: hidden;">
		<div class="titleBar">
            <fmt:message key='sample_size_btn4_define'/>
        </div>
		<div dojoType="dijit.layout.ContentPane" style="width: 600px; height: 480px;overflow:hidden;">
            <div dojoType="dijit.layout.ContentPane" style="width: 100%;height:90%;">
                <div style="margin: 5px;height:422px;width:590px;" id="sample_size_config_grid_container"></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" style="width:100%;height:10%;overflow:hidden;" class="whiteDialogFooter">
                <div dojoType="dijit.form.Button" baseClass="cancelButton"  id="sampleSize_cfg_btn_cancel" type="button" style="margin-top:10px;"><fmt:message key="Cancel" /></div>
                <div dojoType="dijit.form.Button" baseClass="primaryButton" id="sampleSize_cfg_btn_ok" type="button" style="margin-top:10px;"><fmt:message key="OK" /></div>
            </div>

	     </div>
	</div>
</fmt:bundle>
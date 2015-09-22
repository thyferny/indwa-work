<script type="text/javascript">
<!--
	dojo.require("alpine.datasourceexplorer.InspectHadoopFileProperty");
	dojo.require("alpine.datasourceexplorer.HadoopFileDownloadConfigUIHelper");
//-->
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="alpine_datasourceexplorer_InspectHadoopFileProperty_Dialog">
		<div class="titleBar">
            <fmt:message key='hadoop_data_mgr_property_title'/>
        </div>
        <div dojoType="dijit.layout.ContentPane" style="width: 400px; height: 300px">
        	<table width="90%" height="100%" align="center">
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_name"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_name"></label>
        			</td>
        		</tr>
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_owner"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_owner"></label>
        			</td>
        		</tr>
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_group"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_group"></label>
        			</td>
        		</tr>
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_modificationTime"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_modificationTime"></label>
        			</td>
        		</tr>
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_accessTime"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_accessTime"></label>
        			</td>
        		</tr>
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_size"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_size"></label>
        			</td>
        		</tr>
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_blockSize"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_blockSize"></label>
        			</td>
        		</tr>
        		<tr>
        			<td>
        				<fmt:message key="hadoop_data_mgr_property_permission"/>
        			</td>
        			<td>
        				<label id="alpine_datasourceexplorer_InspectHadoopFileProperty_permission"></label>
        			</td>
        		</tr>
        	</table>
        </div>
        <div class="whiteDialogFooter">
                <button type="button" dojoType="dijit.form.Button" baseclass="primaryButton" onClick="dijit.byId('alpine_datasourceexplorer_InspectHadoopFileProperty_Dialog').hide()"><fmt:message key="Done"/></button>
        </div>
	</div>
	
	<div dojoType="dijit.Dialog" draggable="false" id="alpine_datasourceexplorer_HadoopFileDownloadConfig_Dialog">
		<div class="titleBar">
            <fmt:message key='hadoop_data_mgr_property_title'/>
        </div>
        <div dojoType="dijit.layout.ContentPane" style="width: 400px; height: 300px">
        	<div dojoType="dijit.form.Form" id="alpine_datasourceexplorer_HadoopFileDownloadConfig_form">
       			<table width="90%" height="100%" align="center">
	        		<tr>
	        			<td align="left">
	        				<fmt:message key="hadoop_data_mgr_property_name"/>
	        			</td>
	        			<td>
	        				<label id="alpine_datasourceexplorer_HadoopFileDownloadConfig_name"></label>
	        			</td>
	        		</tr>
	        		<tr>
	        			<td align="left">
	        				<fmt:message key="hadoop_data_mgr_property_owner"/>
	        			</td>
	        			<td>
	        				<label id="alpine_datasourceexplorer_HadoopFileDownloadConfig_owner"></label>
	        			</td>
	        		</tr>
	        		<tr>
	        			<td align="left">
	        				<fmt:message key="hadoop_data_mgr_property_group"/>
	        			</td>
	        			<td>
	        				<label id="alpine_datasourceexplorer_HadoopFileDownloadConfig_group"></label>
	        			</td>
	        		</tr>
	        		<tr>
	        			<td align="left">
	        				<fmt:message key="hadoop_data_mgr_property_size"/>
	        			</td>
	        			<td>
	        				<label id="alpine_datasourceexplorer_HadoopFileDownloadConfig_size"></label>
	        			</td>
	        		</tr>
	        		<tr>
	        			<td align="left">
	        				<fmt:message key="hadoop_data_mgr_download_config_startLine"/>
	        			</td>
	        			<td>
	        				<input dojoType="dijit.form.ValidationTextBox" id="alpine_datasourceexplorer_HadoopFileDownloadConfig_startLine" required="true" value = "1" regExp="^[1-9][\d]*$">
	        			</td>
	        		</tr>
	        		<tr>
	        			<td align="left">
	        				<fmt:message key="hadoop_data_mgr_download_config_numberOfLines"/>
	        			</td>
	        			<td>
	        				<input dojoType="dijit.form.ValidationTextBox" id="alpine_datasourceexplorer_HadoopFileDownloadConfig_NumberOfLine" regExp="^[1-9][\d]*$">
	        			</td>
	        		</tr>
                    <tr id="alpine_file_download_tip_container" style="display: none;"><td id="alpine_file_download_tip" colspan="2" style="color:#ff0000;font-size:10px"></td></tr>
	        	</table>
        	</div>
        </div>
        <div class="whiteDialogFooter">
        	<button type="button" dojoType="dijit.form.Button" baseclass="cancelButton" onClick="dijit.byId('alpine_datasourceexplorer_HadoopFileDownloadConfig_Dialog').hide()"><fmt:message key="Close"/></button>
        	<button type="button" dojoType="dijit.form.Button" baseclass="primaryButton" id="alpine_datasourceexplorer_HadoopFileDownloadConfig_submit"><fmt:message key="hadoop_data_mgr_download_config_downloadBtn"/></button>
        </div>
	</div>
	
	<div style="display: none">
		<form method="post" id="alpine_datasourceexplorer_downloadFile_form">
			<input name="connectionInfo" id="alpine_datasourceexplorer_downloadFile_form_connectionJson">
			<input name="from" id="alpine_datasourceexplorer_downloadFile_form_from">
			<input name="numberOfLine" id="alpine_datasourceexplorer_downloadFile_form_numberOfLine">
			<input name="path" id="alpine_datasourceexplorer_downloadFile_form_filePath">
		</form>
	</div>
</fmt:bundle>
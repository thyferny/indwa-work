<script type="text/javascript">
    dojo.require("alpine.import.ImportDataUIHelper");
    dojo.require("alpine.import.UploadFileDisplay");
    dojo.require("alpine.import.DataFormatUIHelper");
</script>
<fmt:bundle basename="app">
    <div id="alpine_import_importData_upload_dialog" dojoType="dijit.Dialog" draggable="false" style="width: 400px;">
        <form method="post" action="UploadFile.php" id="alpine_import_upload_form" enctype="multipart/form-data" >
            <div class="dialogInnerPadding" >
                <div class="lightTitle">
                    <fmt:message key='import_data_dialog_title'/>
                </div>
                <div class="largeSubtitle">
                    <fmt:message key="import_data_pane_upload_title"/>
                </div>
                <div class="dialogInnerPadding" >
                    <%--<div><label class="leftColumnTitle" ><fmt:message key="import_data_pane_upload_label_upload"/></label><input dojoType="dojox.form.FileInput" id="alpine_import_importData_upload_file"/></div>
                    <div><label class="leftColumnTitle" ><fmt:message key="import_data_pane_upload_label_filetype"/></label><select dojoType="dijit.form.Select" id="alpine_import_importData_upload_type"><option></option></select></div>
                    --%>
                    <div dojoType="dijit.form.Form" id="alpine_import_importData_upload_form">
                        <table class="whiteDialogTable">
                            <tr>
                                <td class="leftColumnTitle" style="padding-top: 7px;"><fmt:message key="import_data_pane_upload_label_upload"/></td>
                                <td class="rightColumnControl">
	                            	<span id="SPAN_alpine_import_importData_upload_file"> <input
                                            name="importFile"
                                            multiple="false"
                                            type="file"
                                            data-dojo-type="dojox.form.Uploader"
                                            label="<fmt:message key='CHOOSE_FILE'/>"
                                            id="alpine_import_importData_upload_file"
                                            baseClass="secondaryButton"></span>
                                    <div 	id="alpine_import_uploaded_file_display"
                                             data-dojo-type="alpine.import.UploadFileDisplay"
                                             uploaderId="alpine_import_importData_upload_file"></div>
                                </td>
                            </tr>
                            <%--Save this code for more advanced imports later--%>
                            <tr hidden="true">
                                <td class="leftColumnTitle"><fmt:message key="import_data_pane_upload_label_filetype"/></td>
                                <td class="rightColumnControl">
                                    <select dojoType="dijit.form.Select" id="alpine_import_importData_upload_type" baseclass="greyDropdownButton">
                                        <option>comma or tab-separated (.csv, .tsv)</option>
                                    </select>
                                </td>
                            </tr>
                            <tr>
                                <td class="leftColumnTitle"><fmt:message key="import_data_pane_upload_label_size"/></td>
                                <td class="rightColumnControl" style="padding-left: 5px;">
                                    <input id="alpine_import_importData_upload_size" dojoType="dijit.form.RadioButton" name="uploadRowSize" value="ALL" checked="true"/>
                                    <fmt:message key="import_data_pane_upload_label_size_all"/>
                                    <br/>
                                    <input dojoType="dijit.form.RadioButton" name="uploadRowSize" value="SAMPLE"/>
                                    <fmt:message key="import_data_pane_upload_label_size_simple"/>
	                                <span class="subtleTextbox"><input dojoType="dijit.form.ValidationTextBox"
                                                                       style="width: 100px"
                                                                       id="alpine_import_importData_upload_sampleSize"
                                                                       disabled="true"
                                                                       regExp="^[1-9][\d]*$" required="true" value="10000"></span>
                                </td>
                            </tr>
                            <tr hidden="true">
                                <td class="leftColumnTitle"><fmt:message key="import_data_pane_upload_label_format"/></td>
                                <td class="rightColumnControl" style="padding-left: 5px;">
                                    <input id="alpine_import_importData_upload_format" dojoType="dijit.form.RadioButton" name="uploadFormat" value="AUTO" checked="true"/>
                                    <fmt:message key="import_data_pane_upload_label_format_auto"/>
                                    <br/>
                                    <input dojoType="dijit.form.RadioButton" name="uploadFormat" value="MANUAL"/>
                                    <fmt:message key="import_data_pane_upload_label_format_custom"/>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
            <div class="whiteDialogFooter">
                <button  baseClass="cancelButton" valign = "bottom" onclick="dijit.byId('alpine_import_importData_upload_dialog').hide();" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
                <input  baseClass="primaryButton" valign = "bottom" dojoType="dijit.form.Button" type="button" id="alpine_import_importData_upload_submit" label="<fmt:message key='import_data_pane_upload_button_next'/>">
            </div>  <!--end dialogFooter-->
        </form>
    </div>

    <div id="alpine_import_dataformat_dialog" dojoType="dijit.Dialog" draggable="false" style="width: 1000px; max-height: 660px">
        <%--<div dojoType="dijit.layout.ContentPane" style="width: 1000px; height: 650px;">--%>
        <form id="alpine_import_dataformat_form" dojoType="dijit.form.Form">
	        <div class="dialogInnerPadding" >
	            <table width="100%">
	                <tr>
	                    <td>
	                        <div class="lightTitle">
	                            <fmt:message key='import_data_dialog_title'/>
	                        </div>
	                    </td>
	                    <td align="right">
	                        <a href="#" id="alpine_import_dataformat_option_switcher" style="font-size: 11px; outline:none;"><fmt:message key="import_data_pane_format_label_show_additional_option"/></a>
	                    </td>
	                </tr>
	            </table>
	            <div class="largeSubtitle">
	                <span><fmt:message key="import_data_pane_format_title"/></span>
	                <span style="padding-left: 10px; padding-bottom: 5px;">
		                <input dojoType="dijit.form.ValidationTextBox"
		                       style="width: 400px"
		                       baseClass="alpineImportTextbox"
		                       id="alpine_import_dataformat_tablename"
		                       regExp="^[a-zA-Z]{1}([\w]|[_]){0,19}$" required="true">
	                </span>
	            </div>
	        </div>
	        <div id="alpine_import_dataformat_option_container" class="dialogInnerPadding" style="display: none;" >
	            <table class="whiteDialogTable">
	                <tr>
	                    <td class="alpineImportFormatOptionName">
	                        <fmt:message key="import_data_pane_format_label_delimiter"/>
	                    </td>
	                    <td class="alpineImportFormatOption">
	                        <select dojoType="dijit.form.Select" id="alpine_import_dataformat_option_delimiter" baseClass="greyDropdownButton" style="width:80px">
	                            <option value="Comma"><fmt:message key="hadoop_prop_configure_columns_separator_Comma"/></option>
	                            <option value="Tab"><fmt:message key="hadoop_prop_configure_columns_separator_Tab"/></option>
	                            <option value="Pipe">Pipe (|)</option>
	                            <option value="Semicolon"><fmt:message key="hadoop_prop_configure_columns_separator_Semicolon"/></option>
	                            <option value="Space"><fmt:message key="hadoop_prop_configure_columns_separator_Space"/></option>
	                            <option value="Other"><fmt:message key="hadoop_prop_configure_columns_separator_Other"/></option>
	                        </select>
	                        <input id="alpine_import_dataformat_delimiter" dojoType="dijit.form.ValidationTextBox" regExp="[\W]{1}" style="width: 20px; display: none">
	                    </td>
	                    <td class="alpineImportFormatOptionName">
	                        <fmt:message key="import_data_pane_format_label_escape_chartacter"/>
	                    </td>
	                    <td class="alpineImportFormatOption">
	                        <input id="alpine_import_dataformat_option_escape" dojoType="dijit.form.ValidationTextBox" maxLength="1" required="true" baseClass="alpineImportTextbox" style="width: 20px">
	                    </td>
	                    <td class="alpineImportFormatOptionName">
	                        <fmt:message key="import_data_pane_format_label_quote_chartacter"/>
	                    </td>
	                    <td class="alpineImportFormatOption">
	                        <input id="alpine_import_dataformat_option_quote" dojoType="dijit.form.ValidationTextBox" maxLength="1" required="true" baseClass="alpineImportTextbox" style="width: 20px">
	                    </td>
	                </tr>
	                <tr>
	                    <td style="width:150px">
	                        <input dojoType="dijit.form.CheckBox"  id="alpine_import_dataformat_option_header" checked="true" /><fmt:message key="hadoopPropery_Config_Colum_Include_Header"/>
	                    </td>
	                </tr>
	            </table>
	        </div>
	        <div id="alpine_import_dataformat_formatGrid" style="max-height: 450px; overflow: auto;"></div>
        </form>
        <div class="dialogInnerPadding" >
            <div class="whiteDialogFooter">
                <button  baseClass="cancelButton" valign = "bottom" id="alpine_import_dataformat_format_cancel" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
                <button  baseClass="primaryButton" valign = "bottom" dojoType="dijit.form.Button" type="button" id="alpine_import_dataformat_format_submit"><fmt:message key='import_data_pane_format_button_next'/></button>
                <button  baseClass="primaryButton" valign = "bottom" dojoType="dijit.form.Button" type="button" id="alpine_import_dataformat_format_abort" style="display: none"><fmt:message key='import_data_pane_format_button_abort'/></button>
            </div>
        </div>
        <%--</div>--%>
    </div>
    
    <div id="alpine_import_dataformat_errorMsgDialog" draggable="false" dojoType="alpine.layout.PopupDialog" title="<fmt:message key='import_data_error_grid_title'/>">
    	<div class="innerPadding">
            <div dojoType="dijit.layout.ContentPane" style="width: 600px; height: 500px">
                <div id="alpine_import_dataformat_errorMsgGrid"></div>
            </div>
    	</div>
		<div class="whiteDialogFooter">
			<button type="button" dojoType="dijit.form.Button" baseclass="primaryButton" onclick="dijit.byId('alpine_import_dataformat_errorMsgDialog').hide()"><fmt:message key="Done"/></button>
		</div>
    </div>
    
    
    <div dojoType="dijit.Dialog" id="alpine_import_importData_hd_upload_dialog" title="<fmt:message key='import_data_pane_upload_title'/>">
    	<div class="titleBar">
            <fmt:message key='import_data_pane_upload_title'/>
        </div>
    	<div dojoType="dijit.layout.ContentPane" style="width: 300px; height: 50px">
	    	<input name="importFile"
	               multiple="false"
	               type="file"
	               showInput="before"
	               data-dojo-type="dojox.form.Uploader"
	               label="<fmt:message key='CHOOSE_FILE'/>"
	               id="alpine_import_importData_hd_upload_file"
	               baseClass="secondaryButton">
    	</div>
		<div class="whiteDialogFooter">
			<button  baseClass="cancelButton" valign = "bottom" onclick="dijit.byId('alpine_import_importData_hd_upload_dialog').hide();" dojoType="dijit.form.Button" type="button"><fmt:message key="Cancel"/></button>
            <button  baseClass="primaryButton" valign = "bottom" dojoType="dijit.form.Button" type="button" id="alpine_import_importData_hd_upload_submit"><fmt:message key='import_data_pane_button_upload'/></button>
		</div>
    </div>
</fmt:bundle>
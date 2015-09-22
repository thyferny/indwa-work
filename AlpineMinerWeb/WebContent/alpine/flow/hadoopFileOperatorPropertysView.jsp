<%--
  Created by IntelliJ IDEA.
  User: will
  Date: 12-5-31
--%>
<script type="text/javascript">
    dojo.require("alpine.props.HadoopFileOperatorPropertyHelper");
    dojo.require("alpine.props.HadoopFileStructure4LogHelper");
    dojo.require("alpine.props.HadoopFileStructure4XMLHelper");
    dojo.require("alpine.props.HadoopFileStructure4JSONHelper");
</script>
<fmt:bundle basename="app">
    <div dojoType="dijit.Dialog" id="hadoopFileStructureCfgDlg" draggable="false" title="<fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>">
        <div class="titleBar">
            <fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>
        </div>
        <div dojoType="dijit.layout.LayoutContainer" style="width: 850px;height:530px">
            <div dojoType="dijit.layout.ContentPane" style="width:100%;" region="top">
                <div id="hadoopPropery_Config_Colum_title" class="largeSubtitle">
                    <label><fmt:message key='hadoop_prop_configure_columns_dialog_head_tile' /></label>
                    <label id="hadoopPropery_Config_Colum_file_name" style="border-bottom:1px dotted #c9c9c9;padding-left: 10px;"></label>
                </div>
                <div class="dialogInnerPadding">
                    <table class="whiteDialogTable" id="hadoopPropery_Config_Colum_escape_quote_container">
                        <tr>
                            <td class="alpineImportFormatOptionName" style="width:70px;"><fmt:message key="import_data_pane_format_label_escape_chartacter"/></td>
                            <td class="alpineImportFormatOption"><input id="hadoopPropery_Config_Colum_file_option_escape" dojoType="dijit.form.ValidationTextBox" maxLength="1" required="false" baseClass="alpineImportTextbox" style="width: 20px"></td>
                            <td class="alpineImportFormatOptionName" style="width:70px;"><fmt:message key="import_data_pane_format_label_quote_chartacter"/></td>
                            <td class="alpineImportFormatOption"><input id="hadoopPropery_Config_Colum_file_option_quote" dojoType="dijit.form.ValidationTextBox" maxLength="1" required="false" baseClass="alpineImportTextbox" style="width: 20px"></td>
                        </tr>
                    </table>
                    <div id="hadoopPropery_Config_Colum_Choose_Separator_Container" style="padding-bottom: 5px;">
                        <label style="font-weight:bold;"><fmt:message key='hadoop_prop_configure_columns_dialog_separator_tile'/></label>
                        <label>
                            <input type="radio" name="Separator_Type" value="Tab" />
                            <fmt:message key="hadoop_prop_configure_columns_separator_Tab"/></label>
                        <label>
                            <input type="radio" name="Separator_Type" value="Comma" />
                            <fmt:message key="hadoop_prop_configure_columns_separator_Comma"/></label>
                        <label>
                            <input type="radio" name="Separator_Type" value="Semicolon" />
                            <fmt:message key="hadoop_prop_configure_columns_separator_Semicolon"/></label>
                        <label>
                            <input type="radio" name="Separator_Type" value="Space" />
                            <fmt:message key="hadoop_prop_configure_columns_separator_Space"/></label>
                        <label>
                            <input type="radio" name="Separator_Type" value="Other" id="hadoopPropery_Config_Colum_OtherSeparator_Radio" />
                            <fmt:message key="hadoop_prop_configure_columns_separator_Other"/>
                        </label>
                        <input type="text" name="hadoopPropery_Config_Colum_OtherSeparator_Value"  disabled="disabled" onkeyup="if(this.value!=null){this.value=this.value.substr(0,1);if(/[\W]/.test(this.value)==false){this.value=''}}"  id="hadoopPropery_Config_Colum_OtherSeparator_Value"/>
                        <label style="float:right;margin-right: 10px;">
                            <input dojoType="dijit.form.CheckBox"  id="hadoopPropery_fileStructure_Include_Header" style="vertical-align: bottom;" />
                            <fmt:message key="hadoopPropery_Config_Colum_Include_Header"/></label>
                    </div>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" style="width:100%; overflow:auto;" id="hadoopPropery_Config_Colum_List_Container_pane" region="center">
                <div id="hadoopPropery_Config_Colum_List_Container" style="max-height: 350px;"></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" style="width:90%;" region="bottom">
                <div id="hadoopPropery_Config_Colum_Dlg_Btn_Container" class="whiteDialogFooter">
                    <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button" id="hadoopFileStructure_Dlg_Btn_Cancel"><fmt:message key="Cancel"/></div>
                    <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button" id="hadoopFileStructure_Dlg_Btn_OK"><fmt:message key="OK"/></div>
                </div>
            </div>
        </div>
    </div>

    <%--For choose hadoop file--%>
    <div dojoType="dijit.Dialog" id="hadoopPropery_Choose_HadoopFile_Dlg" draggable="false" title="<fmt:message key='hadoop_prop_choose_file_dlg_title'/>">
        <div class="titleBar">
            <fmt:message key='hadoop_prop_choose_file_dlg_title'/>
        </div>
        <div dojoType="dijit.layout.LayoutContainer" style="width: 400px;height:450px">
            <div dojoType="dijit.layout.ContentPane" style="height:100%;" region="center">
                <%--<div id="hadoopPropery_Choose_HadoopFile_Title" style="margin: 0px 5px;font-weight: bold;"><fmt:message key="hadoopPropery_Choose_HadoopFile_Title"/></div>--%>
                <div class="innerPadding" id="hadoopPropery_Choose_HadoopFile_Tree_Container" style="overflow:auto;"></div>
            </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom">
                <div id="hadoopPropery_Choose_HadoopFile_Foot_Container" class="whiteDialogFooter">
                    <input type="hidden" id="hadoopPropery_Choose_HadoopFile_hidden" />
                    <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button" id="hadoopPropery_Choose_HadoopFile_Dlg_Btn_Cancel"><fmt:message key="Cancel"/></div>
                    <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button" id="hadoopPropery_Choose_HadoopFile_Dlg_Btn_Done"><fmt:message key="OK"/></div>
                </div>
            </div>
        </div>
    </div>

    <%--for xml--%>
    <div dojoType="dijit.Dialog" id="hadoopFileStructureCfgDlg4XML" draggable="false" style="width: 850px;height:630px;overflow:hidden;" title="<fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>">
        <div class="titleBar">
            <fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>
        </div>
        <div dojoType="dijit.layout.LayoutContainer" style="width: 850px;height:600px;overflow:hidden;">
            <div  dojoType="dijit.layout.ContentPane" region="center" style="height:580px;overflow: hidden;">
                <div style="height: 325px;width:850px;overflow: hidden;">
                    <div class="largeSubtitle">
                        <label><fmt:message key='hadoop_prop_configure_columns_dialog_head_tile' /></label>
                        <label id="hadoopPropery_Config_Colum_file_name4xml" style="border-bottom:1px dotted #c9c9c9;padding-left: 10px;"></label>
                    </div>
                    <%--<div>--%>
                        <%--&lt;%&ndash;label&ndash;%&gt;--%>
                        <%--<label style="font-weight:bold;margin-left: 15px"><fmt:message key="hadoop_prop_file_structure_xml_btn_container_element"/>:</label>--%>
                            <%--<input id="hadoop_prop_file_structure_xml_input_container" dojoType="dijit.form.ValidationTextBox"  required="false" baseClass="alpineImportTextbox" style="width: 150px;">--%>
                            <%--<input type="hidden" value="" id="hadoop_prop_file_structure_xml_hidden_containerMode" />--%>
                            <%--&lt;%&ndash;<label style="font-weight:bold;margin-left: 35px"><fmt:message key="hadoop_prop_file_structure_xml_btn_root_element"/>:</label>&ndash;%&gt;--%>
                            <%--&lt;%&ndash;<input id="hadoop_prop_file_structure_xml_input_root" dojoType="dijit.form.ValidationTextBox"  required="false" baseClass="alpineImportTextbox" style="width: 150px">&ndash;%&gt;--%>
                    <%--</div>--%>
                    <fieldset class="xmlfileStruct" style="position:relative;">
                        <div id="mask_xmlfileStruct_column_define" style="width:834px;height:148px;position:absolute;top:17px;left: 2px;text-align:center;vertical-align: middle;background-color:#e9e9e9;filter:alpha(opacity=50);-moz-opacity:0.5;-khtml-opacity: 0.5;opacity: 0.5;padding-top:80px;z-index:99;color:#ffffff;display:none;"><img src="../../images/loading.gif" alt=""></div>
                        <%--<legend><fmt:message key="hadoop_prop_file_structure_xml_column_define_container"/></legend>--%>
                        <div style="height:25px;width:840px;">
                            <div style="float: left;;width:390px;padding-left: 10px;font-weight:bolder;"><fmt:message key="hadoop_prop_file_structure_tree_select_container_label" /></div>
                            <div style="float: left;;width:400px">
                                <label style="font-weight:bold;margin-left: 20px"><fmt:message key="hadoop_prop_file_structure_tree_column_define_container_label"/></label>
                                <input id="hadoop_prop_file_structure_xml_input_container" dojoType="dijit.form.ValidationTextBox"  required="false" baseClass="alpineImportTextbox" style="width: 150px;">
                                <input type="hidden" value="" id="hadoop_prop_file_structure_xml_hidden_containerMode" />
                                <input type="hidden" value="" id="hadoop_prop_file_structure_xml_hidden_containerPath" />
                            </div>
                        </div>
                        <div id="hadoop_prop_file_structure_xml_container_gess_tip" style="font-size:10px;color: #c0c0c0;padding-top:5px;clear: both;margin-left:10px;">ssss</div>
                        <div id="xmlDomTreeContainer" style="float: left;margin: 10px;width:390px;height:180px;background-color: #E9E9E9;overflow:auto;"></div>
                        <div id="hadoop_prop_file_structure_xml_column_define_container" style="padding:10px;height:176px;width: 400px;float:left;">
                                <%--grid--%>
                        </div>

                        <div style="clear:both;padding-left: 2px;margin-top:2px;margin-bottom:2px">
                                <%--button--%>
                            <div style="width: 408px;text-align: left;float: left;position:relative;">
                                    <%--<span style="font-size:11px;color:#808080;position: absolute;left: 0px;"><fmt:message key="hadoop_prop_file_structure_tree_container_tip" /></span>--%>
                                <input type="hidden" value="" id="hadoop_file_struct_xml_tree_node_select" />
                                <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_tree_select_level" baseClass="secondaryButton"><fmt:message key="hadoop_prop_file_structure_tree_select_level"/></button>
                                <button type="button"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_loadData" baseClass="secondaryButton" title="<fmt:message key="hadoop_prop_file_structure_xml_btn_load_data"/>"><fmt:message key="hadoop_prop_file_structure_xml_btn_load_data"/></button>
                                <button type="button"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_preview" baseClass="secondaryButton"><fmt:message key="hadoop_prop_file_structure_xml_btn_preview_struct"/></button>
                            </div>
                            <div style="width: 420px;float: left;text-align:left">
                            <%--<button type="button"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_add" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_add"/></button>--%>
                            <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_delete" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_delete"/></button>
                            <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_moveup" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_moveup"/></button>
                            <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_movedown" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_movedown"/></button>
                            </div>

                            <div style="clear: both;"></div>
                        </div>

                    </fieldset>

                </div>
                <fieldset class="xmlfileStruct" style="margin-top: 20px;padding:5px 10px;position: relative;"><legend><fmt:message key="hadoop_prop_file_structure_xml_btn_preview_struct"/></legend>
                    <div id="mask_xmlfileStruct_preview" style="width:834px;height:132px;position:absolute;top:15px;left: 2px;text-align:center;vertical-align: middle;background-color:#e9e9e9;filter:alpha(opacity=50);-moz-opacity:0.5;-khtml-opacity: 0.5;opacity: 0.5;padding-top:80px;z-index:99;color:#ffffff;display:none;"><img src="../../images/loading.gif" alt=""></div>
                    <div style="height: 200px" id="hadoop_prop_file_structure_xml_preview_grid_container">

                    </div>
                </fieldset>

            </div>
            <div  dojoType="dijit.layout.ContentPane" region="bottom" style="height:40px;overflow: hidden;">
                <div id="hadoopPropery_Config_Colum_Dlg4XML_Btn_Container" class="whiteDialogFooter">
                    <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button" id="hadoopFileStructure_4XML_Dlg_Btn_Cancel"><fmt:message key="Cancel"/></div>
                    <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button" id="hadoopFileStructure_4XML_Dlg_Btn_OK"><fmt:message key="OK"/></div>
                </div>
            </div>
        </div>
    </div>
    <%--for log--%>
    <div dojoType="dijit.Dialog" id="hadoopFileStructureCfgDlg4Log" draggable="false" style="width: 700px;height:550px;overflow:hidden;" title="<fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>">
        <div dojoType="dijit.layout.LayoutContainer" style="width: 700px;height:550px;overflow:hidden;">
         <div dojoType="dijit.layout.ContentPane" region="center" style="width: 700px;height:490px;overflow:hidden;">
                <%--title--%>
                <div class="titleBar">
                    <fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>
                </div>
                    <%--subtitle--%>
                <div class="largeSubtitle">
                    <label><fmt:message key='hadoop_prop_configure_columns_dialog_head_tile' /></label>
                    <label id="hadoopPropery_Config_Colum_file_name4log" style="border-bottom:1px dotted #c9c9c9;padding-left: 10px;"></label>
                </div>
                <div id="log_config_container" style="width:680px;padding:10px;height:60px">
                     <div style="margin: 10px 0px 10px 20px;width:255px"><label style="font-weight:bold;">Log Type: </label>
                         <select name="structure4log_fileType" id="structure4log_fileType" style="width: 160px;" dojoType="dijit.form.Select" baseClass="greyDropdownButton">
                         <option value="">&nbsp;</option>
                         <option value="Apache Log">Apache Web Server</option>
                         <option value="Log4J">Log4J</option>
                     </select></div>
                     <div style="margin: 10px 0px 10px 20px;width:630px"><label style="font-weight:bold;">Format: </label>
                         <span id="hadoop_prop_file_structure_Log_input_format_Container">
                              <select  id="hadoop_prop_file_structure_Log_input_format" dojoType="dijit.form.ComboBox"  required="false"  baseClass="greyDropdownButton" style="width: 450px;margin-left:12px">
                              </select>
                         </span>
                         <button type="button"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_log_preview" baseClass="secondaryButton"><fmt:message key="hadoop_prop_file_structure_xml_btn_preview_struct"/></button>
                     </div>
                </div>
                <fieldset style="width:666px;padding:10px;height:335px;border: 1px solid #f0f3f3!important;margin: 0 5px!important;position:relative;"><legend style="margin-left:10px">Preview</legend>
                    <div id="mask_LogfileStruct_preview" style="width:685px;height:225px;position:absolute;top:15px;left: 2px;text-align:center;vertical-align: middle;background-color:#e9e9e9;filter:alpha(opacity=50);-moz-opacity:0.5;-khtml-opacity: 0.5;opacity: 0.5;padding-top:80px;z-index:99;color:#ffffff;display:none;"><img src="../../images/loading.gif" alt=""></div>
                    <div style="height: 294px;width:670px;overflow:auto;" id="log_preview_container">

                    </div>
                </fieldset>
             </div>
            <div id="hadoopPropery_Config_Colum_Dlg4Log_Btn_Container" class="whiteDialogFooter" dojoType="dijit.layout.ContentPane" region="bottom">
                <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button"
                     id="hadoopFileStructure_4Log_Dlg_Btn_Cancel"><fmt:message key="Cancel"/></div>
                <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button"
                     id="hadoopFileStructure_4Log_Dlg_Btn_OK"><fmt:message key="OK"/></div>
            </div>
        </div>
    </div>

    <%--for json--%>
    <div dojoType="dijit.Dialog" id="hadoopFileStructureCfgDlg4json" draggable="false" style="width: 850px;height:630px;overflow:hidden;" title="<fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>">
        <div class="titleBar">
            <fmt:message key='hadoop_prop_configure_columns_dialog_tile'/>
        </div>
        <div dojoType="dijit.layout.LayoutContainer" style="width: 850px;height:600px;overflow:hidden;">
            <div  dojoType="dijit.layout.ContentPane" region="center" style="height:580px;overflow: hidden;">
                <div style="height: 325px;width:850px;overflow: hidden;">
                    <div class="largeSubtitle">
                        <label><fmt:message key='hadoop_prop_configure_columns_dialog_head_tile' /></label>
                        <label id="hadoopPropery_Config_Colum_file_name4json" style="border-bottom:1px dotted #c9c9c9;padding-left: 10px;"></label>
                    </div>
                    <%--<div>--%>
                            <%--&lt;%&ndash;label&ndash;%&gt;--%>
                            <%--&lt;%&ndash;<label style="font-weight:bold;margin-left: 35px"><fmt:message key="hadoop_prop_file_structure_xml_btn_root_element"/>:</label>&ndash;%&gt;--%>
                            <%--&lt;%&ndash;<input id="hadoop_prop_file_structure_xml_input_root" dojoType="dijit.form.ValidationTextBox"  required="false" baseClass="alpineImportTextbox" style="width: 150px">&ndash;%&gt;--%>
                    <%--</div>--%>
                    <fieldset class="xmlfileStruct" style="position:relative;">
                        <div id="mask_jsonfileStruct_column_define" style="width:834px;height:126px;position:absolute;top:30px;left: 2px;text-align:center;vertical-align: middle;background-color:#e9e9e9;filter:alpha(opacity=50);-moz-opacity:0.5;-khtml-opacity: 0.5;opacity: 0.5;padding-top:80px;z-index:99;color:#ffffff;"><img src="../../images/loading.gif" alt=""></div>
                        <%--<legend>Columns</legend>--%>
                        <div style="height:30px;width:840px;padding-top:10px;">
                            <div style="float: left;;width:390px;padding-left: 10px;font-weight:bolder;"><fmt:message key="hadoop_prop_file_structure_tree_select_container_json_label" /></div>
                            <div style="float: left;;width:250px">
                                <label style="font-weight:bold;margin-left: 20px"><fmt:message key="hadoop_prop_file_structure_tree_column_define_container_label"/></label>
                                <input id="hadoop_prop_file_structure_json_input_container" dojoType="dijit.form.ValidationTextBox"  required="false" baseClass="alpineImportTextbox" style="width: 100px;">
                                <input id="hadoop_prop_file_structure_json_type" type="hidden" value="">
                                <input id="hadoop_prop_file_structure_json_containerpath" type="hidden" value="">
                            </div>
                            <div id="hadoop_prop_file_structure_json_container_tip" style="font-size:10px;color: #c0c0c0;padding-top:5px;clear: both;margin-left:10px;"></div>

                           <%--<div style="float: left;;width:150px;padding-bottom:5px;">--%>
                               <%--<select name="hadoop_prop_file_structure_json_container_type" data-dojo-type="dijit.form.Select" id="hadoop_prop_file_structure_json_container_type" style="width:150px;" baseClass="greyDropdownButton">--%>
                                   <%--<option value="sts"  selected="selected">Standard</option>--%>
                                   <%--<option value="stl">Line</option>--%>
                                   <%--<option value="stp">Pure Data Array</option>--%>
                               <%--</select>--%>
                           <%--</div>--%>
                        </div>
                        <div id="jsonDomTreeContainer" style="float: left;margin: 15px 10px 10px;width:390px;height:188px;background-color: #E9E9E9;overflow:auto;"></div>
                        <div id="hadoop_prop_file_structure_json_column_define_container" style="padding:10px 10px 10px;height:188px;width: 400px;float:left;overflow-y:auto;">
                                <%--grid--%>
                        </div>

                        <div style="clear:both;padding-left: 0px;margin-top:2px;margin-bottom:2px">
                                <%--button--%>
                            <div style="width: 430px;float: left;position:relative;">
                                <%--<span style="font-size:11px;color:#808080;position: absolute;left: 0px;"><fmt:message key="hadoop_prop_file_structure_tree_container_tip" /></span>--%>
                                    <input type="hidden" value="" id="hadoop_file_struct_json_tree_node_select" />
                                    <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_json_tree_select_level" baseClass="secondaryButton"><fmt:message key="hadoop_prop_file_structure_tree_select_level"/></button>
                                    <button type="button"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_json_loadData" baseClass="secondaryButton" title="<fmt:message key="hadoop_prop_file_structure_xml_btn_load_data"/>"><fmt:message key="hadoop_prop_file_structure_xml_btn_load_data"/></button>
                                    <button type="button"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_json_preview" baseClass="secondaryButton"><fmt:message key="hadoop_prop_file_structure_xml_btn_preview_struct"/></button>
                            </div>
                            <div style="width: 400px;float: left;">
                                    <%--<button type="button"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_xml_add" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_add"/></button>--%>
                                <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_json_delete" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_delete"/></button>
                                <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_json_moveup" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_moveup"/></button>
                                <button type="button" disabled="disabled"  dojoType="dijit.form.Button" id="hadoop_prop_file_structure_json_movedown" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_movedown"/></button>
                            </div>
                            <div style="clear: both;"></div>
                        </div>

                    </fieldset>

                </div>
                <fieldset class="xmlfileStruct" style="margin-top: 20px;padding:5px 10px;position:relative;"><legend>Preview</legend>
                    <div id="mask_JSONfileStruct_preview" style="width:834px;height:132px;position:absolute;top:15px;left: 2px;text-align:center;vertical-align: middle;background-color:#e9e9e9;filter:alpha(opacity=50);-moz-opacity:0.5;-khtml-opacity: 0.5;opacity: 0.5;padding-top:80px;z-index:99;color:#ffffff;display:none;"><img src="../../images/loading.gif" alt=""></div>
                    <div style="height: 200px" id="hadoop_prop_file_structure_json_preview_grid_container">

                    </div>
                </fieldset>

            </div>
            <div  dojoType="dijit.layout.ContentPane" region="bottom" style="height:40px;overflow: hidden;">
                <div id="hadoopPropery_Config_Colum_Dlg4json_Btn_Container" class="whiteDialogFooter">
                    <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button" id="hadoopFileStructure_4json_Dlg_Btn_Cancel"><fmt:message key="Cancel"/></div>
                    <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button" id="hadoopFileStructure_4json_Dlg_Btn_OK"><fmt:message key="OK"/></div>
                </div>
            </div>
        </div>
    </div>
</fmt:bundle>
<%--
  User: Will
  File:hadoopPigExecuteFileStructure
  Date: 12-10-10
--%>
<script type="text/javascript">
    dojo.require("alpine.props.HadoopPigExecuteFileStructureHelper");
</script>
<fmt:bundle basename="app">
    <div dojoType="dijit.Dialog" id="hadoopPigExecuteFileStructureCfgDlg" draggable="false"
         title="<fmt:message key='hadoop_pig_execute_pigExecuteFileStructure'/>">
       <div class="titleBar">
            <fmt:message key='pig_define_struct_button'/>
        </div>
        <div dojoType="dijit.layout.LayoutContainer" style="width: 850px;height:530px">
            <div dojoType="dijit.layout.ContentPane" style="height:100%; width:100%;" region="center">
                <table width="820" border="0" cellspacing="10" cellpadding="0">
                    <tr>
                        <td colspan="7" class="largeSubtitle"><fmt:message key='hadoop_prop_configure_columns_dialog_head_tile' /><label id="pigExecFileNameLabel" style="border-bottom:1px dotted #c9c9c9;padding-left: 10px;"></label></td>
                    </tr>
                    <tr style="vertical-align:middle;">
                        <td class="alpineImportFormatOptionName" style="width:140px;vertical-align:bottom;padding-bottom: 0px"><fmt:message key="import_data_pane_format_label_escape_chartacter"/></td>
                        <td class="alpineImportFormatOption" style="vertical-align:bottom;padding-bottom: 0px"><input id="hadoop_pigexecute_escape" dojoType="dijit.form.ValidationTextBox" maxLength="1" required="false" baseClass="alpineImportTextbox" style="width: 20px"></td>
                        <td class="alpineImportFormatOptionName" style="width:140px;vertical-align:bottom;padding-bottom: 0px"><fmt:message key="import_data_pane_format_label_quote_chartacter"/></td>
                        <td class="alpineImportFormatOption" style="vertical-align:bottom;padding-bottom: 0px"><input id="hadoop_pigexecute_quote" dojoType="dijit.form.ValidationTextBox" maxLength="1" required="false" baseClass="alpineImportTextbox" style="width: 20px"></td>
                        <td>&nbsp;</td>
                        <td>
                            <label><input dojoType="dijit.form.CheckBox"  id="hadoop_pigexecute_includeHeader" type="checkbox" />
                            <fmt:message key="hadoopPropery_Config_Colum_Include_Header"/></label>
                        </td>
                        <td>&nbsp;</td>
                    </tr>
                    <tr>
                        <td colspan="7" style="vertical-align:middle;">
                            <table width="100%" align="center" class="seperatorTable"><tr>
                                <td style="font-weight: bold;"><fmt:message key='hadoop_prop_configure_columns_dialog_separator_tile'/></td>
                                <td><label><input type="radio" name="Pig_Separator_Type" value="Tab" checked="checked" />
                                    <fmt:message key="hadoop_prop_configure_columns_separator_Tab"/></label></td>
                                <td><label><input type="radio" name="Pig_Separator_Type" value="Comma" />
                                    <fmt:message key="hadoop_prop_configure_columns_separator_Comma"/></label></td>
                                <td><label> <input type="radio" name="Pig_Separator_Type" value="Semicolon" />
                                    <fmt:message key="hadoop_prop_configure_columns_separator_Semicolon"/></label></td>
                                <td><label><input type="radio" name="Pig_Separator_Type" value="Space" />
                                    <fmt:message key="hadoop_prop_configure_columns_separator_Space"/></label></td>
                                <td><label><input type="radio" name="Pig_Separator_Type" value="Other" id="hadoop_pigexecute_other_Radio" />
                                    <fmt:message key="hadoop_prop_configure_columns_separator_Other"/></label>
                                    <input type="text" name="hadoop_pigexecute_other_value" disabled="disabled"  id="hadoop_pigexecute_other_value" dojoType="dijit.form.ValidationTextBox" maxLength="1" required="false" style="width:80px;" />
                                </td>
                            </tr></table>
                        </td>
                    </tr>
                </table>
                <div id="hadoopPigExecuteFileStructure_Define_Container" style="max-height: 350px; overflow: hidden;">
                    <div id="columnDefineBtnContainer" style="height: 40px">
                        <button type="button"  dojoType="dijit.form.Button" id="hadoop_pigexecute_column_add" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_add"/></button>
                        <button type="button"  dojoType="dijit.form.Button" id="hadoop_pigexecute_column_delete" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_delete"/></button>
                        <button type="button"  dojoType="dijit.form.Button" id="hadoop_pigexecute_column_moveup" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_moveup"/></button>
                        <button type="button"  dojoType="dijit.form.Button" id="hadoop_pigexecute_column_movedown" baseClass="secondaryButton"><fmt:message key="hadoop_pigexecute_column_movedown"/></button>
                    </div>
                    <div id="columnDefineGridContainer" style="height:300px;width:830px;overflow: hidden;margin:0px 10px"></div>
                </div>
            </div>
            <div dojoType="dijit.layout.ContentPane" style="width:90%;" region="bottom">
                <div class="whiteDialogFooter">
                    <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button"
                         id="hadoopPigExecuteFileStructure_Dlg_Btn_Cancel"><fmt:message key="Cancel"/></div>
                    <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button"
                         id="hadoopPigExecuteFileStructure_Dlg_Btn_OK"><fmt:message key="OK"/></div>
                </div>
            </div>
        </div>
    </div>
</fmt:bundle>

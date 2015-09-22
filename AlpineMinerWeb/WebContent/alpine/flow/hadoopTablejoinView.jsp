<!-- THIS IS NO LONGER BEING USED!!!!!!!! -->
<script type="text/javascript">
    //dojo.require("alpine.props.HadoopJoinHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false"		id="hadoopJoinDialog" title='<fmt:message key="Edit_Table_Join" />'>
        <div dojoType="dijit.layout.ContentPane" style="width:700px;height:500px;position:relative;">
            <div style="width: 100%;height: 230px;overflow:hidden;"><fieldset style="margin:0px 8px;border:1px solid #D2D2C2;overflow:hidden;"><legend style="margin-left: 10px;"><fmt:message key="hadoop_join_choose_join_column_legend" /> </legend>
                <div style="margin:3px 8px;overflow-y:auto;height:200px">
                    <div id="hadoop_select_join_column_grids_container">
                      <fieldset><legend>ssss</legend></fieldset>
                      <fieldset class="Even"><legend>ssss</legend></fieldset>
                      <fieldset><legend>ssss</legend></fieldset>
                      <fieldset class="Even"><legend>ssss</legend></fieldset>
                    </div>
                </div>
            </fieldset></div>
            <div style="width: 100%;height: 230px">
                <fieldset style="margin:0px 8px;padding:0px;border:1px solid #D2D2C2;overflow:hidden;"><legend style="margin-left: 10px;"><fmt:message key="hadoop_join_set_condition_legend" /> </legend>
                    <div style="margin:0px 10px;height:30px;"><table width="100%" border="0" cellspacing="0" cellpadding="0">
                        <tr>
                            <td width="15%" align="center"><fmt:message key="hadoop_join_set_condition_choose_type" /></td>
                            <td align="left">
                                <select style="width:100px;" dojoType="dijit.form.Select" id="hadoop_join_config_choose_condition_type" baseClass="greyDropdownButtonFixedWidth greyDropdownButton">
                                    <option value="JOIN">JOIN</option>
                                    <option value="LEFT OUTER">LEFT OUTER</option>
                                    <option value="RIGHT OUTER">RIGHT OUTER</option>
                                    <option value="FULL OUTER">FULL OUTER</option>
                                </select>
                            </td>
                            <td width="15%" align="right">
                                <button type="button" disabled="disabled" dojoType="dijit.form.Button" id="hadoop_join_condition_delete_btn" baseClass="secondaryButton"><fmt:message key="delete_button"/></button>
                            </td>
                        </tr>
                        </table>
                    </div>
                    <div style="margin:3px 8px;overflow-y:auto;height:155px">
                        <div id="hadoop_join_set_condition_container" style="height:150px;"></div>
                    </div>
                </fieldset>
            </div>
            <div></div>
            <div style="position: absolute;bottom:0px;left:0;width: 100%;height:30px;padding-top:10px;"
                 class="dialogFooter">
                <button type="button" baseClass="dialogButton" dojoType="dijit.form.Button" id="hadoopJoin_dlg_btn_OK">
                    <fmt:message key="OK"/></button>
                <button type="button" baseClass="dialogButton" dojoType="dijit.form.Button"
                        id="hadoopJoin_dlg_btn_Cancel"><fmt:message key="Cancel"/></button>
            </div>
        </div>
	</div>

</fmt:bundle>

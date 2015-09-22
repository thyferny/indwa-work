<%--
  User: Will
  File:SystemUpdata
  Date: 12-12-7
 
  
--%>
<script type="text/javascript">
    <!--
    dojo.require("alpine.system.SystemUpdateHelper");
    dojo.require("alpine.system.SystemUpdateManager");
    //-->
</script>
<fmt:bundle basename="app">
    <div id="apline_system_update_dialog" dojoType="dijit.Dialog" title="<fmt:message key="system_updata_dialog_title" />" draggable="false">
        <div class="titleBar">
            <fmt:message key='system_updata_dialog_title'/>
        </div>
        <div dojoType="dijit.layout.LayoutContainer" style="width: 470px;height:250px">
           <div id="system_update_stack_container" dojoType="dijit.layout.StackContainer" region="center">
               <div dojoType="dijit.layout.ContentPane" id="available_update_list_container" style="overflow:hidden;">
                   <%--<div style="height:25px;padding-left: 10px;font-weight:bolder;"><fmt:message key='system_update_file_list'/></div>--%>
                   <div id="available_update_list_grid_container" style="margin:5px;">
                       <table width="430" height="200" border="0" cellspacing="5" cellpadding="0" align="center">
                           <tr>
                               <td style="font-weight: bold;" align="left" valign="middle" width="110px"><fmt:message key="system_update_grid_title_version" /></td>
                               <td align="left"><div id="system_update_grid_title_version"></div></td>
                           </tr>
                           <tr>
                               <td style="font-weight: bold;" align="left" valign="middle"><fmt:message key="system_update_grid_title_date" /></td>
                               <td align="left"><div id="system_update_grid_title_date"></div></td>
                           </tr>
                           <tr>
                               <td style="font-weight: bold;" align="left" valign="middle"><fmt:message key="system_update_grid_title_descrip" /></td>
                               <td align="left"><input id="system_update_grid_title_descrip" style="margin-bottom:3px;padding-top: 3px;padding-bottom: 3px;" dojoType="dijit.form.SimpleTextarea" rows=5/></td>
                           </tr>
                       </table>
                   </div>
               </div>
               <div dojoType="dijit.layout.ContentPane" id="current_running_flow_list_container" style="overflow:hidden;">
                   <div style="height:25px;padding-left: 10px;font-weight:bolder;"><fmt:message key='system_update_fail_tip'/></div>
                   <div id="current_running_flow_list_grid_container"  style="margin:5px;height:320px"></div>
               </div>
           </div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" style="height:50px;overflow:hidden;">
                <div id="hadoopPropery_Config_Colum_Dlg_Btn_Container" class="whiteDialogFooter">
                    <div baseClass="cancelButton" dojoType="dijit.form.Button" type="button" id="system_update_Dlg_Btn_Cancel"><fmt:message key="Cancel"/></div>
                    <div baseClass="primaryButton" dojoType="dijit.form.Button" type="button" id="system_update_Dlg_Btn_OK"><fmt:message key="update_button"/></div>
                </div>
            </div>
        </div>
    </div>

    <div id="apline_system_restart_dialog" dojoType="dijit.Dialog" title="<fmt:message key="system_updata_dialog_title" />" draggable="false">
        <div dojoType="dijit.layout.LayoutContainer" style="width: 160px;height:50px">
           <div dojoType="dijit.layout.ContentPane" region="center" style="height:100%;height:100%;overflow:hidden;">
                <div id="system_update_restart_tip_container">
                    <div style="margin-left:20px;margin-top: 15px;"><div id="system_update_restart_tip_container_tip"></div></div>
                </div>
            </div>
        </div>
    </div>
</fmt:bundle>

<script type="text/javascript">
    dojo.require("alpine.props.JoinTablePropertyHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false"
		id="tableJoinDialog" title='<fmt:message key="Table_Join_Title" />'>
    <div class="titleBar">
        <fmt:message key='Table_Join_Title'/>
    </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 900px;height:700px">
			<div dojoType="dijit.layout.ContentPane" style="width: 95%; " region="top">

			</div>
			<div id="tableJoinDialogCenter" dojoType="dijit.layout.ContentPane" style="width: 100%; height: 100%;" region="center">
                <div dojoType="dijit.layout.LayoutContainer" class="bottomborder">
                    <div dojoType="dijit.layout.ContentPane" style="width:45%;height:100%" id="table_join_tables_toggle" region="left"></div>
                    <div dojoType="dijit.layout.ContentPane" style="width:45%; height:100%; display:none" >
                        <fieldset style="margin: auto; border:1px solid #cecdcd; width: 95%; height: 95%; padding-left:2px;">
                            <legend style="margin-left:8px;"><fmt:message key='Table_Alias'/></legend>
                            <table width="100%">
                                <tr>
                                    <td width="70%">
                                        <table border="1" id="aliasTablesTable"
                                               dojoType="dojox.grid.DataGrid" query="{ tableName: '*' }"
                                               selectionMode= "single"
                                               clientSort="true" style="width: 240; height: 150px">
                                            <thead>
                                            <tr>
                                                <th width="70%" field="tableName"><fmt:message key="Join_Table" /></th>
                                                <th width="30%" field="tableAlias"><fmt:message key="Table_Alias" /></th>

                                            </tr>
                                            </thead>
                                        </table>
                                    </td>

                                </tr>
                            </table>
                        </fieldset>
                    </div>
                    <div dojoType="dijit.layout.ContentPane" style="width:55%; height:100%; border-left: 1px solid #cecdcd; padding-left: 3px;" region="center">
                        <div dojoType="dijit.layout.LayoutContainer" style="width: 100%; height: 100%;">
                            <div dojoType="dijit.layout.ContentPane" region="top">
                                <div  class="tablejointableheader"><fmt:message key="Table_Join_Selected_Columns" /></div>
                            </div>
                            <div dojoType="dijit.layout.ContentPane" region=center  id="table_join_selected_columns_holder"></div>
                        </div>
                    </div>
                </div>
			</div>
  			<div dojoType="dijit.layout.ContentPane" region="bottom">
                  <div class="tablejointableheader"><fmt:message key="Table_Join_Join_Conditions"/> </div>
                  <div class="tablejoinlabel" style="padding-left: 8px;"><fmt:message key="Left_Table" /> </div><span style="width: 90px" dojoType="dijit.form.Select" id="join_left_table_combo" baseclass="greyDropdownButton"> </span>

                  <table width="100%" height="105px;" style="padding: 2px 8px 0px 8px;">
                      <tr style="vertical-align: top;">
                          <td width="100%" >
                              <table id="joinRulesTable" dojoType="dojox.grid.DataGrid" autoHeight=3
                                     query="{ JoinType: '*' }"
                                     clientSort="true">
                                  <thead>
                                  <tr>
                                      <th width="13%" field="JoinType"><fmt:message
                                              key="Join_Type" /></th>
                                      <th width="15%" field="RightTable"><fmt:message
                                              key="RightTable" /></th>

                                      <th width="21%" field="JoinColumn1"><fmt:message
                                              key="Join_Column1" /></th>

                                      <th width="15%" field="JoinCondition"><fmt:message
                                              key="Condition" /></th>


                                      <th width="21%" field="JoinColumn2"><fmt:message
                                              key="Join_Column2" /></th>

                                      <th width="15%" field="AndOr"><fmt:message key="AndOr" />
                                      </th>

                                  </tr>
                                  </thead>
                              </table>
                          </td>
                          <td width=2px></td>
                          <td valign="top" width="30%">
                      </tr>
                  </table>
                  <div class="tablejointableheader" id="join_conditions_edit_title"><fmt:message key="Table_Join_Join_Create_Title"/></div>
                  <table width="100%"  class="bottomborder" style="padding-left: 8px; padding-right:8px" >
                      <tr>
                          <td><label class="tablejoinlabel"><fmt:message key="Join_Type" /></label></td>
                          <td><label class="tablejoinlabel"><fmt:message key="RightTable" /></label></td>
                          <td><label class="tablejoinlabel"><fmt:message key="Join_Column1" /></label></td>
                          <td><label class="tablejoinlabel"><fmt:message key="Condition" /></label></td>
                          <td><label class="tablejoinlabel"><fmt:message key="Join_Column2" /></label></td>
                          <td><label class="tablejoinlabel"><fmt:message key="AndOr" /></label></td>
                      </tr>
                      <tr>
                          <td><input style="align: left; width: 124px; height: 18px"
                                     dojoType="dijit.form.Select" id="edit_join_type" baseclass="greyDropdownButton"></td>
                          <td><input style="align: left; width: 124px; height: 18px"
                                     dojoType="dijit.form.Select" id="edit_join_right_table" baseclass="greyDropdownButton">
                          </td>
                          <td><input style="align: left; width: 124px; height: 18px" onChange="alpine.props.JoinTablePropertyHelper.update_join_column1();"
                                     dojoType="dijit.form.ComboBox" id="edit_join_column1" baseclass="greyDropdownButton"></td>
                          <td><input style="align: left; width: 124px; height: 18px"
                                     dojoType="dijit.form.Select" id="edit_join_condition" baseclass="greyDropdownButton"></td>
                          <td><input style="align: left; width: 124px; height: 18px" onChange="alpine.props.JoinTablePropertyHelper.update_join_column2();"
                                     dojoType="dijit.form.ComboBox" id="edit_join_column2" baseclass="greyDropdownButton"></td>
                          <td><input style="align: left; width: 124px; height: 18px"
                                     dojoType="dijit.form.Select" id="edit_join_andor" baseclass="greyDropdownButton"></td>
                      </tr>


                      <tr align="right">
                          <td colspan=6 style="padding-top: 5px;"><button dojoType="dijit.form.Button" type="button" baseClass="secondaryButton"
                                                                          onclick="alpine.props.JoinTablePropertyHelper.addJoinRuleBtn_Click()" id="addJoinRuleBtn"> <fmt:message
                                  key="create_button" /> </button>
                              <button dojoType="dijit.form.Button" type="button" baseClass="secondaryButton"
                                      disable="true" onclick="alpine.props.JoinTablePropertyHelper.updateJoinRuleBtn_Click()"
                                      id="updateJoinRuleBtn"> <fmt:message key="update_button" />
                              </button> <button disable="true" dojoType="dijit.form.Button" type="button" baseClass="secondaryButton"
                                                id="removeJoinRuleBtn" onclick="alpine.props.JoinTablePropertyHelper.removeJoinRuleBtn_Click()">
                                  <fmt:message key="delete_button" />
                              </button><button disable="true" dojoType="dijit.form.Button" type="button" baseClass="secondaryButton"
                                               id="clearJoinRuleBtn" onclick="alpine.props.JoinTablePropertyHelper.clearJoinRuleBtn_Click()">
                                  <fmt:message key="Table_Join_Clear_Button" /> </button> </td>
                      </tr>



                  </table>
				<div class="whiteDialogFooter">
                    <button type="Button" baseClass="cancelButton" dojoType="dijit.form.Button" onclick="alpine.props.JoinTablePropertyHelper.close_table_join_dialog()"><fmt:message key="Cancel"/></button>
                    <button type="Button" baseClass="primaryButton" dojoType="dijit.form.Button" id="tablejoin_dialog_ok_id" onclick="return alpine.props.JoinTablePropertyHelper.update_table_join_data()"><fmt:message key="OK"/></button>
 				</div>
			</div>
        </div>
	</div>

</fmt:bundle>

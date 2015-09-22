<%@ page language="java" contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<script type="text/javascript"
        src="../../js/alpine/props/variableQuantile.js" charset="utf-8"></script>

<fmt:bundle basename="app">
    <div dojoType="dijit.Dialog" draggable="false" id="variableQuantileConfigEditDialog"
         title="<fmt:message key='variable_quantile_edit_title'/>">
        <div class="titleBar">
            <fmt:message key='variable_quantile_edit_title'/>
        </div>

        <div dojoType="dijit.layout.BorderContainer" design="sidebar"
             style="width: 940px; height: 540px;"
             gutters="true" liveSplitters="true"
             id="variableQuantileConfigBorderContainer">

            <div dojoType="dijit.layout.ContentPane" splitter="true" style="width: 100%;height: 93%"
                 region="center">
                <div dojoType="dijit.layout.LayoutContainer">
                    <div dojoType="dijit.layout.ContentPane" id="var_quantile_gridpane"
                         layoutAlign="client" style="width: 100%; height: 180;"></div>

                    <div dojoType="dijit.layout.ContentPane"
                         layoutAlign="bottom" style="width: 100%;">
                        <table width="100%">
                            <tr>
                                <td>
                                    <div dojoType="dijit.form.Form" id="var_quantileForm"
                                         jsId="var_quantileForm" encType="multipart/form-data" action=""
                                         method="">

                                        <table cellspacing="10" width="400px">
                                            <tr>
                                                <td width="25%" align="right"><label class="valueLabel"
                                                                                     for="columnName">
                                                    <fmt:message key="var_quan_result_column"/> </label></td>
                                                <td>
                                                    <div id="var_quan_result_column"></div>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td width="25%" align="right"><label class="valueLabel"
                                                                                     for="quantileTypeLabel">
                                                    <fmt:message
                                                            key="var_quan_data_type"/> </label></td>

                                                <td><input type="radio"
                                                           dojoType="dijit.form.RadioButton" name="quantileTypeLabel"
                                                           onClick="force_update_bin_editor();"
                                                           checked="true" id="var_quan_data_type_customize"
                                                           value="Customize"/>
                                                    <label style="width: 100px" for="var_quan_data_type_customize">
                                                        Customize </label> <input type="radio"
                                                                                  dojoType="dijit.form.RadioButton"
                                                                                  name="quantileTypeLabel"
                                                                                  onClick="force_update_bin_editor();"
                                                                                  id="var_quan_data_type_aa"
                                                                                  value="Average Ascend"/> <label
                                                            style="width: 100px" for="var_quan_data_type_aa">
                                                        Average Ascend </label>
                                                </td>
                                            </tr>

                                            <tr>
                                                <td width="25%" align="right"><label class="valueLabel"
                                                                                     for="isCreateNewColumn">
                                                    <fmt:message
                                                            key="var_quan_create_new"/> </label></td>

                                                <td><input type="radio"
                                                           dojoType="dijit.form.RadioButton" name="isCreateNewColumn"
                                                           checked="true" id="var_quan_new_column_one" value="true"/>
                                                    <label style="width: 100px" for="var_quan_new_column_one">
                                                        true </label> <input type="radio"
                                                                             dojoType="dijit.form.RadioButton"
                                                                             name="isCreateNewColumn"
                                                                             id="var_quan_new_column_two"
                                                                             value="false"/> <label style="width: 100px"
                                                                                                    for="var_quan_new_column_two">
                                                        false </label>
                                                </td>
                                            </tr>

                                            <tr>
                                                <td width="25%" align="right"><label for="numberOfBin"
                                                                                     class="valueLabel">
                                                    <fmt:message key="var_quantile_bins"/> </label></td>
                                                <td><input id="var_quantile_bins" name="numberOfBin"
                                                           dojoType="dijit.form.NumberTextBox" required="true"
                                                           trim="true" constraints="{min: 1}"
                                                           onBlur="check_bin_editor()"/></td>
                                            </tr>
                                            <tr>
                                                <td width="25%"></td>
                                                <td align="left" colspan="2">
                                                    <button dojoType="dijit.form.Button" type="button"
                                                            baseClass="workflowButton"
                                                            id="var_quantile_create_button"
                                                            jsId="var_quantile_create_button"
                                                            onClick="create_var_quantile()">
                                                        <fmt:message key="create_button"/>
                                                    </button>
                                                    <button dojoType="dijit.form.Button" type="button"
                                                            baseClass="workflowButton"
                                                            id="var_quantile_update_button"
                                                            jsId="var_quantile_update_button"
                                                            onClick="update_var_quantile()">
                                                        <fmt:message key="update_button"/>
                                                    </button>
                                                    <button dojoType="dijit.form.Button" baseClass="workflowButton"
                                                            id="var_quantile_delete_button"
                                                            jsId="var_quantile_delete_button"
                                                            onClick="delete_var_quantile()" type="reset">
                                                        <fmt:message key="delete_button"/>
                                                    </button>
                                                </td>
                                            </tr>
                                        </table>
                                    </div>
                                </td>
                                <td>
                                    <div dojoType="dijit.layout.ContentPane"
                                         id="var_quantile_itembins_gridpane" layoutAlign="client"
                                         style="width: 500px; height: 180px;overflow-y:auto;">

                                        <div dojoType="dijit.form.Form" id="var_quantileBinForm"
                                             jsId="var_quantileBinForm" encType="multipart/form-data"
                                             action="" method="">
                                            <table cellspacing="10" width="450px"
                                                   id="var_quantile_bins_table"></table>
                                        </div>
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </div>
                </div>
            </div>
        </div>
        <div class="whiteDialogFooter" style="text-align: right;">
            <button dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
                    onClick="close_var_quantile_dialog();">
                <fmt:message key="Cancel"/>
            </button>
            <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
                    onClick="return update_var_quantile_data();">
                <fmt:message key="OK"/>
            </button>
        </div>
    </div>

    <div id="alpine_prop_variable_quantile_distinctValues_dialog" dojoType="alpine.layout.PopupDialog" draggable="false">
        <div dojoType="dijit.layout.ContentPane" style="width: 200px; height: 400px">
            <div id="alpine_prop_variable_quantile_distinctValues_grid" style="height: 100%"></div>
        </div>
        <div class="whiteDialogFooter">
            <button dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
                    onClick="cancelQuantileColumnValue()">
                <fmt:message key="Cancel"/>
            </button>
            <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
                    onClick="fillbackQuantileColumnValues()">
                <fmt:message key="OK"/>
            </button>
        </div>
    </div>
</fmt:bundle>
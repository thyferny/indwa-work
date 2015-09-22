
<script type="text/javascript" charset="utf-8">
  dojo.require("alpine.flow.MyFlowResultHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false"
		id="flowResultsDialog" title="<fmt:message key='My_Flow_Results' />">
        <div class="titleBar">
            <fmt:message key='My_Flow_Results' />
        </div>
        <div class="innerPadding">
            <a><fmt:message key='Flow_Name_Filter'/> </a><select name="model_myflowResult_name_filter" id="model_myflowResult_name_filter" baseClass="greyDropdownButton" style="width: 210px;height:5px;font-size: 16px;" dojoType="dijit.form.Select"></select>
            <a></a>
            <div dojoType="dijit.layout.LayoutContainer" style="width: 600px; height: 300px; padding-top:5px;">
                <div dojoType="dijit.layout.ContentPane" style="width: 100%; height: 90%" region="center">
                    <table id="flowResultsTable" dojoType="dojox.grid.DataGrid"
                           query="{ flowName: '*' }" clientSort="true" sortInfo="-4">
                        <thead>
                        <tr style="font-size: 14px;font-weight:bold;">
                            <th width="34%" field="flowFullName"><fmt:message key="Flow_Name" /></th>
                            <th width="10%" field="runType"><fmt:message key="Flow_Run_Type"/></th>
                            <th width="10%" field="version"><fmt:message key="Flow_Version"/></th>
                            <th width="20%" field="startTime"><fmt:message key="Flow_Start_Time" /></th>
                            <th width="20%" field="endTime"><fmt:message key="Flow_End_Time" /></th>
                        </tr>
                        </thead>

                    </table>
                </div>
            </div>
        </div>
        <div class="whiteDialogFooter">
            <button align="right" dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
                    onclick="dijit.byId('flowResultsDialog').hide();">
                <fmt:message key="Done" />
            </button>
            <button id="flow_result_delete_id"
                dojoType="dijit.form.Button" type="button" baseClass="secondaryButton"
                disabled = true >
                <fmt:message key="delete_button" />
            </button>
            <button id="flow_result_open_id"
                    dojoType="dijit.form.Button" type="button"   baseClass="primaryButton"
                    disabled = true >
                <fmt:message key="SHOW_RESULT" />
            </button>
        </div>
	</div>
</fmt:bundle>
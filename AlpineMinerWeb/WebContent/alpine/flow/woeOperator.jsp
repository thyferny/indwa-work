<script type="text/javascript" src="../../js/alpine/props/woeOperator.js"></script>

<fmt:bundle basename="app">
	<div id="editWOEOptionalVal" dojoType="dijit.Dialog" draggable="false" >
		<table width="200px">
			<tr>
				<td>
					<div id="optionalValSel" dojoType="dojox.form.CheckedMultiSelect" multiple="true"></div>
				</td>
			</tr>
			<tr>
				<td align="center">
					<button id="submitOptionalVal" type="button" baseClass="workflowButton" dojoType="dijit.form.Button"><fmt:message key="OK"/></button>
					<button id="cancelOptionalVal" type="button" baseClass="workflowButton" dojoType="dijit.form.Button"><fmt:message key="Cancel"/></button>
				</td>
			</tr>
		</table>
	</div>

	<div id="woeSettingWindow" dojoType="dijit.Dialog" title="<fmt:message key='woe_setting_title'/>" draggable="false" onhide="WOE_Setting._releaseResources()">
        <div class="titleBar">
            <fmt:message key='woe_setting_title'/>
        </div>
		<div dojoType="dijit.layout.BorderContainer" style="width: 600px;height: 400px">
			<div dojoType="dijit.layout.ContentPane" region="center" style="width: 70%; height: 40%">
				<div id="columnsGrid"></div>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="bottom" style="width: 100%; height: 60%">
				<div dojoType="dijit.layout.BorderContainer">
					<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%; height: 100%">
						<div dojoType="dijit.layout.StackContainer" id="editType" style="width: 100%; height: 100%">
							<div id="woeDefaultEditGrid" dojoType="dijit.layout.ContentPane">
								<%-- just display for no column. --%>
							</div>
							<div id="numericType" dojoType="dijit.layout.ContentPane">
								<div id="numericEditGrid" style="width: 100%; height: 100%"></div>
							</div>
							<div id="nominalType" dojoType="dijit.layout.ContentPane">
								<div id="nominalEditGrid" style="width: 100%; height: 100%"></div>
							</div>
						</div>
					</div>
					<div dojoType="dijit.layout.ContentPane" region="right" style="width: 73px; height: 100%">
						<table height="100%" width="100%">
							<tr>
								<td valign="top">
									<button type="button" baseClass="workflowButton" dojoType="dijit.form.Button" id="woe_grouping_add"><fmt:message key="woe_setting_button_add"/></button>
									<button type="button" baseClass="workflowButton" dojoType="dijit.form.Button" id="woe_grouping_delete"><fmt:message key="woe_setting_button_delete"/></button>
								</td>
							</tr>
							<tr>
								<td valign="bottom">
									<button type="button" baseClass="workflowButton" dojoType="dijit.form.Button" id="woe_grouping_autoCalculate" onclick="WOE_Setting.autoCalculate()"><fmt:message key="woe_setting_button_autoCalculate"/></button>
									<button type="button" baseClass="workflowButton" dojoType="dijit.form.Button" id="woe_grouping_calculate"><fmt:message key="woe_setting_button_calculate"/></button>
								</td>
							</tr>
						</table>
					</div>
				</div>
			</div>
		</div>
		<table width="100%" class="whiteDialogFooter">
			<tr>
				<td align="left">
					<button type="button" baseClass="secondaryButton" dojoType="dijit.form.Button" baseClass="dialogButton" onclick="WOE_Setting.autoGroup()" id="woe_autoGrouping"><fmt:message key="woe_setting_button_autoGrouping"/></button>
				</td>
				<td align="right">
					<button type="button" dojoType="dijit.form.Button" baseClass="cancelButton" id="woe_cancel_prop" onclick="WOE_Setting.closeWoeSetting()"><fmt:message key="Cancel"/></button>
					<button type="button" dojoType="dijit.form.Button" baseClass="primaryButton" id="woe_submit_prop" onclick="WOE_Setting.submitWoeSetting()"><fmt:message key="OK"/></button>
				</td>
			</tr>
		</table>
	</div>
</fmt:bundle>
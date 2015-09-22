<fmt:bundle basename="app">
	<body>
		<div dojoType="dijit.Dialog" draggable="false" id="export_flow_dlg" 
		title="<fmt:message key='Export_title' />"
			style="width: 500px">
			<table cellspacing="10px">
				<tr>
					<td align="Left"><h3>
							<fmt:message key="Export_title" />
						</h3>
					</td>
				</tr>
				<tr>
					<td width="480px"><div>
							<label witdh="100%" id="export_flow_label"> </label>
						</div>
					</td>
				</tr>
				<tr>
					<td align="right" >
						<button dojoType=dijit.form.Button type="Reset" name="reset"
							onClick="dijit.byId('export_flow_dlg').hide();">
							<fmt:message key="Done" />
						</button>
					</td>
				</tr>
			</table>
		</div>
        <div dojoType="dijit.Dialog" draggable="false" id="export_exec_flow_dlg"
             title="<fmt:message key='Export_exec_title' />"
             style="width: 300px">
            <div class="titleBar">
	            <fmt:message key='Export_exec_title'/>
	        </div>
            <div dojoType="dijit.layout.BorderContainer" style="width: 100%;height: 200px;">
                <div dojoType="dijit.layout.ContentPane" region="center" style="height: 100%;overflow:hidden;">
                    <div style="height:155px">
                        <label style="font-weight:bold;"><fmt:message key="Export_exec_tip" /></label>
                        <div style="margin: 10px 0px 10px 30px"><input type="radio" name="adpater" dojoType="dijit.form.RadioButton" id="export_exec_flow_adapter_window" value="Windows" checked="checked" ><label style="margin-left:2px;cursor:pointer;" for="export_exec_flow_adapter_window"><fmt:message key="export_exec_flow_adapter_window"/></label></div>
                        <div style="margin: 10px 0px 10px 30px"><input type="radio" name="adpater" dojoType="dijit.form.RadioButton" id="export_exec_flow_adapter_mac" value="Mac" ><label style="margin-left:2px;cursor:pointer;" for="export_exec_flow_adapter_mac"><fmt:message key="export_exec_flow_adapter_mac"/></label></div>
                        <div style="margin: 10px 0px 10px 30px"><input type="radio" name="adpater" dojoType="dijit.form.RadioButton" id="export_exec_flow_adapter_linux" value="Linux"><label style="margin-left:2px;cursor:pointer;" for="export_exec_flow_adapter_linux"><fmt:message key="export_exec_flow_adapter_linux"/></label></div>
                    </div>
                    <div  baseClass="whiteDialogFooter" style="text-align:right;">
                        <%--<input type="hidden" id="selected_flows_4_exec_explort" value=""> --%>
                        <button type="Button" dojoType="dijit.form.Button" baseClass="cancelButton" id="export_exec_flow_btn_cancel" onclick="hidden_export_exec_flow();"><fmt:message key="Cancel"/></button>
                        <button type="Button" dojoType="dijit.form.Button" baseClass="primaryButton" id="export_exec_flow_btn_ok"  onclick="do_export_exec_flow();"><fmt:message key="OK"/></button>
                   </div>
                </div>
             </div>
        </div>

</fmt:bundle>
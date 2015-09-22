
<script type="text/javascript">
    dojo.require("alpine.flow.FindandreplaceparameterHelper");
</script>
<fmt:bundle basename="app">
		<!-- find and replace parameter dialog -->
		<div dojoType="dijit.Dialog" id="findandreplaceparameterDlg"  draggable="false"	title="<fmt:message key="find_replace_parameter_value_dialog_title"/>" style="width:780px;">
            <div class="titleBar">
                <fmt:message key='find_replace_parameter_value_dialog_title'/>
            </div>
            <div dojoType="dijit.layout.ContentPane" baseClass="innerPadding">
			 <div style="position:relative;height:410px;margin: 0;padding: 0;">
			     <div style="float:left;width:520px;height:100px">
			        <div style="margin: 8px 0px"><label class="valueLabel" style="width:140px;text-align:left;float:left;"  for="findandreplaceparameter_Parameter_Name"><fmt:message key="Parameter_Name"/></label>
                        <select dojoType="dijit.form.Select" id="findandreplaceparameter_Parameter_Name" baseClass="greyDropdownButton" style="width:330px;" /></select>
                 </div>
			     <div style="margin: 10px 0px"><label class="valueLabel" style="width:140px;text-align:left;float:left;"  for="findandreplaceparameter_Find_Value"><fmt:message key="Find_Value"/></label><input type="text" dojoType="dijit.form.TextBox" id="findandreplaceparameter_Find_Value"  style="width:330px;" data-dojo-props="trim:true"></div>
			     <div style="margin: 10px 0px 0px;"><label class="valueLabel" style="width:140px;text-align:left;float:left;" for="findandreplaceparameter_Replace_with"><fmt:message key="Replace_with"/></label><input type="text" dojoType="dijit.form.TextBox" id="findandreplaceparameter_Replace_with"  style="width:330px;" data-dojo-props="trim:true"></div>
			     <div style="clear: both;"></div>
			     </div>
			     <div style="float:left;width:114px;height:100px;">
			       <fieldset style="border:1px solid #cecdcd;height: 100px;padding-left:2px;"><legend style="margin-left:8px;font-weight:bolder;"><fmt:message key="Scope"/></legend>
			         <div>
			            <div style="margin: 4px 0px"><input type="radio" name="scope" dojoType="dijit.form.RadioButton" id="findandreplaceparameter_Current_flow" value="current" ><label style="margin-left:2px;cursor:pointer;" for="findandreplaceparameter_Current_flow"><fmt:message key="Current_flow"/></label></div>
			            <div style="margin: 6px 0px"><input type="radio" name="scope" dojoType="dijit.form.RadioButton" id="findandreplaceparameter_All_flows" value="all" ><label style="margin-left:2px;cursor:pointer;" for="findandreplaceparameter_All_flows" ><fmt:message key="All_flows"/></label></div>
			            <div style="margin: 6px 0px;display: none;"><input type="radio" dojoType="dijit.form.RadioButton" id="findandreplaceparameter_Selected_flows" ><label style="margin-left:2px;cursor:pointer;" for="findandreplaceparameter_Selected_flows"><fmt:message key="Selected_flows"/></label></div>
			         </div>
			       </fieldset>
			     </div>
			     <div style="float:left;width:116px;height:100px;margin-left:10px;">
			       <fieldset style="border:1px solid #cecdcd;height: 100px;padding-left:2px;"><legend style="margin-left:8px;font-weight:bolder;"><fmt:message key="Options"/></legend>
			        <div>
			          <div style="margin: 4px 0px"><input type="checkbox" dojoType="dijit.form.CheckBox" id="findandreplaceparameter_IGNORE_CASE"><label style="margin-left:2px;" for="findandreplaceparameter_IGNORE_CASE"><fmt:message key="IGNORE_CASE"/></label></div>
			        </div>
			       </fieldset>
			     </div>
			     <div style="clear: both;"></div>
			     <div style="heigth:300px;margin-top: 20px;">
			       <fieldset style="border:1px solid #cecdcd;height:275px;width:758px;"><legend style="margin-left:8px;font-weight:bolder;"><fmt:message key="Search_Result"/></legend>
			         <div id="findandreplaceparameter_Search_Result_container" style="height:244px;width:98%;padding:8px 4px 4px 0px;margin:auto;overflow-x:hidden;overflow-y: auto;">
			         </div>
			       </fieldset>
			     </div>
			        <div style="clear: both;"></div>
			 </div>

			</div>
            <div dojoType="dijit.layout.ContentPane" baseClass="whiteDialogFooter">
                <div id="findandreplaceparameter_Search_Result_Tip" style="width:80px;float:left;color:#000000; "></div>
                <button baseClass="cancelButton" type="button" style="margin-left: 10px;" dojoType="dijit.form.Button" id="findandreplaceparameter_btn_cancel" ><fmt:message key="Done"/></button>
                <button baseClass="secondaryButton" type="button" style="margin-left: 10px;" dojoType="dijit.form.Button" id="findandreplaceparameter_btn_replace" ><fmt:message key="Replace"/></button>
                <button baseClass="primaryButton" type="button" style="margin-left: 10px;" dojoType="dijit.form.Button" id="findandreplaceparameter_btn_search"  ><fmt:message key="Search" /></button>
            </div>
		
		</div>
</fmt:bundle>
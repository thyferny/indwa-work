
<script type="text/javascript"
	src="../../js/alpine/flow/modelReplace.js" charset="utf-8"></script>
<fmt:bundle basename="app">
	<div dojoType="alpine.layout.PopupDialog" draggable="false"
		id="modelReplaceDialog" title="<fmt:message key='Model_Replace'/>">
		<div dojoType="dijit.layout.BorderContainer" style="width: 650px; height: 400px">
			<div dojoType="dijit.layout.ContentPane" region="center">
				<a> 
				<fmt:message key='Flow_Name_Filter'/>
				</a><select onChange = "filter_model_table(this.value)"  name="model_flowName_filter" id="model_flowName_filter" maxHeight="200" maxWidth="210" style="width: 210px;" dojoType="dijit.form.Select"></select>
				
				<table id="modelListTable" dojoType="dojox.grid.DataGrid" style="width: 600px; height: 300px"
					query="{ modelName: '*' }" 
					 
					clientSort="true">
					<thead>
						<tr>
							<th width="31%" field="flowName"><fmt:message key="Flow_Name"/></th>
							<th width="7%" field="version"><fmt:message key="Flow_Version"/></th>
							<th width="15%" field="modelName"><fmt:message key="Model_Name"/></th>
							<th width="20%" field="createTime"><fmt:message key="Create_Time"/></th>						 
							<th width="20%" field="algorithmName"><fmt:message key="Model_Type"/></th>
		
						</tr>
					</thead>
				</table>

				<div align="left">
					<label witdh="100%" id="download_model_label"></label>
				</div>
			</div>
            <div baseClass="whiteDialogFooter" dojoType="dijit.layout.ContentPane" region="bottom">
                <button align="right" id="model_view_id" baseClass="secondaryButton"
                        dojoType="dijit.form.Button" type="button"
                        onclick="return perform_view_model()">
                    <fmt:message key="View" />
                </button>
                <button align="right" id="model_download_id" baseClass="secondaryButton"
                        dojoType="dijit.form.Button" type="button"
                        onclick="return perform_download_model()">
                    <fmt:message key="Download" />
                </button>
                <button align="right" id="model_replace_ok_id" baseClass="secondaryButton"
                        dojoType="dijit.form.Button" type="button"
                        onclick="return perform_replace_model()">
                    <fmt:message key="Replace_Current" />
                </button>
                <button align="right" id="model_replace_delete_id" baseClass="secondaryButton"
                        dojoType="dijit.form.Button" type="button"
                        onclick="perform_delete_model()">
                    <fmt:message key="delete_button" />
                </button>
                <button align="right" dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
                        onclick="dijit.byId('modelReplaceDialog').hide();">
                    <fmt:message key="Done" />
                </button>
            </div>
		</div>
	</div>
</fmt:bundle>
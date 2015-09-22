<!--  this  has not used yet-->
<script type="text/javascript"
	src="../../js/alpine/visual/dataexplorer.js" charset="utf-8"></script>	
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false"
		id="scatterPlotDialog" closeNode="hidesctterplot"
		title="<fmt:message key='scatter_Plot_Parameters'/>">
		<div class="titleBar">
            <fmt:message key='scatter_Plot_Parameters'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 480px;height:170px">
			<div dojoType="dijit.layout.ContentPane" region="center" class="innerPadding" style="width: 100%;">
				<table>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Analysis_Column" />
							</label>
						</td>
						<td><select id="id_analysisColumn" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Reference_Column" />
							</label>
						</td>
						<td>
							<select id="id_referenceColumn" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select>
						</td>
					</tr>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Category_Column" />
							</label>
						</td>
						<td><select id="id_categoryColumn" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
				</table>
			</div>
            <div dojoType="dijit.layout.ContentPane" region="bottom" style="width: 100%;">
                <div class="whiteDialogFooter"> 
                	<span
                        dojoType="dijit.form.Button" id="hidesctterplot" type="button"    baseClass="cancelButton"
                        onclick="dijit.byId('scatterPlotDialog').hide();"><fmt:message
                        key="Cancel" /> </span>
					<span dojoType="dijit.form.Button" type="button"   baseClass="primaryButton"
                                id="scatterPlotDialog_OK"><fmt:message key="OK" /> </span>
                </div> <!-- end dialog footer -->
            </div> <!--end bottom -->
        </div>
    </div>
	<div dojoType="dijit.Dialog" draggable="false"
		id="scatPlotMartixDlg" closeNode="hidesctterplot"
		title="<fmt:message key='Scat_Plot_Martix'/>">
		<div class="titleBar">
            <fmt:message key='Scat_Plot_Martix'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 270px;height:330px" id="scatPlotMartixDlg_container">
			<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%;overflow: hidden;">
                <div style="margin:5px 3px;" class="textBoxFancyBorder"><div dojoType="alpine.layout.ClearableTextBox" id="dataexplorer_columns_filter4scattermatrix"  style="width:240px;margin-top:-8px;border:none;background:none;font-size:12px;" placeHolder="<fmt:message key='workbench_filterbox_placeholder'/>"></div></div>
			   <div id="scatPlotMartSelectContainer" style="width:100%;height:265px;overflow: hidden;"></div>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="bottom">
				<div class="whiteDialogFooter">
					<button dojoType="dijit.form.Button" id="scatPlotMartix_Cancel" type="button" baseClass="cancelButton"
									onclick="dijit.byId('scatPlotMartixDlg').hide();"><fmt:message
										key="Cancel" /> </button>
					<button dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
									id="scatPlotMartixDlg_OK" disabled="disabled" ><fmt:message key="OK" /></button>
				</div>
			</div>
		</div>
        <div dojoType="dojox.layout.ResizeHandle" targetId="scatPlotMartixDlg_container" constrainMax="true" maxWidth="500" maxHeight="500" minWidth="260" minHeight="330"></div>
	</div>


	<div dojoType="dijit.Dialog" draggable="false" id="boxwhiskerDialog"
		title="<fmt:message key='box_Whisker_Parameters'/>">
		<div class="titleBar">
            <fmt:message key='box_Whisker_Parameters'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 450px;height:150px">
			<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%; height: 85%">
                <div class="innerPadding">
				<table>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Analysis_Value" />
							</label>
						</td>
						<td><select id="id_analysisValue" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Analysis_Series" />
							</label>
						</td>
						<td><select id="id_analysisSeries" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Analysis_type" />
							</label>
						</td>
						<td><select id="id_analysisType" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
				</table>
                </div>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="bottom" style="width: 100%;">
				<div class="whiteDialogFooter">
					 <span dojoType="dijit.form.Button" id="hideboxwhiskerplot" type="button" baseClass="cancelButton"
									onclick="dijit.byId('boxwhiskerDialog').hide();"><fmt:message
											key="Cancel" /> </span>
					<span dojoType="dijit.form.Button" type="button"  baseClass="primaryButton"
									id="boxwhiskerPlotDialog_OK"><fmt:message key="OK" /> </span>
				</div>
			</div>
		</div>
	</div>
 

	<div dojoType="dijit.Dialog" draggable="false" id="barChartDialog"
		title="<fmt:message key='bar_Chart_Parameters'/>">
		<div class="titleBar">
            <fmt:message key='bar_Chart_Parameters'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 480px;height:160px">
			<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%;">
                <div class="innerPadding">
				<table>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Value_Domain" />
							</label>
						</td>
						<td><select id="id_valueDomian" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Category_Type" />
							</label>
						</td>
						<td><select id="id_CategoryType" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
                    <tr>
                        <td nowrap>
                            <label class="valueLabel">
                                <fmt:message key="Scope_Domain" />
                            </label>
                        </td>
                        <td><select id="id_scopeDomain" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
                    </tr>
				</table>
                </div>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="bottom" style="width: 100%;">
				<div class="whiteDialogFooter">
					<span dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
						onclick="dijit.byId('barChartDialog').hide();"><fmt:message
								key="Cancel" /> </span>
					<span dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
						id="barChartDialog_OK"><fmt:message key="OK" /> </span>
				</div>
			</div>
		</div>
	</div>


	<div dojoType="dijit.Dialog" draggable="false" id="timeSeriesDialog"
		title="<fmt:message key='time_Series_Parameters'/>">
		<div class="titleBar">
            <fmt:message key='time_Series_Parameters'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 400px;height:160px">
			<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%;">
                <div class="innerPadding">
				<table>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Sequence_Column" />
							</label>
						</td>
						<td><select id="id_IDColumn" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="Value_Column" />
							</label>
						</td>
						<td><select id="id_valueColumn" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
					
					<tr>
						<td nowrap>
							<label class="valueLabel">
								<fmt:message key="GroupBy_Column" />
							</label>
						</td>
						<td><select id="id_groupByColumn" dojoType="dijit.form.Select" maxHeight="200" style="width: 240px"></select></td>
					</tr>
				</table>
                </div>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="bottom" style="width: 100%;">
				<div class="whiteDialogFooter">
					<span dojoType="dijit.form.Button" type="button" baseClass="cancelButton"
						onclick="dijit.byId('timeSeriesDialog').hide();"><fmt:message
								key="Cancel" /> </span>
					<span dojoType="dijit.form.Button" type="button" baseClass="primaryButton"
						id="timeSeriesDialog_OK"><fmt:message key="OK" /> </span>
				</div>
			</div>
		</div>
	</div>


	<div dojoType="dijit.Dialog" draggable="false" id="univariatePlotDialog" title="<fmt:message key='select_columns'/>">
		<div class="titleBar">
            <fmt:message key='select_columns'/>
        </div>
		
		<div dojoType="dijit.layout.LayoutContainer"
	         style="width: 400px; height: 500px;margin-left: 8px; margin-right:8px;" id="dataExplorerColumnsDialog_container">
	        <div dojoType="dijit.layout.ContentPane" region="top" style="padding:5px 0 5px 0;">
                <label style="font-weight:bolder"><fmt:message key='Reference_Column'/></label>
                <div style="padding:3px 0 10px 0;">
                    <select dojoType="dijit.form.Select" maxHeight="200" style="font-size:14px;height:20px;" baseClass="greyDropdownButton" id="de_refrecen_column_select_id"></select>
                </div>
       			<span style="float: left">
	                <div class="textBoxFancyBorder" style="margin-top:0;"><div dojoType="alpine.layout.ClearableTextBox" id="dataexplorer_columns_filter_for_univariate" style="width:240px;margin-top:-8px;border:none;background:none;font-size:12px;" placeHolder="<fmt:message key='workbench_filterbox_placeholder'/>"></div></div>
	            </span>
	            <span style="float:right;width:80px;padding-top:4px;">
		            <button id="univariate_column_selectall" dojoType=dijit.form.Button baseClass="linkButton" name="All">
	                    <fmt:message key="Table_Join_All_Button" />
	                </button><span class="tablejoinsubfield" style="padding-right:0; color:#949599;">|</span><button id="univariate_column_selectnone" dojoType=dijit.form.Button baseClass="linkButton" name="None">
	                    <fmt:message key="Table_Join_None_Button" />
	                </button>
	            </span>
	        </div>
	        <div dojoType="dijit.layout.ContentPane" region="center" >
	            <div id="univariateColumnDialogTableHolder" style="height:100%;"></div>
	        </div>
	        <div dojoType="dijit.layout.ContentPane" region="bottom">
	            <div class="whiteDialogFooter">
	                <button id="btn_cancel_4_univariate_columnName_select" dojoType=dijit.form.Button type="Reset" baseClass="cancelButton" name="Reset">
	                    <fmt:message key="Cancel" />
	                </button>
	                <button id="btn_ok_4_univariate_columnName_select" dojoType=dijit.form.Button type="button" baseClass="primaryButton" name="submit">
	                    <fmt:message key="OK" />
	                </button>
	            </div>
	        </div>
	    </div>
        <div dojoType="dojox.layout.ResizeHandle" resizeAxis="x" targetId="dataExplorerColumnsDialog_container" constrainMax="true" maxWidth="500" maxHeight="500" minWidth="265" minHeight="280"></div>
	</div>
</fmt:bundle>
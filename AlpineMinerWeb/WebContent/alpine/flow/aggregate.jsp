<style type="text/css">
#aggregateColumnsConfigBorderContainer {
	width: 100%;
	height: 90%;
}
#aggregateWindowsConfigBorderContainer {
	width: 100%;
	height: 90%;
}
#aggregateGroupBysConfigBorderContainer {
	width: 100%;
	height: 88%;
}
</style>
<script type="text/javascript" src="../../js/alpine/props/agg_columns.js" charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/props/agg_windows.js" charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/props/agg_groupby.js" charset="utf-8"></script>
<script type="text/javascript" src="../../js/alpine/props/aggregateUtil.js" charset="utf-8"></script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="aggregateWindowsConfigEditDialog"
		title="<fmt:message key='agg_window_edit_title'/>">
	    <div class="titleBar">
            <fmt:message key='agg_window_edit_title'/>
	    </div>
		<div dojoType="dijit.layout.BorderContainer" design="sidebar"
				style="width: 900px; height: 480px;"
				gutters="true" liveSplitters="true"
				id="aggregateWindowsConfigBorderContainer">
	
			<div dojoType="dijit.layout.ContentPane" splitter="true"
				region="center">
				<div dojoType="dijit.layout.LayoutContainer">
				
					<div dojoType="dijit.layout.ContentPane" id="agg_window_gridpane"
						layoutAlign="client" style="width: 100%; height: 150;">
					</div>
		
					<div dojoType="dijit.layout.ContentPane" layoutAlign="bottom" style="width: 100%;">
						<div dojoType="dijit.form.Form" id="agg_windowForm"
							jsId="agg_windowForm" encType="multipart/form-data" action=""
							method="">
			
							<table cellspacing="10" width="100%">
								<tr>
									<td width="25%" align="right"><label for="resultColumn">
											<b><fmt:message key="agg_win_result_column" /> </b></label>
									</td>
									<td><input type="text" id="agg_win_result_column"
										name="resultColumn" required="true" trim="true"
										dojoType="dijit.form.ValidationTextBox" regExp="^[\w]+"
										/>
									</td>
								</tr>
								<tr>
									<td width="25%" align="right"><label><b>
											<fmt:message key="agg_win_data_type" /></b> </label>
									</td>
									<td><select dojoType="dijit.form.FilteringSelect"
										id="agg_win_data_type" name="dataType"
                                            baseClass="greyDropdownButton" style="width: auto;">
											<option value="BIGINT" selected>BIGINT</option>
											<option value="BOOLEAN">BOOLEAN</option>
											<option value="BIT">BIT</option>
											<option value="BIT VARYING">BIT VARYING</option>
											<option value="CHAR">CHAR</option>
											<option value="DATE">DATE</option>
											<option value="DOUBLE PRECISION">DOUBLE PRECISION</option>
											<option value="NUMERIC">NUMERIC</option>
											<option value="INTEGER">INTEGER</option>
											<option value="VARCHAR">VARCHAR</option>
											
									</select></td>
								</tr>
								<tr>
									<td width="25%" align="right"><b><label for="windowFunction">
											<fmt:message key="agg_win_function" /> </b></label>
									</td>
									<td><input type="text" id="agg_win_function"
										name="windowFunction" required="true" trim="true"
										dojoType="dijit.form.ValidationTextBox" />
									</td>
								</tr>
								<tr>
									<td width="25%" align="right"><b><label
										for="windowSpecification"> <fmt:message
												key="agg_win_spec" /></b> </label>
									</td>
									<td><textarea id="agg_win_spec" name="windowSpecification"
											dojoType="dijit.form.SimpleTextarea" rows="3" cols="30"
											style="width: auto;"></textarea>
									</td>
								</tr>
								<tr>
									<td width="25%"></td>
									<td align="left" colspan="2">
										<button dojoType="dijit.form.Button" type="button"
											id="agg_window_create_button" jsId="agg_window_create_button" baseClass="workflowButton"
											onClick="create_agg_window()">
											<fmt:message key="create_button" />
										</button>
										<button dojoType="dijit.form.Button" type="button"
											id="agg_window_update_button" jsId="agg_window_update_button"   baseClass="workflowButton"
											onClick="update_agg_window()">
											<fmt:message key="update_button" />
										</button>
                                        <button dojoType="dijit.form.Button"
                                                id="agg_window_delete_button" jsId="agg_window_delete_button"     baseClass="workflowButton"
                                                onClick="delete_agg_window()" type="reset">
                                            <fmt:message key="delete_button" />
                                        </button>
										<button dojoType="dijit.form.Button"
											id="agg_window_clear_button" jsId="agg_window_delete_button"     baseClass="workflowButton"
											onClick="clear_agg_window_from()" type="reset">
											<fmt:message key="clear_button" />
										</button></td>
								</tr>
							</table>
						</div>
					</div>
				</div>
			</div>
	
			<div dojoType="dijit.layout.ContentPane" splitter="true"
				region="right" style="width: 160px;" id="agg_window_list">add
				stuff</div>
	

		</div>
        <div dojoType="dijit.layout.ContentPane" region="bottom"
             style="width: 100%;">
            <div class="whiteDialogFooter" style="text-align: right;">
                <button dojoType="dijit.form.Button" type="button"   baseClass="cancelButton"
                        onClick="close_agg_window_dialog();">
                    <fmt:message key="Cancel" />
                </button>
                <button dojoType="dijit.form.Button" type="button"  baseClass="primaryButton"
                        onClick="return update_agg_window_data();">
                    <fmt:message key="OK" />
                </button>
            </div> <!--end DialogFooter-->
        </div>   <!-- end bottom -->
	</div>
		<div dojoType="dijit.Dialog" draggable="false" id="aggregateColumnsConfigEditDialog"
			title="<fmt:message key='agg_column_edit_title'/>">
            <div class="titleBar">
                <fmt:message key='agg_column_edit_title'/>
            </div>

			<div dojoType="dijit.layout.BorderContainer" design="sidebar"
					style="width: 900px; height: 420px;"
				gutters="true" liveSplitters="true"
				id="aggregateColumnsConfigBorderContainer">

				<div dojoType="dijit.layout.ContentPane" splitter="true"
					region="center">
					<div dojoType="dijit.layout.LayoutContainer">
			
					<div dojoType="dijit.layout.ContentPane" id="agg_column_gridpane"
						layoutAlign="client" style="width: 100%; height: 150;"></div>

					<div dojoType="dijit.layout.ContentPane" 
						layoutAlign="bottom" style="width: 100%;">
					<div dojoType="dijit.form.Form" id="agg_columnForm"
						jsId="agg_columnForm" encType="multipart/form-data" action=""
						method="">

						<table cellspacing="10" width="100%">
							<tr>
								<td width="25%" align="right"><label for="alias"><b> <fmt:message
											key="agg_column_alias" /> </b></label>
								</td>
								<td><input type="text" id="agg_column_alias" name="alias"
									required="true" trim="true" regExp="^[\w]+"
									dojoType="dijit.form.ValidationTextBox"
									/>
									<input type="hidden" id="agg_column_dataType">
								</td>
							</tr>
							<tr>
								<td width="25%" align="right"><label
									for="aggregateExpression"> <b><fmt:message
											key="agg_column_expression" /> </b></label>
								</td>
								<td><textarea id="agg_column_expression"
										name="aggregateExpression"
										dojoType="dijit.form.SimpleTextarea" rows="3" cols="30" style="width: 220px"
										style="width: auto;"></textarea>
								</td>
							</tr>
							<tr>
								<td width="25%"></td>
								<td align="left" colspan="2">
									<button dojoType="dijit.form.Button" type="button"   baseClass="workflowButton"
										id="agg_column_create_button" jsId="agg_column_create_button"
										onClick="create_agg_column()">
										<fmt:message key="create_button" />
									</button>
									<button dojoType="dijit.form.Button" type="button"      baseClass="workflowButton"
										id="agg_column_update_button" jsId="agg_column_update_button"
										onClick="update_agg_column()">
										<fmt:message key="update_button" />
									</button>
                                    <button dojoType="dijit.form.Button"
                                            id="agg_column_delete_button" jsId="agg_column_delete_button"    baseClass="workflowButton"
                                            onClick="delete_agg_column()" type="reset">
                                        <fmt:message key="delete_button" />
                                    </button>
									<button dojoType="dijit.form.Button"
										id="agg_column_reset_button" jsId="agg_column_delete_button"    baseClass="workflowButton"
										onClick="resetAggColumnForm()" type="reset">
										<fmt:message key="clear_button" />
									</button></td>
							</tr>
						</table>

					</div>
					</div>
				</div>
				</div>

				<div dojoType="dijit.layout.ContentPane" splitter="true"
					region="right" style="width: 120px;">
					<table witdh="100%" , cellspacing="10">
						<tr>
							<td><label><b><fmt:message key="aggregator_name_select" /> </b>
							</label></td>
						</tr>
						<tr>
							<td><input type="radio" name="aggregator_name"
								id="aggregator_name_sum" checked="true" value="sum"
								onClick="set_aggregator_name('sum')" /> <label
								for="aggregator_name_sum">sum</label>
							</td>
						</tr>
						<tr>
							<td><input type="radio" name="aggregator_name"
								id="aggregator_name_count" value="count"
								onClick="set_aggregator_name('count')" /> <label
								for="aggregator_name_count">count</label>
							</td>
						</tr>
						<tr>
							<td><input type="radio" name="aggregator_name"
								id="aggregator_name_min" value="min"
								onClick="set_aggregator_name('min')" /> <label
								for="aggregator_name_min">min</label>
							</td>
						</tr>
						<tr>
							<td><input type="radio" name="aggregator_name"
								id="aggregator_name_max" value="max"
								onClick="set_aggregator_name('max')" /> <label
								for="aggregator_name_max">max</label>
							</td>
						</tr>
						<tr>
							<td><input type="radio" name="aggregator_name"
								id="aggregator_name_avg" value="avg"
								onClick="set_aggregator_name('avg')" /> <label
								for="aggregator_name_avg">avg</label>
							</td>
						</tr>
						<tr>
							<td><input type="radio" name="aggregator_name"
								id="aggregator_name_variance" value="variance"
								onClick="set_aggregator_name('variance')" /> <label
								for="aggregator_name_variance">variance</label>
							</td>
						</tr>
						<tr>
							<td><input type="radio" name="aggregator_name"
								id="aggregator_name_stddev" value="stddev"
								onClick="set_aggregator_name('stddev')" /> <label
								for="aggregator_name_stddev">stddev</label>
							</td>
						</tr>
					</table>
				</div>

				<div dojoType="dijit.layout.ContentPane" splitter="true"
					region="right" style="width: 160px;" id="agg_column_list">add
					stuff</div>

			</div>
            <div dojoType="dijit.layout.ContentPane" region="bottom"
                 style="width: 100%;">
                <div class="whiteDialogFooter" style="text-align:right;">
                    <button dojoType="dijit.form.Button" type="button"     baseClass="cancelButton"
                            onClick=" close_agg_Columns_dialog();">
                        <fmt:message key="Cancel" />
                    </button>
                    <button dojoType="dijit.form.Button" type="button"  baseClass="primaryButton"
                            onClick="return update_agg_column_data();">
                        <fmt:message key="OK" />
                    </button>
                </div><!-- end dialogFooter -->
            </div><!-- end Bottom -->
        </div>
		
		<div dojoType="dijit.Dialog" draggable="false" id="aggregateGroupBysConfigEditDialog"
			title="<fmt:message key='agg_groupby_edit_title'/>">
            <div class="titleBar">
                <fmt:message key='agg_groupby_edit_title'/>
            </div>
			<div dojoType="dijit.layout.BorderContainer" design="sidebar"
				gutters="true" liveSplitters="true"
				style="width: 600px; height: 350px;"
				id="aggregateGroupBysConfigBorderContainer">

				<div dojoType="dijit.layout.ContentPane" splitter="true"
					region="center">

					<div dojoType="dijit.layout.LayoutContainer"
						style="width: 100%; height: 100%">

						<div dojoType="dijit.layout.ContentPane" id="agg_groupby_gridpane"
							layoutAlign="client" style="width: 80%;height: 100%;"></div>

						<div dojoType="dijit.layout.ContentPane"
							id="agg_groupby_buttonpane" layoutAlign="right"
							style="width: 20%;">
							<div dojoType="dijit.form.Form" id="agg_groupbyForm"
								jsId="agg_groupbyForm" encType="multipart/form-data" action=""
								method="">

								<table cellspacing="20" width="100%">
									<tr>
										<td align="center">
										<a onClick="move_up_agg_groupby()" >
											
											<img src="<%=path%>/images/up_arrow.png"
													width="24" height="24" border="1"  /> 
											</a>											
										</td>
									</tr>
									<tr>
										<td align="center">
											<a onClick="move_down_agg_groupby()" >
											<img src="<%=path%>/images/down_arrow.png"
													width="24" height="24" border="1"  /> 
											</a>											
										</td>
									</tr>
									<tr>
										<td align="center">
											<a onClick="delete_agg_groupby()" >
												<img src="<%=path%>/images/delete-icon.png"
													width="24" height="24"   /> 
											</a>
										</td>
									</tr>
								</table>
							</div>
						</div>
					</div>
				</div>

				<div dojoType="dijit.layout.ContentPane" splitter="true"
					region="right" style="width: 250px;" id="agg_groupby_list">add
					stuff</div>

			</div>
            <div dojoType="dijit.layout.ContentPane" region="bottom"
                 style="width: 100%;">
                <div class="whiteDialogFooter" style="text-align: right;">
                    <button dojoType="dijit.form.Button" type="button"  baseClass="cancelButton"
                            onClick="close_agg_groupby_dialog();">
                        <fmt:message key="Cancel" />
                    </button>
                    <button dojoType="dijit.form.Button" type="button"   baseClass="primaryButton"
                            onClick="return update_agg_groupby_data();">
                        <fmt:message key="OK" />
                    </button>
                </div> <!-- end dialogFooter -->
            </div>  <!-- end bottom -->
        </div>

	</body>
</fmt:bundle>
</html>
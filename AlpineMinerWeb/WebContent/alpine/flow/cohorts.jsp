
<script type="text/javascript"
	src="../../js/alpine/props/cohort.js" charset="utf-8">
</script>

<style type="text/css">

#cohorstsContainer {
	width: 100%;
	height: 88%;
}	
</style>

<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" height=500 id="cohortDialog"
		title="<fmt:message key='cohorst_edit_title'/>">

		<div dojoType="dijit.layout.LayoutContainer" design="sidebar"
			style="width: 600px; height: 350px;"
			gutters="true" liveSplitters="true"
			id="cohorstsContainer">
			
			<div dojoType="dijit.layout.ContentPane" region="center"
				style="width: 100%;">
				<div dojoType="dijit.layout.LayoutContainer"
						style="width: 100%; height: 100%">
						<div dojoType="dijit.layout.ContentPane" id="cohorts_grid_pane"
							layoutAlign="left" style="width: 50%;"></div>
							
						<div dojoType="dijit.layout.ContentPane"
							layoutAlign="center"
							style="width: 50%;">
							<div dojoType="dijit.form.Form" 
								encType="multipart/form-data" action="" method="">
								<table cellspacing="10">
								<tr>
								<td width="20px"></td>
								<td><span><fmt:message key="cohorts_max" /></span></td>
								<td><input id="new_max_cohort" 
										dojoType="dijit.form.NumberTextBox" 
										style="width: 60px"
										trim="true" /></td>
								<td><button dojoType="dijit.form.Button" type="button"
									onClick="return add_cohort();">
									<fmt:message key="Add" /></td>
								<td><button dojoType="dijit.form.Button" type="button"
									onClick="return remove_selected_cohorts();">
									<fmt:message key="Remove" /></td>
								</tr>
								</table>
								
							</div>
						</div>
				</div>
				
			</div>

			<div dojoType="dijit.layout.ContentPane" region="bottom"
				style="width: 100%;">
                <div class="dialogFooter">
							<button dojoType="dijit.form.Button" type="button"  baseClass="dialogButton"
								onClick="return update_cohorst_data();">
								<fmt:message key="OK" />
							</button>

							<button dojoType="dijit.form.Button" type="button"   baseClass="dialogButton"
								onClick="dijit.byId('cohortDialog').hide();">
								<fmt:message key="Cancel" />
							</button>
                </div> <!-- dialog footer -->
			</div>  <!-- end bottom -->
		</div>
	</div>
</fmt:bundle>
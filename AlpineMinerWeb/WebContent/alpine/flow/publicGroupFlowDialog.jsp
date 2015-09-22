<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>

<script type="text/javascript" charset="utf-8">
   dojo.require("alpine.flow.AddToPersonalFlowListHelper");
</script>
<fmt:bundle basename="app">
		<div dojoType="dijit.Dialog" id="PublicGroupFlowDialog" draggable="false"
			title="<fmt:message key='Add_flow_title' />"
			style="background: #ffffff;width:800px">
			<div class="titleBar">
	            <fmt:message key='Add_flow_title'/>
	        </div>
	        <br/>
			<div dojoType="dijit.layout.BorderContainer" style="width: 800px; height: 500px; "
				liveSplitters="true"  id="PublicGroupFlowContainer">
				
				<div dojoType="dijit.layout.ContentPane" region="left"
					splitter="true" style="width: 30%; height: 100%;">
					<div dojoType="dijit.layout.LayoutContainer"
						id="GroupContainer">
						<div dojoType="dijit.layout.ContentPane" layoutAlign="client"
							id="Group"></div>
					</div>
					
				</div> <!--end left -->
			 <div dojoType="dijit.layout.ContentPane" region="center" style="overflow:auto;" id="FlowDisplayPanelGroupHolder">
					<!-- <div dojoType="dijit.layout.LayoutContainer" style="width: 80%; height: 100%;"> -->
						<div id="FlowDisplayPanelGroup"></div>
					<!-- </div>  -->
				</div>  <!--end center-->
				<div dojoType="dijit.layout.ContentPane" region="bottom" style="width: 100%;padding:0px;">

							<table style="padding:10px;">
							<tr>
							<td><b><fmt:message key="Flow_Version" /></b></td><td><font id="add_flow_version"  >  </font></td><td width = "5px"></td>
							<td ><b><fmt:message key='COMMENTS' /> </b></td>


								</tr>
								<tr><td><b><fmt:message key="Publisher" /></b></td><td><font id="add_flow_publisher"  >  </font></td>
                                    <td width = "5px"></td><td rowspan =2>


							 <textarea id="add_flow_comments"
												name="add_flow_comments" rows = 2 cols=35
												style="width: auto; resize:true">	</textarea>

								</td></tr>
								<tr><td><b><fmt:message key="Publish_Time" /></b></td>
								<td width = "180px"><font id="add_flow_publish_time"  >  </font>
								</td> <td width = "5px"></td> </tr>
								</table>
				</div>
			</div>
             <div class="whiteDialogFooter">
                 <div id="cancel_button_4public_group_flow" dojoType="dijit.form.Button" baseClass="cancelButton"  type="button">
                     <fmt:message key='Cancel'/></div>
                 <div id="add_open_button_4public_group_flow" dojoType="dijit.form.Button" baseClass="secondaryButton" type="button">
                     <fmt:message key="AddOpen" /></div>
                 <div id="add_button_4public_group_flow" dojoType="dijit.form.Button" baseClass="primaryButton" type="button">
                     <fmt:message key="Add" /></div>
             </div>   <!-- end footer-->
		</div>
</fmt:bundle>

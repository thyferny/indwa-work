<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<fmt:bundle basename="app">
	<script type="text/javascript">
         dojo.require("alpine.flow.ShareFlowHelper");
	</script>
		<div dojoType="dijit.Dialog" draggable="false" id="share_flow_dlg" 
			title="<fmt:message key='Share_title' />">
            <div class="titleBar">
                <fmt:message key='Share_title'/>
            </div>
			<div dojoType="dijit.layout.LayoutContainer" design="sidebar"
				style="width: 300px; height: 175px;" gutters="true"
				liveSplitters="true">
				<div dojoType="dijit.layout.ContentPane" region="top"
					style="padding-left:10px;padding-right:10px;padding-top:5px">
					<form id="share_flow_form">
							<label for="share_type"> <fmt:message key="share_with_type" /> </label>
                            <div dojoType="dijit.form.Select" id="share_flow_radio_button_table" baseClass="greyDropdownButton" maxHeight="300" style="width:auto;"></div>
					</form>
				</div>
				<div dojoType="dijit.layout.ContentPane" region="center" style="padding-left:10px;">
                     <fmt:message key="COMMENTS"/> <br/>
					 
							<textarea id="share_flow_comments" 
								name="share_flow_comments" rows = 3 cols = 40
								style="width: 95%; resize:false">	</textarea>
				</div> <!-- end center div -->
                <div dojoType="dijit.layout.ContentPane" region="bottom">
                    <div class="whiteDialogFooter">
                        <button dojoType="dijit.form.Button" type="button"   baseClass="cancelButton"
                                onClick="dijit.byId('share_flow_dlg').hide();">
                            <fmt:message key="Cancel" />
                        </button>
                        <button id="share_flow_OK_btn" dojoType="dijit.form.Button" type="button" baseClass="primaryButton">
                            <fmt:message key="OK" />
                        </button>
                    </div>
                </div>  <!--end bottom div -->
            </div> <!-- end sidebar div-->
		</div> <!--end Dialog div-->
</fmt:bundle>
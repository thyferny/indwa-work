<script type="text/javascript" charset="utf-8">
dojo.require("alpine.flow.ImportFlowHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="import_flow_dlg"
		title="<fmt:message key='Import_title' />">
		<div class="titleBar">
            <fmt:message key='Import_title'/>
        </div>
		<div id="import_flow_container"  dojoType="dijit.layout.LayoutContainer" style="width: 480px ; height:370px">

			<div id="import_flow_files_content" dojoType="dijit.layout.ContentPane" style="width: 100%;height:210px;overflow:hidden;" region="center">
			     <div id="fileImportBtnContainer" style="position: relative;width: 380px;padding-top:5px;padding-left:10px;padding-bottom:5px;">
			          <button dojoType="dijit.form.Button" id="button_implort_flow_add_file" type="button" baseClass="workflowButton" title="<fmt:message key="btn_add_file_tip" />"><fmt:message key="btn_add_file" /></button>
				      <label for="button_implort_flow_add_file"><fmt:message key='import_file_maxfile_num'/></label>
				 </div>
				 
				 <div id="flow_import_grid_container" style="height: 160px;width:475px;margin-top:5px;margin: 0px auto;"></div>
				 <div style="padding-top:5px;padding-bottom:5px;padding-left:10px">
				   <button dojoType="dijit.form.Button" id ="remove_last_import_flow"  align = "right" type="button" baseClass="workflowButton">
					 <fmt:message key="Remove_Selected" />
				   </button>
				</div>
				<form name="frmIO_flow" id="frmIO_flow" action="#" enctype="multipart/form-data"
					method="POST">
	 				
				<!-- <table width="100%" height="100%"> -->
				 <div id="div_import_flow_files" style="height:1px;overflow:hidden;position:relative;">
	 			<!-- content will be added dynamically -->
			      </div>
			     <!-- </table>  -->
 
				</form>
                <div id="import_flow_comments_content"style="width: 100%; padding-left:10px;padding-top:5px;overflow: hidden;" >
                                <div><fmt:message key='COMMENTS' /> </div>
                                <textarea id="import_flow_comments"
                                          dojoType="dijit.form.SimpleTextarea"
                                          name="import_flow_comments"
                                          style="resize:false;width: 470px;height:80px;">
                                </textarea>
                </div>    <!-- end import_flow_comments_content-->
            </div>
        </div>
		
			<!-- background-color : #e3e3e3 -->

                <div class="whiteDialogFooter">
	                <button id="btn_close_import_flow_dlg" dojoType="dijit.form.Button" type="button" baseClass="cancelButton">
	                    <fmt:message key="Cancel" />
	                </button>
	                <button id="btn_doFlowUploadWithOpen" dojoType="dijit.form.Button" type="button" baseClass="secondaryButton">
	                    <fmt:message key="import_open_flow_tip" />
	                </button>
	                <button id="btn_doFlowUpload" dojoType="dijit.form.Button" type="button" baseClass="primaryButton">
	                    <fmt:message key="import_flow_tip" />
	                </button>
	            </div> <!--end dialogFooter-->
	</div>
 
</fmt:bundle>

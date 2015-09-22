<script type="text/javascript" src="../../js/alpine/system/licenseUpdate.js"></script>
<fmt:bundle basename="app">
	<div dojoType="alpine.layout.PopupDialog" draggable="false" id="system_licenseupdate_dialog" title="<fmt:message key='license_update_title'/>">
		<div class="titleBar">
            <fmt:message key='license_update_title'/>
        </div>
		<div dojoType="dijit.layout.LayoutContainer" style="width: 300px; height: 100px">
			<div dojoType="dijit.layout.ContentPane" region="center" style="width:100%;">
				<form dojoType="dijit.form.Form" id="system_licenseupdate_form">
					<table width="100%" cellspacing="5">
						<tr>
							<td>
								<fmt:message key="license_update_input_adminpwd"/>
							</td>
							<td>
								<input dojoType="dijit.form.ValidationTextBox" style="width: 150px" type="password" required="true" trim="true" id="system_licenseupdate_input_adminPwd">
							</td>
						</tr>
						<tr>
							<td>
								<fmt:message key="license_update_input_license"/>
							</td>
							<td>
								<input dojoType="dijit.form.ValidationTextBox" style="width: 150px" required="true" trim="true" id="system_licenseupdate_input_license">
							</td>
						</tr>
						<tr style="text-align:right;">
							<td colspan="2">
                                <button dojoType="dijit.form.Button" baseClass="cancelButton"  id="system_licenseupdate_button_close"><fmt:message key="Cancel"/></button>
                                <button dojoType="dijit.form.Button" type="button" baseClass="primaryButton" id="system_licenseupdate_button_save"><fmt:message key="OK"/></button>
							</td>
						</tr>
					</table>
				</form>
			</div>
		</div>
	</div>
</fmt:bundle>
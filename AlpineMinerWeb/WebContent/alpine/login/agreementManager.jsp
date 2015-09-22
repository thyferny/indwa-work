<script type="text/javascript">
	dojo.require("alpine.system.AgreementHelper");
</script>
<fmt:bundle basename="app">
	<div dojoType="dijit.Dialog" draggable="false" id="alpine_system_agreement_Dialog">
		<div dojoType="dijit.layout.LayoutContainer" style="width: 700px; height: 500px">
			<div dojoType="dijit.layout.ContentPane" region="top" style="width: 100%; height: 150px">
				<table width="100%" height="100%">
					<tr>
						<td align="center" valign="middle">
							<img alt="Alpine Data Labs" src="<%=path%>/images/interface/alpine_logo_illuminator.png">
						</td>
					</tr>
				</table>
			</div>
			<div dojoType="dijit.layout.ContentPane" region="center" style="width: 100%; height: 70%">
				<div align="center" style="height: 100%;">
					<iframe id="alpine_system_agreement_content_window" src="./EULA.html" width="90%" height="99%" frameBorder="1"></iframe>
				</div>
			</div>
		</div>
		<div class="whiteDialogFooter" style="height: 100px">
			<div align="center" style="height: 60px">
				<table width="90%" height="100%" cellspacing="0" cellpadding="0">
					<tr>
						<td align="left">
							<input dojoType="dijit.form.CheckBox" disabled="true" id="alpine_system_agreement_button_agree"> <fmt:message key="agreement_tip_agree"/>
						</td>
						<td align="right">
							<button type="button" dojoType="dijit.form.Button" id="alpine_system_agreement_button_print" baseclass="printButton" title="<fmt:message key="agreement_button_print"/>">&nbsp;</button>
						</td>
					</tr>
				</table>
			</div>
			<button dojoType="dijit.form.Button" baseclass="cancelButton" type="button" onClick="dijit.byId('alpine_system_agreement_Dialog').hide()"><fmt:message key="Cancel"/></button>
			<button dojoType="dijit.form.Button" disabled="true" baseclass="primaryButton" id="alpine_system_agreement_button_continue" type="button"><fmt:message key="agreement_button_continue"/></button>
		</div>
	</div>
</fmt:bundle>
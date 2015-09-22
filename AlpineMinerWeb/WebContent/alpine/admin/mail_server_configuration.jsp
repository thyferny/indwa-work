<script type="text/javascript">
    <!--
    dojo.require("alpine.system.MailServerConfigurationUIHelper");
    //-->
</script>
<fmt:bundle basename="app">
    <div dojoType="dijit.Dialog" id="mailServerConfigDialog" draggable="false"
         title="<fmt:message key="Mail_Server_Config"/>"
    style="background: #ffffff;">
	<div class="titleBar">
        <fmt:message key='Mail_Server_Config'/>
    </div>
    <table width="340px" height="240px" class="innerPadding">
        <tr>
            <td width="120px"><label class="valueLabel" for="smtpHost"><fmt:message
                    key="SMTP_HOST" /> </label></td>
            <td><input dojoType="dijit.form.ValidationTextBox" required="true"
                       type="text" id="smtpHost" name="smtpHost" />
            </td>
        </tr>
        <tr>
            <td width="100px"><label  class="valueLabel" for="port"><fmt:message
                    key="Mail_PORT" /> </label></td>
            <td><input dojoType="dijit.form.NumberTextBox"
                       required="true"
                       id="hostPort"
                       constraints="{min: 1,max: 999,places: 0}"
                       name="mail_server_port" id="mail_server_port"
                       invalidMessage="<fmt:message key="NumberTextValid"/>"
                >
            </td>
        </tr>
        <tr>
            <td width="100px"><label  class="valueLabel"><fmt:message key="PROTOCOL" /></label></td>
            <td>
                <input dojoType="dijit.form.CheckBox" id="mailThroughSSL">
                <select dojoType="dijit.form.Select" id="protocolType" disabled>
                    <option value="<%=MailConfiguration.PROTOCOL_TLS %>"><fmt:message key="PROTOCOL_TLS"/></option>
                    <option value="<%=MailConfiguration.PROTOCOL_SSL %>"><fmt:message key="PROTOCOL_SSL"/></option>
                </select>
            </td>
        </tr>
        <tr>
            <td width="120px"><label class="valueLabel" for="mail_account"><fmt:message
                    key="Mail_Account" /> </label></td>
            <td><input dojoType="dijit.form.ValidationTextBox"
                       required="true" trim="true" type="text" name="mail_account"
                       id="mail_account"  >
            </td>
        </tr>
        <tr>
            <td width="120px"><label class="valueLabel" for="mail_sender"><fmt:message
                    key="Mail_Sender" /> </label></td>
            <td><input dojoType="dijit.form.ValidationTextBox"
                       required="true" trim="true" type="text" name="mail_sender"
                       id="mail_sender"  >
            </td>
        </tr>
        <tr>
            <td width="120px"><label class="valueLabel" for="mail_login"><fmt:message
                    key="Mail_Login" /> </label></td>
            <td><input dojoType="dijit.form.ValidationTextBox"
                       required="true" trim="true" type="text" name="mail_login"
                       id="mail_login"  >
            </td>
        </tr>
        <tr>
            <td width="120px"><label class="valueLabel"><fmt:message
                    key="Mail_PASSWORD" /> </label></td>
            <td><input dojoType="dijit.form.ValidationTextBox"
                       required="true" trim="true" type="password" name="mail_password"
                       id="mail_password"  >
            </td>
        </tr>
    </table>
    <div class="whiteDialogFooter">
        <button dojoType="dijit.form.Button" id ="btn_cancel_mail_config"   type="button" baseclass="cancelButton"><fmt:message key="Cancel"/>
        </button>
        <button dojoType="dijit.form.Button" id ="btn_test_mail_config"   type="button"  baseclass="secondaryButton">
            <fmt:message key="SEND_TEST_MAIL"/>
        </button>
        <button id ="btn_save_mail_config" dojoType="dijit.form.Button" type="button" baseclass="primaryButton">
            <fmt:message key="SAVE"/>
        </button>
    </div>
    </div>

</fmt:bundle>
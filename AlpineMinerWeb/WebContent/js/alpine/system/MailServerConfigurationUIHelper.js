define(["alpine/system/MailServerConfigurationManager", "alpine/layout/GuiHelper", "dojox/validate/web"], function(mailManager, guiHelper, dval){

    var constants = {
        TEST_MAIL_BUTTON:"btn_test_mail_config",
        SAVE_MAIL_BUTTON:"btn_save_mail_config",
        CANCEL_MAIL_BUTTON:"btn_cancel_mail_config",
        EDIT_MAIL_CONFIG_BUTTON:"btn_edit_mail_config"

    };

    dojo.ready(function(){
        dojo.connect(dijit.byId(constants.TEST_MAIL_BUTTON), 'onClick', openTestMailEditor);
        dojo.connect(dijit.byId(constants.SAVE_MAIL_BUTTON), 'onClick', save_mail_config);
        dojo.connect(dijit.byId(constants.CANCEL_MAIL_BUTTON), 'onClick', cancel_mail_config_dialog);
        dojo.connect(dijit.byId(constants.EDIT_MAIL_CONFIG_BUTTON), "onClick", showMailConfigDialog);
		dojo.connect(dijit.byId("mailThroughSSL"),"onChange",dijit.byId("mailThroughSSL"),function(selected){
			dijit.byId("protocolType").set("disabled",!selected);
		});
    });


    function init_mail_config_dlg(mailConfig){
    if(mailConfig.host){
        dojo.byId("smtpHost").value = 	mailConfig.host;
    }else{
        dojo.byId("smtpHost").value = 	"";
    }
    if(mailConfig.port){
        dijit.byId("hostPort").set("value",mailConfig.port);
    }else{
        dijit.byId("hostPort").set("value","25");
    }

    dijit.byId("mailThroughSSL").set("checked",mailConfig.useSSL);
    dijit.byId("protocolType").set("value",mailConfig.sslProtocols);



    if(mailConfig.fromMail){
        dojo.byId("mail_account").value = mailConfig.fromMail ;
    }else{
        dojo.byId("mail_account").value = "" ;
    }
    if(mailConfig.sender)	{
        dojo.byId("mail_sender").value=	mailConfig.sender ;
    }else{
        dojo.byId("mail_sender").value=	"" ;
    }
    if(mailConfig.userName ){
        dojo.byId("mail_login").value = mailConfig.userName ;
    }else{
        dojo.byId("mail_login").value = "" ;
    }
    if(mailConfig.password){
        dojo.byId("mail_password").value=mailConfig.password  ;
    }else{
        dojo.byId("mail_password").value=""  ;
    }

}



    function openTestMailEditor(){
        if(validate_mail_config()==false){
            return;
        }
        var email = alpine.userInfo.email;
        guiHelper.showTextFieldEditorDialog(sendTestMail, email, null, "Send test mail to:", "Send", function(dijitid, value)
        {
             return dval.isEmailAddress(value);
        }, true);

    }

    function save_mail_config(){
        if(validate_mail_config()==false){
            return;
        }
        var mailConfig = {};
        mailConfig.host = dojo.byId("smtpHost").value;
        mailConfig.port = dojo.byId("hostPort").value;
        mailConfig.useSSL = dijit.byId("mailThroughSSL").get("checked");
        mailConfig.sslProtocols = dijit.byId("protocolType").get("value");

        mailConfig.fromMail = dojo.byId("mail_account").value;
        mailConfig.sender = dojo.byId("mail_sender").value;
        mailConfig.userName = dojo.byId("mail_login").value;
        mailConfig.password = dojo.byId("mail_password").value;

        mailManager.saveMailServerConfiguration(mailConfig, update_mail_config_callback, null,"mailServerConfigDialog")
    }
    function update_mail_config_callback(obj){

        if(obj.error_code){
            handle_error_result(obj);
            return ;
        }
        dijit.byId("mailServerConfigDialog").hide();
    }

    function cancel_mail_config_dialog(){
        dijit.byId("mailServerConfigDialog").hide();
    }


    function sendTestMail(emailAddress){

        var mailConfig = {};
        mailConfig.host = dojo.byId("smtpHost").value;
        mailConfig.port = dojo.byId("hostPort").value;
        mailConfig.useSSL = dijit.byId("mailThroughSSL").get("checked");
        mailConfig.sslProtocols = dijit.byId("protocolType").get("value");

        mailConfig.fromMail = dojo.byId("mail_account").value;
        mailConfig.sender = dojo.byId("mail_sender").value;
        mailConfig.userName = dojo.byId("mail_login").value;
        mailConfig.password = dojo.byId("mail_password").value;
        mailConfig.receiver = emailAddress;

        mailManager.sendTestMail(mailConfig, function(result){
            if(result&&result.error_code){
                handle_error_result(result);
                return ;
            }
            popupComponent.alert(alpine.nls.Mail_Test_Success, function(){
                guiHelper.hideTextFieldEditorDialog();
            });
        },null, "alpine_textfield_dialog")  ;

    }

    function get_mail_config_callback(mailConfig){
    	dijit.byId("mailServerConfigDialog").titleBar.style.display = "none";
        dijit.byId("mailServerConfigDialog").show();
        init_mail_config_dlg(mailConfig);
    }

    function showMailConfigDialog(){
        mailManager.getMailServerConfiguration(get_mail_config_callback, "FlowDisplayPanelPersonal");
    }

    function validate_mail_config() {


        if (!dijit.byId("smtpHost").isValid())
            return false;
        if(!dijit.byId("hostPort").isValid())
            return false;
        if (!dijit.byId("mail_account").isValid())
            return false;
        if (!dijit.byId("mail_sender").isValid())
            return false;
        if (!dijit.byId("mail_login").isValid())
            return false;
        if (!dijit.byId("mail_password").isValid())
            return false;

        return true;
    }
});
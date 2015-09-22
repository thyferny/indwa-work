
define(function(){

	var adminBaseURL = baseURL + "/main/admin.do";

    function _saveMailServerConfiguration(mailConfig, callback, errorCallback, callbackPanelId)
    {
        var url = adminBaseURL + "?method=updateMailServerConfig" ;
        ds.post(url,mailConfig ,callback, errorCallback, false,callbackPanelId);

    }

    function _sendTestMail(mailConfig, callback, errorCallback, callbackPanelId)
    {
        var url = adminBaseURL + "?method=testMailConfig" ;
        ds.post(url,mailConfig ,callback, errorCallback, false,callbackPanelId);

    }

    function _getMailServerConfiguration(get_mail_config_callback, callbackPanelId)
    {
        var url = adminBaseURL + "?method=getMailServerConfig" ;
        ds.get(url, get_mail_config_callback, null, false, callbackPanelId);
   }

    return {
        saveMailServerConfiguration: _saveMailServerConfiguration,
        sendTestMail:_sendTestMail,
        getMailServerConfiguration:_getMailServerConfiguration
     };
});
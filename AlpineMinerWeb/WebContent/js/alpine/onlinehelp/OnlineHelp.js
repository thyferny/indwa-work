define([
    "../../dijit/registry",
    "dojo/dom",
    "dojo/has",
    "dojo/keys",
    "dojo/on",
    "dojo/ready",
    "dojo/_base/window",
    "alpine/flow/OperatorManagementUIHelper",
    "dojo/i18n!../nls/labels"
], function (registry, dom, has, keys, on, ready, win, opManagement, i18nLabels) {

    var constants = {
        atlassianURL: "https://alpine.atlassian.net/wiki/display/DOC"
    };

    ready(function () {
        if (has("ie")) {
            window.onhelp = function(event){
                showGeneralHelp();
                return false;
            };

        } else {
            on(win.body(), "keydown",function(evt){
                var charOrCode = evt.charCode || evt.keyCode;
                if (charOrCode ==  keys.F1)
                {
                    var operatorPrimaryInfo = opManagement.getSelectedOperator(true);
                    if(operatorPrimaryInfo){
                        showOperatorHelp(baseURL, operatorPrimaryInfo.classname);
                    } else {
                        showGeneralHelp();
                    }
                }
            });
        }

    });

    function showOperatorHelp(baseURL, operatorClassName){
        var langLocale = dojo.config.locale;
        var prefix = "";
        switch(langLocale) {
            case "zh":
                prefix = "ZH";
                break;
            case "ja":
                prefix = "JA";
                break;
            default:
                break;
        }
        var url = constants.atlassianURL + prefix + "/" + i18nLabels[operatorClassName].replace(" ","+");
        window.open(url);
        /* English Only
        var langLocale = dojo.config.locale;
        var url;
        if (langLocale == "zh" || langLocale == "ja") {
            url = baseURL + "/alpine/onlinehelp/index.jsp?operatorName=" + operatorClassName;
            window.open(url,'helpWindow');
        } else {
            url = constants.atlassianURL + "/" + i18nLabels[operatorClassName].replace(" ","+");
            window.open(url);
        }*/
    }
    function showGeneralHelp(){
        var langLocale = dojo.config.locale;
        var prefix = "";
        switch(langLocale) {
            case "zh":
                prefix = "ZH";
                break;
            case "ja":
                prefix = "JA";
                break;
            default:
                break;
        }
        url = constants.atlassianURL + prefix;
        window.open(url);

        /* English Only
        var langLocale = dojo.config.locale;
        var url;
        if (langLocale == "zh" || langLocale == "ja") {
            url = baseURL + "/alpine/onlinehelp/index.jsp?operatorName=Overview";
            window.open(url,'helpWindow');
        } else {
            url = constants.atlassianURL;
            window.open(url);
        }*/
    }

    return {
        showOperatorHelp: showOperatorHelp,
        showGeneralHelp: showGeneralHelp
    };
});


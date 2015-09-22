define([
    "dijit/registry",
    "dojo/dom",
    "dojo/has",
    "dojo/keys",
    "dojo/on",
    "dojo/ready",
    "dojo/_base/window"
], function (registry, dom, has, keys, on, ready, win) {

    var constants = {
        atlassianURL: "https://alpine.atlassian.net/wiki/display/DOC"
    };

    ready(function () {
        if (has("ie"))
        {
            window.onhelp = function(event){
                showGeneralHelp();
                return false;
            };

        } else {
            on(win.body(), "keydown",function(evt){
                var charOrCode = evt.charCode || evt.keyCode;
                if (charOrCode ==  keys.F1) {
                    showGeneralHelp();
                }
            });
        }

    });

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
        showGeneralHelp: showGeneralHelp
    };
});


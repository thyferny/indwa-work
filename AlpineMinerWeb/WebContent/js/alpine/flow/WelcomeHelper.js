define(["dojo/dom-construct", "alpine/flow/OperatorManagementManager", "alpine/operatorexplorer/OperatorUtil",
    "dijit/registry",
    "dojo/dom",
    "dojo/on",
    "dojo/ready",
    "dojo/string",
    "dojo/_base/array",
    "dojo/_base/declare",
    "alpine/flow/WorkFlowManager",
    "alpine/flow/WorkFlowUIHelper",
    "alpine/onlinehelp/OnlineHelp"],


    function (domConstruct, operatorManager, operatorUtil,registry,dom,on, ready,string, array, declare, WorkFlowManager, WorkFlowUIHelper, OnlineHelp) {

        var constants =
        {
            userLogin: "userLogin",
            settings_button: "settings_button",
            logout_button:"logout_button",
            general_help_button:"general_help_button"
        }

        ready(function () {
            alpine.USER =  alpine.userInfo.login;
            var login = alpine.userInfo.login;
            registry.byId(constants.settings_button).set("label", string.substitute(
                registry.byId(constants.settings_button).get("label"),
                { username:login })
            );


            on(registry.byId(constants.settings_button), "click", check_auth_type);
            on(registry.byId(constants.logout_button), "click", user_logout)
            on(registry.byId(constants.general_help_button), "click", OnlineHelp.showGeneralHelp)
        });

        function check_auth_type() {
            var btn = registry.byId("groupusers_button");
            if (btn) {
                if(alpine.auth_type == "LocalProvider") {
                    btn.set("disabled", false);
                }
                else {
                    btn.set("disabled", true);
                }
            }
        }


        function user_logout() {
            var url = baseURL + "/main/admin.do?method=logout";
            var user = {login : alpine.USER};

            if (WorkFlowManager.isEditing() &&WorkFlowManager.isDirty()) {
                popupComponent.saveConfirm(alpine.nls.update_not_saved, {
                    handle: function(){
                        var saveFlowCallback = function(){
                            ds.post(url, user, user_logout_callback, null);
                        };
                        //save_flow();
                        WorkFlowUIHelper.saveWorkFlow(saveFlowCallback);
                    }
                }, {
                    handle: function(){
                        WorkFlowManager.cancelEditingFlow(function(){
                            ds.post(url, user, user_logout_callback, null);
                        });
                    }
                });
            }else{
                ds.post(url, user, user_logout_callback, null);

            }
        }

        function user_logout_callback() {
            window.top.location.pathname = logoutURL;
        }

    });
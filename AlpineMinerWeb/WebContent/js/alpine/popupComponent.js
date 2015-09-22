var popupComponent = (function(){
    var alertInstance;
    var confirmInstance;

    function isNull(val){
        return val == undefined || val == null || val == "";
    }

    function showAlert(content, title, callbackHandle){
        var _title = null;
        if(typeof title != "function"){
            _title = isNull(title) ? "" : title;
        }else{
            _title = "";
            callbackHandle = title;
        }
        var callBack = callbackHandle || function(){};
        if(!alertInstance){
            alertInstance = new dijit.Dialog({
                id: "alertPopupInstance",
                alpine_alert_handler: null
            });
            dojo.addClass(alertInstance, "popup_alert");
        }
        if(alertInstance.alpine_alert_handler != null){
            //disconnect ...
            dojo.disconnect(alertInstance.alpine_alert_handler);
        }


        alertInstance.alpine_alert_handler = dojo.connect(alertInstance, "onHide", callBack);

        alertInstance.set("title", _title);



        var contentNode = dojo.create("div",   { style:"width:400px;"});  //draggable="false" use this to get rid of top bar?
        dojo.addClass(contentNode, "alertDialogMainPanel");
        contentNode.innerHTML = "<div class=\"alertDialogContent\">" + content + "</div>";
        var buttonNode = dojo.create("div", {style:"align: center"}, contentNode);
        dojo.addClass(buttonNode, "whiteDialogFooter");
        var okButton = new dijit.form.Button({
            baseClass:"primaryButton",
            label: alpine.nls.OK ,
            onClick: function(){
                dijit.byId('alertPopupInstance').hide();
            }
        });
        buttonNode.appendChild(okButton.domNode);
        alertInstance.set("content", contentNode);
        alertInstance.titleBar.style.display='none';
        alertInstance.show();
    }

    /**
     * arguments description
     * accept = {
     * 		label: (Option) The name of button. "Yes" will be shown if undefined or null.
     * 		handle: Callback function when click button.
     * }
     *
     * abort = {
     * 		hidden: (default false) display cancel button if false.
     * 		label: (Option) The name of button. "No" will be shown if undefined or null.
     * 		handle: Callback function when click button.
     * }
     *
     * cancel button always shown, whatever the argument is provided or not. Except the argument of hidden is true.
     * cancel = {
     * 		hidden: (default false) display cancel button if false.
     * 		label: (Option) The name of button. "Cancel" will be shown if undefined or null.
     * 		handle: (Option) Callback function, invoked by after close dialog.
     * }
     */
    function showSaveConfirm(content, title, accept, abort, cancel, style){
        if(typeof arguments[1] != "string"){//means there is no title parameter.
        	style = arguments[4];
            cancel = arguments[3];
            abort = arguments[2];
            accept = arguments[1];
            title = "";
        }
        getConfirmInastance(style);
        confirmInstance.set("title",title);
        var contentNode = dojo.create("div");
        contentNode.innerHTML = "<div class=\"innerPadding\">" + content + "</div>";
        var buttonNode = dojo.create("div", null, contentNode);
        dojo.addClass(buttonNode,"whiteDialogFooter");

        if(!cancel || cancel.hidden != true){//if cancel was not provide or attribute of hidden was false, so show it.
            var gobackButton = new dijit.form.Button({
                baseClass:"cancelButton",
                tabIndex: "2",
                label: (isNull(cancel) || isNull(cancel.label)) ?  alpine.nls.Cancel  : cancel.label,
                onClick: function(){
                    dijit.byId('confirmPopupInstance').hide();
                    if(cancel && cancel.handle){
                        cancel.handle();
                    }
                }
            });
            buttonNode.appendChild(gobackButton.domNode);
        }

        if(abort && (abort.hidden != true)){
            var abortButton = new dijit.form.Button({
                baseClass:"secondaryButton",
                tabIndex: "3",
                label: isNull(abort.label) ? alpine.nls.No : abort.label,
                onClick: function(){
                    dijit.byId('confirmPopupInstance').hide();
                    abort.handle();
                }
            });
            buttonNode.appendChild(abortButton.domNode);
        }
        var acceptButton = new dijit.form.Button({
            baseClass:"primaryButton",
            tabIndex: "1",
            label: isNull(accept.label) ? alpine.nls.Yes : accept.label,
            onClick: function(){
                dijit.byId('confirmPopupInstance').hide();
                accept.handle();
            }
        });
        buttonNode.appendChild(acceptButton.domNode);

        confirmInstance.set("content", contentNode);
        confirmInstance.show();
    }

    function showConfirm(content, title, accept, cancel, style){
        if(typeof arguments[1] != "string"){//means there is no title parameter.
        	style = arguments[3];
            cancel = arguments[2];
            accept = arguments[1];
            title = "";
        }
        if(!cancel){
            cancel = {};
        }
        accept.label = accept.label ? accept.label : alpine.nls.OK;
        cancel.label = cancel.label ? cancel.label : alpine.nls.Cancel;

        showSaveConfirm(content, title, accept, {
            hidden: true
        }, cancel, style);
    }

    function getConfirmInastance(style){
        if(!confirmInstance){
            confirmInstance = new dijit.Dialog({
                id: "confirmPopupInstance",
                style: style ? style : {
                    width: "300px"
                }
            });
            confirmInstance.titleBar.style.display='none';

        }
        return confirmInstance;
    }

    return {
        alert: showAlert,
        confirm: showConfirm,
        saveConfirm: showSaveConfirm
    };
})();

var progressBar = (function(){
    var loadingImage = "../../images/progressBar.gif";
    var progressBarInstance;

    function getProgressContent(){
        if(!progressBarInstance){
            progressBarInstance = new dijit.Dialog({
                id: "progressBarInstance",
                //duration: 1, // is this arrtibute can be work?
                style: {
                    width: "300px"
                }
            });
            dojo.addClass(progressBarInstance, "popup_alert");
            var imgContainer = dojo.create("div");
            dojo.create("img", {src: loadingImage, width: "100%", align: "center"}, imgContainer);
            progressBarInstance.set("content", imgContainer);
        }
        return progressBarInstance;
    }

    function showProgressBar(){
        console.log("USING THE PROGRESS BAR!!!!!!!!!!!!!!!!!!!!!!!!!!");
        getProgressContent();
        if(!progressBarInstance.open){
            progressBarInstance.show();
        }
    }
    function closeProgressBar(){
        getProgressContent();
        if(progressBarInstance.open){
            progressBarInstance.hide();
        }
    }
    return {
        showLoadingBar: showProgressBar,
        closeLoadingBar: closeProgressBar
    };
})();
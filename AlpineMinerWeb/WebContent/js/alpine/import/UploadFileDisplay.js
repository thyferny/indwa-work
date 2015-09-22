/* COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.
 * UploadFileDisplay.js
 * Author Robbie
 * Aug 7, 2012
 */

define(["dojox/form/uploader/FileList"],function(FileList) {

    var constants = {
        FILE_ICON: baseURL + "/images/interface/file_upload_csv.png",
        ID_FILE_ICON: "alpineUploaderFileIcon",
        UPLOAD_FILE_SPAN: "SPAN_alpine_import_importData_upload_file"
    };

    dojo.declare('alpine.import.UploadFileDisplay', [FileList], {

        fileType: null,
        fileName: null,
        fileSize: null,

        //override
        templateString:
            '<span class="alpineUploaderFileList">' +
                '<table><tr>' +
                '<td><img id="alpineUploaderFileIcon" src=' + constants.FILE_ICON + ' hidden="true" /></td>' +
                '<td style="width: 100%"><span dojoAttachPoint="nameNode" class="alpineUploaderFileListText" id="fileuploadnameid"></span>' +
                '<span dojoAttachPoint="progressNode" class="alpineUploaderFileListProgress">' +
                    '<span dojoAttachPoint="percentBarNode" class="alpineUploaderFileListProgressBar"></span>' +
                '</span></td></tr></table>' +
                '<span dojoAttachPoint="percentTextNode" class="alpineUploaderFileListPercentText" hidden="true">0%</span>' +
           '</span>',

        //override
        _addRow: function(index, type, name, size) {
            this.fileType = type;
            this.fileName = name;
            this.fileSize = size;

            var n = this.nameNode;
            n.innerHTML = name + ', ' + this.convertBytes(size).value;
            dojo.byId(constants.UPLOAD_FILE_SPAN).hidden=true; //find a better way to do this...
            dojo.byId(constants.ID_FILE_ICON).hidden=false; //find a better way to do this...
        },
        //override
        reset: function(){
            this.fileType = null;
            this.fileName = null;
            this.fileSize = null;

            var n = this.nameNode;
            n.innerHTML = "";

            dojo.byId(constants.ID_FILE_ICON).hidden=true; //find a better way to do this...
        },

        //override
        hideProgress: function(/* Boolean */animate){
            var o = animate ? {
                ani:true,
                endDisp:"none",
                beg:6,   //this is hardcoded to match .alpineUploaderFileListProgress {height: 6px }(and was to 15 px height)
                end:0
            } : {
                endDisp:"none",
                ani:false
            };
            //dijit.byId(constants.ID_FILE_ICON).set("hidden", true);
            this._hideShowProgress(o);
        },

        //override
        showProgress: function(/* Boolean */animate){
            var o = animate ? {
                ani:true,
                endDisp:"block",
                beg:0,
                end:6   //this is hardcoded to match .alpineUploaderFileListProgress {height: 6px }(was hardcoded in FileList.js to 15 px height)
            } : {
                endDisp:"block",
                ani:false
            };
            this._hideShowProgress(o);
        },

        getUploadFileType: function(){
            return this.fileType;
        },

        getUploadFileName: function(){
            return this.fileName;
        }
    });
});
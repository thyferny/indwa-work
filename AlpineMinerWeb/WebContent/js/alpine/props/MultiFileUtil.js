/**
 * User: robbie
 * Date: 10/29/12
 * (c) Alpine Data Labs 2012
 */

define([],function(){

    function _validateHadoopInputFiles(inputFilesInfo) {
        if (inputFilesInfo == null || inputFilesInfo.length < 2) {
            popupComponent.alert(alpine.nls.multi_file_2plus_files);
            return false;
        }
        for (var i=0;i<inputFilesInfo.length;i++) {
            if (!inputFilesInfo[i].columnInfo) {
                popupComponent.alert(alpine.nls.multi_file_config_struc);
                return false;
            }
        }
        return true;
    }


    function _validateDBInputFiles(inputFilesInfo) {
        if (inputFilesInfo == null || inputFilesInfo.length < 2) {
            popupComponent.alert(alpine.nls.multi_file_2plus_files);
            return false;
        }
        return true;
    }

    function _validateHadoopInputFromSame(inputFilesInfo){
      var inputSource = [];
      if(inputFilesInfo!=null){
         for(var i=0;i<inputFilesInfo.length;i++){
            if(dojo.indexOf(inputSource,inputFilesInfo[i].hdfsHostname)==-1){
                inputSource.push(inputFilesInfo[i].hdfsHostname);
            }
         }
      }
      if(inputSource.length==1){
          return true;
      }else{
          popupComponent.alert(alpine.nls.multi_file_2plus_different_from);
          return false;
      }
    }

    function _validateDBInputFromSame(inputFilesInfo){
        var inputSource = [];
        if(inputFilesInfo!=null){
            for(var i=0;i<inputFilesInfo.length;i++){
                if(dojo.indexOf(inputSource,inputFilesInfo[i].url)==-1){
                    inputSource.push(inputFilesInfo[i].url);
                }
            }
        }
        if(inputSource.length==1){
            return true;
        }else{
            popupComponent.alert(alpine.nls.multi_file_2plus_different_from);
            return false;
        }
    }



    return {
        validateDBInputFiles: _validateDBInputFiles,
        validateHadoopInputFiles: _validateHadoopInputFiles,
        validateHadoopInputFromSame:_validateHadoopInputFromSame,
        validateDBInputFromSame:_validateDBInputFromSame
    }


});
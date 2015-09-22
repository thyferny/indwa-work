/**
 * User: sasher
 * Date: 7/3/12
 * Time: 2:28 PM
 */

define([
    "dojo/DeferredList",
    "dojo/_base/Deferred"],

    function( DeferredList, Deferred) {

    var constants = {
        UDF_REQUEST_URL: baseURL + "/main/udf.do?",
        OP_REQUEST_URL: baseURL + "/main/operatorManagement.do"
        
    };
    function _getFrequentlyUsedOperators(def, errorCallback)
    {
//           console.log("okay, doing the _getFreqUsedOperators call");
            ds.get(constants.OP_REQUEST_URL + "?method=getOperatorFrequence", function(freqinfo){
                console.log("got freq op");
                if (def) def.callback(freqinfo);
            }, errorCallback);
    }

    function _getCustomOperators(def)
    {
            var requestUrl = udfBaseURL + "?method=getUDFModels";
            ds.get(requestUrl,function(data){
                var result = data;
                if (def) def.callback(result);
            }, function(error)
            {
                if (def && def.errorback) def.errorback(error);
            });
    }

    function _getRecentAndCustomOperators(handleFreqOperators, handleCustomizedOperators, dataAllLoaded,errorCallback)
    {

        var deferredOne = new Deferred();
        deferredOne.addCallback(function(res) {
        handleCustomizedOperators(res);
        });


        var deferredTwo = new Deferred();
        deferredTwo.addCallback(function(res){
        {
            handleFreqOperators(res);
        }
        });

        _getFrequentlyUsedOperators(deferredTwo);
        _getCustomOperators(deferredOne);

         var allFuncs = new DeferredList([deferredOne, deferredTwo]);
        allFuncs.then(dataAllLoaded);

    }




    return {
        getFrequentlyUsedOperators: _getFrequentlyUsedOperators,
        getRecentAndCustomOperators: _getRecentAndCustomOperators
    };


});
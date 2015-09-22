/**
 * User: sasher
 * Date: 7/26/12
 * Time: 2:34 PM
 */

define(function(){

    var FREQ_OP_ARRAY = [];

   function _setFrequentOperators(serverData)
    {
        if (FREQ_OP_ARRAY && FREQ_OP_ARRAY.length> 0) return;
        FREQ_OP_ARRAY = new Array();

        var obj1 = {};
        obj1.opkey =  "LogisticRegressionOperator";
        obj1.opfreq = 3;

        var obj2 = {};
        obj2.opkey =  "VariableOperator";
        obj2.opfreq = 2;

        var obj3 = {};
        obj3.opkey =  "RandomSamplingOperator";
        obj3.opfreq = 5;

        var obj4 = {};
        obj4.opkey =  "TableJoinOperator";
        obj4.opfreq = 3;

        //Need to figure out how to get this data.
        FREQ_OP_ARRAY.push(obj1);
        FREQ_OP_ARRAY.push(obj2);
        FREQ_OP_ARRAY.push(obj3);
        FREQ_OP_ARRAY.push(obj4);
     }


    function _addOperatorToClientHash(key)
    {
        console.log("adding: " + key);
        //see if already in freqently used array
        var foundit = false;
        for (var i = 0; i < FREQ_OP_ARRAY.length && !foundit; i++) {
            var object = FREQ_OP_ARRAY[i];
            if (object.opkey == key)
            {
                //found it!
                object.opfreq++;
                foundit = true;
            }
        }

        //if not there, add it.
       if (!foundit)
       {
           var newobj = {};
           newobj.opkey =  key;
           newobj.opfreq = 1;
           FREQ_OP_ARRAY.push(newobj);
       }

       //now need to sort the array
        FREQ_OP_ARRAY.sort(function compare(a,b) {
            if (a.opfreq  < b.opfreq)
                return 1;
            if (a.opfreq > b.opfreq)
                return -1;
            return 0;
        });
        console.log("sorting!");
    }

    function _getTopSixOperators()
    {
        return _getTopOperators(6);
    }

    function _getTopOperators(number)
    {
        var topFreqArray = [];

        for (var i = 0; i < FREQ_OP_ARRAY.length && i < number; i++) {
            var object = FREQ_OP_ARRAY[i];
            topFreqArray.push(object.opkey);
        }

        return topFreqArray.sort();
    }

    function _isEmpty()
    {
        return (FREQ_OP_ARRAY.length < 1);
    }


    return {
        getTopOperators: _getTopOperators,
        getTopSixOperators: _getTopSixOperators,
        addOperatorToClientHash: _addOperatorToClientHash,
        setFrequentOperators: _setFrequentOperators,
        isEmpty: _isEmpty
    };


});
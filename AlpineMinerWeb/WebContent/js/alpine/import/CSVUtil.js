/**
 * User: Robbie
 * Date: 8/14/12
 */

/*
Resources:
    http://stackoverflow.com/questions/8493195/how-can-i-parse-a-csv-string-with-javascript/8497474#8497474
*/

define(function(){

    function _csvArrayToArrayOfArrays(rowData, csvParams) {
        var separator = csvParams.separator;
        var quote = csvParams.quote;
        var escaped = csvParams.escaped;

        var parsedArray = [];

        // if no quote and no escape, we don't need anything complicated...
        if (quote + escaped == "") {
            for(var i = 0;i < rowData.length;i++){
                if (rowData[i]=="") {continue;}
                parsedArray.push(rowData[i].split(separator));
            }
            return parsedArray;
        }

        var originalSeparator = separator;
        var originalQuote = quote;
        var originalEscaped = escaped;

        separator = _escapeAllRegExSpecialChars(separator);
        escaped = _escapeAllRegExSpecialChars(escaped);
        quote = _escapeAllRegExSpecialChars(quote);

        // build the CSV line parser regex
        if (quote == "") { // when user has specified no quote value
           if (originalSeparator == "\u0020" || originalSeparator == "\u0009") { //special treatment for space and tab
                var reValue = /(?![^\SL]*$)[^\SL]*(?:([^LE]*(?:[^\SL]+[^LE]+)*))[^\SL]*(?:L|$)/g;
            } else { // base for non-whitespace delimiters
                var reValue = /(?!\s*$)\s*(?:([^L\sE]*(?:\s+[^L\sE]+)*))\s*(?:L|$)/g;
            }
        } else { // defined quote character
            if (originalSeparator == "\u0020" || originalSeparator == "\u0009") { //special treatment for space and tab
                var reValue = /(?![^\SL]*$)[^\SL]*(?:D([^DE]*(?:E[\S\s][^DE]*)*)D|([^LDE]*(?:[^\SL]+[^LDE]+)*))[^\SL]*(?:L|$)/g;
            } else { // base for non-whitespace delimiters
                var reValue = /(?!\s*$)\s*(?:D([^DE]*(?:E[\S\s][^DE]*)*)D|([^LD\sE]*(?:\s+[^LD\sE]+)*))\s*(?:L|$)/g;
            }
        }

        //Mix-in user-defined quote/delimiter/escape with reValue
        reValue = RegExp(reValue.source.replace(/L/g, separator), 'g');
        reValue = RegExp(reValue.source.replace(/E/g, escaped), 'g');
        reValue = RegExp(reValue.source.replace(/D/g, quote), 'g');

        //Prepare to remove escapes from before any character that is not quote
        var removeEscapes = /E(?=[^DE])/g;
        removeEscapes = RegExp(removeEscapes.source.replace(/D/g, quote), 'g');
        removeEscapes = RegExp(removeEscapes.source.replace(/E/g, escaped), 'g');

        //Prepare to remove escapes from quotes in quoted strings
        var postSplitUnescape = /ED/g;
        postSplitUnescape = RegExp(postSplitUnescape.source.replace(/D/, quote), 'g');
        postSplitUnescape = RegExp(postSplitUnescape.source.replace(/E/, escaped), 'g');

        var reCounter = RegExp(separator,'g');

        /* Handle use of DD in addition to ED  */
        if (quote && escaped) {
            var repeatedQuotes = {
                beginQ: /LDDD/g,
                endQ: /DDDL/g,
                centerQ: /DD(?=[^L])/g
            };
            repeatedQuotes.beginQ = RegExp(repeatedQuotes.beginQ.source.replace(/L/g, separator), 'g');
            repeatedQuotes.beginQ = RegExp(repeatedQuotes.beginQ.source.replace(/D/g, quote), 'g');
            repeatedQuotes.endQ = RegExp(repeatedQuotes.endQ.source.replace(/L/g, separator), 'g');
            repeatedQuotes.endQ = RegExp(repeatedQuotes.endQ.source.replace(/D/g, quote), 'g');
            repeatedQuotes.centerQ = RegExp(repeatedQuotes.centerQ.source.replace(/L/g, separator), 'g');
            repeatedQuotes.centerQ = RegExp(repeatedQuotes.centerQ.source.replace(/D/g, quote), 'g');
            repeatedQuotes.centerQ = RegExp(repeatedQuotes.centerQ.source.replace(/E/g, escaped), 'g');
        }

        //Parse each element from rowData
        var output;
        for(var i = 0;i < rowData.length;i++){
            if(rowData[i]!="") {
                if ((rowData[i].match(reCounter)||[]).length == 0) { //if no separator in the string then just return the entire string
                    output = [];
                    output.push(rowData[i]);
                } else {
                    if (quote && escaped) {
                        var rowDataTemp = [];
                        rowDataTemp[i] = _escapeRepeatedQuoteChar(rowData[i], repeatedQuotes);
                        output = _csvLineToArray(rowDataTemp[i], separator, quote, reValue, removeEscapes, postSplitUnescape);
                    } else {
                        output = _csvLineToArray(rowData[i], separator, quote, reValue, removeEscapes, postSplitUnescape);
                    }
                }
                parsedArray.push(output);
            }
        }

        return parsedArray;

        function _escapeRepeatedQuoteChar(line, regex) {
            line = line.replace(regex.beginQ, originalSeparator + originalQuote + originalEscaped + originalQuote);
            line = line.replace(regex.endQ , originalEscaped + originalQuote + originalQuote + originalSeparator);
            line = line.replace(regex.centerQ, originalEscaped + originalQuote);
            return line;
        }
    }

    function _csvLineToArray(csv, separator, quote, reValue, removeEscapes, postSplitUnescape) {

        //Remove escapes from before any character that is not quote
        csv = csv.replace(removeEscapes, "");

        // line to array (replace with callback)
        var outputArray = [];
        csv.replace(reValue, function(m0, m1, m2) {
            if (!(m1 || m2)) { //firefox returns "" when all other browsers return undefined, so we have to do this...
                outputArray.push("");
            } else {
                if (m1) {
                    // Remove escape from any escape,quote in a quoted string
                    outputArray.push(m1.replace(postSplitUnescape, quote));
                } else if (m2) {
                    outputArray.push(m2);
                }
            }
            return '';
        });

        // Handle special case of empty last value.
        var reEmptyLast = /S\s*$/;
        reEmptyLast = RegExp(reEmptyLast.source.replace(/S/, separator));
        if (reEmptyLast.test(csv)) {
            outputArray.push('');
        }

        return outputArray;
    }

    //before putting a string into the regEx, escape it if it's a regEx special character
    function _escapeAllRegExSpecialChars(origString) {
        return origString.replace(/([.?*+^$[\]\\(){}|-])/g, "\\$1");
    }

    function _guessDelimiter(stringRow) {
        var separators = new Array(
            ",",       //comma
            "\u0009",  //tab
            "\u007c",  //pipe
            ";",       //semicolon
            " "        //space
        );
        var separatorsName = new Array(
            "Comma",       //comma
            "Tab",         //tab
            "Pipe",        //pipe
            "Semicolon",   //semicolon
            "Space"        //space
        );
        var counts = new Array(separators.length);
        for (j=0; j<separators.length; j++) {
            var reCounter = RegExp(_escapeAllRegExSpecialChars(separators[j]),'g');
            counts[j] = (stringRow.match(reCounter)||[]).length;
        }
        var maxIndex = 0;
        var maxValue = counts[0];
        for (j=1; j<separators.length; j++) {
            if (counts[j] > maxValue) {
                maxValue = counts[j];
                maxIndex = j;
            }
        }
        return separatorsName[maxIndex];

    }

    function _isNumber(n) {
        return !isNaN(parseFloat(n)) && isFinite(n);
    }

    return {
        csvArrayToArrayOfArrays: _csvArrayToArrayOfArrays,
        csvLineToArray: _csvLineToArray,
        guessDelimiter: _guessDelimiter,
        isNumber: _isNumber
    };

});
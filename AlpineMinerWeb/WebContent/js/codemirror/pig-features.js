//============================================codemirror-2.34/mode/pig/pig.js=======================================================
	/*
	 *	Pig Latin Mode for CodeMirror 2 
	 *	@author Prasanth Jayachandran
	 *	@link 	https://github.com/prasanthj/pig-codemirror-2
	 *  This implementation is adapted from PL/SQL mode in CodeMirror 2.
	*/
	CodeMirror.defineMode("pig", function(config, parserConfig) {
		var indentUnit = config.indentUnit,
			keywords = parserConfig.keywords,
			builtins = parserConfig.builtins,
			types = parserConfig.types,
			multiLineStrings = parserConfig.multiLineStrings;
		
		var isOperatorChar = /[*+\-%<>=&?:\/!|]/;
		
		function chain(stream, state, f) {
			state.tokenize = f;
			return f(stream, state);
		}
		
		var type;
		function ret(tp, style) {
			type = tp;
			return style;
		}
		
		function tokenComment(stream, state) {
			var isEnd = false;
			var ch;
			while(ch = stream.next()) {
				if(ch == "/" && isEnd) {
					state.tokenize = tokenBase;
					break;
				}
				isEnd = (ch == "*");
			}
			return ret("comment", "comment");
		}
		
		function tokenString(quote) {
			return function(stream, state) {
				var escaped = false, next, end = false;
				while((next = stream.next()) != null) {
					if (next == quote && !escaped) {
						end = true; break;
					}
					escaped = !escaped && next == "\\";
				}
				if (end || !(escaped || multiLineStrings))
					state.tokenize = tokenBase;
				return ret("string", "error");
			};
		}
		
		function tokenBase(stream, state) {
			var ch = stream.next();
			
			// is a start of string?
			if (ch == '"' || ch == "'")
				return chain(stream, state, tokenString(ch));
			// is it one of the special chars
			else if(/[\[\]{}\(\),;\.]/.test(ch))
				return ret(ch);
			// is it a number?
			else if(/\d/.test(ch)) {
				stream.eatWhile(/[\w\.]/);
				return ret("number", "number");
			}
			// multi line comment or operator
			else if (ch == "/") {
				if (stream.eat("*")) {
					return chain(stream, state, tokenComment);
				}
				else {
					stream.eatWhile(isOperatorChar);
					return ret("operator", "operator");
				}
			}
			// single line comment or operator
			else if (ch=="-") {
				if(stream.eat("-")){
					stream.skipToEnd();
					return ret("comment", "comment");
				}
				else {
					stream.eatWhile(isOperatorChar);
					return ret("operator", "operator");
				}
			}
			// is it an operator
			else if (isOperatorChar.test(ch)) {
				stream.eatWhile(isOperatorChar);
				return ret("operator", "operator");
			}
			else {
				// get the while word
				stream.eatWhile(/[\w\$_]/);
				// is it one of the listed keywords?
				if (keywords && keywords.propertyIsEnumerable(stream.current().toUpperCase())) {
					if (stream.eat(")") || stream.eat(".")) {
						//keywords can be used as variables like flatten(group), group.$0 etc..
					}
					else {
						return ("keyword", "keyword");
					}
				}
				// is it one of the builtin functions?
				if (builtins && builtins.propertyIsEnumerable(
						["load", "using", "as", "group", "by", "foreach", "generate", "dump"].indexOf(stream.current()) != -1 ? stream.current().toUpperCase() : stream.current()))
				{
					return ("keyword", "variable-2");
				}
				// is it one of the listed types?
				if (types && types.propertyIsEnumerable(stream.current().toUpperCase()))
					return ("keyword", "variable-3");
				// default is a 'variable'
				return ret("variable", "pig-word");
			}
		}
		
		// Interface
		return {
			startState: function(basecolumn) {
				return {
					tokenize: tokenBase,
					startOfLine: true
				};
			},
			
			token: function(stream, state) {
				if(stream.eatSpace()) return null;
				var style = state.tokenize(stream, state);
				return style;
			}
		};
	});

	(function() {
		function keywords(str) {
			var obj = {}, words = str.split(" ");
			for (var i = 0; i < words.length; ++i) obj[words[i]] = true;
	 		return obj;
	 	}

		// builtin funcs taken from trunk revision 1303237
		var pBuiltins = "ABS ACOS ARITY ASIN ATAN AVG BAGSIZE BINSTORAGE BLOOM BUILDBLOOM CBRT CEIL " 
		+ "CONCAT COR COS COSH COUNT COUNT_STAR COV CONSTANTSIZE CUBEDIMENSIONS DIFF DISTINCT DOUBLEABS "
		+ "DOUBLEAVG DOUBLEBASE DOUBLEMAX DOUBLEMIN DOUBLEROUND DOUBLESUM EXP FLOOR FLOATABS FLOATAVG "
		+ "FLOATMAX FLOATMIN FLOATROUND FLOATSUM GENERICINVOKER INDEXOF INTABS INTAVG INTMAX INTMIN "
		+ "INTSUM INVOKEFORDOUBLE INVOKEFORFLOAT INVOKEFORINT INVOKEFORLONG INVOKEFORSTRING INVOKER "
		+ "ISEMPTY JSONLOADER JSONMETADATA JSONSTORAGE LAST_INDEX_OF LCFIRST LOG LOG10 LOWER LONGABS "
		+ "LONGAVG LONGMAX LONGMIN LONGSUM MAX MIN MAPSIZE MONITOREDUDF NONDETERMINISTIC OUTPUTSCHEMA  "
		+ "PIGSTORAGE PIGSTREAMING RANDOM REGEX_EXTRACT REGEX_EXTRACT_ALL REPLACE ROUND SIN SINH SIZE "
		+ "SQRT STRSPLIT SUBSTRING SUM STRINGCONCAT STRINGMAX STRINGMIN STRINGSIZE TAN TANH TOBAG "
		+ "TOKENIZE TOMAP TOP TOTUPLE TRIM TEXTLOADER TUPLESIZE UCFIRST UPPER UTF8STORAGECONVERTER "; 
		
		// taken from QueryLexer.g
		var pKeywords = "VOID IMPORT RETURNS DEFINE LOAD FILTER FOREACH ORDER CUBE DISTINCT COGROUP "
		+ "JOIN CROSS UNION SPLIT INTO IF OTHERWISE ALL AS BY USING INNER OUTER ONSCHEMA PARALLEL "
		+ "PARTITION GROUP AND OR NOT GENERATE FLATTEN ASC DESC IS STREAM THROUGH STORE MAPREDUCE "
		+ "SHIP CACHE INPUT OUTPUT STDERROR STDIN STDOUT LIMIT SAMPLE LEFT RIGHT FULL EQ GT LT GTE LTE " 
		+ "NEQ MATCHES TRUE FALSE "; 
		
		// data types
		var pTypes = "BOOLEAN INT LONG FLOAT DOUBLE CHARARRAY BYTEARRAY BAG TUPLE MAP ";
		
		CodeMirror.defineMIME("text/x-pig", {
		 name: "pig",
		 builtins: keywords(pBuiltins),
		 keywords: keywords(pKeywords),
		 types: keywords(pTypes)
		 });
	}());

	//============================================codemirror-2.34/lib/util/pig-hint.js=======================================================
	(function () {
		  function forEach(arr, f) {
		    for (var i = 0, e = arr.length; i < e; ++i) f(arr[i]);
		  }
		  
		  function arrayContains(arr, item) {
		    if (!Array.prototype.indexOf) {
		      var i = arr.length;
		      while (i--) {
		        if (arr[i] === item) {
		          return true;
		        }
		      }
		      return false;
		    }
		    return arr.indexOf(item) != -1;
		  }

		  function scriptHint(editor, keywords, getToken) {
		    // Find the token at the cursor
		    var cur = editor.getCursor(), token = getToken(editor, cur), tprop = token;
		    // If it's not a 'word-style' token, ignore the token.

		    if (!/^[\w$_]*$/.test(token.string)) {
		        token = tprop = {start: cur.ch, end: cur.ch, string: "", state: token.state,
		                         className: token.string == ":" ? "pig-type" : null};
		    }
		      
		    if (!context) var context = [];
		    context.push(tprop);
		    
		    var completionList = getCompletions(token, context); 
		    completionList = completionList.sort();
		    //prevent autocomplete for last word, instead show dropdown with one word
		    if(completionList.length == 1) {
		      completionList.push(" ");
		    }

		    return {list: completionList,
		              from: {line: cur.line, ch: token.start},
		              to: {line: cur.line, ch: token.end}};
		  }
		  
		  CodeMirror.pigHint = function(editor) {
		    return scriptHint(editor, pigKeywordsU, function (e, cur) {return e.getTokenAt(cur);});
		  };
		 
		 function toTitleCase(str) {
		    return str.replace(/(?:^|\s)\w/g, function(match) {
		        return match.toUpperCase();
		    });
		 }
		  
		  var pigKeywords = "VOID IMPORT RETURNS DEFINE LOAD FILTER FOREACH ORDER CUBE DISTINCT COGROUP "
		  + "JOIN CROSS UNION SPLIT INTO IF OTHERWISE ALL AS BY USING INNER OUTER ONSCHEMA PARALLEL "
		  + "PARTITION GROUP AND OR NOT GENERATE FLATTEN ASC DESC IS STREAM THROUGH STORE MAPREDUCE "
		  + "SHIP CACHE INPUT OUTPUT STDERROR STDIN STDOUT LIMIT SAMPLE LEFT RIGHT FULL EQ GT LT GTE LTE " 
		  + "NEQ MATCHES TRUE FALSE";
		// according to http://pig.apache.org/docs/r0.10.0/basic.html#Reserved-Keywords only below keywords are case insensitive. -Gary
		  var pigKeywordsL = "LOAD USING AS GROUP BY FOREACH GENERATE DUMP ".toLowerCase().split(" ");
		  var pigKeywordsU = pigKeywords.split(" ");
//		  var pigKeywordsL = pigKeywords.toLowerCase().split(" ");
		  
		  var pigTypes = "BOOLEAN INT LONG FLOAT DOUBLE CHARARRAY BYTEARRAY BAG TUPLE MAP";
		  var pigTypesU = pigTypes.split(" ");
		  var pigTypesL = pigTypes.toLowerCase().split(" ");
		  
		  var pigBuiltins = "ABS ACOS ARITY ASIN ATAN AVG BAGSIZE BINSTORAGE BLOOM BUILDBLOOM CBRT CEIL " 
		  + "CONCAT COR COS COSH COUNT COUNT_STAR COV CONSTANTSIZE CUBEDIMENSIONS DIFF DISTINCT DOUBLEABS "
		  + "DOUBLEAVG DOUBLEBASE DOUBLEMAX DOUBLEMIN DOUBLEROUND DOUBLESUM EXP FLOOR FLOATABS FLOATAVG "
		  + "FLOATMAX FLOATMIN FLOATROUND FLOATSUM GENERICINVOKER INDEXOF INTABS INTAVG INTMAX INTMIN "
		  + "INTSUM INVOKEFORDOUBLE INVOKEFORFLOAT INVOKEFORINT INVOKEFORLONG INVOKEFORSTRING INVOKER "
		  + "ISEMPTY JSONLOADER JSONMETADATA JSONSTORAGE LAST_INDEX_OF LCFIRST LOG LOG10 LOWER LONGABS "
		  + "LONGAVG LONGMAX LONGMIN LONGSUM MAX MIN MAPSIZE MONITOREDUDF NONDETERMINISTIC OUTPUTSCHEMA  "
		  + "PIGSTORAGE PIGSTREAMING RANDOM REGEX_EXTRACT REGEX_EXTRACT_ALL REPLACE ROUND SIN SINH SIZE "
		  + "SQRT STRSPLIT SUBSTRING SUM STRINGCONCAT STRINGMAX STRINGMIN STRINGSIZE TAN TANH TOBAG "
		  + "TOKENIZE TOMAP TOP TOTUPLE TRIM TEXTLOADER TUPLESIZE UCFIRST UPPER UTF8STORAGECONVERTER";  
		  var pigBuiltinsU = pigBuiltins.split(" ").join("() ").split(" ");  
//		  var pigBuiltinsL = pigBuiltins.toLowerCase().split(" ").join("() ").split(" ");// all of builtins are case sensitive. -Gary
		  var pigBuiltinsC = ("BagSize BinStorage Bloom BuildBloom ConstantSize CubeDimensions DoubleAbs "
		  + "DoubleAvg DoubleBase DoubleMax DoubleMin DoubleRound DoubleSum FloatAbs FloatAvg FloatMax "
		  + "FloatMin FloatRound FloatSum GenericInvoker IntAbs IntAvg IntMax IntMin IntSum "
		  + "InvokeForDouble InvokeForFloat InvokeForInt InvokeForLong InvokeForString Invoker "
		  + "IsEmpty JsonLoader JsonMetadata JsonStorage LongAbs LongAvg LongMax LongMin LongSum MapSize "
		  + "MonitoredUDF Nondeterministic OutputSchema PigStorage PigStreaming StringConcat StringMax "
		  + "StringMin StringSize TextLoader TupleSize Utf8StorageConverter").split(" ").join("() ").split(" ");
		                    
		  function getCompletions(token, context) {
		    var found = [], start = token.string;
		    function maybeAdd(str) {
		      if (str.indexOf(start) == 0 && !arrayContains(found, str)) found.push(str);
		    }
		    
		    function gatherCompletions(obj) {
		      if(obj == ":") {
		        forEach(pigTypesL, maybeAdd);
		      }
		      else {
		        forEach(pigBuiltinsU, maybeAdd);
//		        forEach(pigBuiltinsL, maybeAdd);// to prevent lowercase appear in completion list. -Gary
		        forEach(pigBuiltinsC, maybeAdd);
		        forEach(pigTypesU, maybeAdd);
		        forEach(pigTypesL, maybeAdd);
		        forEach(pigKeywordsU, maybeAdd);
		        forEach(pigKeywordsL, maybeAdd);
		      }
		    }

		    if (context) {
		      // If this is a property, see if it belongs to some object we can
		      // find in the current environment.
		      var obj = context.pop(), base;

		      if (obj.className == "pig-word") 
		          base = obj.string;
		      else if(obj.className == "pig-type")
		          base = ":" + obj.string;
		        
		      while (base != null && context.length)
		        base = base[context.pop().string];
		      if (base != null) gatherCompletions(base);
		    }
		    return found;
		  }
		})();
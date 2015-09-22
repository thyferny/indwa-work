/**
 * User: sasher
 * Date: 10/18/12
 * Time: 4:25 PM
 */
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

    function sqlscriptHint(editor, keywords, getToken) {
        console.log("get the sql script hints....");
        // Find the token at the cursor
        var cur = editor.getCursor(), token = getToken(editor, cur), tprop = token;
        // If it's not a 'word-style' token, ignore the token.

        if (!/^[\w$_]*$/.test(token.string)) {
            token = tprop = {start: cur.ch, end: cur.ch, string: "", state: token.state,
                className: null};
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

    CodeMirror.plsqlHint = function(editor) {
        console.log("in CodeMirror.plsqlHint")
        return sqlscriptHint(editor, plsqlKeywordsU, function (e, cur) {return e.getTokenAt(cur);});
    };

    function toTitleCase(str) {
        return str.replace(/(?:^|\s)\w/g, function(match) {
            return match.toUpperCase();
        });
    }

    var plsqlKeywords = "abort accept access add all alter and any array arraylen as asc assert assign at attributes audit " +
        "authorization avg " +
        "base_table begin between binary_integer body boolean by " +
        "case cast char char_base check close cluster clusters colauth column comment commit compress connect " +
        "connected constant constraint crash create current currval cursor " +
        "data_base database date dba deallocate debugoff debugon decimal declare default definition delay delete " +
        "desc digits dispose distinct do drop " +
        "else elsif enable end entry escape exception exception_init exchange exclusive exists exit external " +
        "fast fetch file for force form from function " +
        "generic goto grant group " +
        "having " +
        "identified if immediate in increment index indexes indicator initial initrans insert interface intersect " +
        "into is " +
        "key " +
        "level library like limited local lock log logging long loop " +
        "master maxextents maxtrans member minextents minus mislabel mode modify multiset " +
        "new next no noaudit nocompress nologging noparallel not nowait number_base " +
        "object of off offline on online only open option or order out " +
        "package parallel partition pctfree pctincrease pctused pls_integer positive positiven pragma primary prior " +
        "private privileges procedure public " +
        "raise range raw read rebuild record ref references refresh release rename replace resource restrict return " +
        "returning reverse revoke rollback row rowid rowlabel rownum rows run " +
        "savepoint schema segment select separate session set share snapshot some space split sql start statement " +
        "storage subtype successful synonym " +
        "tabauth table tables tablespace task terminate then to trigger truncate type " +
        "union unique unlimited unrecoverable unusable update use using " +
        "validate value values variable view views " +
        "when whenever where while with work";
    var plsqlKeywordsL = plsqlKeywords.split(" ");
    var plsqlKeywordsU = plsqlKeywords.toUpperCase().split(" ");

    var plsqlTypes ="bfile blob " +
        "character clob " +
        "dec " +
        "float " +
        "int integer " +
        "mlslabel " +
        "natural naturaln nchar nclob number numeric nvarchar2 " +
        "real rowtype " +
        "signtype smallint string " +
        "varchar varchar2";

    var plsqlTypesU = plsqlTypes.toUpperCase().split(" ");
    var plsqlTypesL = plsqlTypes.split(" ");

    var plsqlFunctions = "abs acos add_months ascii asin atan atan2 average " +
        "bfilename " +
        "ceil chartorowid chr concat convert cos cosh count " +
        "decode deref dual dump dup_val_on_index " +
        "empty error exp " +
        "false floor found " +
        "glb greatest " +
        "hextoraw " +
        "initcap instr instrb isopen " +
        "last_day least lenght lenghtb ln lower lpad ltrim lub " +
        "make_ref max min mod months_between " +
        "new_time next_day nextval nls_charset_decl_len nls_charset_id nls_charset_name nls_initcap nls_lower " +
        "nls_sort nls_upper nlssort no_data_found notfound null nvl " +
        "others " +
        "power " +
        "rawtohex reftohex round rowcount rowidtochar rpad rtrim " +
        "sign sin sinh soundex sqlcode sqlerrm sqrt stddev substr substrb sum sysdate " +
        "tan tanh to_char to_date to_label to_multi_byte to_number to_single_byte translate true trunc " +
        "uid upper user userenv " +
        "variance vsize";

    var plsqlFunctionsU = plsqlFunctions.toUpperCase().split(" ").join("() ").split(" ");
    var plsqlFunctionsL = plsqlFunctions.split(" ").join("() ").split(" ");


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
                forEach(plsqlFunctionsU, maybeAdd);
                forEach(plsqlFunctionsL, maybeAdd);
                forEach(plsqlTypesU, maybeAdd);
                forEach(plsqlTypesL, maybeAdd);
                forEach(plsqlKeywordsU, maybeAdd);
                forEach(plsqlKeywordsL, maybeAdd);
            }
        }

        if (context) {
            // If this is a property, see if it belongs to some object we can
            // find in the current environment.
            var obj = context.pop(), base;
            base = obj.string;

            while (base != null && context.length)
                base = base[context.pop().string];
            if (base != null) gatherCompletions(base);
        }
        return found;
    }
})();

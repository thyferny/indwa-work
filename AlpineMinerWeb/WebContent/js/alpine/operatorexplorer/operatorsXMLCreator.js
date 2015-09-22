/**
 * User: robbie
 * use this to generate
 */

define([
    "alpine/operatorexplorer/OperatorUtil"
], function(opUtil) {

    var ops = alpine.operatorexplorer.OperatorUtil.OP_HASH;
    var xml, op, opTag;
    xml = "<OperatorList>";
    for (key in ops) {
        if (ops.hasOwnProperty(key)) {
            op = ops[key];
            opTag = "<Operator>" +
                "<ClassName>" + op.key + "</ClassName>" +
                "<IconName>" + op.icon + "</IconName>" +
                "</Operator>";
            xml += opTag;
        }
    }
    xml += "</OperatorList>";

    function print() {
        console.log(xml);
    }

    return {
        print: print
    };
});
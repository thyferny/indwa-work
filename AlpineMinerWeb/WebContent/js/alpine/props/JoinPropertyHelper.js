define(["dojo/dom-construct", "alpine/flow/OperatorManagementManager", "alpine/operatorexplorer/OperatorUtil",    "dojo/_base/declare"],


    function (domConstruct, operatorManager, operatorUtil, declare) {

        var joins = {
            INNER: "JOIN",
            OUTER: "FULL OUTER",
            LEFT:"LEFT OUTER",
            RIGHT:"RIGHT OUTER"
        }

        var DojoCheckboxBool = declare("alpine.props.DojoCheckboxBool", dojox.grid.cells.Bool, {
            constructor: function(args) {
                dojo.safeMixin(this,args);
            },
            uniquetag: "overwriteme",
            formatEditing:function (inDatum, inRowIndex) {
                var uid = this.uniquetag + inRowIndex;
                return '<input id="' + uid + '" type="checkbox"' + (inDatum ? ' checked="checked"' : '') + '  class="dojo-checkbox" style="width: auto" ><label for="' + uid + '"/>';

            }
        });

        function getDojoCheckbox(theUID)
        {
            var bool1 =  declare("AlpineCheckboxes", alpine.props.DojoCheckboxBool, {
                uniquetag:theUID
            });

            return bool1;
        }

        return {
            getDojoCheckbox : getDojoCheckbox,
            joins : joins
        };

});
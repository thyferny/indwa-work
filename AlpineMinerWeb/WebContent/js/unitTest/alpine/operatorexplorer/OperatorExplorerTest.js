dojo.provide("unitTest.alpine.operatorexplorer.OperatorExplorerTest");
dojo.registerModulePath("alpine.operatorexplorer.OperatorExplorerUIHelper","../../js/alpine/operatorexplorer/OperatorExplorerUIHelper")

//dojo.require("alpine.httpService");
//dojo.require("dojox.json.ref");
//baseURL= "/AlpineMinerWeb";
//connBaseURL = baseURL + "/main/dbconnection.do";
//ds = new httpService();

dojo.require("alpine.operatorexplorer.OperatorUtil");

//define(["alpine.operatorexplorer.OperatorUtil"],function(operUtil)
//{

doh.register("OperatorExplorerText", [
     function confirmHashIntegrity() {
         var theHashArray = alpine.operatorexplorer.OperatorUtil.getAllOperatorObjects();
         //SAMPLE: OP_HASH["ScatterPlotOperator"] ={ key:"ScatterPlotOperator", icon:"histogram.png", icons:"histogram_s.png", optype: OP_EXPLORATION, imgtype:IMG_TYPE_OCT };

         for (var count = 0; count < theHashArray.length; count++)
         {
             var oneOperator = theHashArray[count];
             doh.assertTrue(oneOperator.key && oneOperator.key.length > 0);
             console.log("testing key: " + oneOperator.key);
             doh.assertTrue(oneOperator.icon && oneOperator.icon.length > 0);
             doh.assertTrue( +oneOperator.optype > -2 && +oneOperator.optype < 8);
             doh.assertTrue(+oneOperator.imgtype >= 0 && +oneOperator.imgtype <= 11);
             doh.assertTrue(oneOperator.label && oneOperator.key.length > 0);
             doh.assertTrue(oneOperator.terminal === true || oneOperator.terminal === false);
             doh.assertTrue(oneOperator.showhadoop === true || oneOperator.showhadoop === false);
         }

     }


]);

//});
//doh.register("dbconnectionManagerTest", [
//    function testLoad_all_conn_tree(){
//        alpine.dbconnection.DbconnectionManager.load_all_conn_tree(
//            function(data){
//                //doh.assertEqual(data.message,"success");
//                doh.assertTrue(data['Public'].length>0);
//            },null);
//    },
//
//    function testConn_loadGroupNames(){
//        alpine.dbconnection.DbconnectionManager.conn_loadGroupNames(
//            function(data){
//                doh.assertTrue(data.length>0);
//            }
//        );
//    }
//
//]);
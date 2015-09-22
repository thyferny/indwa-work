dojo.provide("alpinetestcase.sample.SampleTestCase");
//dojo.require("alpine.test.Simple1");
doh.register("SampleTestCase", [
	function simpleTest(){
		doh.assertEqual(Math.pow(5, 3), 125);
		doh.assertTrue(123 == "123");
		doh.assertFalse(99999999 > Infinity);
	},
	function testClass(assert){
//		var test = alpine.test.Simple1.getName();
		//you can do some test to 
//		assert.assertEqual(test, "Simple1");
	}
]);
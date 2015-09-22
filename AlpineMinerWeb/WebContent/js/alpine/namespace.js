var Namespace = (function(){
	function Namespace(){};
	
	Namespace.prototype.getNamespace = function(namespace){
	    return eval(namespace);
	};
	
	Namespace.prototype.register = function(namespace){
	    var chk = false;
	    var cob = "";
	    var spc = namespace.split(".");
	    for(var i = 0; i < spc.length; i++){
	        if(cob != ""){
	        	cob += ".";
	        }
	        cob += spc[i];
	        chk = this._exists(cob);
	        if(!chk){
	        	this._create(cob);
	        }
	    }
	    if(chk){
	    	throw "Namespace: " + namespace + " is already defined.";
	    }
	    return this.getNamespace(namespace);
	};
	Namespace.prototype._create = function(namespace){
	    eval("window." + namespace + " = new Object();");
	};
	Namespace.prototype._exists = function(namespace){
	    eval("var NE = false; try{if(" + namespace + "){NE = true;}else{NE = false;}}catch(err){NE=false;}");
	    return NE;
	};
	return new Namespace();
})();

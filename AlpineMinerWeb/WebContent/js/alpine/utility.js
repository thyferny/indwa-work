var toolkit = {
	getValue: function(value){
		return dojo.isArray(value) ? value[0] : value; 
	}
}
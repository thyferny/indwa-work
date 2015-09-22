dojo.declare("DataStore",null,{
	constructor:function(){
		this.primary = [];
		this.pageNumber = 1;
		this.pageSize = 10;
		this.recordCount = -1;
		this.rowSetName = "";
		this.condition = [];
		this.result = [];
	},
	
	addProperty:function(prop){//propertyName:value
		this.primary.push(prop);
	},
	
	getPrimary:function(){
		return this.primary;
	},
	
	setPageNumber:function(num){
		this.pageNumber = num;
	},
	
	getPageNumber:function(){
		return this.pageNumber;
	},
	
	setPageSize:function(size){
		this.pageSize = size;
	},
	
	getPageSize:function(){
		return this.pageSize;
	},
	
	setRecordCount:function(count){
		this.recordCount = count;
	},
	
	setRowSetName:function(name){
		this.rowSetName = name;
	},
	
	addCondition:function(condition){
		this.condition.push(condition);
	},
	
	addResultRow:function(row){
		this.result.push(row);
	},
	
	toJson:function(){
		var result = [];
		result.push("primary:{".concat(this.primary).concat("}"));
		result.push(",pageNumber:".concat(this.pageNumber));
		result.push(",pageSize:".concat(this.pageSize));
		result.push(",recordCount:".concat(this.recordCount));
		result.push(",name:".concat(this.rowSetName));
		result.push(",condition:{".concat(this.condition.join("")).concat("}"));
		result.push(",result:[".concat(this.result.join("")).concat("]"));
		return result.join("");
	}
});
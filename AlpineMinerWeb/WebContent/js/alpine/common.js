
function handle_error_result(obj)
{		
	//0 means success
	if(!obj.error_code||obj.error_code==0) {
		return ;
	}
	if (obj.error_code == -1) {
		popupComponent.alert(alpine.nls.no_login, function(){
			window.top.location.pathname = loginURL;	
		});
	}
	else {
		var msg ="";
		if (obj.message) {
			msg = obj.message;
		}else{
			 msg=alpine.nls.flow_not_found;
		}
		popupComponent.alert(msg);
	}
}

function get_open_window_options(){
	var open_window_options="";
	if(dojo.isIE){
		open_window_options="";
	}else{
		open_window_options = "location=no,menubar=no,toolbar=no";	
	}
	return open_window_options;
}

//java script date object
function alpine_format_date(dateTime){
	return dojo.date.locale.format(dateTime,{selector:'datetime',datePattern:'yyyy-MM-dd',timePattern:'HH:mm:ss'});
}


/**
 * validateTableName
 *
 * This should match the validation as defined in AbstractOperator.validateTableName
 *
 * @param tableName
 * @param allowBlank
 * @return {Boolean}
 */
function validateTableName(tableName, allowBlank)
{
    if (tableName.trim().length==0) return allowBlank;
    var patrn = /^[a-zA-Z_@]*$/;  //This filters to make sure we have no invalid characters.
    return patrn.test(tableName.substring(0,1));

}



function RemoveArrayElement(array,element)
{
	if(!array){
		return ;
	}
    for(var i=0,n=0;i<array.length;i++)
    {
        if(array[i]!=element)
        {
            array[n++]=array[i];
        }
    }
    array.length -= 1;
}



function purge(d) {
    var a = d.attributes, i, l, n;
    if (a) {
        for (i = a.length - 1; i >= 0; i -= 1) {
            n = a[i].name;
            if (typeof d[n] === 'function') {
                d[n] = null;
            }
        }
    }
    a = d.childNodes;
    if (a) {
        l = a.length;
        for (i = 0; i < l; i += 1) {
            purge(d.childNodes[i]);
        }
    }
}	

/**
 * COPYRIGHT 2012 (c) Alpine Data Labs. All Rights Reserved.    
 *
 * security_config.js
 * 
 * Author sam_zang
 * Version 3.0
 * Date Jan 11, 2012
 */


define([],function()
{



	function showSecurityDialog() {
		load_security_config();
	}

	var login = alpine.USER;
	var adminBaseURL = baseURL + "/main/admin.do";

	function load_security_config() {
		var url = adminBaseURL + "?method=loadSecurityConfig";		 	
		ds.get(url, generate_auth_select_list);
	}

	function save_security_config() {
		var url = adminBaseURL + "?method=updateSecurityConfig";		 	
		var data = get_security_config_date();
		if (data != null) {
			ds.post(url, data, auth_update_callback);
		}
	}

	function test_security_config() {
		var jarFileName = dojo.byId("sec_custom_jarFile").value;
		var value = get_auth_type_value();
		if (jarFileName && value == "CustomProvider") {
			upload_jar_file();
		}
		else {
			run_test_security_config();
		}
	}

	function upload_jar_file() {
		var url = adminBaseURL + "?method=uploadCustomJar";
		ds.upload(url, "customForm", run_test_security_config, null);
	}
	
	function run_test_security_config() {
		var url = adminBaseURL + "?method=testSecurityConfig";
		var data = get_security_config_date();
		if (data != null) {
			ds.post(url, data, auth_test_callback);
		}
	}
	function get_security_config_date() {
		var data = {};
		var value = get_auth_type_value();
		data.current_choice = value;
		switch(value) {
		case "LocalProvider":
			break;
		case "LDAPProvider":
			if (ldapConfigForm.validate() == false) {
				popupComponent.alert(alpine.nls.ConnectionValid);
				return null;
			}
			data.ldapCfg = ldapConfigForm.getValues();
			break;
		case "ADProvider":
			if (adConfigForm.validate() == false) {
				popupComponent.alert(alpine.nls.ConnectionValid);
				return null;
			}
			data.adCfg = adConfigForm.getValues();
			break;
		case "CustomProvider":
			if (customForm.validate() == false) {
				popupComponent.alert(alpine.nls.ConnectionValid);
				return null;
			}
			data.customCfg = customForm.getValues();
			break;	
		}
		return data;
	}
	
	var Auth_choice_prop = null;
//	var security_handlers = null;
	function generate_auth_select_list(obj) {
		if (obj.error_code) {
			handle_error_result(nameList);
			return;
		}

//		var id = "auth_provider_radio_button_table";
//		var ID_TAG = "__prop_form_value__";	
		
//		var parent = dojo.byId(id);
		Auth_choice_prop = {};
//		Auth_choice_prop.name = id;
		Auth_choice_prop.value = obj.current_choice;
//		Auth_choice_prop.displayName = id;
		Auth_choice_prop.fullSelection = obj.choice_list;
//		generate_input_choice(parent, Auth_choice_prop);

//		security_handlers = new Array();
//		var list = Auth_choice_prop.fullSelection;
//		for (j = 0; j < list.length; j++) {
//			var btn = get_auth_type_button(list[j]);
//			var handler = dojo.connect(btn, "onClick", function(value) {
//				check_auth_mode();
//			});
//			security_handlers.push(handler);
//		}	


		dijit.byId("auth_provider_radio_button_table_" + Auth_choice_prop.value).set("checked", true);
		ldapConfigForm.setValues(obj.ldapCfg);
		adConfigForm.setValues(obj.adCfg);
		customForm.setValues(obj.customCfg);
		dijit.byId("security_config").titleBar.style.display = "none";
		dijit.byId("security_config").show();
		
		check_sec_cfg_auth_mode();
	}

	function auth_update_callback(obj) {
		close_security_config();
		alpine.auth_type = obj.current_choice;
	}

	function auth_test_callback(obj) {
		var msg = get_test_message(obj);
		popupComponent.alert(msg);
	}
	
	function get_test_message(obj) {
		if (obj.connection == false) {
			return obj.message;
		}
		
		var msg = alpine.nls.LDAP_TEST_CONNECT;
		msg = msg.replace("{user}", obj.userCount);
		msg = msg.replace("{group}", obj.groupCount);
//		msg += "Connection passed.\n";
//		msg += "find " + obj.userCount + " users.\n";
//		msg += "find " + obj.groupCount + " groups.\n";
		return msg;
	}

	function check_sec_cfg_auth_mode() {
		disable_ldap_controls(true);
		disable_custom_controls(true);
		disable_ad_controls(true);
		
		var value = get_auth_type_value();
		var id = null;
		switch(value) {
		case "LocalProvider":
			break;
		case "LDAPProvider":
			id = "LDAPProvider_tab";
			disable_ldap_controls(false);			
			break;
		case "ADProvider":
			id = "ADProvider_tab";
			disable_ad_controls(false);			
			break;
		case "CustomProvider":
			id = "CustomProvider_tab";
			disable_custom_controls(false);
			break;	
		}	
		if (id) {
			var tab = dijit.byId(value + "_tab");
			dijit.byId("sec_cfg_tab_container").selectChild(tab);
		}

	}

	function get_auth_type_value() {
		var value = "";
		var list = Auth_choice_prop.fullSelection;
		for (j = 0; j < list.length; j++) {
			var btn = get_auth_type_button(list[j]);
			if (btn.checked == true) {
				value = btn.value;
				break;
			}
		}	
		return value;
	}

	function get_auth_type_button(name) {
//		var ID_TAG = "__prop_form_value__";
		var id = "auth_provider_radio_button_table_";

		var button_id = id + name;
		var btn = dojo.byId(button_id);
		return btn;
	}

	function disable_ldap_controls(flag) {
		var idx = 1;
		var base_id = "sec_cfg_ldap_";
		while(idx < 19) {
			dijit.byId(base_id + idx).set("disabled", flag);
			idx ++;
		}
		dijit.byId("ldapConfigForm").set("disabled", flag);
	}

	function disable_ad_controls(flag) {
		var idx = 1;
		var base_id = "sec_cfg_AD_";
		while(idx < 7) {
			dijit.byId(base_id + idx).set("disabled", flag);
			idx ++;
		}
		dijit.byId("adConfigForm").set("disabled", flag);
	}
	
	function disable_custom_controls(flag) {
		dijit.byId("customForm").set("disabled", flag);
		dojo.byId("sec_custom_jarFile").disabled = flag;
		dijit.byId("sec_custom_className").set("disabled", flag);
		dijit.byId("sec_custom_test_button").set("disabled", flag);
	}
	function close_security_config() {
		dijit.byId('security_config').hide();
		Auth_choice_prop = null;
//		if (security_handlers) {
//			for ( var i = 0; i < security_handlers.length; i++) {
//				if (security_handlers[i]) {
//					dojo.disconnect(security_handlers[i]);
//				}
//			}
//			security_handlers = null;
//		}
	}

    return {
        check_sec_cfg_auth_mode: check_sec_cfg_auth_mode,
        test_security_config:test_security_config,
        close_security_config:close_security_config,
        save_security_config:save_security_config,
        showSecurityDialog:showSecurityDialog
    }

});
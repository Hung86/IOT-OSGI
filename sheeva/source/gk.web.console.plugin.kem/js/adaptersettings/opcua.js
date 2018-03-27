var timer_scanning;

var ip_address_format = /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/;
$(document).ready(function() {
    $.get(pluginRoot, {'id':'settings','adapter':cur_adapter}, function(data) {
		renderAdapterSettings(data);
	},'json');
		
	$(document).on("change","input:text",function(){
		var inputSet = document.getElementsByTagName("input");
		for (var j = 0; j < inputSet.length; j++) {
			inputSet[j].style.border = "none";
		}
		//check unique
		//checkUniqueAddress(device_table);
		
		// if (this.name === _device.address) {
			// //check ip format
			// if (!(ip_address_format.test(this.value))) {
				// this.style.border = "1px solid red";
				// alert("You have entered an invalid IP address!");
			// }
		// }
		//
		if ((this.name === _device.category) ||
			(this.name === _device.instanceid)) {
			if (isNaN(this.value)) {
				this.style.border = "1px solid red";
				alert("You have entered an invalid number");
			}
		}
		debugger
	});
	
});

function renderAdapterSettings(data) {
	showStatusForm();
	adapterSettingsData = data;
	debugger;
	$("#titleform").html("Adapter Settings - " + convertAdapterName2UserName(cur_adapter));
    $.each(data.settings, function(key, value) {
	    console.log("---pair value : key =  "+ key + " - value = " + value);
		$("#"+key).val(value);
    });
	buildDeviceTableHTML(data);
}

//need to change , it depend on each adapter
function setAdapterDefaults() { //GK need to modify
	//$("#port").val("47808");
	$("#query_timeout").val("2000");
}

function buildDeviceRowHTML (table_id, row_id, data) {
	var table = document.getElementById(table_id);
		
	var name_text = document.createElement("INPUT");
		name_text.type = "text";
		name_text.name = _device.name;
		name_text.id = _device.name + "_" + row_id;
		name_text.className = "inputText ui-corner-all";
		name_text.style.width = "50%";
		name_text.value = data[_device.name];
	var category_text = document.createElement("INPUT");
		category_text.type = "text";
		category_text.name = _device.category;
		category_text.id = _device.category + "_" + row_id;
		category_text.className = "inputText ui-corner-all";
		category_text.style.width = "50%";
		category_text.value = data[_device.category];
	var address_text = document.createElement("INPUT");
		address_text.type = "text";
		address_text.name = _device.address;
		address_text.id = _device.address + "_" + row_id;
		address_text.className = "inputText ui-corner-all";
		address_text.value = data[_device.address];
	var instanceid_text = document.createElement("INPUT");
		instanceid_text.type = "text";
		instanceid_text.name = _device.instanceid;
		instanceid_text.id = _device.instanceid + "_" + row_id;
		instanceid_text.className = "inputText ui-corner-all";
		instanceid_text.style.width = "50%";
		instanceid_text.value = data[_device.instanceid];
	var security_text = document.createElement("INPUT");
		security_text.type = "text";
		security_text.name = _device.security;
		security_text.id = _device.security + "_" + row_id;
		security_text.className = "inputText ui-corner-all";
		security_text.style.width = "50%";
		security_text.value = data[_device.security];
	var user_text = document.createElement("button");
		user_text.type = "button";
		// user_text.name = _device.user;
		// user_text.id = _device.user + "_" + row_id;
		user_text.className = "formbtn ui-state-default ui-corner-all";
		user_text.style.width = "30%";
		user_text.innerHTML = "Upload ";
		user_text.onchange = function(){
			debugger
			
		}
		user_text.onclick = function(){
			let form = document.getElementById("uploadform");
			form.action = "AdapterSettings/opc_security_file?index="+row_id;	
			$("#file").trigger("click");
		}
		// user_text.value = data[_device.user];
	// var pass_text = document.createElement("INPUT");
		// pass_text.type = "text";
		// pass_text.name = _device.pass;
		// pass_text.id = _device.pass + "_" + row_id;
		// pass_text.className = "inputText ui-corner-all";
		// pass_text.style.width = "50%";
		// pass_text.value = data[_device.pass];		
	var delButton = document.createElement('BUTTON');
		delButton.className = "ui-state-default ui-corner-all"
		delButton.innerHTML = "<span class='ui-icon ui-icon-trash'> </span>";
		delButton.type = "button";
		delButton.onclick = deleteDeviceItem;
		
	var row = document.createElement("TR");
		row.id = row_id;
	var cell = document.createElement("TD");	
		cell.style.verticalAlign = "middle";
		cell.appendChild(name_text);
		row.appendChild(cell);
				
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";		
		cell.appendChild(category_text);
		row.appendChild(cell);
				
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";
		cell.appendChild(address_text);
		row.appendChild(cell);
				
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";
		cell.appendChild(instanceid_text);
		row.appendChild(cell);
				
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";
		cell.appendChild(security_text);
		row.appendChild(cell);
		
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";
		cell.appendChild(user_text);
		row.appendChild(cell);
		
		// cell = document.createElement("TD");
		// cell.style.verticalAlign = "middle";
		// cell.appendChild(pass_text);
		// row.appendChild(cell);
					
		cell = document.createElement("TD");	
		cell.style.verticalAlign = "middle";
		cell.appendChild(delButton);
		row.appendChild(cell);
			
		table.appendChild(row);	
}

function buildDeviceTableHTML(data) {
	var table = document.createElement("TABLE");
		table.style.width="100%";
		table.className = "nicetable ui-widget";
		table.id = device_table;
	var thead = document.createElement("THEAD");
	var th = document.createElement("TH");
		th.innerHTML = "Device Name"
		th.className = "ui-widget-header";
		th.style.width = "9%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Category"
		th.className = "ui-widget-header";
		th.style.width = "9%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Endpoint URL"
		th.className = "ui-widget-header";
		th.style.width = "25%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Instance Id"
		th.className = "ui-widget-header";
		th.style.width = "9%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Security"
		th.className = "ui-widget-header";
		th.style.width = "25%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = ""
		th.className = "ui-widget-header";
		th.style.width = "9%";
		thead.appendChild(th);
		
		// th = document.createElement("TH");
		// th.innerHTML = "Password"
		// th.className = "ui-widget-header";
		// th.style.width = "9%";
		// thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Select"
		th.className = "ui-widget-header";
		th.style.width = "5%";
		thead.appendChild(th);
		
		table.appendChild(thead);
		$("#adaptersettingsformtable").after(table);
		
		if (typeof data.devices !== "undefined" &&  data.devices !== null) {
			addedDeviceTotal = data.devices.length;
			for (var i = 0; i < data.devices.length ; i++) {
				buildDeviceRowHTML(device_table, i, data.devices[i]);
			}
		} else {
			addedDeviceTotal = 0;
		}
		$("#device_num").text(addedDeviceTotal);
}

function buildScanningDeviceTableHTML() {
	let div = document.createElement("DIV");
	div.style.width="100%";
	div.id = "div_discovery";
	let table = document.createElement("TABLE");
	table.style.width="100%";
	table.id = "device_discovery";
	table.className = "nicetable ui-widget";
	let thead = document.createElement("THEAD");
	let th = document.createElement("TH");
	th.innerHTML = "Device Discovery";
	th.colSpan = "4";
	th.className = "ui-widget-header";
	thead.appendChild(th);
	table.appendChild(thead);
	
	let row = document.createElement("TR");
	let cell = document.createElement("TH");
	cell.innerHTML = "<b>Select</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Category</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Endpoint URL</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Security policy</b>";
	row.appendChild(cell);
	
	table.appendChild(row);
	var okButton = document.createElement('BUTTON'); 
		okButton.innerHTML = 'Ok';
		okButton.type = "button";
		okButton.className = "ui-state-default ui-corner-all disabledmouse";
		okButton.style.width = "4em";
		okButton.onclick=function(event) {
			var savedDeviceList=[];
			$("input:checkbox[name=device_checkbox]:checked").each(function(){
				let rows = this.closest('tr');
				let labels = rows.getElementsByTagName("label");
				var deviceAttributes={};
				deviceAttributes[_device.category] = labels[0].innerHTML;
				deviceAttributes[_device.address] = labels[1].innerHTML;
				deviceAttributes[_device.security] = labels[2].innerHTML;
				savedDeviceList.push(deviceAttributes);
				debugger;
			});
			
			var send_to_server = {};
			send_to_server["adapter"] =  cur_adapter;
			send_to_server["device_list"] = JSON.stringify(savedDeviceList)
			submitdata("save_devices_discovery", send_to_server);
		}
	var cancelButton = document.createElement('BUTTON');
		cancelButton.innerHTML = 'Cancel';	
		cancelButton.type = "button";
		cancelButton.className = "ui-state-default ui-corner-all disabledmouse";
		cancelButton.style.width = "4em";
		cancelButton.onclick=function(event) {
			$("#div_discovery").remove();
			$("#functionalbuttons").show();
			$("#mainbuttons").removeClass("disabledmouse");
			$("#menuleftcontent").removeClass("disabledmouse");
			$("#navmenu").removeClass("disabledmouse");
			$("#adaptersettingsformtable").removeClass("disabledmouse");
			$("#" + device_table).removeClass("disabledmouse");
		}
	var stopScanningButton = document.createElement('BUTTON');
		stopScanningButton.id = "stop_scanning";
		stopScanningButton.innerHTML = 'Stop';	
		stopScanningButton.type = "button";
		stopScanningButton.className = "ui-state-default ui-corner-all";
		stopScanningButton.style.width = "4em";
		stopScanningButton.onclick=function(event) {
			okButton.className = "ui-state-default ui-corner-all";
			cancelButton.className = "ui-state-default ui-corner-all";
			stopScanningButton.className = "ui-state-default ui-corner-all disabledmouse";
			stopScanningDevices();
		}

	div.appendChild(table);
	div.appendChild(stopScanningButton);
	div.appendChild(okButton);
	div.appendChild(cancelButton);
	$("#functionalbuttons").before(div);	
	
}

function buildScanningDeviceRowHTML(data) {
	let table = document.getElementById("device_discovery");
	if (typeof table !== "undefined" &&  table !== null) {
		for( let idx = 0;  idx < data.length; idx++) {
			let checkbox = document.createElement('INPUT'); 
				checkbox.type= 'checkbox';
				checkbox.name = 'device_checkbox';
			let categorylabel = document.createElement("LABEL"); 
			categorylabel.innerHTML=data[idx].device_category;
			let addresslabel = document.createElement("LABEL"); 
			addresslabel.innerHTML=data[idx].device_address;
			let securitylabel = document.createElement("LABEL"); 
			securitylabel.innerHTML=data[idx].device_security_policy;
			
			let row = document.createElement("TR");
			let cell = document.createElement("TD");
			cell.appendChild(checkbox);
			row.appendChild(cell);
			

			
			cell = document.createElement("TD");
			cell.appendChild(categorylabel);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(addresslabel);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(securitylabel);
			row.appendChild(cell);
			
			table.appendChild(row);
		}
	}
}

function scanDeviceOfAdapter(){
	buildScanningDeviceTableHTML();
	
	$("#functionalbuttons").hide();
	$("#mainbuttons").addClass("disabledmouse");
	$("#menuleftcontent").addClass("disabledmouse");
	$("#navmenu").addClass("disabledmouse");
	$("#adaptersettingsformtable").addClass("disabledmouse");
	$("#" + device_table).addClass("disabledmouse");
	
	$("#scanning_wait").show();
	//timer_scanning = setInterval(function(){
	var request = $.ajax({
			url: pluginRoot,
			data : { 'id': 'scanning', 'scanning' : '1' },
			timeout: 60000, //in milliseconds
			dataType : "json",
			statusCode: {
				400: function() {
					},
				404: function() {
					location.replace("");//clear the page content
				},
				408: function() {

				}
			}
		});
	request.done(function( data ) {
		console.log("----data = " + data);
		buildScanningDeviceRowHTML(data);
		$("#stop_scanning").click();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
	});
	//    	},5000);

}

function stopScanningDevices(){
	// debugger;
	// clearInterval(timer_scanning);
	 $("#scanning_wait").hide();
	// var request = $.ajax({
				// url: pluginRoot,
				// data : { 'id': 'scanning', 'scanning' : '0' },
				// timeout: 30000, //in milliseconds
				// dataType : "json",
				// statusCode: {
					// 400: function() {
						// },
					// 404: function() {
						// location.replace("");//clear the page content
					// },
					// 408: function() {

					// }
				// }
			// });
			// request.done(function( data ) {
			// });
			// request.fail(function( jqXHR, textStatus, errorThrown  ) {
			// });
}

function saveAdapterSettings() {
//	var result = checkDataValidations();
//	if (result.length > 0) {
//		showErrorForm(result);
//		return 0;
//	}
		
	$.each(adapterSettingsData.settings, function(key, value) {
		if ($("#" + key).val() != value) {
			adapterSettingsData.settings[key] = $("#" + key).val();
		}
	});
	var table = document.getElementById(device_table);
	for (var i = 0; i < table.rows.length; ++i) {
		var row = table.rows[i];
		if (parseInt(row.id) >= adapterSettingsData.devices.length) {
			var device_item = {};
			for (var j = 0; j < row.cells.length; j++) {
				device_item[row.cells[j].firstChild.name] = row.cells[j].firstChild.value;
			}
			inserted_device.push(device_item);
		} else {
			for (var j = 0; j < row.cells.length; j++) {
				var cell_name = row.cells[j].firstChild.name;
				var cell_value = row.cells[j].firstChild.value;
				if ( cell_value != adapterSettingsData.devices[i][cell_name]) {
					if (row.cells[j].firstChild.type === "text") {
						adapterSettingsData.devices[i][cell_name] = cell_value;
					}
				}

			}
			updated_device.push(adapterSettingsData.devices[i]);
		}
	}
	debugger;
	var data_send_to_server = {};
	data_send_to_server["adapter"] = cur_adapter;
	var updated_setting_list = [];
	var setting_item = {};
		setting_item["function"] = "updateAdapterSettings";
		setting_item["data"] = adapterSettingsData.settings;
		updated_setting_list.push(setting_item);
		
	if (deleted_device.length > 0) {	
		var deleting_item = {};
		deleting_item["function"] = "deleteDeviceList";
		deleting_item["data"] = deleted_device;
		updated_setting_list.push(deleting_item);
	}
		
	if (updated_device.length > 0) {
		var updating_item = {};
		updating_item["function"] = "updateDeviceList";
		updating_item["data"] = updated_device;
		updated_setting_list.push(updating_item);
	}
	
	if (inserted_device.length > 0) {
		var inserting_item = {};
		inserting_item["function"] = "insertDeviceList";
		inserting_item["data"] = inserted_device;
		updated_setting_list.push(inserting_item);
	}
	data_send_to_server["data"] = JSON.stringify(updated_setting_list);
	
	// data_send_to_server["general_settings"] = JSON.stringify(adapterSettingsData.settings);
	// data_send_to_server["device_deleting"] = JSON.stringify(deleted_device); 
	// data_send_to_server["device_updating"] = JSON.stringify(updated_device);
	// data_send_to_server["device_inserting"] = JSON.stringify(inserted_device);
	// debugger
	submitdata("update_adapter_settings", data_send_to_server);
	return 1;
}

function resetDefaultAdapterSettings() {

}


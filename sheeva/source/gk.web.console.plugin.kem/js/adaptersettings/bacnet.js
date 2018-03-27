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
		checkUniqueAddress(device_table);
		
		if (this.name === _device.address) {
			//check ip format
			if (!(ip_address_format.test(this.value))) {
				this.style.border = "1px solid red";
				alert("You have entered an invalid IP address!");
			}
		}
		//
		if ((this.name === _device.category) ||
			(this.name === _device.instanceid) ||
			(this.name === _device.alternativeid)) {
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
	$("#titleform").html("Adapter Settings - " + convertAdapterName2UserName(cur_adapter));
    $.each(data.settings, function(key, value) {
	    console.log("---pair value : key =  "+ key + " - value = " + value);
		$("#"+key).val(value);
    });
	buildDeviceTableHTML(data);
}

//need to change , it depend on each adapter
function setAdapterDefaults() { //GK need to modify
	$("#port").val("47808");
	$("#query_timeout").val("2000");
}

function buildDeviceRowHTML (table_id, row_id, data) {
	var table = document.getElementById(table_id);
		
	var name_text = document.createElement("INPUT");
		name_text.type = "text";
		name_text.name = _device.name;
		name_text.id = _device.name + "_" + row_id;
		name_text.className = "inputText ui-corner-all";
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
		
	var alternativeid_text = document.createElement("INPUT");
		alternativeid_text.type = "text";
		alternativeid_text.name = _device.alternativeid;
		alternativeid_text.id = _device.alternativeid + "_" + row_id;
		alternativeid_text.className = "inputText ui-corner-all";
		alternativeid_text.style.width = "50%";
		alternativeid_text.value = data[_device.alternativeid];	
		
	var networknum_text = document.createElement("INPUT");
		networknum_text.type = "text";
		networknum_text.name = _device.netnum;
		networknum_text.id = _device.netnum + "_" + row_id;
		networknum_text.className = "inputText ui-corner-all";
		networknum_text.style.width = "50%";
		networknum_text.value = data[_device.netnum];
		
	var networkaddr_text = document.createElement("INPUT");
		networkaddr_text.type = "text";
		networkaddr_text.name = _device.netaddr;
		networkaddr_text.id = _device.netaddr + "_" + row_id;
		networkaddr_text.className = "inputText ui-corner-all";
		networkaddr_text.value = data[_device.netaddr];
		
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
		cell.appendChild(alternativeid_text);
		row.appendChild(cell);
		
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";
		cell.appendChild(networknum_text);
		row.appendChild(cell);
		
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";
		cell.appendChild(networkaddr_text);
		row.appendChild(cell);
					
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
		th.style.width = "13%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Category"
		th.className = "ui-widget-header";
		th.style.width = "13%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Address"
		th.className = "ui-widget-header";
		th.style.width = "13%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Instance Id"
		th.className = "ui-widget-header";
		th.style.width = "13%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Delegate Id"
		th.className = "ui-widget-header";
		th.style.width = "13%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Network Number"
		th.className = "ui-widget-header";
		th.style.width = "13%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Network Address"
		th.className = "ui-widget-header";
		th.style.width = "13%";
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Select"
		th.className = "ui-widget-header";
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
	th.colSpan = "7";
	th.className = "ui-widget-header";
	thead.appendChild(th);
	table.appendChild(thead);
	
	let row = document.createElement("TR");
	let cell = document.createElement("TH");
	cell.innerHTML = "<b>Select</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Device Name</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Category</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Address</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Instance Id</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Network Address</b>";
	row.appendChild(cell);
	
	cell = document.createElement("TH");
	cell.innerHTML = "<b>Network Number</b>";
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
				deviceAttributes[_device.name] = labels[0].innerHTML;
				deviceAttributes[_device.category] = labels[1].innerHTML;
				deviceAttributes[_device.address] = labels[2].innerHTML;
				deviceAttributes[_device.instanceid] = labels[3].innerHTML;
				deviceAttributes[_device.alternativeid] = "-1";
				deviceAttributes[_device.netaddr] = labels[4].innerHTML;
				deviceAttributes[_device.netnum] = labels[5].innerHTML;
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
			let namelabel = document.createElement("LABEL"); 
			namelabel.innerHTML=data[idx].device_name;
			let categorylabel = document.createElement("LABEL"); 
			categorylabel.innerHTML=data[idx].device_category;
			let addresslabel = document.createElement("LABEL"); 
			addresslabel.innerHTML=data[idx].device_address;
			let instanceidlabel = document.createElement("LABEL"); 
			instanceidlabel.innerHTML=data[idx].device_instanceid;
			let networkAddresslabel = document.createElement("LABEL"); 
			networkAddresslabel.innerHTML=data[idx].device_network_address;
			let networkNumberLabel = document.createElement("LABEL"); 
			networkNumberLabel.innerHTML=data[idx].device_network_number;
			
			let row = document.createElement("TR");
			let cell = document.createElement("TD");
			cell.appendChild(checkbox);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(namelabel);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(categorylabel);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(addresslabel);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(instanceidlabel);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(networkAddresslabel);
			row.appendChild(cell);
			
			cell = document.createElement("TD");
			cell.appendChild(networkNumberLabel);
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
	timer_scanning = setInterval(function(){
			var request = $.ajax({
					url: pluginRoot,
					data : { 'id': 'scanning', 'scanning' : '1' },
					timeout: 3000, //in milliseconds
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
					console.log("----------------data = " + data);
					buildScanningDeviceRowHTML(data);
				});
				request.fail(function( jqXHR, textStatus, errorThrown  ) {
				});
	    	},5000);

}

function stopScanningDevices(){
	debugger;
	clearInterval(timer_scanning);
	$("#scanning_wait").hide();
	var request = $.ajax({
				url: pluginRoot,
				data : { 'id': 'scanning', 'scanning' : '0' },
				timeout: 3000, //in milliseconds
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
			});
			request.fail(function( jqXHR, textStatus, errorThrown  ) {
			});
}

function saveAdapterSettings() {
	var result = checkDataValidations();
	if (result.length > 0) {
		showErrorForm(result);
		return 0;
	}
		
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
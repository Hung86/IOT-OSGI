var hasVersionCol = false;
window.onload =  entryScript();

function entryScript() {
    $.get(pluginRoot, {'id':'settings','adapter':cur_adapter}, function(data) {
		renderAdapterSettings(data);
	},'json');
	
    $("#scanbutton").hide();
    
	$('button').mouseover(function(){
		$(this).removeClass('ui-state-default').addClass('ui-state-hover');
	});

	$('button').mouseout(function(){
		$(this).removeClass('ui-state-hover').addClass('ui-state-default');
	});
}


function renderAdapterSettings(data) {
	showStatusForm();
	adapterSettingsData = data;
	$("#titleform").html("Adapter Settings - " + convertAdapterName2UserName(cur_adapter));
    $.each(data.settings, function(key, value) {
	    console.log("---pair value : key =  "+ key + " - value = " + value);
		$("#"+key).val(value);
    });
	$("#protocol").trigger("change");
	buildDeviceTableHTML(data);
	debugger;
}

function buildDeviceTableHTML(data) {
	let width  = "30%";
	if ((cur_adapter == "EnergetixPowerMeterAdapter") ||
			(cur_adapter == "EnergetixSensorAdapter") ||
			(cur_adapter == "KamAdapter")) {
		width  = "22%";
		hasVersionCol = true;
	}
	var table = document.createElement("TABLE");
		table.style.width="100%";
		table.className = "nicetable ui-widget";
		table.id = device_table;
	var thead = document.createElement("THEAD");
	var th = document.createElement("TH");
		th.innerHTML = "Device Name";
		th.className = "ui-widget-header";
		th.style.width = width;
		thead.appendChild(th);
		
		th = document.createElement("TH");
		th.innerHTML = "Category"
		th.className = "ui-widget-header";
		th.style.width = width;
		thead.appendChild(th);
		
	
		th = document.createElement("TH");
		th.innerHTML = "Device Id";
		th.className = "ui-widget-header";
		th.style.width = width;
		thead.appendChild(th);
	
		
		if (hasVersionCol) {
			th = document.createElement("TH");
			th.innerHTML = "H/W version";
			th.className = "ui-widget-header";
			th.style.width = width;
			thead.appendChild(th);
		}
		
		th = document.createElement("TH");
		th.innerHTML = "Select";
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
				debugger

		$("#device_num").text(addedDeviceTotal);
}

function buildDeviceRowHTML (table_id, row_id, data) {
	var table = document.getElementById(table_id);
	var name_select = createSelectOptionList(device_type[cur_adapter]);
		name_select.name = _device.name;
		name_select.id = _device.name + "_" + row_id;
		name_select.value = data[_device.category];
		name_select.onchange = function() {
			let rows = $(this).closest('tr');
			rows[0].cells[1].firstChild.innerHTML = this.value;
			if (hasVersionCol) {
				let _version = rows[0].cells[3].firstChild;
				if (typeof _version !== "undefined" && _version !== null)  {
					_version.remove();
				} 
				if ((this.value == "1009") || (this.value == "1012") || (this.value == "2000") || (this.value == "1010")) {
					let version_select = createSelectOptionList(version_table[cur_adapter][this.value]);
					version_select.name = _device.version;
					version_select.id = _device.version + "_" + row_id;
					version_select.value =  data[_device.version];
					rows[0].cells[3].appendChild(version_select);
				}
			}
			
		};
	var category_text = document.createElement("LABEL");
		category_text.setAttribute("name",_device.category);
		category_text.id = _device.category + "_" + row_id;
		category_text.innerHTML = data[_device.category];
	var instanceid_text = document.createElement("INPUT");
		instanceid_text.type = "text";
		instanceid_text.name = _device.instanceid;
		instanceid_text.id = _device.instanceid + "_" + row_id;
		instanceid_text.className = "inputText ui-corner-all";
		instanceid_text.style.width = "50%";
		instanceid_text.value = data[_device.instanceid];
		
	var delButton = document.createElement('BUTTON');
		delButton.className = "ui-state-default ui-corner-all"
		delButton.innerHTML = "<span class='ui-icon ui-icon-trash'> </span>";
		delButton.type = "button";
		delButton.onclick = deleteDeviceItem;
		
	var row = document.createElement("TR");
		row.id = row_id;
	var cell = document.createElement("TD");	
		cell.style.verticalAlign = "middle";
		cell.appendChild(name_select);
		row.appendChild(cell);
				
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";		
		cell.appendChild(category_text);
		row.appendChild(cell);
						
		cell = document.createElement("TD");
		cell.style.verticalAlign = "middle";
		cell.appendChild(instanceid_text);
		row.appendChild(cell);
		debugger;

		if (hasVersionCol) {
			cell = document.createElement("TD");
			cell.style.verticalAlign = "middle";
			if (( data[_device.category] == "1009") || ( data[_device.category] == "1012") || ( data[_device.category] == "2000") || ( data[_device.category] == "1010")) {
				let version_select = createSelectOptionList(version_table[cur_adapter][data[_device.category]]);
				version_select.name = _device.version;
				version_select.id = _device.version + "_" + row_id;
				version_select.value = data[_device.version];
				cell.appendChild(version_select);
			}
			row.appendChild(cell);
		}
		cell = document.createElement("TD");	
		cell.style.verticalAlign = "middle";
		cell.appendChild(delButton);
		row.appendChild(cell);
			
		table.appendChild(row);	
}

function saveAdapterSettings() {
	 var result = checkDataValidations();
	 if (result.length > 0) {
		showErrorForm(result);
		 return 0;
	 }
		
	$.each(adapterSettingsData.settings, function(key, value) {
		let items = $("#" + key).val();
		if (typeof items != "undefined") {
			if (items != value) {
				adapterSettingsData.settings[key] = items;
			}
		}
	});
	
	let table = document.getElementById(device_table);
	for (let i = 0; i < table.rows.length; ++i) {
		let rowId = table.rows[i].id;
		let device_item = {};
		device_item[_device.name] = $("#" + _device.name + "_" + rowId +" option:selected").text();
		device_item[_device.category] = $("#" + _device.category + "_" + rowId).text();
		device_item[_device.instanceid] = $("#" + _device.instanceid + "_" + rowId).val();
		device_item[_device.version] = $("#" + _device.version + "_" + rowId +" option:selected").text();
		if (typeof device_item[_device.version] == "undefined") {
			device_item[_device.version] = "";
		}
		if (parseInt(rowId) >= adapterSettingsData.devices.length) {
			inserted_device.push(device_item);
		} else {
			adapterSettingsData.devices[rowId][_device.name] = device_item[_device.name];
			adapterSettingsData.devices[rowId][_device.category] = device_item[_device.category];
			adapterSettingsData.devices[rowId][_device.instanceid] = device_item[_device.instanceid];
			adapterSettingsData.devices[rowId][_device.version] = device_item[_device.version];
			updated_device.push(adapterSettingsData.devices[rowId]);
		}
	}
	let data_send_to_server = {};
	data_send_to_server["adapter"] = cur_adapter;
	let updated_setting_list = [];
	let setting_item = {};
		setting_item["function"] = "updateAdapterSettings";
		setting_item["data"] = adapterSettingsData.settings;
		updated_setting_list.push(setting_item);
		
	if (deleted_device.length > 0) {	
		let deleting_item = {};
		deleting_item["function"] = "deleteDeviceList";
		deleting_item["data"] = deleted_device;
		updated_setting_list.push(deleting_item);
	}
		
	if (updated_device.length > 0) {
		let updating_item = {};
		updating_item["function"] = "updateDeviceList";
		updating_item["data"] = updated_device;
		updated_setting_list.push(updating_item);
	}
	
	if (inserted_device.length > 0) {
		let inserting_item = {};
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
	debugger
	submitdata("update_adapter_settings", data_send_to_server);
	return 1;
}
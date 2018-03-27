var all_adapter;
var cur_adapter;
var reloadTimer;
var countDown = 0;
var shouldReloadPage = 0;
var status_update = "status_update";

var userName = {
	"GentosAdapter" : "Gentos", "gentos" : "Gentos",
	"EmersonAdapter" : "Emerson", "emerson" : "Emerson",
	"EntesAdapter" : "Entes", "entes" : "Entes", 
	"EnergetixPowerMeterAdapter" : "Energetix Power Meter", "powermeter" : "Energetix Power Meter",
	"BrainchildAdapter" : "Brainchild", "brainchild" : "Brainchild",
	"JanitzaAdapter" : "Janitza", "janitza" : "Janitza",
	"ContrecAdapter" : "Contrec", "contrec" : "Contrec",
	"RielloAdapter" : "Riello", "riello" :  "Riello",
	"EnergetixSensorAdapter" : "Energetix Sensor", "sensor" : "Energetix Sensor",
	"GeAquatransAdapter" : "GE", "aquatrans" : "GE",
	"BaylanAdapter" : "Baylan", "baylan" : "Baylan",
	"SiemensAdapter" : "Siemens", "siemens" : "Siemens",
	"DentAdapter" : "Dent", "dent" : "Dent",
	"SocomecAdapter" : "Socomec", "socomec" : "Socomec",
	"BacnetAdapter" : "Bacnet Generic", "bacnet" : "Bacnet Generic",
	"OpcuaAdapter" : "OPC Generic", "opcua" : "OPC Generic",
	"ModbusConverterAdapter" : "Modbus Converter" , "modbusconverter" : "Modbus Converter",
	"AquametroAdapter" : "Aquametro", "aquametro" : "Aquametro", 
	"KamAdapter" : "Kam", "kam" : "Kam",
	"DaikinAdapter" : "Daikin", "daikin" : "Daikin",
	"EpowerAdapter" : "Epower", "epower" : "Epower",
	"CiscoAdapter" : "Cisco", "cisco" : "Cisco",
	"SchneiderAdapter" : "Schneider", "schneider" : "Schneider",
	"PhidgetsAdapter" : "Phidgets", "phidgets" : "Phidgets",
	"SitelabAdapter" : "Sitelab", "sitelab" : "Sitelab",
	"IneproAdapter" : "Inepro", "inepro" : "Inepro",
	"BoschAdapter" : "Bosch XDK", "bosch" : "Bosch XDK",
	"CircuitControllerAdapter" : "Circuit Controller", "circuitcontroller" : "Circuit Controller"
};
var adapterName = {
	"Gentos" : "GentosAdapter",
	"Emerson" : "EmersonAdapter",
	"Entes" : "EntesAdapter",
	"Energetix Power Meter" : "EnergetixPowerMeterAdapter",
	"Brainchild" : "BrainchildAdapter",
	"Janitza" : "JanitzaAdapter",
	"Contrec" : "ContrecAdapter",
	"Riello" : "RielloAdapter",
	"Energetix Sensor" : "EnergetixSensorAdapter",
	"GE" : "GeAquatransAdapter",
	"Baylan" : "BaylanAdapter",
	"Siemens" : "SiemensAdapter",
	"Dent" : "DentAdapter",
	"Phidgets" : "PhidgetsAdapter",
	"Sitelab" : "SitelabAdapter",
	"Socomec" : "SocomecAdapter",
	"Bacnet Generic" : "BacnetAdapter",
	"OPC Generic" : "OpcuaAdapter",
	"Modbus Converter" : "ModbusConverterAdapter",
	"Aquametro" : "AquametroAdapter",
	"Kam" : "KamAdapter",
	"Daikin" : "DaikinAdapter",
	"Epower" : "EpowerAdapter",
	"Rest Client" : "RestClientAdapter",
	"Cisco" : "CiscoAdapter",
	"Schneider" : "SchneiderAdapter",
	"Phidgets" : "PhidgetsAdapter",
	"Sitelab" : "SitelabAdapter",
	"Inepro" : "IneproAdapter",
	"Bosch XDK" : "BoschAdapter",
	"Circuit Controller" : "CircuitControllerAdapter"
};

$(document).ready(function() {
	$(document).off('ajaxError');//Disale global event in support.js of felix webconsole
	$.get(pluginRoot,{'id':'adapters'}, function(data) {
		all_adapter = data;
		renderAdaptersList(data);
		if (data.size > 0) {
			cur_adapter = data.curAdapter;
			console.log("------------------------cur_adapter = " + cur_adapter);
			var request = $.ajax({
						url: pluginRoot,
						data : {'html': cur_adapter},
						timeout: 1500, //in milliseconds
						dataType : "html",
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
				$("#adaptersettings").empty();
				$("#adaptersettings").append(data);
				loadJavaScript(cur_adapter);
			});
			request.fail(function( jqXHR, textStatus, errorThrown  ) {
				
			});
		}
	}, 'json');
});

function convertAdapterName2UserName(adapterName) {
	var user_name = userName[adapterName];
	if (typeof user_name != "undefined" && user_name != null) {
		return user_name;
	}
	return adapterName;
}

function convertNormalName2AdapterName(normalName) {
	var adapter_name = adapterName[normalName];
	if (typeof adapter_name != "undefined" && adapter_name != null) {
		return adapter_name;
	}
	return normalName;
}

function renderAdaptersList(data) {
	$("#menuleftcontent_table,tbody").empty();
	if (data.size > 0) {
		var i = 0;
		$.each(data.adapters, function(key, value) {
		    i++;
			$("#menuleftcontent_table").append("<tr id='bundle_" + i +"'><td><a href='adaptersettings?adaptername=" + key + "'>" + convertAdapterName2UserName(key) + "</a></td><td>" + value+ "</td><td style='width:10%'><button type='button' title='Restart' id='restartbundle' class='ui-state-default ui-corner-all' onclick='restartBundle(" + i +");'><span class='ui-icon ui-icon-refresh'> </span></button><button type='button' title='Remove' id='removebundle' class='ui-state-default ui-corner-all' onclick='delBundle(" + i +");'><span class='ui-icon ui-icon-trash'> </span></button><input type='hidden' id='adaptername_" + i + "' value='" + key + "'></td></tr>");
		}); 
	} else {
		$("#settingsform").addClass("disabledmouse");
	}
	
	var arrayAdapterBundles = [];
	$.each(data.bundles, function(key, value) {
		arrayAdapterBundles.push(convertAdapterName2UserName(key));
	});

	arrayAdapterBundles.sort();
	for(var n = 0; n < arrayAdapterBundles.length; n++) {
		$.each(data.bundles, function(key, value) {
			if (arrayAdapterBundles[n] === convertAdapterName2UserName(key)) {
				$("#selectbundle").append($('<option>', {value:value, text:convertAdapterName2UserName(key)}));
			}
		});
	}

}

function gotoStatusPage() {
   location.href=appRoot + "/gatewaystatus";
}

function addBundle() {
	var selectedBundle = $("#selectbundle").val();
	var adapterName = $("#selectbundle option:selected").text();
	console.log("...addBundle...adaptername=" + adapterName );
	if(selectedBundle === "null"){
		alert("Please choose adapter that you would like to add !");
	} else if (checkExistedBundle(adapterName) == 1) {  
		alert("The " + convertAdapterName2UserName(adapterName) + " have been added before !");
	} else {
		if((all_adapter.adapters.length > 0) && (confirmBeforeAddRemoveBundle("adding") === 0)) {
			return false;
		}
		$.post(pluginRoot + "/bundle", {'action':'adding', 'bundle':selectedBundle} , function(data,status,xhr) {
			if (status != "success") {
				alert("Error : Server can not add the "+convertAdapterName2UserName(adapterName));
			} else {
				if (data.exitcode === 1) {
					alert("Can not copy file: " + data.errors[0].string);
				} else if (data.exitcode === 0) {
					//alert("The " + convertAdapterName2UserName(adapterName) + " was copied, please wait after 10s, page will automatically reload !");
					disableThePage();				
					countDown = 10;
					$("#messagesid").text("Adding new Adapter... Please wait ");
					$("#countdowntimer").text(countDown + "s");
					$("#labeltimer").show();
					reloadTimer = setInterval(function(){
									countDown--;
									$("#countdowntimer").text(countDown + "s");
									if (countDown === 0) {
										//enableThePage();
										shouldReloadPage = 1;
										clearInterval(reloadTimer);
										$("#labeltimer").hide();
										if (!cur_adapter) {
											location.href=appRoot + "/adaptersettings";
										} else {
											//location.reload();
											location.href=appRoot + "/adaptersettings?adaptername=" +convertNormalName2AdapterName(adapterName);
										}
									}
								},1000);
				}
			}
		},'json');
	}
	
}

function delBundle(id){
	var adaptername = $("#adaptername_" + id).val();
	console.log("...delBundle... adaptername=" + adaptername);
	if (confirm("This will remove the " + convertAdapterName2UserName(adaptername) + " adapter. Please confirm !" )) {
		if(confirmBeforeAddRemoveBundle("deleting") === 0) {
			return false;
		}
		disableThePage();
		$("#bundle_"+id).remove();		
		$.post(pluginRoot + "/bundle", {'action':'deleting', 'adapter':adaptername} , function(data,status,xhr) {
			countDown = 0;
			clearInterval(reloadTimer);
			$("#labeltimer").hide();
			if (status != "success") {
				alert("Error : Server can not delete the "+convertAdapterName2UserName(adaptername));
				enableThePage();
			} else {
				shouldReloadPage = 1;
				if (cur_adapter == adaptername) {
					location.href=appRoot + "/adaptersettings";
				} else {
					location.reload();
				}
			}
		});
	
		countDown = 0;
		$("#messagesid").text("Removing Adapter... Please wait");
		$("#countdowntimer").text("...");
		$("#labeltimer").show();
		reloadTimer = setInterval(function(){
				countDown++;
				if ((countDown%3) === 0) {
					$("#countdowntimer").text(".");
				} else if ((countDown%3) === 1) {
					$("#countdowntimer").text("..");
				} else if ((countDown%3) === 2) {
					$("#countdowntimer").text("...");
				}
			},1000);
	}

}

function restartBundle(id){
	disableThePage();
    countDown = 0;
    $("#messagesid").text("Restarting Adapter... Please wait");
    $("#countdowntimer").text("...");
    $("#labeltimer").show();
    reloadTimer = setInterval(function(){
        countDown++;
        if ((countDown%3) === 0) {
            $("#countdowntimer").text(".");
        } else if ((countDown%3) === 1) {
            $("#countdowntimer").text("..");
        } else if ((countDown%3) === 2) {
            $("#countdowntimer").text("...");
        }
    },1000);
			
    var adaptername = $("#adaptername_" + id).val();
	console.log("-------------------adaptername = " + adaptername);
    $.post(pluginRoot + "/bundle", {'action':'restarting', 'adapter':adaptername} , function(data,status,xhr) {
        if (status != "success") {
            alert("Error : Server can not restart "+convertAdapterName2UserName(adaptername) + " adapter !");
        } else {
        }
        countDown = 0;
        clearInterval(reloadTimer);
		$("#labeltimer").hide();
		enableThePage();
    });
}

function checkExistedBundle(adapterName) {
	for (var i = 0; i < all_adapter.adapters.length; i++){
		if (convertAdapterName2UserName(all_adapter.adapters[i].adapter_name) === convertAdapterName2UserName(adapterName)) {
			return 1;
		}
	}
	return 0;
}

function disableThePage() {
	$("#navmenu").addClass("disabledmouse");
	$("#maincontent").addClass("disabledmouse");
	$("#menuleftcontent_table").addClass("disabledmouse");
	$("#selectbundle").addClass("disabledmouse");
	$("#addbundle").addClass("disabledmouse");
	$("#uploadbtn").addClass("disabledmouse");
	$(".adaptertitle").addClass("disabledmouse");
}

function enableThePage() {
	$("#navmenu").removeClass("disabledmouse");
	$("#maincontent").removeClass("disabledmouse");
	$("#menuleftcontent_table").removeClass("disabledmouse");
	$("#selectbundle").removeClass("disabledmouse");
	$("#addbundle").removeClass("disabledmouse");
	$("#uploadbtn").removeClass("disabledmouse");
	$(".adaptertitle").removeClass("disabledmouse");
}


function goToDevicePage(adapterName, index, category){
	localStorage.setItem("category",category);
	localStorage.setItem("adapterName",adapterName);
	localStorage.setItem("index",index);
	window.location.href = "/system/console/DeviceSettings";
}

function submitForm(){
	$("#uploadform").submit();
	alert("Upload file success");
}

function uploadFile	(){
	let form = document.getElementById("uploadform");
	form.action = "adaptersettings/upload_file";	
	$("#file").trigger("click");
}

function confirmBeforeAddRemoveBundle(action) {
	return 1;
}

function loadJavaScript(adapter) {
	if (adapter == "BacnetAdapter") {
		$.getScript(pluginRoot+'/js/adaptersettings/bacnet.js');
	} else if (adapter == "OpcuaAdapter") {
		$.getScript(pluginRoot+'/js/adaptersettings/opcua.js');
	} else {
		$.getScript(pluginRoot+'/js/adaptersettings/modbus.js');
	}
}

///API
var adapterSettingsData ;
var addedDeviceTotal = 0;
var updated_device = [];
var deleted_device = [];
var inserted_device = [];
var device_table = "device_list";
var _device = {
	"name" : "device_name",
	"address" : "device_address",
	"category": "device_category",
	"instanceid": "device_instanceid",
	"alternativeid" : "device_alternativeid",
	"version" : "device_version",
	"netaddr" : "device_network_address",
	"netnum" : "device_network_number"
}

var device_type = {"AquametroAdapter" : {"5010" : "AMTRON_MAG", "5011" : "CALECST_II"},
		   "BaylanAdapter" : {"7001" : "BL"},
		   "BrainchildAdapter" : {"3000" : "DI-16", "3002" : "DI-16 State", "3001" : "AI-08", "3003" : "DAIO-08"},
		   "ContrecAdapter" : {"5001" : "BTU-212"},
		   "DaikinAdapter" : {"7050" : "MicroTechII"},
		   "DentAdapter" : {"1006" : "PM-100Plus", "1003" : "PM-100_3", "1004" : "PM-100_18"},
		   "DummyAdapter" : {"3000" : "DI-16","3002" : "DI-16_State", "2000" : "TSA01", "1013" : "MPR46S", "8001" : "MULTICOM_30X", "5001" : "MODEL212", "1012" : "PM200_v20", "1025" : "Pro1250D"},
		   "EmersonAdapter" : {"7101" : "Liebert_CRV","7102" : "Liebert_PEX","7103" : "Liebert_NXR"},
		   "EnergetixPowerMeterAdapter" : {"1002" : "EM-100", "1009" : "EM-101", "1011" : "EM-200", "1012" : "PM-200", "1016" : "EM-300-6", "1017" : "MULTI-GEM-18"},
		   "EnergetixSensorAdapter" : {"2000" : "TSA-01", "2001" : "TST-01", "2002" : "TSA-02", "2003" : "TST-02", "2004" : "LS-01"},
		   "EntesAdapter" : {"1013" : "MPR 46S/47S"},
		   "EpowerAdapter" : {"1070" : "ECM770"},
		   "GeAquatransAdapter" : {"7021" : "AT600"},
		   "IneproAdapter" : {"1025" : "PRO-1250D"},
		   "JanitzaAdapter" : {"1014" : "UMG-96S"},
		   "KamAdapter" : {"1010" : "PFM-DPM"},
		   "ModbusConverterAdapter" : {"7031" : "ADFWeb-HD67029M", "7030":"Val-Technik", "7032" : "HCS"},
		   "RielloAdapter" : {"8001" : "MTCOM_30X"},
		   "SchneiderAdapter" : {"1021" : "PM710","1022" : "iEM3100/iEM3200", "1023" : "ION7650", "1024" : "PM2200", "1026" : "PM1200"},
		   "SiemensAdapter" : {"5002" : "FUE-950","5003" : "MAG-6000"},
		   "SitelabAdapter" : {"7026" : "SL1168"},
		   "SocomecAdapter" : {"1060" : "COUNTISE44"},
			"BoschAdapter" : {"2050" : "XDK"},
		   "PhidgetsAdapter" : {"2010" : "IK888"},
		   "CircuitControllerAdapter" : {"2102" : "Relay3", "2103" : "Relay8"}
};

var default_device = {"AquametroAdapter" : {"device_name" : "AMTRON_MAG","device_category" : "5010","device_instanceid" : "1"},
	   "BacnetAdapter" : {"device_name" : "Bacnet Device","device_address" : "192.168.1.10","device_category" : "10000","device_instanceid" : "100","device_alternativeid" : "-1","device_network_number" : "0","device_network_address" : "{-1}"},
	   "BaylanAdapter" : {"device_name" : "BL","device_category" : "7001","device_instanceid" : "1"},
	   "BrainchildAdapter" :{"device_name" : "DI-16","device_category" : "3000","device_instanceid" : "1"},
	   "ContrecAdapter" : {"device_name" : "BTU-212","device_category" : "5001","device_instanceid" : "1"},
	   "DaikinAdapter" : {"device_name" : "MicroTechII","device_category" : "7050","device_instanceid" : "1"},
	   "DentAdapter" : {"device_name" : "PM-100Plus","device_category" : "1006","device_instanceid" : "1"},
	   "DummyAdapter" : {"device_name" : "DI-16","device_category" : "3000","device_instanceid" : "1"},
	   "EmersonAdapter" : {"device_name" : "Liebert_CRV","device_category" : "7101","device_instanceid" : "1"},
	   "EnergetixPowerMeterAdapter" : {"device_name" : "PM-200","device_category" : "1012","device_instanceid" : "1","device_version" : "2.0"},
	   "EnergetixSensorAdapter" : {"device_name" : "TSA-02","device_category" : "2002","device_instanceid" : "1"},
	   "EntesAdapter" : {"device_name" : "MPR 46S/47S","device_category" : "1013","device_instanceid" : "1"},
	   "EpowerAdapter" : {"device_name" : "ECM770","device_category" : "1070","device_instanceid" : "1"},
	   "GeAquatransAdapter" : {"device_name" : "AT600","device_category" : "7021","device_instanceid" : "1"},
	   "IneproAdapter" : {"device_name" : "PRO-1250D","device_category" : "1025","device_instanceid" : "1"},
	   "JanitzaAdapter" : {"device_name" : "UMG-96S","device_category" : "1014","device_instanceid" : "1"},
	   "KamAdapter" : {"device_name" : "PFM-DPM","device_category" : "1010","device_instanceid" : "1","device_version" : "2.0"},
	   "ModbusConverterAdapter" : {"device_name" : "ADFWeb-HD67029M","device_category" : "7031","device_instanceid" : "1"},
	   "RielloAdapter" : {"device_name" : "MTCOM_30X","device_category" : "8001","device_instanceid" : "1"},
	   "SchneiderAdapter" : {"device_name" : "PM710","device_category" : "1021","device_instanceid" : "1"},
	   "SiemensAdapter" : {"device_name" : "FUE-950","device_category" : "5002","device_instanceid" : "1"},
	   "SitelabAdapter" : {"device_name" : "SL1168","device_category" : "7026","device_instanceid" : "1"},
	   "SocomecAdapter" : {"device_name" : "COUNTISE44","device_category" : "1060","device_instanceid" : "1"},
	   "PhidgetsAdapter" : {"device_name" : "IK888","device_category" : "2010","device_instanceid" : "1"},
		"BoschAdapter" : {"device_name" : "XDK","device_category" : "2050","device_instanceid" : "1"},
	   "CircuitControllerAdapter" : {"device_name" : "Relay3","device_category" : "2102","device_instanceid" : "1"},
	   "OpcuaAdapter" : {"device_name" : "Opcua Device","device_address" : "","device_category" : "10010","device_instanceid" : "1","device_security_policy" : "","device_user" : "","device_pass" : ""}}
		   
var version_table  ={"EnergetixPowerMeterAdapter" : {"1012" :{"1.0" : "1.0", "1.3" : "1.3", "2.0" : "2.0"}, "1009" : {"1.0" : "1.0", "2.0" : "2.0"}},
						"EnergetixSensorAdapter" : {"2000" : {"1.0" : "1.0", "2.0" : "2.0"}}, "KamAdapter" : {"1010" : {"1.0" : "1.0", "2.0" : "2.0"}}};

function addDeviceForAdapter() {
	let maxid = 0;
	let rows = $("#device_list tr");
	for (let i = 0; i < rows.length; i++) {
		let curr_id = $("#device_instanceid_" + i).val();
		if ( typeof(curr_id) !== "undefined" && curr_id !== null ) {
			if (parseInt(curr_id) > maxid) {
				maxid = parseInt(curr_id);
			}
		}
		debugger;
	}
	debugger
	maxid++;
	default_device[cur_adapter][_device.instanceid] = maxid;
	buildDeviceRowHTML(device_table, addedDeviceTotal, default_device[cur_adapter]);
	addedDeviceTotal++;
	$("#device_num").text(parseInt($("#device_num").text()) + 1);
}

function deleteDeviceItem() {
	var row = $(this).closest('tr');
	var id = parseInt(row[0].id);
	if (id < adapterSettingsData.devices.length) {
		deleted_device.push(adapterSettingsData.devices[id]);
	}
	debugger;
	$(this).closest('tr').remove();
	$("#device_num").text(parseInt($("#device_num").text()) - 1);
}

function showSuccessForm() {
	$("#" + status_update).remove();
	var _ul = document.createElement("ul");
	_ul.className="formsuccess";
	_ul.id = status_update;
	var _li = document.createElement("li");
	_li.innerHTML = "Settings have been updated";
	_ul.appendChild(_li);
	$("#titleform").after(_ul);

}

function showErrorForm(errors_data) {
	$("#" + status_update).remove();
	var _ul = document.createElement("ul");
	_ul.className="formerrors";
	_ul.id = status_update;
	var _li = document.createElement("li");
	_li.innerHTML = "Cannot save for Adapter Settings";
	_ul.appendChild(_li);
	for (var i = 0; i < errors_data.length; i++) {
		_li = document.createElement("li");
		_li.innerHTML = "&nbsp&nbsp"+errors_data[i];
		_ul.appendChild(_li);
	}
	$("#titleform").after(_ul);	
}

function showStatusForm() {
	let show_error_status = "";
	if (localStorage) {
		// LocalStorage is supported!
		//localStorage.getItem("adapter_setting_error", "true");
		show_error_status = localStorage.getItem("adapter_setting_notify");
		debugger
		if (show_error_status == "true") {
			localStorage.removeItem('adapter_setting_notify');
			showSuccessForm();
		}
	} 
}

function submitdata(action, myData) {
	disableThePage();
	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/' + action,
		data: myData,
		timeout: 20000, //in milliseconds
		dataType : "json",
		async: 'false',
		statusCode: {
			400: function () {
			},
			404: function () {
				location.replace(''); //clear the page content
			},
			408: function () {
			}
		}
	});
	
	request.done(function (data) {
		debugger
		enableThePage();
		if (data.result == "success") {
			if (localStorage) {
				// LocalStorage is supported!
				localStorage.setItem("adapter_setting_notify", "true");
			} else {
				// No support. Use a fallback such as browser cookies or store on the server.
			}
			location.reload();
		} else {
			showErrorForm(data.errors);
		}
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		enableThePage();
	});
}

function onChangeProtocol(){
	var pro = $("#protocol").val();
	if (pro == "rtu") {
		$(".modbus_serial").show();
		$(".modbus_tcp").hide();
	} else if (pro == "tcp") {
		$(".modbus_serial").hide();
		$(".modbus_tcp").show();
	}
}
function createSelectOptionList(data) {
	let select_object;
	if (Object.keys(data).length > 0) {
		select_object = document.createElement("SELECT");
		select_object.className = "ui-corner-all";
		$.each(data, function(key, value) {
			let option_object = document.createElement("OPTION");
				option_object.value = key;
				option_object.innerHTML = value;
				select_object.appendChild(option_object);
		});
	}
	return select_object;
}

function checkUniqueAddress(table_id){
	var ok = 1;
	var errors = [];
	var table = document.getElementById(table_id);
	var rows = table.rows;
	debugger
	for (var n = 0; n < rows.length; n++) {
		for (var m = 0; m < rows.length; m++) {
			if (m == n) {
				continue;
			}
			let address_n = document.getElementById(_device.address + "_" + rows[n].id);
			let address_m = document.getElementById(_device.address + "_" + rows[m].id);
			let instanceid_n = document.getElementById(_device.instanceid + "_" + rows[n].id);
			let instanceid_m = document.getElementById(_device.instanceid + "_" + rows[m].id);
		
			if (typeof address_n !== "undefined" && address_n !== null) {
				if (address_n.value === address_m.value) {
					if (instanceid_n.value === instanceid_m.value) {
						address_n.style.border = "1px solid red";
						instanceid_n.style.border = "1px solid red";
						errors.push("Device (" + address_n.value + ", " + instanceid_n.value + ") is using existed address and intance id");
						break;
					} else {
						let alternativeid_n = document.getElementById(_device.alternativeid + "_" + rows[n].id);
						let alternativeid_m = document.getElementById(_device.alternativeid + "_" + rows[m].id);
						if (alternativeid_n.value != "-1") {
							if (alternativeid_n.value === alternativeid_m.value) {
								address_n.style.border = "1px solid red";
								alternativeid_n.style.border = "1px solid red";
								errors.push("Device (" + address_n.value + ", " + alternativeid_n.value + ") is using existed address and delegated id");
								break;
							}
						}
					}
				}
			} else {
				if (instanceid_n.value === instanceid_m.value) {
					instanceid_n.style.border = "1px solid red";
					errors.push("Device (" + instanceid_n.value + ") is using existed Device Id");
					break;
				}
			}
		}
	}
	return errors;
}

function checkDataValidations() {
	var error_string = [];

	error_string = checkUniqueAddress(device_table);
	
	
	var inputSet = document.getElementsByTagName("input");
	for (var i = 0; i < inputSet.length; i++) {
		if (inputSet[i].name === _device.address) {
			if (!(ip_address_format.test(inputSet[i].value))) {
				inputSet[i].style.border = "1px solid red";
				error_string.push("Ip address " + inputSet[i].value + " is invalid");
			}
			continue;
		}
		
		if (inputSet[i].name === _device.category) {
			if (isNaN(inputSet[i].value)) {
				inputSet[i].style.border = "1px solid red";
				error_string.push("Category " + inputSet[i].value + " is invalid");
			}
			continue;
		}
		
		if (inputSet[i].name === _device.alternativeid) {
			if (isNaN(inputSet[i].value)) {
				inputSet[i].style.border = "1px solid red";
				error_string.push("Delete Id " + inputSet[i].value + " is invalid");
			}
			continue;
		}
		
		if (inputSet[i].name === _device.instanceid) {
			if (isNaN(inputSet[i].value)) {
				inputSet[i].style.border = "1px solid red";
				error_string.push("Instance Id " + inputSet[i].value + " is invalid");
			}
			continue;
		}
	}
	debugger
	return error_string;
}

function exportAdapterSettings(){
	window.location.href = appRoot + "/adaptersettings?adapter="+cur_adapter +"&action=exporting";
}

function importAdapterSettings(){
	
	// let adapter = document.createElement("INPUT");
	// adapter.setAttribute("type", "hidden");
	// adapter.setAttribute("value",cur_adapter);
	// adapter.setAttribute("name","adapter_name");
	// adapter.setAttribute("id","adapter_name");
	
	let form = document.getElementById("uploadform");
	form.action = "adaptersettings/importing";	
	$("#file").trigger("click");
	
}

function reloadAdapterSettings() {
	location.reload();
}

function resetDefaultAdapterSettings() {
	var current_device_num = parseInt($("#device_num").text());
	if (current_device_num > 1) {
		for (let i = 1; i < current_device_num; i++) {
			deleted_device.push(adapterSettingsData.devices[i]);
			$("#"+i).remove();
			$("#device_num").text(parseInt($("#device_num").text()) - 1);
		}
	}
}
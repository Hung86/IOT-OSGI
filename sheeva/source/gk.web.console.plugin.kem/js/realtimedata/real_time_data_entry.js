var adapter;
var timer;
var apdapter_mode = 0;
var is_getting_data = 0;
var normal_data_timeout = 30000;//0.5 minute
var fast_data_timeout = 3000;//3 seconds

var _device = {
	"name" : "device_name",
	"address" : "device_address",
	"category": "device_category",
	"instanceid":"device_instanceid",
	"alternativeid" : "device_alternativeid"
}
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
	"ModbusConverterAdapter" : "Modbus Converter",
	"BacnetAdapter" : "Bacnet Generic",
	"AquametroAdapter" : "Aquametro",
	"KamAdapter" : "Kam",
	"DaikinAdapter" : "Daikin",
	"EpowerAdapter" : "Epower",
	"CiscoAdapter" : "Cisco",
	"SchneiderAdapter" : "Schneider",
	"PhidgetsAdapter" : "Phidgets",
	"SitelabAdapter" : "Sitelab",
	"CircuitControllerAdapter" : "Circuit Controller",
	"IneproAdapter" : "Inepro",
	"BoschAdapter" : "Bosch XDK"
};

$(document).ready(function(){
	$(document).off('ajaxError');//Disale global event in support.js of felix webconsole
	$.get(pluginRoot, { 'id':'adapters'}, function(data) {
		debugger
		if(data.length > 0) {
			renderAllAdapters(data);
			$('#adapters').trigger("change");
		}
	}, 'json');
	
	//change to another device
	$('#adapters').change(function () {
		adapter = $('#adapters').val();
		console.log("===>adapter is changed to " + adapter);
		if ((adapter == "BacnetAdapter") || (adapter == "OpcAdapter") ) {
			$.getScript(pluginRoot+'/js/realtimedata/bacnet.js');
		} else if (adapter == "ModbusConverterAdapter") {
			$.getScript(pluginRoot+'/js/realtimedata/converter.js');
		} else {
			$.getScript(pluginRoot+'/js/realtimedata/modbus.js');
		}
	})
	
	//radio button
	$('#radio').change(function () {
		processRadioButtonEvent(adapter, apdapter_mode);
	});
	
	// change auto button
	$('#auto').click(function(){
		if($(this).is(':checked')){
	    	timer = setInterval(function(){
				refreshData(fast_data_timeout);
	    	},5000);
	    	//disable refresh button
	    	$('#refresh').prop('disabled', true);
	    } else {
	    	clearInterval(timer);
	    	//enable refresh button
	    	$('#refresh').prop('disabled', false);
	    }
	});
});


function renderAllAdapters(data) {
	data.sort();
	for (var i = 0; i < data.length; i++){
		$("#adapters").append($('<option>', {value:data[i], text:convertAdapterName2UserName(data[i])}));
	}	
	adapter = $("#adapters").val();
}

function convertAdapterName2UserName(adapterName) {
	var user_name = userName[adapterName];
	if (typeof user_name != "undefined" && user_name != null) {
		return user_name;
	}
	return adapterName;
}

//API
function renderAdapterSettings(data) {
}

function onChangeAddress(){
}

function onChangeInstanceId(){
}

function showDeviceName(id){
}

function getDeviceCategory(address, id) {
	debugger
	for (let i = 0 ; i < adapterSettingsData.devices.length; i++) {
		if (address != null) {
			if ((id == adapterSettingsData.devices[i][_device.instanceid]) && (address == adapterSettingsData.devices[i][_device.address])) {
				return adapterSettingsData.devices[i][_device.category];
			}
		} else {
			if (id == adapterSettingsData.devices[i][_device.instanceid]) {
				return adapterSettingsData.devices[i][_device.category];
			}
		}
	}
	return "0";
}

function showMode0() {
	$('#radio1').prop('checked', false);
	$('#radio2').prop('checked', true);
	$('#auto').hide("fast");
	$('#warning').hide();
	if ($('#auto').is(':checked')) {
		$('#auto').attr("checked",false);
		$('#refresh').prop('disabled', false);
	}
	clearInterval(timer);
}

function showMode1() {
	$('#radio1').prop('checked', true);
	$('#radio2').prop('checked', false);
	$('#auto').show("fast");
	$("#auto").attr("checked",true);
	$("#auto").trigger('click');
	$("#auto").attr("checked",true);
	$('#warning').show();
}

function setMode(current_adapter, mode) {
	var request = $.ajax({
		url: pluginRoot,
		type: "POST",
		data : { 'action': 'set_mode','adapter': current_adapter, 'mode' : mode},
		timeout: fast_data_timeout, //in milliseconds
		statusCode: {
			400: function() {
				//alert( "Bad Request" );
			},
			404: function() {
				location.replace("");//clear the page content
			},
			408: function() {
				//alert( "Request Timeout" );
			}
		}
	});
	request.done(function( data ) {
		apdapter_mode = parseInt(data.mode);
		console.log("Successfully ! set adapter to mode " + apdapter_mode);
		if (apdapter_mode == 0) {
			showMode0();
		} else if (apdapter_mode == 1) {
			showMode1();
		}
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		console.log("Failure ! couldn't set adapter to mode " + mode);
	});
}

function processRadioButtonEvent(current_adapter){
	if($('#radio :radio:checked').val() == '1') {
		if (!confirm('Setting the KEM Gateway into Web Console mode will disable data transmission to the Cloud. WARNING - Please remember to switch back to Send To Remote Host before you log off.')) {
			showMode0();		
			return false;
		}	
		setMode(current_adapter, 1);
	} else if($('#radio :radio:checked').val() == '0'){
		setMode(current_adapter, 0);
	}
}

function refreshData(time_out) { // update apdater status and get fresh data
}

function initializeDataPage(adapter, address, id, category, mode, time_out) {
}

function getUpdatedData(adapter_name, device_address, instance_id, index, node_len, mode, time_out){
	var request = $.ajax({
	    	url: pluginRoot,
			type: "POST",
	    	data : { 'action':'read_data','adapter': adapter_name,'address': device_address, 'id':instance_id, 'channel': index,  'length': node_len, 'mode': mode },
	    	timeout: time_out, //in milliseconds
	    	dataType: "json",
	    	statusCode: {
	    		400: function() {
//	     			alert( "Bad Request" );
	    		},
	    		404: function() {
	    			location.replace("");//clear the page content
	    		},
	    		408: function() {
	     			//alert( "Request Timeout" );
	    		}
	    	},
	    	error: function (xhr, ajaxOptions, thrownError) {
	    		}
	});
	request.done(function( data ) {
	    console.log("--------------call getUpdatedData done : mode = " + mode);
		debugger
	    renderDeviceData(data);
	});
	
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		console.log("--------------call getUpdatedData fail");
	    //alert( "Request failed: status= " + textStatus+",error="+errorThrown );
	    if(textStatus === "timeout"){
			$('#view_status').text("Request timeout");
	    }
	});
}

function renderDeviceData(real_time_data) {
}

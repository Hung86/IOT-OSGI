function getCategory(instanceId, adapterName){
	let adapter = all_adapter[adapterName];
	for(let i = 0 ; i < adapter.length ; i ++){
		if(adapter[i].device_instanceid == instanceId)
			return adapter[i].device_category ;
	}
	return "undefined";
}

function getAllInstanceId(adapterName){
	let adapter = all_adapter[adapterName];
	let result = [];
	for(let i = 0 ; i < adapter.length ; i++){
		result.push(adapter[i].device_instanceid);
	}
	return result ;
}

function onchangeAdapter(){
	$("#modbus").show();
	$("#bacnet").hide();
	let adapterName = $("#adapter").val();
	let deviceIntanceIds = getAllInstanceId(adapterName);
	let currentDeviceId = deviceIntanceIds[0];
	if (localStorage.device_id) {
		currentDeviceId = localStorage.device_id;
		localStorage.removeItem("device_id");
	}
	let category = getCategory(currentDeviceId,adapterName);
	renderDeviceInstance(adapterName);
	$("#modbus #device_instanceid").val(currentDeviceId);
	$("#modbus #device_instanceid").trigger("change");
}
function onchangeDeviceAddress(){
	
}
function onchangeDeviceInstance(){
	let deviceIntanceId  = $("#modbus #device_instanceid").val() ;
	let adapterName = $("#adapter").val() ;
	let adapter = all_adapter[adapterName];
	let category = getCategory(deviceIntanceId, adapterName);

	for(let i = 0 ; i< adapter.length ; i ++){
		if(adapter[i].device_instanceid == deviceIntanceId 
		){
			$("#modbus #device_name").html(adapter[i].device_name);
			$("#modbus #prefix_gateway").html(cur_gwid + "-" + category + "-" + deviceIntanceId);
		}
	}
	
	$(".div_validation").hide();
	goToDeviceSettings(deviceIntanceId,category,adapterName);
	$("#writingbutton").hide();
	// getChannel(adapterName, 1) ;
	// fillData();
	
}
function renderDeviceInstance(adapterName){
	let deviceIntanceDom = $("#modbus #device_instanceid");
	$(deviceIntanceDom).empty();
	let adapter = all_adapter[adapterName];
	for(let i = 0 ; i < adapter.length ; i ++){
		let deviceIntance = adapter[i].device_instanceid ;
		let option = $("<option></option>").val(deviceIntance).text(deviceIntance);
		$(deviceIntanceDom).append(option); 
	}
}
function goToDeviceSettings(addressId, category, adapterName){
	var request = $.ajax({
		type : "get",
		url : pluginRoot,
		data : {
			'action' : "template",
			'category' : category,
			'adapterName' : adapterName
		},
		timeout : 15000, //in milliseconds
		dataType : "html",
		async : 'false',
		statusCode : {
			400 : function() {},
			404 : function() {
				location.replace(''); //clear the page content
			},
			408 : function() {}
		}
	});
	request.done(function(data) {
		$('#adaptersettings').empty();
		$('#adaptersettings').append(data);
		// disable button write
		$("#writingbutton").prop('disabled', true);
		$("#writingbutton").css("opacity",0.5);
		
		// readConfig();
		readSettings(adapterName, addressId);


		let adapter = all_adapter[adapterName];
		let version = "";
		for (let i in adapter) {
			if (adapter[i].device_instanceid == addressId) {
				if (adapter[i].device_version != "") {
					version = adapter[i].device_version ;
				}
			}
		}
		if (parseInt(version) == 1) {
			$("#configuration").hide();
			$("#configuration_v1").show();
		} else if (parseInt(version) == 2) {
			$("#configuration").show();
			$("#configuration_v1").hide();
		}

		force = 1;

	});
	request.fail(function(jqXHR, textStatus, errorThrown) {
		console.log("tao lao");

	});
}

function renderDevice(data,adapterName){
	if(data[adapterName] == "[]"){
		console.log("don't have anything in adpater");
		return ;
	}
		
	let instanceId = data[adapterName][0].device_instanceid ;
	let category = data[adapterName][0].device_category ;

}


$(document).on("click","#configuration_tab",function(){
	$(".configuration").show();
	$(".channel").hide();
	$("#setting_tab").removeClass("active");
	$("#configuration_tab").addClass("active");
	let adapterName = $("#adapter").val();
	if (adapterName == "BoschAdapter") {
		let addressId = $("#modbus #device_instanceid").val();
		localStorage.setItem("current_tab", "config");
		readConfigXDK(adapterName, addressId);
	}
});

$(document).on("click","#setting_tab",function(){ 
	debugger
	$(".channel").show();
	$("#setting_tab").addClass("active");
	$("#configuration_tab").removeClass("active");
	$(".configuration").hide();
	let adapterName = $("#adapter").val();
	let addressId = $("#modbus #device_instanceid").val();
	if (adapterName == "BoschAdapter") {
		localStorage.setItem("current_tab", "setting");
	}
	readSettings(adapterName,addressId);
});

function readSettings(adapterName, addressId){
	console.trace();
	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/readingsettings',
		data: {
		'addressId': addressId,
		'adapterName': adapterName
		},
		timeout: 15000, //in milliseconds
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
		
		let rows = $("#channel tbody tr");
		for(let i = 0 ; i < data.length ; i++){
			let dataPoint = $(rows[i]).find("#data_point");
			$(dataPoint).val(data[i].data_point);
			readBrandChildSettings(rows[i],data[i]);
			readPhidGetSettings(rows[i],data[i]);
			readXDKSettings(rows[i],data[i]);
			readInePro(rows[i],data[i]);
			readEnergetix(rows[i],data[i]);
			if(adapterName == "ModbusConverterAdapter"){
				addDataPointToChannel(data[i]);
			}
		}
		hideNullTrane();
		
		//forward to config tab if it have
		if (adapterName == "BoschAdapter") {
			if (localStorage.action == "import") {
				localStorage.removeItem("action");
			}
			
			if(localStorage.current_tab) {
				if (localStorage.current_tab == "config") {
					$("#configuration_tab").trigger("click");
				}
				//localStorage.removeItem("current_tab");
			} 
		}
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		alert("No Response");
		$('#page').removeClass('disabledmouse');
		console.log(textStatus + "---- "+ errorThrown);
		
	});
}

function hideNullTrane(){
	var element   = $("#device select");
	$.each(element,function(key, selector){
		// oid and fomula on the same tr
		if($(selector).css('display')== "none")
			return ;
		if($(selector).val() == "n/a" || $(selector).val() == null){
			$(this).closest('tr').css('opacity', 0.5);
		}else{
			$(this).closest('tr').css('opacity', 1);
		}
	});
}

function readConfigXDK(adapterName, addressId) {
	console.trace();
	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/readingsettingXDK',
		data: {
		'addressId': addressId,
		'adapterName': adapterName
		},
		timeout: 15000, //in milliseconds
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
			let rows_config = $("#configuration tbody tr");
			for(let i = 0 ; i < data.length; i++){
				let dataPoint = $(rows_config[i]).find("#data_point");
				$(dataPoint).val(data[i].data_point);
				let input = $(rows_config[i]).find("#"+data[i].name);
				$(input).val(data[i].value);

			}
		
			$("#deviceaddress").text($("#modbus #device_instanceid").val());
			
			$('input:radio[name=mode]').change(function() {
				if($(this).val() === 'dhcp') {
					console.log('dhcp selected!');
					$("#netmode").val("dhcp");
					disableInputFields();
				} else  {
					console.log('static selected!');
					$("#netmode").val("static");
					enableInputFields();
				} 
			});
			
			$('input:radio[name=protocol]').change(function() {
				 if ($(this).val() === 'mqtt') {
					console.log('mqtt selected!');
					$("#msgmode").val("mqtt");
				} else  {
					console.log('http selected!');
					$("#msgmode").val("http");
				}
			});
			
			if ($("#netmode").val() == "dhcp") {
				document.getElementById('mode_static').checked = false;
				document.getElementById('mode_dhcp').checked = true;
				disableInputFields();
			} else if ($("#netmode").val() == "static") {
				document.getElementById('mode_dhcp').checked = false;
				document.getElementById('mode_static').checked = true
				enableInputFields();
			}
			
			if ($("#msgmode").val() == "mqtt") {
				document.getElementById('mode_mqtt').checked = true;
				document.getElementById('mode_http').checked = false;
			} else if ($("#msgmode").val() == "http") {
				document.getElementById('mode_mqtt').checked = false;
				document.getElementById('mode_http').checked = true
			}
			debugger;

		
		hideNullTrane();
	});
	
	request.fail(function (jqXHR, textStatus, errorThrown) {
		alert("No Response");
		$('#page').removeClass('disabledmouse');
		console.log(textStatus + "---- "+ errorThrown);
		
	});
}

function saveConfigXDK() {
	$('#page').addClass('disabledmouse');
	let adapterName = $("#adapter").val();
	let addressId = $("#modbus #device_instanceid").val();
	let dataSend = [];
	
	let rows = $("#configuration tbody tr");
	for (let i = 0; i < rows.length ; i++) {
		let setting = {};
		
		let dataPoint =  $(rows[i]).find("#data_point");
		setting["data_point"] = $(dataPoint).val();
		
		let ssidname = $(rows[i]).find("#ssidname");
		if(ssidname.length > 0){
			setting["value"] = $(ssidname).val();
			setting["name"] = "ssidname";
			dataSend.push(setting);
		}
		
		let wifipass = $(rows[i]).find("#wifipass");
		if(wifipass.length > 0){
			setting["value"] = $(wifipass).val();
			setting["name"] = "wifipass";
			dataSend.push(setting);
		}
		
		let netmode = $(rows[i]).find("#netmode");
		if(netmode.length > 0){
			setting["value"] = $(netmode).val();
			setting["name"] = "netmode";
			dataSend.push(setting);
		}
		
		let staticip = $(rows[i]).find("#staticip");
		if(staticip.length > 0){
			setting["value"] = $(staticip).val();
			setting["name"] = "staticip";
			dataSend.push(setting);
		}
		
		let subnet = $(rows[i]).find("#subnet");
		if(subnet.length > 0){
			setting["value"] = $(subnet).val();
			setting["name"] = "subnet";
			dataSend.push(setting);
		}
		
		let gateway = $(rows[i]).find("#gateway");
		if(gateway.length > 0){
			setting["value"] = $(gateway).val();
			setting["name"] = "gateway";
			dataSend.push(setting);
		}
		
		let dns = $(rows[i]).find("#dns");
		if(dns.length > 0){
			setting["value"] = $(dns).val();
			setting["name"] = "dns";
			dataSend.push(setting);
		}
		
		let msgmode = $(rows[i]).find("#msgmode");
		if(msgmode.length > 0){
			setting["value"] = $(msgmode).val();
			setting["name"] = "msgmode";
			dataSend.push(setting);
		}
		
		let gatewayip = $(rows[i]).find("#gatewayip");
		if(gatewayip.length > 0){
			setting["value"] = $(gatewayip).val();
			setting["name"] = "gatewayip";
			dataSend.push(setting);
		}
		
		let gatewayport = $(rows[i]).find("#gatewayport");
		if(gatewayport.length > 0){
			setting["value"] = $(gatewayport).val();
			setting["name"] = "gatewayport";
			dataSend.push(setting);
		}
		
		let sendinterval = $(rows[i]).find("#sendinterval");
		if(sendinterval.length > 0){
			setting["value"] = $(sendinterval).val();
			setting["name"] = "sendinterval";
			dataSend.push(setting);
		}
	}
	debugger;


	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/saveConfigXDK',
		data: {
		'addressId': addressId,
		'adapterName': adapterName,
		'data': JSON.stringify(dataSend)
		},
		timeout: 30000, //in milliseconds
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
		$('#page').removeClass('disabledmouse');
		if(data == null){
			showSuccessFormXDK();
			return ;
		}
		if (data.errors.length > 0) {
			showErrorFormXDK(data.errors);
		}
	});
	
	request.fail(function (jqXHR, textStatus, errorThrown) {
		$('#page').removeClass('disabledmouse');
		alert("No Response");
		console.log(textStatus + "---- "+ errorThrown);
	});
	
}

function saveSettings(){
	$('#page').addClass('disabledmouse');
	 
	let adapterName = $("#adapter").val();
	let addressId = $("#modbus #device_instanceid").val();
	let rows = $("#channel tbody tr");
	let dataSend = [];
	for(let i = 0 ; i < rows.length ; i++){
		let setting = {};
		
		let dataPoint =  $(rows[i]).find("#data_point");
		// if(dataPoint.length == 0){
			// continue ;
		// }
		
		setting["data_point"] = $(dataPoint).val();
		
		let label = $(rows[i]).find("#label");
		if(label.length > 0){
			setting["label"] = $(label).val();
		}else{
			setting["label"] = "";
		}
		
		let name = $(rows[i]).find("#name");
		if(name.length > 0){
			setting["name"] = $(name).val();
		}else{
			setting["name"] = "";
		}
		
		let unit = $(rows[i]).find("#unit");
		if(unit.length > 0){
			setting["unit"] = $(unit).val();
		}else{
			setting["unit"] = "";
		}
		
		let max = $(rows[i]).find("#max");
		if(max.length > 0){
			setting["max"] = $(max).val();
		}else{
			setting["max"] = "";
		}
		
		let min = $(rows[i]).find("#min");
		if(min.length > 0){
			setting["min"] = $(min).val();
		}else{
			setting["min"] = "";
		}
		
		let ratio = $(rows[i]).find("#ratio");
		if(ratio.length > 0){
			setting["ratio"] = $(ratio).val();
		}else{
			setting["ratio"] = "";
		}
		
		let scalar = $(rows[i]).find("#data_scalar");
		if(scalar.length > 0){
			setting["data_scalar"] = $(scalar).val();
		}
		else{
			setting["data_scalar"] = "";
		}
		
		let sensortype = $(rows[i]).find("#sensor_type");
		if(sensortype.length > 0){
			setting["sensor_type"] = $(sensortype).val();
		}
		else{
			setting["sensor_type"] = "";
		}
		
		let threshold = $(rows[i]).find("#threshold");
		if(threshold.length > 0){
			setting["threshold"] = $(threshold).val();
		}
		else{
			setting["threshold"] = "";
		}
		
		let triggermin = $(rows[i]).find("#trigger_min");
		if(triggermin.length > 0){
			setting["trigger_min"] = $(triggermin).val();
		}
		else{
			setting["trigger_min"] = "";
		}
		let formula = $(rows[i]).find("#formula");
		if(formula.length > 0){
			setting["formula"] = $(formula).val();
		}
		else{
			setting["formula"] = "";
		}
		//  Type
		let type = $(rows[i]).find("#type");
		if(type.length > 0){
			setting["type"] = $(type).val();
		}else{
			setting["type"] = "";
		}
		
		// Channel
		let channel = $(rows[i]).find("#channel");
		if(channel.length > 0){
			setting["channel"] = $(channel).val();
		}else{
			setting["channel"] = "";
		}
		
		let sensor = $(rows[i]).find("#sensor");
		if(sensor.length > 0){
			setting["sensor"] = $(sensor).val();
		}
		else{
			setting["sensor"] = "";
		}
		
		let offset = $(rows[i]).find("#offset");
		if(offset.length > 0){
			setting["offset"] = $(offset).val();
		}
		else{
			setting["offset"] = "0";
		}
		
		let delta = $(rows[i]).find("#delta");
		if(delta.length > 0){
			setting["delta"] = $(delta).val();
		}
		else{
			setting["delta"] = "0";
		}
		dataSend.push(setting);
	}
	debugger
	
	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/savesettings',
		data: {
		'addressId': addressId,
		'adapterName': adapterName,
		'data': JSON.stringify(dataSend)
		},
		timeout: 30000, //in milliseconds
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
		$('#page').removeClass('disabledmouse');
		if(data == null){
			if (adapterName == "BoschAdapter") {
				showSuccessFormXDK();
			} else {
				showSuccessForm();
			}
			return ;
		}
		if (data.errors.length > 0) {
			if (adapterName == "BoschAdapter") {
				showErrorFormXDK(data.errors);
			} else {
				showErrorForm(data.errors);
			}
		}
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		$('#page').removeClass('disabledmouse');
		alert("No Response");
		console.log(textStatus + "---- "+ errorThrown);
	});
}

function mySelectChannel(element) {
	 ;
	let tr = $(element).closest("tr");
	let name = $(tr).find("#name");
	if($(name).val() == "n/a"){
		let input = $('#channel input');
		$(tr).css('opacity', 0.5);
		$(tr).find('input').val('n/a');
		return;
	}

	let unit = $(tr).find("#unit");
	
	if ($(name).val() == 'Water Volume') {
		$(unit).val('cu m');
	} 
	else if ($(name).val() == 'Volume') {
		$(unit).val('l');
	}
	$(tr).css('opacity', 1);
}

function saveMbusConverterSettings(){
	$('#page').addClass('disabledmouse');
	let adapterName = $("#adapter").val();
	let addressId = $("#modbus #device_instanceid").val();
	let rows = $("#channel tbody tr");
	let dataSend = [];
	// let dataEdit = [];
	// let dataInsert = [] ;
	for(let i = 0  ; i < rows.length ; i ++){
		let setting = {};
		// Register
		let register = $(rows[i]).find("#register");
		if(register.length > 0){
			setting["register"] = $(register).val();
		}else{
			setting["register"] = "";
		}
		// Data Type
		let datatype = $(rows[i]).find("#data_type");
		if(datatype.length > 0){
			setting["data_type"] = $(datatype).val();
		}else{
			setting["data_type"] = "";
		}
		
		// Channel
		let channel = $(rows[i]).find("#channel");
		if(channel.length > 0){
			setting["channel"] = $(channel).val();
		}else{
			setting["channel"] = "";
		}
		
		// Name
		let name = $(rows[i]).find("#name");
		if(name.length > 0){
			setting["name"] = $(name).val();
		}else{
			setting["name"] = "";
		}
		
		// Unit
		let unit = $(rows[i]).find("#unit");
		if(unit.length > 0){
			setting["unit"] = $(unit).val();
		}else{
			setting["unit"] = "";
		}
		
		// Unit
		let multiplier = $(rows[i]).find("#multiplier");
		if(multiplier.length > 0){
			setting["multiplier"] = $(multiplier).val();
		}else{
			setting["multiplier"] = "";
		}
		
		// Consumption
		let hasconsumption = $(rows[i]).find("#hasconsumption");
		if(hasconsumption.length > 0){
			setting["hasconsumption"] = $(hasconsumption).is(":checked").toString();
		}else{
			setting["hasconsumption"] = "";
		}
		
		// Data Point
		let dataPoint = $(rows[i]).find("#data_point");
		if(dataPoint.length > 0){
			setting["data_point"] = $(dataPoint).val();
		}else{
			setting["data_point"] = "";
		}
		
		
		
		dataSend.push(setting);

	}
	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/savesettings',
		data: {
		'addressId': addressId,
		'adapterName': adapterName,
		'data': JSON.stringify(dataSend)
		},
		timeout: 30000, //in milliseconds
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
		$('#page').removeClass('disabledmouse');
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		$('#page').removeClass('disabledmouse');
		alert("No Response");
		console.log(textStatus + "---- "+ errorThrown);
	});
}

function readXDKSettings(row,data){
	let label = $(row).find("#label");
	$(label).val(data.label);
	
	let sensor = $(row).find("#sensor");
	$(sensor).val(data.sensor);
	
	let offset = $(row).find("#offset");
	$(offset).val(data.offset);
	
	let delta = $(row).find("#delta");
	$(delta).val(data.delta);

}


function readPhidGetSettings(row,data){
	let sensortype = $(row).find("#sensor_type");
	$(sensortype).val(data.sensor_type);
	
	let threshold = $(row).find("#threshold");
	$(threshold).val(data.threshold);
	
	let triggermin = $(row).find("#trigger_min");
	$(triggermin).val(data.trigger_min);
}

function readInePro(row, data){
	let data_scalar = $(row).find("#data_scalar");
	$(data_scalar).val(data.data_scalar);
}
function readEnergetix(row, data){
	let type = $(row).find("#type");
	$(type).val(data.type);
	
	let data_point = $(row).find("#data_point");
	$(data_point).val(data.data_point);
}

function readBrandChildSettings(row,data){
	let name = $(row).find("#name");
	$(name).val(data.name);
	
	let unit = $(row).find("#unit");
	$(unit).val(data.unit);
	
	let max = $(row).find("#max");
	$(max).val(data.max);
	
	let min = $(row).find("#min");
	$(min).val(data.min);
	
	let ratio = $(row).find("#ratio");
	$(ratio).val(data.ratio);
	
	let formula = $(row).find("#formula");
	$(formula).val(data.formula);
	
	let data_point = $(row).find("#data_point");
	$(data_point).val(data.data_point);
}

$(document).on("change","#record_num",function(){
	allIndex = [];
});

function showErrorForm(errors_data) {
	$("#status_update").remove();
	var _ul = document.createElement("ul");
	_ul.className="formerrors";
	_ul.id = "status_update";
	var _li = document.createElement("li");
	_li.innerHTML = "Can not save for Adapter Settings";
	_ul.appendChild(_li);
	for (var i = 0; i < errors_data.length; i++) {
		_li = document.createElement("li");
		_li.innerHTML = "&nbsp&nbsp"+errors_data[i];
		_ul.appendChild(_li);
	}
	$("#header_name").after(_ul);	
}

function showSuccessForm() {
	$("#status_update").remove();
	var _ul = document.createElement("ul");
	_ul.className="formsuccess";
	_ul.id = "status_update";
	var _li = document.createElement("li");
	_li.innerHTML = "Settings have been updated. Please restart adapter on <a href=\"#\" onclick=\"gotoAdapterSettingsPage();\">Adapter Settings</a> for changes to take effect.";
	_ul.appendChild(_li);
	$("#header_name").after(_ul);
}

function gotoAdapterSettingsPage() {
	location.href=appRoot + "/adaptersettings";
}

function saveConfigXDKtoSD() {
	
	$('#page').addClass('disabledmouse');
	let adapterName = $("#adapter").val();
	let addressId = $("#modbus #device_instanceid").val();
	let dataSend = [];
	
	let rows = $("#configuration tbody tr");
	for (let i = 0; i < rows.length ; i++) {
		let setting = {};
		
		let dataPoint =  $(rows[i]).find("#data_point");
		setting["data_point"] = $(dataPoint).val();
		
		let ssidname = $(rows[i]).find("#ssidname");
		if(ssidname.length > 0){
			setting["value"] = $(ssidname).val();
			setting["name"] = "ssidname";
			dataSend.push(setting);
		}
		
		let wifipass = $(rows[i]).find("#wifipass");
		if(wifipass.length > 0){
			setting["value"] = $(wifipass).val();
			setting["name"] = "wifipass";
			dataSend.push(setting);
		}
		
		let netmode = $(rows[i]).find("#netmode");
		if(netmode.length > 0){
			setting["value"] = $(netmode).val();
			setting["name"] = "netmode";
			dataSend.push(setting);
		}
		
		let staticip = $(rows[i]).find("#staticip");
		if(staticip.length > 0){
			setting["value"] = $(staticip).val();
			setting["name"] = "staticip";
			dataSend.push(setting);
		}
		
		let subnet = $(rows[i]).find("#subnet");
		if(subnet.length > 0){
			setting["value"] = $(subnet).val();
			setting["name"] = "subnet";
			dataSend.push(setting);
		}
		
		let gateway = $(rows[i]).find("#gateway");
		if(gateway.length > 0){
			setting["value"] = $(gateway).val();
			setting["name"] = "gateway";
			dataSend.push(setting);
		}
		
		let dns = $(rows[i]).find("#dns");
		if(dns.length > 0){
			setting["value"] = $(dns).val();
			setting["name"] = "dns";
			dataSend.push(setting);
		}
		
		let msgmode = $(rows[i]).find("#msgmode");
		if(msgmode.length > 0){
			setting["value"] = $(msgmode).val();
			setting["name"] = "msgmode";
			dataSend.push(setting);
		}
		
		let gatewayip = $(rows[i]).find("#gatewayip");
		if(gatewayip.length > 0){
			setting["value"] = $(gatewayip).val();
			setting["name"] = "gatewayip";
			dataSend.push(setting);
		}
		
		let gatewayport = $(rows[i]).find("#gatewayport");
		if(gatewayport.length > 0){
			setting["value"] = $(gatewayport).val();
			setting["name"] = "gatewayport";
			dataSend.push(setting);
		}
		
		let sendinterval = $(rows[i]).find("#sendinterval");
		if(sendinterval.length > 0){
			setting["value"] = $(sendinterval).val();
			setting["name"] = "sendinterval";
			dataSend.push(setting);
		}
	}
	debugger;


	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/exportingXDK',
		data: {
		'addressId': addressId,
		'adapterName': adapterName,
		'data': JSON.stringify(dataSend)
		},
		timeout: 30000, //in milliseconds
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
		window.location.href = appRoot + "/devicesettings?adapter=BoschAdapter&action=exportingXDK&deviceid=" + addressId ;
		$('#page').removeClass('disabledmouse');
	});
	
	request.fail(function (jqXHR, textStatus, errorThrown) {
		$('#page').removeClass('disabledmouse');
		alert("No Response");
		console.log(textStatus + "---- "+ errorThrown);
	});
	
}

function readConfigXDKfromSD() {
	let form = document.getElementById("uploadform");
	form.action = "devicesettings/importingXDK";
	
	localStorage.setItem("current_adapter", "BoschAdapter");
	localStorage.setItem("device_id", $("#modbus #device_instanceid").val());
	localStorage.setItem("current_tab", "config");
	localStorage.setItem("action", "import");
	
	$("#file").trigger("click");
}

function submitForm(){
	$("#uploadform").submit();
	alert("Upload file success");
}

function disableInputFields() {
	$('.staticip').prop('disabled', true);
	$('.staticip').addClass('ui-state-disabled');
}

function enableInputFields() {
	$('.staticip').prop('disabled', false);
	$('.staticip').removeClass('ui-state-disabled');
}


function showSuccessFormXDK() {
	$("#status_update").remove();
	var _ul = document.createElement("ul");
	_ul.className="formsuccess";
	_ul.id = "status_update";
	var _li = document.createElement("li");
	_li.innerHTML = "Settings have been updated.";
	_ul.appendChild(_li);
	$("#header_name").after(_ul);
}

function showErrorFormXDK(errors_data) {
	$("#status_update").remove();
	var _ul = document.createElement("ul");
	_ul.className="formerrors";
	_ul.id = "status_update";
	var _li = document.createElement("li");
	_li.innerHTML = "Can not save for Settings";
	_ul.appendChild(_li);
	for (var i = 0; i < errors_data.length; i++) {
		_li = document.createElement("li");
		_li.innerHTML = "&nbsp&nbsp"+errors_data[i];
		_ul.appendChild(_li);
	}
	$("#header_name").after(_ul);	
}

$(window).on('unload', function(e) {
	if (localStorage.action == "import") {
		//localStorage.removeItem("action");
	} else {
		if(localStorage.current_tab) {
			localStorage.removeItem("current_tab");
		} 
	}
	
});

function setNDecimalPoint(pointer , num) {
	let int_n = parseInt(num);
	if (pointer.value == "") {
		pointer.value = parseFloat("0").toFixed(int_n);
	}
}
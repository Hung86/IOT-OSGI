var all_adapter;
var cur_adapter;
var cur_gwid;
var currentDeviceConfigData ;
var tab = "";
var active_tab = "scanning" ;
var type_bacnet = "";

var nameList = [
'Power','Energy','Active Energy','Reactive Energy','Apparent Energy','Active Power','Reactive Power',
'Apparent Power','Peak Demand','Voltage','Voltage L1-L2','Voltage L2-L3','Voltage L1-L3','Current',
'Power Factor','Frequency','Active Energy Reading','Reactive Energy Reading','Apparent Energy Reading',
'Regenerated Energy Reading','Regenerated Energy','Regenerated Power','Relay Status','Analog Output',
'Running Hours Lamp','Lamp Failure','Lost Communication','Node Failure','Light Level','Motion Switch',
'Temperature','Flow Rate','VSD Speed','Pressure','Efficiency','CHWP Efficiency','CDWP Efficiency',
'CT Efficiency','Cooling Load','Cooling Load RTh','Humidity','Heat Balance','Water Volume','Water Volume Reading',
'Cooling Load kWc','COP','Utilization','Active Power Device','Cooling Consumption Reading','Cooling Consumption',
'Load Phase','Trip Status','Alarm Status','Battery Percentage','Battery Time','Peak Current','PUE',
'UPS Efficiency','Luminosity','Noise','Dust','Lux','Input Voltage L1-N','Input Voltage L2-N','Input Voltage L3-N',
'Input Current L1','Input Current L2','Input Current L3','Input Frequency','Bypass Voltage L1-N','Bypass Voltage L2-N',
'Bypass Voltage L3-N','Bypass Frequency','Output Voltage L1-N','Output Voltage L2-N','Output Voltage L3-N',
'Output Current L1','Output Current L2','Output Current L3','Output Peak Current L1','Output Peak Current L2',
'Output Peak Current L3','Load Phase L1','Load Phase L2','Load Phase L3','Output Active Power L1',
'Output Active Power L2','Output Active Power L3','Output Frequency','Battery Voltage','Positive Battery Voltage',
'Negative Battery Voltage','Battery Current','Remain Battery Capacity','Remain Battery Time','Total Output Energy',
'Internal UPS Temperature','Sensor 1 Temperature','Sensor 2 Temperature','Total Output Active Power',
'Average Load Phase','W/sqm','Operation Time','Generated Energy Reading','Generated Energy','Generated Power',
'Raw Sensor Value','Max Sensor Value','Occupant Status','Occupant Count','Event Count','Occupant In',
'Occupant Out','CH Water Consumption RTh','BTU Meter Reading','CH Water Consumption','CH Supply Temp',
'CH Return Temp','CH Temp Diff','Temperature Set point','Supply Air Temp','Return Air Temp','Humidity Set point',
'Equipment Status','Oxygen','pH','Conductivity','Water Velocity','Volume Reading','Volume','Diesel Efficiency',
'Diesel Plant Efficiency','Diesel Efficiency lkWh','Heating Consumption Reading','Heating Consumption',
'CHW Volume Reading','CHW Volume','CHW Flow Rate','CHWR Temp','CHWS Temp','Input Voltage L1-L2','Input Voltage L2-L3',
'Input Voltage L1-L3','Output Reactive Power L1','Output Reactive Power L2','Output Reactive Power L3',
'Total Output Reactive Power','Output Apparent Power L1','Output Apparent Power L2','Output Apparent Power L3',
'Total Output Apparent Power','Battery Temperature','Battery Aging Coefficient','Sensor 1 Humidity',
'Sensor 2 Humidity','Vibration','Sound','ORP','Diesel Flow Rate','Generated Reactive Energy Reading',
'Generated Apparent Energy Reading','Generated Reactive Energy','Generated Apparent Energy','Generated Reactive Power',
'Generated Apparent Power','Active Client Count','CDW Flow','CD Flow Imperial','Pressure Imperial'];
	
var listUnit = ['W','Wh','VARh','VAh','kW','kVAR','kVA','V','A','None','Hz','kWh','kVARh','kVAh','C','l/s','%','kPa',
                'ikW/RT','kW/m3/s','l/s/kW','RT','RTh','cu m','kWc','cop','min','dBA','mg/m3','lux','W/sqm','minute',
                'mg/L','pH','mS/cm','m/s','l','l/kW','l/kWh','dB','l/s/RT','gpm/RT','bar'];

var deviceNameConvert = {
  '1012': 'PM-200',
  '1016': 'EM-300-6',
  '1009': 'EM-101',
  '1002': 'EM-100',
  '1006': 'PM-100P',
  '1011': 'EM-200',
  '7001': 'BL',
  '3000': 'DI-16',
  '3001': 'AI-08',
  '1014': 'UMG-96S',
  '7020': 'PFLOW_CA20',
  '1013': 'MPR-46S',
  '5001': 'BTU-212',
  '7026': 'SL1168',
  '1060': 'COUNTISE44',
  '8001': 'MTCOM_30X',
  '7100': 'SWG',
  '7021': 'AT600',
  '5002': 'FUE-950',
  '2000': 'TSA-01',
  '2001': 'TSA-01',
  '2002': 'TSA-02',
  '2003': 'TST-02',
  '2004': 'LS-01',
  '2010': 'IK888',
  '1003': 'PM-100',
  '10000' : 'Trane',
  '1021'  : 'PM710',
  '7030'  : 'Val-Technik',
  '7031' : 'ADFWeb-HD67029M',
  '1025': 'PRO-1250D',
  '5003': 'MAG-6000'
}

var deviceChannel = {
  '3000': [
    'measurename',
    'ratio',
    'unit'
  ],
  '3001': [
    'measurename',
    'max',
    'unit',
    'min'
  ],
  '2010' :[
   'sensortype',
   'threshold',
   'triggermin'
  ]
};
var dataWrite = [
];
var dataBeforWrite = {};
$(document).ready(function () {
  $(document).off('ajaxError'); //Disale global event in support.js of felix webconsole
  $.get(pluginRoot, {
    'id': 'adapters'
  }, function (data) {
	debugger ;
	cur_gwid = data.gwid;
    all_adapter = data.gwdata;
	let list_adapter_name = Object.keys(all_adapter);
    renderDeviceList(all_adapter);
  }, 'json');
});
function convertAdapterName2UserName(adapterName) {
  var userName;
  if ((adapterName === 'GentosAdapter') || (adapterName === 'gentos')) {
    userName = 'Gentos';
  } else if ((adapterName === 'EmersonAdapter') || (adapterName === 'emerson')) {
    userName = 'Emerson';
  } else if ((adapterName === 'EntesAdapter') || (adapterName === 'entes')) {
    userName = 'Entes';
  } else if ((adapterName === 'EnergetixPowerMeterAdapter') || (adapterName === 'powermeter')) {
    userName = 'Energetix Power Meter';
  } else if ((adapterName === 'BrainchildAdapter') || (adapterName === 'brainchild')) {
    userName = 'Brainchild';
  } else if ((adapterName === 'NgeeAnnPolyAdapter') || (adapterName === 'poly')) {
    userName = 'Ngee Ann Poly';
  } else if ((adapterName === 'JanitzaAdapter') || (adapterName === 'janitza')) {
    userName = 'Janitza';
  } else if ((adapterName === 'ContrecAdapter') || (adapterName === 'contrec')) {
    userName = 'Contrec';
  } else if ((adapterName === 'RielloAdapter') || (adapterName === 'riello')) {
    userName = 'Riello';
  } else if ((adapterName === 'EnergetixSensorAdapter') || (adapterName === 'sensor')) {
    userName = 'Energetix Sensor';
  } else if ((adapterName === 'GeAquatransAdapter') || (adapterName === 'aquatrans')) {
    userName = 'GE';
  } else if ((adapterName === 'BaylanAdapter') || (adapterName === 'baylan')) {
    userName = 'Baylan';
  } else if ((adapterName === 'SiemensAdapter') || (adapterName === 'siemens')) {
    userName = 'Siemens';
  } else if ((adapterName === 'DentAdapter') || (adapterName === 'dent')) {
    userName = 'Dent';
  } else if ((adapterName === 'PhidgetsAdapter') || (adapterName === 'phidgets')) {
    userName = 'Phidgets';
  } else if ((adapterName === 'SitelabAdapter') || (adapterName === 'sitelab')) {
    userName = 'Sitelab';
  } else if ((adapterName === 'BacnetAdapter') || (adapterName === 'trane')) {
    userName = 'Bacnet Generic';
  } else if ((adapterName === 'SocomecAdapter') || (adapterName === 'socomec')) {
    userName = 'Socomec';
  } else if ((adapterName === 'BoschAdapter') || (adapterName === 'bosch')) {
    userName = 'Bosch XDK';
  }else if ((adapterName === "MbustoModbusConverterAdapter") || (adapterName === "mbustomodbus")) {
		userName = "Mbus to Modbus Converter";
  }else if ((adapterName === "OpcAdapter") || (adapterName === "opcadapter")) {
		userName = "Opc Generic";
  }  
  else {
    userName = adapterName;
  }
  return userName;
}

var device_name = "" ;
function renderDeviceList(data) {
	let list_adapter_name = Object.keys(data);
	if(list_adapter_name.length == 0)
		return ;
	// load script by adapter name
	let adapterName = list_adapter_name[0] ;
	loadScript(adapterName);
	
	renderDevice(data,adapterName);
	$.each(data,function(key,value){
		var option = $("<option></option>").val(key).text(convertAdapterName2UserName(key));
		$("#adapter").append(option);
	});
	
	if (localStorage.current_adapter) {
		$("#adapter").val(localStorage.current_adapter).trigger("change");
		localStorage.removeItem("current_adapter");
		debugger;
	} else {
		$("#adapter").prop('selectedIndex', 1);
		$("#adapter").trigger("change");
	}

	
	
}


function gotoStatusPage() {
  location.href = appRoot + '/gatewaystatus';
}

function readConfig() {
	console.trace();
	debugger
	var addressId = $("#modbus #device_instanceid").val();
	var adapterName = $("#adapter").val();
	$('.show-writting-status').hide();
	$('#page').addClass('disabledmouse');

	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/readingconfiguration',
		data: {
		'addressId': addressId,
		'force': force,
		'adapterName': adapterName
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
	request.done(function (currentDeviceConfigData) {
		// enable write button
		if(Object.keys(currentDeviceConfigData).length != 0){
			$("#writingbutton").prop('disabled', false);
			$("#writingbutton").css("opacity",1);
		}
		dataBeforWrite = currentDeviceConfigData;
		$.each(currentDeviceConfigData, function (key, value) {
			$('#' + key).prop('value',value);
		});
		$('#page').removeClass('disabledmouse');
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		alert("No Response");
		$('#page').removeClass('disabledmouse');
		console.log(textStatus + "---- "+ errorThrown);
		
	});
}
function writeConfig() {
	let wroteData = {
	};

	let addressId = $("#modbus #device_instanceid").val();
	let adapterName = $("#adapter").val();
	
	debugger
	let elementAfterModify = getDataOfDevice();
	$.each(elementAfterModify,function(key,value){
		$.each(dataBeforWrite,function(index,val){
			if(key != index)
				return ;
			if(value != val){
				wroteData[key] = $.trim(value) ;
			}
		  
		})
	  
	});
	if(!$.isEmptyObject(reset)){
	  $.each(reset,function(key,item){
		  wroteData[key] = $.trim(item) ;
		})
	}
	debugger ;
	if(Object.keys(wroteData).length == 0){
		alert("Nothing to write");
		return ;
	}
	debugger ;
	callAjaxWriteData(wroteData,addressId,adapterName);
}


var addressReset = {
	"1006" : {
		"4065" : "1234"
	},
	"7001" : {
		"2"    : "0"
	}
}

function resetData(){
	var writeData = {};
	var addressId = $("#modbus device_instanceid").val();
	var adapterName = $("#adapter").val();
	var category = getCategory(objectRead.index,adapterName);
	
	$.each(addressReset,function(index, value){
		if(index == category){
			$.each(value,function(indexItem, valueItem){
				writeData[indexItem] = $.trim(valueItem) ;	
			});
		}
	});
	callAjaxWriteData(writeData,addressId,adapterName);
}

function callAjaxWriteData(wroteData,addressId,adapterName){
	debugger
	$('#page').addClass('disabledmouse');
	var request = $.ajax({
		type : "post",
		url: pluginRoot + '/writtingconfiguration',
		data: {
			"wroteData" : JSON.stringify(wroteData),
			"adapterName" : adapterName,
			"addressId" : addressId
		},
		timeout: 15000, // in milliseconds
		dataType : "json",
		async: 'false',
		statusCode: {
			400: function () {
			},
			404: function () {
				location.replace(''); // clear the page content
			},
			408: function () {
			}
		}
	});
	
	request.done(function (data) {
		var success = $(".success");
		for(var i = 0 ; i < success.length;i++){
			$(success[i]).remove();
		}
		
		var fail = $(".fail");
		for(var i = 0 ; i < fail.length; i++){
			$(fail[i]).remove();
		}
			
		$.each(data, function (key, value) {
			$('.show-writting-status').show();
			$('#index_' + key).show();
			if (value === '1') {
				dataBeforWrite[key] = wroteData[key];
			  $('#index_' + key).html('<label class="success" style="color:green">Successed</label>');
			} else if (typeof value !== "undefined" || value === '0'){
			  $('#index_' + key).html('<label class = "fail" style="color:red">Failed</label>');
			}
		});
		$('#page').removeClass('disabledmouse');
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		alert("No Response");
		$('#page').removeClass('disabledmouse');
		console.log(textStatus + "---- "+ errorThrown);
		
	});
}


function convertNameCategory(CategoryId) {
  var convert = String(CategoryId);
  var result = deviceNameConvert[convert];
  if (typeof result === 'undefined')
	return 'N/A';
  return result;
}
var objectRead = {
};
var force = '';


function buildHtml(channels,itemIndex){
	var arrayChannelNumber = [];
	$.each(channels,function(key,item){
		var listChannel = key.split("_");
		var numberChannel = listChannel[2];
		arrayChannelNumber.push(parseInt(numberChannel));
	});
	
	var uniqueNames = [];
	$.each(arrayChannelNumber, function(i, el){
		if($.inArray(el, uniqueNames) === -1)
			uniqueNames.push(el);
	});
	var largest = Math.max.apply(Math, uniqueNames);
	
	for(var i = 0 ; i <= largest; i++){
		addChannel();
		var arrayDataPoint = [];
		$.each(channels,function(key,value){
			if(key.indexOf("device"+"_"+itemIndex+"_"+ i +"_"+"0") >= 0){
				arrayDataPoint.push(key);
			}
		});
		if(arrayDataPoint.length == 0)
			continue ;
		
		var arrayNumberDataPoint = [];
		$.each(arrayDataPoint,function(key,item){
			var listDataPoint = item.split("_");
			var numberDataPoint = listDataPoint[4];
			arrayNumberDataPoint.push(parseInt(numberDataPoint));
		});
		
		
		var uniqueNamesDataPoint = [];
		$.each(arrayNumberDataPoint, function(i, el){
			if($.inArray(el, uniqueNamesDataPoint) === -1)
				uniqueNamesDataPoint.push(el);
		});
		var largestDataPoint = Math.max.apply(Math, uniqueNamesDataPoint);
		var divNode = $(document.getElementById("add_datapoint"+"_"+i)).closest("tr");
		for(var j = 0 ; j<= largestDataPoint ; j++){
			addDataPointToChannel(i,divNode);
		}
	}
}



$('#channel select').change(function () {
	debugger  ;
	if ($(this).val() == 'n/a') {
		$(this).closest('tr').css('opacity', 0.5);
		$(this).closest('tr').find('input').val('n/a');
		return;
	}
	var input = $('#device_' + index + '_' + id + '_0_0_unit');
	if ($(this).val() == 'Water Volume') {
		$(input).val('cu m');
	} 
	else if ($(this).val() == 'Volume') {
		$(input).val('l');
	}
	$(this).closest('tr').css('opacity', 1);
});

function getDeviceFromAllAdapter(adapterName) {
	for (var i in all_adapter) {
		if (all_adapter[i]['name'] == adapterName) {
			return all_adapter[i]; // object Devices
		}
	}
}
function getDeviceFromIndex(index, adapterName) {
	var objectDevice = getDeviceFromAllAdapter(adapterName);
	var devices = objectDevice.devices;
	return devices.device[index];
}
function getChannelFromIndex(index, adapterName) {
	var device = getDeviceFromIndex(index, adapterName);
	return device.channels;
}


	
	function getDataOfDevice(){
		var dataRender = {};
		var input = $("#configuration input, #configuration select");
		for(var i = 0 ; i < input.length;i++){
			dataRender[input[i].id] = $(input[i]).prop("value");
		}
		return dataRender ;
	}
	
	
	var reset = {};
	$(document).on ("change", "#configuration select", function () {
        if($(this).val() != "null"){
			$(this).closest('tr').css('opacity', 1);
			reset[this.id] = $(this).val();
		}else {
			$(this).closest('tr').css('opacity', 0.5);
		}
    });
	
	$(document).on("change","#configuration input",function(){
		$(".max").remove();
		$(".min").remove();
		
		var value = $(this).val();
		var max   = $(this).attr("max");
		var min   = $(this).attr("min");
		if(parseInt(value) > parseInt(max) || parseInt(value) < parseInt(min)){
			alert("Max value is "+max+", Min value is "+min+"");
			return;
		}
	});
	
	function resetValue (element){
		if(!confirm("Are you want to "+ $(element).html()))
			return ;
		var writeData = {};
		
		let addressId = $("#modbus #device_instanceid").val();
		let adapterName = $("#adapter").val();
		let valueReset = $(element).val();
	
		writeData[element.id] = valueReset ;
		
		callAjaxWriteData(writeData,addressId,adapterName);
	};
	
	
	function addDataPointToChannel(data){
		let tbody = $("#channel table tbody") ;
		let tr = $("<tr></tr>");
		// Data Point
		let dataPoint =  document.createElement("input");
		dataPoint.setAttribute("type", "hidden");
		dataPoint.setAttribute("id","data_point");
		if(typeof data !== "undefined"){
			dataPoint.setAttribute("value", data.data_point);
		}
		tr.append(dataPoint);
		
		// Register
		var td = $("<td></td>").css("border","0");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   : "register"
		}).css("width","100%").addClass("ui-corner-all inputText");
		if(typeof data !== "undefined"){
			$(input).val(data.register);
		}
		td.append(input);
		tr.append(td);
		
		// Data Type
		var listDataType = ["short","ushort","int","uint","float"];
		var td = $("<td></td>").css("border","0");
		var select = $("<select></select>").attr({
			"type" : "text",
			"id"   : "data_type"
		}).css("width","100%").addClass("ui-corner-all inputText");
		
		for(var i in listDataType){
			var option = $("<option></option>").text(listDataType[i]).html(listDataType[i]);
			select.append(option);
		}
		if(typeof data !== "undefined"){
			$(select).val(data.data_type);
		}
		td.append(select);
		tr.append(td);
		
		// Channel
		var td = $("<td></td>").css("border","0");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   : "channel"
		}).css("width","100%").addClass("ui-corner-all inputText");
		if(typeof data !== "undefined"){
			$(input).val(data.channel);
		}
		td.append(input);
		tr.append(td);
		
	
		var td = $("<td></td>").css("border","0");
		nameList.sort();
		var select = $("<select></select>").attr({
			"type" : "text",
			"id"   : "name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.change(function(){
			changeUnit(this);
		});
		for(var i in nameList){
			var option = $("<option></option>").val(nameList[i]).html(nameList[i]);
			select.append(option);
		}
		if(typeof data !== "undefined"){
			$(select).val(data.name);
		}
		td.append(select);
		tr.append(td);
		
		
		var td = $("<td></td>").css("border","0");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   : "unit",
			"readonly" : "true"
		}).css("width","100%").addClass("ui-corner-all inputText").val("kWh");
		if(typeof data !== "undefined"){
			$(input).val(data.unit);
		}
		td.append(input);
		tr.append(td);
		
		var td = $("<td></td>").css("border","0");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   : "multiplier"
		}).css("width","100%").addClass("ui-corner-all inputText");
		if(typeof data !== "undefined"){
			$(input).val(data.multiplier);
		}
		td.append(input);
		tr.append(td);
		
		var comsumnameList = ["Active Energy","Reactive Energy","Apparent Energy","Regenerated Energy","Cooling Consumption"
		,"Heating Consumption","Water Volume","Diesel Volume"];
		
		var td = $("<td></td>").css({
			"border" : "0", 
			"text-align": "center"
		});
		
		var x = document.createElement("INPUT");
		x.setAttribute("type", "checkbox");
		x.defaultValue = "false";
		x.id = "hasconsumption" ;
		x.className = "ui-corner-all" ;
		if(typeof data !== "undefined"){
			if(data.consumption == "true"){
				x.checked = true ;
			}
			else
				x.checked = false ;
		}
		// x.defaultChecked = "false";
		
		td.append(x);
		tr.append(td);
		
		var td = $("<td></td>").css("border","0");
		var button = $("<button></button>").attr({
			"type" : "button",
			"title" : "Remove",
			
		}).addClass("ui-corner-all ui-state-default")
		.append($("<span></span>").addClass("ui-icon ui-icon-trash"))
		.click(function(){
			let tr = $(this).closest("tr") ;
			$(tr).remove() ;
		});
		td.append(button);
		tr.append(td);

		$(tbody).append(tr) ;
		
	}
	var nameAndUnit = {
	'Power':'W',
	'Energy':'Wh',
	'Active Energy':'Wh',
	'Reactive Energy':'VARh',
	'Apparent Energy':'VAh',
	'Active Power':'kW',
	'Reactive Power':'kVAR',
	'Apparent Power':'kVA',
	'Peak Demand':'kW',
	'Voltage':'V',
	'Voltage L1-L2':'V',
	'Voltage L2-L3':'V',
	'Voltage L1-L3':'V',
	'Current':'A',
	'Power Factor':'None',
	'Frequency':'Hz',
	'Active Energy Reading':'kWh',
	'Reactive Energy Reading':'kVARh',
	'Apparent Energy Reading':'kVAh',
	'Regenerated Energy Reading':'kWh',
	'Regenerated Energy':'Wh',
	'Regenerated Power':'kW',
	'Relay Status':'None',
	'Analog Output':'None',
	'Running Hours Lamp':'None',
	'Lamp Failure':'None',
	'Lost Communication':'None',
	'Node Failure':'None',
	'Light Level':'None',
	'Motion Switch':'None',
	'Temperature':'C',
	'Flow Rate':'l/s',
	'VSD Speed':'%',
	'Pressure':'kPa',
	'Efficiency':'ikW/RT',
	'CHWP Efficiency':'kW/m3/s',
	'CDWP Efficiency':'kW/m3/s',
	'CT Efficiency':'l/s/kW',
	'Cooling Load':'RT',
	'Cooling Load RTh':'RTh',
	'Humidity':'%',
	'Heat Balance':'%',
	'Water Volume':'cu m',
	'Water Volume Reading':'cu m',
	'Cooling Load kWc':'kWc',
	'COP':'cop',
	'Utilization':'%',
	'Active Power Device':'kW',
	'Cooling Consumption Reading':'kWh',
	'Cooling Consumption':'kWh',
	'Load Phase':'%',
	'Trip Status':'None',
	'Alarm Status':'None',
	'Battery Percentage':'%',
	'Battery Time':'min',
	'Peak Current':'A',
	'PUE':'None',
	'UPS Efficiency':'%',
	'Luminosity':'%',
	'Noise':'dBA',
	'Dust':'mg/m3',
	'Lux':'lux',
	'Input Voltage L1-N':'V',
	'Input Voltage L2-N':'V',
	'Input Voltage L3-N':'V',
	'Input Current L1':'A',
	'Input Current L2':'A',
	'Input Current L3':'A',
	'Input Frequency':'Hz',
	'Bypass Voltage L1-N':'V',
	'Bypass Voltage L2-N':'V',
	'Bypass Voltage L3-N':'V',
	'Bypass Frequency':'Hz',
	'Output Voltage L1-N':'V',
	'Output Voltage L2-N':'V',
	'Output Voltage L3-N':'V',
	'Output Current L1':'A',
	'Output Current L2':'A',
	'Output Current L3':'A',
	'Output Peak Current L1':'A',
	'Output Peak Current L2':'A',
	'Output Peak Current L3':'A',
	'Load Phase L1':'%',
	'Load Phase L2':'%',
	'Load Phase L3':'%',
	'Output Active Power L1':'kW',
	'Output Active Power L2':'kW',
	'Output Active Power L3':'kW',
	'Output Frequency':'Hz',
	'Battery Voltage':'V',
	'Positive Battery Voltage':'V',
	'Negative Battery Voltage':'V',
	'Battery Current':'A',
	'Remain Battery Capacity':'%',
	'Remain Battery Time':'min',
	'Total Output Energy':'kWh',
	'Internal UPS Temperature':'C',
	'Sensor 1 Temperature':'C',
	'Sensor 2 Temperature':'C',
	'Total Output Active Power':'kW',
	'Average Load Phase':'%',
	'W/sqm':'W/sqm',
	'Operation Time':'minute',
	'Generated Energy Reading':'kWh',
	'Generated Energy':'Wh',
	'Generated Power':'kW',
	'Raw Sensor Value':'None',
	'Max Sensor Value':'None',
	'Occupant Status':'None',
	'Occupant Count':'None',
	'Event Count':'None',
	'Occupant In':'None',
	'Occupant Out':'None',
	'CH Water Consumption RTh':'RTh',
	'BTU Meter Reading':'kWh',
	'CH Water Consumption':'Wh',
	'CH Supply Temp':'C',
	'CH Return Temp':'C',
	'CH Temp Diff':'C',
	'Temperature Set point':'C',
	'Supply Air Temp':'C',
	'Return Air Temp':'C',
	'Humidity Set point':'%',
	'Equipment Status':'None',
	'Oxygen':'mg/L',
	'pH':'pH',
	'Conductivity':'mS/cm',
	'Water Velocity':'m/s',
	'Volume Reading':'l',
	'Volume':'l',
	'Diesel Efficiency':'l/kW',
	'Diesel Plant Efficiency':'l/kWh',
	'Diesel Efficiency lkWh':'l/kWh',
	'Heating Consumption Reading':'kWh',
	'Heating Consumption':'kWh',
	'CHW Volume Reading':'l',
	'CHW Volume':'l',
	'CHW Flow Rate':'l/s',
	'CHWR Temp':'C',
	'CHWS Temp':'C',
	'Input Voltage L1-L2':'V',
	'Input Voltage L2-L3':'V',
	'Input Voltage L1-L3':'V',
	'Output Reactive Power L1':'kVAR',
	'Output Reactive Power L2':'kVAR',
	'Output Reactive Power L3':'kVAR',
	'Total Output Reactive Power':'kVAR',
	'Output Apparent Power L1':'kVA',
	'Output Apparent Power L2':'kVA',
	'Output Apparent Power L3':'kVA',
	'Total Output Apparent Power':'kVA',
	'Battery Temperature':'C',
	'Battery Aging Coefficient':'None',
	'Sensor 1 Humidity':'%',
	'Sensor 2 Humidity':'%',
	'Vibration':'None',
	'Sound':'dB',
	'ORP':'V',
	'Diesel Flow Rate':'l/s',
	'Generated Reactive Energy Reading':'kVARh',
	'Generated Apparent Energy Reading':'kVAh',
	'Generated Reactive Energy':'VARh',
	'Generated Apparent Energy':'VAh',
	'Generated Reactive Power':'kVAR',
	'Generated Apparent Power':'kVA',
	'Active Client Count':'None',
	'CDW Flow':'l/s/RT',
	'CD Flow Imperial':'gpm/RT',
	'Pressure Imperial':'bar'
	}
	function changeUnit(element){
		$.each(nameAndUnit,function(key,value){
			if($(element).val() == key){
				let tr = $(element).closest("tr");
				let unit = $(tr).find("#unit");
				$(unit).val(value);
			}
		})
	}
	
	function changeUnitBacNet(element){
		debugger
		let tr = $(element).closest("tr");
		let unit = $(tr).find("#measure_unit");
		let unitValue = nameAndUnit[element.value];
		$(unit).val(unitValue);
	}
	
	function writeConfigMbusConverter (){

		var wroteData = {};
		var data = $("#device input, #device input:hidden,#device select");
		for(var i = 0 ; i< data.length ; i ++){
			if(data[i].value != ""){
				wroteData[data[i].id] = $.trim(data[i].value );
			}
		}
					
		var validate = validation(wroteData);
		if(validate !== ""){
			$("#"+ validate).css('border-color', 'red');
			return ;
		}
		wroteData['index'] = objectRead.index;
		wroteData['devAddress'] = objectRead.addressId;;
		wroteData['currentAdapter'] =  objectRead.adapterName;
	
		
		$('#page').addClass('disabledmouse');
		var request = $.ajax({
			type : "post",
			url: pluginRoot + '/writtingconfiguration',
			data: wroteData,
			timeout: 15000, // in milliseconds
			dataType : "json",
			async: 'false',
			statusCode: {
				400: function () {
				},
				404: function () {
					location.replace(''); // clear the page content
				},
				408: function () {
				}
			}
		});
		
		request.done(function (data) {
			var success = $(".success");
			for(var i = 0 ; i < success.length;i++){
				$(success[i]).remove();
			}
			
			var fail = $(".fail");
			for(var i = 0 ; i < fail.length; i++){
				$(fail[i]).remove();
			}
				
			$.each(data, function (key, value) {
				$('.show-writting-status').show();
				$('#index_' + key).show();
				if (value === '1') {
					dataBeforWrite[key] = wroteData[key];
				  $('#index_' + key).html('<label class="success" style="color:green">Successed</label>');
				} else if (typeof value !== "undefined" || value === '0'){
				  $('#index_' + key).html('<label class = "fail" style="color:red">Failed</label>');
				}
			});
			$('#page').removeClass('disabledmouse');
			var category = getCategory(objectRead.index,objectRead.adapterName);
			goToDeviceSettings(objectRead.addressId, category, objectRead.adapterName);
		});
		request.fail(function (jqXHR, textStatus, errorThrown) {
			alert("No Response");
			$('#page').removeClass('disabledmouse');
			console.log(textStatus + "---- "+ errorThrown);
			
		});		
		// callAjaxWriteData(wroteData);
		
	}
	
	function validation(wroteData){
		for(var key in wroteData){
			if(key.indexOf("register") >= 0){
				if(parseInt(wroteData[key]) < 0){
					alert("Register can not positive");
					return key;
				}
			}
		}
		return "" ;
	}
	
	function deleteChannel(index){
		var divChannel = $("#template_html"+ "_" + index) ;
		divChannel.remove();
		$.each(consume,function(item, value){
			if(item.indexOf("device"+ "_"+ objectRead.index + "_" + index) >= 0){
				delete consume[item] ;
			}
		});
	}
	function writeConfigInepro(){
		var wroteData = {} ;
		
		let addressId = $("#device_instanceid").val();
		let adapterName = $("#adapter").val();
		
		var data = $("#device input, #device select");
		for(var i = 0 ; i< data.length ; i ++){
			wroteData[data[i].id] = $.trim($(data[i]).val());
		}
		callAjaxWriteData(wroteData,addressId,adapterName);
	}
	
	function loadScript(adapterName){
		let path =  "" ;
		if(adapterName ==  "BacnetAdapter"){
			path = "/system/console/devicesettings/js/devicesettings/bacnet.js?format=js"
		}else if(adapterName ==  "OpcuaAdapter"){
			path = "/system/console/devicesettings/js/devicesettings/opc_ua.js?format=js"
		}
		else{
			path = "/system/console/devicesettings/js/devicesettings/modbus.js?format=js"
		}
		$.ajax({
				type: "GET",
				url: path,
				success: function(data){
				    
				},
				error: function(xhr, textStatus, errorThrown){
					  // console.log(arguments);
						alert('HTTP Error: '+errorThrown+' | Error Message: '+textStatus);
					// return;
				},
				dataType: "script",
				cache: false,
				async :false
		});
	}
	
	$(document).on("change","#adapter",function(){
		loadScript($("#adapter").val());
		onchangeAdapter();
	});
	
	$(document).on("change","#device_address",function(){
		onchangeDeviceAddress();
	});
	
	$(document).on("change","#device_instanceid",function(){
		onchangeDeviceInstance();
	});
	function getInstanceId(adapterName){
		let result = [] ;
		let adapter = all_adapter[adapterName]
		for(let i = 0 ; i < adapter.length ; i++){
			result.push(adapter[i].device_instanceid);
		}
	return result ;
}


$(document).on("click","#validation_node",function(){
		$("#real").hide();
		$("#virtual").hide();
		$("#validation_show").show();
		$(".div_validation").hide();
		$("#save").hide();
		$("#scan").hide();
		
		$("#virtual_node").removeClass("active");
		$("#real_node").removeClass("active");
		$("#validation_node").addClass("active");
		localStorage.setItem("tab", "validation_node");
		
		var request = $.ajax({
			url: pluginRoot,
			data: {
				'action' : 'validation_show',
				'device_address': $("#device_address").val(),
				'device_instanceid': $("#device_instanceid").val(),
				'adapter_name' : $("#adapter").val()
			},
			timeout: 15000, // in milliseconds
			dataType: 'json',
			async: 'true',
			type : 'get',
			statusCode: {
			400: function () {
			},
			404: function () {
				location.replace(''); // clear the page content
			},
			408: function () {
			}
			}
		});
		request.done(function (data) {
			debugger ;
			$("#validation_show tbody").empty();
			for(let i  in data ){
				let tr = $("<tr></tr>");
				
				let id = document.createElement("input");
				id.id = "id";
				id.setAttribute("type", "hidden");
				
				id.setAttribute("value", data[i].id);
				$(tr).append(id);
				
				td =  $("<td></td>").css("text-align","left");
				let label = $("<label></label>");
				$(label).html(data[i].name) ;
				$(td).append(label);
				$(tr).append(td);
				
				td =  $("<td></td>").css("text-align","left");
				label = $("<label></label>");
				$(label).html(data[i].condition) ;
				$(td).append(label);
				$(tr).append(td);
				
				td =  $("<td></td>").css("text-align","left");;
				label = $("<label></label>");
				$(label).html(data[i].action );
				// label.innerHTML = data[i].action ;
				$(td).append(label);
				$(tr).append(td);
				
				td =  $("<td></td>").css("text-align","left");
				var editButton = document.createElement('BUTTON');
				editButton.className = "ui-state-default ui-corner-all"
				editButton.innerHTML = "<span class='ui-icon  ui-icon-pencil'> </span>";
				editButton.type = "button";
				editButton.id   = "validation_edit";
				editButton.onclick = function(){
					editValidationRule(data[i].id);
				}
				td.append(editButton);
				tr.append(td);
				
				var td = $("<td></td>").css("text-align","left");
				var delButton = document.createElement('BUTTON');
				delButton.className = "ui-state-default ui-corner-all"
				delButton.innerHTML = "<span class='ui-icon ui-icon-trash'> </span>";
				delButton.type = "button";
				delButton.id   = "validation_delete";
				let row = tr ;
				delButton.onclick = function(){
					deleteValidation(row);
				}
				td.append(delButton);
				tr.append(td);
				
				$("#validation_show tbody").append(tr);
			}
		});
		request.fail(function (jqXHR, textStatus, errorThrown) {
		});
		
	});
	$(document).on("click","#cancel_virtual",function(){
		$("#create").show();
		$("#right").hide();
	});
	$(document).on("click","#cancel_validation",function(){
		resetColorTr();
		$(".div_validation").hide();
	});
	
	function resetColorTr(){
		let table = $("table :visible");
		let rows = $(table).find("tr");
		for(let i = 0 ; i < rows.length ; i ++){
			$(rows[i]).css("background-color","");
		}
	}
	
	
	$(document).on("click","#expand_condition",function(){
		
		var tr = $("<tr></tr>");
		var th = $("<th></th>").css({
			"padding":"0 10px 0 10px",
			"width": "5%"
		});
		var label = $("<label></label>");
		$(label).html("If");
		$(th).append(label);
		$(tr).append(th);
		
		// Operator
		var operator = $("#operator").clone();
		$(operator).css("display","");
		var th = $("<th></th>").css({
			"padding":"0 10px 0 10px",
			"width": "16%"
			
		});
		$(th).append(operator);
		$(tr).append(th);
		
		// Expression
		let expression = $("#expression1").clone() ;
		$(expression).css("display","");
		var th = $("<th></th>").css({
			"padding":"0 0 0 0",
			"width": "8%"
			
		});
		$(th).append(expression);
		$(tr).append(th);
		
		// Value
		var th = $("<th></th>").css({
			"padding":"0 10px 0 10px",
			"width": "5%"
			
		});
		let value = $("#value").clone() ;
		$(value).css("display","");
		var th = $("<th></th>").css({
			"padding":"0 10px 0 10px",
			"width": "5%"
		});
		$(th).append(value);
		$(tr).append(th);
		// Logic
		var logic = $("#logic").clone() ;
		$(logic).css("display","");
		var th = $("<th></th>").css({
			"padding":"0 10px 0 10px",
			"width": "5%"
		});
		$(th).append(logic);
		$(tr).append(th);
		
		
		// var expand = $("#expand").clone();
		var th = $("<th></th>").css({
			"padding":"0 10px 0 10px",
			"width": "5%"
		});
		let remove = $("<label></label>").html("-");
		let button = $("<button></button>").attr({
			"type" : "button",
			"id"   : "remove"
		}).css("margin","0 2px");
		$(button).addClass("ui-corner-all ui-state-default");
		$(button).append(remove);
		$(th).append(button);
		$(tr).append(th);
		
		$("#condition thead").append(tr);
		
	});
	$(document).on("click","#remove",function(){
		let tr = $(this).closest("tr");
		$(tr).remove();
	});
	
	
	var data_point_validation = "";
	$(document).on("click","#validation",function(){
		let tr = $(this).closest("tr");
		let table = $(this).closest("table");
		let allTr = $(table).find("tr");
		for(let i =  0 ; i < allTr.length; i ++){
			$(allTr[i]).css("background-color","");
		}
		$(tr).css("background-color","#8dc63f");
		data_point_validation = $(tr).find("#data_point").val(); 
		let data_point_name = $(tr).find("#name").val();
		
		$(".div_validation").show(); 
		$(".div_validation #operator").show();
		$(".div_validation #operator_text").hide();
		let pointer = "";
		if(localStorage.getItem("tab") == "real_node"){
			pointer = $("#real").find("#action");
		}else if(localStorage.getItem("tab") == "virtual_node"){
			pointer = $("#virtual").find("#action");
		}
		let operator = $(pointer).find("#operator");
		$(operator).show();
		let value =  $(pointer).find("#value");
		$(value).hide();
		let node =  $(pointer).find("#node");
		$(node).hide();
		let constant =  $(pointer).find("#constant");
		$(constant).show();
		
		
		// scroll to div validation
		debugger ;
		$(window).scrollTop($('.div_validation :visible').offset().top);
		
		let conditionTr = $(".div_validation :visible").find("#condition tr");
		for(let i =0 ; i < conditionTr.length ; i++){
			if( i > 0 ){
				$(conditionTr[i]).remove();
			}
		}
		
		$(".div_validation #name").html(data_point_name);
		
		// Reset Value
		$(".div_validation").find("select").prop('selectedIndex', 0);
		$(".div_validation").find("input").prop('value',"");
		
		// hide function create new node
		$("#create_object").hide();
	});
	var indexVirtual = 0 ;
	$(document).on("click","#ok",function(){
		
		
		let dataBacNet = [];
		let allOperator = $("#tbody select:visible,#tbody input:text:visible");
		
		for (let i = 0 ; i < allOperator.length ; i ++){
			if(allOperator[i].value == "null"){
				alert("You must fill all data");
				return ;
			}
		}
		var formula = "";
		for(let j = 0 ; j < allOperator.length ; j ++){
			var operator = allOperator[j];
			let value = "";
			if($(operator).is(":visible") == false){
				continue ;
			}
			if($(operator).is("input")){
				if(operator.id == "virtual_node_name")
					continue ;
				value = $(operator).val();
			}
			else if ($(operator).is("select") && operator.id == "expression"){
				value = $(operator).val();
			}
			else if ($(operator).is("select")){
				value = $(operator).val();
			}
			formula += value ;
		}
		debugger ;
		if(formula == ""){
			formula = $("#formula_").val();
		}
		var dataPoint = {};
		dataPoint.formula = formula ;
		dataPoint.channel = "";
		dataPoint.measure_name = "";
		dataPoint.unit = "" ;
		dataPoint.name = $("#virtual_node_name").val();
		dataPoint.consumption = "false";
		dataPoint.type = "data";
		// Always write last page
		if(total_vitural > 0){
			indexWrite = parseInt(total_vitural);
		}
		dataBacNet.push(dataPoint);
		
		write(dataBacNet);
		
		// Reset value
		$("#right").hide();
		$("#right").find("select").prop('selectedIndex',0);
		$("#create").show();
	});
	
	$(document).on("click","#cancel",function(){
		$(".left").show();
		$(".right").hide();
	});
	
	$(document).on("click","#create",function(){
		debugger
		$("#create").hide();
		$("#right").show();
		$("#right table:first").show();
		$("#keyin_table").hide();
		$("#formula_").attr("value","");
		$("#virtual_node_name").attr("value","");
		let tr = $("#tbody tr");
		for(let i = 0 ; i < tr.length ; i ++){
			if(i > 1)
				$(tr[i]).remove();
		}
	});
	
	$(document).on("click","#expand",function(){
		let tr = $("<tr></tr>");
		var td = $("<td></td>").css({
			"border": "0",
			"text-align": "center"
		});
		$(tr).append(td);
		
		var td = $("<td></td>").css({
			"border": "0",
			"text-align": "center"
		});
		$(tr).append(td);
		// Expression
		var expression = $("#expression").clone() ;
		$(expression).css("display","");
		var td = $("<td></td>").css({
			"border": "0",
			"text-align": "center",
			
		});
		$(td).append(expression);
		$(tr).append(td);
		// Operator
		var operator = $("#operator").clone();
		$(operator).css("display","");
		var td = $("<td></td>").css({
			"border": "0",
			"text-align": "center",
			
		});
		var input =  $("<input></input>").css({
			"display":"none",
			"border": "0px", 
			"width" : "100%"
		});
		// input.id = "value" ;
		$(input).attr("id","value");
		$(input).addClass("ui-corner-all inputText");
		$(td).append(operator);
		$(td).append(input);
		
		$(tr).append(td);
		
		// // transfer
		// var transfer = $("#transfer").clone();
		// $(transfer).css("display","");
		// var th = $("<th></th>").css({
			// "border" : "0px",
			// "text-align" : "center"
		// });
		// $(th).append(transfer);
		// $(tr).append(th);
		
		// var expand = $("#expand").clone();
		var remove = $("<label></label>").html("-");
		var button = $("<button></button>").attr({
			"type" : "button"
		}).click(function(){
			$(tr).remove();
		});
		$(button).addClass("ui-corner-all ui-state-default");
		$(button).append(remove);
		
		var td = $("<td></td>").css({
			"border": "0",
			"text-align": "center"
		});
		// $(td).append(expand);
		$(td).append(button);
		$(tr).append(td);
		
		var td = $("<td></td>").css({
			"border": "0",
			"text-align": "center"
		});
		var expand = $("#expand").clone();
		$(td).append(expand);
		$(tr).append(td);
		
		let nextTr = $(this).closest("tr");
		
		$(nextTr).after(tr);
	})
	$(document).on("click","#ok_validation",function(){
		$(".div_validation").show(); 
		let pointer = $(".div_validation :visible") ;
		
		let  rows = $(pointer).find("#condition tr") ;
		// Validation
		let allOperator = $(rows).find("select:visible,input:text:visible");
		for (let i = 0 ; i < allOperator.length ; i ++){
			if(allOperator[i].value == "null" || allOperator[i].value == ""){
				alert("You must fill all data");
				return ;
			}
		}
		
		let expressions = "" ;
		for(let i = 0 ; i < rows.length ; i ++){
			
			let columns = rows[i];
			let operator =  $(columns).find("#operator");
			let operator_text = $(columns).find("#operator_text");
			let expression = $(columns).find("#expression1");
			let value   = $(columns).find("#value");
			
			let condition = "";
			debugger ;
			if($(operator).is(":visible")){
				condition += $(operator).val() ;
			}else{
				if($.isNumeric($(operator_text).val()) == true){
					condition += $(operator_text).val();
				}
				else{
					condition += "N" + $(operator_text).val();
				}
				
			}
			condition += $(expression).val() ;
			condition += $(value).val();
			
			if(i < rows.length - 1){
				let logic = $(columns).find("#logic");
				condition += $(logic).val();
			}
			expressions += condition ;
		}
		let actionRows  = $(pointer).find("#action tr");
		let actionResult = "";
		let operator = $(actionRows[0]).find("#operator");
		let texbox = $(actionRows[0]).find("#value");
		let value = "";
		if($(texbox).is(":visible")){
			value = $(texbox).val();
		}else if($(operator).is(":visible")){
			value = $(operator).val();
		}
		actionResult = "N" + data_point_validation + "=" + value;

		var request = $.ajax({
			url: pluginRoot + "/validation",
			data: {
				'device_address': $("#device_address").val(),
				'expressions': expressions,
				'device_instanceid': $("#device_instanceid").val(),
				'adapter_name' : $("#adapter").val(),
				'action_result' : actionResult ,
				'data_point' : data_point_validation,
				'validation_id' : validation_id_edit 
			},
			timeout: 15000, // in milliseconds
			dataType: 'html',
			async: 'true',
			type : 'post',
			statusCode: {
			400: function () {
			},
			404: function () {
				location.replace(''); // clear the page content
			},
			408: function () {
			}
			}
		});
		request.done(function (data) {
			alert("Success");
			$(".div_validation").hide();
			resetColorTr();
			if(validation_id_edit != "")
				$("#validation_node").trigger("click");
		});
		request.fail(function (jqXHR, textStatus, errorThrown) {
		});
	});
	$(document).on("click","#virtual_node",function(){
		// Reset validation id edit
		validation_id_edit = "";
		
		$("#real").hide();
		$("#virtual").show();
		$("#validation_show").hide();
		$("#scan").hide();
		$("#save").show();
		
		$("#virtual_node").addClass("active");
		$("#real_node").removeClass("active");
		$("#validation_node").removeClass("active");
		indexWrite = 1 ;
		
		
		localStorage.setItem("tab", "virtual_node");
		getChannel($("#adapter").val(),1) ;
		// fillData();
		
	});
	
	$(document).on("click","#real_node",function(){
		// Reset value validation edit
		validation_id_edit = "";
		
		$("#real").show();
		$("#virtual").hide();
		$("#real_node").addClass("active");
		$("#virtual_node").removeClass("active");
		$("#validation_node").removeClass("active");
		$("#save").show();
		$("#validation_show").hide();
		if($("#adapter").val()== "BacnetAdapter"){
			$("#scan").hide();
		}else if($("#adapter").val()== "OpcuaAdapter"){
			$("#scan").show();
		}
		
		indexWrite = 1 ;
		
		localStorage.setItem("tab", "real_node");
		getChannel($("#adapter").val(),1) ;
		
	});
	$(document).on("click","#transfer",function(){
		debugger
		let pointer = $(this).closest("tr");
		let operator = $(pointer).find("#operator");
		let value = $(pointer).find("#value");
		if($(operator).is(":visible")){
			$(operator).hide();
			$(value).show();
		}else{
			$(operator).show();
			$(value).hide();
		}
	});
	
	var isCreateNew = false ;
	$(document).on("click","#ok_",function(){
		let dataBacNet = [] ;
		let dataPoint = {} ;
		
		isCreateNew = true ;
		$('#page').addClass('disabledmouse');
		dataPoint.object_identifier = $("#object").val() ;
		dataPoint.oi_measure_name  = $("#object_name").val();
		dataPoint.oi_measure_unit  = $("#object_unit").val();
		if(dataPoint.object_identifier == ""){
			alert("Object Identifier is not null");
			$('#page').removeClass('disabledmouse');
			return ;
		}
		var patt1 = /[a-z0-9]/g;
		var result = dataPoint.object_identifier.match(patt1);
		debugger
		if(dataPoint.object_identifier.length !== result.length){
			alert("Wrong format object identifier");
			$('#page').removeClass('disabledmouse');
			return ;
		}
		
		$("#object").attr("value","");
		$("#object_name").attr("value","");
		$("#object_unit").attr("value","");
		dataBacNet.push(dataPoint);
		
		let request = $.ajax({
			type : "post",
			url: pluginRoot + '/writeChannel',
			data: {
			"adapterName" : $("#adapter").val(),
			"address"     : $("#device_address").val(),
			"deviceInstance" : $("#device_instanceid").val(),
			"delete" : JSON.stringify(data_point_delete),
			"update" : JSON.stringify(dataBacNet)},
			
			timeout: 15000, // in milliseconds
			dataType : "json",
			async: 'false',
			statusCode: {
				400: function () {
				},
				404: function () {
					location.replace(''); // clear the page content
				},
				408: function () {
				}
			}
		});
	
		request.done(function (data) {
			
			let errorMessage =  "";
			debugger
			if(data != null ){
				if(typeof data.errors !== "undefined"){
					showErrorForm(data.errors);
					$('#page').removeClass('disabledmouse');
					return ;
				}
				showSuccessForm();
			}
			indexWrite = totalPage ;
			getChannel($("#adapter").val(),indexWrite) ;
			$("#status_update").hide();
			$('#page').removeClass('disabledmouse');
		});
		request.fail(function (jqXHR, textStatus, errorThrown) {
			alert("No Response");
			$('#page').removeClass('disabledmouse');
			console.log(textStatus + "---- "+ errorThrown);
			
		});
	
	});
	
	$(document).on("click","#scanning",function(){
		$(this).css("background-color","#acacac");
		$("#read").css("background-color","");
		$("#write").css("background-color","");
		debugger
		active_tab = "scanning";
		
		$("#table_real_node_").show();
		$("#table_real_node").hide();
		
		$("#scanning").addClass("active");
		$("#read").removeClass("active");
		$("#write").removeClass("active");
		$("#menu_2").hide();
		$("#real").show();
		$("#virtual").hide();
		$("#table_writing").hide();
		$("#create_node").show();
		$("#scan").show();
		
		indexWrite = 1 ;
		// filter bacnet
		type_bacnet = "null";
		
		$("#type_bacnet").val("null");
		
		$("#div_bacnet_type").show();
		
	
		
		scanning($("#adapter").val(),1,"") ;
	});

	$(document).on("click","#read",function(){
		$("#div_bacnet_type").hide();
		$(this).css("background-color","#acacac");
		$("#scanning").css("background-color","");
		$("#write").css("background-color","");
		debugger
		active_tab = "read";
		type_bacnet = "data";
		$("#table_real_node_").hide();
		$("#table_real_node").show();
		$("#read").addClass("active");
		$("#scanning").removeClass("active");
		$("#write").removeClass("active");
		$("#menu_2").show();
		$("#table_writing").hide();
		$("#create_node").hide();
		
		
		indexWrite = 1 ;
		$("#real_node").trigger("click");
		// getChannel($("#adapter").val(),1) ;
	});

	$(document).on("click","#write",function(){
		$("#div_bacnet_type").hide();
		/*$("#type_bacnet").hide();*/
		$(this).css("background-color","#acacac");
		$("#scanning").css("background-color","");
		$("#read").css("background-color","");
		debugger
		active_tab = "write";
		type_bacnet = "setting" ;
		$("#write").addClass("active");
		$("#scanning").removeClass("active");
		$("#read").removeClass("active");
		$("#table_writing").show();
		$("#table_real_node_").hide();
		$("#table_real_node").hide();
		$("#menu_2").hide();
		indexWrite = 1 ;
		$("#create_node").hide();
		$("#scan").hide();
		writing($("#adapter").val(),1) ;
	});
	
	$(document).on("change","#type_bacnet",function(){
		debugger
		type_bacnet = $("#type_bacnet").val();
		indexWrite = 1 ;
		scanning($("#adapter").val(),1) ;
	});
	
	$(document).on("click","#formula",function(){
		if($(this).is("input")){
			return ;
		}
		$(this).hide();
		let td = $(this).closest("td");
		
		var input = document.createElement("input");
		input.style.width = "100%";
		input.type = "text"
		input.id = "formula";
		input.className = "ui-corner-all inputText";
		input.value = $(this).val() ;
		$(td).append(input);
		
	});
	$(document).on("change","#none,#setting,#data",function(){
		debugger
		let value = $(this).val();
		let confirm_ =  confirm("Do you want set for whole page");
		$(this).prop('checked', false);
		if(confirm_ == false){
			return ;
		}
		writeScanning(value);
	});
	
	
	
	
	
	

	
	
	
	
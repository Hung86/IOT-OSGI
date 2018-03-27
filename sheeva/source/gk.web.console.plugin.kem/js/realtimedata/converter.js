var adapterSettingsData;
var current_Real_Time_Data = {};
var current_page = 0;
window.onload =  entryScript();

function entryScript() {
	$.get( pluginRoot,{'id':'data','adapter': adapter}, function(data) {
		renderAdapterSettings(data);
	},'json');
}

function renderAdapterSettings(data) {
	console.log("[Modbus] call renderAdapterSettings");
	adapterSettingsData = data;
	apdapter_mode = parseInt(data.mode);
	if (apdapter_mode == 0) {
		showMode0();
	} else if (apdapter_mode == 1) {
		showMode1();
	}

	$("#address_label").hide();
	$("#address").hide();
	$("#instanceid_label").text("Device Id");
	var instanceidArray = [];
	for (var i = 0 ; i < data.devices.length; i++) {
		if (instanceidArray.indexOf(data.devices[i][_device.instanceid]) == -1) {
			instanceidArray.push(data.devices[i][_device.instanceid]);
		}
	}
	
	instanceidArray.sort();
	$('#instanceid').empty();
	for(var m = 0; m < instanceidArray.length; m++) {
		$('#instanceid').append($('<option>', {value:instanceidArray[m], text:instanceidArray[m]}));
	}
	$("#instanceid").trigger("change");
	
	debugger
}

function onChangeInstanceId(){
	var devive_instanceid = $('#instanceid').val();
	console.log("===>adapter is changed another instance id to " + devive_instanceid);
	showDeviceName(devive_instanceid);
	$("#view_status").empty();
	$("#data_view").empty();
	is_getting_data = 0;
	current_page = 0;
	current_Real_Time_Data = {};
	clearInterval(timer);
}

function showDeviceName(id){
	for (let i = 0 ; i < adapterSettingsData.devices.length; i++) {
		if (id == adapterSettingsData.devices[i][_device.instanceid]) {
			$("#devicename").text(adapterSettingsData.devices[i][_device.name]);
			break;
		}
	}
}

function renderDeviceData(real_time_data) {
	if (real_time_data.status === -4) {
		$('#view_status').text("Hardware not detected. Please connect hardware and try again");	
	} else if (real_time_data.status === -3) {
		$('#view_status').text("Adapter isn't available");	
	} else if (real_time_data.status === -2) {
		$('#view_status').text("Mode is not consistent,please switch to correct mode " + real_time_data.mode);
	}else if (real_time_data.status === -1) {
		$('#view_status').text("Comunication timeout. Data is not available");
	}else if (real_time_data.status === 0) {
		$('#view_status').text("");
		debugger
		$.each(real_time_data.data, function(key, value){
			$.each(value, function(key2, value2) {
				if (typeof current_Real_Time_Data.data != "undefined") {
					if (current_Real_Time_Data.data[key][key2] != value2) {
						$("#"+ key2).html("<p class='change_val'>" + value2 + "</p>");
					} else {
						$("#"+ key2).html("<p class='unchange_val'>" + value2 + "</p>");
					}
				} else {
					$("#"+ key2).html("<p>" + value2 + "</p>");
				}
			
				if (key2 == "row") {
					$("#row_" + value2).show();
				}
				
			});
		})
		current_Real_Time_Data = real_time_data;

		setDecorationandTimeout();
		debugger
	}
}
function setDecorationandTimeout() {
	$(".change_val").css('color', 'blue');
	var color_timeout = setInterval(function(){
			$(".change_val").css('color', 'black');
			clearInterval(color_timeout);
		},2000);
}
function initializeDataPage(adapter, address, id, category, mode, time_out) {
	var request2 = $.ajax({
			url: pluginRoot,
			data : { 'action': 'html','adapter': adapter,'device': category},
			timeout: time_out, //in milliseconds
			dataType : "html",
			statusCode: {
				400: function() {
					},
				404: function() {
					},
				408: function() {
					}
				}
	});
	request2.done(function( data2 ) {
			$('#data_view').empty();
			$('#data_view').append(data2);
			paging(category);
			getUpdatedData(adapter, "" , id, current_page , "" , mode, time_out);
	});
	request2.fail(function( jqXHR, textStatus, errorThrown  ) {
	});
}
function paging(cat){
	if (cat == "1012") {
		current_page = 0;
		var ul_page = document.getElementById("paging");
		var li = document.createElement("LI"); 
		var a  = document.createElement("A");
		a.innerHTML = "<b>Channel</b>";
		li.appendChild(a);
		li.id  = "first";
		$(ul_page).append(li);
	
		for(var i = 0 ; i < 4 ; i++){
			li = document.createElement("LI"); 
			a  = document.createElement("A");
			a.innerHTML = i ;
			a.href = "#";
			li.appendChild(a);
			let localIndex = i ;
			 if(i == current_page){
				 a.className = "active" ;
			}
			a.onclick = function(){
				$("#paging").find(".active").removeClass("active");
				this.className = "active" ;
				current_page = parseInt(localIndex) ;
				showCurrentPage();
				
			}
			
			$(ul_page).append(li);
		}
		showCurrentPage();
	} else {
		current_page = 0;
	}
}

function refreshData(time_out) { // update apdater status and get fresh data
	let id = $("#instanceid").val();
	console.log("----------refreshData : time_out = " + time_out);
	if (is_getting_data == 0) {
		is_getting_data = 1;
		let category = getDeviceCategory(null, id);
		initializeDataPage(adapter, "", id, category, apdapter_mode, time_out);
	} else {
		getUpdatedData(adapter, "" , id, current_page , "" , apdapter_mode, time_out);
	}
}

function showCurrentPage() {
	var tables = document.getElementsByTagName("TABLE");
	for (var i = 0; i < tables.length; i++) {
		if (tables[i].id.indexOf("plugin_table") != -1 ) {
			if (tables[i].id == ("plugin_table_" + current_page)) {
				$("#plugin_table_" + current_page).show();
			} else {
				tables[i].style.display = "none";
			}
		}
	}
}
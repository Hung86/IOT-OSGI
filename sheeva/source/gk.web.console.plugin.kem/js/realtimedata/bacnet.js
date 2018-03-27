var adapterSettingsData;
var maxItemPerPage = 40;
var total_node_data = 0;
var total_page = 0;
var current_page = 0;
var page_name = "paging";
var waiting_timeout = 0;
var node_type = "";

window.onload =  entryScript();

function entryScript() {
	$.get( pluginRoot,{'id':'data','adapter': adapter}, function(data) {
		renderAdapterSettings(data);
	},'json');
}

function onChangeAddress(){
	var devive_address = $('#address').val();
	console.log("===>adapter is changed another address to " + devive_address);
	var idArray = [];
	for (var i = 0 ; i < adapterSettingsData.devices.length; i++) {
		if (devive_address == adapterSettingsData.devices[i][_device.address]) {
			if (idArray.indexOf(adapterSettingsData.devices[i][_device.instanceid]) == -1) {
				idArray.push(adapterSettingsData.devices[i][_device.instanceid]);
			}
		}
	}
	
	idArray.sort();
	$('#instanceid').empty();
	for(var n = 0; n < idArray.length; n++) {
		$('#instanceid').append($('<option>', {value:idArray[n], text:idArray[n]}));
	}
	$("#instanceid").trigger("change");
}

function onChangeInstanceId(){
	var device_address = $('#address').val();
	var devive_instanceid = $('#instanceid').val();
	console.log("===>[Bacnet] adapter is changed another instance id to " + devive_instanceid);
	showDeviceName(device_address, devive_instanceid);
	$("#view_status").empty();
	$("#data_view").empty();
	is_getting_data = 0;
	total_node_data = 0;
	total_page = 0;
	current_page = 0;
	waiting_timeout = normal_data_timeout;
	clearInterval(timer);
}

function showDeviceName(addr, id){
	for (var i = 0 ; i < adapterSettingsData.devices.length; i++) {
		if ((addr == adapterSettingsData.devices[i][_device.address]) &&
		(id == adapterSettingsData.devices[i][_device.instanceid])) {
			$("#devicename").text(adapterSettingsData.devices[i][_device.name]);
			break;
		}
	}
}

function renderAdapterSettings(data) {
	console.log("[Bacnet] call renderAdapterSettings");
	adapterSettingsData = data;
	apdapter_mode = parseInt(data.mode);
	if (apdapter_mode == 0) {
		showMode0();
	} else if (apdapter_mode == 1) {
		showMode1();
	}
	
	$("#address_label").show();
	$("#address").show();
	$("#instanceid_label").text("Instance Id");
	var addressArray = [];
	for (var i = 0 ; i < data.devices.length; i++) {
		if (addressArray.indexOf(data.devices[i][_device.address]) == -1) {
			addressArray.push(data.devices[i][_device.address]);
		}
	}
	
	addressArray.sort();
	$('#address').empty();
	for(var m = 0; m < addressArray.length; m++) {
		$('#address').append($('<option>', {value:addressArray[m], text:addressArray[m]}));
	}
	$("#address").trigger("change");
	
	debugger
	
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
		
		let allLabels;
		let allRows;
		debugger

		if (node_type == "real_node") {
			allLabels = $("#real_time_data_table tbody").find("label");
			allRows = $("#real_time_data_table tbody").find("tr");
			$("#real_time_data_table").show();
			$("#virtual_time_data_table").hide();
		} else if (node_type == "virtual_node") {
			allLabels = $("#virtual_time_data_table tbody").find("label");
			allRows = $("#virtual_time_data_table tbody").find("tr");
			$("#real_time_data_table").hide();
			$("#virtual_time_data_table").show();
		}
		
		$.each(allRows, function(key, value) {
			$(value).hide();
		});

		$.each(allLabels, function(key, value) {
			value.innerHTML = "";
		});
		
		$.each(real_time_data.data, function(key, value){
			 let row_element = allRows[key];
			 $(row_element).show();
			 $.each(value, function(key2, value2) {
				 $(row_element).find("#"+ key2).text(value2);
		   });
		})

	}
}

function refreshData(time_out) { // update apdater status and get fresh data
	var device_address = $('#address').val();
	var instance_id = $('#instanceid').val();
	waiting_timeout = time_out;
	console.log("bacnet----------refreshData : time_out = " + time_out);
	if (is_getting_data == 0) {
		is_getting_data = 1;
		let category = getDeviceCategory(device_address, instance_id);
		initializeDataPage(adapter, device_address , instance_id, category, apdapter_mode, time_out);
	} else {
		getDataCurrentPage(adapter, apdapter_mode);
	}
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
			$('#real_node').click();
		});
		request2.fail(function( jqXHR, textStatus, errorThrown  ) {
		});

}

function paging(){
	maxItemPerPage = $("#records_per_page").val();
	total_page = parseInt(total_node_data / maxItemPerPage) + ((total_node_data % maxItemPerPage) === 0 ? 0 : 1);
	if(total_page <= 1)
		return ;
	$("#page_number").val("1");
	current_page = 1;
	getDataCurrentPage(adapter, apdapter_mode);
}
function onChangePaging() {
	paging();
}

function goToPage(){
	current_page = $("#page_number").val();
	if ((current_page >=1) && (current_page <= total_page)) {
		getDataCurrentPage(adapter, apdapter_mode);
	} else {
		alert("Page number " + current_page + " is illegal !");
	}
};

function first(){
	current_page = 1;
	$("#page_number").val("1");
	getDataCurrentPage(adapter, apdapter_mode);
}

function previous(){
	if (current_page > 1) {
		current_page--;
		$("#page_number").val(current_page);
		getDataCurrentPage(adapter, apdapter_mode);
	}
}

function next(){
	if (current_page < total_page) {
		current_page++;
		$("#page_number").val(current_page);
		getDataCurrentPage(adapter, apdapter_mode);
	}
}

function last(){
	current_page = total_page;
	$("#page_number").val(current_page);
	getDataCurrentPage(adapter, apdapter_mode);
}

function detectPagingStatus(){
	$("#current").text("Page " + current_page + " of " + total_page);
	if (total_page <= 1) {
		$("#go").addClass("disabledmouse");
		$("#first").addClass("disabledmouse");
		$("#previous").addClass("disabledmouse");
		$("#next").addClass("disabledmouse");
		$("#last").addClass("disabledmouse");
		return;
	}
	
	$("#go").removeClass("disabledmouse");
	$("#first").removeClass("disabledmouse");
	$("#previous").removeClass("disabledmouse");
	$("#next").removeClass("disabledmouse");
	$("#last").removeClass("disabledmouse");
	
	if (current_page <= 1) {
		$("#first").addClass("disabledmouse");
		$("#previous").addClass("disabledmouse");
		return;
	}
	
	if (current_page >= total_page) {
		$("#next").addClass("disabledmouse");
		$("#last").addClass("disabledmouse");
		return;
	}
}
function getDataCurrentPage(adapter, mode) {
	if (total_node_data > 0) {
		var addr = $('#address').val();
		var id = $('#instanceid').val();
		var node_index = maxItemPerPage*(current_page - 1);
		var node_length = 0;
		if (current_page == total_page) {
			node_length = total_node_data - (maxItemPerPage*(current_page-1));
		} else {
			node_length = maxItemPerPage;
		}
		getUpdatedData(adapter, addr, id, node_index, node_length, mode, waiting_timeout);
		debugger
	} else {
		$('#view_status').text("Datapoints are not available");
	}
	detectPagingStatus();
}

function openTab(event, nodeType) {
    // Declare all variables
	console.log("---------------[Bacnet] : chose tab " + nodeType);
	debugger;
	if (node_type == nodeType) {
		return;
	}
    let i, tablinks;
    node_type = nodeType;
    
    // Get all elements with class="tablinks" and remove the class "active"
    tablinks = document.getElementsByClassName("tablinks");
    for (i = 0; i < tablinks.length; i++) {
        tablinks[i].className = tablinks[i].className.replace(" active", "");
    }

    event.target.className += " active";
    
	let device_address = $('#address').val();
	let instance_id = $('#instanceid').val();
	var request = $.ajax({
	    url: pluginRoot,
		type: "POST",
	    data : { 'action':'node_num','adapter': adapter,'address': device_address, 'id':instance_id , 'node_type' : node_type},
	    timeout: waiting_timeout, //in milliseconds
	    dataType: "json",
	    statusCode: {
	    	400: function() {
//	     		alert( "Bad Request" );
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
		debugger
		total_node_data = parseInt(data.node_num);
		current_page = 1;
		paging();
		getDataCurrentPage(adapter, apdapter_mode);

	});
	
	request.fail(function( jqXHR, textStatus, errorThrown  ) {

	});
}

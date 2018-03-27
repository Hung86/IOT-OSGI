/* fill in the data */
$(document).ready(function(){
	
	$.get(pluginRoot, { 'id':'gateway_status'}, function(data) {
		renderNetworkDetail(data);
		setGraphicStatus(data);
		finishLoadingThePage();
	}, 'json');
	

	loadingThePage();
});

function renderNetworkDetail(data) {
	$.each(data, function(key, value) {
		$("#" + key).text(value);
	})
}

function setGraphicStatus(data) {
	$( "#pb_mem_usage" ).progressbar({  change: function( event, ui ) {
		var selector = "#" + this.id + " > div";
		var value = $("#" + this.id + "").progressbar("value");
		if (value <= 50){
            $(selector).css({ 'background': 'Green' });
        } else if (value <= 80){
            $(selector).css({ 'background': 'Yellow' });
        } else{
            $(selector).css({ 'background': 'Red' });
        }
         $('#pb_mem_usage_label' ).css('background-color', 'transparent');
         $( '#pb_mem_usage_label' ).text( value + "%" );
	}}); 
	
	$( "#pb_jvm_mem" ).progressbar({  change: function( event, ui ) {
		var selector = "#" + this.id + " > div";
		var value = $("#" + this.id + "").progressbar("value");
		if (value <= 50){
            $(selector).css({ 'background': 'Green' });
        } else if (value <= 80){
            $(selector).css({ 'background': 'Yellow' });
        } else{
            $(selector).css({ 'background': 'Red' });
        }
         $('#pb_jvm_mem_label' ).css('background-color', 'transparent');
         $( '#pb_jvm_mem_label' ).text( value + "%" );
	}}); 
	
	$( "#pb_internal_buffer" ).progressbar({  change: function( event, ui ) {
		var selector = "#" + this.id + " > div";
		var value = $("#" + this.id + "").progressbar("value");
		if (value <= 50){
            $(selector).css({ 'background': 'Green' });
        } else if (value <= 80){
            $(selector).css({ 'background': 'Yellow' });
        } else{
            $(selector).css({ 'background': 'Red' });
        }
         $('#pb_internal_buffer_label' ).css('background-color', 'transparent');
         $('#pb_internal_buffer_label' ).text( value + "%" );
	}}); 
	
	$( "#pb_external_buffer" ).progressbar({  change: function( event, ui ) {
		var selector = "#" + this.id + " > div";
		var value = $("#" + this.id + "").progressbar("value");
		if (value <= 50){
            $(selector).css({ 'background': 'Green' });
        } else if (value <= 80){
            $(selector).css({ 'background': 'Yellow' });
        } else{
            $(selector).css({ 'background': 'Red' });
        }
         $('#pb_external_buffer_label' ).css('background-color', 'transparent');
         $('#pb_external_buffer_label' ).text( value + "%" );
	}}); 
	
	if (data.internet_status  == "Good") {
		$('#display_img').attr("src", pluginRoot + "/res/img/Box_Green.png");
	} else {
		$('#display_img').attr("src", pluginRoot + "/res/img/Box_Red.png");
	}
	var tmp = parseInt(data.mem_usage);
	if(tmp < 1){
		tmp = 0;
	}
	$("#pb_mem_usage").progressbar({ "value": tmp });
	
	tmp = parseInt(data.jvm_mem_usage);
	
	if(tmp < 1){
		tmp = 0;
	}
	$("#pb_jvm_mem").progressbar({ "value": tmp });
	
	tmp = parseInt(data.internal_buffer_usage);
	if(tmp < 1){
		tmp = 0;
	}
	$("#pb_internal_buffer").progressbar({ "value": tmp });
	
	tmp = parseInt(data.external_buffer_usage);
	if(tmp < 1){
		tmp = 0;
	}
	$("#pb_external_buffer").progressbar({ "value": tmp });
}


function getInternetStatus() {
	console.log("---getInternetStatus");
	loadingThePage("Getting internet status...");
	var request = $.ajax({
		url: pluginRoot,
		data : { 'id':'internet_status'},
		timeout: 5000, //in milliseconds
		dataType: "json",
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
		console.log("---getInternetStatus : value = " + data.internet_status);
		$( '#internet_status' ).text( data.internet_status );
		var src;
		if(data.internet_status === "Good"){
			src = pluginRoot+"/res/img/Box_Green.png";
		}else{
			src = pluginRoot+"/res/img/Box_Red.png";
		}
		$('#display_img').attr("src", src);
		finishLoadingThePage();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		finishLoadingThePage();
	});
}

function getSocketStatus() {
	console.log("---getSocketStatus");
	loadingThePage("Getting socket status...");
	var request = $.ajax({
		url: pluginRoot,
		data : { 'id':'internet_error'},
		timeout: 5000, //in milliseconds
		dataType: "json",
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
		console.log("---getSocketStatus : value = " + data.internet_error);
		$( '#internet_error' ).text(data.internet_error + " Error(s) in 24 hours");
		finishLoadingThePage();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		finishLoadingThePage();
	});
}

function getSerialPortStatus() {
	console.log("---getSerialPortStatus");
	loadingThePage("Getting serial port status...");
	var request = $.ajax({
		url: pluginRoot,
		data : { 'id':'serial_error'},
		timeout: 5000, //in milliseconds
		dataType: "json",
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
		console.log("---getSerialPortStatus : value = " + data.serial_error);
		$( '#serial_error' ).text( data.serial_error + " Error(s) in 24 hours" );
		finishLoadingThePage();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		finishLoadingThePage();
	});
}

function getCpuUsageStatus() {
	console.log("---getCpuUsageStatus");
	loadingThePage("Getting CPU utilization info...");
	var request = $.ajax({
		url: pluginRoot,
		data : { 'id':'cpu_usage'},
		timeout: 5000, //in milliseconds
		dataType: "json",
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
		console.log("---getCpuUsageStatus : value = " + data.cpu_usage);
		$('#cpu_usage').text(data.cpu_usage);
		finishLoadingThePage();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		finishLoadingThePage();
	});
}

function getMemStatus() {
	console.log("---getMemStatus");
	loadingThePage("Getting physical memory info...");
	var request = $.ajax({
		url: pluginRoot,
		data : { 'id':'mem_usage'},
		timeout: 5000, //in milliseconds
		dataType: "json",
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
		js_mem_usage = parseFloat(data.mem_usage);
		var tmp = parseInt(js_mem_usage);
		console.log("---getMemStatus : value = " + tmp);
		if((js_mem_usage < 1) && (js_mem_usage > 0)){
			tmp = 1;
		}
		$("#pb_mem_usage").progressbar({ "value": tmp});
		$("#used_mem").text(data.used_mem);
		finishLoadingThePage();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {	
		finishLoadingThePage();
	});
}

function clearInternalStore() {
	console.log("---clearInternalStore");
	if (confirm("Warning ! Clearing the Internal Non-Volatile Buffer Data, will erase all historical data in both Internal and External")) {
		loadingThePage("Clearing internal store...");
		var request = $.ajax({
			url: pluginRoot,
			data : { 'id':'clear_internal_store'},
			timeout: 5000, //in milliseconds
			dataType: "json",
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
			console.log("---clearInternalStore : internal_usage=" + data.internal_buffer_usage + " - external_usage=" + data.external_buffer_usage);
			js_internal_buffer_usage = parseFloat(data.internal_buffer_usage);
			js_external_buffer_usage = parseFloat(data.external_buffer_usage);
			tmp = parseInt(js_internal_buffer_usage);
			if(( js_internal_buffer_usage< 1) && (js_internal_buffer_usage > 0)){
				tmp = 1;
			}
			$("#pb_internal_buffer").progressbar({ "value": tmp });
			$("#in_cap_total").text(data.in_cap_total);
			$("#in_cap_used").text(data.in_cap_used);
			
			tmp = parseInt(js_external_buffer_usage);
			if((js_external_buffer_usage < 1) && (js_external_buffer_usage > 0)){
				tmp = 1;
			}
			$("#pb_external_buffer").progressbar({ "value": tmp });
			$("#ex_cap_total").text(data.ex_cap_total);
			$("#ex_cap_used").text(data.ex_cap_used);
			finishLoadingThePage();
		});
		request.fail(function( jqXHR, textStatus, errorThrown  ) {	
			finishLoadingThePage();
		});
	}
}

function clearExternalStore() {
	console.log("---clearExternalStore");
	if (confirm("Warning ! Clearing the External Non-Volatile Buffer Data, will erase all historical data in External")) {
		loadingThePage("Clearing external store...");
		var request = $.ajax({
			url: pluginRoot,
			data : { 'id':'clear_external_store'},
			timeout: 5000, //in milliseconds
			dataType: "json",
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
			console.log("---clearExternalStore :  external_usage=" + data.external_buffer_usage);
			js_external_buffer_usage = parseFloat(data.external_buffer_usage);
			tmp = parseInt(js_external_buffer_usage);
			if((js_external_buffer_usage < 1) && (js_external_buffer_usage > 0)){
				tmp = 1;
			}
			$("#pb_external_buffer").progressbar({ "value": tmp });
			$("#ex_cap_total").text(data.ex_cap_total);
			$("#ex_cap_used").text(data.ex_cap_used);
			finishLoadingThePage();
		});
		request.fail(function( jqXHR, textStatus, errorThrown  ) {	
			finishLoadingThePage();
		});
	}
}

function gatewayTimeRefresh(){
	loadingThePage("Refreshing gateway time...");
	var request = $.ajax({
		url: pluginRoot,
		data : { 'id':'gateway_time'},
		timeout: 5000, //in milliseconds
		dataType: "json",
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
		$( '#gateway_time_label' ).text( data.time );
		finishLoadingThePage();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		finishLoadingThePage();
	});
}

function syncGatewayLock() {
	loadingThePage("Synchronizing gateway time to server");
	var request = $.ajax({
		url: pluginRoot,
		data : { 'id': 'sync_clock'},
		timeout: 10000, //in milliseconds
		dataType: "json",
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
		$('#gateway_time_label' ).text( data.time );
		finishLoadingThePage();
	});
	request.fail(function( jqXHR, textStatus, errorThrown  ) {
		finishLoadingThePage();
	});	
}

function refreshJVMMemory(){
	loadingThePage("Refreshing JVM memory...");
	$("#refresh_jvm_memory").submit();
}
function rebootGateway() {
	console.log("---rebootGateway");
	if (confirm('This will reboot the gateway, the process takes 2 minutes and 20 secs. Please confirm to continue.')) {
		$("#rebootform").hide();
		$("#rebootform2").show();
		$("#reboot_type").val("gateway");
		shutdown(3, 'rebootform2', 'countdowncell');
	}
}

function rebootJVM() {
	console.log("---rebootJVM");
	if (confirm('This will stop and restart the framework and all bundles. Please confirm to continue.')) {
		$("#rebootform").hide();
		$("#rebootform2").show();
		$("#reboot_type").val("jvm");
		shutdown(3, 'rebootform2', 'countdowncell');
	}
}


/* shuts down server after [num] seconds */
function shutdown(num, formname, elemid) {
	var elem = $('#' + elemid);
	var secs=" second";
	//var ellipsis="...";
	var ellipsis="";
	if (num > 0) {
		if (num != 1) {
			secs+="s";
		}
	    elem.html(num+secs+ellipsis);
		setTimeout('shutdown('+(--num)+', "'+formname+'", "'+elemid+'")',1000);
	} else {
	    $('#' + formname).submit();
	}
}

/* aborts server shutdown and redirects to [target] */
function abort(target) {
    top.location.href=target;
}

function finishLoadingThePage(){
	$("#navmenu").removeClass("disabledmouse");
	$("#maincontent").removeClass("disabledmouse");
	$("#gatewaystatus").removeClass("disabledmouse");
	$("#rebootform").removeClass("disabledmouse");
	$("#rebootform2").removeClass("disabledmouse");
	$("#wait_loading").hide();
}

function loadingThePage(name) {
	$("#navmenu").addClass("disabledmouse");
	$("#maincontent").addClass("disabledmouse");
	$("#gatewaystatus").addClass("disabledmouse");
	$("#rebootform").addClass("disabledmouse");
	$("#rebootform2").addClass("disabledmouse");
	$("#wait_loading").show();
	$("#wait_loading_label").text(name);
	
}

function refreshGateway() {
	location.reload();
}

function clearReadData() {
	var request = $.ajax({
		url : pluginRoot,
		data : {
			'action' : 'clear_data_read'
		},
		timeout : 10000, // in milliseconds
		dataType : "json",
		statusCode : {
			400 : function() {
				console.log("code 400")
			},
			404 : function() {
				location.replace("");// clear the page content
			},
			408 : function() {
				// alert( "Request Timeout" );
			}
		}
	});
	request.done(function(data) {
		$("#total_reading_data").text("0 bytes");
	});
	request.fail(function(jqXHR, textStatus, errorThrown) {

	});
}
function clearSentData() {
	var request = $.ajax({
		url : pluginRoot,
		data : {
			'action' : 'clear_data_sent'
		},
		timeout : 10000, // in milliseconds
		dataType : "json",
		statusCode : {
			400 : function() {
				console.log("code 400")
			},
			404 : function() {
				location.replace("");// clear the page content
			},
			408 : function() {
				// alert( "Request Timeout" );
			}
		}
	});
	request.done(function(data) {
		$("#total_sent_data").text("0 bytes");
	});
	request.fail(function(jqXHR, textStatus, errorThrown) {

	});
}
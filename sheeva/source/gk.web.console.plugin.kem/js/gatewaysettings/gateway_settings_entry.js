$(document).ready(function(){
	$.get(pluginRoot,{'id':'all'}, function(data) {
		console.log("---------------------protocol = " + data.bridge_mode);
		renderBridgeMode(data);
		renderProtocolSettings(data);
	}, 'json');
	
	$(".loghandler").change(function () {
		if($(this).val() === "on"){
			$('#logDebug').prop('checked', true);
			$('#logInfo').prop('checked', false);
		} else if($(this).val() === "off"){
			$('#logDebug').prop('checked', false);
			$('#logInfo').prop('checked', true);
		}
	});
	
	$('#checkBoxHost').click(function(){
		if($(this).is(':checked')){
			$("#optionRemoteHost").addClass('ui-state-disabled');
			$("#manuallyRemoteHost").removeClass('ui-state-disabled');
			$("#manuallyRemoteHost").prop('disabled', false);
			
	    } else {
	    	$("#optionRemoteHost").removeClass('ui-state-disabled');
			$("#manuallyRemoteHost").addClass('ui-state-disabled');
			$("#manuallyRemoteHost").prop('disabled', true);
	    }
	})
	
});

function mySelectedProtocol(){
	let bridgeMode = $("#bridge_mode").val();
	$.get(pluginRoot,{'id':bridgeMode}, function(data) {
		renderProtocolSettings(data);
	}, 'json')
}

function renderBridgeMode(data) {
	$("#bridge_mode").val(data.bridge_mode);
	if(data.log_mode == "debug") {
		$('#logInfo').prop('checked', false);
		$('#logDebug').prop('checked', true);
	} else {
		$('#logInfo').prop('checked', true);
		$('#logDebug').prop('checked', false);
	}
	$("#last_modified").text(data.last_modified);
	let show_error_status = "";
	if (localStorage) {
		show_error_status = localStorage.getItem("gateway_setting_notify");
		if (show_error_status == "true") {
			localStorage.removeItem('gateway_setting_notify');
			$("#restartform").attr("style","display:block");
		}
	}

}
function renderProtocolSettings(data) {
	let socket_table = document.getElementById("socket");
	let mqtt_table = document.getElementById("mqtt");
	let bridgeMode = $("#bridge_mode").val();
	if (bridgeMode == "socket") {
		socket_table.style.display="";
		mqtt_table.style.display="none";
		loadSocketSettings(data);
	} else if (bridgeMode == "mqtt") {
		socket_table.style.display="none";
		mqtt_table.style.display="";
		loadMqttSettings(data);
	} else {
		socket_table.style.display="none";
		mqtt_table.style.display="none";
		console.log("---------------unknown protcol : " + data.bridgeMode);
	}
	debugger;
}

function loadSocketSettings(data) {
	$('#socket tr td input[type="text"]').each(function() {
		if (typeof data[this.name] !== "undefined") {
			this.value = data[this.name];
		}
	});
	
	$('#socket tr td input[type="number"]').each(function() {
		if (typeof data[this.name] !== "undefined") {
			this.value = data[this.name];
		}
	});
	
	$("#socket tr td select").each(function() {
		if (typeof data[this.name] !== "undefined") {
			this.value = data[this.name];
		}
	});
	
	if ((data.remote_host == "test.greenkoncepts.com") ||
			(data.remote_host == "kem.greenkoncepts.com") ||
			(data.remote_host == "kemap.greenkoncepts.com")) {
		$("#optionRemoteHost").val(data.remote_host);
		$("#optionRemoteHost").removeClass('ui-state-disabled');
		$("#manuallyRemoteHost").addClass('ui-state-disabled');
		$("#checkBoxHost").prop('checked', false);
	} else{
		$("#manuallyRemoteHost").val(data.remote_host);
		$("#manuallyRemoteHost").removeClass('ui-state-disabled');
		$("#optionRemoteHost").addClass('ui-state-disabled');
		$("#checkBoxHost").prop('checked', true);
	}	
}

function loadMqttSettings(data) {
	$('#mqtt tr td input[type="text"]').each(function() {
		if (typeof data[this.name] !== "undefined") {
			this.value = data[this.name];
		}
	});
	
	$('#mqtt tr td input[type="number"]').each(function() {
		if (typeof data[this.name] !== "undefined") {
			this.value = data[this.name];
		}
	});
	
	$("#mqtt tr td select").each(function() {
		if (typeof data[this.name] !== "undefined") {
			this.value = data[this.name];
		}
	});
}

function gotoStatusPage() {
	location.href=appRoot + "/gatewaystatus";
}
//	
function saveSettings() {
	let bridge_settings ={};
	bridge_settings.bridge_mode  = $("#bridge_mode").val();
	if (bridge_settings.bridge_mode == "socket") {
		$('#socket tr td input[type="text"]').each(function() {
			bridge_settings[this.name] = this.value
		
		});
		
		$('#socket tr td input[type="number"]').each(function() {
			bridge_settings[this.name] = this.value
		
		});
		
		$("#socket tr td select").each(function() {
			bridge_settings[this.name] = this.value
		});
		
		if ($("#checkBoxHost").is(':checked')) {
			bridge_settings.remote_host = $("#manuallyRemoteHost").val();
		} else {
			bridge_settings.remote_host = $("#optionRemoteHost").val();
		}
		
	} else if (bridge_settings.bridge_mode == "mqtt") {
		$('#mqtt tr td input[type="text"]').each(function() {
			bridge_settings[this.name] = this.value
		
		});
		
		$('#mqtt tr td input[type="number"]').each(function() {
			bridge_settings[this.name] = this.value
		
		});
		
		$("#mqtt tr td select").each(function() {
			bridge_settings[this.name] = this.value
		});
	}
	
	if ($("#logDebug").is(':checked')) {
		bridge_settings.log_mode = "debug";
	} else {
		bridge_settings.log_mode = "info";
	}
	
	debugger;
	
	var request = $.ajax({
		type : "post",
		url: pluginRoot,
		data: {"bridge" : JSON.stringify(bridge_settings)},
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
		if (data.result == "success") {
			if (localStorage) {
				// LocalStorage is supported!
				localStorage.setItem("gateway_setting_notify", "true");
			} else {
				// No support. Use a fallback such as browser cookies or store on the server.
			}
			location.reload();
		} else {
			$("#errorsform").show();
		}

	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
	});
}
//
function exportSettings(){
	window.location.href = pluginRoot + "/gatewaysettings?id=exporting";
}
//	
function cancelSettings() {
	location.reload();
}
//	
function importSettings(){
	$("#selectedFile").trigger("click");
}
//
function importFile() {
	$form = $("#fileupload");
	$form.attr("action", "gatewaysettings/importing");
	$form.attr("enctype","multipart/form-data");
	$form.submit();
}
//
//function defaultSettings() {
//	$form = $("#settingsform");
//	$form.attr("action", "GatewaySettings/default");
//	$form.submit();
//}

var restartWaitTimer;
$(document).ready(function(){
	$.get(pluginRoot, { 'id':'network'}, function(data) {
		//Display network
		renderNetworkDetail(data);
		$('#networkRestartMessage').hide();
		waiting(0);
	}, 'json');
	
	waiting(1, "Network scanning... Please wait !");

	$('input[name=mode]').change(function() {
		if($(this).val() === 'dhcp') {
			console.log('dhcp selected!');
			disableInputFields();
			$.get(pluginRoot, { 'id':'network'}, function(data) {
				setInputFields(data);
			}, 'json');
		} else {
			console.log('static selected!');
			enableInputFields();
		}
	});

	addCustomValidationRules();
	$("#settingsform").validate({
		submitHandler: function(form) {
			saveSettings();
		}
	});
});

function renderNetworkDetail( data )  {
	setInputFields(data);
	if(data.mode == 'static'){
		document.getElementById('mode_d').checked = false;
		document.getElementById('mode_s').checked = true;
		enableInputFields();
	}else{
		document.getElementById('mode_s').checked = false;
		document.getElementById('mode_d').checked = true;
		disableInputFields();
	}
}

function setInputFields(data) {
    $.each(data, function(key, value) {
		if((key != "status") && (key != "mode")) {
			$("#"+key).val(value);
		}	
    });
}

function disableInputFields() {
	$('.disableable').prop('disabled', true);
	$('.disableable').addClass('ui-state-disabled');
}

function enableInputFields() {
	$('.disableable').prop('disabled', false);
	$('.disableable').removeClass('ui-state-disabled');
}

function addCustomValidationRules() {
	$.validator.addMethod('ipv4address', function(value) {
		if (value==="") return true;
		return /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$/.test(value);
	}, "Invalid IP Address");
	
	$.validator.addMethod('remoteHost', function(value) {
		if (value==="") return true;
		return /^(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$|^([\\da-z\\.-]+)\.([a-z\\.]{2,6})$/.test(value);
	}, "Invalid Host Address");
}



function saveSettings() {
	var mode = $('input[name=mode]:checked').val();
	var ip = $("#ip").val();		
	var subnet = $("#subnet").val();
	var gateway = $("#gateway").val();
	var dns1 = $("#dns1").val();
	var	dns2 = $("#dns2").val();
	var ntp1 = $("#ntp1").val();
	var ntp2 =$("#ntp2").val();
	
	var network = {
			"mode" : mode, "ip" : ip, "subnet" : subnet, "gateway" : gateway, "dns1" : dns1, "dns2" : dns2, "ntp1":ntp1, "ntp2" : ntp2 
	}
	
	
	if(confirm("Warning: This will change network interface settings. If you're setting a static address on a different network, you may not be able to reach this gateway at it's hostname. Please check that your settings are correct before clicking OK.") === true) {
		waiting(1, "Saving Changes... Please wait !");
		$.post(pluginRoot, { 'network' : JSON.stringify(network)},function(data, status, xhrObject){
			console.log("===> finish for network setup : Status = " + xhrObject.status);
			waiting(0);
		});

	}
}
	
function waiting(flag, text) {
	console.log("-------------flag = " + flag);
	if (flag == 1) {
		$("#navmenu").addClass("disabledmouse");
		$("#settingsform").addClass("disabledmouse");
		$('#waiting').text(text);
		$('#networkRestartMessage').show();
	} else {
		$("#navmenu").removeClass("disabledmouse");
		$("#settingsform").removeClass("disabledmouse");
		$('#networkRestartMessage').hide();
	}
}


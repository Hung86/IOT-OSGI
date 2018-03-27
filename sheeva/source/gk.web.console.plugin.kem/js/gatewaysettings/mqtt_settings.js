$(document).ready(function(){
	$.get(pluginRoot,{'id':'settings', 'protocol':protocol}, function(data) {
		renderDataGatewaySettings(data);
		processDataGatewaySettings ();
	}, 'json');	

	$(".radiohandler").change(function () {
		if($(this).val() === "on"){
			$('#dradio1').prop('checked', true);
			$('#dradio2').prop('checked', false);
			$('#debug').val('true');
		} else if($(this).val() === "off"){
			$('#dradio1').prop('checked', false);
			$('#dradio2').prop('checked', true);
			$('#debug').val('false');
		}
	});
	
	$(".logserverhandler").change(function () {
		if($(this).val() === "on"){
			$('#enableradio').prop('checked', true);
			$('#disableradio').prop('checked', false);
			$('#enableLogServer').val('true');
			enable($('#remoteLogServer'));
		} else if($(this).val() === "off"){
			$('#enableradio').prop('checked', false);
			$('#disableradio').prop('checked', true);
			$('#enableLogServer').val('false');
			disable($('#remoteLogServer'));
		}
	});
});

function renderDataGatewaySettings(data) {
	$.each(data, function(key, value) {
	    console.log("---pair value : key =  "+ key + " - value = " + value);
		if(key === "errorLength"){
		    if(parseInt(value) > 0) {
				for (i = 0; i < data.errorLength; i++) {	
					$("#" + data.errors[i].error_messages).css("border", "1px solid red");
					$("#" + data.errors[i].error_messages + "_error").show();					
				}
				$("#errorsform").show();
			} else if (parseInt(value) == 0) {
				$("#restartform").attr("style","display:block");
			}
		} else if(key === "last_modified") {
			$("#lastmodified").empty();
			$("#lastmodified").append("<b>Last Modified " + value + " </b>");
		} else {
			$("#"+key).val(value)
		}
    });
}

function processDataGatewaySettings () {	
	var debugLog = $("#debug").val();
	if(debugLog === "false") {
		$('#dradio2').prop('checked', true);
		$('#dradio1').prop('checked', false);
	} else {
		$('#dradio2').prop('checked', false);
		$('#dradio1').prop('checked', true);
	}
	
	var enableLogServer = $("#enableLogServer").val();
	
	if (enableLogServer === "false") {
		$('#enableradio').prop('checked', false);
		$('#disableradio').prop('checked', true);
		disable($('#remoteLogServer'));
	} else {
		$('#enableradio').prop('checked', true);
		$('#disableradio').prop('checked', false);
		enable($('#remoteLogServer'));
	}
}

function disable($object) {
	$object.prop('disabled', true);
	$object.addClass('ui-state-disabled');
}

function enable($object) {
	$object.prop('disabled', false);
	$object.removeClass('ui-state-disabled');
}
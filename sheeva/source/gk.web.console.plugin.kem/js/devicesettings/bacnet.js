	function deleteValidation(row){
		var result = confirm("Are you sure want to delete rule");
		if(result == false)
			return ;
		let validation_id = $(row).find("#id").val();
		$(row).remove();
		var request = $.ajax({
			url: pluginRoot,
			data: {
				'action' : 'delete_validation',
				'validation_id': validation_id,
				'adapter_name' : $("#adapter").val()
			},
			timeout: 15000, //in milliseconds
			dataType: 'json',
			async: 'true',
			type : 'get',
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
	}
	function resetUI(){
		$("#create").show();
		$("#right").hide();
		$("#validation_show").hide();
		let validations = $(".div_validation");
		for(let i = 0 ; i< validations.length; i++){
			$(validations[i]).hide();
		}
		let rows = $("#table_real_node tr");
		for(let i = 0 ; i < rows.length ; i++){
			$(rows[i]).css("border","");
		}
	}
	var validation_id_edit = "";
	function editValidationRule(id){
	
	validation_id_edit = id ;
	var request = $.ajax({
			url: pluginRoot,
			data: {
				'action' : 'edit_validation',
				'validation_id': id,
				'device_address' : $("#device_address").val(),
				'device_instanceid' : $("#device_instanceid").val(),
				'adapter_name' : $("#adapter").val()
			},
			timeout: 15000, //in milliseconds
			dataType: 'json',
			async: 'true',
			type : 'get',
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
			
			let condition = parseExpression(data.condition) ;
			
			let action    = data.action.split("=");
			$("#validation").trigger("click");
			let rowNumber = Math.round(condition.length/4);
			condition.push("&&");
			for(let i = 0 ; i < rowNumber-1 ; i++){
				$("#expand_condition").trigger("click");
			}
			let pointer = $(".div_validation :visible");
			// Condition
			let conditionRow = $(pointer).find("#condition tr");
			let j = 0 ;
			for(let i = 0 ; i < conditionRow.length ;i++){
				let operator = $(conditionRow[i]).find("#operator");
				$(operator).val(condition[j++]);
				let expression1 = $(conditionRow[i]).find("#expression1");
				$(expression1).val(condition[j++]);
				let value = $(conditionRow[i]).find("#value");
				$(value).val(condition[j++]);
				let logic = $(conditionRow[i]).find("#logic");
				$(logic).val(condition[j++]);
			}
			// Action
			let actionRows = $(pointer).find("#action tr");
			let name =  $(actionRows[0]).find("#name");
			$(name).html(data.name);
			let operator =  $(actionRows[0]).find("#operator");
			$(operator).val(action[1]);
		});	
	}
	function parseExpression(expression){
		expression = expression.replaceAll("(","");
		expression = expression.replaceAll(")","");
		let opteratorList = ["<" , ">" , "=" , "!", "&", "|"];
		let parser_array = [];
		let start_index = 0;
		let idx = 0;
		while(idx < expression.length) {
			let token = expression.slice(idx,idx+1);
			if (opteratorList.indexOf(token) != -1){
				parser_array.push(expression.slice(start_index, idx));
				if ((token != "<") && (token != ">")) {
					 idx++;
					 token = token + expression.slice(idx,idx+1);
				}
				parser_array.push(token);
				start_index = idx + 1;
			}
			idx++;
		}
		parser_array.push(expression.slice(start_index, idx));
		return parser_array ;
	}
	
	String.prototype.replaceAll = function(target, replacement) {
		return this.split(target).join(replacement);
	};
	
	var data_point_delete = [];
	function deleteNode(row){
		let data_point = $(row).find("#data_point");
		data_point_delete.push($(data_point).val());
		$(row).remove();
	}
	
	function loadVirtualNode(dataPoint){
		let table = $("#virtual_body");
		let tr = document.createElement("tr");
		table.append(tr);
		
		if(typeof dataPoint.data_point  !== "undefined"){
			var hidden = document.createElement("input");
			hidden.type = "hidden";
			hidden.value = dataPoint.data_point ;
			hidden.id = "data_point";
			tr.appendChild(hidden);
		}
		
		
		var td = document.createElement("td");
		td.style.border = "0";
		td.style.textAlign = "left" ;
		var label = document.createElement("label");
		label.innerHTML = indexVirtual + 1 ;
		label.id = "index";
		label.value = indexVirtual ;
		td.appendChild(label);
		tr.appendChild(td);
		// Name
		var td = document.createElement("td");
		td.style.border = "0";
		td.style.textAlign = "left" ;
		var input = document.createElement("input");
		input.type = "text"
		input.id = "name";
		input.className = "ui-corner-all inputText";
		if(typeof dataPoint.name === "undefined")
			dataPoint.name = "";
		input.value = dataPoint.name ;
		td.appendChild(input);
		tr.appendChild(td);
		// Formula
		var td = document.createElement("td");
		td.style.border = "0";
		td.style.textAlign = "left" ;
		var label = document.createElement("label");
		label.innerHTML = dataPoint.formula ;
		
		label.value = dataPoint.formula ;
		label.id = "formula" ;
		label.style.wordBreak = "break-all";
		td.appendChild(label);
		tr.appendChild(td);
		// Channel
		var td = document.createElement("td");
		td.style.border = "0";
		td.style.textAlign = "left" ;
		var input = document.createElement("input");
		input.id = "channel";
		input.className = "ui-corner-all inputText";
		input.type  = "text" ;
		input.style.width = "95%";
		if(typeof dataPoint.channel === "undefined")
			dataPoint.channel = "";
		input.value = dataPoint.channel ;
		td.appendChild(input);
		tr.appendChild(td);
		// Measure Name
		var td = document.createElement("td");
		td.style.border = "0";
		td.style.textAlign = "left" ;
		// var input = document.createElement("input");
		// input.className = "ui-corner-all inputText";
		// input.id = "measure_name";
		var select = $("<select></select>").attr({
			"type" : "text",
			"id"   : "measure_name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		// .val(dataPoint.measure_name)
		.change(function(){
			changeUnitBacNet(this);
		});var option = $("<option></option>").val("null").html("Select");
		select.append(option);
		for(var i in nameList){
			var option = $("<option></option>").val(nameList[i]).html(nameList[i]);
			select.append(option);
		}
		if(typeof dataPoint.measure_name === "undefined" || dataPoint.measure_name == "null"){
			select.selectedIndex = 0 ;
		}
		else
			$(select).val(dataPoint.measure_name);
		$(td).append(select);
		tr.appendChild(td);
		// Unit
		var td = document.createElement("td");
		td.style.border = "0";
		td.style.textAlign = "left" ;
		var select = $("<select></select>").attr({
			"id"   : "measure_unit"
		}).css("width","100%").addClass("ui-corner-all inputText");
		var option = $("<option></option>").val("null").html("Select");
		select.append(option);
		for(var i in listUnit){
			var option = $("<option></option>").val(listUnit[i]).html(listUnit[i]);
			select.append(option);
		}
		if(typeof dataPoint.measure_unit === "undefined"|| dataPoint.measure_unit == "null"){
			select.selectedIndex = 0 ;
		}
		else
			$(select).val(dataPoint.measure_unit);
		$(td).append(select);
		tr.appendChild(td);
		
		
		
		// var td = $("<td></td>").css({
			// "border" : "0",
			// "text-align" : "left"
		// });
		// var checkbox = document.createElement("INPUT");
		// checkbox.setAttribute("type", "checkbox");
		// checkbox.id = "consumption";
		// if(dataPoint.consumption === "true"){
			// checkbox.checked = true ;
		// }
		// else {
			// checkbox.checkbox = false ;
		// }
		
		// $(td).append(checkbox);
		// $(tr).append(td);
		
		
		// Validation Rule
		var td = $("<td></td>").css({
			"border" : "0",
			"text-align" : "left"
		});
		var button = document.createElement("button");
		button.type = "button" ;
		button.id = "validation";
		button.innerHTML = "Add";
		button.className = "ui-corner-all ui-state-hover" ;
		$(td).append(button);
		$(tr).append(td);
		
		
		var td = $("<td></td>").css("border","0");
		var delButton = document.createElement('BUTTON');
		delButton.className = "ui-state-default ui-corner-all"
		delButton.innerHTML = "<span class='ui-icon ui-icon-trash'> </span>";
		delButton.type = "button";
		delButton.onclick = function(){
			deleteNode(tr);
		}
		$(td).append(delButton);
		$(tr).append(td);
		
		
		indexVirtual++;
	}
	
	function scanDevice(){
		
		var alert = confirm("Warning – Performing a Scan will erase ALL previously stored Mapping Data. Do you want to proceed ?");
		if (alert == false)
			return ;
		$("#scanning_wait").show();
		$.get(pluginRoot, {
		'scan'  : 'true',
		'addressId': $("#device_address").val(),
		'adapterName' : $("#adapter").val(),
		'instanceId'  : $("#device_instanceid").val()
		}, function (data) {
			$("#scanning_wait").hide();
			// Reset
			allIndex = [];
			
			getChannel($("#adapter").val(),1);
		}, 'json');
		fillData();
	}
	
	// fill Data to create Vitural Node
	
	function fillData(){
		
		object_identifier_fill = allObjectIdentify ;
		$("#operator0,#operator").empty();
		var option = $("<option></option>").val("null").html("Select");
		$("#operator0,#operator").append(option);
		for(var i in object_identifier_fill){
			if(typeof  object_identifier_fill[i] === "undefined"){
				continue ;
			}
			let object_identifier = object_identifier_fill[i].object_identifier;
			let measure_name      = object_identifier_fill[i].measure_name ;
			if(typeof measure_name === "undefined" || measure_name == "null"
			|| measure_name == ""
			)
				continue ;
			let data_point  = object_identifier_fill[i].object_identifier ;
			;
			// let name = measure_name + "(" + object_identifier + ")";
			var option = $("<option></option>").val(data_point).html(data_point);
			$("#operator0,#operator").append(option);
		}
	}
	
	var object_identifier_fill ;
	
	
	
	function write(dataBacNet){
		let request = $.ajax({
			type : "post",
			url: pluginRoot + '/writeChannel',
			data: {
			"adapterName" : $("#adapter").val(),
			"address"     : $("#device_address").val(),
			"deviceInstance" : $("#device_instanceid").val(),
			"delete" : JSON.stringify(data_point_delete),
			"update" : JSON.stringify(dataBacNet)},
			
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
			
			getChannel($("#adapter").val(),indexWrite) ;

			$('#page').removeClass('disabledmouse');
			;
			console.log("----------------data = " + data);
			if(data == null){
				showSuccessForm();
				return ;
			}
			if (data.errors.length > 0) {
				showErrorForm(data.errors);
			}
		});
		request.fail(function (jqXHR, textStatus, errorThrown) {
			alert("No Response");
			$('#page').removeClass('disabledmouse');
			console.log(textStatus + "---- "+ errorThrown);
			
		});
	}
	
	function writeBacNet(){
		if(active_tab == "scanning"){
			writeScanning();
			return ;
		}
		if(active_tab == "write"){
			writingBacnet();
			return ;
		}
	
		let dataBacNet = [];
		$('#page').addClass('disabledmouse');
		if($("#table_real_node").is(":visible")){
			let tr = $("#table_real_node > tbody > tr") ;
			let vaild = true ;
			let array_tr = [] ;
			
			for(let i = 0 ; i < tr.length ; i ++ ){
				let check_name = false ;
				let check_channel = false ;
				let check_measure_name = false ;
				let check_measure_unit = false ;
				let check_measure_ratio = false ;
				let dataPoint = {}; 
				dataPoint.data_point = $(tr[i]).find("#data_point").val();
				if(typeof dataPoint.data_point === "undefined")
					continue ;
				dataPoint.object_identifier = $(tr[i]).find("#object_identifier").val() ;
				dataPoint.oi_measure_name  = $(tr[i]).find("#oi_measurement_name").val();
				dataPoint.oi_measure_unit  = $(tr[i]).find("#oi_measure_unit").val();
				dataPoint.type   = $(tr[i]).find("#type").val();
				dataPoint.measure_ratio   = $(tr[i]).find("#measure_ratio").val();
				
				if(dataPoint.measure_ratio != "")
					check_measure_ratio = true ;
				dataPoint.channel       = $(tr[i]).find("#channel").val();
				if(dataPoint.channel != "")
					check_channel = true ;
				dataPoint.measure_name      = $(tr[i]).find("#measure_name").val();
				if(dataPoint.measure_name != "" && dataPoint.measure_name != "null")
					check_measure_name = true ;
				dataPoint.measure_unit          = $(tr[i]).find("#measure_unit").val();
				if(dataPoint.measure_unit != "" && dataPoint.measure_unit != "null")
					check_measure_unit = true ;
				dataPoint.consumption = $(tr[i]).find("#consumption").prop("checked");
				dataPoint.name = $(tr[i]).find("#name").val();
				// if(dataPoint.name != "")
					// check_name = true ;
				if(check_channel == false 
				&& check_measure_name == false && check_measure_unit == false && check_measure_ratio == false
				
				){
					continue ;
				}
				if((check_channel && check_measure_name && check_measure_ratio
				 && check_measure_unit) == false ){
					alert("You must fill all field");
					$('#page').removeClass('disabledmouse');
					$(tr[i]).css("border","2px solid red");
					return ;
				}
				dataBacNet.push(dataPoint);
			}
		
			write(dataBacNet);
			
		}
		if($("#table_virtual_node").is(":visible")){
			$("#table_virtual_node > tbody > tr").each(function(){
				var dataPoint = {};
				 ;
				// dataPoint.formula = $(this).find("#formula").val();
				// if(typeof dataPoint.formula === "undefined")
					// return ;
				dataPoint.channel = $(this).find("#channel").val();
				dataPoint.measure_name = $(this).find("#measure_name").val();
				dataPoint.measure_unit  = $(this).find("#measure_unit").val();
				dataPoint.formula =  $(this).find("input#formula").val();
				dataPoint.consumption =  $(this).find("input#consumption").prop('checked');
				if(dataPoint.formula == null){
					dataPoint.formula = $(this).find("#formula").val();
				}
				
				
				// dataPoint.data_point = $(this).find("#index").val().toString();
				var data_point = $(this).find("#data_point").val();
				if(typeof data_point !== "undefined"){
					dataPoint.data_point = data_point ;
				}else{
					return ;
				}
				dataPoint.name = $(this).find("#name").val();
				dataBacNet.push(dataPoint);
			});
			
			write(dataBacNet);
		}
		
		
	}
	$(document).on("change","input[type = 'checkbox']",function(){
		if(this.checked) {
			$(this).val("true");
		}
		else{
			$(this).val("false");
		}
		
	});
	
	function renderDeviceInstance(adapterName,valueAddress){
		$("#device_instanceid").empty();
		let adapter = all_adapter[adapterName];
		for(let i = 0 ; i < adapter.length ; i ++){
			if(adapter[i].device_address == valueAddress){
				var option = $("<option></option>").val(adapter[i].device_instanceid).text(adapter[i].device_instanceid);
				$("#device_instanceid").append(option); 
			}
		}
	}
	
	function renderDeviceAddress(adapterName,valueAddress){
		
		$("#device_address").empty();
		
		// for(var i in all_adapter){
			var adapters = all_adapter[adapterName];
			var deviceAddress = [];
			
			for(var key in adapters){
				
				deviceAddress.push(adapters[key].device_address);
			}
			var uniqueNames = [] ;
			$.each(deviceAddress, function(i, el){
				if($.inArray(el, uniqueNames) === -1) uniqueNames.push(el);
			});
			for(var key in uniqueNames){
				
				var option = $("<option></option>").val(uniqueNames[key]).text(uniqueNames[key]);
				if(uniqueNames[key] == valueAddress){
					$(option).attr('selected','selected');
				}
				$("#device_address").append(option); 
			}
			
		// }
	}
	
	function onchangeDeviceAddress(){
		let adapterName = $("#adapter").val();
		let address = $("#device_address").val();
		
		renderDeviceInstance(adapterName,address);
		$("#device_instanceid").trigger("change");
	}
	
	function onchangeAdapter(){
		localStorage.setItem("tab","real_node");
		$("#bacnet").show();
		$("#modbus").hide();
		// clear DropDownList
		$("#device_address").empty();
		var defaultValue  = $("<option></option>").val("null").text("Select");
		$("#device_address").append(defaultValue);
		// Fill out data to DropDownList
		var adapterName = $("#adapter").val();
		var address = getAddress(adapterName)[0];
		// var category = getCategory(0, adapterName);
		
		renderDeviceAddress(adapterName,address);
		$("#device_address").trigger("change");
	}
	function onchangeDeviceInstance(){
	
		let addressId  = $("#device_address").val() ;
		let adapterName = $("#adapter").val() ;
		let adapter = all_adapter[adapterName];
		for(let i = 0 ; i< adapter.length ; i ++){
			if(adapter[i].device_address == addressId && 
			adapter[i].device_instanceid  == this.value
			){
				$("#device_name").html(adapter[i].device_name);
			}
		}
		
		$(".div_validation").hide();
		let category = getCategory(addressId, adapterName);
		goToDeviceSettings(addressId,category,adapterName);
		active_tab = "scanning";
		
		// for filter bacnet
		type_bacnet = "null" ;
		$("#type_bacnet").show();
		// scanning(adapterName, 1) ;
		// fillData();
	}
	function scanning(adapterName,indexPage){
		
		console.trace();
		var addressId = $("#device_address").val();
		var instanceId = $("#device_instanceid").val();
		var category = getCategory(addressId, adapterName);
	
		renderDeviceName(adapterName,instanceId,addressId);
		
		
		$.get(pluginRoot, {
		'addressId': addressId,
		'adapterName' : adapterName,
		'instanceId'  : instanceId,
		'indexPage'  : indexPage,
		'record_num' : record_num,
		'type' : type_bacnet,
		'scanning' : 'true'
	}, function (data) {
		
		channels = data.device_attributes ;
		
		
		// add object identify
		if(allIndex.indexOf(indexWrite) == "-1"){
			allObjectIdentify = allObjectIdentify.concat(channels);
			allIndex.push(indexWrite);
		}
		
		if(typeof channels !== "undefined"){
			buildHtmlBacnetScanning(channels);
			
			totalPage = data.total ;
			if(totalPage == 1){
				$("#real_node #pagination").hide();
			}
			$("#record_num").val(record_num);
			$("#page_num").val(indexWrite);
			
			$("#virtual").hide();
			$("#real").show();
		}
		
		// Disable link Next, Last
		if(indexWrite == totalPage){
			$("#last").addClass("disabledmouse");
			
			$("#next").addClass("disabledmouse");
		}else{
			$("#last").removeClass("disabledmouse");
			$("#next").removeClass("disabledmouse");
		}
		// Disable link First, Previous
		if(indexWrite == 1){
			$("#first").addClass("disabledmouse");
			$("#previous").addClass("disabledmouse");
		}else{
			$("#first").removeClass("disabledmouse");
			$("#previous").removeClass("disabledmouse");
		}
		$("#current_page").html("Page "+ indexWrite + " of " + totalPage);
		
		fillData();
		resetUI();
	}, 'json');
		
		
	}
	
	function buildHtmlBacnet(data){
	var table = $("#real_body") ;
	$(table).empty();
	
	for(var i in data){
		var dataPoint  = data[i];
		var tr = $("<tr></tr>");
		
		var input = document.createElement("INPUT");
		input.setAttribute("type", "hidden");
		input.id = "data_point" ;
		input.value = dataPoint.data_point ;
		tr.append(input);
		
		var input = document.createElement("INPUT");
		input.setAttribute("type", "hidden");
		input.id = "type" ;
		input.value = dataPoint.type ;
		tr.append(input);
		
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var label = $("<label></label>").attr({
			"id" : "index" 
		}).html(parseInt(i)+1) ;
		td.append(label);
		tr.append(td);
		
		// Object Identify
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "object_identifier"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.object_identifier);
		$(input).attr('readonly',true);
		td.append(input);
		tr.append(td);
		
		// Measure Name
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var input = $("<input></input>").attr({
				"type" : "text",
				"id"   :  "oi_measurement_name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.oi_measure_name);
		$(input).attr('readonly', true);
		td.append(input);
		tr.append(td);
		
		// Unit
		var td = $("<td></td>").css({
			"border" : "0",
			"border-right": "2px solid white"
		})
		.addClass("bacnet");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "oi_measure_unit"
		}).css("width","90%").addClass("ui-corner-all inputText")
		.val(dataPoint.oi_measure_unit);
		$(input).attr('readonly', true);
		td.append(input);
		tr.append(td);
		// // space
		// var td = $("<td></td>").css("border","0")
		// tr.append(td);
		
		// Name
		var td = $("<td></td>").css({
			"border" : "0"
		})
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.name);
		td.append(input);
		tr.append(td);
		
		// Channel
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "channel"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.channel);
		;
		td.append(input);
		tr.append(td);
		
		// Measure
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var select = $("<select></select>").attr({
			"type" : "text",
			"id"   : "measure_name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		// .val(dataPoint.measure_name)
		.change(function(){
			changeUnitBacNet(this);
		});
		var option = $("<option></option>").val("null").html("Select");
		select.append(option);
		nameList.sort();
		for(var i in nameList){
			var option = $("<option></option>").val(nameList[i]).html(nameList[i]);
			select.append(option);
		}
		if(typeof dataPoint.measure_name === "undefined" || dataPoint.measure_name == "null"){
			select.selectedIndex = 0 ;
		}
		else
			$(select).val(dataPoint.measure_name);
		td.append(select);
		tr.append(td);
		
		
		// Unit
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var select = $("<select></select>").attr({
			// "type" : "text",
			"id"   : "measure_unit"
		}).css("width","100%").addClass("ui-corner-all inputText");
		var option = $("<option></option>").val("null").html("Select");
		select.append(option);
		for(var i in listUnit){
			var option = $("<option></option>").val(listUnit[i]).html(listUnit[i]);
			select.append(option);
		}
		if(typeof dataPoint.measure_unit === "undefined"|| dataPoint.measure_unit == "null"){
			select.selectedIndex = 0 ;
		}
		else
			$(select).val(dataPoint.measure_unit);
		td.append(select);
		tr.append(td);
		
		// Data Scalar 
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "measure_ratio"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.measure_ratio);
		;
		td.append(input);
		tr.append(td);
		
		// Calculated measure
		var td = $("<td></td>").css({
			"border" : "0",
			"text-align" : "center"
		}).addClass("mapping");
		
		var checkbox = document.createElement("INPUT");
		checkbox.type = "checkbox" ;
		checkbox.id = "consumption";
		if(dataPoint.consumption === "true"){
			checkbox.checked = true ;
		}
		else {
			checkbox.checkbox = false ;
		}
		td.append(checkbox);
		tr.append(td);
		
		// Validation Rule
		var td = $("<td></td>").css({
			"border" : "0",
			"text-align" : "center"
		}).addClass("mapping");
		var button = document.createElement("Button");
		button.type = "button" ;
		button.id = "validation";
		button.innerHTML = "Add";
		button.className = "ui-corner-all ui-state-default";
		
		td.append(button);
		tr.append(td);
		
		
		var td = $("<td></td>").css("border","0").addClass("mapping");
		var delButton = document.createElement('BUTTON');
		delButton.className = "ui-state-default ui-corner-all"
		delButton.innerHTML = "<span class='ui-icon ui-icon-trash'> </span>";
		delButton.type = "button";
		delButton.id   = "real_delete";
		let row = tr ;
		delButton.onclick = function(){
			deleteNode(row);
		}
		td.append(delButton);
		tr.append(td);
		
		table.append(tr);
	}
}

var indexWrite = 1 ;
var total_vitural = "";
var device_attributes_vitural ;


function renderDeviceName(adapterName,instanceId,addressId){
	let adapter = all_adapter[adapterName];
	for(let i = 0 ; i< adapter.length ; i ++){
		if(adapter[i].device_address == addressId && 
		adapter[i].device_instanceid  == instanceId
		){
			$("#device_name").html(adapter[i].device_name);
			break ;
		}
	}

}
function getCategory(address, adapterName) {
	let adapter =  all_adapter[adapterName];
	for(let i = 0 ; i< adapter.length ; i ++){
		if(adapter[i].device_address == address){
			return adapter[i].device_category ;
		}
	}
	console.log("Category is not vaild");
	return "";
}

var totalPage = "" ;
var allObjectIdentify = [];
var allIndex = [] ;
function getChannel(adapterName,indexPage) {
	console.trace();
	
	var addressId = $("#device_address").val();
	var instanceId = $("#device_instanceid").val();
	var category = getCategory(addressId, adapterName);
	
	renderDeviceName(adapterName,instanceId,addressId);
	
	var channels = '';
	var tab = localStorage.getItem("tab");
	if(tab == "validation_node"){
		$("#validation_node").trigger("click");
		return ;
	}else if (tab == null){
		tab = "real_node";
	}
	
	$.get(pluginRoot, {
		'channel'  : 'true',
		'addressId': addressId,
		'adapterName' : adapterName,
		'instanceId'  : instanceId,
		'indexPage'  : indexPage,
		'tab' : tab,
		'type' : type_bacnet,
		'record_num' : record_num ,
		'isCreateNew' : isCreateNew
	}, function (data) {
		
		isCreateNew = false ;
		
		channels = data.device_attributes ;
		total_vitural = data.total_vitural ;
		device_attributes_vitural = data.device_attributes_vitural;
		
		
		// add object identify
		if(allIndex.indexOf(indexWrite) == "-1"){
			allObjectIdentify = allObjectIdentify.concat(channels);
			allIndex.push(indexWrite);
		}
		if(active_tab == "read"){
			
			// Real Node
			if(typeof channels !== "undefined"){
				
				let dataChannels = [];
				for(let i = 0 ; i < channels.length;i++){
					if(channels[i].type == "data"){
						dataChannels.push(channels[i]);
					}
				}
				
				buildHtmlBacnet(dataChannels);

				totalPage = data.total ;
				if(totalPage == 1){
					$("#real_node #pagination").hide();
				}
				$("#record_num").val(record_num);
				$("#page_num").val(indexWrite);
				
				$("#virtual").hide();
				$("#real").show();
			}
			// Vitural Node
			if(typeof data.device_attributes_vitural !== "undefined"){
				
				
				$("#virtual_body").empty();
				indexVirtual = 0 ;
				for(let i = 0 ; i< data.device_attributes_vitural.length;i++){
					loadVirtualNode(data.device_attributes_vitural[i]);
				}
				totalPage = total_vitural ;
				
				if(totalPage == 1){
					$("#virtual #pagination").hide();
				}else{
					$("#virtual #pagination").show();
				}
				
				$("#virtual #record_num").val(record_num);
				$("#virtual #page_num").val(indexWrite);
				
				$("#real").hide();
				$("#virtual").show();
			}
		}
		if(active_tab == "scanning"){
			if(typeof channels !== "undefined"){
				buildHtmlBacnetScanning(channels);
				

				totalPage = data.total ;
				if(totalPage == 1){
					$("#real_node #pagination").hide();
				}
				$("#record_num").val(record_num);
				$("#page_num").val(indexWrite);
				
				$("#virtual").hide();
				$("#real").show();
			}
			
		}
		if(active_tab == "write"){
			if(typeof channels !== "undefined"){
				buildHtmlBacnetScanning(channels);

				totalPage = data.total ;
				if(totalPage == 1){
					$("#real_node #pagination").hide();
				}
				$("#record_num").val(record_num);
				$("#page_num").val(indexWrite);
				
				$("#virtual").hide();
				$("#real").show();
			}
			
		}
		
		// Disable link Next, Last
		
		let pointer = $("#virtual").is(":visible") ? $("#virtual") : $("#real");
		if(indexWrite == totalPage){
			$(pointer).find("#last").addClass("disabledmouse");
			$(pointer).find("#next").addClass("disabledmouse");
		}else{
			$(pointer).find("#last").removeClass("disabledmouse");
			$(pointer).find("#next").removeClass("disabledmouse");
		}
		// Disable link First, Previous
		if(indexWrite == 1){
			$(pointer).find("#first").addClass("disabledmouse");
			$(pointer).find("#previous").addClass("disabledmouse");
		}else{
			$(pointer).find("#first").removeClass("disabledmouse");
			$(pointer).find("#previous").removeClass("disabledmouse");
		}
		
		$(pointer).find("#current_page").html("Page "+ indexWrite + " of " + totalPage);
		
		fillData();
		resetUI();
	}, 'json');
}

function goToDeviceSettings(addressId, category, adapterName) {
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
	request.done(function (data) {
		$('#adaptersettings').empty();
		$('#adaptersettings').append(data);
		getChannel(adapterName,1);
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		$('#adaptersettings').empty();
	});
}
function getAddress(adapterName) {
	let result = [];
	let adapter = all_adapter[adapterName] ;
	for(let i = 0 ; i< adapter.length ; i ++){
		if(adapter[i].device_address == "")
			continue ;
		result.push(adapter[i].device_address);
	}
	return result ;
}

function getInstanceId(address, adapterName){
	for(var key in all_adapter){
		if(key == adapterName){
			var devices = all_adapter[key];
			for(var i  in devices){
				if(devices[i].device_address == address)
					return devices[i].device_instanceid ;
			}
		}
	}
	console.log("instanceId is not vaild");
	return "";
}
function renderDevice(data,adapterName){
	
	let addressId = data[adapterName][0].device_address ;
	let instanceId = data[adapterName][0].device_instanceid ;
	let category = data[adapterName][0].device_category ;
	// goToDeviceSettings( addressId, category, adapterName); 
	localStorage.setItem("tab","real_node");
}
function goToDeviceSetttings(adapterName,category,deviceIntanceId){
	
	var request = $.ajax({
    url: pluginRoot,
    data: {
		'action': 'html_page',
		'page': 'template/device_settings.html',
		'adapter': String(adapterName),
		'category': String(category),
		'addressId': String(deviceIntanceId)
    },
    // timeout: 1500, //in milliseconds
    dataType: 'html',
    async: 'true',
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
		$('#adaptersettings').empty();
		$('#adaptersettings').append(data);
	});
	request.fail(function (jqXHR, textStatus, errorThrown) {
		$('#adaptersettings').empty();
	});
}
var record_num = 20 ;
$(document).on("click","#go",function(){
	let adapterName = $("#adapter").val();
	
	let page_num  = $(this).closest("div").find("#page_num");
	let value = $(page_num).val() == "" ? 1 :  $(page_num).val();
	
	let record_num_element =  $(this).closest("div").find("#record_num");
	
	if($(record_num_element).val() != ""){
		record_num = $("#record_num").val();
	}
	
	if(value > totalPage){
		alert("Max page is "+ totalPage);
		return ;
	}
	
	indexWrite = value ;
	getChannel(adapterName,value);
	$('input[name=bacnet_type]').attr('checked',false);
	
});

function first(){
	$('input[name=bacnet_type]').attr('checked',false);
	indexWrite = 1 ;
	let adapterName = $("#adapter").val();
	if(active_tab == "scanning"){
		scanning(adapterName,indexWrite);
		return ;
	}
	getChannel(adapterName,1);
}

function previous(){
	$('input[name=bacnet_type]').attr('checked',false);
	if(parseInt(indexWrite) > 1){
			indexWrite --;
	}
	let adapterName = $("#adapter").val();
	if(active_tab == "scanning"){
		scanning(adapterName,indexWrite);
		return ;
	}
	getChannel(adapterName,indexWrite);	
}

function next(){
	$('input[name=bacnet_type]').attr('checked',false);
	if(parseInt(indexWrite) >= totalPage){
		indexWrite == totalPage ;
		return ;
	}else{
		indexWrite ++ ;
	}
	let adapterName = $("#adapter").val();
	if(active_tab == "scanning"){
		scanning(adapterName,indexWrite);
		return ;
	}
	getChannel(adapterName,indexWrite);
}
function last(){
	$('input[name=bacnet_type]').attr('checked',false);
	indexWrite = totalPage ;
	let adapterName = $("#adapter").val();
	if(active_tab == "scanning"){
		scanning(adapterName,indexWrite);
		return ;
	}
	getChannel(adapterName,totalPage);
}

$(document).on("change","#condition #operator",function(){
	if($(this).val() == "constant"){
		let nextElement= $(this).next();
		$(nextElement).show();
		$(this).hide();
	}
});


$(document).on("click","#keyin",function(){
	$("#right table:first").hide();
	$("#keyin_table").show();
	
});
$(document).on("click","#back",function(){
	$("#right table:first").show();
	$("#keyin_table").hide();
	
});
function createNode(){
	$("#create_object").show();
	$("#real .div_validation").hide();
}
$(document).on("click","#cancel_",function(){
	$("#create_object").hide();
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

function buildHtmlBacnetScanning(data){
	console.log(data);
	var table = $("#scan_real_body") ;
	$(table).empty();
	console.log("--------------------------------");
	for(var i in data){
		var dataPoint  = data[i];
		var tr = $("<tr></tr>");
		
		var input = document.createElement("INPUT");
		input.setAttribute("type", "hidden");
		input.id = "data_point" ;
		input.value = dataPoint.data_point ;
		tr.append(input);
		
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var label = $("<label></label>").attr({
			"id" : "index" 
		}).html(parseInt(i)+1) ;
		td.append(label);
		tr.append(td);
		
		// Object Identify
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "object_identifier"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.object_identifier);
		$(input).attr('readonly',true);
		td.append(input);
		tr.append(td);
		
		// Measure Name
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var input = $("<input></input>").attr({
				"type" : "text",
				"id"   :  "oi_measurement_name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.oi_measure_name);
		$(input).attr('readonly', true);
		td.append(input);
		tr.append(td);
		
		// Unit
		var td = $("<td></td>").css({
			"border" : "0",
			"border-right": "2px solid white"
		})
		.addClass("bacnet");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "oi_measure_unit"
		}).css("width","90%").addClass("ui-corner-all inputText")
		.val(dataPoint.oi_measure_unit);
		$(input).attr('readonly', true);
		td.append(input);
		tr.append(td);
		
		// Type
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var select = $("<select></select>").attr({
			// "type" : "text",
			"id"   : "type"
		}).css("width","100%").addClass("ui-corner-all inputText");
		var option = $("<option></option>").val("").html("None");
		select.append(option);
		var option = $("<option></option>").val("data").html("Data");
		select.append(option);
		var option = $("<option></option>").val("setting").html("Setting");
		select.append(option);
		
		
		// if(typeof dataPoint.type === "undefined"|| dataPoint.type == "null"){
			// select.selectedIndex = 0 ;
		// }
		// else
			
			console.log(dataPoint.type)
			$(select).val(dataPoint.type);
		td.append(select);
		tr.append(td);
		table.append(tr);
		
	}
	console.log("--------------------------------");
	
	
}
function writeScanning(type){
	$('#page').addClass('disabledmouse');
	let rows = $("#scan_real_body > tr");
	let dataWrite = [];
	for(let i = 0 ; i< rows.length ; i++){
		 
		let dataPoint = {} ;
		dataPoint.data_point = $(rows[i]).find("#data_point").val();
		if(typeof dataPoint.data_point === "undefined")
			continue ;
		// dataPoint.object_identifier = $(rows[i]).find("#object_identifier").val() ;
		// dataPoint.oi_measure_name  = $(rows[i]).find("#oi_measurement_name").val();
		// dataPoint.oi_measure_unit  = $(rows[i]).find("#oi_measure_unit").val();
		
		/*dataPoint.type =  $(rows[i]).find("#type").val();
		if(typeof dataPoint.type == "null" || typeof dataPoint.type == "undefined"){
			continue ;
		}*/
		if(type != ""){
			dataPoint.type = type || $(rows[i]).find("#type").val();
		}
		if(typeof dataPoint.type == "null" /*|| typeof dataPoint.type == "undefined"*/){
			continue ;
		}
		dataWrite.push(dataPoint);
	}
	
	let request = $.ajax({
			type : "post",
			url: pluginRoot + '/writeScanning',
			data: {
			"adapterName" : $("#adapter").val(),
			"address"     : $("#device_address").val(),
			"deviceInstance" : $("#device_instanceid").val(),
			"delete" : JSON.stringify(data_point_delete),
			"update" : JSON.stringify(dataWrite)},
			
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
			scanning($("#adapter").val(),indexWrite) ;
			$('#page').removeClass('disabledmouse');
		});
		request.fail(function (jqXHR, textStatus, errorThrown) {
			alert("No Response");
			$('#page').removeClass('disabledmouse');
			console.log(textStatus + "---- "+ errorThrown);
		});
}
function writing(adapterName,indexPage){
		var addressId = $("#device_address").val();
		var instanceId = $("#device_instanceid").val();
		var category = getCategory(addressId, adapterName);
	
		renderDeviceName(adapterName,instanceId,addressId);
		
		
		$.get(pluginRoot, {
		'addressId': addressId,
		'adapterName' : adapterName,
		'instanceId'  : instanceId,
		'indexPage'  : indexPage,
		'record_num' : record_num,
		'type' : type_bacnet,
		'writing' : 'true'
	}, function (data) {
		 ;
		channels = data.device_attributes ;
		
		
		// add object identify
		if(allIndex.indexOf(indexWrite) == "-1"){
			allObjectIdentify = allObjectIdentify.concat(channels);
			allIndex.push(indexWrite);
		}
		
		if(typeof channels !== "undefined"){
			buildHtmlBacnetWriting(channels);

			totalPage = data.total ;
			if(totalPage == 1){
				$("#real_node #pagination").hide();
			}
			$("#record_num").val(record_num);
			$("#page_num").val(indexWrite);
			
			$("#virtual").hide();
			$("#real").show();
		}
		
		// Disable link Next, Last
		if(indexWrite == totalPage){
			$("#last").addClass("disabledmouse");
			
			$("#next").addClass("disabledmouse");
		}else{
			$("#last").removeClass("disabledmouse");
			$("#next").removeClass("disabledmouse");
		}
		// Disable link First, Previous
		if(indexWrite == 1){
			$("#first").addClass("disabledmouse");
			$("#previous").addClass("disabledmouse");
		}else{
			$("#first").removeClass("disabledmouse");
			$("#previous").removeClass("disabledmouse");
		}
		$("#current_page").html("Page "+ indexWrite + " of " + totalPage);
		
		fillData();
		resetUI();
	}, 'json');
}
function buildHtmlBacnetWriting(data){
	var table = $("#writing_real_body") ;
	$(table).empty();
	
	for(var i in data){
		var dataPoint  = data[i];
		var tr = $("<tr></tr>");
		
		var input = document.createElement("INPUT");
		input.setAttribute("type", "hidden");
		input.id = "data_point" ;
		input.value = dataPoint.data_point ;
		tr.append(input);
		
		var input = document.createElement("INPUT");
		input.setAttribute("type", "hidden");
		input.id = "type" ;
		input.value = dataPoint.type ;
		tr.append(input);
		
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var label = $("<label></label>").attr({
			"id" : "index" 
		}).html(parseInt(i)+1) ;
		td.append(label);
		tr.append(td);
		
		// Object Identify
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "object_identifier"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.object_identifier);
		$(input).attr('readonly',true);
		td.append(input);
		tr.append(td);
		
		// Measure Name
		var td = $("<td></td>").css("border","0")
		.addClass("bacnet");
		var input = $("<input></input>").attr({
				"type" : "text",
				"id"   :  "oi_measurement_name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.oi_measure_name);
		$(input).attr('readonly', true);
		td.append(input);
		tr.append(td);
		
		// Unit
		var td = $("<td></td>").css({
			"border" : "0",
			"border-right": "2px solid white"
		})
		.addClass("bacnet");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "oi_measure_unit"
		}).css("width","90%").addClass("ui-corner-all inputText")
		.val(dataPoint.oi_measure_unit);
		$(input).attr('readonly', true);
		td.append(input);
		tr.append(td);
		// // space
		// var td = $("<td></td>").css("border","0")
		// tr.append(td);
		
		// Name
		var td = $("<td></td>").css({
			"border" : "0"
		})
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.name);
		td.append(input);
		tr.append(td);
		
		// Channel
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "channel"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.channel);
		;
		td.append(input);
		tr.append(td);
		
		// Default Value
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "default_value"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.default_value);
		;
		td.append(input);
		tr.append(td);
		
		// Max Value
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "max_value"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.max_value);
		;
		td.append(input);
		tr.append(td);
		
		// Min Value
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var input = $("<input></input>").attr({
			"type" : "text",
			"id"   :  "min_value"
		}).css("width","100%").addClass("ui-corner-all inputText")
		.val(dataPoint.min_value);
		;
		td.append(input);
		tr.append(td);
		debugger
		// Measure
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var select = $("<select></select>").attr({
			"type" : "text",
			"id"   : "measure_name"
		}).css("width","100%").addClass("ui-corner-all inputText")
		// .val(dataPoint.measure_name)
		.change(function(){
			changeUnitBacNet(this);
		});
		var option = $("<option></option>").val("null").html("Select");
		select.append(option);
		nameList.sort();
		for(var i in nameList){
			var option = $("<option></option>").val(nameList[i]).html(nameList[i]);
			select.append(option);
		}
		if(typeof dataPoint.measure_name === "undefined" || dataPoint.measure_name == "null"){
			select.selectedIndex = 0 ;
		}
		else
			$(select).val(dataPoint.measure_name);
		td.append(select);
		tr.append(td);
		
		
		// Unit
		var td = $("<td></td>").css("border","0")
		.addClass("mapping");
		var select = $("<select></select>").attr({
			// "type" : "text",
			"id"   : "measure_unit"
		}).css("width","100%").addClass("ui-corner-all inputText");
		var option = $("<option></option>").val("null").html("Select");
		select.append(option);
		for(var i in listUnit){
			var option = $("<option></option>").val(listUnit[i]).html(listUnit[i]);
			select.append(option);
		}
		if(typeof dataPoint.measure_unit === "undefined"|| dataPoint.measure_unit == "null"){
			select.selectedIndex = 0 ;
		}
		else
			$(select).val(dataPoint.measure_unit);
		td.append(select);
		tr.append(td);
		
		var td = $("<td></td>").css("border","0").addClass("mapping");
		var delButton = document.createElement('BUTTON');
		delButton.className = "ui-state-default ui-corner-all"
		delButton.innerHTML = "<span class='ui-icon ui-icon-trash'> </span>";
		delButton.type = "button";
		delButton.id   = "real_delete";
		let row = tr ;
		delButton.onclick = function(){
			deleteNode(row);
		}
		td.append(delButton);
		tr.append(td);
		
		table.append(tr);
	}
}
function writingBacnet(){
		let dataBacNet = [];
		 
		$('#page').addClass('disabledmouse');
		
		if($("#table_writing").is(":visible")){
			let tr = $("#table_writing > tbody > tr") ;
			let vaild = true ;
			let array_tr = [] ;
			for(let i = 0 ; i < tr.length ; i ++ ){
				let check_name = false ;
				let check_channel = false ;
				let check_max_value = false ;
				let check_min_value = false ;
				let check_measure_name = false ;
				let check_measure_unit = false ;
				let check_measure_ratio = false ;
				let dataPoint = {}; 
				dataPoint.data_point = $(tr[i]).find("#data_point").val();
				if(typeof dataPoint.data_point === "undefined")
					continue ;
				dataPoint.object_identifier = $(tr[i]).find("#object_identifier").val() ;
				// dataPoint.oi_measure_name  = $(tr[i]).find("#oi_measurement_name").val();
				// dataPoint.oi_measure_unit  = $(tr[i]).find("#oi_measure_unit").val();
				
				dataPoint.channel       = $(tr[i]).find("#channel").val();
				if(dataPoint.channel != "")
					check_channel = true ;
				
				dataPoint.default_value       = $(tr[i]).find("#default_value").val();
				
				dataPoint.max_value       = $(tr[i]).find("#max_value").val();
				if(dataPoint.max_value != "")
					check_max_value = true ;
				
				dataPoint.min_value       = $(tr[i]).find("#min_value").val();
				if(dataPoint.min_value != "")
					check_min_value = true ;
				
				dataPoint.measure_name      = $(tr[i]).find("#measure_name").val();
				if(dataPoint.measure_name != "" && dataPoint.measure_name != "null")
					check_measure_name = true ;
				
				dataPoint.measure_unit          = $(tr[i]).find("#measure_unit").val();
				if(dataPoint.measure_unit != "" && dataPoint.measure_unit != "null")
					check_measure_unit = true ;
				
				dataPoint.name = $(tr[i]).find("#name").val();
				
				if(check_channel == false 
				&& check_measure_name == false && check_measure_unit == false 
				){
					continue ;
				}
				if((check_channel && check_measure_name
				&& check_measure_unit) == false ){
					alert("You must fill all field");
					$('#page').removeClass('disabledmouse');
					$(tr[i]).css("border","2px solid red");
					return ;
				}
				debugger
				dataBacNet.push(dataPoint);
			}
		
		}
		
	
	let request = $.ajax({
			type : "post",
			url: pluginRoot + '/writeConfiguration',
			data: {
			"adapterName" : $("#adapter").val(),
			"address"     : $("#device_address").val(),
			"deviceInstance" : $("#device_instanceid").val(),
			"delete" : JSON.stringify(data_point_delete),
			"update" : JSON.stringify(dataBacNet)},
			
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
			$('#page').removeClass('disabledmouse');
			writing($("#adapter").val(),indexWrite) ;
			
		});
		request.fail(function (jqXHR, textStatus, errorThrown) {
			alert("No Response");
			$('#page').removeClass('disabledmouse');
			console.log(textStatus + "---- "+ errorThrown);
		});
}







	
	
	
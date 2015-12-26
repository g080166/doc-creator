var globalData;

$(document).ready(function(){
	$.ajax({
		url:'./interface.json',
		success:function(data){
			var dataObj = eval("("+data+")");
//			var dataObj = data;
			globalData = dataObj;
			var tmp = "";
			for(var index =0;index<dataObj.length;index++){
				var _data = dataObj[index];
				var name =_data.name;
				tmp += "<li>";
				tmp += "<a href='#' onclick='showInterface("+index+")'>"+(index+1)+":"+name+"</a>";
				tmp += "</li>";
			}
			
			$("#interface_box").html(tmp);
			showInterface(0);
		}
		
	});
});


function showInterface(index){
	var data = globalData[index];
	var interfaceUrl = data.interfaceUrl;
	var desc = data.desc;
	var originalRequest = data.originalRequest;
	var extraRequest = data.extraRequest;
	var response = data.response;
	
	$("#interface_url").html(interfaceUrl);
	$("#interface_desc").html(desc);
	$("#interface_method").html("POST");
	showRequestParam(originalRequest,extraRequest);
	showResponse(response);
	var obj = {};
	createResponseJSON(response,obj);
	var result = toJSON(obj);
	$("#result").html(result);
}

function showRequestParam(originalRequest,extraRequest){
	var tmp = "";
	if(originalRequest){
		
		for(var index =0;index<originalRequest.length;index++){
			var r = originalRequest[index];
			tmp += "<tr>";
			tmp += "<td>"+r.name+"</td>";
			tmp += "<td>"+(r.type||'未知')+"</td>";
			tmp += "<td>"+(r.necessary||'未知')+"</td>";
			tmp += "<td>基本参数</td>";
			tmp += "<td>"+(r.desc||'未知')+"</td>";
			tmp += "</tr>"
		}
	}
	
	if(extraRequest){
		
		for(var index =0;index<extraRequest.length;index++){
			var r = extraRequest[index];
			tmp += "<tr>";
			tmp += "<td>"+r.name+"</td>";
			tmp += "<td>"+(r.type||'未知')+"</td>";
			tmp += "<td>"+(r.necessary||'未知')+"</td>";
			tmp += "<td>扩展参数</td>";
			tmp += "<td>"+(r.desc||'未知')+"</td>";
			tmp += "</tr>"
		}
	}
	
	$("#request_body").html(tmp);
}

function showResponse(responseArray){
	var rowArray = [];
	var repeatedParam = {};
	createRow(responseArray, null, rowArray,repeatedParam);
	
	var tmp = '';
	for(var index in rowArray){
		tmp += rowArray[index];
	}
	$("#response_body").html(tmp);
}

function createRow(responseArray,realType,rowArray,repeatedParam){
	if(repeatedParam[realType]){
		return;
	}
	repeatedParam[realType] = realType;

	var tmp = "<tr>";
	tmp += "<td colspan='5'>"+(realType||"")+"</td>";
	tmp += "</tr>";
	
	for(var index in responseArray){
		var response = responseArray[index];
		var name = response.name;
		var realType = response.realType;
		var desc =response.desc;
		tmp += "<tr>";
		tmp += "<td>"+name+"</td>";
		tmp += "<td>"+realType+"</td>";
		tmp += "<td></td>";
		tmp += "<td></td>";
		tmp += "<td>"+desc+"</td>";
		tmp += "</tr>";
		
	}
	rowArray.push(tmp);
	for(var index in responseArray){
		var response = responseArray[index];
		var name = response.name;
		var realType = response.realType;
		if(response.subResponseParam&&(response.type=='jsonObject'||response.type=='jsonArray')){
			createRow(response.subResponseParam,realType,rowArray,repeatedParam);
		}
	}
}

function createResponseJSON(responseArray,obj){
		createResponseWithSingleParam(responseArray, obj);
}

function createResponseWithSingleParam(responseArray,obj){
	for(var index in responseArray){
		var response = responseArray[index];
		var type = response.type;
		var name = response.name;
		if(type=='jsonObject'){
			var unknownObj = {};
			createResponseWithSingleParam(response.subResponseParam, unknownObj);
			obj[name] = unknownObj;
			
		}else if(type=='jsonArray'){
			var unknownObj={};
			createResponseWithSingleParam(response.subResponseParam, unknownObj);
			var unknownArray=[];
			unknownArray.push(unknownObj);
			obj[name]= unknownArray;
			
		}else if(type=='String'){
			obj[name] = response.defaultValue||'测试数据'+name;
		}else if(type == 'int'||type=='Integer'||type=='float'||type=='Float'||type=='short'||type=='Short'||type=='Long'||type=='long'||type=='double'||type=='Double'){
			obj[name] = response.defaultValue||0;
		}
	}
}

function toJSON(response){
	$("#result").html(syntaxHighlight(JSON.stringify(response, null, 2)));
}

function syntaxHighlight(json) {
    if (typeof json != 'string') {
        json = JSON.stringify(json, undefined, 2);
    }
    json = json.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>');
    return json.replace(/("(\\u[a-zA-Z0-9]{4}|\\[^u]|[^\\"])*"(\s*:)?|\b(true|false|null)\b|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?)/g, function(match) {
        var cls = 'number';
        if (/^"/.test(match)) {
            if (/:$/.test(match)) {
                cls = 'key';
            } else {
                cls = 'string';
            }
        } else if (/true|false/.test(match)) {
            cls = 'boolean';
        } else if (/null/.test(match)) {
            cls = 'null';
        }
        return '<span class="' + cls + '">' + match + '</span>';
    });
}

//function showResponse(responseArray){
//	console.info(responseArray);
//	var allResponse ={};
//	var allResponseRel = {};
//	var lay0=[];
//	for(var index =0;index<responseArray.length;index++){
//		var response =responseArray[index];
//		allResponse[response.realType]= response;
//		getResponse(response, allResponse);
//		getResponseRel(response, allResponseRel, 0);
//		
//		lay0.push(response);
//	}
//	
//	allResponseRel["lay0"] = lay0;
//	console.info(allResponseRel);
//	var count = 0;
//	for(var index in allResponseRel){
//		count ++;
//	}
//	
//	for(var index =0;index<count;index++){
//		console.info(allResponseRel["lay"+index]);
//	}
//}

function getResponseRel(response,allResponseRel,index){
	if(!response||!response.subResponseParam){
		return;
	}
	index++;
	
	var subResponseParam = response.subResponseParam;
	for(var i =0;i<subResponseParam.length;i++){
		var _subResponseParam = subResponseParam[i];
//		if(_subResponseParam.type=='jsonObject'||_subResponseParam.type=='jsonArray'){
			allResponseRel["lay"+index] = allResponseRel["lay"+index]||[];
			allResponseRel["lay"+index].push(_subResponseParam);
			getResponseRel(_subResponseParam, allResponseRel,index);
//		}
	}
}

function getResponse(response,allResponse){
	if(!response||!response.subResponseParam){
		return;
	}
	
	var subResponseParam = response.subResponseParam;
	for(var index =0;index<subResponseParam.length;index++){
		var _subResponseParam = subResponseParam[index];
		if(_subResponseParam.type=='jsonObject'||_subResponseParam.type=='jsonArray'){
			allResponse[_subResponseParam.realType] = _subResponseParam;
			getResponse(_subResponseParam, allResponse);
		}
	}
	
}

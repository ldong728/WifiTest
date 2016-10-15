//function sendautoCode(json){
//    var
//
//}
var debug=false;
//var debug=true;

function getJsonString(json){
    var str=JSON.stringify(json);
    return str;
}
function headerTo(url){
    window.location.href=url;
}
function l(data){
    console.log(data);
}
function getUserList(){
    if(!debug)return window.light.getUserList();
    return  '{U_ID:"1",U_NAME:"张三",U_EMAIL:"god@163.com",U_PHONE:"13566603735",U_DEFAULT:1},{U_ID:"9527",U_NAME:"test",U_EMAIL:"abc@abc.com",U_PHONE:"123456789",U_DEFAULT:"0"}'
}
function signIn(data){

}
function getUserInf(){
    //alert("get user inf");
    if(!debug){
        return window.light.getUserInf();
    }else{
        return '{U_ID:"9527",U_NAME:"test",U_EMAIL:"abc@abc.com",U_PHONE:"123456789",U_DEFAULT:1}'
    }

}
function getUserInfOnline(){

}
function getGroupList(data){
    if(!debug)return window.light.getGroupList(data);
    return '[{"G_NAME":"abc","G_INF":"","U_ID":"1","G_TYPE":"local","G_ID":"1"}]';
}
function getGroupInf(){
    if(!debug){
        return window.light.getGroupInf();
    }
    return '{G_SSID:"abcd",G_SSID_PASD:"abcd"}'
}

function sendAutoCode(color,time,level,mode){
    var modeData=mode?"confirm":"not";
    if(!debug){
        window.light.sendAutoCode(JSON.stringify({color:color,time:time,level:level,mode:modeData}))
        saveCode('TYPE_AUTO');
    }else{
        //l(mode);
    }

}
function changeGroupType(ssid,pasd,merge){
    if(!debug){
        if(!merge){
            window.light.changeGroupType(getJsonString({ssid:ssid,pasd:pasd}));
        }else{
            window.light.mergenGroup();
        }

    }else{

    }

}
function searchLight(){
    if(!debug)window.wifi.linkLights();
    else lightStandby('1234231212');
}

function getCurrentSSID(){
    if(!debug)return window.wifi.getCurrentSSID();
    else return "abc";
}

function getCode(codeType){
    if(!debug)return window.light.getControlCode(codeType);
    else return '{"0":{"12":"60","20":"100"},"1":{"5":"100","30":"0"}}';
}
function addDeviceToGroup(jsonData){
    if(!debug)window.light.addDevice(getJsonString(jsonData))
}
function initGroup(){
    if(!debug) {
        //alert('hahahaha');
        //alert("info");
        return window.light.initGroup();
    }
    else return '{"device":{"0":{"D_MAC":"C4BE8474EE31","D_SSID":"USR-C322","G_ID":"1","D_TYPE":"light","D_NAME":"light"}},"inf":{"G_ID":"1","G_NAME":"高性能","U_ID":"1","G_INF":"inf","G_TYPE":"online","G_SSID":"TL-WVR450G","G_SSID_PASD":"gooduo.net"},"type":"online","ssid":"TL-WVR450G"}'
}
function ap2sta(ssid,pasd){
    if(!debug)window.wifi.ap2sta(getJsonString({ssid:ssid,pasd:pasd}))
}

function saveCode(codeType){
    if(!debug)window.light.saveCodeToDb(codeType);
}
function deviceCodeSave(){
    //alert("save");
    if(!debug)window.light.saveCode();
}
function chooseGroup(groupId){
    if(!debug)window.light.chooseGroup(groupId);
}
function scanWifi(){
    if(!debug)window.wifi.scanWifi();
    else setTimeout(function(){
        onGetWifiList('["ssid1","ssid2"]');
    },5000)
}
function linkWifi(ssid){
    if(!debug)window.wifi.linkWifi(getJsonString({ssid:ssid}));
}





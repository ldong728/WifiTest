//function sendautoCode(json){
//    var
//
//}
var debug=false;

function getJsonString(json){
    var str=JSON.stringify(json);
    return str;
}
function headerTo(url){
    window.location.href=url;
};
function getUserList(){
    if(!debug)return window.light.getUserList();
    return  '[{U_ID:"1",U_NAME:"张三",U_EMAIL:"god@163.com",U_PHONE:"13566603735",U_DEFAULT:1},{U_ID:"9527",U_NAME:"test",U_EMAIL:"abc@abc.com",U_PHONE:"123456789",U_DEFAULT:0}]'
}
function signIn(data){

}
function getUserInf(){
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



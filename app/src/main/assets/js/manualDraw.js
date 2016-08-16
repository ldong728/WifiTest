/**
 * Created by godlee on 2016/7/5.
 */

var colorNumber = 7;//控制点数组长度
var colorList = ['#A61583', '#571785', '#0D308E', '#0085C8', '#43AF37', '#B7080A', '#B1D0E2'];//颜色列表
var colorNameList=['UV', '紫色', '深蓝', '蓝色', '绿色', '深红', '白色'];
var drawList = new Array(colorNumber);
var margin=20;
var padding=10;
var marginH = 10;
var marginV = 10;
var textSize=20;
var baseValue=padding+textSize*3;
var valueRange;
var colorHeight;
var myCanvas;
var myContext;
var bufferCanvas;
var bufferContext;
var canvasWidth;
var canvasHeight;
var pOffset;
var currentColor = 6;
var canvasLeft, canvasTop;
var edgeIndex = -1;
$(document).ready(function () {
    initCanvas();
    window.requestAnimationFrame(draw);
});
function initCanvas() {
    var sCanvas = $('.canvas_wrap').get(0);
    canvasWidth = sCanvas.clientWidth;
    canvasHeight = sCanvas.clientHeight;
    canvasLeft = sCanvas.getBoundingClientRect().left;
    canvasTop = sCanvas.getBoundingClientRect().top;
    //pOffset = (canvasWidth - marginV * 2) / colorNumber;
    myCanvas = $('#drawing').get(0);
    myCanvas.width = canvasWidth;
    myCanvas.height = canvasHeight;
    valueRange=canvasWidth-marginH*2-padding*3-textSize*4.5;
    colorHeight=(canvasHeight-marginV*2)/colorNumber
    myContext = myCanvas.getContext('2d');
    bufferCanvas = document.createElement('canvas');
    bufferContext = bufferCanvas.getContext('2d');
    bufferCanvas.width = canvasWidth;
    bufferCanvas.height = canvasHeight;
    myCanvas.addEventListener('touchstart', touchStart, false);
    document.addEventListener('touchmove', touchMove, false);
    myCanvas.addEventListener('touchend', touchEnd, false);
    for (var i = 0; i < 7; i++) {
        drawList[i] = new color(i, colorList[i]);
    }
    drawBuffer();
}
function drawBuffer() {
    bufferCanvas.width = bufferCanvas.width;
    $.each(drawList, function (k, v) {
        if (k != currentColor) {
            //alert(v.color);
            v.drawSelf(bufferContext);
        }
    });
}
function draw() {
    myCanvas.width = myCanvas.width;
    myContext.drawImage(bufferCanvas, 0, 0);
    drawList[currentColor].drawSelf(myContext);
    requestAnimationFrame(draw);
}
function touchStart(e) {
    var y = e.touches[0].clientY - canvasTop;
    var index = getIndex(y);
    if(index>-1){
        currentColor=index;
        drawBuffer();
        //alert(currentColor);
    }

}
function touchMove(e) {
        var x = e.touches[0].clientX - canvasLeft;
        var value = x - baseValue;
    if(value>=0&&value<=valueRange){
        drawList[currentColor].setLevel(value);
    }

        //$('#temp').text(level);
        //drawList[currentColor].level = level;

}
function touchEnd(e) {
    var x= e.changedTouches[0].clientX - canvasLeft;
    var y = e.changedTouches[0].clientY - canvasTop;




}
function getIndex(y) {
    var index = ((y - marginV) / colorHeight + 0.5) | 0;
    if (index > colorNumber-1)index =  -1;
    if (index < 0)index =-1;
    return index;
}

function color(index, color) {
    this.index = index;
    this.color = color;
    colorHeight=canvasHeight/7;
    this.top=index*colorHeight;
    this.value=0;
    this.level=0;

    //this.controlPoints = new Array(pNumber);
    //this.drawPoints = new Array();
    //this.add = add;
    //this.drawSelf = drawSelf;
    //this.clearRange = clearRange;
    //this.add(new point(marginH, canvasHeight - marginV));
    //this.add(new point(canvasWidth - marginH, canvasHeight - marginV));
    //function add(p) {
    //    var index = getIndex(p.x);
    //    this.controlPoints[index] = p;
    //}
    this.setLevel=function(value){
        this.value=value;
        this.level=parseInt(value * 100 / valueRange)
    }

    this.drawSelf=function(context) {
        var drawTop=this.top+padding;
        context.beginPath();
        context.lineWidth=3;
        context.strokeStyle='#828282';
        context.moveTo(baseValue,this.top+padding+5);
        context.lineTo(baseValue+valueRange,this.top+padding+5);
        context.stroke();
        context.strokeStyle = this.color;
        context.fillStyle=this.color;
        context.lineWidth = 10;
        context.beginPath();
        context.moveTo(baseValue,drawTop+5)
        context.lineTo(baseValue+this.value,this.top+padding+5);
        context.arc(baseValue+this.value,this.top+padding+5,8, 0, Math.PI * 2);
        context.stroke();
        context.closePath();
        context.beginPath();
        context.strokeStyle='#ffffff';
        context.lineWidth=6
        context.arc(baseValue+this.value,this.top+padding+5,3, 0, Math.PI * 2,true);
        context.stroke();
        context.closePath();
        context.font=textSize+"px '微软雅黑'";
        context.fillText(colorNameList[this.index],padding,drawTop+textSize/2);
        context.fillText(this.level+'%',baseValue+valueRange+padding*1.5,drawTop+textSize/2);

    }

}






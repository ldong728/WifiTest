/**
 * Created by godlee on 2016/7/5.
 */

var pNumber = 48;//控制点数组长度
var colorNumber=7;
var colorList = ['#0D308E','#B1D0E2', '#B7080A','#A61583','#571785', '#0085C8', '#43AF37'];
var colorNameList=[ '深蓝','白色', '深红', 'UV', '紫色', '蓝色', '绿色'];
var edgeL = 0;
var edgeR = pNumber - 1;
var drawList = new Array(7);
var palette = new Array(7);
var aCanvas;
var aContext;
var aBufferCanvas;
var aBufferContext;
var aCanvasWidth;
var aCanvasHeight;
var bCanvas;
var bContext;
var bBufferCanvas;
var bBufferContext;
var valueRange;
var colorHeight;
var colorWidth;
var innerPaddingV;
var innerPaddingH;
var bCanvasWidth;
var bCanvasHeight;
var tempLevel;
var margin=20;
var bPadding=10;
var textSize=14;
var baseLineBorder=1;
var baseValue=bPadding+textSize*3;
var pOffset;
var currentColor = 6;
var aCanvasLeft, aCanvasTop;
var bCanvasLeft,bCanvasTop;
var marginH = 10;
var marginV = 10;
var paddingBottom;
var edgeIndex = -1;
var bgImg;
var aRatio;
var bRatio;
$(document).ready(function () {
    initCanvas();
    window.requestAnimationFrame(draw);
});
var getPixelRatio = function(context) {
    var backingStore = context.backingStorePixelRatio ||
        context.webkitBackingStorePixelRatio ||
        context.mozBackingStorePixelRatio ||
        context.msBackingStorePixelRatio ||
        context.oBackingStorePixelRatio ||
        context.backingStorePixelRatio || 1;
    return (window.devicePixelRatio || 1) / backingStore;
};

function initCanvas() {

    aCanvas = $('#aCanvas').get(0);
    aContext = aCanvas.getContext('2d');
    bCanvas = $('#bCanvas').get(0);
    bContext = bCanvas.getContext('2d');
    //aRatio=getPixelRatio(aContext);
    //bRatio=getPixelRatio(bContext);


    bgImg=$('#timeImg').get(0);
    aCanvasWidth = aCanvas.clientWidth;
    aCanvasHeight = aCanvas.clientHeight;
    aCanvasLeft = aCanvas.getBoundingClientRect().left;
    aCanvasTop = aCanvas.getBoundingClientRect().top;
    pOffset = (aCanvasWidth - marginH * 2) / pNumber;
    //aCanvas = $('#drawing').get(0);
    aCanvas.width = aCanvasWidth;
    aCanvas.height = aCanvasHeight;

    aBufferCanvas = document.createElement('canvas');
    aBufferContext = aBufferCanvas.getContext('2d');
    aBufferCanvas.width = aCanvasWidth;
    aBufferCanvas.height = aCanvasHeight;
    paddingBottom=parseInt((aCanvasHeight-marginV)*30/230)
    bCanvasWidth = bCanvas.clientWidth;
    bCanvasHeight = bCanvas.clientHeight;
    bCanvasLeft = bCanvas.getBoundingClientRect().left;
    bCanvasTop = bCanvas.getBoundingClientRect().top;
    //bCanvas = $('#manual_canvas').get(0);
    bCanvas.width = bCanvasWidth;
    bCanvas.height = bCanvasHeight;

    bBufferCanvas = document.createElement('canvas');
    bBufferContext = bBufferCanvas.getContext('2d');
    bBufferCanvas.width = bCanvasWidth;
    bBufferCanvas.height = bCanvasHeight;
    colorWidth=bCanvasWidth/colorNumber;
    colorHeight=bCanvasHeight-marginV*2;
    innerPaddingV=parseInt(colorHeight*0.05);
    innerPaddingH=parseInt(colorWidth*0.05);
    textSize=parseInt(colorHeight*0.05);
    valueRange=colorHeight*0.78;

    //colorHeight = (bCanvasHeight-marginV*2)/colorNumber;
    aCanvas.addEventListener('touchstart', touchStart, false);
    aCanvas.addEventListener('touchmove', touchMove, true);
    aCanvas.addEventListener('touchend', touchEnd, false);
    //$('.colorSelect').click(function () {
    //    var id = $(this).attr('id');
    //    currentColor = id;
    //    drawBuffer();
    //});
    for (var i = 0; i < 7; i++) {
        drawList[i] = new color(i, colorList[i]);
        palette[i]= new manualColor(i,colorList[i]);
    }

    drawBuffer();
}
function drawBuffer() {
    //aaBufferContext.clearRect(0,0,aCanvasWidth,aCanvasHeight);
    aBufferCanvas.width = aBufferCanvas.width;
    bBufferCanvas.width = bBufferCanvas.width;
    //aBufferContext.drawImage(bgImg,0,marginV,aCanvasWidth*aRatio,(aCanvasHeight-marginV)*aRatio);
    aBufferContext.drawImage(bgImg,0,marginV,aCanvasWidth,(aCanvasHeight-marginV));
    for(var i=0;i<colorNumber;i++){
        if(i!=currentColor){
            drawList[i].drawSelf(aBufferContext);
            palette[i].drawSelf(bBufferContext);
        }
    }
    //$.each(drawList, function (k, v) {
    //    if (k != currentColor) {
    //        //alert(v.manualColor);
    //        v.drawSelf(aBufferContext);
    //    }
    //});
    //$.each(palette,function(k,v){
    //    )
    //})
}
function draw() {
    aCanvas.width = aCanvas.width;
    bCanvas.width = bCanvas.width;
    aContext.drawImage(aBufferCanvas, 0, 0,aCanvasWidth,aCanvasHeight);
    bContext.drawImage(bBufferCanvas,0,0,bCanvasWidth,bCanvasHeight);
    //aContext.drawImage(aBufferCanvas, 0, 0,aCanvasWidth,aCanvasHeight);
    //bContext.drawImage(bBufferCanvas,0,0,bCanvasWidth,bCanvasHeight);
    drawList[currentColor].drawSelf(aContext);
    requestAnimationFrame(draw);
}
function touchStart(e) {
    $('html,body').css('overflow','hidden');
    var p = getTouchP(e);
    var x = p.x;
    var y = p.y;
    var index = getIndex(x);
    if (0 == index || pNumber - 1 == index) {
        edgeIndex = index;
    } else {
        edgeIndex = -1;
    }
    edgeL = index;
    edgeR = index;
    if (index == pNumber - 1)edgeR = index - 1;
    while (index > 0 && !drawList[currentColor].controlPoints[--edgeL]) {
        continue;
    }
    while (index < pNumber - 1 && !drawList[currentColor].controlPoints[++edgeR]) {
        continue;
    }
    drawList[currentColor].add(new point(x, y));
    //window.wifi.sendCode(JSON.stringify({manualColor:currentColor,time:index,level:105}))//必须代码
}
function touchMove(e) {
    if (edgeIndex < 0) {
        var x = e.touches[0].clientX - aCanvasLeft;
        var y = e.touches[0].clientY - aCanvasTop;
        var index = getIndex(x);
        if (index > edgeL && index < edgeR) {
            drawList[currentColor].clearRange(edgeL, edgeR);
            drawList[currentColor].add(new point(x, y));
        }
    } else {
        var index = edgeIndex;
        var x = index * pOffset + marginH;
        var y = e.touches[0].clientY - aCanvasTop;
        drawList[currentColor].clearRange(edgeL, edgeR);
        drawList[currentColor].add(new point(x, y));
    }


}
function touchEnd(e) {
    var y = e.changedTouches[0].clientY - aCanvasTop;
    //var level = parseInt((aCanvasHeight - marginV - y) / (aCanvasHeight - marginV * 2) * 100);
    if (edgeIndex < 0) {
        var x = e.changedTouches[0].clientX - aCanvasLeft;
        var index = getIndex(x);
        if (index <=edgeL || index >= edgeR|| y<0) {
            drawList[currentColor].clearRange(edgeL, edgeR);
            return;
        }else{
            if(y<marginV)y=marginV;
            if(y>aCanvasHeight-paddingBottom)y=aCanvasHeight-paddingBottom;
            //if(x<marginH)x=marginH;
            //if(x>aCanvasWidth-marginH)x=aCanvasWidth-marginH;
            var level=parseInt((aCanvasHeight -paddingBottom - y) / (aCanvasHeight - marginV-paddingBottom) * 100);
            drawList[currentColor].add(new point(x, y));
            sendCode(currentColor,index,level);
        }

    } else {
        var index=edgeIndex;
        if(y<marginV)y=marginV;
        if(y>aCanvasHeight-paddingBottom)y=aCanvasHeight-paddingBottom;
        var level=parseInt((aCanvasHeight - marginV - y) / (aCanvasHeight - marginV * 2) * 100);
        var x = index * pOffset + marginH;
        drawList[currentColor].add(new point(x, y));
        sendCode(currentColor,index,level);
    }
    //alert(level);

}
function getIndex(x) {
    var index = ((x - marginH) / pOffset + 0.5) | 0;
    if (index > pNumber - 1)index = pNumber - 1;
    if (index < 0)index = 0;
    return index;
}

function color(index, color) {
    this.index = index;
    this.color = color;
    this.controlPoints = new Array(pNumber);
    this.drawPoints = new Array();
    this.add = add;
    this.drawSelf = drawSelf;
    this.clearRange = clearRange;
    this.add(new point(marginH, aCanvasHeight - paddingBottom));
    this.add(new point(aCanvasWidth - marginH, aCanvasHeight - paddingBottom));
    function add(p) {
        var index = getIndex(p.x);
        this.controlPoints[index] = p;
    }

    function drawSelf(context) {
        var cu = this.index == currentColor ? true : false;
        context.beginPath();
        context.strokeStyle = this.color;
        context.fillStyle=this.color;
        context.lineWidth = 2;
        context.moveTo(this.controlPoints[0].x, this.controlPoints[0].y);
        $.each(this.controlPoints, function (k, v) {
            if (k > 0 && v) {
                context.lineTo(v.x, v.y);
                context.moveTo(v.x, v.y);
            }
        });
        context.stroke();
        context.closePath();
        if (cu) {
            $.each(this.controlPoints, function (k, v) {
                if (k > -1 && v) {
                    context.beginPath();
                    //context.lineWidth = 10;
                    context.arc(v.x, v.y, 5, 0, Math.PI * 2);
                    context.fill();
                }

            })

        }

    }

    function clearRange(l, r) {
        for (var i = l + 1; i < r; i++) {
            this.controlPoints[i] = null;
        }
    }
}
function manualColor(index, color) {
    this.index = index;
    this.color = color;
    this.top=marginV;
    this.left=index*colorWidth;
    this.value=0;
    this.level=0;

    this.setValue=function(value){
        this.value=value;
        this.level=parseInt(value * 100 / valueRange)
    }
    this.setLevel=function(level){
        this.level=level;
        this.value=parseInt(level*valueRange / 100);
    }

    this.drawSelf=function(context) {
        var drawTop=this.top+bPadding;
        context.strokeStyle = this.color;
        context.fillStyle=this.color;
        context.font='bold '+textSize+"px 'Helvetica,Arial'";
        context.fillText(colorNameList[this.index],this.left+innerPaddingH,colorHeight-innerPaddingV);
        context.fillText(this.level+'%',this.left+innerPaddingH,textSize+innerPaddingV);

        //context.beginPath();
        //context.lineWidth=baseLineBorder;
        //context.strokeStyle='#CCCCCC';
        //context.moveTo(baseValue,this.top+bPadding+5);
        //context.lineTo(baseValue+valueRange,this.top+bPadding+5);
        //context.stroke();
        //
        //context.lineWidth = 3;
        //context.beginPath();
        //context.moveTo(baseValue,drawTop+5)
        //context.lineTo(baseValue+this.value,this.top+bPadding+5);
        //context.stroke();
        //context.closePath();
        //context.arc(baseValue+this.value,this.top+bPadding+5,6, 0, Math.PI * 2, true);
        //context.fill();




        //context.beginPath();
        //context.fillStyle='#ffffff';
        //context.lineWidth =2;
        //context.arc(baseValue+this.value,this.top+bPadding+5, 2, 0, Math.PI * 2,true);
        //context.fill();
        //context.closePath();
    }

}

function point(x, y) {
    this.x = x;
    this.y = y;
}



/**
 *坐标偏移量计算（暂未用到）
 */
function getTouchP(e) {
    //var x=e.touches[0].clientX;
    //var y= e.touches[0].clientY;
    var x = e.touches[0].clientX - aCanvasLeft;
    var y = e.touches[0].clientY - aCanvasTop;
    if (x < 0)x = 0;
    if (x > aCanvasWidth)x = aCanvasWidth;
    if (y < 0)y = 0;
    if (y > aCanvasHeight)y = aCanvasHeight;
    return new point(x, y);
}

function drawDotedLine(p1, p2) {
//            test('indraw');
    var a = p2.x - p1.x;
//            test('a='+a);
    var b = p2.y - p1.y;
//            test('b='+b);
    var c = Math.sqrt(Math.pow(a, 2) + Math.pow(b, 2));
//            test('c='+c)
    var dotCount = c / 5;
//            test('dotCount='+dotCount);
    var dx = a / dotCount;
//            test('dx='+dx);
    var dy = b / dotCount;
//            test('dy='+dy);
    var Dx = 0;
    var Dy = 0;
    var isEmpty = false;
    aContext.beginPath();
    aContext.moveTo(p1.x, p1.y);

    while (Math.abs(a - Dx) > 5 || Math.abs(b - Dy) > 5) {
        Dx += dx;
        Dy += dy;
        if (!isEmpty) {
            aContext.lineTo(p1.x + Dx, p1.y + Dy);
            aContext.stroke();
//                    test('线');
            isEmpty = true;
        } else {
            aContext.moveTo(p1.x + Dx, p1.y + Dy);
            aContext.stroke();
//                    test('空');
            isEmpty = false;
        }
    }
//                test('结束');
    if (!isEmpty) {
        aContext.lineTo(p2.x, p2.y);
        aContext.stroke();
    } else {
        aContext.moveTo(p2.x, p2.y);
        aContext.stroke();
    }


//            aContext.stroke();
    aContext.closePath();
//            test('closePath');
}
function sendCode(color,time,level){
    //alert("manualColor:"+currentColor+",index:"+time+",level:"+level);
}

function initCode(data){
    var json=eval("("+data+")");
    $.each(json,function(color,stu){
        $.each(stu,function(time,level){
            var x = index * pOffset + marginH;
            //parseInt((aCanvasHeight - marginV - y) / (aCanvasHeight - marginV * 2) * 100);
            var y= parseInt(canvas-marginV-level*(aCanvasHeight-marginV *2 )/100)
            drawList[color].add(new point(x,y));
        })
    });
}




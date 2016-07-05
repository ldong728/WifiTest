/**
 * Created by godlee on 2015/4/1.
 */

var pNumber=48;
var colorList=['#00d4ff','#F7F7F7','#EE2C2C','#4B0082','#7D26CD','#00008B','#66CD00'];
var edgeL=0;
var edgeR=pNumber-1;
var drawList=new Array(7);
var myCanvas;
var myContext;
var bufferCanvas;
var bufferContext;
var canvasWidth;
var canvasHeight;
var pOffset;
var testCount=0;
var lineType=0;
var lastPoint;
var mousePressed;
var currentColor=1;

//var points = Array();
//var lines = Array();
//var chars = Array();
var currentX=0;
var currentY=0;
$(document).ready(function(){
    initCanvas();
    window.requestAnimationFrame(draw);
});
function initCanvas(){
    canvasWidth=$('.canvas_wrap').get(0).clientWidth;
    canvasHeight=$('.canvas_wrap').get(0).clientHeight;
    pOffset=canvasWidth/pNumber;
    myCanvas=$('#drawing').get(0);
    myCanvas.width=canvasWidth;
    myCanvas.height=canvasHeight;
    myContext=myCanvas.getContext('2d');
    bufferCanvas=document.createElement('canvas');
    bufferContext=bufferCanvas.getContext('2d');
    bufferCanvas.width=canvasWidth;
    bufferCanvas.height=canvasHeight;
    //myContext.fillStyle="#000000";
    //myContext.strokeStyle="#000000";
    //myContext.font = "bold 20px Arial";
    //myContext.textAlign = "center";
    //myContext.textBaseline = "middle";
    //myCanvas.onmousedown=mouseDown;
    //myCanvas.addEventListener('mousedown',mouseDown,true);
    //myCanvas.onmouseup=mouseUp;
    //myCanvas.onmousemove=mouseMove;
    //myCanvas.ondblclick=mouseDouble;
    //window.addEventListener('keydown', keyDown,true);
    //window.addEventListener('keypress',keyPress,true);
    myCanvas.addEventListener('touchstart',touchStart, false);
    document.addEventListener('touchmove',touchMove, false);
    //document.addEventListener('touchend',touchEnd, false);
    $('#temp').append('offset: '+pOffset);
    for(var i=0;i<7;i++){
        drawList[i]=new color(color[i]);
    }
    drawBuffer();
}
function drawBuffer(){
    bufferContext.clearRect(0,0,canvasWidth,canvasHeight);
    $.each(drawList,function(k,v){
        if(k!=currentColor){
            v.drawSelf(bufferContext);
        }
    });
}
function draw(){
        //myContext.clearRect(0,0,canvasWidth,canvasHeight);
        myCanvas.width=myCanvas.width;
        myContext.drawImage(bufferCanvas,0,0);
        drawList[currentColor].drawSelf(myContext);






    requestAnimationFrame(draw);
}
function touchStart(e){
    var x=e.touches[0].clientX;
    var y= e.touches[0].clientY;
    var index=getIndex(x);
    edgeL=index;
    edgeR=index;
    while(!drawList[currentColor].controlPoints[--edgeL]){
        continue;
    }
    while(!drawList[currentColor].controlPoints[++edgeR]){
        continue;
    }
    //alert("l:"+edgeL);
    //alert("r:"+edgeR);




    drawList[currentColor].add(new point(x,y));

}
function touchMove(e){
    var x=e.touches[0].clientX;
    var y= e.touches[0].clientY;
    var index=getIndex(x);
    if(index>edgeL&&index<edgeR){
        drawList[currentColor].clearRange(edgeL,edgeR);
        drawList[currentColor].add(new point(x,y));
    }

}
function touchEnd(e){

}


function getIndex(x){
    var index=(x/pOffset+ 0.5) | 0;
    if(index>pNumber-1)index=pNumber-1;
    return index;
}
function color(color){
    this.color=color;
    this.controlPoints= new Array(pNumber);
    this.add=add;
    this.drawSelf=drawSelf;
    this.clearRange=clearRange;
    this.add(new point(0,canvasHeight));
    this.add(new point(canvasWidth,canvasHeight));
    this.add(new point(100,100));
    function add(p){
        var index=getIndex(p.x);
        this.controlPoints[index]=p;
    }
    function drawSelf(context){
        //context.save();
        //$('#temp').empty();
        //context.strokeStyle=colorList[this.color];
        //context.fillStyle=colorList[this.color];
        context.moveTo(this.controlPoints[0].x,this.controlPoints[0].y);
        $.each(this.controlPoints,function(k,v){
           if(k>0&&v){
               //$('#temp').append(v.x+"</br>");
               context.lineTo(v.x, v.y);
           }
        });
        context.stroke();
        //context.restore();
    }

    function clearRange(l,r){
        for(var i=l+1;i<r;i++){
            this.controlPoints[i]=null;
        }

    }

}
function point(x,y){
    this.x=x;
    this.y=y;
}
function line(p1,p2,type){
    this.p1=p1;
    this.p2=p2;
    this.type=type;
    this.enable=true;
    this.drawSelf =function(){
        this.isEnable();
        if(this.enable){
            switch(this.type){
                case 0:{
                    drawLine(this.p1,this.p2);
                    break;
                }
                case 1:{
                    drawDotedLine(this.p1,this.p2);
                    break;
                }
            }
        }else{
        }
    }
    this.isEnable = function(){
        if(!this.p1.enable||!this.p2.enable){
            this.enable=false;
//                    this.p1=null;
//                    this.p2=null;
        }else{
            this.enable=true;
        }
    }
}
function char(x,y,value){
    this.x=x;
    this.y=y;
    this.value=value;
    this.enable=true;
    this.drawSelf=function(){
        if(this.enable)myContext.fillText(value,x,y);
    }
}
var mouseDown=function(e){
    //alert('mouse down');
    $('#temp').empty();
    $.each(e,function(k,v){
        $('#temp').append(k+":"+v+"</br>")
    });
    var P = realP(e.x, e.y);
    var tempPoint = new point(P.x,P.y);
    lastPoint =getPointfromArray(tempPoint);
    if(lastPoint==null){
        lastPoint=tempPoint;
        lastPoint.drawSelf();
        points.push(lastPoint);
    }else{
//                lastPoint=points[index];
    }
    mousePressed=true;
}
var mouseUp=function(e){
    var P = realP(e.x, e.y);
    var newPoint = new point(P.x,P.y);
    var nowPoint = getPointfromArray(newPoint);
    if(nowPoint==null){
        nowPoint=newPoint;
        nowPoint.drawSelf();
        points.push(nowPoint);
        var mLine=new line(lastPoint,nowPoint,lineType);
        mLine.drawSelf();
        lines.push(mLine);
    }else{
        if(lastPoint!=nowPoint){
            var mLine=new line(lastPoint,nowPoint,lineType);
            mLine.drawSelf();
            lines.push(mLine);
        }
    }
//                drawLine(lastPoint,nowPoint);
//            }
    mousePressed=false;
}
var mouseDouble=function(){
    myContext.clearRect(0,0,500,500);
    for(x in points){
        points[x].visible=false;

    }
    reDraw();
}
var mouseMove = function(e){
    currentX= e.x;
    currentY= e.y;
//            $('#mouseX').empty();
//            $('#mouseY').empty();
//            $('#mouseX').append(currentX);
//            $('#mouseY').append(currentY);
    $('#temp').text(currentX);

}
var keyDown = function(e){

}
var keyPress = function(e){
    var keyID = e.keyCode ? e.keyCode :e.which;
    var keychar = String.fromCharCode(keyID);
    var p = realP(currentX,currentY);
    var sChar = new char(p.x, p.y,keychar);
    sChar.drawSelf();
    chars.push(sChar);
//            $('#keyCode').append(keychar);

}
var drawLine=function(p1,p2){
    myContext.beginPath();
    myContext.moveTo(p1.x,p1.y);
    myContext.lineTo(p2.x,p2.y);
    myContext.stroke();
    myContext.closePath();
}
var drawDotedLine = function(p1,p2){
//            test('indraw');
    var a=p2.x-p1.x;
//            test('a='+a);
    var b=p2.y-p1.y;
//            test('b='+b);
    var c=Math.sqrt(Math.pow(a,2)+Math.pow(b,2));
//            test('c='+c)
    var dotCount = c/5;
//            test('dotCount='+dotCount);
    var dx=a/dotCount;
//            test('dx='+dx);
    var dy=b/dotCount;
//            test('dy='+dy);
    var Dx=0;
    var Dy=0;
    var isEmpty = false;
    myContext.beginPath();
    myContext.moveTo(p1.x, p1.y);

    while(Math.abs(a-Dx)>5||Math.abs(b-Dy)>5){
        Dx+=dx;
        Dy+=dy;
        if(!isEmpty){
            myContext.lineTo(p1.x+Dx,p1.y+Dy);
            myContext.stroke();
//                    test('线');
            isEmpty=true;
        }else{
            myContext.moveTo(p1.x+Dx,p1.y+Dy);
            myContext.stroke();
//                    test('空');
            isEmpty=false;
        }
    }
//                test('结束');
    if(!isEmpty){
        myContext.lineTo(p2.x,p2.y);
        myContext.stroke();
    }else{
        myContext.moveTo(p2.x,p2.y);
        myContext.stroke();
    }


//            myContext.stroke();
    myContext.closePath();
//            test('closePath');
}
var drawPoint = function(x,y){
    myContext.beginPath(x+10,y);
    myContext.arc(x,y,10,0,2*Math.PI,false);
    myContext.stroke();
    myContext.closePath();
}
function getPointfromArray(p) {
    if (points.length > 0) {
        for (x in points) {
            if (points[x].isIn(p.x, p.y)) {
                return points[x];
            }else{
                continue;
            }
        }
        return null;
    }
    return null;
}
function realP(x,y) {
    var bbox =myCanvas.getBoundingClientRect();
    var x= x-bbox.left *(myCanvas.width / bbox.width);
    var y= y-bbox.top *(myCanvas.height / bbox.height);
    return new point(x,y);
}
function switchLineType(){
    $('#lineType').empty();
    lineType=(lineType==1? 0: 1);
    switch(lineType){
        case 0:{
            $('#lineType').append('实线');
            break;
        }
        case 1:{
            $('#lineType').append('虚线');
            break;
        }
    }
}
function reDraw(){
    myContext.clearRect(0,0,500,500);
    for(x in points){
        points[x].drawSelf();
    }
    for(y in lines){
        lines[y].drawSelf();
        if(!lines[y].enable)lines.splice(y,1);
    }
    for(z in chars){
        chars[z].drawSelf();
    }
}
function cancel(){
    points.pop().enable = false;
    reDraw();
}
function saveImg(){
//            var imgUrl = myCanvas.toDataURL("image/jpg");
//            var w=window.open('about:blank','image from canvas');
//            w.document.write("<img src='"+imgUrl+"' alt='from canvas'/>");
    var image = myCanvas.toDataURL("image/jpg")//.replace("image/png", "image/octet-stream");
    window.location.href=image;
}
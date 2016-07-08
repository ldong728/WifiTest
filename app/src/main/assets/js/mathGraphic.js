/**
 * Created by godlee on 2016/7/5.
 */

var pNumber=48;//控制点数组长度
var colorList=['#00d4ff','#F7F7F7','#EE2C2C','#4B0082','#7D26CD','#00008B','#66CD00'];//颜色列表
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
var currentColor=6;
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
    myCanvas.addEventListener('touchstart',touchStart, false);
    document.addEventListener('touchmove',touchMove, false);
    $('.colorSelect').click(function(){
        var id=$(this).attr('id');
        currentColor=id;
        drawBuffer();
    })
    for(var i=0;i<7;i++){
        drawList[i]=new color(i,colorList[i]);
    }
    drawBuffer();
}
function drawBuffer(){
    //bufferContext.clearRect(0,0,canvasWidth,canvasHeight);
    bufferCanvas.width=bufferCanvas.width;
    $.each(drawList,function(k,v){
        if(k!=currentColor){
            //alert(v.color);
            v.drawSelf(bufferContext);
        }
    });
}
function draw(){
        myCanvas.width=myCanvas.width;
        myContext.drawImage(bufferCanvas,0,0);
        drawList[currentColor].drawSelf(myContext);
    requestAnimationFrame(draw);
}
function touchStart(e){
    var p=getTouchP(e);
    var x= p.x;
    var y= p.y;
    var index=getIndex(x);
    edgeL=index;
    edgeR=index;
    if(index==pNumber-1)edgeR=index-1;
    while(index>0&&!drawList[currentColor].controlPoints[--edgeL]){
        continue;
    }
    while(index<pNumber-1&&!drawList[currentColor].controlPoints[++edgeR]){
        continue;
    }
    drawList[currentColor].add(new point(x,y));
    window.wifi.sendCode(JSON.stringify({color:currentColor,time:index,level:105}))
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
    var x=e.touches[0].clientX;
    var y= e.touches[0].clientY;
    var index=getIndex(x);
    var level=canvasHeight-y;
    if(y<1||y>canvasHeight){
        window.wifi.sendCode(JSON.stringify({color:currentColor,time:index,level:105}))
    }

}
function getIndex(x){
    var index=(x/pOffset+ 0.5) | 0;
    if(index>pNumber-1)index=pNumber-1;
    return index;
}
function color(index,color){
    this.index=index;
    this.color=color;
    this.controlPoints= new Array(pNumber);
    this.drawPoints=new Array();
    this.add=add;
    this.drawSelf=drawSelf;
    this.clearRange=clearRange;
    this.add(new point(0,canvasHeight));
    this.add(new point(canvasWidth,canvasHeight));
    function add(p){
        var index=getIndex(p.x);
        this.controlPoints[index]=p;
    }
    function drawSelf(context){
        var cu=this.index==currentColor? true:false;
        context.beginPath();
        context.strokeStyle=this.color;
        context.fillStyle=this.color;
        context.lineWidth=3;
        context.moveTo(this.controlPoints[0].x,this.controlPoints[0].y);
        $.each(this.controlPoints,function(k,v){
           if(k>0&&v){
               context.lineTo(v.x, v.y);
               if(cu)context.arc(v.x, v.y,10,0,Math.PI*2,true);
               //context.fill();
               context.moveTo(v.x, v.y);

           }
        });
        context.stroke();
        context.closePath();

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
        }else{
            this.enable=true;
        }
    }
}/**
 *坐标偏移量计算（暂未用到）
 */
function getTouchP(e){
    var x=e.touches[0].clientX;
    var y= e.touches[0].clientY;
    if(x<0)x=0;
    if(x>canvasWidth)x=canvasWidth;
    if(y<0)y=0;
    if(y>canvasHeight)y=canvasHeight;
    return new point(x,y);
}

function realP(x,y) {
    var bbox =myCanvas.getBoundingClientRect();
    var x= x-bbox.left *(myCanvas.width / bbox.width);
    var y= y-bbox.top *(myCanvas.height / bbox.height);
    return new point(x,y);
}




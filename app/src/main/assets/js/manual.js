var canvasList;
var contextList;
var sliderList;
var sConfig={
    color:config.color||'#ffffff',
    totalLevel:config.totalLevel||100

}
init();

function init(){

    $('.canvas_container').each(function(k,v){
        var canvasWrap= v.get(0);
        var sWidth= canvasWrap.clientWidth;
        var sHeight= canvasWrap.clientHeight;
        var left = canvasWrap.getBoundingClientRect().left;
        var top = canvasWrap.getBoundingClientRect().top;

        var id='canvas'+k;
        v.append('<canvas id="'+id+'"></canvas>')
        var sCanvas=$('#'+id).get(0);
        //var sContext=sCanvas.getContext();
        sCanvas.width=sWidth*2;
        sCanvas.height=sHeight*2;
        sliderList.add(new Slider(sCanvas,k,sConfig.color,left,top));
        $('#'+id).css('width',sWidth+'px');
        $('#'+id).css('height',sHeight+'px');
        



    })
}

function Slider(canvasWrap,index, color,left,top) {

    var _=this;
    _.left=left;
    _.top=top;
    _.mContext = canvas.getContext;
    _.index = index;
    _.color = color;
    colorHeight=canvas.height;
    _.top=0;
    _.value=0;
    _.level=0;
    _.setValue=function(value){
        _.value=value;
        _.level=parseInt(value * 100 / valueRange)
    }

    _.drawSelf=function() {
        var drawTop=this.top+padding;
        _.mContext.beginPath();
        _.mContext.lineWidth=baseLineBorder;
        _.mContext.strokeStyle='#CCCCCC';
        _.mContext.moveTo(baseValue,this.top+padding+5);
        _.mContext.lineTo(baseValue+valueRange,this.top+padding+5);
        _.mContext.stroke();
        _.mContext.strokeStyle = this.color;
        _.mContext.fillStyle=this.color;
        _.mContext.lineWidth = 3;
        _.mContext.beginPath();
        _.mContext.moveTo(baseValue,drawTop+5);
        _.mContext.lineTo(baseValue+this.value,this.top+padding+5);
        _.mContext.stroke();
        _.mContext.closePath();
        _.mContext.arc(baseValue+this.value,this.top+padding+5,6, 0, Math.PI * 2, true);
        _.mContext.fill();
        _.mContext.font='bold '+textSize+"px 'Helvetica,Arial'";
        _.mContext.fillText(colorNameList[this.index],padding,drawTop+textSize/2);
        _.mContext.fillText(this.level+'%',baseValue+valueRange+padding*1.5,drawTop+textSize/2);
        _.mContext.beginPath();
        _.mContext.fillStyle='#ffffff';
        _.mContext.lineWidth =2;
        _.mContext.arc(baseValue+this.value,this.top+padding+5, 2, 0, Math.PI * 2,true);
        _.mContext.fill();
        _.mContext.closePath();
    }
    _.touchStart=function(e){
        //var x=
    }

}
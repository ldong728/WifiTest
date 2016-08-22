function manualColor(context,index, color) {
    this.index = index;
    this.color = color;
    colorHeight=canvasHeight/7;
    this.top=index*colorHeight;
    this.value=0;
    this.level=0;
    this.setValue=function(value){
        this.value=value;
        this.level=parseInt(value * 100 / valueRange)
    }

    this.drawSelf=function() {
        var drawTop=this.top+padding;
        context.beginPath();
        context.lineWidth=baseLineBorder;
        context.strokeStyle='#CCCCCC';
        context.moveTo(baseValue,this.top+padding+5);
        context.lineTo(baseValue+valueRange,this.top+padding+5);
        context.stroke();
        context.strokeStyle = this.color;
        context.fillStyle=this.color;
        context.lineWidth = 3;
        context.beginPath();
        context.moveTo(baseValue,drawTop+5);
        context.lineTo(baseValue+this.value,this.top+padding+5);
        context.stroke();
        context.closePath();
        context.arc(baseValue+this.value,this.top+padding+5,6, 0, Math.PI * 2, true);
        context.fill();
        context.font='bold '+textSize+"px 'Helvetica,Arial'";
        context.fillText(colorNameList[this.index],padding,drawTop+textSize/2);
        context.fillText(this.level+'%',baseValue+valueRange+padding*1.5,drawTop+textSize/2);
        context.beginPath();
        context.fillStyle='#ffffff';
        context.lineWidth =2;
        context.arc(baseValue+this.value,this.top+padding+5, 2, 0, Math.PI * 2,true);
        context.fill();
        context.closePath();




    }

}
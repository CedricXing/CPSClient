var POINT_NUM=10;
var TIME_INTERVAL=100;
var MAX_V=11,MIN_V=0;
var xValue = 0;
var yValue = 10;
var chart;
var dataSet=[];
var pointCount=0;

function init()
{
    var dataPoints = [];

    chart = new CanvasJS.Chart("chartContainer", {
        theme: "light2",
        title: {
            text: "V-T"
        },
        animationEnabled: true, 
        animationDuration: 2000,
        axisY:{
            //suffix:"cm/s",
            maximum:MAX_V,
            mimimum:MIN_V,
            interval:1
        },
        axisX:{
            suffix:"s",
            interval:0.1
        },
        data: [{
            type: "line", //or "line"?
            dataPoints: dataSet
        }]
    });
    
    for(var i=0,tmpX=0;i<POINT_NUM;i++,tmpX+=0.1)
        dataSet.push({x:tmpX,y:null});

    chart.render();
}

function addData(v) {
    pointCount++;
    dataSet[(pointCount-1)%POINT_NUM].y=v;
    xValue+=0.1;
    chart.render();
    if(pointCount%POINT_NUM==0){
        for(var i=0;i<POINT_NUM;i++){
            dataSet[i].x=xValue+i*0.1;
            dataSet[i].y=null;
            pointCount=0;
        }
    }
    //document.getElementById("DEBUG").innerHTML=xValue+","+dataSet[1].x;
    //setTimeout(updateData, TIME_INTERVAL);
}

function updateData(v) {
    //TODO:get new data from this function
    addData(v); 
}
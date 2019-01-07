var POINT_NUM=10;
var TIME_INTERVAL=100;
var MAX_V=70,MIN_V=-1;
var xValue = 0;
var yValue = 10;
var chart;
var dataSet1=[];
var dataSet2=[];
var dataSet3=[];
var pointCount1=0,pointCount2=0,pointCount3=0;
var numCars=0;

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
            suffix:"cm/s",
            maximum:MAX_V,
            mimimum:MIN_V,
            interval:10
        },
        axisX:{
            suffix:"s",
            interval:1
        },
        data: [
            {
                type: "line", //or "line"?
                dataPoints: dataSet1
            },
            {
                type:"line",
                dataPoints:dataSet2
            },
            {
                type:"line",
                dataPoints:dataSet3
            }
        ]
    });
    
    for(var i=0,tmpX=0;i<POINT_NUM;i++,tmpX+=1){
        dataSet1.push({x:tmpX,y:null});
        dataSet2.push({x:tmpX,y:null});
        dataSet3.push({x:tmpX,y:null});
    }

    //car num
    numCars=car_number;
    chart.render();
}

function addData(car_id,v) {
    if(car_id==1)
         dataSet1[(pointCount1++)%POINT_NUM].y=v;
    else if(car_id==2)
        dataSet2[(pointCount2++)%POINT_NUM].y=v;
    else if(car_id==3)
        dataSet3[(pointCount3++)%POINT_NUM].y=v;

    if(numCars==1)
        xValue+=1;
    else if(numCars==2&&pointCount1==pointCount2)
        xValue+=1;
    else if(numCars==3&&pointCount1==pointCount2&&pointCount1==pointCount3)
        xValue+=1;

    chart.render();
    if((numCars==1&&pointCount1%POINT_NUM==0)||(numCars==2&&pointCount1==pointCount2&&pointCount1%POINT_NUM==0)
        ||(numCars==3&&pointCount1==pointCount2&&pointCount1==pointCount3&&pointCount1%POINT_NUM==0)){
        for(var i=0;i<POINT_NUM;i++){
            dataSet1[i].x=dataSet2[i].x=dataSet3[i].x=xValue+i;
            dataSet1[i].y=dataSet2[i].y=dataSet3[i].y=null;
            pointCount1=pointCount2=0;
        }
    }
    //document.getElementById("DEBUG").innerHTML=xValue+","+dataSet[1].x;
    //setTimeout(updateData, TIME_INTERVAL);
}

function updateData(car_id,v) {    //v1 for car1, v2 for car2
    //TODO:get new data from this function
    document.getElementById('DEBUG').innerHTML=car_id+","+v;
    addData(car_id,v); 
}
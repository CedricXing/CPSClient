#include "RTLSClient.h"



RTLSClient::RTLSClient()
{
	_width=280-40*2-15;
	_length=320-40*2-15;
	_radius=40;
	_devia=10;
	double arc=PI/2*_radius;
	_cumulate_dis[0]=0;
	_cumulate_dis[1]=_length;
	_cumulate_dis[2]=_cumulate_dis[1]+arc;
	_cumulate_dis[3]=_cumulate_dis[2]+_width;
	_cumulate_dis[4]=_cumulate_dis[3]+arc;
	_cumulate_dis[5]=_cumulate_dis[4]+_length;
	_cumulate_dis[6]=_cumulate_dis[5]+arc;
	_cumulate_dis[7]=_cumulate_dis[6]+_width;
}


RTLSClient::~RTLSClient()
{
}

void RTLSClient::getData(char * t0, char * t1, char * a0)
{
	memcpy(_A0, a0, 256);
	memcpy(_T0, t0, 256);
	memcpy(_T1, t1, 256);
}

void RTLSClient::setPos(Tag*t,int id,int n,int offset)
{
	t->effective=true;
	t->id=id;
	t->pos.n=n;
	t->pos.offset=offset;
	t->pos.distance=_cumulate_dis[n]+offset;
}

bool RTLSClient:: between(double x,double a,double b){
	if(x>=a-_devia&&x<=b+_devia){return true;}
	return false;
}

bool RTLSClient::between2(double x,double fixed,double a,double b)
{
	if(x>=sqrt(fixed*fixed+a*a)-_devia
	&&x<=sqrt(fixed*fixed+b*b)+_devia){
		return true;
	}
	else return false;

}

double RTLSClient:: cal_arc(double rmax,double dis){
	double rmin=_radius;
	double x,y;
	/*
		x^2+y^2=rmax^2
		(x-dis)^2+(y-rmin)^2=rmin^2
	*/
	double a,b,c,delta;
	a=1+(dis/rmin)*(dis/rmin);
	b=-2*(dis/rmin)*((rmax*rmax+dis*dis)/(2*rmin));
	c=((rmax*rmax+dis*dis)/(2*rmin))*((rmax*rmax+dis*dis)/(2*rmin));
	delta=b*b-4*a*c;
	x=(-b+sqrt(delta))/(2*a);

	double alpha=asin((x-dis)/_radius);
	double arc=alpha*PI/180*_radius;

	return arc;

}

void RTLSClient:: check_offset(int n,double offset,double dis0,double dis1,double dis2)
{
	if(n==0&&offset>_length-_devia){
		cout<<"Error #n:"<<n<<" #offset:"<<offset<<" #dis0:"<<dis0<<" #dis1:"<<dis1<<" #dis2:"<<dis2<<endl;
	}
	if(between(offset,0,_cumulate_dis[n]-_cumulate_dis[n-1]));
	else {
		cout<<"Error #n:"<<n<<" #offset:"<<offset<<" #dis0:"<<dis0<<" #dis1:"<<dis1<<" #dis2:"<<dis2<<endl;
	}
	return ;
}


void RTLSClient::processTag(char * frame,int tag_id)
{
	//check frame header
	if (frame[0] != 0xD6 || frame[1] != 0x6D) {
		cout << "Error:frame header not 0xD66D\n";
		return;
	}

	if (frame[2] != 0x2) {
		cout << "Error:wrong frame type\n";
		return;
	}

	if (frame[3] != (char)tag_id) {
		cout << "Error:wrong tag id\n";
		return;
	}

	uint16_t sequence = frame[4] | frame[5] << 8;
	double dis0 = (double)(uint16_t)(frame[6] | frame[7] << 8);
	double dis1 = (double)(uint16_t)(frame[8] | frame[9] << 8);
	double dis2 = (double)(uint16_t)(frame[10] | frame[11] << 8);

	Tag *t;
	if (tag_id == 0) {
		t = &_t0;
	}
	else {
		t = &_t1;
	}

	if (dis0 <= 0 || dis1 <= 0 || dis2 <= 0) {
		cout << "Error:distance is less than zero\n";
		return;
	}

	if(between(dis0,_radius,_radius+_length)
	&&between(dis1,_radius,_radius+_length)
	){
		double a0,a1,a2;
		a0=_length+_radius-dis0;
		a1=dis1-_radius;
		a2=sqrt(dis2*dis2-(_width+2*_radius)*(_width+2*_radius))-_radius;
		double avg=(a0+a1+a2)/3;
		this->check_offset(0,avg,dis0,dis1,dis2);
		this->setPos(t,tag_id,0,avg);
		return;
	}

	if(between(dis0,(sqrt(2)-1)*_radius,_radius)
	&& between(dis1,_radius+_length,sqrt(_radius*_radius+(2*_radius+_length)*(2*_radius+_length)))
	){
		double arc=cal_arc(dis1,_length);
		this->check_offset(1,arc,dis0,dis1,dis2);
		this->setPos(t,tag_id,1,arc);
		return;
	}

	if(between(dis0,_radius,_radius+_width)
	&&between2(dis1,2*_radius+_length,_radius,_radius+_width)
	){
		double a0,a1,a2;
		a0=dis0-_radius;
		a1=sqrt(dis1*dis1-(2*_radius+_length)*(2*_radius+_length))-_radius;
		a2=_radius+_width-sqrt(dis2*dis2-(2*_radius+_length)*(2*_radius+_length));
		double avg=(a0+a1+a2)/3;
		this->check_offset(2,avg,dis0,dis1,dis2);
		this->setPos(t,tag_id,2,avg);
		return;
	}

	if(between(dis0,_width+_radius,sqrt(_radius*_radius+(2*_radius+_width)*(2*_radius+_width)))
	&&between2(dis1,2*_radius+_length,_radius+_width,2*_radius+_width)){
		double arc=(dis0,_width);
		this->check_offset(3,arc,dis0,dis1,dis2);
		this->setPos(t,tag_id,3,arc);
		return;
	}

	if(between(dis2,_radius,_radius+_width)
	&&between2(dis1,2*_radius+_width,_radius,_radius+_length)){
		double a0,a1,a2;
		a0=sqrt(dis0*dis0-(2*_radius+_width)*(2*_radius+_width))-_radius;
		a1=_radius+_length-sqrt(dis1*dis1-(2*_radius+_width)*(2*_radius+_width));
		a2=_radius+_length-dis2;
		double avg=(a0+a1+a2)/3;
		this->check_offset(4,avg,dis0,dis1,dis2);
		this->setPos(t,tag_id,4,avg);
		return;
	}

	if(between(dis2,(sqrt(2)-1)*_radius,_radius)
	&&between(dis1,_radius+_width,sqrt(_radius*_radius+(2*_radius+_width)*(2*_radius+_width)))){
		double arc=(dis1,_width);
		arc=PI/2*_radius-arc;
		this->check_offset(5,arc,dis0,dis1,dis2);
		this->setPos(t,tag_id,5,arc);
		return;
	}

	if(between(dis1,_radius,_radius+_width)
	&&between(dis2,_radius,_radius+_width)){
		double a0,a1,a2;
		a0=_radius+_width-sqrt(dis0*dis0-(2*_radius+_length)*(2*_radius+_length));
		a1=_radius+_width-dis1;
		a2=dis2-_radius;
		double avg=(a0+a1+a2)/3;
		this->check_offset(6,avg,dis0,dis1,dis2);
		this->setPos(t,tag_id,6,avg);
		return;
	}

	if(between(dis1,(sqrt(2)-1)*_radius,_radius)
	&&between(dis0,_radius+_length,sqrt(_radius*_radius+(2*_radius+_length)*(2*_radius+_length)))){
		double arc=(dis2,_width);
		this->check_offset(7,arc,dis0,dis1,dis2);
		this->setPos(t,tag_id,7,arc);
		return;
	}
	cout << "Error:distance calculation error\n";
	return;
}

void RTLSClient::processAnchor(char * frame)
{
	return;

}



void RTLSClient::processData(Position&p0,Position&p1)
{
	_t0.effective = false;
	_t0.effective = false;
	processAnchor(_A0);
	processTag(_T0,0);
	processTag(_T1,1);
	if (_t0.effective) {
		p0=_t0.pos;
	}
	else {
		cout << "Error:calculate t0 distance error\n";
	}
	if (_t1.effective) {
		p1=_t1.pos;
	}
	else {
		cout << "Error:calculate t1 distance error\n";
	}

	return;
}


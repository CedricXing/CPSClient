#include "RTLSClient.h"


void RTLSClient::init_track() {
	double arc = (PI * _radius / 2.0);
	double accumulate = 0;
	//0
	for (int i = 0; i < _length / _precision; i++) {
		track.push_back({ i*_precision + _radius,0 });
	}
	accumulate += _length;

	//1
	for (int i = accumulate / _precision; i < (accumulate + arc) / _precision; i++) {
		double j = i - accumulate / _precision;
		track.push_back({ _radius*sin(j*_precision / _radius) + _length + _radius,_radius - _radius*cos(j*_precision / _radius) });
	}
	accumulate += arc;
	//2
	for (int i = accumulate / _precision; i < (accumulate + _width) / _precision; i++) {
		double j = i - accumulate / _precision;
		track.push_back({ _length + _radius * 2,j*_precision + _radius });
	}
	accumulate += _width;
	//3
	for (int i = accumulate / _precision; i < (accumulate + arc) / _precision; i++) {
		double j = i - accumulate / _precision;
		track.push_back({ _radius + _length + _radius * cos(j*_precision / _radius),_width + _radius + _radius * sin(j*_precision / _radius) });
	}
	accumulate += arc;
	//4
	for (int i = accumulate / _precision; i < (accumulate + _length) / _precision; i++) {
		double j = i - accumulate / _precision;
		track.push_back({ _radius + _length - j * _precision,_radius * 2 + _width });
	}
	accumulate += _length;
	//5
	for (int i = accumulate / _precision; i < (accumulate + arc) / _precision; i++) {
		double j = i - accumulate / _precision;
		track.push_back({ _radius - _radius*sin(j*_precision / _radius),_radius*cos(j*_precision / _radius) + _width + _radius });
	}
	accumulate += arc;
	//6
	for (int i = accumulate / _precision; i < (accumulate + _width) / _precision; i++) {
		double j = i - accumulate / _precision;
		track.push_back({ 0,_width + _radius - j*_precision });
	}
	accumulate += _width;
	//7
	for (int i = accumulate / _precision; i < (accumulate + arc) / _precision; i++) {
		double j = i - accumulate / _precision;
		track.push_back({ _radius - _radius*sin(j*_precision / _radius),_radius - _radius * cos(j*_precision / _radius) });
	}
}

RTLSClient::RTLSClient()
{
	_width=80;//185
	_length=122;//225
	_radius=85;
	_devia=20;
	_precision = 1;
	double arc=PI/2*_radius;
	_cumulate_dis[0]=0;
	_cumulate_dis[1]=_length;
	_cumulate_dis[2]=_cumulate_dis[1]+arc;
	_cumulate_dis[3]=_cumulate_dis[2]+_width;
	_cumulate_dis[4]=_cumulate_dis[3]+arc;
	_cumulate_dis[5]=_cumulate_dis[4]+_length;
	_cumulate_dis[6]=_cumulate_dis[5]+arc;
	_cumulate_dis[7]=_cumulate_dis[6]+_width;
	_cumulate_dis[8] = _cumulate_dis[7] + arc;

	this->init_track();
	return;
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

void RTLSClient::setPosSimplified(Tag*t, int id, double distance)
{
	t->effective = true;
	t->id = id;
	t->pos.distance = distance;
	for (int i = 0; i < 8; i++) {
		if (distance >= _cumulate_dis[i] && distance < _cumulate_dis[i + 1]) {
			t->pos.n = i;
			t->pos.offset = distance - _cumulate_dis[i];
			break;
		}
	}
	//cout << "distance:" << t->pos.distance << endl;
}
void RTLSClient::setPos(Tag*t,int id,int n,int offset)
{
	t->effective=true;
	t->id=id;
	t->pos.n=n;
	t->pos.offset=offset;
	t->pos.distance=_cumulate_dis[n]+offset;
	//cout <<"distance:"<< t->pos.distance << endl;
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

void RTLSClient:: cal_circle(double a1,double b1,double a2,double b2,double r1,double r2){
	//y=kx+b
	//cout <<"&&"<< a1 <<" "<< b1<<" " << a2 << " "<< b2 <<" "<< r1 <<" " << r2 << endl;
	if (b1 - b2<1e-6&&b1 - b2>-(1e-6)) {
		cal_circle(b1, a1, b2, a2, r1, r2);
		double temp = _x1;
		_x1 = _y1;
		_y1 = _x1;
		temp = _x2;
		_x2 = _y2;
		_y2 = _x2;
		return;
	}

	double k=-(a2-a1)/(b2-b1);
	double b=(r1*r1-r2*r2-a1*a1+a2*a2-b1*b1+b2*b2)/(2*(b2-b1));
	cout << k << " " << b << " " << endl;
	//p1x^2+p2x+p3;;
	double p1=1+k*k;
	double p2=-2*a1+2*k*(b-b1);
	double p3=a1*a1+(b-b1)*(b-b1)-r1*r1;
	
	double delta=p2*p2-4*p1*p3;
	if(delta<0){
		//cout << "No crossover point " << delta << endl;
		_y1 = _y2 = b;
		_x1 = _x2 = 0;
	}
	else
	{
		double x1 = (-p2 - sqrt(delta)) / (2 * p1);
		double x2 = (-p2 + sqrt(delta)) / (2 * p1);

		double y1 = k*x1 + b;
		double y2 = k*x2 + b;
		cout << "x:" << x1 << "y:" << y1 << endl;
		cout << "x:" << x2 << "y:" << y2 << endl;

		_x1 = x1, _x2 = x2, _y1 = y1, _y2 = y2;
	}
	//exit(0);
	return;
}

void RTLSClient::processTag(char * frame,int tag_id)
{
	//check frame header
	if (frame[0] != (char)0xD6 || frame[1] != (char)0x6D) {
		cout << "Error:frame header not 0xD66D\n";
		return;
	}

	if (frame[2] != (char)0x2) {
		cout << "Error:wrong frame type\n";
		return;
	}

	if (frame[3] != (char)tag_id) {
		cout << "Error:wrong tag id\n";
		return;
	}
	uint16_t dis_frame[12];
	for(int i=6;i<12;i++){
		dis_frame[i]=(uint8_t)frame[i];
	}
	uint16_t sequence = dis_frame[4] | dis_frame[5] << 8;
	double dis0 = (double)(uint16_t)(dis_frame[6] | (dis_frame[7] << 8));
	double dis1 = (double)(uint16_t)(dis_frame[8] | (dis_frame[9] << 8));
	double dis2 = (double)(uint16_t)(dis_frame[10] | (dis_frame[11] << 8));

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

	this->cal_circle(0,0,0,_width+2*_radius,dis1,dis2);
	double disxy1=(_x1-(_length+2*_radius))*(_x1-(_length+2*_radius))+_y1*_y1;
	double disxy2=(_x2-(_length+2*_radius))*(_x2-(_length+2*_radius))+_y2*_y2;
	double x,y;
	if(disxy1<disxy2){
		_choose=1;
		x=_x1;
		y=_y1;
	}
	else{
		_choose=2;
		x=_x2;
		y=_y2;
	}
	cout << "x: " << x << "y: " << y << endl;

	int track_x, track_y;
	int pos = 0;
	double ans = 1000;
	for (int i = 0; i < track.size(); i++) {
		track_x = (int)track[i].first;
		track_y = (int)track[i].second;
		if (ans >(x - track_x)*(x - track_x) + (y - track_y)*(y - track_y)) {
			ans = (x - track_x)*(x - track_x) + (y - track_y)*(y - track_y);
			pos = i;
		}
	}

	this->setPosSimplified(t, tag_id, pos*_precision);
	return;

	

	//0
	if(between(x,_radius,_radius+_length)&&between(y,0,0)){
		if (x - _radius < 0) {
			this->setPos(t, tag_id, 8, x - _radius);
		}
		else {
			this->setPos(t, tag_id, 0, x - _radius);
		}
	}

	//1
	else if(between(x,_radius+_length,2*_radius+_length)&&between(y,0,_radius)){
		double alpha=asin((x-_radius-_length)/_radius);
		if (x - _radius - _length < 0) {
			this->setPos(t, tag_id, 1, 0);
		}
		else {
			this->setPos(t, tag_id, 1, _radius*alpha* PI / 180);
		}
	}

	//2
	else if(between(x,_radius*2+_length,_radius*2+_length)&&between(y,_radius,_radius+_width)){
		this->setPos(t,tag_id,2,y-_radius);
	}

	//3
	else if(between(x,_radius+_length,_radius*2+_length)&&between(y,_radius+_width,2*_radius+_width)){
		double alpha=asin((y-_radius-_width)/_radius);
		if (y - _radius - _width < 0) {
			this->setPos(t, tag_id, 3,0);
		}
		else {
			this->setPos(t, tag_id, 3, _radius*alpha*PI / 180);
		}
	}

	//4
	else if(between(x,_radius,_radius+_length)&&between(y,2*_radius+_width,2*_radius+_width)){
		this->setPos(t,tag_id,4,_radius+_length-x);
	}

	//5
	else if(between(x,0,_radius)&&between(y,_radius+_width,2*_radius+_width)){
		double alpha=asin((_radius-x)/_radius);
		if (_radius - x < 0) {
			this->setPos(t, tag_id, 5, 0);
		}
		this->setPos(t,tag_id,5,_radius*alpha*PI/180);
	}
	
	//6
	else if(between(x,0,_devia*2)&&between(y,_radius,_radius+_width)){
		this->setPos(t, tag_id, 6, dis2-_radius);
		//this->setPos(t,tag_id,6,_radius+_width-y);
	}

	//7
	else if(between(x,0,_radius)&&between(y,0,_radius)){
		double alpha=asin((_radius-y)/_radius);
		if (_radius - y < 0) {
			this->setPos(t, tag_id, 7, 0);
		}
		else {
			this->setPos(t, tag_id, 7, _radius*alpha*PI / 180);
		}
	}

	else {

	}

	return ;
	/*
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
	cout << "Error:distance calculation error\n";*/
	return;
}

void RTLSClient::processAnchor(char * frame)
{
	return;

}



void RTLSClient::processData(Position&p0,Position&p1,int numCars)
{
	_t0.effective = false;
	_t1.effective = false;
	processAnchor(_A0);
	processTag(_T0,0);
	if(numCars==2){
		processTag(_T1,1);
	}

	if (_t0.effective) {
		p0=_t0.pos;
	}
	else {
		cout << "Error:calculate t0 distance error\n";
	}

	if (_t1.effective&&numCars==2) {
		p1=_t1.pos;
	}
	else if(numCars==2) {
		cout << "Error:calculate t1 distance error\n";
	}

	return;
}


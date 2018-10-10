#include "RTLSClient.h"



RTLSClient::RTLSClient()
{
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

void RTLSClient::processData(int&dis_t0, int &dis_t1)
{
	_t0.effective = false;
	_t0.effective = false;
	processAnchor(_A0);
	processTag(_T0,0);
	processTag(_T1,1);
	if (_t0.effective) {
		dis_t0 = (int)(_t0.pos);
	}
	else {
		cout << "Error:calculate t0 distance error\n";
	}
	if (_t1.effective) {
		dis_t1 = (int)(_t1.pos);
	}
	else {
		cout << "Error:calculate t1 distance error\n";
	}

	return;
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

	/*location 0,1,2,3
	0 : anchor 0-1
	1 : anchor 1-2
	2 : anchor 2-null-0
	*/

	if (dis0 <= 0 || dis1 <= 0 || dis2 <= 0) {
		cout << "Error:distance is less than zero\n";
		return;
	}

	if (dis0 >= (_bias - _devia)
		&& dis0 <= (sqrt(_bias*_bias + _width * +_width) + _devia)
		&& dis1 >= (_bias - _devia)
		&& dis1 <= (_width + _bias + _devia)
		){
		double a0, a1,a2;
		a0 = sqrt(dis0*dis0 - _bias * _bias);
		a1 = dis1 - _bias;
		a2 = sqrt(dis2*dis2 - (_bias + _length)*(_bias + _length));
		a1 = _width - a1;
		a2 = _width - a2;

		double avg = (a0 + a1 + a2) / 3;
		if (avg<-_devia || avg>_width + _devia) {
			cout << "Error:distance calculation error 0 #" << avg << "#\n";
			return;
		}

		//write to tag
		Tag *t;
		if (tag_id == 0) {
			t = &_t0;
		}
		else {
			t = &_t1;
		}
		t->id = tag_id;
		t->zone = 0;
		t->pos = avg;
		t->effective = true;
		return;
	}
	
	if (dis1 >= (_bias - _devia)
		&& dis1 <= (sqrt(_bias*_bias + _length * _length) + _devia)
		&& dis2 >= (_bias - _devia)
		&& dis2 >= (_length + _bias + _devia)
		) {
		double a0, a1, a2;
		a0 = sqrt(dis0*dis0 - _width * _width) - _bias;
		a1 = sqrt(dis1*dis1 - _bias * _bias);
		a2 = dis2 - _bias;
		a2 = _length - a2;
		double avg = (a0 + a1 + a2) / 3;
		if (avg<-_devia || avg>_length + _devia) {
			cout << "Error:distance calculation error 1 #" << avg << "#\n";
			return;
		}

		//write to tag
		Tag *t;
		if (tag_id == 0) {
			t = &_t0;
		}
		else {
			t = &_t1;
		}
		t->id = tag_id;
		t->zone = 1;
		t->pos = avg+_width;
		t->effective = true;
		return;
	}
	
	if(dis2>=(_bias-_devia)
		&&dis2*dis2<=(_bias*_bias+_width*_width+_devia)
		&&dis0>=(_length+_bias-_devia)
		&& dis0 <= (sqrt(_width*_width + (_length + _bias)*(_length + _bias) )+ _devia) 
		){

		double a0, a1, a2;
		a0 = sqrt(dis0*dis0 - (_length + _bias)*(_length + _bias));
		a0 = _width - a0 + _length + _width;
		a1 = sqrt(dis1*dis1 - _length * _length) - _bias + _length + _width;
		a2 = sqrt(dis2*dis2 - _bias * _bias) + _width + _length;

		double avg = (a0 + a1 + a2) / 3;
		if (avg<-_devia || avg>_length + _devia) {
			cout << "Error:distance calculation error 2 #" << avg << "#\n";
			return;
		}

		//write to tag
		Tag *t;
		if (tag_id == 0) {
			t = &_t0;
		}
		else {
			t = &_t1;
		}
		t->id = tag_id;
		t->zone = 2;
		t->pos = avg ;
		t->effective = true;
		return;
	}

	if (dis0 >= (_bias - _devia)
		&& dis0 <= (_bias + _devia + _length)
		&& dis2 >= (sqrt(_width*_width + _bias * _bias) - _devia)
		&& dis2 <= (sqrt(_width*_width + (_bias + _length)*(_bias + _length)) + _devia)
		) {

		double a0, a1, a2;
		a0 = dis0 - _bias;
		a1 = sqrt(dis1*dis1 - (_bias + _width)*(_bias + _width));
		a2 = sqrt(dis2*dis2 - _width * _width) - _bias;
		a2 = _length - a2;
		double avg = (a0 + a1 + a2) / 3;
		if (avg<-_devia || avg>_length + _devia) {
			cout << "Error:distance calculation error 3 #" << avg << "#\n";
			return;
		}

		//write to tag
		Tag *t;
		if (tag_id == 0) {
			t = &_t0;
		}
		else {
			t = &_t1;
		}
		t->id = tag_id;
		t->zone = 1;
		t->pos = avg + _width+_width+_length;
		t->effective = true;
		return;
	}

	cout << "Error:distance not in zone 0,1,2,3\n";
	return;
}

void RTLSClient::processAnchor(char * frame)
{
	return;

}

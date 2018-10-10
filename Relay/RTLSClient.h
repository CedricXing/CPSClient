#pragma once
#include <iostream>
#include <cmath>
using namespace std;
#define DEVIA_OF_RTLS (10)

class RTLSClient
{
	struct Tag{
		bool effective;
		int id;
		double pos;
		int zone;//0,1,2,3
	};
public:
	RTLSClient();
	~RTLSClient();
	void getData(char*t0, char*t1, char*a0);
	void processData(int&t0,int &t1);
	void processTag(char*frame,int tag_id);
	void processAnchor(char*frame);
private:
	char _A0[256];
	char _T0[256];
	char _T1[256];

	//tag structure
	struct Tag _t0;
	struct Tag _t1;
	//configuration
	double _width;
	double _length;
	double _bias;
	double _gradient;

	//diviation
	double _devia = DEVIA_OF_RTLS;
};


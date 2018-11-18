#pragma once
#include <iostream>
#include <cmath>
using namespace std;
#define DEVIA_OF_RTLS (10)
#define PI (3.14159265358979323846)

struct Position{
	int n;//0-7
	double offset;
	double distance;
};
class RTLSClient
{
	struct Tag{
		bool effective;
		int id;
		Position pos;
	};
public:
	RTLSClient();
	~RTLSClient();
	void getData(char*t0, char*t1, char*a0);
	//void processData(int&t0,int &t1);
	void processData(Position&p0,Position&p1);
	void processTag(char*frame,int tag_id);
	void processAnchor(char*frame);
	void setPos(Tag*t,int id,int n,int offset);
	bool between(double x,double a,double b);
	bool between2(double x,double fixed,double a,double b);
	double cal_arc(double rmax,double dis);
	void check_offset(int n,double offset,double dis0,double dis1,double dis2);
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
	double _radius;
	double _cumulate_dis[8];

	//diviation
	double _devia = DEVIA_OF_RTLS;
};


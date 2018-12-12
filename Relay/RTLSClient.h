#pragma once
#include <iostream>
#include <cmath>
#include <vector>
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
	void processData(Position&p0,Position&p1,int numCars);
	void processTag(char*frame,int tag_id);
	void processAnchor(char*frame);
	void setPos(Tag*t,int id,int n,int offset);
	void setPosSimplified(Tag*t, int id, double distance);
	bool between(double x,double a,double b);
	bool between2(double x,double fixed,double a,double b);
	void cal_circle(double a1,double b1,double a2,double b2,double r1,double r2);
	double cal_arc(double rmax,double dis);
	void check_offset(int n,double offset,double dis0,double dis1,double dis2);
	void init_track();
private:
	char _A0[256];
	char _T0[256];
	char _T1[256];
	//crossover points
	double _x1,_x2,_y1,_y2;
	int _choose;
	//tag structure
	struct Tag _t0;
	struct Tag _t1;
	//configuration
	double _width;
	double _length;
	double _radius;
	double _cumulate_dis[9];
	double _precision;
	vector<pair<double, double> > track;


	//diviation
	double _devia = DEVIA_OF_RTLS;
};


#pragma once

#include "SerialPort.h"
#include "UDP.h"
#include <iostream>
#include <cstdio>
#include "RTLSClient.h"
#include <fstream>
#include <string>
using namespace std;

#define MAX_SPEED 100.00
#define MAX_POSITION 1200

class Bridge {

	struct Buf {
		char buffer[1024];
		int cnt;
		Buf() :cnt(0) {}
	};

	struct uwbBuf {
		char A0[256];
		char T0[256];
		char T1[256];
		bool hasA0, hasT0, hasT1;
		uwbBuf() :hasA0(false), hasT0(false), hasT1(false) {}
		bool hasAll() {
			return hasA0 && hasT0 && hasT1;
		}
		void reset() {
			hasA0 = hasT0 = hasT1 = false;
		}
	};
public:
	Bridge(int zigbeePortNum, int uwbPortNum, char* ipAddr, int numCars=2);
	~Bridge();
	void listen();
public:
	void reset();
	void sendPosToCar();
	void sendMaToCar();
	void sendToVerify();
	void updatePosition();
	void printBuffer(Buf& buffer);
	bool ableToVerify();
	bool checkCarData();
	void writeCarPosition(Position**list, int num);
	CSerialPort zigbeePort;
	CSerialPort uwbPort;
	UDP udp;
	Buf carBuffer;
	uwbBuf uwbBuffer;
	int numCars = 2;
	int *pos, *ma, *safe;
	float *speed;
};

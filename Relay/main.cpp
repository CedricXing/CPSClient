#include <iostream>
#include <string>
#include <cstdio>
#include "Bridge.h"
#include "SerialPort.h"
using namespace std;

int main()
{
	char ipAddr[64];
	cout << "IP:";
	cin >> ipAddr;
	Bridge bridge(3, 4, ipAddr, 2);
	//bridge.listen();
	cout << "SDFS";
	bridge.sendToVerify();
	return 0;
}

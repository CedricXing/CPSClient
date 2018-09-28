#include <iostream>
#include <string>
#include <cstdio>
#include "Bridge.h"
#include "SerialPort.h"
using namespace std;

int main()
{
	char ipAddr[64];
	cin >> ipAddr;
	Bridge bridge(5, 4, ipAddr, 2);
	bridge.listen();
	return 0;
}

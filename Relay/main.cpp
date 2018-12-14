#include <iostream>
#include <string>
#include <cstdio>
#include "Bridge.h"
#include "SerialPort.h"
using namespace std;

const int numCars = 1;

void writeNumCars() {
	string filename = "CarNumber.txt";
	ofstream out(filename);
	out << numCars << endl;
	out.close();
}

int main()
{
	char ipAddr[64];
	cout << "IP:";
	cin >> ipAddr;
	writeNumCars();
	Bridge bridge(3,4, ipAddr, numCars);
	bridge.listen();
	//cout << "SDFS";
	//bridge.sendToVerify();
	return 0;
}

#include "Bridge.h"
#include <Windows.h>

Bridge::Bridge(int zigbeePortNum, int uwbPortNum, char* ipAddr, int numCars) {
	udp.setAddr(ipAddr);
	if (!zigbeePort.InitPort(zigbeePortNum, CBR_115200) || !uwbPort.InitPort(uwbPortNum, CBR_115200)) {
		cout << "Init Port Fail!" << endl;
		exit(-1);
	}
	pos = new int[numCars];
	ma = new int[numCars];
	safe = new int[numCars];
	speed = new float[numCars];
	reset();
}

Bridge::~Bridge() {
	delete[]pos;
	delete[]ma;
	delete[]safe;
	delete[]speed;
}

void Bridge::listen() {
	char preCharZigbee = '0';
	while (1) {
		int BytesInQueZigbee = zigbeePort.GetBytesInCOM();
		char cRecved = 0x00;
		int alertCnt = 0;
		while (BytesInQueZigbee > 0) {
			if (zigbeePort.ReadChar(cRecved)) {
				if (cRecved == 'X') {
					alertCnt = (preCharZigbee == 'X') ? alertCnt + 1 : 1;
					preCharZigbee = 'X';
					if (alertCnt >= 5) {
						alertCnt = 0;
						preCharZigbee = '0';
						carBuffer.cnt = 0;
					}
				}
				else {
					alertCnt = 0;
					preCharZigbee = '0';
					carBuffer.buffer[carBuffer.cnt++] = cRecved;
					if (carBuffer.cnt == 1 + 4 + 4) {
						if (checkCarData()) {
							int number;
							number = carBuffer.buffer[0] - '0';
							memcpy(speed + number, carBuffer.buffer + 1, 4);
							memcpy(pos + number, carBuffer.buffer + 1 + 4, 4);
							cout << "carNum = " << number << " speed = " << speed[number] << " pos = " << pos[number] << endl;
						}
					}
				}
				BytesInQueZigbee--;
			}
		}

		int BytesInQueUwb = uwbPort.GetBytesInCOM();
		if (BytesInQueUwb >= 14) {
			Buf buffer;
			BytesInQueUwb--;
			while (BytesInQueUwb > 0 && uwbPort.ReadChar(cRecved) && cRecved != (char)0xd6) {
				BytesInQueUwb--;
			}
			if (cRecved == (char)0xd6 && BytesInQueUwb >= 13) {
				buffer.buffer[buffer.cnt++] = (char)0xD6;
				while (buffer.cnt < 14) {
					if (uwbPort.ReadChar(cRecved)) {
						buffer.buffer[buffer.cnt++] = cRecved;
					}
				}
				if (buffer.buffer[2] == (char)0x01) {
					memcpy(uwbBuffer.A0, buffer.buffer, 14);
					uwbBuffer.hasA0 = true;
					printBuffer(buffer);
				}
				else if (buffer.buffer[2] == (char)0x02) {
					if (buffer.buffer[3] == (char)0x00) {
						memcpy(uwbBuffer.T0, buffer.buffer, 14);
						uwbBuffer.hasT0 = true;
						printBuffer(buffer);
					}
					else if (buffer.buffer[3] == (char)0x01) {
						memcpy(uwbBuffer.T1, buffer.buffer, 14);
						uwbBuffer.hasT1 = true;
						printBuffer(buffer);
					}
					else {
						cout << "error when receiving uwb message1." << endl;
						exit(-3);
					}
					if (uwbBuffer.hasAll()) {
						updatePosition();
						sendPosToCar();
						uwbBuffer.reset();
					}
				}
				else {
					cout << "error when receiving uwb message2." << endl;
					exit(-2);
				}
			}
		}
		if (ableToVerify()) {
			sendToVerify();
			reset();
			uwbPort.ClearBuffer(); // whether to clear zigbeePort depends on time interval
		}

	}
}

void Bridge::sendPosToCar() {
	char buffer[64];
	/*
		0-4: "AAAAA"
		5: car number
		6: reserved
		7-10: position
	*/
	memset(buffer, 'A', 5);
	for (int i = 0; i < numCars; i++) {
		buffer[5] = '0' + i;
		buffer[6] = 0; // reserved;
		memcpy(buffer + 7, pos + i, 4);
		zigbeePort.WriteData(buffer, 5 + 1 + 1 + 4);
		Sleep(5);
	}
}

void Bridge::sendMaToCar() {
	char buffer[64];
	/*
		0-4: "BBBBB"
		5: car number
		6: safe or not
		7-10: MA
	*/
	memset(buffer, 'B', 5);
	for (int i = 0; i < numCars; i++) {
		buffer[5] = '0' + i;
		buffer[6] = '0' + safe[i];
		memcpy(buffer + 7, ma + i, 4);
		zigbeePort.WriteData(buffer, 5 + 1 + 1 + 4);
		cout << "send to car: carNum = " << i << " safe = " << safe[i] << " ma = " << ma[i] << endl;
		Sleep(5);
	}
}

void Bridge::sendToVerify() {
	char sendBuf[100];
	/*
		0: car number
		1-5: speed
		6-8: position
	*/
	for (int i = 0; i < numCars; i++) {
		memset(sendBuf, '0', sizeof sendBuf);
		sendBuf[0] = '0' + i;
		sprintf(sendBuf + 1, "%.2f", speed[i]);
		sprintf(sendBuf + 1 + 5, "%d", pos[i]);
		udp.sendPacket(sendBuf, 1 + 5 + 3);
	}
	cout << "Done sending!" << endl;

	char recvBuf[100];
	/*
		0: car number
		1: safe or not
		2-4: MA
	*/
	for (int i = 0; i < numCars; i++) {
		udp.recvPacket(recvBuf); 
		zigbeePort.WriteData(recvBuf, 1 + 1 + 3);
		int number = recvBuf[0] - '0';
		sscanf(recvBuf + 1, "%d", &safe[number]);
		sscanf(recvBuf + 1 + 1, "%d", &ma[number]);
	}
	cout << "Done receiving!" << endl;
	sendMaToCar();
}

void Bridge::updatePosition() {
	
}

void Bridge::printBuffer(Buf& buffer) {
	return;
	for (int i = 0; i < 14; i++) {
		printf("%02x ", ((((unsigned)buffer.buffer[i]) << 24) >> 24));
	}
	cout << endl;
}

bool Bridge::ableToVerify() {
	for (int i = 0; i < numCars; i++) {
		if (pos[i] == -1 || speed[i] < 0)return false;
	}
	return true;
}

bool Bridge::checkCarData() {
	int number = carBuffer.buffer[0] - '0';
	float speed;
	int pos;
	memcpy(&speed, carBuffer.buffer + 1, 4);
	memcpy(&pos, carBuffer.buffer + 1 + 4, 4);
	if (!(number >= 0 && number < numCars))return false;
	if (!(speed >= 0 && speed <= MAX_SPEED))return false;
	if (!(pos >= 0 && pos <= MAX_POSITION))return false; 
	return true;
}

void Bridge::reset() {
	memset(pos, -1, sizeof pos);
	memset(ma, -1, sizeof ma);
	memset(safe, -1, sizeof safe);
	for (int i = 0; i < numCars; i++) {
		speed[i] = -1;
	}
}
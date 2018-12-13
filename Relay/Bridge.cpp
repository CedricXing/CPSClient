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
	requestVerification = new int[numCars];
	reset();
	this->numCars = numCars;
}

Bridge::~Bridge() {
	delete[]pos;
	delete[]ma;
	delete[]safe;
	delete[]speed;
	delete[]requestVerification;
}

void Bridge::listen() {
	char preCharZigbee = '0';
	char cRecved = 0x00;
	int alertCnt = 0;
	int verifyCnt = 0;
	bool prepared = false;
	bool preparedToVerify = false;

	Buf cycleBuf;
	
	while (1) {
		/*
			speed message:
			0-4: XXXXX
			5:   carNum
			6-9: speed (float binary)

			verification request message:
			0-4: SSSSS
			5:   carNum
			6-9: current verification cycle number
		*/
		int BytesInQueZigbee = zigbeePort.GetBytesInCOM();
		

		while (BytesInQueZigbee > 0) {
			if (zigbeePort.ReadChar(cRecved)) {
				// printf("%02x ", ((((unsigned)cRecved) << 24) >> 24));
				if (cRecved == 'X'&&!prepared&&!preparedToVerify) {
					alertCnt = (preCharZigbee == 'X') ? alertCnt + 1 : 1;
					preCharZigbee = 'X';
					prepared = false;
					if (alertCnt >= 5) {
						prepared = true;
						alertCnt = 0;
						carBuffer.cnt = 0;
					}
				}
				else if (cRecved == 'S'&&!preparedToVerify&&!prepared) {
					cout << "!!!!!!!!!!!!!!!!!SSSSSSSSSSSSSSSSSSSS" << endl;
					verifyCnt = (preCharZigbee == 'S') ? verifyCnt + 1 : 1;
					preCharZigbee = 'S';
					preparedToVerify = false;
					if (verifyCnt >= 5) {
						preparedToVerify = true;
						verifyCnt = 0;
						cycleBuf.cnt = 0;
					}
				}
				else if(prepared){
					preCharZigbee = '0';
					carBuffer.buffer[carBuffer.cnt++] = cRecved;
					if (carBuffer.cnt == 1 + 4) {
						int number;
						number = carBuffer.buffer[0] - '0';
						memcpy(speed + number, carBuffer.buffer + 1, 4);
						if (speed[number] >= 0 && speed[number] <= MAX_SPEED) 
						{
							cout << "carNum = " << number << " speed = " << speed[number] << endl;
						}
						else {
							cout << "wrong message received from car "<<number<<" set speed to 0!" << endl;
							speed[number] = 0;
						}
						prepared = false;
					}
				}
				else if (preparedToVerify) {
					preCharZigbee = '0';
					cycleBuf.buffer[cycleBuf.cnt++] = cRecved;
					if (cycleBuf.cnt == 1 + 4) {
						int number = cycleBuf.buffer[0] - '0';
						int cycle;
						memcpy(&cycle, cycleBuf.buffer + 1, 4);
						cout << "recv cycle = " << cycle << endl;
						if (number >= 0 && number < numCars){
							if (cycle == curCycle) {
								requestVerification[number] = cycle;
							}
							else if(cycle<curCycle) {
								cout << "recv cycleNum already verified! resend results!" << endl;
								//sendMaToCar(cycle);
								sendMaToCarTest(cycle);
							}
							else {
								cout << "error! recv cycleNum larger than curCycle!" << endl;
							}
						}
						else {
							cout << "wrong carNum in verification request message: " << number << endl;
						}
						preparedToVerify = false;
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
					//cout << "has A0" << endl;
					//printBuffer(buffer);
				}
				else if (buffer.buffer[2] == (char)0x02) {
					if (buffer.buffer[3] == (char)0x00) {
						memcpy(uwbBuffer.T0, buffer.buffer, 14);
						uwbBuffer.hasT0 = true;
						//cout << "has T0" << endl;
						//printBuffer(buffer);
					}
					else if (buffer.buffer[3] == (char)0x01) {
						memcpy(uwbBuffer.T1, buffer.buffer, 14);
						uwbBuffer.hasT1 = true;
						//cout << "hasT1" << endl;
						//printBuffer(buffer);
					}
					else {
						cout << "error when receiving uwb message1." << endl;
						exit(-3);
					}
					if (uwbBuffer.hasAll(numCars)) {
						//cout << "enter update" << endl;
						//system("pause");
						updatePosition();
						for (int i = 0; i < numCars; i++) {
							cout << "pos"<<i<<"="<<pos[i] << endl;
						}
						// reduce pressure on zigbee buffer in car
						if (sendPosOrNot()) {
							sendPosToCar();
						}
						uwbBuffer.reset();
					}
				}
				else {
					cout << "error when receiving uwb message2." << endl;
					exit(-2);
				}
			}
		}
		if (ableToVerify()){
			//sendToVerify();
			//recvVerifyInfo();
			
			pushVerifyInfo();

			//sendMaToCar(curCycle);
			sendMaToCarTest(curCycle);
			cout << "OK" << endl;

			writeVerifyInfo();
			curCycle++;
		//	system("pause");
			reset();
			uwbPort.ClearBuffer(); // whether to clear zigbeePort depends on time interval
		}

	}
}

bool Bridge::sendPosOrNot() {
	static int cnt = 0;
	if (cnt++ % 5 == 0)return true;
	return false;
}

void Bridge::sendPosToCar() {
	const int unitLen = 11;
	char buffer[MAX_NUM_CARS*unitLen];
	/*
		0-4: "AAAAA"
		5: car number
		6: reserved
		7-10: position
	*/
	for (int i = 0; i < numCars; i++) {
		memset(buffer + i*unitLen, 'A', 5);
		buffer[i*unitLen + 5] = '0' + i;
		memcpy(buffer + i*unitLen + 7, pos + i, 4);
	}
	zigbeePort.WriteData(buffer, unitLen*numCars);
}

void Bridge::sendMaToCar(int cycleNum) {
	const int unitLen = 15;
	char buffer[MAX_NUM_CARS*unitLen];
	/*
		0-4: "BBBBB"
		5: car number
		6: safe or not
		7-10: MA  (in binary)
		11-14: cycleNum (in binary)
	*/
	for (int i = 0; i < numCars; i++) {
		memset(buffer+i*unitLen, 'B', 5);
		buffer[i*unitLen+5] = '0' + i;
		buffer[i*unitLen+6] = '0' + verifyMsgs[cycleNum].safe[i];
		memcpy(buffer+i*unitLen + 7, verifyMsgs[cycleNum].ma + i, 4);
		memcpy(buffer + i*unitLen + 11, &cycleNum, 4);
		/*cout << "BUFFER:" << endl;
		for (int i = 0; i < 14; i++) {
			printf("%02x ", ((((unsigned)buffer[i]) << 24) >> 24));
		}
		cout << endl;*/
		cout << "cycleNum = "<<cycleNum<<" send to car: carNum = " << i << " safe = " << 
			verifyMsgs[cycleNum].safe[i] << " ma = " << verifyMsgs[cycleNum].ma[i] << endl;
	}
	zigbeePort.WriteData(buffer, unitLen*numCars);
}

void Bridge::sendMaToCarTest(int cycleNum) {
	cout << "testing ..." << endl;
	const int unitLen = 15;
	char buffer[MAX_NUM_CARS*unitLen];
	/*
	0-4: "BBBBB"
	5: car number
	6: safe or not
	7-10: MA  (in binary)
	11-14: cycleNum (in binary)
	*/
	for (int i = 0; i < numCars; i++) {
		verifyMsgs[cycleNum].safe[i] = 1;
		verifyMsgs[cycleNum].ma[i] = 900+cycleNum;
		memset(buffer + i*unitLen, 'B', 5);
		buffer[i*unitLen + 5] = '0' + i;
		buffer[i*unitLen + 6] = '0' + verifyMsgs[cycleNum].safe[i];
		memcpy(buffer + i*unitLen + 7, verifyMsgs[cycleNum].ma + i, 4);
		memcpy(buffer + i*unitLen + 11, &cycleNum, 4);
		cout << "test: cycleNum = "<<cycleNum<<" send to car: carNum = " << i << " safe = " << safe[i] << " ma = " << ma[i] << endl;
	}
	zigbeePort.WriteData(buffer, unitLen*numCars);
}

void Bridge::sendToVerify() {
	char sendBuf[100];
	/*
		0: car number
		1-5: speed
		6-9: position
	*/
	int packageCnt=0;
	int unitLen = 10;

	
	//test ------------
	/*speed[0] = 12.34;
	pos[0] = 567;
	speed[1] = 98.76;
	pos[1] = 901;*/

	// -----------------
	
	memset(sendBuf, '0', sizeof(sendBuf));
	for (int i = 0; i < numCars; i++) {
		char tbuf[10];
		tbuf[0] = '0' + i;
		sprintf(tbuf+1, "%02.2f", speed[i]);
		sprintf(tbuf+1+5, "%04d", pos[i]);
		memcpy(sendBuf + i*unitLen, tbuf, unitLen);
	}
	udp.sendPacket(sendBuf, unitLen*numCars);
	cout << "Done sending!" << endl;	
}

void Bridge::recvVerifyInfo()
{
	char recvBuf[100];
	/*
	0: car number
	1: safe or not
	2-5: MA
	*/
	cout << "receiving..." << endl;
	udp.recvPacket(recvBuf);
	char* recvBufPoint = recvBuf;
	for (int i = 0; i < numCars; i++) {
		//udp.recvPacket(recvBuf); 
		// zigbeePort.WriteData(recvBuf, 1 + 1 + 3);
		int number = recvBufPoint[0] - '0';
		safe[number] = recvBufPoint[1] - '0';
		ma[number] = 1000 * (recvBufPoint[2] - '0') +
			100 * (recvBufPoint[3] - '0') +
			10 * (recvBufPoint[4] - '0') +
			1 * (recvBufPoint[5] - '0');
		recvBufPoint += 6;
		cout << "carnum = " << number << " safe = " << safe[number] << " ma = " << ma[number] << endl;
		// system("pause");
	}
	cout << "Done receiving!" << endl;
}

void Bridge::updatePosition() {
//	cout << "in position" << endl;
	RTLSClient* rtls = new RTLSClient();
	rtls->getData(uwbBuffer.T0, uwbBuffer.T1, uwbBuffer.A0);
	Position pos0, pos1;
	rtls->processData(pos0, pos1,this->numCars);
	this->pos[0] = (int)(pos0.distance);
//	cout << "pos0=" << pos[0] << endl;

	if(numCars==2)(this->pos[1] = (int)(pos1.distance));
	Position*list[2];
	list[0] = &pos0;
	list[1] = &pos1;
	if (hasAllCarInfo()) {
		this->writeCarInfo(list, this->numCars);
	}
	return;
}

void Bridge::printBuffer(Buf& buffer) {
//	return;
	for (int i = 0; i < 14; i++) {
		printf("%02x ", ((((unsigned)buffer.buffer[i]) << 24) >> 24));
	}
	cout << endl;
}

bool Bridge::ableToVerify() {
	for (int i = 0; i < numCars; i++) {
		if (pos[i] < 0 || speed[i] < 0 || curCycle!=requestVerification[i])return false;
	}
	return true;
}

bool Bridge::hasAllCarInfo() {
	for (int i = 0; i < numCars; i++) {
		if (pos[i] < 0 || speed[i] < 0)return false;
	}
	return true;
}

//bool Bridge::checkCarData() {
//	int number = carBuffer.buffer[0] - '0';
//	float speed;
//	memcpy(&speed, carBuffer.buffer + 1, 4);
//	if (!(number >= 0 && number < numCars))return false;
//	if (!(speed >= 0 && speed <= MAX_SPEED)) {
//	//	return false;
//	//  if speed is invalid then set to 0 for now
//		speed = 0;
//		return true;
//	}
//	return true;
//}

void Bridge::reset() {
	memset(pos, -1, sizeof(int)*numCars);
	memset(ma, -1, sizeof(int)*numCars);
	memset(safe, -1, sizeof(int)*numCars);
	memset(requestVerification, 0, sizeof(int)*numCars);
	for (int i = 0; i < numCars; i++) {
		speed[i] = -1;
	}
}

void Bridge::writeCarInfo(Position**list, int num) {
	string filename = "StatusInfo.txt";
	ofstream out(filename);
	for (int i = 0; i<num; i++) {
		out << list[i]->n << " " << (int)list[i]->offset << " " << (speed[i]>=0?(int)speed[i]:0) << endl;
	}
	out.close();
}

void Bridge::writeVerifyInfo() {
	string filename = "VerifyInfo.txt";
	ofstream out(filename);
	for (int i = 0; i<numCars; i++) {
		out << (safe[i]?0:1) << " " << ma[i] << endl;
	}
	out.close();
}

void Bridge::pushVerifyInfo() {
	verifyMsg node(numCars);
	for (int i = 0; i < numCars; i++) {
		node.ma[i] = ma[i];
		node.safe[i] = safe[i];
	}
	verifyMsgs.push_back(node);
}
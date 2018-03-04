#include"SerialPort.h"
#include<iostream>

/*
	通过串口发送数据，同时监听串口输入并输出
*/
int main()
{

	CSerialPort mySerialPort;

	if (!mySerialPort.InitPort(5))
	{
		std::cout << "initPort fail !" << std::endl;
	}
	else
	{
		std::cout << "initPort success !" << std::endl;
	}

	if (!mySerialPort.OpenListenThread())
	{
		std::cout << "OpenListenThread fail !" << std::endl;
	}
	else
	{
		std::cout << "OpenListenThread success !" << std::endl;
	}
	unsigned char sendBuf[20];
	sendBuf[0] = 0x02; sendBuf[1] = 0x04; sendBuf[2] = 0x57; sendBuf[3] = 0xA8;
	sendBuf[4] = 0x06; sendBuf[5] = 0x11; sendBuf[6] = 0x22;
	while (1) {
		int a;
		std::cin >> a;
		memcpy(sendBuf + 4, &a, 1);
		mySerialPort.WriteData(sendBuf, 7);
	}
	int temp;
	std::cin >> temp;

	return 0;
}
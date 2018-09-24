#include"UDP.h"
UDP::UDP() {
	Port = 4455;
	BufLen = 1024;
	WSAStartup(MAKEWORD(2, 2), &wsaData);
	//创建Socket对象
	SendSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	RecvSocket = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
	//设置服务器地址
	RecvAddr.sin_family = AF_INET;
	RecvAddr.sin_port = htons(Port);
	RecvAddr2.sin_family = AF_INET;
	RecvAddr2.sin_port = htons(Port);
	RecvAddr2.sin_addr.s_addr = htonl(INADDR_ANY);
	bind(RecvSocket, (SOCKADDR *)&RecvAddr2, sizeof(RecvAddr2));
}
UDP::~UDP() {
	closesocket(SendSocket);
	WSACleanup();
}
void UDP::sendPacket(char data[],int len)
{
	memcpy(SendBuf, data, len);
	SendBuf[len] = '\0';
	int Len=sendto(SendSocket, SendBuf, len, 0, (SOCKADDR *)&RecvAddr, sizeof(RecvAddr));
	cout << Len << endl;
}
void UDP::setAddr(char addr[]) {
	RecvAddr.sin_addr.s_addr = inet_addr(addr);// ("172.25.181.125");
}
void UDP::recvPacket(char data[])
{
	int len = recvfrom(RecvSocket, data, BufLen, 0, (SOCKADDR *)&SenderAddr, &SenderAddrSize);
}
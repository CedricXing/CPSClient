#pragma warning(disable:4996)
#include<WINSOCK2.H>
#include<windows.h>
#include<iostream>
#pragma comment(lib,"WS2_32.lib")
#pragma once
using namespace std;
class UDP {
public:
	WSADATA wsaData;//初始化
	SOCKET SendSocket;
	SOCKET RecvSocket;
	sockaddr_in RecvAddr;//服务器地址
	sockaddr_in RecvAddr2;
	int Port;//服务器监听地址
	char SendBuf[1024];//发送数据的缓冲区
	char RecvBuf[1024];
	int BufLen;//缓冲区大小
	sockaddr_in SenderAddr;
	int SenderAddrSize = sizeof(SenderAddr);
	
	UDP();
	~UDP();
	void sendPacket(char data[],int len);
	void recvPacket(char data[]);
	void setAddr(char addr[]);
};

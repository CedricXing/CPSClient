#pragma warning(disable:4996)
#include<WINSOCK2.H>
#include<windows.h>
#include<iostream>
#pragma comment(lib,"WS2_32.lib")
#pragma once
using namespace std;
class UDP {
public:
	WSADATA wsaData;//��ʼ��
	SOCKET SendSocket;
	SOCKET RecvSocket;
	sockaddr_in RecvAddr;//��������ַ
	sockaddr_in RecvAddr2;
	int Port;//������������ַ
	char SendBuf[1024];//�������ݵĻ�����
	char RecvBuf[1024];
	int BufLen;//��������С
	sockaddr_in SenderAddr;
	int SenderAddrSize = sizeof(SenderAddr);
	
	UDP();
	~UDP();
	void sendPacket(char data[],int len);
	void recvPacket(char data[]);
	void setAddr(char addr[]);
};

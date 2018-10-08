#include <iostream>
#include <string>
#include <map>
#include <stdio.h>
#include <stdlib.h>
#include <cstdlib>
#include <fstream>
#include <vector>
#include <time.h>

using namespace std;
ofstream *out;
string deltat="0.1";

string _label(int x,int y){
    string str = "["+to_string(x)+","+to_string(x+1)+"]["+to_string(y)+","+to_string(y+1)+"]";
    return str;
}

void print(string str)
{
    (*out)<<str<<endl;
}

void print_cfg();
void print_xml();
int ** A;
int row,col;

int main()
{
	cout<<"input size of A:";
	cin>>row>>col;
	cout<<"input A"<<endl;
	A = new int* [row];
	for(int i=0;i<row;i++){
		A[i] = new int[col];
		for(int j=0;j<col;j++)
			cin>>A[i][j];
	}
	
		
	out=new ofstream("navigation_staliro.xml");
    	print_xml();
	out->close();

	out=new ofstream("navigation_staliro.cfg");
	print_cfg();
    	out->close();
    	return 0;
}

void print_xml(){
	print("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<sspaceex xmlns=\"http://www-verimag.imag.fr/xml-namespaces/sspaceex\" version=\"0.2\" math=\"SpaceEx\">");
	print("\t<component id=\"system\">");
	print("\t\t<param name=\"x\" type=\"real\" local=\"false\" d1=\"vx\"  dynamics=\"any\" />");
	print("\t\t<param name=\"y\" type=\"real\" local=\"false\" d1=\"vy\"  dynamics=\"any\" />");
	print("\t\t<param name=\"vx\" type=\"real\" local=\"false\" d1=\"1\"  dynamics=\"any\" />");
	print("\t\t<param name=\"vy\" type=\"real\" local=\"false\" d1=\"1\"  dynamics=\"any\" />");
	print("\t\t<param name=\"t\" type=\"real\" local=\"false\" d1=\"1\"  dynamics=\"any\" />");
	for(int i=1;i<=4*row*col-2*row-2*col;i++)
		print("\t\t<param name=\"e"+to_string(i)+"\" type=\"label\" local=\"false\" />");

	int locid=1;
	for(int i=row-1;i>=0;i--){
		for(int j=0;j<col;j++){
			int type = A[i][j];

			print("\t\t<location id=\""+to_string(locid)+"\" name=\"v"+to_string(locid)+"\">");
			print("\t\t\t<flow>t:=1 &amp;x:="+deltat+"*vx &amp; y:="+deltat+"*vy &amp;vx:="+deltat+ "*(-1.2*(vx-sin("+to_string(type)+"*3.1415926/4))+0.1*(vy-cos("+to_string(type)+"*3.1415926/4))) &amp; vy:="+deltat+ "*(-1.2*(vy-cos("+to_string(type)+"*3.1415926/4))+0.1*(vx-sin("+to_string(type)+"*3.1415926/4)))</flow> ");
			print("\t\t</location>");
			locid++;
		}
	}

	int adjid=1;
	for(int i=0;i<row;i++){
		for(int j=0;j<col-1;j++){
			int lid=i*col+j+1;
			int rid=lid+1;

			print("\t\t<transition source=\""+to_string(lid)+"\" target=\""+to_string(rid)+"\">");
			print("\t\t\t<label>e"+to_string(adjid)+"</label>");
			print("\t\t\t<guard>x&gt;="+to_string(j+1)+"</guard>");
			print("\t\t</transition>");
			adjid++;
			print("\t\t<transition source=\""+to_string(rid)+"\" target=\""+to_string(lid)+"\">");
			print("\t\t\t<label>e"+to_string(adjid)+"</label>");
			print("\t\t\t<guard>x&lt;="+to_string(j+1)+"</guard>");
			print("\t\t</transition>");
			adjid++;
		}
	}
	for(int i=0;i<row-1;i++){
		for(int j=0;j<col;j++){
			int did=i*col+j+1;
			int uid=did+col;//up one

			print("\t\t<transition source=\""+to_string(did)+"\" target=\""+to_string(uid)+"\">");
			print("\t\t\t<label>e"+to_string(adjid)+"</label>");
			print("\t\t\t<guard>y&gt;="+to_string(i+1)+"</guard>");
			print("\t\t</transition>");
			adjid++;
			print("\t\t<transition source=\""+to_string(uid)+"\" target=\""+to_string(did)+"\">");
			print("\t\t\t<label>e"+to_string(adjid)+"</label>");
			print("\t\t\t<guard>y&lt;="+to_string(i+1)+"</guard>");
			print("\t\t</transition>");
			adjid++;
		}
	}

	print("</component>");
	print("</sspaceex>");
	return;
}

void print_cfg(){
	print("# analysis options");
	print("system = \"system\"");
	print("initially = \"loc()==v3 & x==0.5 & y==3.5 & vx==0.1 & vy==-0.1 & t==0\"");
	print("forbidden = \"loc()==v2 & t<=10\"");
	
	print("rel-err = 1.0e-12");
	print("abs-err = 1.0e-13");
	print("time-step = "+deltat);
	print("max-step = 120");

	return;
}

//g++ -std=c++0x generate.cpp -o generate  

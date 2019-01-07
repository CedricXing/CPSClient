#include "include/common.h"
#include "include/telecom.h"
#include <stdlib.h>
int fd_zigbee=-1;
int fd_grating=-1;
static int cnt;
extern int CUR_POSITION;

int CUR_MA=201;
int CUR_SAFE=1;

int is_me(char buf[],int car_ID){
	int i=0;
	for(i=0;i<6;i++){
		if(isdigit(buf[i])){
			if(buf[i]-'0'==car_ID)return 1;
		}
	}
	return 0;
}
int is_type_B(char buf[]){
	int i=0;
	for(i=0;i<5;i++){
		if(buf[i]!='B')break;
	} 
	return i;
}
int is_type_A(char buf[]){
	int i=0;
	for(i=0;i<5;i++){
		if(buf[i]!='A')break;
	}
	return i;
}

int CUR_TURN=-1;
int TURN_IN_MAIN=-1;

void send_speed(int car_ID){
	char buf[10];
	float speed = get_speed();
	int i=0;
	int cnt=0;
	while(speed<0){
	  usleep(10000);
	  speed=get_speed();
	  cnt++;
	  if(cnt>10)speed=0;
	}
	memset(buf, '\0', sizeof(buf));
	
	for(i=0;i<5;i++)buf[i]='X';
	buf[5] = car_ID + '0';
	memcpy(buf + 5 + 1, &speed, 4);
	printf("\033[1;33;40m send speed ID: %d speed: %f\033[0m\n", car_ID, speed);
	zigbee_send_cmd(buf, 5 + 1 + 4);
}

void request_verification(int car_ID){
	char buf[20];
	int i;
	send_speed(car_ID);
	for(i=0;i<5;i++)buf[i]='S';
	buf[5]=car_ID+'0';
	memcpy(buf+6,&TURN_IN_MAIN,sizeof(int));
	zigbee_send_cmd(buf,5+1+4);
}

int telecom_main(int car_ID,int *safe) {
	printf("\033[1;35;40m request verification\033[0m\n");
	TURN_IN_MAIN++;
	int i,j;
	
	/*float speed=get_speed();
	while(speed<0){
	  usleep(1000);
	  speed=get_speed();
	}
	char buf[20];

	for(j=0;j<5;j++)buf[j]='X';
	buf[5] = car_ID + '0';
	memcpy(buf + 5 + 1, &speed, 4);
	for(i=0;i<3;i++){
	    printf("\033[1;33;40m send speed ID: %d speed: %f\033[0m\n", car_ID, speed);
	    zigbee_send_cmd(buf, 5 + 1 + 4);
	};*/
	int cnt=0;
	while(TURN_IN_MAIN>CUR_TURN){
	    if(cnt>0){
		printf("\033[1;37;40m recv verification failed! resend.\033[0m\n");
	    }
	    cnt++;
	    request_verification(car_ID);
	    usleep(100000);
	}

	*safe=CUR_SAFE;
	printf("\033[1;32;40m fetch SAFE=%d MA=%d\033[0m\n",CUR_SAFE,CUR_MA);
	
	return CUR_MA;
}

int get_position(int car_ID){
	char buf[100];
	int cntA=0;
	int cntB=0;
	int ableToBreak=0;
	int rtn=-1;
	while(1){
	    if(read(fd_zigbee,buf,1)==0)continue;
	    if(buf[0]=='A'&&cntA<5&&cntB<5){
		cntB=0;
		cntA++;
	    }else if(buf[0]=='B'&&cntA<5&&cntB<5){
		cntA=0;
		cntB++;
	    }
            else if(cntA>=5){
		cntA=0;
		cntB=0;
                if(buf[0]==car_ID+'0'){
		    read(fd_zigbee,buf,11-5-1);
		    memcpy(&rtn,buf+1,4);
		    if(CUR_TURN==TURN_IN_MAIN)
			break;
		    else{
			ableToBreak=1;
		    }
		}else{
		    read(fd_zigbee,buf,11-5-1);
		}
	    }else if(cntB>=5){
		cntB=0;
		cntA=0;
                if(buf[0]==car_ID+'0'){
		    read(fd_zigbee,buf,15-5-1);
		    int safe=buf[0]-'0';
		    int ma=-1;
		    memcpy(&ma,buf+1,4);
		    int cycleNum=-1;
		    memcpy(&cycleNum,buf+1+4,4);
		    if(TURN_IN_MAIN==cycleNum){
			CUR_SAFE=safe;
			CUR_MA=ma;
			printf("\033[1;34;40m recv cycleNum=%d recv SAFE=%d MA=%d\033[0m\n",cycleNum,CUR_SAFE,CUR_MA);
			CUR_TURN=TURN_IN_MAIN;
			if(ableToBreak)break;
		    }
		    
		}else{
		    read(fd_zigbee,buf,15-5-1);
		}
	    }
	}
	
	while(read(fd_zigbee,buf,90));
	printf("received position = %d\n",rtn);
	
	return rtn;
}

void update_position(int car_ID){
    CUR_POSITION=get_position(car_ID);
}

void update_position_loop(void* p){
    int car_ID=*((int*)p);
    while(1){
	printf("in position\n");
	update_position(car_ID);
    }
}

void init_telecom_device()
{
	fd_grating = open("/dev/mini210-grat", 0);
	if(fd_grating<0){
		printf("open grating failed!\n");
		assert(0);
	}
	zigbee_init();
}

void close_grating()
{
	close(fd_grating);	
}

float get_speed(void)
{
	char buffer[16];
	int len = read(fd_grating, buffer, sizeof(buffer) - 1);
	int value;
	float iSpeed;
	if (len > 0) {
		buffer[len] = '\0';
		sscanf(buffer, "%d", &value);
		iSpeed = 3.14 * 6.8 / 20 * 1000 * 1000 / value;	//n cm/s
	}
	else
	{
	    iSpeed = -1;
	   // printf("fail to get the correct speed\n");
	}
	//printf("Speed %.2fcm/s\n", iSpeed);
	return iSpeed;
}

int zigbee_init(void)
{
	char *Dev = "/dev/ttySAC1";
	int i;
	fd_zigbee = OpenDev(Dev);
	if (fd_zigbee > 0) {
		set_speed(fd_zigbee, 115200);
	}
	else {
		printf("Can't Open Serial Port!\n");
		exit(0);
	}
	if (set_Parity(fd_zigbee, 8, 1, 'N') == FALSE)
	{
		printf("Set parity Error\n");
		exit(1);
	}
	return 0;
}

int zigbee_send_cmd(char* buff, int len)
{
	int ret;
	ret = write(fd_zigbee, buff, len);
	usleep(1000);
	return ret;
}

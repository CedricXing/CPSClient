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

void send_speed(int car_ID){
	char buf[10];
	float speed = get_speed();
	memset(buf, '\0', sizeof(buf));
	int i;
	for(i=0;i<5;i++)buf[i]='X';
	buf[5] = car_ID + '0';
	memcpy(buf + 5 + 1, &speed, 4);
	printf("\033[1;33;40m send speed ID: %d speed: %f\033[0m\n", car_ID, speed);
	zigbee_send_cmd(buf, 5 + 1 + 4);
}

int telecom_main(int car_ID,int *safe) {
	static int TURN_IN_MAIN=-1;
	TURN_IN_MAIN++;
	
	send_speed(car_ID);

	char buf[6];
	int i;
	for(i=0;i<5;i++)buf[i]='S';
	buf[5]=car_ID+'0';

	zigbee_send_cmd(buf,5+1);

	while(TURN_IN_MAIN>CUR_TURN);

	*safe=CUR_SAFE;
	printf("\033[1;32;40m fetch SAFE=%d MA=%d\033[0m\n",CUR_SAFE,CUR_MA);
	
	return CUR_MA;
}

int get_position(int car_ID){
	char buf[100];
	int cntA=0;
	int cntB=0;
	while(1){
	    if(read(fd_zigbee,buf,1)==0)continue;
	    if(buf[0]=='A'){
		cntB=0;
		cntA++;
	    }else if(buf[0]=='B'){
		cntA=0;
		cntB++;
	    }
            else if(cntA>=5){
		cntA=0;
		cntB=0;
                if(buf[0]==car_ID+'0'){
		    read(fd_zigbee,buf,11-5-1);
		    break;
		}else{
		    read(fd_zigbee,buf,11-5-1);
		}
	    }else if(cntB>=5){
		cntB=0;
		cntA=0;
                if(buf[0]==car_ID+'0'){
		    read(fd_zigbee,buf,11-5-1);
		    CUR_SAFE=buf[0]-'0';
		    memcpy(&CUR_MA,buf+1,4);
		    CUR_TURN++;
		    printf("\033[1;34;40m cur_turn=%d recv SAFE=%d MA=%d\033[0m\n",CUR_TURN,CUR_SAFE,CUR_MA);
		}else{
		    read(fd_zigbee,buf,11-5-1);
		}
	    }
	}
	
	int rtn;
	memcpy(&rtn,buf+1,4);
	printf("received position = %d\n",rtn);
	while(read(fd_zigbee,buf,99));
	
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
	rfid_init();
	rfid_open();
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
	printf("Speed %.2fcm/s\n", iSpeed);
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

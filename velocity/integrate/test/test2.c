#include <stdio.h>
#include <unistd.h>
#include <assert.h>
#include <time.h>
#include <math.h>
#include <stdlib.h>
#include "car.h"
#include "rfid.h"
//	time interval
#define TOT_INTERVAL		12
#define CONTACT_INTERVAL	2
#define ADJUST_INTERVAL		1
#define CNT					(TOT_INTERVAL - CONTACT_INTERVAL) / ADJUST_INTERVAL

#define RFID_NUM 126

//	acceleration and speed
#define ACC 14
#define MAX_SPEED 65
#define N_LEVEL 9          
const double V_LEVEL[N_LEVEL]={0, 19, 22, 25, 29, 36, 42, 49, 55};

//	for car driver function
int SPEED_LEVEL = 0;
pthread_mutex_t mutex;

//	3 status:
//	AC -> +1
//	CC -> +1 / 0 / -1
//	EB -> -2

//	calculate ebi
//	input: dis, output: ebi
double calc_ebi(double dis) { 
	return sqrt(2 * ACC * dis); 
}

int min(int a, int b) { return a < b ? a : b; }
int max(int a, int b) { return a > b ? a : b; }

int AC(int cur_lvl) {
	return min(N_LEVEL-1, cur_lvl+1);
}

int EB(int cur_lvl) {
	return max(0, cur_lvl-2);
}

int CC(int cur_lvl) {
	int change = rand() % 3;
	switch (change) {
		case 0: return max(0, cur_lvl-1);
		case 1: return cur_lvl;
		case 2: return min(N_LEVEL-1, cur_lvl+1);
		default: return cur_lvl;
	}
}

int ebi2level(double ebi)
{
//	assert(ebi<=MAX_SPEED&&ebi>=0);
	if(ebi>=52){
		return 8;
	}
	else if(ebi>=45.5){
		return 7;
	}
	else if(ebi>=39){
		return 6;
	}
	else if(ebi>=32.5){
		return 5;
	}
	else if(ebi>=27){
		return 4;
	}
	else if(ebi>=23.5){
		return 3;
	}
	else if(ebi>=20.5){
		return 2;
	}
	else if(ebi>=16){
		return 1;
	}
	return 0;
} 

int car_ID;

int main() {
	printf("input car ID:");
	scanf("%d", &car_ID);
	init_telecom_device();
	
	pthread_t read_speed;
//	pthread_t read_card;
    pthread_mutex_init(&mutex,NULL);
    pthread_create(&read_speed,NULL,(void*)&car_driver,NULL);
//	pthread_create(&read_card,NULL,(void*)&car_rfid,NULL);
	int ebi_lvl = 0, cur_lvl = 0;
	

//	sleep(10);
	cur_lvl = SPEED_LEVEL = 4;
	sleep(5);

	while (1) {
		//	communicate and get updated MA	
		//int cur_id = get_card();
		//if (cur_id >= 20 && cur_id <= 30) {
		//	SPEED_LEVEL = 0;
		//	sleep(1);
		//	break;
		}
		cur_lvl = AC(cur_lvl);
		SPEED_LEVEL = cur_lvl;
		sleep(5);	
	}

	return 0;
}



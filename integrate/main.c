#include "include/common.h"
#include "include/car.h"
#include "include/rfid.h"
     
const double V_LEVEL[N_LEVEL]={0, 19, 22, 25, 29, 36, 42, 49, 55};

//	for car driver function
int SPEED_LEVEL = 0;
pthread_mutex_t mutex;
int CUR_POSITION=0;

//	calculate ebi
//	input: dis, output: ebi
double calc_ebi(double dis) { 
	return sqrt(2 * ACC * dis); 
}

int min(int a, int b) { return a < b ? a : b; }
int max(int a, int b) { return a > b ? a : b; }

//	3 status:
//	AC -> +1
//	CC -> +1 / 0 / -1
//	EB -> -2

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

void update_position_loop(void* p);

int main() {
	printf("input car ID:");
	scanf("%d", &car_ID);
	init_telecom_device();
	
	pthread_t read_speed;
//	pthread_t read_card;
    pthread_mutex_init(&mutex,NULL);
    pthread_create(&read_speed,NULL,(void*)&car_driver,NULL);
//	pthread_create(&read_card,NULL,(void*)&car_rfid,NULL);

	pthread_t pth_position;
	printf("%d",(pthread_create(&pth_position,NULL,(void*)&update_position_loop,&car_ID)));

	

	int ebi_lvl = 0, cur_lvl = 0;
	

	sleep(10);
	cur_lvl = SPEED_LEVEL = 2;
	sleep(5);

	while (1) {
		//	communicate and get updated MA	
		int safe;
		int dest_id = telecom_main(car_ID, &safe);	//	card id where ma ends,  modified
		
		printf( "\033[1;31;40m dest_id=%d \033[0m\n",dest_id ); 
		
		if (!safe) {
			SPEED_LEVEL = 0;
			sleep(1);
			break;
		}
			
		//sleep(CONTACT_INTERVAL);
        
        int i = 0;
		for (; i < CNT; ++i) {
			// int cur_id = get_card();
			// substitude rfid with uwb 
			int cur_id = CUR_POSITION;//get_position(car_ID);
			
			printf("\033[1;31;40m cur_id=%d \033[0m\n",cur_id ); 
			double dis = (dest_id + RFID_NUM - cur_id) % RFID_NUM * 10;
            dis = (dis < MAX_DISTANCE ? dis : MAX_DISTANCE);
			double ebi = calc_ebi(dis);

			if (ebi >= MAX_SPEED) {
				//	AC
				cur_lvl = AC(cur_lvl);
			}
			else {
				ebi_lvl = ebi2level(ebi);

				if (ebi_lvl - cur_lvl > 4) {
					//	AC
					cur_lvl = AC(cur_lvl);
				}
				else if (ebi_lvl - cur_lvl <= 0) {
					//	EB
					cur_lvl = EB(cur_lvl);
				}
				else {
					//	CC
					cur_lvl = CC(cur_lvl);
				}
			}
		
			SPEED_LEVEL = cur_lvl;


			sleep(ADJUST_INTERVAL);
		}
	}

	return 0;
}



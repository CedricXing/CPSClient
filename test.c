#include <stdio.h>
#include <unistd.h>
#include <assert.h>
#include <time.h>
#include <math.h>
#include <stdlib.h>

//	time interval
#define TOT_INTERVAL		12
#define CONTACT_INTERVAL	2
#define ADJUST_INTERVAL		1
#define CNT					(TOT_INTERVAL - CONTACT_INTERVAL) / ADJUST_INTERVAL

#define ACC 14
#define MAX_SPEED 65

//	speed level
#define N_LEVEL 9          
const double V_LEVEL[N_LEVEL]={0, 19, 22, 25, 29, 36, 42, 49, 55};

//	3 status:
//	AC -> +1
//	CC -> +1 / 0 / -1
//	EB -> -2

//	calculate ebi
//	input: dis, output: ebi
inline double calc_ebi(double dis) { 
	return sqrt(2 * ACC * dis); 
}

inline int AC(int cur_lvl) {
	return min(N_LEVEL-1, cur_lvl+1);
}

inline int EB(int cur_lvl) {
	return max(0, cur_lvl-2);
}

inline int CC(int cur_lvl) {
	int change = rand() % 3;
	switch (change) {
		case 0: return max(0, cur_lvl-1);
		case 1: return cur_lvl;
		case 2: return min(N_LEVEL-1, cur_lvl+1);
		default: return cur_lvl;
	}
}

int main() {
	int ebi_lvl = 0, cur_lvl = 0;

	while (1) {
		//	TODO: 
		//	communicate and get updated MA
		
		sleep(CONTACT_INTERVAL);

		for (int i = 0; i < CNT; ++i) {
			//	TODO: API: rfid card -> distance
			//	dis = ...
			double dis = 0;	//	to be modified

			double ebi = calc_ebi(dis);

			if (ebi >= MAX_SPEED) {
				//	AC
				cur_lvl = AC(cur_lvl);
			}
			else {
				//	TODO: API: ebi -> ebi level
				//	evi_lvl = ...

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
		
			//	TODO: change speed according to the computed value
		}
	}

	return 0;
}


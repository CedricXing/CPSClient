#include <stdio.h>
#include <stdlib.h>
#include <windows.h>
#include <math.h>
#include <time.h>
#define PI acos(-1.0)
FILE* stream;
void output(int id, int dist) {
	Sleep(1000);
	stream = fopen("StatusInfo.txt", "w");
	fprintf(stream, "%d %d %d\n", id, dist, 40+rand()%10);
	fprintf(stream, "%d %d %d\n",(id+2)%8,dist,40+rand()%10);
	fclose(stream);
}
int main() {
	srand(time(NULL));
	int i = 0;
	int y = 210, x = 170, r_1 = 55, r_2 = 40, r = (r_1+r_2)>>1, arc = (int)(PI/2*r);
	int moving = 20;
	
	for (; ;) {
		int id, dist;
		for (id = 0, dist = 0; dist <= x; moving = moving + (rand() % 2 ? 1 : -1) * rand() % 5, dist += moving) output(id, dist);
		for (id = 1, dist = 0; dist <= arc; dist += moving) output(id, dist);
		for (id = 2, dist = 0; dist <= y; dist += moving) output(id, dist);			
		for (id = 3, dist = 0; dist <= arc; dist += moving) output(id, dist);			
		for (id = 4, dist = 0; dist <= x; dist += moving) output(id, dist);			
		for (id = 5, dist = 0; dist <= arc; dist += moving) output(id, dist);			
		for (id = 6, dist = 0; dist <= y; dist += moving) output(id, dist);			
		for (id = 7, dist = 0; dist <= arc; dist += moving) output(id, dist);			
	}
	
	return 0;
}
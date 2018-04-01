#include <math.h>
#include "ma.h"

double time = 0;
double ma;

//	changes MA
void signal() {

}

double calc_ebi(double ma) {
	return sqrt(2*A*ma);
}

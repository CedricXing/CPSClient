#ifndef __VELOCITY_H_INCLUDED
#define __VELOCITY_H_INCLUDED

#define N_LEVEL 4
#define EPS_V 5
#define THRE_V EPS_V
#define INT_Q 0.5
#define UPD_T 0.01
#define inf 10000

double calc_ebi(double);
int safe(double, double);
double level(double);
void upd_ret(double, double);
void query(double, double);

#endif

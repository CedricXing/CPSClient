#include "velocity.h"
#include "ma.h"

const double v_std[N_LEVEL] = {0, 14.84, 28.26, 47};
double v_gap[N_LEVEL];
double t_gap[N_LEVEL];

double pos;
int upd_q;
int cnt=0;

//	3 return value
//
//	flag_ret = 0 : no need to change
//			 = 1 : accelerate according to t_ret & r_ret
//			 = -1: decelerate
//
//	n_ret : the number of stages between two states
//	
//	t_ret : timespan of each stage
//			It is guaranteed that ``t_ret[n_ret-1] = inf``
double t_ret[N_LEVEL];
int n_ret;
int flag_ret;

double myabs(double x) { return x < 0 ? -x : x; }

//	initialization
double init_v() {
	for (int i = 1; i < N_LEVEL; ++i) {
		v_gap[i] = v_std[i] - v_std[i-1];
	}
	
	for (int i = 0; i < N_LEVEL; ++i) {
		t_gap[i] = v_gap[i] * UPD_T;
	}

	upd_q = INT_MA / INT_Q;
}

//	parameter: v - current velocity; x - current position
//	function: updates the return value, which indicates the way the velocity changes.
void query(double v, double x) {
	//	dist : 
	//	distance between (current position)[x] and (stopping point)[pos]
	double dist = 0;

	//	cnt == 0 : ma updates
	if (!cnt) {
		pos = x + ma;
		dist = ma;
	}
	else {
		dist = pos - x;
	}
	cnt = (cnt + 1) % upd_q;

	//	calculate V_EBI based on MA-EBI formula
	double v_ebi = calc_ebi(ma);

	upd_ret(v_ebi, v);
}

int safe(double v1, double v2) {
	return myabs(v1-v2) < THRE_V;
}

//	parameter: v - velocity
//	returns: the level where the velocity falls
double level(double v) {
	int lvl = 0;
	for (; lvl < N_LEVEL; ++lvl) {
		if (myabs(v_std[lvl] - v) < EPS_V) {
			break;
		}
	}
	return lvl;
}


void upd_ret(double v_t, double v_s) {
	if (safe(v_t, v_s)) {
		n_ret = 1;
		t_ret[0] = inf;
		flag_ret = 0;
		return;
	}

	int lvl_t = level(v_t), lvl_s = level(v_s);
	
	if (lvl_s < lvl_t) {
		n_ret = lvl_t - lvl_s;

		for (int i = 0; i < n_ret-1; ++i) {
			t_ret[i] = t_gap[lvl_s+i+1];
		}
		t_ret[n_ret-1] = inf;
		
		flag_ret = 1;
	}
	else {
		n_ret = lvl_s - lvl_t;

		for (int i = 0; i < n_ret-1; ++i) {
			t_ret[i] = t_gap[lvl_s-1-i];
		}
		t_ret[n_ret-1] = inf;

		flag_ret = -1;
	}
}

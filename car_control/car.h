#ifndef CAR_H
#define CAR_H

extern int SPEED_LEVEL;
void car_driver(void);
int car_open(void);
/*  
    open the driver devices of the car : motor, trailing and grat
    the value  returned:
    0:successful
    other:failed
*/

int car_close(void);
/*
  same as above
*/ 

void car_control(void);
/*
    
*/

int mgtic_read(void);
/*
    read the mgtic value
*/

int mgtic_set(void);

double get_speed(void);
/*
    read the speed of the car
*/


#endif

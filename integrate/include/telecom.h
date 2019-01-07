#ifndef __TELECOM_H__
#define __TELECOM_H__


#define      TRUE   1
#define      FALSE  0
#define      MAXLEN 20

void init_telecom_device();
float get_speed(void);
int telecom_main(int car_ID,int* safe);
int zigbee_init(void);
int zigbee_send_cmd(char* buff, int len);

#endif
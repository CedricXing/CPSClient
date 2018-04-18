#ifndef __TELECOM_H__
#define __TELECOM_H__

#include "rfid.h"
#include <fcntl.h>
#include <linux/fs.h>
#include <errno.h>
#include <string.h>
#include <termios.h>
#include <time.h>
#include <sys/time.h>
#include <signal.h>
#include <assert.h>

#define      TRUE   1
#define      FALSE  0
#define      MAXLEN 20

void init_telecom_device();
float get_speed(void);
int telecom_main(int car_ID,int* safe);
int zigbee_init(void);
int zigbee_send_cmd(char* buff, int len);

#endif
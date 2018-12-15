#ifndef __ZIGBEE_H__
#define __ZIGBEE_H__

#include "common.h"

#define      TRUE   1
#define      FALSE  0
#define      MAXLEN 20

int OpenDev(char *Dev);
void set_speed(int fd, int speed);
int set_Parity(int fd, int databits, int stopbits, int parity);

#endif

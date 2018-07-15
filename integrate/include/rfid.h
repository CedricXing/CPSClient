#ifndef __RFID_H__
#define __RFID_H__

#include "common.h"

#define      TRUE   1
#define      FALSE  0
#define      MAXLEN 20

extern int fd_rfid;

void car_rfid(void);
unsigned rfid_get_card(void);
int rfid_init(void);
int rfid_open(void);
int OpenDev(char *Dev);
void set_speed(int fd, int speed);
int set_Parity(int fd, int databits, int stopbits, int parity);

#endif

#ifndef __RFID_H__
#define __RFID_H__

#include <stdio.h>
#include <unistd.h>
#include <stdlib.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/ioctl.h>
#include <fcntl.h>
#include <linux/fs.h>
#include <errno.h>
#include <string.h>
#include <termios.h>
#include <time.h>
#include <sys/time.h>
#include <signal.h>

#define      TRUE   1
#define      FALSE  0
#define      MAXLEN 20

extern int fd_rfid;

unsigned get_card(void);
int rfid_init(void);
int rfid_open(void);
int OpenDev(char *Dev);
void set_speed(int fd, int speed);
int set_Parity(int fd, int databits, int stopbits, int parity);

#endif
#ifndef __ZIGBEE_H__
#define __ZIGBEE_H__

#include <fcntl.h>
#include <linux/fs.h>
#include <errno.h>
#include <string.h>
#include <termios.h>
#include <time.h>
#include <sys/time.h>
#include <signal.h>
#include "rfid.h"
#define      TRUE   1
#define      FALSE  0
#define      MAXLEN 20

extern int fd_zigbee;
int zigbee_init(void);
int zigbee_send_cmd(char* buff, int len);

#endif // !__ZIGBEE_H__


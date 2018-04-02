#ifndef __TELECOM_H__
#define __TELECOM_H__

#include "zigbee.h"
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

unsigned telecom_main();

#endif
#include "zigbee.h"

int fd_zigbee = -1;

int zigbee_init(void)
{
	char *Dev = "/dev/ttySAC1";
	int i;
	fd_zigbee = OpenDev(Dev);
	if (fd_zigbee > 0) {
		set_speed(fd_zigbee, 115200);
	}
	else {
		printf("Can't Open Serial Port!\n");
		exit(0);
	}
	if (set_Parity(fd_zigbee, 8, 1, 'N') == FALSE)
	{
		printf("Set parity Error\n");
		exit(1);
	}
	return 0;
}

int zigbee_send_cmd(char* buff, int len)
{
	int ret;
	ret = write(fd_zigbee, buff, len);
	usleep(1000);
	return ret;
}
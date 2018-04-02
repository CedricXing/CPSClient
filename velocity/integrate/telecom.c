#include "telecom.h"

extern int fd_zigbee;
int car_ID=0;
unsigned telecom_main() {
	char buf[30];
	float speed = get_speed();
	unsigned card = get_card();
	//int car_ID = get_ID();
	memset(buf, '\0', sizeof(buf));
	buf[0] = car_ID + '0';
	memcpy(buf + 1, &speed, 4);
	memcpy(buf + 5, &card, 4);
	//	printf("%s\n", buf);
	printf("ID: %d speed: %f location: %d\n", car_ID, speed, card);
	zigbee_send_cmd(buf, 1 + 4 + 4);
	while (read(fd_zigbee, buf, 32) == 0);
	printf("RECV\n");
	//	int i;
	//	for (i = 0; i < 5; i++)printf("%c", buf[i]);
	//	printf("\n");
	unsigned *rtn = (unsigned*)(buf + 1);
	return *rtn;
}
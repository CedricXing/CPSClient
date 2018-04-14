#include "telecom.h"

int fd_zigbee=-1;
int fd_grating=-1;
static int cnt;
int telecom_main(int car_ID) {
	char buf[100];
	float speed = get_speed();
	printf("speed=%f\n",speed);
	unsigned card = get_card();
	//int car_ID = get_ID();
	memset(buf, '\0', sizeof(buf));
	buf[0] = car_ID + '0';
	memcpy(buf + 1, &speed, 4);
	memcpy(buf + 5, &card, 4);
	//	printf("%s\n", buf);
	printf("ID: %d speed: %f location: %d\n", car_ID, speed, card);
	zigbee_send_cmd(buf, 1 + 4 + 4);
	int i,j;
	while (read(fd_zigbee, buf, 99) == 0){
	}
	char tbuf[32];
	for(i=0;i<100;i++){
		if(buf[i]=='a'){
			memcpy(tbuf,buf+i,7);
		}
	}
	int rtn = (tbuf[3]-'0')*100+(tbuf[2]-'0')*10+(tbuf[1]-'0');
	while(read(fd_zigbee,buf,99));
	printf("RECV!!!!!!   %d\n",cnt);
	cnt++;
	//	int i;
	//	for (i = 0; i < 5; i++)printf("%c", buf[i]);
	//	printf("\n");
	return rtn;
}

void init_telecom_device()
{
	fd_grating = open("/dev/mini210-grat", 0);
	if(fd_grating<0){
		printf("open grating failed!\n");
		assert(0);
	}
	rfid_init();
	rfid_open();
	zigbee_init();
}

void close_grating()
{
	close(fd_grating);	
}

float get_speed(void)
{
	char buffer[16];
	int len = read(fd_grating, buffer, sizeof(buffer) - 1);
	int value;
	float iSpeed;
	if (len > 0) {
		buffer[len] = '\0';
		sscanf(buffer, "%d", &value);
		iSpeed = 3.14 * 6.8 / 20 * 1000 * 1000 / value;	//n cm/s
	}
	else
	{
	    iSpeed = 0;
	    printf("fail to get the correct speed\n");
	}
	printf("Speed %.2fcm/s\n", iSpeed);
	return iSpeed;
}

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

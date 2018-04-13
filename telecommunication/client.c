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

#define     TRUE   1
#define     FALSE  0
#define     MAXLEN 20

static int zigbee_fd;

static int speed_arr[] = { B230400, B115200, B57600, B38400, B19200, B9600, B4800, B2400, B1200, B300,
B38400, B19200, B9600, B4800, B2400, B1200, B300 };

static int name_arr[] = { 230400, 115200, 57600, 38400, 19200, 9600, 4800, 2400, 1200, 300,
38400, 19200, 9600, 4800, 2400, 1200, 300 };

char transBuffer[40];

static void set_speed(int fd, int speed)
{
	int i;
	int status;
	struct termios Opt;

	tcgetattr(fd, &Opt);
	for (i = 0; i < sizeof(speed_arr) / sizeof(int); i++)
	{
		if (speed == name_arr[i])
		{
			tcflush(fd, TCIOFLUSH);
			cfsetispeed(&Opt, speed_arr[i]);
			cfsetospeed(&Opt, speed_arr[i]);
			status = tcsetattr(fd, TCSANOW, &Opt);
			if (status != 0)
				perror("tcsetattr fd1");
			return;
		}
		tcflush(fd, TCIOFLUSH);
	}
}

static int set_Parity(int fd, int databits, int stopbits, int parity)
{
	struct termios options;
	if (tcgetattr(fd, &options) != 0)
	{
		perror("SetupSerial 1");
		return(FALSE);
	}

	options.c_cflag &= ~CSIZE;

	switch (databits)
	{
	case 7:
		options.c_cflag |= CS7;
		break;
	case 8:
		options.c_cflag |= CS8;
		break;
	default:
		fprintf(stderr, "Unsupported data size\n");
		return(FALSE);
	}

	switch (parity)
	{
	case 'n':
	case 'N':
		options.c_cflag &= ~PARENB;
		options.c_iflag &= ~INPCK;
		break;
	case 'o':
	case 'O':
		options.c_cflag |= (PARODD | PARENB);
		options.c_iflag |= INPCK;
		break;
	case 'e':
	case 'E':
		options.c_cflag |= PARENB;
		options.c_cflag &= ~PARODD;
		options.c_iflag |= INPCK;
		break;
	case 'S':
	case 's':  /*as no parity*/
		options.c_cflag &= ~PARENB;
		options.c_cflag &= ~CSTOPB;
		break;
	default:
		fprintf(stderr, "Unsupported parity\n");
		return(FALSE);
	}

	switch (stopbits)
	{
	case 1:
		options.c_cflag &= ~CSTOPB;
		break;
	case 2:
		options.c_cflag |= CSTOPB;
		break;
	default:
		fprintf(stderr, "Unsupported stop bits\n");
		return(FALSE);
	}
	if (parity != 'n'&& parity != 'N')
		options.c_iflag |= INPCK;

	options.c_cflag &= ~CRTSCTS;
	options.c_iflag &= ~IXOFF;
	options.c_iflag &= ~IXON;
	if (parity != 'n'&& parity != 'N')
		options.c_iflag |= INPCK;
	tcflush(fd, TCIOFLUSH);

	//	options.c_iflag |= IGNPAR|ICRNL;
	options.c_lflag &= ~(ICANON | ECHO | ECHOE | ISIG);
	//	options.c_oflag |= OPOST; 
	options.c_oflag &= ~OPOST;
	options.c_iflag &= ~(ICRNL | IGNCR);
	//	options.c_iflag &= ~(IXON|IXOFF|IXANY);
	options.c_cc[VTIME] = 0;        //150;                  //15 seconds
	options.c_cc[VMIN] = 0;

	tcflush(fd, TCIFLUSH);
	if (tcsetattr(fd, TCSANOW, &options) != 0)
	{
		perror("SetupSerial 1");
		return(FALSE);
	}
	return(TRUE);
}

static int OpenDev(char *Dev)
{
	int fd = open(Dev, O_RDWR);
	if (-1 == fd)
	{
		perror("Can't Open Serial Port");
		return -1;
	}
	else {
		return fd;
	}
}

int zigbee_init(void)
{
	char *Dev = "/dev/ttySAC1";

	int i;

	zigbee_fd = OpenDev(Dev);

	if (zigbee_fd > 0) {
		set_speed(zigbee_fd, 115200);
	}
	else {
		printf("Can't Open Serial Port!\n");
		exit(0);
	}
	if (set_Parity(zigbee_fd, 8, 1, 'N') == FALSE)
	{
		printf("Set parity Error\n");
		exit(1);
	}
	return 0;
}

/*
	接收指令
*/
int zigbee_get_cmd(void)
{
	unsigned char buf[32];
	int nread = 0, cmd = -1;

	memset(buf, 0, 32);
	nread = read(zigbee_fd, buf, 32);
	if (nread > 0)
	{
		printf("zigbee: %x %x %x %x %x %x\n", buf[0], buf[1], buf[2], buf[3], buf[4], buf[5]);
		printf("\n");
	}

	if (nread >= 6) {
		if (buf[0] == 0x02 && buf[1] == 0x04 && buf[2] == 0x57 && buf[3] == 0xa8)
		{
			cmd = buf[4];
			return cmd;
		}
	}
	return -1;
}

/*
	通过zigbee广播发送字符串
*/
int zigbee_send_cmd(char* buff, int len)
{
	int ret;

	ret = write(zigbee_fd, buff, len);
	usleep(1000);
	return ret;
}

int zigbee_close(void)
{
	close(zigbee_fd);
	return 0;
}

int fdMotor;
int fdMagtic;
int fdGrat;
int rfid_fd;
int ID;

int car_open(void)
{
	rfid_init();
	rfid_open();
	fdMotor = open("/dev/mini210-motors", 0);
	fdMagtic = open("/dev/mini210-mgtics", 0);
	fdGrat=open("/dev/mini210-grat", 0);
	if (fdMotor < 0) {
		printf("open car device error\n");
		exit(1);
	}
	printf("open motor id:%d\n", fdMotor);

	zigbee_init();
	return 0;
}

int car_close(void)
{
	close(fdMagtic);
	close(fdMotor);
	close(rfid_fd);
	return 0;
}

int mgtic_read(void)
{
	int value = -1;
	char buffer[16];

	int len = read(fdMagtic, buffer, sizeof(buffer) - 1);
	if (len > 0) {
		buffer[len] = '\0';
		sscanf(buffer, "%d", &value);
	}
	else {
		printf("read mgtic device error");
		close(fdMagtic);
		return -1;
	}
	return value;
}

/*
bit 0,1	FL	(10前进, 01后退, 00停止)
2,3	FR	(01前进, 10后退, 00停止)
4,5	BL	(10前进, 01后退, 00停止)
6,7	BR	(01前进, 10后退, 00停止)
前后左轮与右轮电机方向是相反的.
*/

int motor_set(int value)
{
	switch (value)
	{
	case 0:
		ioctl(fdMotor, 0, 0x99);	//前行
		break;
	case 1:
		ioctl(fdMotor, 0, 0x66);	//后退
		break;
	case 2:
		ioctl(fdMotor, 0, 0xaa);	//逆转
		break;
	case 3:
		ioctl(fdMotor, 0, 0x55);	//顺转
		break;
	case 4:
		ioctl(fdMotor, 0, 0x88);	//左转
		break;
	case 5:
		ioctl(fdMotor, 0, 0x44);	//右转
		break;
	case 6:
		ioctl(fdMotor, 0, 0x00);	//停止
		break;
	default:
		printf("Cmd error\n");
		break;
	}
	return 0;
}

float get_speed() {
	char speed_buffer[20];
	int value;
	int len = read(fdGrat, speed_buffer, sizeof(speed_buffer) - 1);
	if (len > 0) {
		speed_buffer[len] = '\0';
		sscanf(speed_buffer, "%d", &value);
		return 3.14 * 6.8 / 20 * 1000 * 1000 / value;	//n cm/s
	}
	else {
		return 0;
	}
}

int get_location() {
	return rfid_get_card();
}

int get_ID() {
	return ID;
}

int rfid_get_card(void)
{
	int nread, i, num;
	unsigned char buf[16];

	memset(buf, 0, 16);
	buf[0] = 0xaa;
	buf[1] = 0xbb;
	buf[2] = 0x02;
	buf[3] = 0x20;
	buf[4] ^= buf[2] ^ buf[3];
	write(rfid_fd, buf, 5);
	usleep(50 * 1000);
	nread = read(rfid_fd, buf, 16);
	/*
	printf("rfid read:");
	for(i=0; i< nread; i++)
	{
	printf("%x ", buf[i]);
	}
	printf("\n");
	*/
	if (nread >= 0x9)
	{
		if (buf[0] == 0xaa && buf[1] == 0xbb &&
			buf[2] == 0x06 && buf[3] == 0x20)
		{
			//获得卡号 buf[4],buf[5],buf[6],buf[7];
			memcpy(&num, &buf[4], 4);
			return num;
		}
	}
	return 0;
}

int rfid_init(void)
{
	char *Dev = "/dev/ttySAC3";
	int i;

	rfid_fd = OpenDev(Dev);

	if (rfid_fd > 0) {
		set_speed(rfid_fd, 19200);
	}
	else {
		printf("Can't Open Serial Port!\n");
		exit(0);
	}
	if (set_Parity(rfid_fd, 8, 1, 'N') == FALSE)
	{
		printf("Set parity Error\n");
		exit(1);
	}
	return 0;
}

int rfid_open(void)
{
	int ret, i;
	unsigned char buf[16];

	memset(buf, 0, 16);
	buf[0] = 0xaa;
	buf[1] = 0xbb;
	buf[2] = 0x03;
	buf[3] = 0x13;
	buf[4] = 0x01;
	buf[5] ^= buf[2] ^ buf[3] ^ buf[4];
	write(rfid_fd, buf, 6);
	usleep(50 * 1000);
	ret = read(rfid_fd, buf, 16);
	/*
	printf("rfid read:");
	for(i=0; i< ret; i++)
	{
	printf("%x ", buf[i]);
	}
	printf("\n");
	*/
	if (ret >= 0x5)
	{
		if (buf[0] == 0xaa && buf[1] == 0xbb && buf[2] == 0x02 && buf[3] == 0x13)
			return 0;
	}
	return -1;
}


int main(int argc, char* argv[])
{
	printf("input car ID:\n");
	scanf("%d", &ID);

	int cmd;
	char buf[MAXLEN];
	car_open();

	while (1) {
		int mgtic = mgtic_read();

		if (mgtic == 0) {
			motor_set(6);
		}
		if (mgtic & (1 << 2)) {
			motor_set(0);
		}
		if (mgtic & (1 << 0)) {
			motor_set(2);
		}
		if (mgtic & (1 << 4)) {
			motor_set(3);
		}
		if (mgtic & (1 << 1)) {
			motor_set(4);
		}
		if (mgtic & (1 << 3)) {
			motor_set(5);
		}
		/*
			接收指令，指令解析与处理待完成。
		*/
		//cmd = zigbee_get_cmd();    
		//printf("cmd %d\n", cmd);
	//	motor_set(cmd);


		/*
			数据包格式：
			以字符为单位；
			第0个字符是'0'+车编号；
			第1-4个是速度，为float型的内存表示；
			第5-8个是位置，为RFID卡的UID编号；
			余下字符位置备用。
		*/
		float speed = get_speed();
		int location = get_location();
		int car_ID = get_ID();
		memset(buf, '\0', sizeof(buf));
		buf[0] = car_ID + '0';
		memcpy(buf + 1, &speed, 4);
		memcpy(buf + 5, &location, 4);
	//	printf("%s\n", buf);
		printf("ID: %d speed: %f location: %d\n", car_ID, speed, location);
		zigbee_send_cmd(buf, 1+4+4);
		int lenRead = read(zigbee_fd, buf, 32);
		printf("RECV\n");
		int i;
			for (i = 0; i < 5; i++)printf("%c", buf[i]);
		printf("\n");
	}
	car_close();
	return 0;
}


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
#include  <termios.h>   

#define      TRUE   1
#define      FALSE  0
#define      MAXLEN 20

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
	����ָ��
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
	ͨ��zigbee�㲥�����ַ���
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

int car_open(void)
{
	fdMotor = open("/dev/mini210-motors", 0);

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
	close(fdMotor);
	return 0;
}
/*
bit 0,1	FL	(10ǰ��, 01����, 00ֹͣ)
2,3	FR	(01ǰ��, 10����, 00ֹͣ)
4,5	BL	(10ǰ��, 01����, 00ֹͣ)
6,7	BR	(01ǰ��, 10����, 00ֹͣ)
ǰ�����������ֵ���������෴��.
*/
int motor_set(int value)
{
	switch (value)
	{
	case 0:
		ioctl(fdMotor, 0, 0x99);	//ǰ��
		usleep(100 * 1000);
		break;
	case 1:
		ioctl(fdMotor, 0, 0x66);	//����
		sleep(1);
		break;
	case 2:
		ioctl(fdMotor, 0, 0xaa);	//��ת
		sleep(1); usleep(500 * 1000);
		break;
	case 3:
		ioctl(fdMotor, 0, 0x55);	//˳ת
		sleep(1); usleep(500 * 1000);
		break;
	case 4:
		ioctl(fdMotor, 0, 0x88);	//��ת
		sleep(1); usleep(500 * 1000);
		break;
	case 5:
		ioctl(fdMotor, 0, 0x44);	//��ת
		sleep(1); usleep(500 * 1000);
		break;
	case 6:
		ioctl(fdMotor, 0, 0x00);	//ֹͣ
		usleep(100 * 1000);
	default:
		printf("Cmd error\n");
		break;
	}
	return 0;
}

float get_speed() {
	/*
		TODO: return speed 
	*/
	return 1.1;
}

int get_location() {
	/*
		TODO: return rfid UID
	*/
	return 1;
}

int get_ID() {
	/*
		TODO: return car ID;
	*/
	return 0;
}

int main(int argc, char* argv[])
{
	int cmd;
	char buf[MAXLEN];
	car_open();

	while (1) {
		/*
			����ָ�ָ������봦�����ɡ�
		*/
		cmd = zigbee_get_cmd();    
		printf("cmd %d\n", cmd);
	//	motor_set(cmd);


		/*
			���ݰ���ʽ��
			���ַ�Ϊ��λ��
			��0���ַ���'0'+����ţ�
			��1-4�����ٶȣ�Ϊfloat�͵��ڴ��ʾ��
			��5-8����λ�ã�ΪRFID����UID��ţ�
			�����ַ�λ�ñ��á�
		*/
		float speed = get_speed();
		int location = get_location();
		int car_ID = get_ID();
		memset(buf, 0, sizeof(buf));
		buf[0] = car_ID + '0';
		memcpy(buf + 1, &speed, 4);
		memcpy(buf + 5, &location, 4);

		zigbee_send_cmd(buf, strlen(buf));

		sleep(2);
	}
	car_close();
	return 0;
}


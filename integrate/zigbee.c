#include "include/zigbee.h"

static int speed_arr[] = { B230400, B115200, B57600, B38400, B19200, B9600, B4800, B2400, B1200, B300,
B38400, B19200, B9600, B4800, B2400, B1200, B300 };

static int name_arr[] = { 230400, 115200, 57600, 38400, 19200, 9600, 4800, 2400, 1200, 300,
38400, 19200, 9600, 4800, 2400, 1200, 300 };

int fd_rfid=-1;
int pre_id=0;

int OpenDev(char *Dev)
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

void set_speed(int fd, int speed)
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

int set_Parity(int fd, int databits, int stopbits, int parity)
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



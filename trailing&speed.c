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

int fd_magtic=-1, fd_motor=-1, fd_grating=-1;
int SPEED_LEVEL = 0;
//打开磁感应和电机设备
int car_open(void)
{
  fd_magtic = open("/dev/mini210-mgtics", 0);
  fd_motor = open("/dev/mini210-motors", 0);
  fd_grating = open("/dev/mini210-grat", 0);

  if((fd_magtic < 0)||(fd_motor < 0)||fd_grating<=0) {
    printf("open car device error\n");
    exit(1);
  }
  printf("open magnetics id:%d\n", fd_magtic);
  printf("open motor id:%d\n", fd_motor);
  printf("open grating id:%d\n", fd_grating);

  return 0;
}

int car_close(void)
{
  close(fd_magtic);
  close(fd_motor);
  close(fd_grating)

  return 0;
}

//读取磁感应检测值
int mgtic_read(void)
{
  int value = -1;
  char buffer[16];

  int len = read(fd_magtic, buffer, sizeof(buffer) -1);
  if (len > 0) {
	buffer[len] = '\0';
	sscanf(buffer, "%d", &value);
  } else {
	printf("read mgtic device error");
 	close(fd_magtic);
	return -1;
  }
  return value;
}

//用于获得小车的速度
//fd_grating = open("/dev/mini210-grat", 0);
//读取成功时success，否则false
double get_speed(int fd_grating, bool& success)
{
	char buffer[16];
	int len = read(fd_grating, buffer, sizeof(buffer) - 1);
	int value;
	double iSpeed;
	if (len > 0) {
		success = true;
		buffer[len] = '\0';
		sscanf(buffer, "%d", &value);
		iSpeed = 3.14 * 6.8 / 20 * 1000 * 1000 / value;	//n cm/s
	}
	else
	{
		success = false;
	}
	printf("Speed %.2fcm/s\n", iSpeed);
	return iSpeed;
}

//改变小车速度档位
void  set_speed_level(int speed_level)
{
	SPEED_LEVEL = speed_level;
}

//电机控制函数
void motor_control()
{
	while (1)
	{
		unsigned j = 0;
		while (1)
		{
			int speed_level = SPEED_LEVEL * 2 + 1;
			switch (speed_level)
			{
			case 0:
				ioctl(fd_motor, 0, 0x00);	//刹车
				break;
			default:
			{
				j = 0;
				int mgtic = mgtic_read();
				int motor_level = 0x00;
				if (mgtic == 0) {
					motor_level = 0x00;	//刹车
				}
				else if (mgtic & (1 << 2)) {
					motor_level = 0x99;	//前行
				}
				else if (mgtic & (1 << 0)) {
					motor_level = 0xaa;	//顺时针转动
				}
				else if (mgtic & (1 << 4)) {
					motor_level = 0x55;	//逆时针
				}
				else if (mgtic & (1 << 1)) {
					motor_level = 0x88;	//左转
				}
				else if (mgtic & (1 << 3)) {
					motor_level = 0x11;	//右转
				}
				else {
					motor_level = 0x00;
				}
				while (1)
				{
					j++;
					if (j % 10 <= speed_level)
					{      
						ioctl(fd_motor, 0, motor_level);//按照相应的速度
					}
					else
					{
						ioctl(fd_motor, 0, 0x00);
					}
					if (speed_level != SPEED_LEVEL * 2 + 1||mgtic!=mgtic_read())//检测到全局变量的改变，重新读取新的速度
					{
						break;
					}
				}
			}
			}
		}
	}
}

int main()
{
	 printf("press Ctrl-C to stop\n");
	 car_open();
	 motor_control();
	 car_close();
	 return 0;
}

//int main(int argc, char* argv[])
//{
//  int ret,ctr=0;
//  int mgtic;
//
//  printf("press Ctrl-C to stop\n");
//  car_open();
//  sleep(5);
//
//  while(1){
//    puts("\033[2J");
//		//根据磁感应状态控制电机运行状态
//			mgtic = mgtic_read();
//
//			if(mgtic == 0){
//				motor_set(6);
//				continue;
//			}
//			if(mgtic & (1<<2)) {
//				motor_set(0);
//				continue;
//			}
//			if(mgtic & (1<<0)) {
//				motor_set(2);
//				continue;
//			}
//			if(mgtic & (1<<4)) {
//				motor_set(3);
//				continue;
//			}
//			if(mgtic & (1<<1)) {
//				motor_set(4);
//				continue;
//			}
//			if(mgtic & (1<<3)) {
//				motor_set(5);
//				continue;
//			}
//  }
//  car_close();
//  return 0;
//}

//电机控制函数
//int motor_set(int value)
//{
//    switch(value)
//    {
//        case 0:
//            ioctl(fd_motor,0,0x99);	//前行
//            usleep(50*1000);
//            break;
//        case 1:
//	        ioctl(fd_motor,0,0x66);	//后退
//            break;
//        case 2:
//            ioctl(fd_motor,0,0xaa);	//顺时针转动
//            usleep(50*1000);
//            break;
//        case 3:
//        	ioctl(fd_motor,0,0x55);	//逆转
//        	usleep(50*1000);
//            break;
//        case 4:
//        	ioctl(fd_motor,0,0x88);	//左转
//	        usleep(100*1000);
//            break;
//        case 5:
//        	ioctl(fd_motor,0,0x11);	//右转
//        	usleep(100*1000);
//            break;
//        case 6:
//            ioctl(fd_motor,0,0x00);	//刹车
//            usleep(100*1000);
//        default:
//            printf("Cmd error\n");
//            break;
//    }
//    return 0;
//}
//主函数
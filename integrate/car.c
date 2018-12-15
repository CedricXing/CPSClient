#include "include/common.h"
#include "include/car.h"

static int fd_magtic=-1,fd_motor=-1;

extern int car_ID;

int car_open(void)
{
  fd_magtic = open("/dev/mini210-mgtics", 0);
  fd_motor = open("/dev/mini210-motors", 0);
  
  if((fd_magtic < 0)||(fd_motor < 0)){
    printf("open car device error\n");
    exit(1);
  }
  
  printf("open magnetics id:%d\n", fd_magtic);
  printf("open motor id:%d\n", fd_motor);
  
  return 0;
}

int car_close(void)
{
  close(fd_magtic);
  close(fd_motor);
//  close(fd_grating);
  
  return 0;
}

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

int mgtic_set(void)
{
    int mgtic = mgtic_read();
    int motor_level = 0x00;
	if (mgtic == 0) {
		motor_level = 0x99;	//前行    !
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
		motor_level = 0x99;//   !
	}
	return motor_level;
}

/*float get_speed(void)
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
	    iSpeed = -1;
	    printf("fail to get the correct speed\n");
	}
	printf("Speed %.2fcm/s\n", iSpeed);
	return iSpeed;
}
*/
void car_control(void)
{
    int level=0;
    int j=0;
    int motor_state=0x99;
	int k = 0;
    //bool flag=false;
    
    while(1)
    {
		//send_speed(car_ID);
		if (level != SPEED_LEVEL)
		{
			level = SPEED_LEVEL;
			//printf("speed level has changed to %d \n", level);
			//flag=true;    

			if (level == 0)
			{
				k = -16;
			}
			else
			{
				k = level;
			}
		}
		motor_state=mgtic_set();
		j++;
		if (j % 24 <= k+15 )    //
		{      //前行
		    ioctl(fd_motor, 0, motor_state);
		    send_speed(car_ID);
		}
		else
		{ 
			ioctl(fd_motor, 0, 0x00); 
		}
		//if(flag)
		//{
		  //  get_speed();
		    //flag=false;
		//}
    }
    return ;
}

void car_driver(void)
{
	car_open();
	car_control();
	car_close();
}

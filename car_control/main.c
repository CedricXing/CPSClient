/*************************************

NAME:pthread.c
COPYRIGHT:www.cvtech.com.cn

*************************************/

#include<stddef.h>
#include<stdio.h>
#include<unistd.h>
#include<pthread.h>
#include<stdlib.h>
#include<errno.h>
//#include<time.h>
#include"car.h"
void speed_control_function(void);
void speed_driver_function(void);
int SPEED_LEVEL=7;

double INTERVAL=0.5;

pthread_mutex_t mutex;

int main()
{
    pthread_t reader;
    pthread_mutex_init(&mutex,NULL);
    pthread_create(&reader,NULL,(void*)&speed_driver_function,NULL);
    speed_control_function();
	return 0;
}

void speed_driver_function(void)
{
    car_open();
    car_control();
    car_close();
}

void set_speed(void)
{
    if (SPEED_LEVEL==9)
    {
        SPEED_LEVEL=0;
    }
    else 
    {
        SPEED_LEVEL++;
    }
}

void speed_control_function(void)
{   
	while (1) {
		clock_t timer_begin = clock();
		clock_t timer_end = 0;
		while (1)
		{
			timer_end = clock();
			// 0.5 second has passed
			if ((double)(timer_end - timer_begin) / (CLOCKS_PER_SEC) >= INTERVAL)
			{
				printf("0.5 second has passed\n ");
				// change speed level
				set_speed();
				timer_begin = clock();
				timer_end = clock();
				break;
			}
		}
	}
}

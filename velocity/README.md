## 2018.3.27 - raw

**尚未测试**

1. 每隔5s改变一次ma，每隔0.5s算一次当前的ebi并且给出速度曲线. 尚未采用多线程，目前期望的时间的控制/同步需要依靠main函数.

2. 因为速度曲线更新很快，所以揣测可以依赖速度曲线的更新达到预期效果，因而**尚未**单独去写制动的情况，但也可能导致边界的问题，暂时未知.

3. ebi的计算公式以及出现的所有参数都有待确定.

## 2018.4.2 - integrate

1. gcc -lm -o test test.c

2.
> TODO:
>- 通信获取ma
>- 当ebi合理时，根据ebi值获得ebi所在的lvl
>- 根据rfid卡片得到当前位置
>- 根据计算出的速度 在物理上 实际调整小车速度

3. todo的1234已完成

4. arm-linux-gcc -lm -lpthread -o test rfid.c zigbee.c telecom.c car.c test.c 




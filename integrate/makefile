src = rfid.c telecom.c car.c main.c trie.c
target = control

CC = arm-linux-gcc
ARG = -lm -lpthread -o

$(target):$(src)
	$(CC) $(ARG) $(target) $(src)

.PHONY:clean
clean:
	-rm $(target)

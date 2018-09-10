src = rfid.c telecom.c car.c main.c trie.c
target = control

CC = arm-linux-gcc

$(target):$(src)
	$(CC) -lm -lpthread -o $(target) $(src)

.PHONY:clean
clean:
	-rm $(target)

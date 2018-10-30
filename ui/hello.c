#include <stdio.h>
#include <stdlib.h>
#include <windows.h>
int main() {
	int i = 0;
	for (; i < 100; ++i) {
		Sleep(1000);
		FILE* stream = fopen("hello.txt", "w");
		fprintf(stream, "Hello, person %d\n", i);
		fclose(stream);
	}
	return 0;
}
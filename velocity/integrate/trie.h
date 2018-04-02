#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#define SIZE 256
#define MAXM 1000
struct Trie {
	int id[MAXM];
	int son[MAXM][SIZE];
	int tot;
};
typedef struct Trie Trie;

void int2char(unsigned int x, unsigned char* buf) {
<<<<<<< HEAD
	int i;
	for (i = 0; i < 4 ; ++i) {
=======
    int i = 0;
	for (; i < 4 ; ++i) {
>>>>>>> 9267393a24f29a306a9bc5d8ab978b97cca2b731
		buf[i] = x % 256;
		x /= 256;
	}
}

void insert(Trie* trie, unsigned char* buf, int id) {
	int p = 0;
<<<<<<< HEAD
	int i;
	for (i = 0; i < 4; ++i) {
=======
	int i = 0;
	for (; i < 4; ++i) {
>>>>>>> 9267393a24f29a306a9bc5d8ab978b97cca2b731
		if (trie->son[p][buf[i]] == -1) {
			trie->id[++trie->tot] = 0;
			memset(trie->son[trie->tot], -1, SIZE*sizeof(int));
			trie->son[p][buf[i]] = trie->tot;
		}	
		p = trie->son[p][buf[i]];
	}
	trie->id[p] = id;
}

void init(Trie* trie, const char* filename) {
	trie->id[trie->tot = 0] = -1;
	memset(trie->son[0], -1, SIZE*sizeof(int));

	FILE* file = fopen(filename, "r");
<<<<<<< HEAD
	int i;
	for (i = 0; i < 126; ++i) {
=======
	int i = 0;
	for (; i < 126; ++i) {
>>>>>>> 9267393a24f29a306a9bc5d8ab978b97cca2b731
		unsigned int cid;
		fscanf(file, "%u", &cid);
		unsigned char* buf = (char*)malloc(4*sizeof(char));	
		int2char(cid, buf);
		insert(trie, buf, i);
	}
}

int query(Trie* trie, int cid) {
	int p = 0;
	unsigned char* buf = (char*)malloc(4*sizeof(char));
	int2char(cid, buf);
<<<<<<< HEAD
	int i;
	for (i = 0; i < 4; ++i) {
=======
	int i = 0;
	for (; i < 4; ++i) {
>>>>>>> 9267393a24f29a306a9bc5d8ab978b97cca2b731
		p = trie->son[p][buf[i]];
	}
	return trie->id[p];
}

void test(Trie* trie, const char* filename) {
	FILE* file = fopen(filename, "r");
<<<<<<< HEAD
	int i;
	for (i = 0; i < 126; ++i) {
=======
	int i = 0;
	for (; i < 126; ++i) {
>>>>>>> 9267393a24f29a306a9bc5d8ab978b97cca2b731
		unsigned int cid;
		fscanf(file, "%u", &cid);
		printf("%u\t->\t%d\n", cid, query(trie, cid));
	}
}


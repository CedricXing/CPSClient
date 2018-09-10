#ifndef __TRIE_H__
#define __TRIE_H__

#define SIZE 256
#define MAXM 1000

struct Trie {
	int id[MAXM];
	int son[MAXM][SIZE];
	int tot;
};
typedef struct Trie Trie;

void int2char(unsigned int x, unsigned char* buf);
void insert(Trie* trie, unsigned char* buf, int id);
void init(Trie* trie, const char* filename);
int query(Trie* trie, int cid);

//test function
void test(Trie* trie, const char* filename);

#endif
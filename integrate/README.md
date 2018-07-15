## 2018.7.15重新组织了文件结构

#### 1.删除了velocity/integrate，在根目录下重新创建了integrate

#### 2.在integrate中：

> 文件变动：
>
> * 新增了include头文件夹存放头文件
> * 新增makefile
> * 新增trie.c，修改原来的trie.h
> * 原来的test.c更名为main.c
> * 新增了test文件夹用以存储新的测试文件
> * 新增了common.h头文件包含系统头文件

> makefile使用：
>
> * 支持 "make" 和 "make clean"两条指令
> * 如创建了新的 "your_main.c" 来测试新的功能，修改makefile中的"SRC"变量即可。(测试完记得将测试文件移入test文件夹，保持文件结构不变)

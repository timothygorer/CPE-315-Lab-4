mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
1	 j	 empty	 empty	    empty

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
2	 squash	 j	 empty	    empty

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
3	 addi	 squash	 j	    empty

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
4	 addi	 addi	 squash	    j

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
5	 beq	 addi	 addi	    squash

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
6	 addi	 beq	 addi	    addi

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
7	 beq	 addi	 beq	    addi

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
8	 lw	 beq	 addi	    beq

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
9	 lw	 lw	 beq	    addi

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
10	 squash	 squash	 squash	    beq

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
11	 add	 squash	 squash	    squash

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
12	 add	 add	 squash	    squash

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
13	 add	 add	 add	    squash

mips> r

Program complete
CPI = 1.727	 Cycles = 19	 Instructions = 11
mips> q

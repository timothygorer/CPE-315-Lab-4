mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
1	 lw	 empty	 empty	    empty

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
2	 add	 lw	 empty	    empty

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
2	 add	 stall	 lw	    empty

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
3	 lw	 add	 stall	    lw

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
4	 add	 lw	 add	    stall

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
4	 add	 stall	 lw	    add

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
5	 lw	 add	 stall	    lw

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
6	 add	 lw	 add	    stall

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
7	 lw	 add	 lw	    add

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
8	 sub	 lw	 add	    lw

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
8	 sub	 stall	 lw	    add

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
9	 lw	 sub	 stall	    lw

mips> s

pc	 if/id	 id/exe	 exe/mem  mem/wb
10	 sub	 lw	 sub	    stall

mips> r

Program complete
CPI = 1.533	 Cycles = 46	 Instructions = 30
mips> q

make lab4
java lab4 lab4_test1.asm lab4_test1.script > lab4_test1.pls
java lab4 lab4_fib10.asm lab4_fib10.script > lab4_fib10.pls
java lab4 lab4_fib20.asm lab4_fib20.script > lab4_fib20.pls
java lab4 lab4_test2.asm lab4_test2.script > lab4_test2.pls
diff -w -B -q lab4_test1.pls lab4_test1.output
diff -w -B -q lab4_fib20.pls lab4_fib20.output
diff -w -B -q lab4_fib10.pls lab4_fib10.output
diff -w -B -q lab4_test2.pls lab4_test2.output

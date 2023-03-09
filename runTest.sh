echo code\ now\ compiling\ and\ will\ execute\ momentarily
echo
for y in $(ls -1 | grep "_test") 
    do
        javac -cp ".;hamcrest-core-1.3.jar;junit-4.13.2.jar;json-20220924.jar" $y/*.java
    done
for x in $(ls -1 | grep "_test" | ls -1 */*.class | sed 's/.class//' | grep "Test_") 
    do 
        java -classpath ".;json-20220924.jar;hamcrest-core-1.3.jar;junit-4.13.2.jar" $x 
        echo
    done
echo finished,\ hit\ enter\ to\ exit
read
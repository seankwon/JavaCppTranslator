define TESTS 
	java Sentinel tests/Test001.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test002.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test003.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test004.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test005.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test006.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test007.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test008.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test009.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test010.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test011.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test018.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test019.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test020.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test021.java
	g++ main.cc && ./a.out 
	java Sentinel tests/Test022.java
	g++ main.cc && ./a.out 
	java Sentinel tests/Test023.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test024.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test025.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test032.java
	g++ main.cc && ./a.out 
	java Sentinel tests/Test033.java
	g++ main.cc && ./a.out 
	java Sentinel tests/Test034.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test035.java
	g++ main.cc && ./a.out 
	"Done." - Romeo
endef

define EXCEPTIONS
	java Sentinel tests/Test014.java
	g++ main.cc && ./a.out
endef

clean:
	rm *.class

program:
	javac Sentinel.java

all:
	javac *.java

run:
	$(TESTS)

exceptions:
	$(EXCEPTIONS)

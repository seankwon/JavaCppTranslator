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
	java Sentinel tests/Test009.java
	g++ main.cc && ./a.out
	java Sentinel tests/Test019.java
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

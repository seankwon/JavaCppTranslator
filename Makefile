clean:
	rm *.class

program:
	javac Sentinel.java

all:
	javac *.java

compile:
	g++ main.cc

run:
	./a.out

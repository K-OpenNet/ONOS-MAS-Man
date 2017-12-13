rm -rf out
mkdir out
javac -cp $(pwd)/src:$(pwd)/lib/* -d out $(find . -name *.java)
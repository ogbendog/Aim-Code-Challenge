javac -cp "./lib/*" src/test/ApiTest.java
java -cp "./lib/*:./src/" org.testng.TestNG testng.xml
#start server
mvn exec:java -Dexec.mainClass="Server"

#start application
mvn exec:java -Dexec.mainClass="App"
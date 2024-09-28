echo ""
echo ""
ollama list
mvn package
mvn compile exec:java -Dexec.mainClass="dev.datainmotion.Aircraft" -Dexec.args="'$1'"

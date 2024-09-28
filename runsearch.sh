mvn package
echo "Searching for $1"
mvn compile exec:java -Dexec.mainClass="dev.datainmotion.AircraftSearch"  -Dexec.args="'$1'"

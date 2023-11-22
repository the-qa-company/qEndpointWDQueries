# Merge profiling

for this benchmark, the qEndpoint version from the branch `dev_dl_file` of the repo https://github.com/the-qa-company/qEndpoint should be installed

Java 17 is required, to compile qendpoint maven is required

```powershell
# clone endpoint to qEndpoint
git clone -b dev_dl_file https://github.com/the-qa-company/qEndpoint
cd qEndpoint
# install endpoint
mvn clean install -DskipTests
# create file
./gradlew shadowJar

cp build/libs/*-all.jar bench.jar
java -jar bench.jar
```
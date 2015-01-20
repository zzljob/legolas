mvn clean dependency:sources eclipse:eclipse
mvn clean package -Dmaven.test.skip=true
mvn clean install -Dmaven.test.skip=true

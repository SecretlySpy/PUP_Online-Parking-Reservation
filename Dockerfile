FROM eclipse-temurin:17-jdk

WORKDIR /app

RUN apt-get update \
	&& apt-get install -y --no-install-recommends curl \
	&& rm -rf /var/lib/apt/lists/*

COPY src ./src

RUN mkdir -p build/lib build/classes \
	&& curl -fsSL https://repo.maven.apache.org/maven2/mysql/mysql-connector-java/8.0.19/mysql-connector-java-8.0.19.jar \
		-o build/lib/mysql-connector-java-8.0.19.jar \
	&& curl -fsSL https://repo.maven.apache.org/maven2/com/toedter/jcalendar/1.4/jcalendar-1.4.jar \
		-o build/lib/jcalendar-1.4.jar \
	&& javac -cp "build/lib/jcalendar-1.4.jar:build/lib/mysql-connector-java-8.0.19.jar:src" \
		-d build/classes $(find src -name "*.java")

ENV OPR_DB_HOST=db
ENV OPR_DB_PORT=3306
ENV OPR_DB_NAME=onlineparkingreservation
ENV OPR_DB_USER=root

CMD ["java", "-cp", "build/classes:build/lib/jcalendar-1.4.jar:build/lib/mysql-connector-java-8.0.19.jar:src", "usermanagement.login"]

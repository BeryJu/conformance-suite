FROM eclipse-temurin:17
COPY target/fapi-test-suite.jar /server/
ENV BASE_URL https://localhost:8443
ENV BASE_MTLS_URL https://localhost:8444
ENV MONGODB_HOST mongodb
ENV JAVA_EXTRA_ARGS=
EXPOSE 8080
ENTRYPOINT java \
	-Xdebug -Xrunjdwp:transport=dt_socket,address=*:9999,server=y,suspend=n \
	-jar /server/fapi-test-suite.jar \
	-Djdk.tls.maxHandshakeMessageSize=65536 \
	--fintechlabs.base_url=https://localhost.emobix.co.uk:8443 \
	--fintechlabs.base_mtls_url=https://localhost.emobix.co.uk:8444 \
	--fintechlabs.devmode=true \
	--fintechlabs.startredir=true


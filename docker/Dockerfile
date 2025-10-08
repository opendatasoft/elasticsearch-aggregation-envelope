FROM docker.elastic.co/elasticsearch/elasticsearch:8.18.1 AS elasticsearch-plugin-debug

COPY /build/distributions/envelope-aggregation-8.18.1.0.zip /tmp/envelope-aggregation-8.18.1.0.zip
RUN ./bin/elasticsearch-plugin install file:/tmp/envelope-aggregation-8.18.1.0.zip

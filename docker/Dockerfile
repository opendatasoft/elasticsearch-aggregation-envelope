FROM docker.elastic.co/elasticsearch/elasticsearch:7.17.6 AS elasticsearch-plugin-debug

COPY /build/distributions/envelope-aggregation-7.17.6.0.zip /tmp/envelope-aggregation-7.17.6.0.zip
RUN ./bin/elasticsearch-plugin install file:/tmp/envelope-aggregation-7.17.6.0.zip

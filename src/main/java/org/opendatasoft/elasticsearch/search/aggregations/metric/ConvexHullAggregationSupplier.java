package org.opendatasoft.elasticsearch.search.aggregations.metric;


import org.elasticsearch.search.aggregations.Aggregator;

@FunctionalInterface
public interface ConvexHullAggregationSupplier {
    Aggregator build();
}
package org.opendatasoft.elasticsearch.search.aggregations.metric;


import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.support.AggregationContext;
import org.elasticsearch.search.aggregations.support.ValuesSource;

import java.io.IOException;
import java.util.Map;

@FunctionalInterface
public interface ConvexHullAggregationSupplier {
    Aggregator build(String name, AggregationContext context, Aggregator parent,
                     ValuesSource.GeoPoint valuesSource,
                     Map<String, Object> metadata) throws IOException;
}
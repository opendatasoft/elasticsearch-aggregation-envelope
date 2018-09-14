package org.opendatasoft.elasticsearch.search.aggregations.metric;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.ObjectParser;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.aggregations.support.ValuesSourceParserHelper;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;
import org.elasticsearch.search.internal.SearchContext;

import java.io.IOException;
import java.util.Map;


public class ConvexHullAggregationBuilder extends ValuesSourceAggregationBuilder<ValuesSource.GeoPoint, ConvexHullAggregationBuilder> {

    public static final String NAME = "envelope";

    private static final ObjectParser<ConvexHullAggregationBuilder, Void> PARSER;
    static {
        PARSER = new ObjectParser<>(ConvexHullAggregationBuilder.NAME);
        ValuesSourceParserHelper.declareGeoFields(PARSER, false, false);
    }

    public static AggregationBuilder parse(String aggregationName, XContentParser parser) throws IOException {
        return PARSER.parse(parser, new ConvexHullAggregationBuilder(aggregationName), null);
    }

    public ConvexHullAggregationBuilder(String name) {
        super(name, ValuesSourceType.GEOPOINT, ValueType.GEOPOINT);
    }

    public ConvexHullAggregationBuilder(StreamInput in) throws IOException {
        super(in, ValuesSourceType.GEOPOINT, ValueType.GEOPOINT);
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    protected void innerWriteTo(StreamOutput out) throws IOException {

    }

    @Override
    protected ValuesSourceAggregatorFactory<ValuesSource.GeoPoint, ?> innerBuild(
            SearchContext context,
            ValuesSourceConfig<ValuesSource.GeoPoint> config,
            AggregatorFactory<?> parent,
            AggregatorFactories.Builder subFactoriesBuilder) throws IOException {
        return new ConvexHullAggregator.Factory(name, config, context, parent, subFactoriesBuilder, metaData);
    }

    @Override
    protected XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        return builder;
    }

    @Override
    protected int innerHashCode() {
        return 0;
    }

    @Override
    protected boolean innerEquals(Object obj) {
        return false;
    }
}

package org.opendatasoft.elasticsearch.search.aggregations.metric;

import org.elasticsearch.TransportVersion;
import org.elasticsearch.TransportVersions;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.AggregationContext;
import org.elasticsearch.search.aggregations.support.CoreValuesSourceType;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregationBuilder;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.elasticsearch.search.aggregations.support.ValuesSourceRegistry;
import org.elasticsearch.search.aggregations.support.ValuesSourceType;
import org.elasticsearch.xcontent.ObjectParser;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentParser;

import java.io.IOException;
import java.util.Map;

public class ConvexHullAggregationBuilder extends ValuesSourceAggregationBuilder<ConvexHullAggregationBuilder> {
    public static final String NAME = "envelope";

    public static final ValuesSourceRegistry.RegistryKey<ConvexHullAggregationSupplier> REGISTRY_KEY =
        new ValuesSourceRegistry.RegistryKey<>(NAME, ConvexHullAggregationSupplier.class);

    public static final ObjectParser<ConvexHullAggregationBuilder, String> PARSER = ObjectParser.fromBuilder(
        NAME,
        ConvexHullAggregationBuilder::new
    );
    static {
        ValuesSourceAggregationBuilder.declareFields(PARSER, false, false, false);
    }

    public static AggregationBuilder parse(String aggregationName, XContentParser parser) throws IOException {
        return PARSER.parse(parser, new ConvexHullAggregationBuilder(aggregationName), null);
    }

    public ConvexHullAggregationBuilder(String name) {
        super(name);
    }

    protected ConvexHullAggregationBuilder(
        ConvexHullAggregationBuilder clone,
        AggregatorFactories.Builder factoriesBuilder,
        Map<String, Object> metaData
    ) {
        super(clone, factoriesBuilder, metaData);
    }

    @Override
    public TransportVersion getMinimalSupportedVersion() {
        return TransportVersions.MINIMUM_COMPATIBLE;
    }

    @Override
    protected AggregationBuilder shallowCopy(AggregatorFactories.Builder factoriesBuilder, Map<String, Object> metaData) {
        return new ConvexHullAggregationBuilder(this, factoriesBuilder, metaData);
    }

    @Override
    public BucketCardinality bucketCardinality() {
        return null;
    }

    public ConvexHullAggregationBuilder(StreamInput in) throws IOException {
        super(in);
    }

    @Override
    public String getType() {
        return NAME;
    }

    @Override
    protected void innerWriteTo(StreamOutput out) throws IOException {

    }

    @Override
    protected ValuesSourceType defaultValueSourceType() {
        return CoreValuesSourceType.GEOPOINT;
    }

    @Override
    protected ValuesSourceAggregatorFactory innerBuild(
        AggregationContext context,
        ValuesSourceConfig config,
        AggregatorFactory parent,
        AggregatorFactories.Builder subFactoriesBuilder
    ) throws IOException {
        return new ConvexHullAggregator.Factory(name, config, context, parent, subFactoriesBuilder, metadata);
    }

    @Override
    protected XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        return builder;
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }

    public static void registerAggregators(ValuesSourceRegistry.Builder builder) {
        builder.register(ConvexHullAggregationBuilder.REGISTRY_KEY, CoreValuesSourceType.GEOPOINT, ConvexHullAggregator::new, true);
    }
}

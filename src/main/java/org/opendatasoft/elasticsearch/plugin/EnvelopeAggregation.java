package org.opendatasoft.elasticsearch.plugin;

import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.opendatasoft.elasticsearch.search.aggregations.metric.ConvexHullAggregationBuilder;
import org.opendatasoft.elasticsearch.search.aggregations.metric.InternalConvexHull;

import java.util.ArrayList;

public class EnvelopeAggregation extends Plugin implements SearchPlugin {
    @Override
    public ArrayList<SearchPlugin.AggregationSpec> getAggregations() {
        ArrayList<SearchPlugin.AggregationSpec> specs = new ArrayList<>();

        specs.add(
            new SearchPlugin.AggregationSpec(
                ConvexHullAggregationBuilder.NAME,
                ConvexHullAggregationBuilder::new,
                ConvexHullAggregationBuilder.PARSER
            ).addResultReader(InternalConvexHull::new).setAggregatorRegistrar(ConvexHullAggregationBuilder::registerAggregators)
        );

        return specs;
    }
}

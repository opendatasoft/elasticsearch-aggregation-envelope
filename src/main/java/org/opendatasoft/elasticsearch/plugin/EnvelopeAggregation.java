package org.opendatasoft.elasticsearch.plugin;

import java.util.ArrayList;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.plugins.SearchPlugin;
import org.opendatasoft.elasticsearch.search.aggregations.metric.ConvexHullAggregationBuilder;
import org.opendatasoft.elasticsearch.search.aggregations.metric.InternalConvexHull;

public class EnvelopeAggregation extends Plugin implements SearchPlugin {
    @Override
    public ArrayList<SearchPlugin.AggregationSpec> getAggregations() {
        ArrayList<SearchPlugin.AggregationSpec> r = new ArrayList<>();

        r.add(
                new AggregationSpec(
                        ConvexHullAggregationBuilder.NAME,
                        ConvexHullAggregationBuilder::new,
                        ConvexHullAggregationBuilder::parse)
                .addResultReader(InternalConvexHull::new)
        );

        return r;
    }
}

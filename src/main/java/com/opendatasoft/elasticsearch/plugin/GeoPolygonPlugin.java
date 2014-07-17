package com.opendatasoft.elasticsearch.plugin;

import com.opendatasoft.elasticsearch.search.aggregations.metric.ConvexHullParser;
import org.elasticsearch.plugins.AbstractPlugin;
import org.elasticsearch.search.aggregations.AggregationModule;
import com.opendatasoft.elasticsearch.search.aggregations.metric.InternalConvexHull;

public class GeoPolygonPlugin extends AbstractPlugin{

    @Override
    public String name() {
        return "envelope";
    }

    @Override
    public String description() {
        return "Returns envelope of geo points";
    }

    public void onModule(AggregationModule aggModule) {
        aggModule.addAggregatorParser(ConvexHullParser.class);
        InternalConvexHull.registerStreams();
    }

}

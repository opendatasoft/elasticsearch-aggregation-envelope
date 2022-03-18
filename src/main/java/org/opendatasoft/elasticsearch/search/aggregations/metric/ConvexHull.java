package org.opendatasoft.elasticsearch.search.aggregations.metric;

import org.elasticsearch.search.aggregations.Aggregation;
import org.locationtech.jts.geom.Geometry;

public interface ConvexHull extends Aggregation {
    Geometry getShape();
}

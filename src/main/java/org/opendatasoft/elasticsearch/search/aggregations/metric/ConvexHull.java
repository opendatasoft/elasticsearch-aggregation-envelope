package org.opendatasoft.elasticsearch.search.aggregations.metric;

import org.locationtech.jts.geom.Geometry;
import org.elasticsearch.search.aggregations.Aggregation;

public interface ConvexHull extends Aggregation {
    Geometry getShape();
}

package org.elasticsearch.search.aggregations.metric;

import com.vividsolutions.jts.geom.Geometry;
import org.elasticsearch.search.aggregations.Aggregation;

public interface ConvexHull extends Aggregation {

    public Geometry getShape();

}

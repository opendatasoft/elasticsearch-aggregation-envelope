package com.opendatasoft.elasticsearch.search.aggregations.metric;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.lucene.index.AtomicReaderContext;
import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.lease.Releasables;
import org.elasticsearch.common.util.ObjectArray;
import org.elasticsearch.index.fielddata.GeoPointValues;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregator;
import org.elasticsearch.search.aggregations.support.AggregationContext;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public final class ConvexHullAggregator extends MetricsAggregator {

    private final ValuesSource.GeoPoint valuesSource;
    private GeoPointValues values;
    private ObjectArray<Set<Coordinate>> geoPoints;


    protected ConvexHullAggregator(String name, long estimatedBucketsCount, AggregationContext aggregationContext, Aggregator parent, ValuesSource.GeoPoint valuesSource) {
        super(name, estimatedBucketsCount, aggregationContext, parent);
        this.valuesSource = valuesSource;
        geoPoints = bigArrays.newObjectArray(estimatedBucketsCount);
    }


    @Override
    public boolean shouldCollect() {
        return valuesSource != null;
    }

    @Override
    public InternalAggregation buildAggregation(long owningBucketOrdinal) {
        if (valuesSource == null) {
            return buildEmptyAggregation();
        }
        Set<Coordinate> points = geoPoints.get(owningBucketOrdinal);

        if (points == null) {
            return buildEmptyAggregation();
        }

        Geometry convexHull = new com.vividsolutions.jts.algorithm.ConvexHull(points.toArray(new Coordinate[points.size()]), ShapeBuilder.FACTORY).getConvexHull();

        return new InternalConvexHull(name, convexHull);
    }

    @Override
    public InternalAggregation buildEmptyAggregation() {
        return new InternalConvexHull(name, null);
    }

    @Override
    public void collect(int docId, long owningBucketOrdinal) throws IOException {

        if (owningBucketOrdinal >= geoPoints.size()) {
            geoPoints = bigArrays.grow(geoPoints, owningBucketOrdinal + 1);
        }

        Set<Coordinate> polygon = geoPoints.get(owningBucketOrdinal);

        if (polygon == null) {
            polygon = new HashSet<Coordinate>();
            geoPoints.set(owningBucketOrdinal, polygon);
        }

        final int valuesCount = values.setDocument(docId);

        for (int i=0; i<valuesCount; i++) {
            GeoPoint value = values.nextValue();
            polygon.add(new Coordinate(value.getLon(), value.getLat()));
        }
    }

    @Override
    protected void doClose() {
        Releasables.close(geoPoints);
    }

    @Override
    public void setNextReader(AtomicReaderContext reader) {
        this.values = this.valuesSource.geoPointValues();
    }

    public static class Factory extends ValuesSourceAggregatorFactory<ValuesSource.GeoPoint> {


        protected Factory(String name, ValuesSourceConfig<ValuesSource.GeoPoint> config) {
            super(name, InternalConvexHull.TYPE.name(),config);
        }

        @Override
        protected Aggregator createUnmapped(AggregationContext aggregationContext, Aggregator parent) {
            return new ConvexHullAggregator(name, 0, aggregationContext, parent, null);
        }

        @Override
        protected Aggregator create(ValuesSource.GeoPoint valuesSource, long expectedBucketsCount, AggregationContext aggregationContext, Aggregator parent) {
            return new ConvexHullAggregator(name, expectedBucketsCount, aggregationContext, parent, valuesSource);
        }
    }
}

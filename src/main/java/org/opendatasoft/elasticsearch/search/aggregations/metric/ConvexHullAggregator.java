package org.opendatasoft.elasticsearch.search.aggregations.metric;

import org.elasticsearch.common.geo.GeoPoint;
import org.elasticsearch.common.util.BigArrays;
import org.elasticsearch.common.util.ObjectArray;
import org.elasticsearch.core.Releasables;
import org.elasticsearch.index.fielddata.MultiGeoPointValues;
import org.elasticsearch.search.aggregations.AggregationExecutionContext;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.CardinalityUpperBound;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.LeafBucketCollector;
import org.elasticsearch.search.aggregations.LeafBucketCollectorBase;
import org.elasticsearch.search.aggregations.metrics.MetricsAggregator;
import org.elasticsearch.search.aggregations.support.AggregationContext;
import org.elasticsearch.search.aggregations.support.ValuesSource;
import org.elasticsearch.search.aggregations.support.ValuesSourceAggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValuesSourceConfig;
import org.locationtech.jts.algorithm.ConvexHull;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ConvexHullAggregator extends MetricsAggregator {

    private final ValuesSource.GeoPoint valuesSource;
    private ObjectArray<Set<Coordinate>> geoPoints;
    private BigArrays bigArrays;

    public ConvexHullAggregator(
        String name,
        AggregationContext context,
        Aggregator parent,
        ValuesSource.GeoPoint valuesSource,
        Map<String, Object> metadata
    ) throws IOException {
        super(name, context, parent, metadata);
        this.valuesSource = valuesSource;
        bigArrays = context.bigArrays();
        geoPoints = context.bigArrays().newObjectArray(10);
    }

    @Override
    public LeafBucketCollector getLeafCollector(AggregationExecutionContext aggCtx, LeafBucketCollector sub) throws IOException {
        if (valuesSource == null) {
            return LeafBucketCollector.NO_OP_COLLECTOR;
        }
        final MultiGeoPointValues values = valuesSource.geoPointValues(aggCtx.getLeafReaderContext());
        return new LeafBucketCollectorBase(sub, values) {
            @Override
            public void collect(int doc, long bucket) throws IOException {
                if (bucket >= geoPoints.size()) {
                    geoPoints = bigArrays.grow(geoPoints, bucket + 1);
                }

                Set<Coordinate> polygon = geoPoints.get(bucket);

                if (polygon == null) {
                    polygon = new HashSet<Coordinate>();
                    geoPoints.set(bucket, polygon);
                }

                values.advanceExact(doc);
                final int valuesCount = values.docValueCount();

                for (int i = 0; i < valuesCount; i++) {
                    GeoPoint value = values.nextValue();
                    polygon.add(new Coordinate(value.getLon(), value.getLat()));
                }
            }
        };
    }

    @Override
    public InternalAggregation buildAggregation(long bucket) throws IOException {
        if (valuesSource == null) {
            return buildEmptyAggregation();
        }
        Set<Coordinate> points = geoPoints.get(bucket);

        if (points == null) {
            return buildEmptyAggregation();
        }

        GeometryFactory fact = new GeometryFactory();
        Geometry convexHull = new ConvexHull(points.toArray(new Coordinate[points.size()]), fact).getConvexHull();

        return new InternalConvexHull(name, convexHull, null, metadata());
    }

    @Override
    public InternalAggregation buildEmptyAggregation() {
        return new InternalConvexHull(name, null, null, metadata());
    }

    @Override
    protected void doClose() {
        Releasables.close(geoPoints);
    }

    public static class Factory extends ValuesSourceAggregatorFactory {

        protected Factory(
            String name,
            ValuesSourceConfig config,
            AggregationContext context,
            AggregatorFactory parent,
            AggregatorFactories.Builder subFactoriesBuilder,
            Map<String, Object> metaData
        ) throws IOException {
            super(name, config, context, parent, subFactoriesBuilder, metaData);
        }

        @Override
        protected Aggregator createUnmapped(Aggregator parent, Map<String, Object> metadata) throws IOException {
            return new ConvexHullAggregator(name, context, parent, null, metadata);
        }

        @Override
        protected Aggregator doCreateInternal(Aggregator parent, CardinalityUpperBound cardinalityUpperBound, Map<String, Object> metadata)
            throws IOException {
            ValuesSource.GeoPoint valuesSource = (ValuesSource.GeoPoint) config.getValuesSource();
            return new ConvexHullAggregator(name, context, parent, valuesSource, metadata);
        }
    }
}

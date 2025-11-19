package org.opendatasoft.elasticsearch.search.aggregations.metric;

import org.elasticsearch.common.geo.GeoJson;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.geometry.Line;
import org.elasticsearch.geometry.LinearRing;
import org.elasticsearch.geometry.Polygon;
import org.elasticsearch.search.DocValueFormat;
import org.elasticsearch.search.aggregations.AggregationReduceContext;
import org.elasticsearch.search.aggregations.AggregatorReducer;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.xcontent.XContentBuilder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

public class InternalConvexHull extends InternalNumericMetricsAggregation.MultiValue implements ConvexHull {

    final Geometry convexHull;

    public InternalConvexHull(String name, Geometry convexHull, DocValueFormat format, Map<String, Object> metaData) {
        super(name, format, metaData);
        this.convexHull = convexHull;
    }

    public static final String CONVEX_HULL = "convex_hull";

    public enum GeoJsonGeoShapeType {
        POINT("Point"),
        MULTIPOINT("MultiPoint"),
        LINESTRING("LineString"),
        MULTILINESTRING("MultiLineString"),
        POLYGON("Polygon"),
        MULTIPOLYGON("MultiPolygon"),
        ENVELOPE("Envelope"),
        CIRCLE("Circle");

        protected final String shapename;

        GeoJsonGeoShapeType(String shapename) {
            this.shapename = shapename;
        }

        public static GeoJsonGeoShapeType forName(String geoshapename) {
            String typename = geoshapename.toLowerCase(Locale.ROOT);
            for (GeoJsonGeoShapeType type : values()) {
                if (type.shapename.equals(typename)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("unknown geo_shape [" + geoshapename + "]");
        }
    }

    /**
     * Utility class for converting libs/geo shapes thanks to coordinates array from some jts geom type.
     */
    public static class JtsGeometryConverter {
        private static org.elasticsearch.geometry.Geometry convertPoint(Coordinate[] coords) {
            return new org.elasticsearch.geometry.Point(coords[0].x, coords[0].y);
        }

        private static org.elasticsearch.geometry.Geometry convertLineString(Coordinate[] coordinates) {
            int size = coordinates.length;
            double[] x = new double[size];
            double[] y = new double[size];
            for (int i = 0; i < size; i++) {
                x[i] = coordinates[i].getX();
                y[i] = coordinates[i].getY();
            }
            return new Line(x, y);
        }

        private static org.elasticsearch.geometry.Geometry convertPolygon(Coordinate[] coordinates) {
            int size = coordinates.length;
            double[] x = new double[size];
            double[] y = new double[size];
            for (int i = 0; i < size; i++) {
                x[i] = coordinates[i].getX();
                y[i] = coordinates[i].getY();
            }
            LinearRing ring = new LinearRing(x, y);
            return new Polygon(ring);
        }
    }

    @Override
    public Iterable<String> valueNames() {
        return null;
    }

    @Override
    public double value(String name) {
        return 0;
    }

    public InternalConvexHull(StreamInput in) throws IOException {
        super(in);
        GeometryFactory fact = new GeometryFactory();
        int coordsSize = in.readInt();
        if (coordsSize > 0) {
            Coordinate[] coords = new Coordinate[coordsSize];
            for (int i = 0; i < coordsSize; i++) {
                coords[i] = new Coordinate(in.readDouble(), in.readDouble());
            }
            this.convexHull = new org.locationtech.jts.algorithm.ConvexHull(coords, fact).getConvexHull();
        } else {
            this.convexHull = null;
        }
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        out.writeNamedWriteable(format);
        if (convexHull != null) {
            out.writeInt(convexHull.getCoordinates().length);
            for (Coordinate coord : convexHull.getCoordinates()) {
                out.writeDouble(coord.x);
                out.writeDouble(coord.y);
            }
        } else {
            out.writeInt(0);
        }
    }

    @Override
    protected AggregatorReducer getLeaderReducer(AggregationReduceContext reduceContext, int size) {
        return new AggregatorReducer() {
            Geometry merged = null;

            @Override
            public void accept(InternalAggregation aggregation) {
                InternalConvexHull internalGeoPolygon = (InternalConvexHull) aggregation;

                if (internalGeoPolygon.convexHull == null) {
                    return;
                }

                if (merged == null) {
                    merged = internalGeoPolygon.convexHull;
                } else {
                    merged = merged.union(internalGeoPolygon.convexHull).convexHull();
                }
            }

            @Override
            public InternalAggregation get() {
                return new InternalConvexHull(name, merged, null, metadata);
            }
        };
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        org.locationtech.jts.geom.Geometry convexGeom = getShape();
        if (convexGeom != null) {
            builder.field(CONVEX_HULL);
            if (convexGeom.getGeometryType().equals("Point")) {
                GeoJson.toXContent(JtsGeometryConverter.convertPoint(convexGeom.getCoordinates()), builder, params);
            } else if (convexGeom.getGeometryType().equals("LineString")) {
                GeoJson.toXContent(JtsGeometryConverter.convertLineString(convexGeom.getCoordinates()), builder, params);
            } else if (convexGeom.getGeometryType().equals("Polygon")) {
                GeoJson.toXContent(JtsGeometryConverter.convertPolygon((convexGeom.getCoordinates())), builder, params);
            } else {
                throw new IllegalArgumentException("unknown geo_shape [" + convexGeom.getGeometryType() + "]");
            }
        }
        return builder;
    }

    @Override
    public int hashCode() {
        return Objects.hash(convexHull);
    }

    @Override
    public boolean equals(Object obj) {
        InternalConvexHull other = (InternalConvexHull) obj;
        return convexHull.equals(other.convexHull);
    }

    @Override
    public String getWriteableName() {
        return ConvexHullAggregationBuilder.NAME;
    }

    @Override
    public Geometry getShape() {
        return convexHull;
    }
}

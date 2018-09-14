package org.opendatasoft.elasticsearch.search.aggregations.metric;

import org.elasticsearch.common.geo.builders.CoordinatesBuilder;
import org.elasticsearch.common.geo.builders.LineStringBuilder;
import org.elasticsearch.common.geo.builders.PointBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalNumericMetricsAggregation;
import org.elasticsearch.search.aggregations.pipeline.PipelineAggregator;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class InternalConvexHull extends InternalNumericMetricsAggregation.MultiValue implements ConvexHull {

    final Geometry convexHull;

    public InternalConvexHull(
            String name, Geometry convexHull, List<PipelineAggregator> pipelineAggregators,
            Map<String, Object> metaData) {
        super(name, pipelineAggregators, metaData);
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
                if(type.shapename.equals(typename)) {
                    return type;
                }
            }
            throw new IllegalArgumentException("unknown geo_shape ["+geoshapename+"]");
        }
    }

    public static class GeoJsonPointBuilder extends PointBuilder {
        public static final GeoJsonGeoShapeType TYPE = GeoJsonGeoShapeType.POINT;

        private Coordinate coordinate;

        public GeoJsonPointBuilder(Coordinate coordinate) {
            super();
            this.coordinate = coordinate;
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(FIELD_TYPE, TYPE.shapename);
            builder.field(FIELD_COORDINATES);
            toXContent(builder, coordinate);
            return builder.endObject();
        }
    }

    public static class GeoJsonLineStringBuilder extends LineStringBuilder {
        public static final GeoJsonGeoShapeType TYPE = GeoJsonGeoShapeType.LINESTRING;

        public GeoJsonLineStringBuilder(CoordinatesBuilder coordinates) {
            super(coordinates);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(FIELD_TYPE, TYPE.shapename);
            builder.field(FIELD_COORDINATES);
            coordinatesToXcontent(builder, false);
            builder.endObject();
            return builder;
        }

    }

    public static class GeoJsonPolygonBuilder extends PolygonBuilder {
        public static final GeoJsonGeoShapeType TYPE = GeoJsonGeoShapeType.POLYGON;

        public GeoJsonPolygonBuilder(CoordinatesBuilder coordinates) {
            super(coordinates);
        }

        @Override
        public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
            builder.startObject();
            builder.field(FIELD_TYPE, TYPE.shapename);
            builder.startArray(FIELD_COORDINATES);
            coordinatesArray(builder, params);
            builder.endArray();
            builder.endObject();
            return builder;
        }

    }

    public static class GeoJsonShapeBuilder{
        public static GeoJsonPointBuilder newPoint(Coordinate coordinate) {
            return new GeoJsonPointBuilder(coordinate);
        }

        public static GeoJsonLineStringBuilder newLineString(Coordinate[] coordinates) {
            return new GeoJsonLineStringBuilder(new CoordinatesBuilder().coordinates(coordinates));
        }

        public static GeoJsonPolygonBuilder newPolygon(Coordinate[] coordinates) {
            return new GeoJsonPolygonBuilder(new CoordinatesBuilder().coordinates(coordinates));
        }
    }

    @Override
    public double value(String name) {
        return 0;
    }

    public InternalConvexHull(StreamInput in) throws IOException {
        super(in);
        int coordsSize = in.readInt();
        if (coordsSize > 0) {
            Coordinate[] coords = new Coordinate[coordsSize];
            for (int i = 0; i < coordsSize; i++) {
                coords[i] = new Coordinate(in.readDouble(), in.readDouble());
            }
            this.convexHull = new com.vividsolutions.jts.algorithm.ConvexHull(coords, ShapeBuilder.FACTORY).getConvexHull();
        } else {
            this.convexHull = null;
        }
    }

    @Override
    protected void doWriteTo(StreamOutput out) throws IOException {
        if (convexHull != null) {
            out.writeInt(convexHull.getCoordinates().length);
            for (Coordinate coord: convexHull.getCoordinates()) {
                out.writeDouble(coord.x);
                out.writeDouble(coord.y);
            }
        } else {
            out.writeInt(0);
        }
    }

    @Override
    public InternalAggregation doReduce(List<InternalAggregation> aggregations, ReduceContext reduceContext) {
        Geometry merged = null;

        for (InternalAggregation aggregation: aggregations) {
            InternalConvexHull internalGeoPolygon = (InternalConvexHull) aggregation;
            if (merged == null) {
                merged = internalGeoPolygon.convexHull;
            } else if (internalGeoPolygon.convexHull == null) {
                continue;
            } else {
                merged = merged.union(internalGeoPolygon.convexHull).convexHull();
            }
        }
        return new InternalConvexHull(name, merged, pipelineAggregators(), metaData);
    }

    @Override
    public XContentBuilder doXContentBody(XContentBuilder builder, Params params) throws IOException {
        Geometry convexGeom = getShape();
        if (convexGeom != null) {
            builder.field(CONVEX_HULL);
            if (convexGeom.getGeometryType().equals("Point")) {
                GeoJsonShapeBuilder.newPoint(convexGeom.getCoordinate()).toXContent(builder, params);
            } else if (convexGeom.getGeometryType().equals("LineString")) {
                GeoJsonShapeBuilder.newLineString(convexGeom.getCoordinates()).toXContent(builder, params);
            } else if (convexGeom.getGeometryType().equals("Polygon")) {
                GeoJsonShapeBuilder.newPolygon(convexGeom.getCoordinates()).toXContent(builder, params);
            }
        }
        return builder;
    }

    @Override
    protected int doHashCode() {
        return convexHull.hashCode();
    }

    @Override
    protected boolean doEquals(Object obj) {
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

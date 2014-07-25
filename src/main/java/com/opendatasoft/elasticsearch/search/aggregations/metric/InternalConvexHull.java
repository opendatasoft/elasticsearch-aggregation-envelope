package com.opendatasoft.elasticsearch.search.aggregations.metric;

import com.spatial4j.core.shape.Shape;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import org.elasticsearch.ElasticsearchIllegalArgumentException;
import org.elasticsearch.common.geo.builders.LineStringBuilder;
import org.elasticsearch.common.geo.builders.PointBuilder;
import org.elasticsearch.common.geo.builders.PolygonBuilder;
import org.elasticsearch.common.geo.builders.ShapeBuilder;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentBuilderString;
import org.elasticsearch.search.aggregations.AggregationStreams;
import org.elasticsearch.search.aggregations.InternalAggregation;
import org.elasticsearch.search.aggregations.metrics.InternalMetricsAggregation;

import java.io.IOException;
import java.util.*;

public class InternalConvexHull extends InternalMetricsAggregation implements ConvexHull {

    public final static Type TYPE = new Type("envelope");

    public final static AggregationStreams.Stream STREAM = new AggregationStreams.Stream() {
        @Override
        public InternalConvexHull readResult(StreamInput in) throws IOException {
            InternalConvexHull result = new InternalConvexHull();
            result.readFrom(in);
            return result;
        }
    };

    public static void registerStreams() {
        AggregationStreams.registerStream(STREAM, TYPE.stream());
    }

    public InternalConvexHull() {}

    Geometry convexHull;

    public InternalConvexHull(String name, Geometry convexHull) {
        super(name);
        this.convexHull = convexHull;
    }

    @Override
    public Geometry getShape() {
        return convexHull;
    }


    @Override
    public Type type() {
        return TYPE;
    }

    @Override
    public InternalAggregation reduce(ReduceContext reduceContext) {

        Geometry merged = null;

        for (InternalAggregation aggregation: reduceContext.aggregations()){
            InternalConvexHull internalGeoPolygon = (InternalConvexHull) aggregation;
            if (merged == null){
                merged = internalGeoPolygon.convexHull;
            } else {
                merged = merged.union(internalGeoPolygon.convexHull).convexHull();
            }
        }
        return new InternalConvexHull(name, merged);
    }

    public static final XContentBuilderString CONVEX_HULL = new XContentBuilderString("convex_hull");


    public static enum GeoJsonGeoShapeType {
        POINT("Point"),
        MULTIPOINT("MultiPoint"),
        LINESTRING("LineString"),
        MULTILINESTRING("MultiLineString"),
        POLYGON("Polygon"),
        MULTIPOLYGON("MultiPolygon"),
        ENVELOPE("Envelope"),
        CIRCLE("Circle");

        protected final String shapename;

        private GeoJsonGeoShapeType(String shapename) {
            this.shapename = shapename;
        }

        public static GeoJsonGeoShapeType forName(String geoshapename) {
            String typename = geoshapename.toLowerCase(Locale.ROOT);
            for (GeoJsonGeoShapeType type : values()) {
                if(type.shapename.equals(typename)) {
                    return type;
                }
            }
            throw new ElasticsearchIllegalArgumentException("unknown geo_shape ["+geoshapename+"]");
        }
    }

    public static class GeoJsonPointBuilder extends PointBuilder {
        public static final GeoJsonGeoShapeType TYPE = GeoJsonGeoShapeType.POINT;

        private Coordinate coordinate;

        public GeoJsonPointBuilder coordinate(Coordinate coordinate) {
            this.coordinate = coordinate;
            return this;
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

    public abstract static class GeoJsonShapeBuilder extends ShapeBuilder {
        public abstract Shape build();

        public static GeoJsonPointBuilder newPoint(Coordinate coordinate) {
            return new GeoJsonPointBuilder().coordinate(coordinate);
        }

        public static GeoJsonLineStringBuilder newLineString() {
            return new GeoJsonLineStringBuilder();
        }

        public static GeoJsonPolygonBuilder newPolygon() {
            return new GeoJsonPolygonBuilder();
        }
    }

    @Override
    public XContentBuilder toXContent(XContentBuilder builder, Params params) throws IOException {
        Geometry convexGeom = getShape();
        ShapeBuilder res = null;
        if (convexGeom.getGeometryType().equals("Point")){
            res = GeoJsonShapeBuilder.newPoint(convexGeom.getCoordinate());
        } else if (convexGeom.getGeometryType().equals("LineString")){
            res = GeoJsonShapeBuilder.newLineString().points(convexGeom.getCoordinates());
        } else if (convexGeom.getGeometryType().equals("Polygon")) {
            res = GeoJsonShapeBuilder.newPolygon().points(convexGeom.getCoordinates());
        }
        builder.startObject(name);
        builder.field(CONVEX_HULL);

        res.toXContent(builder, params);
        builder.endObject();
        return builder;
    }

    @Override
    public void readFrom(StreamInput in) throws IOException {
        name = in.readString();
        int coordsSize = in.readInt();
        Coordinate[] coords = new Coordinate[coordsSize];
        for (int i=0; i < coordsSize; i++) {
            coords[i] = new Coordinate(in.readDouble(), in.readDouble());
        }
        this.convexHull = new com.vividsolutions.jts.algorithm.ConvexHull(coords, ShapeBuilder.FACTORY).getConvexHull();
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(name);
        out.writeInt(convexHull.getCoordinates().length);
        for (Coordinate coord: convexHull.getCoordinates()) {
            out.writeDouble(coord.x);
            out.writeDouble(coord.y);
        }
    }

}

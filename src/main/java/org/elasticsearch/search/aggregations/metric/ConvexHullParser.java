package org.elasticsearch.search.aggregations.metric;

import org.elasticsearch.common.xcontent.XContentParser;
import org.elasticsearch.search.aggregations.Aggregator;
import org.elasticsearch.search.aggregations.AggregatorFactory;
import org.elasticsearch.search.aggregations.support.ValueType;
import org.elasticsearch.search.aggregations.support.ValuesSourceParser;
import org.elasticsearch.search.internal.SearchContext;
import org.elasticsearch.search.aggregations.support.ValuesSource.GeoPoint;

import java.io.IOException;


public class ConvexHullParser implements Aggregator.Parser {

    @Override
    public String type() {
        return InternalConvexHull.TYPE.name();
    }

    @Override
    public AggregatorFactory parse(String aggregationName, XContentParser parser, SearchContext context) throws IOException {
        ValuesSourceParser<GeoPoint> vsParser = ValuesSourceParser.geoPoint(aggregationName, InternalConvexHull.TYPE, context)
                .targetValueType(ValueType.GEOPOINT)
                .formattable(true)
                .build();

        XContentParser.Token token;

        String currentFieldName = null;

        while((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
            if (token == XContentParser.Token.FIELD_NAME) {
                currentFieldName = parser.currentName();
            } else if (vsParser.token(currentFieldName, token, parser)) {
                continue;
            }
        }
        return new ConvexHullAggregator.Factory(aggregationName, vsParser.config());
    }
}

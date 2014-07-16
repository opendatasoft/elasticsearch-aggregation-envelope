Elasticsearch Aggregation Envelope Plugin
=========================================

The envelope aggregation plugin adds the possibility to compute convex envelope for geo points.

This is a metric aggregation.

|   Envelope aggregation Plugin  | elasticsearch     | Release date |
|--------------------------------|-------------------|:------------:|
| 1.0.0                          | 1.2.2 -> master   |  2014-07-16  |


Usage
-----

```json
{
  "aggregations": {
    "<aggregation_name>": {
      "envelope": {
        "field": "<field_name>"
      }
    }
  }
}
```

`field` must be of type geo_point.

It returns a Geometry:
    - Point if the bucket contains only one unique point
    - LineString if the bucket contains two unique points
    - Polygon if the bucket contains more than three unique points

Installation
------------

`bin/plugin --install envelope_aggregation --url "https://github.com/opendatasoft/elasticsearch-aggregation-envelope/raw/master/dist/elasticsearch-envelope-aggregation-1.0.0.zip"`

License
-------

This software is under The MIT License (MIT)
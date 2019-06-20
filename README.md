Elasticsearch Envelope Aggregation
==================================

The envelope aggregation plugin adds the possibility to compute convex envelope for geo points.

This is a metric aggregation.

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

For example :

```json
{
    "convex_hull": {
      "type": "Polygon",
      "coordinates": [
        [
          [
            2.454928932711482,
            48.82157796062529
          ],
          [
            2.336266916245222,
            48.82202098611742
          ],
          [
            2.252937974408269,
            48.84604096412659
          ],
          [
            2.240357995033264,
            48.86348098050803
          ],
          [
            2.245857948437333,
            48.86913998052478
          ],
          [
            2.2791109699755907,
            48.87238298077136
          ],
          [
            2.380628976970911,
            48.879756960086524
          ],
          [
            2.4384649470448494,
            48.8420399883762
          ],
          [
            2.454928932711482,
            48.82157796062529
          ]
        ]
      ]
    }
}
```

Installation
------------

Plugin versions are available for (at least) all minor versions of Elasticsearch since 6.0.

The first 3 digits of plugin version is Elasticsearch versioning. The last digit is used for plugin versioning under an elasticsearch version.

To install it, launch this command in Elasticsearch directory replacing the url by the correct link for your Elasticsearch version (see table)
`./bin/elasticsearch-plugin install https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.8.0.1/envelope-aggregation-6.8.0.1.zip`

| elasticsearch version | plugin version | plugin url |
| --------------------- | -------------- | ---------- |
| 6.0.1 | 6.0.1.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.0.1.0/envelope-aggregation-6.0.1.0.zip |
| 6.1.4 | 6.1.4.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.1.4.0/envelope-aggregation-6.1.4.0.zip |
| 6.2.4 | 6.2.4.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.2.4.0/envelope-aggregation-6.2.4.0.zip |
| 6.3.2 | 6.3.2.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.3.2.0/envelope-aggregation-6.3.2.0.zip |
| 6.4.3 | 6.4.3.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.4.3.0/envelope-aggregation-6.4.3.0.zip |
| 6.5.4 | 6.5.4.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.5.4.0/envelope-aggregation-6.5.4.0.zip |
| 6.6.2 | 6.6.2.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.6.2.0/envelope-aggregation-6.6.2.0.zip |
| 6.7.1 | 6.7.1.0 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.7.1.0/envelope-aggregation-6.7.1.0.zip |
| 6.8.0 | 6.8.0.1 | https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v6.8.0.1/envelope-aggregation-6.8.0.1.zip |



License
-------

This software is under The MIT License (MIT)
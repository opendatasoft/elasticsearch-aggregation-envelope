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

```
PUT test_envelope

PUT test_envelope/_mapping
{
  "properties": {
    "location": {"type": "geo_point"}
  }
}

POST test_envelope/_bulk?refresh
{"index":{"_id":1}}
{"location":[2.454929, 48.821578]}
{"index":{"_id":2}}
{"location":[2.245858, 48.86914]}

POST test_envelope/_search?size=0
{
    "aggs": {
        "my_agg": {
            "envelope": {
                "field": "location"
            }
        }
    }
}
```

This query should return the envelope of the two points (i.e., the following line):

```json
{
  "aggregations": {
    "my_agg": {
      "convex_hull": {
        "type": "LineString",
        "coordinates": [
          [
            2.454928932711482,
            48.82157796062529
          ],
          [
            2.245857948437333,
            48.86913998052478
          ]
        ]
      }
    }
  }
}
```

Installation
------------

Plugin versions are available for (at least) all minor versions of Elasticsearch since 6.0.

The first 3 digits of plugin version is Elasticsearch versioning. The last digit is used for plugin versioning under an elasticsearch version.

To install it, launch this command in Elasticsearch directory replacing the url by the correct link for your Elasticsearch version (see GitHub releases for available versions):
`./bin/elasticsearch-plugin install https://github.com/opendatasoft/elasticsearch-aggregation-envelope/releases/download/v8.19.6.1/envelope-aggregation-8.19.6.1.zip`


## Development Environment Setup

Build the plugin using gradle:
``` shell
# to format the code then build
./gradlew spotlessApply build
```

or
``` shell
./gradlew assemble  # (to avoid the test suite)
```

To run tests thanks to the Yaml Rest Test framework:
```sh
./gradlew yamlRestTest
```

Then the following command will start a dockerized ES and will install the previously built plugin:
``` shell
docker-compose up
```

Please be careful during development: you'll need to manually rebuild the .zip using `./gradlew build` on each code
change before running `docker-compose` up again.

> NOTE: In `docker-compose.yml` you can uncomment the debug env and attach a REMOTE JVM on `*:5005` to debug the plugin.


License
-------

This software is under AGPL (GNU Affero General Public License)

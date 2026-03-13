# JCT ELK Analysis Cookbook

This guide shows how to analyze JCT UDP and TCP events in Elasticsearch and Kibana, with focus on the `stack` field.

With the current `logstash.conf`, both UDP and TCP input are written to the same index pattern: `jct-events-*`.

Example event shape:

```json
{
  "timestampMillis": "1773395676972",
  "stack": [
    "de.otto.droporder.cockpit.model.DlqStorageModel.payloadHash()"
  ]
}
```

## What you can answer with this guide

- Which events contain class `de.otto.droporder.cockpit.model.DlqStorageModel`?
- How often was that class hit over time?
- Which methods are called most often?
- Which classes/methods are hot overall?

## Transport note (UDP vs TCP)

- Current default setup: UDP and TCP events are stored together in `jct-events-*`.
- That means all search and aggregation examples below work for both transports as-is.
- If you split indices later (for example `tcp-events-*`), use the same queries and replace the index pattern.

## 1) First check your mapping (important)

Open Kibana -> Dev Tools and run:

```http
GET jct-events-*/_mapping/field/stack*
```

In most setups, `stack` is mapped as `text` with `stack.keyword` as `keyword`.
If your mapping differs, replace field names in queries below.

## 2) Quick filtering in Discover (KQL)

Use a data view for `jct-events-*` and set the time range first.

Search for classes:

```kql
stack:de.a.b.c.controller.SomeController*
```

Search for methods

```kql
stack:de.a.b.c.controller.SomeController.someMethod*
```

## 3) Elasticsearch queries you can copy

The examples below use `jct-events-*` because that is what the current Logstash config writes.
If you route TCP to `tcp-events-*`, just replace the index name.

### A) Show latest documents that contain the class

```http
GET jct-events-*/_search
{
  "size": 20,
  "sort": [
    { "@timestamp": "desc" }
  ],
  "query": {
    "wildcard": {
      "stack.keyword": {
        "value": "de.some.package.SomeClass*"
      }
    }
  }
}
```

### Count hits for that class over time

This is your "how many times over which timeframe" query.

```http
GET jct-events-*/_search
{
  "size": 0,
  "query": {
    "wildcard": {
      "stack.keyword": {
        "value": "de.some.package.SomeClass*"
      }
    }
  },
  "aggs": {
    "calls_over_time": {
      "date_histogram": {
        "field": "@timestamp",
        "fixed_interval": "5m",
        "min_doc_count": 0
      }
    }
  }
}
```

Tip: use `1m`, `15m`, `1h`, or `1d` for `fixed_interval` depending on volume.

Top methods for class or package

This extracts method names from stack entries at query time (no reindex needed).

```http
GET jct-events-*/_search
{
  "size": 0,
  "runtime_mappings": {
    "stack_method": {
      "type": "keyword",
      "script": {
        "source": "for (def s : doc['stack.keyword']) { int dot = s.lastIndexOf('.'); int paren = s.indexOf('(', dot + 1); if (dot > 0 && paren > dot) { emit(s.substring(dot + 1, paren)); } }"
      }
    }
  },
  "query": {
    "wildcard": {
      "stack.keyword": {
        "value": "de.some.package*"
      }
    }
  },
  "aggs": {
    "top_methods": {
      "terms": {
        "field": "stack_method",
        "size": 20
      }
    }
  }
}
```

### Calls over time for one method

```http
GET jct-events-*/_search
{
  "size": 0,
  "query": {
    "term": {
      "stack.keyword": "de.some.package.SomeClass.someMethod()"
    }
  },
  "aggs": {
    "method_calls_over_time": {
      "date_histogram": {
        "field": "@timestamp",
        "fixed_interval": "5m",
        "min_doc_count": 0
      }
    }
  }
}
```

### Top classes overall

```http
GET jct-events-*/_search
{
  "size": 0,
  "runtime_mappings": {
    "stack_class": {
      "type": "keyword",
      "script": {
        "source": "for (def s : doc['stack.keyword']) { int dot = s.lastIndexOf('.'); if (dot > 0) { emit(s.substring(0, dot)); } }"
      }
    }
  },
  "aggs": {
    "top_classes": {
      "terms": {
        "field": "stack_class",
        "size": 30
      }
    }
  }
}
```

### TCP examples (same query style, different index pattern)

If you route TCP to its own index, these are direct equivalents:

```http
GET tcp-events-*/_search
{
  "size": 20,
  "sort": [
    { "@timestamp": "desc" }
  ],
  "query": {
    "wildcard": {
      "stack.keyword": {
        "value": "de.otto.droporder.cockpit.model.DlqStorageModel*"
      }
    }
  }
}
```

```http
GET tcp-events-*/_search
{
  "size": 0,
  "query": {
    "wildcard": {
      "stack.keyword": {
        "value": "de.otto.droporder.cockpit.model.DlqStorageModel*"
      }
    }
  },
  "aggs": {
    "calls_over_time": {
      "date_histogram": {
        "field": "@timestamp",
        "fixed_interval": "5m",
        "min_doc_count": 0
      }
    }
  }
}
```

## 4) Kibana visualizations (Lens)

### Calls over time for one class

1. Open Visualization Library -> Create New -> Lens with data view `jct-events-*`.
2. Filter (KQL):
   `stack.keyword : "de.otto.droporder.cockpit.model.DlqStorageModel*"`
3. X-axis: Date histogram on `@timestamp`.
4. Y-axis: Count.
5. Save as `DlqStorageModel calls over time`.

### Top methods table

1. Keep the same filter.
2. Visualization type: Data table.
3. Rows: `Top values of stack.keyword`.
4. Metric: Count.
5. Optional: set row limit to 20.

If you want only bare method names (without class), use the runtime field query from section 3C in Dev Tools, or create an equivalent runtime field in Kibana Data View.

For long-term production usage, prefer parsing `timestampMillis` into a real `date` field at ingest time.

### TCP examples in Kibana

- Shared index setup (default): use the same Lens/Discover examples with data view `jct-events-*`.
- Split index setup: create another data view for `tcp-events-*` and reuse the same visualizations.
- Optional comparison dashboard: place one panel for `jct-events-*` and one for `tcp-events-*` with identical filters.

## 5) Common pitfalls

- `stack` is an array of strings, not nested objects.
- `terms` aggregation needs a `keyword`-style field, not plain analyzed `text`.
- Wildcard on huge high-cardinality fields can be expensive; narrow time range first.
- Counts are per document. If one document contains many stack entries, each matching value still counts once per doc for that bucket.

## 6) Fast workflow recommendation

1. Start in Discover with KQL (`stack.keyword : "...*"`).
2. Move to Lens for trend charts.
3. Use Dev Tools runtime fields when you need class/method extraction.
4. If this becomes a daily workflow, add ingest-time fields (`stack_class`, `stack_method`, `event_time`) and index templates.

# qEndpointWDQueries

- [qEndpointWDQueries](#qendpointwdqueries)
  - [Scripts list](#scripts-list)
    - [work.py](#workpy)
    - [test3.ipynb](#test3ipynb)
    - [test.ipynb](#testipynb)
    - [test2.ipynb](#test2ipynb)
    - [wikidata-changes.ipynb](#wikidata-changesipynb)
  - [Query dump](#query-dump)
- [BSBM Benchmark](#bsbm-benchmark)

Queries and tool used to compare qEndpoint against other systems

These queries are based on the [Wikidata_SPARQL_Logs](https://iccl.inf.tu-dresden.de/web/Wikidata_SPARQL_Logs/en), mainly the Interval 7.

## Scripts list

This repository contains a lot of old scripts, the most importants are the [`work.py`](#workpy) and the [`test3.ipynb`](#test3ipynb), the others are here to show the previous expermeriments we've tried. We move our work on a remote qEndpoint endpoint to have a fair comparison against the others.

- "rec" = Recursive (=Queries with a path query)

To run these scripts from your own without using our sorted dataset, you need first to download and uncompress the query log file into `wdlogsh.tsv`.

### work.py

This is the script used to query the endpoints.

To config it, you need to open the file and search for the lines:

- `# <<CONFIG POINT 1>>`, after this line you can configure tests without or with only recursive queries (aka path queries) or to increase the count of queries used from the `wdlogsh.tsv` file, the current one is 100k queries.
- `# <<CONFIG POINT 2>>`, after this line you can configure the endpoints to send the queries.

the results files will be written in the file `results.json` (non recursive) and `results_rec.json` (recursive) with the format:

```json
{
  "engines": [
      {
          "id": "sparql endpoint id",
          "name": "sparql endpoint name",
          "time1": [
            number
          ],
          "number_result1": [
            number
          ],
          "error1": [
            boolean
          ]
      }
  ]
}
```

- The ith element of time1 is the time to run the query i to run.
- The ith element of number_result1 is the number of the result of the query i to run.
- The ith element of error1 is if the ith query thrown an error.

### test3.ipynb

Script used to get information from a run against a remote endpoint vs the others.

### test.ipynb

Old script getting the information from a run against a local endpoint vs the others.

### test2.ipynb

Same as [test.ipynb](#testipynb) with more values.

### wikidata-changes.ipynb

script used to do the experiments for [Easily setting up a local Wikidata SPARQL endpoint using the qEndpoint](https://ceur-ws.org/Vol-3262/paper10.pdf). These results were done the start of the query logs with a local endpoint vs remote endpoints, added to the fact that the first queries were easier to run than the others, these values can't really be taken into account.

## Query dump

We took the interval 7 of the [Wikidata_SPARQL_Logs](https://iccl.inf.tu-dresden.de/web/Wikidata_SPARQL_Logs/en) and we randomly pick 100k queries from it. These queries can be find in the `query_dump_100k.json`. We then tried the first queries over all the endpoints. Once 10k queries were sent without any error, we have the `query_dump_10k_valid.json` dataset, the `query_dump_10k_failed.json` is containing all the failing queries with all the valid queries.

# BSBM Benchmark

The benchmark to compare qendpoint with other systems using the Berlin SPARQL Benchmark (BSBM) is available in the [bsbm-bench](bsbm-bench/README.md) directory.

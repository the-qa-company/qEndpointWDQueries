# qEndpointWDQueries

- [qEndpointWDQueries](#qendpointwdqueries)
  - [Scripts list](#scripts-list)
    - [work.py](#workpy)
    - [test3.ipynb](#test3ipynb)
    - [test.ipynb](#testipynb)
    - [test2.ipynb](#test2ipynb)
    - [wikidata-changes.ipynb](#wikidata-changesipynb)

Queries and tool used to compare qEndpoint against other systems

These queries are based on the [Wikidata_SPARQL_Logs](https://iccl.inf.tu-dresden.de/web/Wikidata_SPARQL_Logs/en), mainly the Interval 7.

## Scripts list

This repository contains a lot of old scripts, the most importants are the [`work.py`](#workpy) and the [`test3.ipynb`](#test3ipynb), the others are here to show the previous expermeriments we've tried. We move our work on a remote qEndpoint endpoint to have a fair comparison against the others.

- "rec" = Recursive (=Queries with a path query)

### work.py

This is the script used to query the endpoints.

### test3.ipynb

Script used to get information from a run against a remote endpoint vs the others.

### test.ipynb

Old script getting the information from a run against a local endpoint vs the others.

### test2.ipynb

Same as [test.ipynb](#testipynb) with more values.

### wikidata-changes.ipynb

script used to do the experiments for [Easily setting up a local Wikidata SPARQL endpoint using the qEndpoint](https://ceur-ws.org/Vol-3262/paper10.pdf). These results were done the start of the query logs with a local endpoint vs remote endpoints, added to the fact that the first queries were easier to run than the others, these values can't really be taken into account.

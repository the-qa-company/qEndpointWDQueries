#!/usr/bin/env python3
import datetime
import sys
from SPARQLWrapper import SPARQLWrapper, JSON
import csv
import urllib
import json
import itertools

COUNT = 10_000

DO_NORMAL = True
DO_RECURSIVE = True

exclude = []
exclude.append("http://www.bigdata.com/")
exclude.append("http://www.bigdata.com/")

print("read tsv file...")

# total number of queries
sample_r = []
count = 0
with open("wdlogsh.tsv") as file:
    tsv_file = csv.reader(file, delimiter="\t")
    for line in tsv_file:
        count = count + 1
        if count > 1:
            query = (
                urllib.parse.unquote_plus(line[0])
                .replace("> *", ">*")
                .replace("> / <", ">/<")
            )
            sample_r.append(query)
print("sample size:", len(sample_r))


def filter(x: str):
    return "http://www.bigdata.com" in x or "MINUS" in x


sample_no_minus = itertools.filterfalse(filter, sample_r)
sample = [x for x in sample_no_minus]


def filter(x: str):
    return (">*" in x or ">+" in x or " | " in x or ">?" in x or " / " in x or "^(" in x) and ("REGEX" not in x)


rec_queries_startt = set(i for (i, e) in enumerate(sample) if filter(e))

rec_queries_all = [e for e in sample if filter(e)]

rec_queries_5k = rec_queries_all[:5000]

print(len(rec_queries_5k))


class ComputedInfo:
    def __init__(self, id: str, host1: str, name: str):
        self.id = id
        self.name = name
        self.time1 = []
        self.number_result1 = []
        self.error1 = []

        self.sparql1 = SPARQLWrapper(
            host1,
            agent="D063520 (https://the-qa-company.com/; dennis.diefenbach@the-qa-company.com)",
        )
        self.sparql1.setReturnFormat(JSON)


if not (DO_NORMAL or DO_RECURSIVE):
    print("no DO_ set", f=sys.stderr)
    exit - 1

sparlq = [ComputedInfo(id, url, name) for url, id, name in [
    ("https://qlever.cs.uni-freiburg.de/api/wikidata", "ql", "QLever"),
    ("https://wikidata.demo.openlinksw.com/sparql", "vs", "Virtuoso"),
    ("http://query.wikidata.org/sparql", "wd", "Blazegraph"),
    ("http://51.158.120.80:1234/api/endpoint/sparql", "qe", "qEndpoint"),
]]

if DO_NORMAL:
    count = 8000
    for query in sample[8000:]:
        # print(query.replace('\n', ' '))
        if count >= COUNT:
            break

        for i in range(len(sparlq)):
            sp = sparlq[i]
            try:
                print(f"{count}", sp.name + "...",
                      end="", file=sys.stderr, flush=True)
                before = datetime.datetime.now()
                sp.sparql1.setQuery(query + "")
                ret = sp.sparql1.queryAndConvert()
                delta = datetime.datetime.now() - before
                microseconds = delta.total_seconds() * 1000

                sp.time1.append(microseconds)

                if "boolean" in ret:
                    if ret["boolean"] == "True":
                        sp.number_result1.append(1)
                    else:
                        sp.number_result1.append(0)
                else:
                    rv = 0
                    for r in ret["results"]["bindings"]:
                        rv += 1
                    sp.number_result1.append(rv)
                print("Done", end="", file=sys.stderr, flush=True)
                sp.error1.append(False)
            except Exception as inst:
                print(inst, file=sys.stderr)
                sp.error1.append(True)
        count += 1
        if count % 1000 == 0:
            with open("results_pre_" + str(count) + ".json", "w") as f:
                json.dump(
                    {
                        "precount": count,
                        "engines": [
                            {
                                "id": x.id,
                                "name": x.name,
                                "time1": x.time1,
                                "number_result1": x.number_result1,
                                "error1": x.error1
                            } for x in sparlq
                        ]
                    },
                    f,
                    indent=2,
                )
        print(file=sys.stderr)
        for i in range(len(sparlq)):
            sp = sparlq[i]

    print("result wrote in file results.json")

    with open("results.json", "w") as f:
        json.dump(
            {
                "engines": [
                    {
                        "id": x.id,
                        "name": x.name,
                        "time1": x.time1,
                        "number_result1": x.number_result1,
                        "error1": x.error1
                    } for x in sparlq
                ]
            },
            f,
            indent=2,
        )

if DO_RECURSIVE:
    count = 0
    for query in rec_queries_5k:
        # print(query.replace('\n', ' '))
        if count >= COUNT:
            break

        for i in range(len(sparlq)):
            sp = sparlq[i]
            try:
                print(f"{count}", sp.name + "...",
                      end="", file=sys.stderr, flush=True)
                before = datetime.datetime.now()
                sp.sparql1.setQuery(query + "")
                ret = sp.sparql1.queryAndConvert()
                delta = datetime.datetime.now() - before
                microseconds = delta.total_seconds() * 1000

                sp.time1.append(microseconds)

                if "boolean" in ret:
                    if ret["boolean"] == "True":
                        sp.number_result1.append(1)
                    else:
                        sp.number_result1.append(0)
                else:
                    rv = 0
                    for r in ret["results"]["bindings"]:
                        rv += 1
                    sp.number_result1.append(rv)
                print("Done", end="", file=sys.stderr, flush=True)
                sp.error1.append(False)
            except Exception as inst:
                print(inst, file=sys.stderr)
                sp.error1.append(True)
        count += 1
        if count % 100 == 0:
            with open("results_rec_pre_" + str(count) + ".json", "w") as f:
                json.dump(
                    {
                        "precount": count,
                        "engines": [
                            {
                                "id": x.id,
                                "name": x.name,
                                "time1": x.time1,
                                "number_result1": x.number_result1,
                                "error1": x.error1
                            } for x in sparlq
                        ]
                    },
                    f,
                    indent=2,
                )
        print(file=sys.stderr)
        for i in range(len(sparlq)):
            sp = sparlq[i]

    print("result wrote in file results.json")

    with open("results_rec.json", "w") as f:
        json.dump(
            {
                "engines": [
                    {
                        "id": x.id,
                        "name": x.name,
                        "time1": x.time1,
                        "number_result1": x.number_result1,
                        "error1": x.error1
                    } for x in sparlq
                ]
            },
            f,
            indent=2,
        )

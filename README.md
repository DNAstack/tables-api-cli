# Dataset API Command-line Client

## Intro

**GA4GH Dataset API** is a subset of endpoints and schemas of **GA4GH Search API**, defined in [ga4gh-search-apis](https://github.com/DNAstack/ga4gh-search-apis) repository.


## Usage

### Installation

Download [dataset-api-cli-0.2.0-executable.jar](https://nexus.dnastack.com/service/local/repositories/releases/content/org/ga4gh/dataset/dataset-api-cli/0.2.0/dataset-api-cli-0.2.0-executable.jar). Make it executable (e.g.
`chmod +x dataset-api-cli-0.2.0-executable.jar`)

Optionally create an executable `datasets` script, with contents like this:

```bash
#!/bin/bash
/path/to/dataset-api-cli-0.2.0-executable.jar $@
```

In all of the examples bellow we'll use `datasets` shortcut instead of the full JAR name.

### Configuration

```
$ datasets set-config \
  --api-url https://pgp-dataset-service.staging.dnastack.com/data/v1 \
  --username yourname \
  --password yourpassword
```

This will store the config to your user path `~/.config/datasets/config.json`

### List datasets

```
$ datasets list
┌────────────┬───────────────────────┬────────────────────────────────────────┐
│ Dataset ID │ Dataset description   │ Schema                                 │
├────────────┼───────────────────────┼────────────────────────────────────────┤
│ pgp-1      │ All Males             │ ca.personalgenomes.schemas.SubjectData │
│ pgp-2      │ All Females           │ ca.personalgenomes.schemas.SubjectData │
│ pgp-3      │ All Subjects over 40  │ ca.personalgenomes.schemas.SubjectData │
│ pgp-4      │ All Subjects under 40 │ ca.personalgenomes.schemas.SubjectData │
│ pgp-all    │ All Subjects          │ ca.personalgenomes.schemas.SubjectData │
│ subjects   │ Subject info          │ ca.personalgenomes.schemas.Subject     │
└────────────┴───────────────────────┴────────────────────────────────────────┘
```

### Get dataset

```
$ datasets get -I subjects
┌─────────┬────────────┬────────────┬─────┐
│ id      │ birth_date │ blood_type │ sex │
├─────────┼────────────┼────────────┼─────┤
│ PGPC-1  │ 1973-07    │            │ F   │
│ PGPC-10 │ 1963-11    │ A+         │ M   │
│ PGPC-11 │ 1963-05    │            │ F   │
│ PGPC-12 │ 1970-05    │ 0+         │ M   │
│ PGPC-13 │ 1953-07    │ 0+         │ F   │
│ PGPC-14 │ 1955-11    │ A-         │ F   │
│ PGPC-15 │ 1955-11    │ B+         │ F   │
│ PGPC-16 │ 1950-03    │ A+         │ M   │
│ PGPC-17 │ 1957-08    │ B+         │ M   │
│ PGPC-18 │ 1949-07    │ B+         │ F   │
│ PGPC-19 │ 1949-07    │            │ M   │
│ PGPC-2  │ 1974-10    │ A+         │ M   │

...

```

### Download dataset

You can download a snapshot of particular dataset to local filesystem like this:

```
$ datasets download -I subjects -o output
```

The directory `output` will then have the following structure:

```
output/
├── dataset
│   └── subjects
├── dataset-pages
│   └── subjects
│       ├── 1
│       ├── 2
│       ├── 3
│       ├── 4
│       └── 5
├── datasets
├── schema
│   ├── ca.personalgenomes.schemas.BloodType
│   ├── ca.personalgenomes.schemas.Sex
│   └── ca.personalgenomes.schemas.Subject
└── schemas
```

Where

- `datasets` is the dataset index, when creating a new `output` folder, this will contain only `subjects` dataset, you can download multiple datasets into one output folder. In such case subsequent datasets will be added to the index.
- `dataset/subjects` is JSON file containing the first page of the `subjects` dataset
- `dataset-pages` subsequent dataset pages. These are pointed to by the URLS inside of the `pagination` element.
- `schemas` is the schema index. Should contain all of the schemas used in the downloaded datasets
- `schema/{id}` are JSON files containing the schemas used in the downloaded datasets


## Release

Do the following to release and upload to Nexus

```
mvn release:prepare
mvn release:perform
```


# Dataset API Command-line Client

## Intro

**GA4GH Dataset API** is a subset of endpoints and schemas of **GA4GH Search API**, defined in [ga4gh-search-apis](https://github.com/DNAstack/ga4gh-search-apis) repository.


## Usage

Download [dataset-api-cli-0.1.0-executable.jar](https://nexus.dnastack.com/service/local/repositories/releases/content/org/ga4gh/dataset/dataset-api-cli/0.1.0/dataset-api-cli-0.1.0-executable.jar). Make it executable (e.g.
`chmod +x dataset-api-cli-0.1.0-executable.jar`)

### Set configuration to user config

```
$ ./dataset-api-cli-0.1.0-executable.jar set-config \
  --api-url https://pgp-dataset-service.staging.dnastack.com/data/v1 \
  --username yourname \
  --password yourpassword
```

This will store the config to your user path `~/.config/datasets/config.json`

### List datasets

```
$ ./dataset-api-cli-0.1.0-executable.jar list
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
$ ./dataset-api-cli-0.1.0-executable.jar get -I subjects
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

## Release

Do the following to release and upload to Nexus

```
mvn release:prepare
mvn release:perform
```


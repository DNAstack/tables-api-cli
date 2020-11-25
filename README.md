# Tables API Command-line Client

## Intro

**GA4GH Tables API** is a subset of endpoints and schemas of **GA4GH Search API**, defined in [ga4gh-search-apis](https://github.com/ga4gh-discovery/ga4gh-search) repository.


## Usage

### Installation

Download [tables-api-cli-executable.jar](). Make it executable (e.g.
`chmod +x tables-api-cli-executable.jar`)

Optionally create an executable `tables` script, with contents like this:

```bash
#!/bin/bash
/path/to/tables-api-cli-executable.jar $@
```

In all of the examples bellow we'll use `tables` shortcut instead of the full JAR name.

### Configuration

Initialize the configuration
```
$ tables config init
```

List The Config values to set and their current values
```
$ tables config list
```

Set a config value
```
$ tables config set <key> <value>
```

Delete a config value
```
$ tables config unset <key>
```

Get a specific config value
```
$ tables config get <key>
```

The config will be made available at `~/.config/tables/config.json`

### List table

```
$ tables list

┌────────────────┬─────────────────┐
│   Table Name   │   Description   │
├────────────────┼─────────────────┤
│subjects        │Generated table  │
└────────────────┴─────────────────┘
```

### Get table info

```
$ tables info subjects

┌────────────────┬──────────────────────────────────────┬────────────────┐
│   Table Name   │   Description                        │   Properties   │
├────────────────┼──────────────────────────────────────┼────────────────┤
│   subjects     │   Sample table describing subjects   │   birth_date   │
├────────────────┼──────────────────────────────────────┼────────────────┤
│                │                                      │   blood_type   │
├────────────────┼──────────────────────────────────────┼────────────────┤
│                │                                      │   id           │
├────────────────┼──────────────────────────────────────┼────────────────┤
│                │                                      │   sex          │
└────────────────┴──────────────────────────────────────┴────────────────┘
```
### Get table data

```
$ tables data subjects

┌────────────────┬────────────────┬─────────────┬─────────┐
│   birth_date   │   blood_type   │   id        │   sex   │
├────────────────┼────────────────┼─────────────┼─────────┤
│   1973-07      │                │   PGPC-1    │   F     │
│   1963-11      │   A+           │   PGPC-10   │   M     │
│   1963-05      │                │   PGPC-11   │   F     │
│   1970-05      │   0+           │   PGPC-12   │   M     │
│   1953-07      │   0+           │   PGPC-13   │   F     │
│   1955-11      │   A-           │   PGPC-14   │   F     │
│   1955-11      │   B+           │   PGPC-15   │   F     │
│   1950-03      │   A+           │   PGPC-16   │   M     │
│   1957-08      │   B+           │   PGPC-17   │   M     │
│   1949-07      │   B+           │   PGPC-18   │   F     │
│   1949-07      │                │   PGPC-19   │   M     │
│   1974-10      │   A+           │   PGPC-2    │   M     │
│   1953-08      │                │   PGPC-20   │   F     │
│   1960-07      │   0-           │   PGPC-21   │   M     │
│   1949-03      │   A+           │   PGPC-22   │   M     │
│                │   A+           │   PGPC-23   │   F     │
│                │                │   PGPC-24   │         │
│   1944-12      │                │   PGPC-25   │   M     │
│   1933-08      │   A+           │   PGPC-26   │   M     │

......
```

### Search

```
$ tables query -q "SELECT * FROM TABLE"


┌────────────────┬────────────────┬─────────────┬─────────┐
│   birth_date   │   blood_type   │   id        │   sex   │
├────────────────┼────────────────┼─────────────┼─────────┤
│   1973-07      │                │   PGPC-1    │   F     │
│   1963-11      │   A+           │   PGPC-10   │   M     │
│   1963-05      │                │   PGPC-11   │   F     │
│   1970-05      │   0+           │   PGPC-12   │   M     │
│   1953-07      │   0+           │   PGPC-13   │   F     │
│   1955-11      │   A-           │   PGPC-14   │   F     │
│   1955-11      │   B+           │   PGPC-15   │   F     │
│   1950-03      │   A+           │   PGPC-16   │   M     │
│   1957-08      │   B+           │   PGPC-17   │   M     │
│   1949-07      │   B+           │   PGPC-18   │   F     │
│   1949-07      │                │   PGPC-19   │   M     │
│   1974-10      │   A+           │   PGPC-2    │   M     │
│   1953-08      │                │   PGPC-20   │   F     │
│   1960-07      │   0-           │   PGPC-21   │   M     │
│   1949-03      │   A+           │   PGPC-22   │   M     │
│                │   A+           │   PGPC-23   │   F     │
│                │                │   PGPC-24   │         │
│   1944-12      │                │   PGPC-25   │   M     │
│   1933-08      │   A+           │   PGPC-26   │   M     │


```


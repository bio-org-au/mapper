#!/bin/bash

dropdb -U nsl --if-exists nsl-test &&
createdb -U nsl nsl-test &&
psql -U nsl -f ddl.sql nsl-test &&
psql -U nsl -f test-data.sql nsl-test

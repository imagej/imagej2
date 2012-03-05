#!/bin/bash

#
# trac-times.sh
#

# Fixes Trac timestamps that are off by a factor of a million.
# Must be run with appropriate permissions on the server side.

SCRIPT='update ticket_change set time = time * 1000000 where time < 9999999999;'
DB=$1
DB_PATH=/data/devel/trac/$DB/db/trac.db
if [ -e "$DB_PATH" ];
then
  echo "$SCRIPT" | sqlite3 $DB_PATH
else
  if [ "$DB" == "" ];
  then
    echo Please specify a Trac DB to fix.
  else
    echo No such Trac DB: $DB_PATH
  fi
  exit 1
fi


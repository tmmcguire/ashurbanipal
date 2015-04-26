Install and Configure PostgreSQL
================================

Basic instructions from
[How To Install and Use PostgreSQL on Ubuntu 14.04][]. See also [How To Secure PostgreSQL on an Ubuntu VPS][].

[How To Install and Use PostgreSQL on Ubuntu 14.04]: https://www.digitalocean.com/community/tutorials/how-to-install-and-use-postgresql-on-ubuntu-14-04

[How To Secure PostgreSQL on an Ubuntu VPS]: https://www.digitalocean.com/community/tutorials/how-to-secure-postgresql-on-an-ubuntu-vps


# Install PostgreSQL #

    $ sudo apt-get update
    $ sudo apt-get install postgresql postgresql-contrib

# As the system postgresql user... #

    $ sudo -i -u postgres

## ...Create a new Postgres user... ##

    $ createuser --interactive
    $ sudo adduser <user>  # to create the system account

Note: set database password here. If needed later, use the command:

    alter role <user> with encrypted password '<password>';

## ...Create a new database ##

    $ createdb <user>

# As the new user, log in #

    $ sudo -i -u <user>
    $ psql

# Create the database #

As the user, pipe the SQL script to psql.

    $ cat schema.sql | psql

# Read metadata #

Remove header line from `gutenberg.metadata` and use the following
psql command to read the metadata as-is:

    \copy book_metadata from 'gutenberg.metadata';

# Read parts of speech #

Use the following command to join the gutenberg.formats (to get the
etext_no from the file name) and the gutenberg.pos (with
parts-of-speech data) files into a temporary file.

    $ join -t"<tab>" -1 3 -2 1 \
      <(sort -t"<tab>" -k3 gutenberg.formats) \
      <(sort -t"<tab>" -k1 gutenberg.pos) | cut -f2,4- > dbgutenberg.pos

Then use the following psql command to read the parts-of-speech data
into the table.

    \copy parts_of_speech from 'dbgutenberg.pos';

# Read nouns table #

Use the following command to pick the nouns out of `gutenberg.nouns`
into a temporary file.

    $ awk 'BEGIN { FS="\t" } { for (i = 2; i < NF; ++i) { print $i; } }' \
      gutenberg.nouns \
      | sed 's/ .*//' | sort | uniq > dbnouns

Then use the following psql command to read the nouns table (using the
default id sequence value).

    \copy nouns (word) from 'dbnouns';

# Read topics table #

Use the following command to join the gutenberg.formats (for the
etext_no) and the gutenberg.nouns (with topic data) files into a
temporary file.

    $ join -t" " -1 3 -2 1 \
      <(sort -t"  " -k3 gutenberg.formats) \
      <(sort -t"   " -k1 gutenberg.nouns) \
      | cut -f2,4- > /tmp/gutenberg.nouns

Use the following command to convert the gutenberg.nouns file into a
collection of SQL insert statements.

    $ awk -f topics.awk /tmp/gutenberg.nouns > /tmp/nouns.inserts


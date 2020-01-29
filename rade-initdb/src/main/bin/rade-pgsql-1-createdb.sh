# This file is part of the Rade project (https://github.com/mgimpel/rade).
# Copyright (C) 2018 Marc Gimpel
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#
# $Id$

# Create a new Rade User and Database.

#. rade-db-init-parameters.sh
sudo -i -u postgres psql -c "CREATE USER $PG_USER WITH NOSUPERUSER NOCREATEDB NOCREATEROLE PASSWORD '$PG_PASSWORD';"
sudo -i -u postgres createdb -O $PG_USER $PG_DATABASE
#sed -i -e 's/^host/#host/g' /var/lib/pgsql/data/pg_hba.conf
#echo "host    $PG_DATABASE          $PG_USER        127.0.0.1/32            md5" >> /var/lib/pgsql/data/pg_hba.conf
#systemctl restart postgresql
 
#!/bin/bash

EXECUTABLE_FILE=linkchecker.jar
PROPERTIES_FILE=application-production.properties

HELP="Usage: linkchecker_linux [KEY]

Script without key is run program in DEMO mode. Also vailable next switches:

--debug - running program in DEMO mode with extended debug information.

--production - running program in PRODUCTION mode. For running in this mode needed additional
file application-production.properties with PostgreSQL dataset information. Also for this mode
available addition key --debug for runnning program with extended debug information.

--help - display this is message

Examples:

linkchecker_linux - run program in DEMO mode

linkchecker_linux --debug - run program in DEMO mode with extended debug information.

linkchecker_linux --production - run program in PRODUCTION mode.

linkchecker_linux --production --debug - run program in PRODUCTION mode with extended debug information.

For more information see https://gitlab.com/Aleksandrov/linkchecker/wikis/
"

PROPERTIES_FILE_NOT_FOUND="
WARNING!

You try run program in PRODUCTION mode. For this mode need PostgreSQL but file 
$PROPERTIES_FILE with dataset information is not found. Please fill next information and run program again!

"

if [ -f "$EXECUTABLE_FILE" ]; then
    if [ -z "$1" ]; then
        echo "Running program in DEMO mode"
        java -jar linkchecker.jar
    else
        case "$1" in
            --help)
                echo "$HELP"
            ;;
            --debug)
                echo "Running program in DEMO mode with extended debug information"
                java -jar linkchecker.jar --spring.profiles.active=demo,debug
            ;;
            --production)
                if [ -f "$PROPERTIES_FILE" ]; then
                    if [ -z "$2" ]; then
                        echo "Running program in PRODUCTION mode"
                        java -jar linkchecker.jar --spring.profiles.active=production
                    else
                        if [ "$2" = "--debug" ]; then
                            echo "Running program in PRODUCTION mode with extended debug information"
                            java -jar linkchecker.jar --spring.profiles.active=production,debug
                        else
                            echo "linkchecker: unknown option $2"
                            echo "Try 'linkchecker --help' for more information."
                        fi
                    fi
                else
                    echo "$PROPERTIES_FILE_NOT_FOUND"
                    printf 'PostgreSQL database host name or IP address (default localhost): '
                    read -r LINKCHECKER_PGSQL_DB_HOST
                    if [ -z "$LINKCHECKER_PGSQL_DB_HOST" ]; then
                        LINKCHECKER_PGSQL_DB_HOST="jdbc:postgresql://localhost"
                    else
                        LINKCHECKER_PGSQL_DB_HOST="jdbc:postgresql://$LINKCHECKER_PGSQL_DB_HOST"
                    fi
                    printf 'PostgreSQL database port (default 5432): '
                    read -r LINKCHECKER_PGSQL_DB_PORT
                    if [ -z "$LINKCHECKER_PGSQL_DB_PORT" ]; then
                        LINKCHECKER_PGSQL_DB_PORT=5432
                    fi
                    printf 'PostgreSQL database name (default linkchecker): '
                    read -r LINKCHECKER_PGSQL_DB_NAME
                    if [ -z "$LINKCHECKER_PGSQL_DB_NAME" ]; then
                        LINKCHECKER_PGSQL_DB_NAME="linkchecker"
                    fi
                    printf 'PostgreSQL database user name: '
                    read -r LINKCHECKER_PGSQL_DB_USER
                    printf 'PostgreSQL database password: '
                    read  -r -s LINKCHECKER_PGSQL_DB_PASSWORD
                    echo
                    touch "$PROPERTIES_FILE"
                    {
                      echo "LINKCHECKER_PGSQL_DB_HOST=$LINKCHECKER_PGSQL_DB_HOST"
                      echo "LINKCHECKER_PGSQL_DB_PORT=$LINKCHECKER_PGSQL_DB_PORT"
                      echo "LINKCHECKER_PGSQL_DB_NAME=$LINKCHECKER_PGSQL_DB_NAME"
                      echo "LINKCHECKER_PGSQL_DB_USER=$LINKCHECKER_PGSQL_DB_USER"
                      echo "LINKCHECKER_PGSQL_DB_PASSWORD=$LINKCHECKER_PGSQL_DB_PASSWORD"
                    } > "$PROPERTIES_FILE"
                fi
            ;;
            *)
                echo "linkchecker_linux: unknown option $1"
                echo "Try 'linkchecker_linux --help' for more information."
            ;;
        esac
    fi
else
    echo "Executable file linkchecker.jar is not found!"
fi
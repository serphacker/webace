
#!/usr/bin/env bash

DOCKER_OPTIONS=""

show_help() {
    echo "$0 --env <file.env> <docker-compose-command>"
}

# https://stackoverflow.com/questions/192249/how-do-i-parse-command-line-arguments-in-bash
POSITIONAL=()
while [[ $# -gt 0 ]]
do
key="$1"

case $key in
    -e|--env)
    ENVFILE="$2"
    shift # past argument
    shift # past value
    ;;
    --default)
    DEFAULT=YES
    shift # past argument
    ;;
    *)    # unknown option
    POSITIONAL+=("$1") # save it in an array for later
    shift # past argument
    ;;
esac
done
set -- "${POSITIONAL[@]}" # restore positional parameters

if [ "$ENVFILE" == "" ]; then
    show_help
    exit 1
fi

export $(cat $ENVFILE| xargs)
docker-compose -p $ENVFILE $@
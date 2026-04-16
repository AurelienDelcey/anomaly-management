#!/bin/bash

set -eu

cd "$(dirname "$(realpath "$0")")"

ROLE=""
NAME=""
ID=""

while [[ "$#" -gt 0 ]]
do
	case $1 in
		--role)ROLE="$2" 
		shift 
		;;
		--name)NAME="$2"
		shift
		;;
		--id)ID="$2"
		shift
		;;
	esac
	shift
done

if [[ -z "$ROLE"  || -z "$NAME" || -z "$ID" ]]
	then printf "\n⚠️ Missing argument(s): default actor will be used.\n"
fi

printf "\nUsed argument(s):\n"
printf "ROLE = %s\n" "$ROLE"
printf "NAME = %s\n" "$NAME"
printf "ID = %s\n\n" "$ID"

if lsof -i :3307 >/dev/null 2>&1; 
	then if ! docker ps | grep -q anomaly-db;
		then printf "❌ Port 3307 is already used.\n"
     		 printf "   Stop local MySQL or change docker-compose port.\n"
    		 exit 1
    fi
fi

docker compose up -d --build

printf "\n⌛ Starting database"
COUNT=0
I=0

until [[ "$COUNT" -ge 3 ]]; do 
	if [[ "$I" -eq 3 ]]
		then printf "\r\033[K⌛ Starting database"
		I=0
		else printf "."
		I=$((I+1))
	fi
	if docker exec anomaly-db mysql -uanomaly_user -panomaly_pass -e "USE anomaly; SELECT COUNT(*) FROM anomalies" >/dev/null 2>&1;
		then COUNT=$((COUNT+1))
		else COUNT=0
	fi
	sleep 1
done

printf "\r\033[K⌛ Starting database... ✅ Success!\n"

java -jar ./target/anomaly-management-1.0.0.jar --name="$NAME" --id="$ID" --role="$ROLE"

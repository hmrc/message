#!/bin/bash
GG_AUTH_PORT=8585
GG_AUTH_DATA=' { "credId": "1234", "affinityGroup": "Organisation", "confidenceLevel": 200, "credentialStrength": "strong", "nino": "AB234567C", "enrolments": [] }'
GG_AUTH_ENDPOINT='government-gateway/session/login'
raw_token=`curl "http://localhost:$GG_AUTH_PORT/$GG_AUTH_ENDPOINT" -d "$GG_AUTH_DATA" -H 'Content-type: application/json' 2>&1  -v | grep 'Authorization' | cut -f2- -d ' '`
echo ${raw_token%$'\r'}
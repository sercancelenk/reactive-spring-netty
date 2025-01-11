# Test max concurrent connections for 30s
wrk -t16 -c10000 -d30s -H "Accept: text/event-stream" http://localhost:8080/cold-numbers
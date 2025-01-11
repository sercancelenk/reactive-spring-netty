# Test 1000 requests with 50 concurrent connections
ab -n 1000 -c 50 -H "Accept: text/event-stream" http://localhost:8080/cold-numbers
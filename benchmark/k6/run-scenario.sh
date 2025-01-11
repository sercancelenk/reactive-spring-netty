# Run specific scenario
echo $1
k6 run -e SCENARIO="$1" load-test.js
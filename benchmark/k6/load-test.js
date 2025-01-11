import http from 'k6/http';
import { check, sleep } from 'k6';
import { htmlReport } from "https://raw.githubusercontent.com/benc-uk/k6-reporter/main/dist/bundle.js";

export const options = {
    scenarios: selectedScenarios(),
    thresholds: {
        http_req_duration: ['p(95)<2000'],
        http_req_failed: ['rate<0.01'],
    }
};

// Determine which scenarios to run based on environment variable
function selectedScenarios() {
    const scenario = __ENV.SCENARIO;

    const scenarios = {
        spike_test: {
            executor: 'ramping-vus',
            startVUs: 20,
            stages: [
                { duration: '10s', target: 0 },
                { duration: '20s', target: 3000 },
                { duration: '30s', target: 10000 },
                { duration: '20s', target: 0 },
            ],
        },
        peak_test: {
            executor: 'ramping-vus',
            startVUs: 0,
            stages: [
                { duration: '2m', target: 100 },
                { duration: '5m', target: 100 },
                { duration: '2m', target: 200 },
                { duration: '5m', target: 200 },
                { duration: '2m', target: 0 },
            ],
        }
    };

    // If scenario is specified, return only that scenario
    if (scenario && scenarios[scenario]) {
        return { [scenario]: scenarios[scenario] };
    }

    return scenarios;
}

export default function () {
    const response = http.get('http://localhost:8080/cold-numbers', {
        headers: {
            'Accept': 'text/event-stream',
        }
    });

    check(response, {
        'is status 200': (r) => r.status === 200,
        'is SSE content-type': (r) => r.headers['Content-Type'].includes('text/event-stream'),
    });

    sleep(3);
}

export function handleSummary(data) {
    const timestamp = new Date().toISOString().replace(/[:.]/g, '-');
    const scenario = __ENV.SCENARIO || 'all-scenarios';

    return {
        [`test_results/${timestamp}-${scenario}.html`]: htmlReport(data),
    };
}
import http from 'k6/http';
import { check, sleep } from 'k6';

const baseUrl = __ENV.BASE_URL || 'http://host.docker.internal:8080';
const docsUrl = `${baseUrl}/v3/api-docs`;
const adminPassword = 'ipcIpkFE9_OKFr9LSWNp1MYDK68P_NS4';
const virtualUsers = Number(__ENV.K6_VUS || 50);
const categoriesPerVu = Number(__ENV.K6_CATEGORIES_PER_VU || 10);
const holdSeconds = Number(__ENV.K6_HOLD_SECONDS || 0);

export const options = {
  scenarios: {
    admin_login_and_create_categories: {
      executor: 'per-vu-iterations',
      vus: virtualUsers,
      iterations: 1,
      maxDuration: '5m',
    },
  },
  thresholds: {
    checks: ['rate>0.99'],
    http_req_duration: ['p(95)<2000'],
  },
};

export function setup() {
  for (let attempt = 0; attempt < 30; attempt += 1) {
    const response = http.get(docsUrl, { timeout: '2s' });
    if (response.status === 200) {
      return;
    }
    sleep(2);
  }

  throw new Error('API did not become ready within 60 seconds');
}

export default function () {
  const loginResponse = http.post(
    `${baseUrl}/api/auth/login`,
    JSON.stringify({ login: 'admin', clave: adminPassword }),
    { headers: { 'Content-Type': 'application/json' } },
  );
  const loginOk = check(loginResponse, {
    'admin login returns 200': (response) => response.status === 200,
    'login response contains token': (response) => Boolean(response.json('token')),
  });

  if (!loginOk) {
    return;
  }

  const token = loginResponse.json('token');
  for (let categoryIndex = 1; categoryIndex <= categoriesPerVu; categoryIndex += 1) {
    const categoryName = `k6-${Date.now()}-vu-${__VU}-category-${categoryIndex}`;
    const response = http.post(
      `${baseUrl}/api/categorias`,
      JSON.stringify({ nombre: categoryName }),
      {
        headers: {
          'Content-Type': 'application/json',
          Authorization: `Bearer ${token}`,
        },
      },
    );

    check(response, {
      'category creation returns 200': (result) => result.status === 200,
    });
  }

  if (holdSeconds > 0) {
    sleep(holdSeconds);
  }
}
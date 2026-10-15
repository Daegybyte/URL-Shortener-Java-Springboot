import http from "k6/http";
import { check } from "k6";

// Usage: k6 run -e CODE=yourShortCode k6/redirect.js
export const options = {
  vus: 20,
  duration: "20s",
};

export default function () {
  // redirects: 0 stops k6 from following the 302 to the destination site
  const res = http.get(`http://localhost:8080/${__ENV.CODE}`, { redirects: 0 });
  check(res, { "is 302": (r) => r.status === 302 });
}

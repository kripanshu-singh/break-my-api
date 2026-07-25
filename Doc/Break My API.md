## What Every Piece Does — In Plain English

**Next.js Frontend** is the face. The user lands here, pastes their endpoint or uploads a Swagger JSON, clicks "Break It", and watches a live dashboard light up as tests fire in real time. It also renders the final shareable report — this is your viral moment. Clean, beautiful, screenshot-worthy output.

**Node.js / Express Backend** is the brain. It receives the user's input, calls the Claude API to generate adversarial payloads, dispatches jobs to BullMQ, manages the WebSocket connection that streams live results back to the frontend, and saves everything to PostgreSQL when done.

**Claude API (AI Engine)** is the magic. You send it the API schema or endpoint description and it returns 20 intelligent, targeted payloads — not random garbage. It knows to send `null` where a string is expected, `99999999` where a small number is expected, SQL injection strings, empty arrays, malformed JWTs. This is what makes your tool genuinely useful vs. a dumb fuzzer.

**BullMQ + Redis** is the queue. When a test job comes in, it doesn't execute immediately and block everything. It goes into a queue. Workers pick jobs off the queue and execute them. Redis stores the queue state and also handles your rate limiting per IP. This is the piece that screams "distributed systems" on your resume even though it's simple to implement.

**Worker Node** is the muscle. It picks a job off the BullMQ queue, takes the 20 AI-generated payloads, fires them all concurrently at the target endpoint using `Promise.all`, measures response times, detects errors, crashes, and timeouts, then sends results back via WebSocket and saves to PostgreSQL.

**PostgreSQL** stores every test run, its payloads, results, and the final report so users can share a permanent link.

---

## User Scenarios — Real Examples

**Scenario 1: The Solo Developer**

Arjun is building a fintech API. He has a `/transfer` endpoint that takes `userId`, `amount`, and `accountNumber`. He pastes the endpoint URL into BreakMyAPI with a sample request body. The AI generates payloads like: `amount: -99999`, `amount: 0.000001`, `amount: "'; DROP TABLE users; --"`, `accountNumber: null`, `userId: ""`, `amount: 9999999999999`. The worker fires all 20 concurrently. Three of them return 500 errors. One causes a 12-second timeout. The report shows Arjun exactly which payloads broke what, with HTTP status codes, response times, and a severity rating. He fixes the bugs before shipping to production. He screenshots the report and tweets it. That's your viral moment.

**Scenario 2: The Engineering Manager**

A senior engineer at a startup is evaluating whether to star your repo. She opens the README, sees "drop your Swagger file, get an adversarial test report in 60 seconds", tries it on her own staging API, sees real vulnerabilities caught. She stars it, shares it in her team's Slack. Her team stars it. This is how developer tools get 500 stars — not from consumers, from teams.

**Scenario 3: The Intern**

A Google intern wants to test the API they just built before their code review. They paste one endpoint, get a report in 90 seconds, fix the two edge cases it caught, and look like a rockstar in their PR. They post about it on LinkedIn. That post reaches 3,000 engineers.

---

## Phase-by-Phase Build Roadmap

## The Content Strategy Running In Parallel

This is as important as the build. You post every 3 days on LinkedIn and X, starting from Day 1 — not after you ship. The content builds anticipation and gets you followers before launch day.

**Day 1 post:** "I'm building a tool that lets AI try to break your API. Here's why I think every developer needs this. Building in public → follow along." No code yet. Just the idea. Gauge interest.

**Day 7 post:** "The AI generated this payload for a `/login` endpoint: `{'email': 'a'*10000, 'password': null}`. It crashed a real staging server in under 2 seconds. This is why I'm building BreakMyAPI." Show a screenshot of the raw output. Engineers will retweet this.

**Day 14 post:** Screen recording of the live WebSocket dashboard — payloads firing, results streaming in real time. No explanation needed. The visual does the work.

**Day 21 post:** "I added a shareable report card. Here's what BreakMyAPI found when I tested my own startup's API." Post the actual report from your CongKong API. Real data. Maximum authenticity.

**Day 28 post:** The launch thread. "I spent 4 weeks building BreakMyAPI. Here's everything I learned about API security, distributed job queues, and building in public. GitHub link below." This is your star spike moment.

---

## Your File Structure (So You Start Clean)

```
breakmyapi/
├── frontend/          ← Next.js app
│   ├── app/
│   │   ├── page.tsx          ← landing + input form
│   │   ├── report/[id]/      ← shareable report page
│   └── components/
│       ├── Dashboard.tsx     ← live results view
│       └── ReportCard.tsx    ← the viral shareable card
├── backend/           ← Node.js / Express
│   ├── routes/
│   │   ├── test.ts           ← POST /api/test
│   │   └── report.ts         ← GET /api/report/:id
│   ├── workers/
│   │   └── testWorker.ts     ← BullMQ worker
│   ├── ai/
│   │   └── generatePayloads.ts ← Claude API call
│   └── queue/
│       └── queue.ts          ← BullMQ setup
├── docker-compose.yml ← Redis + PostgreSQL local
└── README.md          ← your GitHub face
```

---

Start with Day 1–2 this weekend. Get the Next.js and Node skeleton deployed — even if it does nothing yet. Then make your first LinkedIn post announcing you're building it. The project and the content engine start simultaneously.

Want me to now write the exact Claude system prompt for payload generation, or the BullMQ worker code to get you started?
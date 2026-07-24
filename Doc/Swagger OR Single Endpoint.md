## The Two Input Modes — Simply Explained

**Mode 1: Single Endpoint** User pastes one URL + selects HTTP method + pastes a sample request body. That's it. You test that one endpoint.

**Mode 2: Swagger/OpenAPI file** User uploads their entire API spec. Your system reads it, extracts ALL endpoints automatically, and tests each one. The user doesn't tell you which to hit first — **the Swagger file already contains that information.**

Let me show you exactly what a Swagger file looks like and how you extract endpoints from it.---

## The Complete User Journey — Step by Step

Now let me walk you through exactly what the user sees and does, with a real example.

**Meet Rahul.** He's a backend developer at a fintech startup. He just built a payment API and wants to test it before shipping to production.---

## Your Question: "How Do I Know When to Hit Which API First?"

The honest answer is — **you don't need to know, and it doesn't matter.** Here's why:

You are not testing whether Rahul's APIs work together in sequence. You are not simulating a real user flow like "login first, get token, then transfer money." That would be an **integration test**, which is a completely different product.

You are doing **isolated security and resilience testing** on each endpoint. Does this endpoint crash when it receives bad input? Does it leak data? Does it return a 500 instead of a 400? Each endpoint is independent for this purpose.

Think of it like a doctor. They don't check your heart before your lungs because one affects the other. They check each organ independently to see if it's healthy on its own.

---

## The One Exception — Auth Dependency

The only real ordering problem is when an endpoint needs a token from another endpoint. Like: you must hit `POST /login` first to get a JWT, then use that JWT to hit `POST /transfer`.

You handle this with a simple **auth pre-step** in your UI:

```
Does your API require authentication?
○ No — test all endpoints freely
○ Yes — paste your staging token here: [____________]
○ Yes — my token comes from a login endpoint
    Login URL: [____________]
    Login body: [____________]
    Token field in response: [____________]  e.g. "data.token"
```

If they pick the third option, your backend hits the login endpoint first, extracts the token from the response using the field path they gave you, then injects it into every subsequent request as a header. That's 20 lines of code and covers 95% of real APIs.

This is actually a **great feature to add in Phase 4** — not phase 1. Start simple: ask the user to paste their token. The auto-login flow is a polish feature that makes your tool feel magical when you ship it as an upgrade.

---

## The Complete Mental Model in One Sentence

**Swagger file = your menu of what to test. Base URL + token = how to reach it. BullMQ = the kitchen that processes each item on the menu. Worker = the chef. WebSocket = the waiter bringing food to the table as it's ready.**

That's the entire system. Now you know exactly what to build. Want to start with the Swagger parser code, or the UI input form first?
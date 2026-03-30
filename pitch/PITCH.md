# I Build E-Commerce Apps That Don't Break When the Business Changes

**TL;DR:** 10 years in mobile, 6 in e-commerce. I build systems
that scale without growing complexity. Android + iOS, CI/CD,
automated quality, full documentation.
[See the proof →](https://github.com/satanyakiv/NutriSport)

---

## NutriSport — Proof of Concept

Everything above — the modularity, the automation, the documentation, the testing discipline — I applied deliberately to one project. As proof that these principles work together at scale.

| Android                                     | iOS                                 |
| ------------------------------------------- | ----------------------------------- |
| ![Android Demo](../assets/demo-android.gif) | ![iOS Demo](../assets/demo-ios.gif) |

<!-- TODO: replace with real GIFs after recording -->
<!-- Flow: Auth → Home → Add to Cart → Search → Product Details → Add to Cart → Checkout -->

- **25 independent modules** — features are built without affecting each other. A change to the cart doesn't recompile the product catalog.
- **Android and iOS from one codebase** — market expansion without doubling the team or maintaining two separate apps.
- **Offline-first shopping cart** — orders complete even on a bad connection. Prices are validated before checkout so users never pay a stale amount.
- **169 automated tests** — changes don't break payments silently. The full test suite runs in 30 seconds, no device or emulator needed.
- **4 automated pipelines** — from pull request validation to app store release. No manual builds, no human error in deploys.
- **8 documentation files** — covering CI/CD, testing strategy, security audit, performance, offline architecture, crash reporting. A new developer is productive in under an hour.
- **AI-enforced architecture standards** — rules, agents, and automated checks that ensure every contributor — human or AI — follows the same engineering standards.

Every engineering decision in this project has a documented reason.

**Want to see it running?** [Email me](mailto:yakiv.bondar@gmail.com) — I'll add your Google account to the testers list and send a debug APK.

[View the source on GitHub](https://github.com/satanyakiv/NutriSport)

---

[Why e-commerce is hard](#i-build-e-commerce-apps-that-dont-break-when-the-business-changes) ·
[What I do](#what-i-actually-do) ·
[Three stories](#three-stories) ·
[The proof](#nutrisport--proof-of-concept) ·
[Let's talk](#lets-talk)

E-commerce projects don't die from missing features. They die because every next feature costs more than the previous one. I build systems to prevent that.

---

It's Black Friday. Fifty thousand users are on the app. At 23:58, checkout crashes. The team scrambles, but the fix takes four hours — the payment code is tangled with the cart, which is tangled with the promo engine. Revenue lost overnight: enough to fund a developer for a quarter.

Or this: marketing changes a promo rule at 2 AM. Nobody checks. The cart silently miscalculates totals for six hours before a customer complains on Twitter.

Or this: a new developer joins the team, touches the payment flow, and breaks something. Nobody remembers why it was built that way. There are no tests, no documentation, no guardrails.

I've spent 10 years making sure this doesn't happen.

---

## What I Actually Do

I'm a mobile engineer. But when people ask what I do, "I write code" isn't the honest answer. The honest answer is: I make sure the codebase doesn't fight the business.

| Business problem                               | What I do about it                                                                            |
| ---------------------------------------------- | --------------------------------------------------------------------------------------------- |
| Every new feature takes longer than the last   | Modular architecture — features are built in isolation, so changing one doesn't break another |
| Expanding to new markets takes months          | Shared codebase for Android and iOS — one change ships to both platforms                      |
| Payment issues cost real money                 | Offline-first design + two-step checkout validation + automated tests on every commit         |
| Nobody remembers why a decision was made       | Architecture documentation, decision records, onboarding guides — on every project            |
| New developer takes weeks to become productive | Written conventions, automated style checks, templates — the codebase teaches itself          |
| Manual builds, broken deploys                  | Automated pipelines — from commit to app store, no human steps in between                     |

---

## Three Stories

### The Jenkins Handover

At a large outsource company, one person maintained the entire CI system. They were the only one who understood the build scripts, the server configuration, the deployment flow. When they left — suddenly, with two days' notice — the builds stopped.

I took it over in three days. Not by becoming the next single point of failure, but by doing the opposite: I refactored the setup into something any developer could understand, wrote documentation for every pipeline, created templates for new projects, and trained four developers on how to maintain it themselves.

Within a month, four developers could independently set up and maintain their own CI pipelines. The system no longer depended on any one person. That was the point — not to be the hero, but to build something that doesn't need one.

### Nobody Asked

At a grocery delivery startup, builds were dropped manually into a Slack channel. Each developer signed the app with their own certificate — which meant testers had to reinstall the app every time they got a new build. It was slow, error-prone, and everybody had accepted it as "just how things work."

Nobody was bothered. I was. I identified the DevOps contact, made the case in a single conversation — no long proposals, no committee meetings — and we had proper automated CI running within the sprint. No extra budget. No dedicated time. Just one engineer who decided this was worth fixing.

The testers stopped reinstalling. The developers stopped uploading to Slack. The builds just worked.

### 100 Minutes Returned Per Day

On a subscription e-commerce product, the CI build took 5 minutes. That sounds reasonable — until you multiply it: 5 developers, 10 builds a day each, every day. That's over four hours of collective waiting, daily.

I brought it down to 2 minutes 30 seconds through a series of targeted changes: parallel test execution, a Gradle and build tools upgrade, incremental builds, configuration cache, and restructuring modules so they compiled independently. Then I migrated the dependency injection framework from one that slowed compilation to one that didn't — and added a consistency check so the migration couldn't introduce regressions.

5 developers multiplied by 10 builds multiplied by 2.5 minutes saved — that's 125 minutes returned to actual work, every single day. Not a feature. Not a redesign. An engineering decision that paid for itself in the first week.

---

## What Changes When I Join

Code quality becomes consistent — not because everyone agrees on style, but because the tools enforce it automatically regardless of team size. Technical debt gets communicated early, before it becomes a crisis that delays the roadmap. New team members onboard faster because conventions are written down, not tribal knowledge. Deploys become boring — which is exactly what a deploy should be.

---

## Let's Talk

If your team is building or scaling a mobile e-commerce product and needs someone who thinks about systems, not just features — let's talk.

[Email](mailto:yakiv.bondar@gmail.com) · [LinkedIn](https://www.linkedin.com/in/yakivbondar) · [GitHub](https://github.com/satanyakiv/NutriSport)

# I Build E-Commerce Apps That Don't Break When the Business Changes

**TL;DR:** 10 years in mobile, 6 in e-commerce. Android + iOS from
one codebase, CI/CD, automated testing, docs for everything.
[See the proof →](https://github.com/satanyakiv/NutriSport)

---

## NutriSport — Proof of Concept

I applied all of this to one project deliberately: the modularity, the automation, the documentation, the testing discipline. Proof that these principles work together at scale.

| Android                                     | iOS                                 |
| ------------------------------------------- | ----------------------------------- |
| ![Android Demo](../assets/demo-android.gif) | ![iOS Demo](../assets/demo-ios.gif) |

- **25 independent modules.** Features don't affect each other. Change to cart doesn't recompile product catalog.
- Android and iOS from one codebase. No need to double the team or maintain two apps.
- **Offline-first shopping cart.** Orders complete even on a bad connection. Prices are validated before checkout so users never pay a stale amount.
- **169 automated tests.** Changes don't break payments silently. Full suite runs in 30 seconds, no device needed.
- 4 automated pipelines from PR validation to app store release. No manual builds.
- **8 documentation files** covering CI/CD, testing, security, performance, offline architecture, crash reporting. New developer is productive in under an hour.
- AI-enforced architecture standards: rules, agents, automated checks. Every contributor (human or AI) follows the same engineering standards.

Every engineering decision in this project has a documented reason.

**Want to see it running?** [Email me](mailto:yakiv.bondar@gmail.com). I'll add your Google account to the testers list and send a debug APK.

[View the source on GitHub](https://github.com/satanyakiv/NutriSport)

---

[Why e-commerce is hard](#i-build-e-commerce-apps-that-dont-break-when-the-business-changes) ·
[What I do](#what-i-actually-do) ·
[Three stories](#three-stories) ·
[The proof](#nutrisport--proof-of-concept) ·
[Let's talk](#lets-talk)

E-commerce projects don't die from missing features. They die because every next feature costs more than the previous one. I build systems to prevent that.

---

It's Black Friday. Fifty thousand users are on the app. At 23:58, checkout crashes. The team scrambles, but the fix takes four hours. The payment code is tangled with the cart, which is tangled with the promo engine. Revenue lost overnight: enough to fund a developer for a quarter.

Or this: marketing changes a promo rule at 2 AM. Nobody checks. The cart silently miscalculates totals for six hours before a customer complains on Twitter.

Or this: a new developer joins the team, touches the payment flow, and breaks something. Nobody remembers why it was built that way. There are no tests, no documentation, no guardrails.

I've spent 10 years making sure this doesn't happen.

---

## What I Actually Do

I'm a mobile engineer. But when people ask what I do, "I write code" isn't the honest answer. The honest answer is: I make sure the codebase doesn't fight the business.

| Business problem                               | What I do about it                                                                    |
| ---------------------------------------------- | ------------------------------------------------------------------------------------- |
| Every new feature takes longer than the last   | Modular architecture. Features built in isolation, changing one doesn't break another |
| Expanding to new markets takes months          | Shared codebase for Android and iOS. One change ships to both platforms               |
| Payment issues cost real money                 | Offline-first design + two-step checkout validation + automated tests on every commit |
| Nobody remembers why a decision was made       | Architecture docs, decision records, onboarding guides. On every project              |
| New developer takes weeks to become productive | Written conventions, automated checks, templates. The codebase teaches itself         |
| Manual builds, broken deploys                  | Automated pipelines from commit to app store. No human steps                          |

---

## Three Stories

### The Jenkins Handover

At a large outsource company, one person maintained the entire CI system. They were the only one who understood the build scripts, server config, deployment flow, release signing. When they left with two days' notice, the builds stopped.

I took it over in three days. Refactored the setup so any developer could understand it, wrote docs for every pipeline, created templates, trained four developers to maintain it.

Within a month, four developers could independently set up and maintain their own CI pipelines. The system no longer depended on any one person. That was the point: build something that doesn't need a hero.

### Nobody Asked

At a grocery delivery startup, builds were dropped manually into a Slack channel. Each developer signed the app with their own certificate, so testers had to reinstall every time they got a new build. It was slow, error-prone, and everybody had accepted it as "just how things work."

Nobody was bothered. I was. Talked to the DevOps person, made the case in one conversation. No proposals, no meetings. We had automated CI running within the sprint. No extra budget. Just one engineer who decided this was worth fixing.

The testers stopped reinstalling. The developers stopped uploading to Slack. The builds just worked.

### 100 Minutes Returned Per Day

On a subscription e-commerce product, the CI build took 5 minutes. Sounds reasonable until you multiply it. 5 developers, 10 builds a day each. Four hours of waiting, daily.

I brought it down to 2 minutes 30 seconds. Parallel test execution, Gradle upgrade, incremental builds, configuration cache, module restructuring for independent compilation. Then migrated the DI framework from one that slowed compilation to one that didn't. Added a consistency check so the migration couldn't break anything.

5 developers × 10 builds × 2.5 minutes saved = 125 minutes back, every day. Not a feature. Not a redesign. An engineering decision that paid for itself in the first week.

---

## What Changes When I Join

The tools enforce code quality automatically, regardless of team size; nobody needs to agree on style. Technical debt gets communicated early, before it becomes a crisis that delays the roadmap. New team members onboard faster because conventions are written down instead of tribal knowledge. Deploys become boring. That's exactly what a deploy should be.

---

## Let's Talk

If your team is building or scaling a mobile e-commerce product and needs someone who thinks about systems as much as features, let's talk.

[Email](mailto:yakiv.bondar@gmail.com) · [LinkedIn](https://www.linkedin.com/in/yakivbondar) · [GitHub](https://github.com/satanyakiv/NutriSport)

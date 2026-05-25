---
name: r8-analyzer
description: Аналізує Android build-файли і ProGuard/R8 keep rules проєкту NutriSport — знаходить redundant broad rules, рекомендує точкові заміни, перевіряє що конфігурація мініфікації відповідає Google best practices. Викликати коли додається нова бібліотека, після падіння release, перед релізом, або явним `/skill r8-analyzer`.
---

# r8-analyzer

Адаптовано з офіційного [android/skills `performance/r8-analyzer`](https://github.com/android/skills/tree/main/performance/r8-analyzer) (Apache License 2.0). Порт для Claude Code з невеликими адаптаціями під workflow NutriSport.

## Коли запускати

- Після додавання нової бібліотеки з native/reflection кодом → перевір чи треба нові keep rules
- Якщо `:androidApp:assembleRelease` впав з `Missing class …` або `NoClassDefFoundError`
- Якщо runtime release крашиться на `@Serializable` декодуванні, DI resolve, Room, чи reflection-heavy call-site
- Перед кожним релізом — audit конфігурації
- Явний виклик користувачем

## Проєктний контекст (NutriSport)

- **R8 fullMode**: увімкнено через AGP 9.1.0 (дефолт, не змінюємо)
- **Мініфікація**: `release` має `isMinifyEnabled = true` + `isShrinkResources = true` + `proguard-android-optimize.txt` + `proguard-rules.pro` (build type `debug` — без мініфікації)
- **Стек із reflection-чутливими бібліотеками**: Room (KMP), kotlinx.serialization (Screen routes + DTOs), Ktor, Koin, Coil, Napier, dev.gitlive.firebase (KMP-обгортка) + Google Firebase BoM (Crashlytics, common), KMPAuth. Немає Gson/Retrofit.
- **`proguard-rules.pro` НЕ мінімальний** — містить keep rules для Room, kotlinx.serialization (`$$serializer`), dev.gitlive.firebase, KMPAuth, Ktor engine, Koin core/mp + `-dontwarn` для Compose/Coil/Napier

## Workflow (з оригінального SKILL.md Google)

- [ ] **Step 1** — Створи файл `R8_Configuration_Analysis.md` у корені проєкту (або онови якщо існує)
- [ ] **Step 2** — Перевір конфігурацію R8 у `androidApp/build.gradle.kts`, root `build.gradle.kts`, `gradle.properties`. Орієнтир: [`references/CONFIGURATION.md`](references/CONFIGURATION.md). Збережи секцію в репорт.
- [ ] **Step 3** — Якщо AGP < 9, рекомендуй апгрейд (`references/android/topic/performance/app-optimization/enable-app-optimization.md`). У нас вже 9.1.0 — skip.
- [ ] **Step 4** — Прогляньте `androidApp/proguard-rules.pro` + consumer rules бібліотек. Для кожного правила:
  - 4a) **Library check** — правило в списку [`references/REDUNDANT-RULES.md`](references/REDUNDANT-RULES.md)? → пропонувати видалити
  - 4b) **Impact analysis** — залишкові rules оцінювати по [`references/KEEP-RULES-IMPACT-HIERARCHY.md`](references/KEEP-RULES-IMPACT-HIERARCHY.md)
- [ ] **Step 5** — Знайди subsuming rules (одне правило перекриває інше) по impact hierarchy → пропонувати видалити ширші
- [ ] **Step 6** — Для кожного залишкового правила: прочитай код що воно покриває + суміжні файли, шукай reflection usages, пропонуй narrow rule ([`references/REFLECTION-GUIDE.md`](references/REFLECTION-GUIDE.md))
- [ ] **Step 7** — Для кожного правила визначити: залишити / звузити / видалити (з обґрунтуванням)
- [ ] **Step 8** — Відсортувати знахідки по impact hierarchy
- [ ] **Step 9** — Порекомендуй запустити smoke-тести на `release` APK перед мержем

## Mandatory rules (з Google оригіналу)

- Ніколи не змінюй `.pro` файли прямо — тільки пропонуй зміни в репорті
- Не вказуй level кожного keep rule
- Не генеруй секції звіту якщо там нема що репортити
- Не згадуй внутрішні файли скіла в репорті
- Не описуй переваги R8
- Не згадуй exceptions які виникли під час виконання

## Критичні файли для NutriSport

```
androidApp/build.gradle.kts            # buildTypes { debug, release }
androidApp/proguard-rules.pro          # поточні keep rules
gradle.properties                      # R8 fullMode flags (не має бути enableR8.fullMode=false)
```

## Audit checkpoint — Firebase ComponentRegistrar під R8 full-mode

NutriSport використовує Google Firebase Crashlytics (`firebase-crashlytics` через BoM) разом із R8 full-mode. Це відоме поєднання, де R8 full-mode може вирізати no-arg конструктори класів, що реалізують `com.google.firebase.components.ComponentRegistrar`, бо Firebase інстанціює реєстратори рефлективно через Java SPI loader при старті `FirebaseInitProvider`.

- **Симптом**: Crashlytics не ініціалізується в release, **production crashes silently dropped** — нічого в Console, попри робочий debug.
- **Keep rule**: `-keep class * implements com.google.firebase.components.ComponentRegistrar { <init>(); }`
- **Consumer rules gap**: Firebase BoM НЕ постачає це правило через consumer ProGuard files — його треба прописувати в проектному `proguard-rules.pro` вручну.
- **Coverage**: wildcard `* implements ComponentRegistrar` покриває майбутні Firebase модулі (Performance, Remote Config, App Check) без додаткових правок.
- **Audit signal**: Step 4 НЕ повинен пропонувати видалити це правило, якщо воно присутнє (false positive). Step 6 має підтвердити SPI usage у Firebase SDK і залишити його. Якщо правило **відсутнє** — рекомендуй додати й верифікувати через signed release APK + контрольований crash → перевірити що issue зʼявляється в Crashlytics Console із resolved stack trace через auto-uploaded R8 mapping.

## Атрибуція

Цей скіл — порт з [google/android-skills](https://github.com/android/skills), ліцензовано під Apache License 2.0. Референсні файли `references/**/*.md` скопійовані verbatim з оригіналу. Оригінальні права — Google LLC.

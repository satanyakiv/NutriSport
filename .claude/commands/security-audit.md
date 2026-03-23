Read .claude/agents/security-kotlin.md

## OWASP Mobile Security Audit

$ARGUMENTS

Launch the `security-kotlin` agent to perform a full OWASP Mobile Top-10 security audit of the NutriSport project.

Use the Agent tool with subagent_type="general-purpose" to spawn the security audit agent. Pass the full contents of `.claude/agents/security-kotlin.md` as instructions.

The agent must:

1. Follow all 11 steps from the audit process in security-kotlin.md
2. Scan all modules: domain, network, database, shared/utils, shared/ui, feature/\*, androidApp, analytics
3. Produce a structured severity report (Critical → Info) with OWASP mapping
4. Mask any real secret values in the output (show only first 4 chars + `...XXXX`)
5. **Never modify files** — report only, patches as diffs in the report

If `$ARGUMENTS` specifies a category (e.g., "M1", "M3", "secrets"), limit the audit to that category only.

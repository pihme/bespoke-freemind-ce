# BESPOKE.md

This project is a tailored version of an upstream open-source project, built with the [Bespoke assembly line](https://github.com/pihme/bespoke/blob/main/ASSEMBLY-LINE.md). Each stage has its own branch (`1-raw` … `6-bespoke`) and one chapter here. Decisions are recorded here first; a stage that outgrows its chapter moves the details to `bespoke/<n>-<stage>.md` and links it.

## Source

- Upstream repo: https://github.com/Denomas/freemind-ce
- Following: main
- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f

## Vision / North Star

A modernized and streamlined version of Freemind. Modernized pertains to the UI. Streamlined points at limiting the feature set to the core of mind map modeling.

## Constraints / Do not touch

- Stay in Java.
- Keep the license from upstream.
- Work hard to keep the file format compatible. If that is not possible, add an importer for freemind-ce files.

## Spec

- Feature spec: <path, e.g. docs/<project>-spec.md>; testspec: <path>
- Upstream spec: <none / path> – <usable as is / usable with changes / not usable, and why>

## 1 raw

- Upstream commit: 88075b1ed47f8e58d813775e41836e08d0681f1f
- Built from: upstream
- Skill: fork
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/1-raw.md)

### Inventory

- Languages and build system:
- Build:
- Tests and result:
- License:
- Direct dependencies:
- Shipped artifacts and size:

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| RAW-001 | Fork Denomas/freemind-ce as public pihme/bespoke-freemind-ce, copying only the default branch `main` at 88075b1ed47f8e58d813775e41836e08d0681f1f, and start the raw stage on `1-raw` at that commit. | The user asked for that repo name, that upstream, and only the main branch. | user | `gh repo fork Denomas/freemind-ce --fork-name bespoke-freemind-ce --default-branch-only`, then `1-raw` at the same commit. |

### Last run

- Date, result (passed / failed at decision ID), notes

## 2 covered

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 1-raw@<sha>
- Skill: increase-test-coverage
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/2-covered.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| COV-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

## 3 tailored

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 2-covered@<sha>
- Skill: remove-features
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/3-tailored.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| TAI-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

## 4 customized

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 3-tailored@<sha>
- Skill: customize-features
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/4-customized.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| CUS-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

## 5 trimmed

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 4-customized@<sha>
- Skill: trim
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/5-trimmed.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| TRM-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

## 6 bespoke

- Upstream commit: <sha of 1-raw this run started from>
- Built from: 5-trimmed@<sha>
- Skill: shrink-wrap
- Gate: <command(s) that must pass at the end of this stage>
- Details: none (or bespoke/6-bespoke.md)

### Decisions

| ID | Decision | Reason | Decided by | Replay hint |
|----|----------|--------|------------|-------------|
| BSP-001 | | | user / agent | |

### Last run

- Date, result (passed / failed at decision ID), notes

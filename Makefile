# FreeMind CE - Development Makefile
# Cross-platform: macOS (Homebrew/MacPorts), Linux (apt/dnf/sdkman), Windows (WSL/Git Bash)
# Run 'make help' to see all available targets.

# Reference environment is flake.nix. Gradle runs inside that dev shell.
GRADLE       := ./gradlew
GRADLE_FLAGS := --no-configuration-cache
GRADLE_CMD   = env -u JAVA_HOME nix develop -c $(GRADLE)

.PHONY: help build run debug clean test test-gui test-performance test-fuzz mutate coverage jaxb javadoc \
        package package-mac package-win package-linux \
        dist-zip install-dist check audit audit-full info all

.DEFAULT_GOAL := help

# ── General ──────────────────────────────────────────────────────────
##@ General

help: ## Show this help message
	@awk 'BEGIN {FS = ":.*##"; printf "\nUsage:\n  make \033[36m<target>\033[0m\n"} \
		/^[a-zA-Z_-]+:.*?##/ { printf "  \033[36m%-20s\033[0m %s\n", $$1, $$2 } \
		/^##@/ { printf "\n\033[1m%s\033[0m\n", substr($$0, 5) } ' $(MAKEFILE_LIST)

info: ## Show the reference environment
	@echo "OS         = $$(uname -s 2>/dev/null || echo Windows) $$(uname -m 2>/dev/null)"
	@env -u JAVA_HOME nix develop -c java -version
	@echo "Gradle     = $$($(GRADLE_CMD) --version $(GRADLE_FLAGS) 2>/dev/null | grep '^Gradle' || echo 'run make build first')"

# ── Development ──────────────────────────────────────────────────────
##@ Development

build: ## Compile and package, without tests
	$(GRADLE_CMD) assemble $(GRADLE_FLAGS)

run: ## Run FreeMind CE
	$(GRADLE_CMD) run $(GRADLE_FLAGS)

debug: ## Run in debug mode (attach debugger on port 5005)
	$(GRADLE_CMD) run $(GRADLE_FLAGS) --debug-jvm

clean: ## Clean all build artifacts
	$(GRADLE_CMD) clean $(GRADLE_FLAGS)

test: ## Run tests only
	$(GRADLE_CMD) test $(GRADLE_FLAGS)

test-gui: ## Run GUI tests with screenshots (requires display)
	$(GRADLE_CMD) testGui $(GRADLE_FLAGS)

test-performance: ## Run performance tests
	$(GRADLE_CMD) testPerformance $(GRADLE_FLAGS)

test-fuzz: ## Run jqwik and Jazzer tests (not part of unit tests or coverage)
	$(GRADLE_CMD) testFuzz $(GRADLE_FLAGS)

mutate: ## Mutation-test one package (override MUTATE and MUTATE_TESTS)
	$(GRADLE_CMD) pitest $(GRADLE_FLAGS) \
		$(if $(MUTATE),-PmutateClasses=$(MUTATE),) \
		$(if $(MUTATE_TESTS),-PmutateTests=$(MUTATE_TESTS),)

coverage: ## Run tests with JaCoCo coverage report
	$(GRADLE_CMD) test jacocoTestReport $(GRADLE_FLAGS)
	@echo "Coverage report: build/reports/jacoco/test/html/index.html"

coverage-report: ## Print JaCoCo totals from exec files already on disk
	@if ! ls build/jacoco/*.exec >/dev/null 2>&1; then \
		echo "No exec files in build/jacoco. Run the test tasks first."; \
		exit 1; \
	fi
	$(GRADLE_CMD) jacocoTestReport -x test -x testGui -x testPerformance $(GRADLE_FLAGS)
	@python3 -c 'import xml.etree.ElementTree as ET; root = ET.parse("build/reports/jacoco/test/jacocoTestReport.xml").getroot(); counters = {c.get("type"): c for c in root.findall("counter")}; \
order = ("INSTRUCTION", "LINE", "BRANCH"); \
[print("%s: %.1f%% (%s of %s)" % (name, 100.0 * int(counters[name].get("covered")) / (int(counters[name].get("covered")) + int(counters[name].get("missed"))), format(int(counters[name].get("covered")), ","), format(int(counters[name].get("covered")) + int(counters[name].get("missed")), ","))) for name in order]'
	@echo "Coverage report: build/reports/jacoco/test/html/index.html"

check: ## Run build + all quality checks
	$(GRADLE_CMD) check $(GRADLE_FLAGS)

# ── Security ────────────────────────────────────────────────────────
##@ Security

audit: ## Run security audit — Grype (fast, ~30s, fails on High+)
	@command -v grype >/dev/null 2>&1 || { echo "Error: grype not found. Install: mise install grype"; exit 1; }
	grype dir:build --only-fixed --fail-on high
	@echo ""
	@echo "Full OWASP report: make audit-full"

audit-full: ## Run full OWASP dependency-check (slower, detailed HTML report)
	$(GRADLE_CMD) dependencyCheckAnalyze $(GRADLE_FLAGS)
	@echo ""
	@echo "Report: build/reports/dependency-check-report.html"

##@ Code Generation

jaxb: ## Regenerate JAXB classes from XSD schema
	$(GRADLE_CMD) generateJaxb $(GRADLE_FLAGS)

javadoc: ## Generate API documentation
	$(GRADLE_CMD) javadoc $(GRADLE_FLAGS)
	@echo "Javadoc: build/docs/javadoc/index.html"

# ── Packaging ────────────────────────────────────────────────────────
##@ Packaging

package: ## Build native package for current OS
	@case "$$(uname -s 2>/dev/null)" in \
		Darwin) $(MAKE) package-mac ;; \
		Linux)  $(MAKE) package-linux ;; \
		*)      echo "Auto-detect failed. Use: make package-mac|package-win|package-linux" ;; \
	esac

package-mac: ## Build macOS DMG package
	$(GRADLE_CMD) jpackageMac $(GRADLE_FLAGS)

package-win: ## Build Windows EXE installer
	$(GRADLE_CMD) jpackageWin $(GRADLE_FLAGS)

package-linux: ## Build Linux DEB package
	$(GRADLE_CMD) jpackageLinux $(GRADLE_FLAGS)

dist-zip: ## Create distribution ZIP archive
	$(GRADLE_CMD) distZip $(GRADLE_FLAGS)

install-dist: ## Create local distribution layout
	$(GRADLE_CMD) installDist $(GRADLE_FLAGS)

# ── Aliases ──────────────────────────────────────────────────────────
##@ Aliases

all: build ## Alias for build

#!/usr/bin/env bash

# ABOUTME: Script to build and test all Ktor sample projects
# ABOUTME: Iterates through each sample directory and runs gradle or maven build and test tasks

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Array to track results
declare -a PASSED_PROJECTS=()
declare -a FAILED_PROJECTS=()
declare -a SKIPPED_PROJECTS=()

# Projects to skip (e.g., special cases)
SKIP_PROJECTS=(
    ".git"
    ".github"
    ".idea"
)

should_skip_project() {
    local project=$1
    for skip in "${SKIP_PROJECTS[@]}"; do
        if [[ "$project" == "$skip" ]]; then
            return 0
        fi
    done
    return 1
}

echo -e "${BLUE}==================================================================${NC}"
echo -e "${BLUE}Building and Testing All Ktor Samples${NC}"
echo -e "${BLUE}==================================================================${NC}"
echo ""

# Find all directories with build.gradle.kts, build.gradle, or pom.xml
for project_dir in */; do
    project_name="${project_dir%/}"

    # Skip excluded projects
    if should_skip_project "$project_name"; then
        echo -e "${YELLOW}⊘ Skipping: $project_name${NC}"
        SKIPPED_PROJECTS+=("$project_name")
        continue
    fi

    # Determine build system
    if [[ -f "$project_dir/pom.xml" ]]; then
        BUILD_SYSTEM="maven"
    elif [[ -f "$project_dir/build.gradle.kts" ]] || [[ -f "$project_dir/build.gradle" ]]; then
        BUILD_SYSTEM="gradle"
    else
        echo -e "${YELLOW}⊘ Skipping $project_name (no build.gradle.kts, build.gradle, or pom.xml)${NC}"
        SKIPPED_PROJECTS+=("$project_name")
        continue
    fi

    echo -e "${BLUE}------------------------------------------------------------------${NC}"
    echo -e "${BLUE}Building: $project_name (${BUILD_SYSTEM})${NC}"
    echo -e "${BLUE}------------------------------------------------------------------${NC}"

    cd "$project_dir"

    # Run the build based on the build system
    if [[ "$BUILD_SYSTEM" == "maven" ]]; then
        if [[ -x "./mvnw" ]]; then
            BUILD_COMMAND="./mvnw clean test"
        else
            BUILD_COMMAND="mvn clean test"
        fi
    else
        BUILD_COMMAND="./gradlew clean check --console=plain"
    fi

    if $BUILD_COMMAND 2>&1; then
        echo -e "${GREEN}✓ SUCCESS: $project_name${NC}"
        PASSED_PROJECTS+=("$project_name")
    else
        echo -e "${RED}✗ FAILED: $project_name${NC}"
        FAILED_PROJECTS+=("$project_name")
    fi

    cd "$SCRIPT_DIR"
    echo ""
done

# Print summary
echo -e "${BLUE}==================================================================${NC}"
echo -e "${BLUE}Build Summary${NC}"
echo -e "${BLUE}==================================================================${NC}"
echo ""

echo -e "${GREEN}Passed (${#PASSED_PROJECTS[@]}):${NC}"
for project in "${PASSED_PROJECTS[@]}"; do
    echo -e "  ${GREEN}✓${NC} $project"
done
echo ""

if [[ ${#FAILED_PROJECTS[@]} -gt 0 ]]; then
    echo -e "${RED}Failed (${#FAILED_PROJECTS[@]}):${NC}"
    for project in "${FAILED_PROJECTS[@]}"; do
        echo -e "  ${RED}✗${NC} $project"
    done
    echo ""
fi

if [[ ${#SKIPPED_PROJECTS[@]} -gt 0 ]]; then
    echo -e "${YELLOW}Skipped (${#SKIPPED_PROJECTS[@]}):${NC}"
    for project in "${SKIPPED_PROJECTS[@]}"; do
        echo -e "  ${YELLOW}⊘${NC} $project"
    done
    echo ""
fi

echo -e "${BLUE}==================================================================${NC}"
echo -e "Total: ${#PASSED_PROJECTS[@]} passed, ${#FAILED_PROJECTS[@]} failed, ${#SKIPPED_PROJECTS[@]} skipped"
echo -e "${BLUE}==================================================================${NC}"

# Exit with error if any project failed
if [[ ${#FAILED_PROJECTS[@]} -gt 0 ]]; then
    exit 1
fi

exit 0

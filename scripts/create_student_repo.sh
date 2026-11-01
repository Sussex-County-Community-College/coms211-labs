#!/usr/bin/env bash
# Create one student's private COMS211 lab repository from the prepared VM clone.
# Run inside a clone of Sussex-County-Community-College/coms211-labs.
# Workflow adapted from the instructor-supplied xv6++ setup script.
# Original COMS211 implementation: MIT, see LICENSE-CODE in the curriculum repo.
set -euo pipefail

STARTER_REPO="Sussex-County-Community-College/coms211-labs"
NEW_REPO_NAME="coms211-private"
INSTRUCTOR_GH="jsissler"
DEFAULT_BRANCH="main"
export GH_HOST=github.com

fail() { printf 'Error: %s\n' "$*" >&2; exit 1; }
trap 'printf "Setup stopped. Preserve any repository or folder already created; ask the instructor for help before retrying.\n" >&2' ERR

normalize_git_url() {
    local url="${1%.git}"
    case "$url" in
        https://github.com/*) printf '%s\n' "${url#https://github.com/}" ;;
        git@github.com:*) printf '%s\n' "${url#git@github.com:}" ;;
        ssh://git@github.com/*) printf '%s\n' "${url#ssh://git@github.com/}" ;;
        *) return 1 ;;
    esac
}

for cmd in git gh; do
    command -v "$cmd" >/dev/null 2>&1 || fail "Install $cmd using the instructor's setup directions."
done
[[ $# -eq 0 ]] || fail "Run this script without arguments, inside the starter clone."
REPO_ROOT="$(git rev-parse --show-toplevel 2>/dev/null)" || fail "Open a terminal inside the coms211-labs starter clone."
cd "$REPO_ROOT"
ORIGIN="$(git remote get-url origin 2>/dev/null)" || fail "The starter clone needs an origin remote."
SOURCE="$(normalize_git_url "$ORIGIN")" || fail "Origin must be the GitHub coms211-labs starter."
[[ "${SOURCE,,}" == "${STARTER_REPO,,}" ]] || fail "Run inside a clone of ${STARTER_REPO}, not the curriculum or student repository."
[[ "$(git rev-parse --is-shallow-repository)" == false ]] || fail "Use an instructor-prepared full clone, not a shallow clone."
[[ -z "$(git status --porcelain)" ]] || fail "The starter has uncommitted files. Ask the instructor to prepare a clean starter."
git show-ref --verify --quiet "refs/remotes/origin/${DEFAULT_BRANCH}" || fail "The prepared clone is missing origin/main. Ask the instructor to refresh it."

# Copy only the prepared origin branches and tags. Local working branches,
# untracked/ignored files, credentials and source-clone settings are not copied.
REFS=()
SPECS=()
EXPECTED=()
while IFS=' ' read -r object ref; do
    [[ -n "$ref" && "$ref" != refs/remotes/origin/HEAD ]] || continue
    case "$ref" in
        refs/remotes/origin/*) destination="refs/heads/${ref#refs/remotes/origin/}" ;;
        refs/tags/*) destination="$ref" ;;
        *) continue ;;
    esac
    REFS+=("$destination")
    SPECS+=("${ref}:${destination}")
    EXPECTED+=("$object")
done < <(git for-each-ref --format='%(objectname) %(refname)' refs/remotes/origin refs/tags)
[[ ${#SPECS[@]} -gt 0 ]] || fail "No prepared branches or tags found."

printf '\nSign in to GitHub as the student who will own this repository.\n'
if ! gh auth status --hostname github.com >/dev/null 2>&1; then
    gh auth login --hostname github.com
fi
STUDENT_LOGIN="$(gh api --hostname github.com user --jq .login)"
[[ "$STUDENT_LOGIN" =~ ^[A-Za-z0-9][A-Za-z0-9-]*$ ]] || fail "Could not identify the signed-in GitHub user."
[[ "${STUDENT_LOGIN,,}" != "${INSTRUCTOR_GH,,}" ]] || fail "You are signed in as the instructor. Switch to the student's account and rerun."
TARGET_FULL="${STUDENT_LOGIN}/${NEW_REPO_NAME}"
TARGET_URL="https://github.com/${TARGET_FULL}.git"
TARGET_DIR="$(dirname "$REPO_ROOT")/${NEW_REPO_NAME}"
[[ ! -e "$TARGET_DIR" && ! -L "$TARGET_DIR" ]] || fail "The sibling coms211-private path already exists. Keep it and ask the instructor for help."
if gh repo view "$TARGET_FULL" >/dev/null 2>&1; then
    fail "${TARGET_FULL} already exists. This script does not modify existing student repositories."
fi
# Confirm the instructor account is valid before creating a repository.
gh api --hostname github.com "users/${INSTRUCTOR_GH}" --jq .login >/dev/null
printf '\nStudent account: %s\nNew private repository: %s\nLocal folder: %s\n' "$STUDENT_LOGIN" "$TARGET_FULL" "$TARGET_DIR"
printf 'Instructor: %s (write access invitation)\nOpening branch: %s\n' "$INSTRUCTOR_GH" "$DEFAULT_BRANCH"
printf 'Prepared branches/tags to copy:\n'
printf '  %s\n' "${REFS[@]}"
read -r -p "Type your GitHub username to confirm ownership and continue: " CONFIRM
[[ "${CONFIRM,,}" == "${STUDENT_LOGIN,,}" ]] || fail "Account not confirmed; nothing created."

# Use GitHub CLI's credential helper for this run, without changing global Git
# settings or the starter's remotes. Never embed a token in a remote URL.
student_git() { git -c credential.helper= -c 'credential.helper=!gh auth git-credential' "$@"; }
printf '\nCreating %s as a private repository...\n' "$TARGET_FULL"
gh repo create "$TARGET_FULL" --private --description 'Private COMS211 lab work' >/dev/null
PRIVATE="$(gh api --hostname github.com "repos/${TARGET_FULL}" --jq .private)"
[[ "$PRIVATE" == true ]] || fail "Private visibility could not be verified. No source pushed."

# Atomic, explicit refspecs: no force, mirror, prune, branch deletion or local
# branch upload. If any ref is rejected, the entire ref update is rejected.
student_git -c push.followTags=false push --atomic "$TARGET_URL" "${SPECS[@]}"
gh api --hostname github.com --method PATCH "repos/${TARGET_FULL}" -f default_branch="$DEFAULT_BRANCH" >/dev/null
REMOTE_REFS="$(student_git ls-remote --refs "$TARGET_URL")"
for i in "${!REFS[@]}"; do
    found=false
    while IFS=$'\t' read -r object ref; do
        if [[ "$ref" == "${REFS[$i]}" && "$object" == "${EXPECTED[$i]}" ]]; then found=true; break; fi
    done <<< "$REMOTE_REFS"
    [[ "$found" == true ]] || fail "A copied branch/tag did not match the prepared source."
done

printf '\nInviting %s with write access...\n' "$INSTRUCTOR_GH"
INVITATION_ID="$(gh api --hostname github.com --method PUT \
    -H 'Accept: application/vnd.github+json' \
    "repos/${TARGET_FULL}/collaborators/${INSTRUCTOR_GH}" \
    -f permission=push --jq '.id // empty')"
student_git clone --branch "$DEFAULT_BRANCH" "$TARGET_URL" "$TARGET_DIR"
[[ "$(git -C "$TARGET_DIR" branch --show-current)" == "$DEFAULT_BRANCH" ]] || fail "The clone is not on main."
[[ "$(git -C "$TARGET_DIR" remote get-url origin)" == "$TARGET_URL" ]] || fail "The clone's origin is unexpected."
[[ "$(gh api --hostname github.com "repos/${TARGET_FULL}" --jq .private)" == true ]] || fail "Final private visibility verification failed."

printf '\nSetup complete.\nRepository: https://github.com/%s\nFolder: %s\nBranch: %s\n' "$TARGET_FULL" "$TARGET_DIR" "$DEFAULT_BRANCH"
if [[ -n "$INVITATION_ID" ]]; then
    printf 'Instructor invitation sent; %s must accept it before access is active.\n' "$INSTRUCTOR_GH"
else
    printf 'Instructor collaborator request succeeded; confirm access with the instructor.\n'
fi
printf '\nOpen this student folder in GitHub Desktop, Eclipse and Codex.\n'
printf 'Use GitHub Desktop to review changes, commit and push.\n'
printf 'Select the branch named in each lab; this clone starts on main.\n'
printf 'Sign in to GitHub Desktop as the same student; this script does not configure its login.\n'

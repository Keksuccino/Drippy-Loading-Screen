#!/usr/bin/env bash
set -euo pipefail

usage() {
    cat <<'EOF'
Usage: ./run-loader-wsl.sh <fabric|neoforge> [client|server|runClient|runServer] [gradle args...]

Examples:
  ./run-loader-wsl.sh fabric
  ./run-loader-wsl.sh fabric server
  ./run-loader-wsl.sh neoforge client --stacktrace
  ./run-loader-wsl.sh fabric runClient --args='--demo'
EOF
}

fail() {
    printf 'Error: %s\n' "$*" >&2
    exit 1
}

is_wsl() {
    [[ -n "${WSL_INTEROP:-}" ]] || grep -qi 'microsoft' /proc/version 2>/dev/null
}

ensure_gitattributes() {
    local attributes_file="$project_root/.gitattributes"
    local marker="# Added by run-loader-wsl.sh for WSL launcher compatibility."
    local -a missing_lines=()

    [[ -f "$attributes_file" ]] || : > "$attributes_file"

    grep -Fxq '*.sh text eol=lf' "$attributes_file" || missing_lines+=('*.sh text eol=lf')
    grep -Fxq 'gradlew text eol=lf' "$attributes_file" || missing_lines+=('gradlew text eol=lf')
    grep -Fxq 'gradlew.bat text eol=crlf' "$attributes_file" || missing_lines+=('gradlew.bat text eol=crlf')

    if ((${#missing_lines[@]} > 0)); then
        [[ -s "$attributes_file" ]] && printf '\n' >> "$attributes_file"
        printf '%s\n' "$marker" >> "$attributes_file"
        printf '%s\n' "${missing_lines[@]}" >> "$attributes_file"
    fi
}

detect_windows_gradle_home() {
    local windows_gradle_home_raw

    command -v cmd.exe >/dev/null 2>&1 || fail "cmd.exe is not available from this shell."

    windows_gradle_home_raw="$(
        env -u GRADLE_USER_HOME -u JAVA_HOME -u JDK_HOME \
            cmd.exe /Q /D /C "if defined GRADLE_USER_HOME (echo %GRADLE_USER_HOME%) else echo %USERPROFILE%\\.gradle" \
            2>/dev/null | tr -d '\r' | tail -n 1
    )"
    [[ -n "$windows_gradle_home_raw" ]] || fail "Could not resolve the Windows Gradle user home."

    printf '%s\n' "$windows_gradle_home_raw"
}

project_root="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$project_root"

[[ -f "$project_root/gradlew.bat" ]] || fail "No gradlew.bat script exists in $project_root."
is_wsl || fail "This launcher is meant to be run from WSL."
ensure_gitattributes

loader="${1:-}"
case "$loader" in
    fabric|neoforge)
        shift
        ;;
    ""|-h|--help|help)
        usage
        exit 0
        ;;
    *)
        fail "Unknown loader '$loader'. Expected fabric or neoforge."
        ;;
esac

run_target="${1:-}"
if [[ -z "$run_target" || "$run_target" == -* ]]; then
    gradle_task="runClient"
else
    case "$run_target" in
        client)
            gradle_task="runClient"
            shift
            ;;
        server)
            gradle_task="runServer"
            shift
            ;;
        runClient|runServer)
            gradle_task="$run_target"
            shift
            ;;
        help|-h|--help)
            usage
            exit 0
            ;;
        *)
            fail "Unknown run target '$run_target'. Expected client, server, runClient, or runServer."
            ;;
    esac
fi

windows_gradle_home="$(detect_windows_gradle_home)"

printf 'Project root: %s\n' "$project_root"
printf 'Windows Gradle user home: %s\n' "$windows_gradle_home"
printf 'Gradle task: :%s:%s\n' "$loader" "$gradle_task"

exec env -u GRADLE_USER_HOME -u JAVA_HOME -u JDK_HOME \
    cmd.exe /Q /D /C \
    gradlew.bat \
    --console=plain \
    --no-daemon \
    ":$loader:$gradle_task" \
    "$@"

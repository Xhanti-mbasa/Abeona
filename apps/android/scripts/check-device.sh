#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_status=0
distribution=${1:-github}
expected_page_size=${2:-4096}
case "$distribution" in
    github) task=connectedGithubDebugAndroidTest ;;
    play) task=connectedPlayDebugAndroidTest ;;
    *) echo 'Choose github or play.' >&2; exit 2 ;;
esac
actual_page_size=$(adb shell getconf PAGE_SIZE | tr -d '\r')
if [[ "$actual_page_size" != "$expected_page_size" ]]; then
    echo "Expected $expected_page_size byte pages; emulator reports $actual_page_size." >&2
    exit 1
fi
./gradlew "$task" --no-daemon || test_status=$?
# Copy while emulator-runner still has the device connected, including failures.
mkdir -p app/build/outputs/ui-screenshots
adb pull /data/local/tmp/cttrains-ui-screenshots/. app/build/outputs/ui-screenshots/ || true
exit "$test_status"

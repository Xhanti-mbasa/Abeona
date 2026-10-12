#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")/.."
test_status=0
distribution=${1:-github}
expected_page_size=${2:-4096}
build_type=${3:-debug}
case "$distribution-$build_type" in
    github-debug) task=connectedGithubDebugAndroidTest ;;
    github-release) task=connectedGithubReleaseAndroidTest ;;
    play-debug) task=connectedPlayDebugAndroidTest ;;
    play-release) task=connectedPlayReleaseAndroidTest ;;
    *) echo 'Choose github or play and debug or release.' >&2; exit 2 ;;
esac
export CTTRAINS_TEST_BUILD_TYPE="$build_type"
export CTTRAINS_USE_DEVELOPMENT_SIGNING=true
actual_page_size=$(adb shell getconf PAGE_SIZE | tr -d '\r')
if [[ "$actual_page_size" != "$expected_page_size" ]]; then
    echo "Expected $expected_page_size byte pages; emulator reports $actual_page_size." >&2
    exit 1
fi
mkdir -p app/build/outputs/ui-screenshots
if [[ "$expected_page_size" == 16384 ]]; then
    adb shell setprop bionic.linker.16kb.app_compat.enabled false
    adb shell setprop pm.16kb.app_compat.disabled true
fi
printf 'distribution=%s\nbuild_type=%s\npage_size=%s\nfingerprint=%s\n' "$distribution" "$build_type" "$actual_page_size" "$(adb shell getprop ro.build.fingerprint | tr -d '\r')" > app/build/outputs/ui-screenshots/device-environment.txt
./gradlew "$task" --no-daemon || test_status=$?
# Copy while emulator-runner still has the device connected, including failures.
mkdir -p app/build/outputs/ui-screenshots
adb pull /data/local/tmp/cttrains-ui-screenshots/. app/build/outputs/ui-screenshots/ || true
exit "$test_status"

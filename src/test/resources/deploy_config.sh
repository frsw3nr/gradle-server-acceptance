#!/bin/bash
export GETCONFIG_BASE="$HOME/work/gradle/gradle-server-acceptance"
export GETCONFIG_HOME="$HOME/work"

cp "${GETCONFIG_BASE}/src/test/resources/check_sheet.xlsx" \
    "${GETCONFIG_BASE}/src/main/resources/root/jp/check_sheet.xlsx"
cp "${GETCONFIG_BASE}/src/test/resources/config.groovy" \
    "${GETCONFIG_BASE}/src/main/resources/root/jp/config/"

cd "${GETCOFNG_BASE}"
gradle zip

cd "${GETCONFIG_HOME}"
unzip -q -o "${GETCONFIG_BASE}/build/distributions/gradle-server-acceptance-0.1.17.zip"


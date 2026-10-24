#!/bin/sh
cd "$(dirname "$0")"
command -v java >/dev/null || { echo "Java 21 fehlt"; exit 1; }
GV=9.2.1
if [ ! -x ".gradle-dist/gradle-$GV/bin/gradle" ]; then
  mkdir -p .gradle-dist
  curl -L "https://services.gradle.org/distributions/gradle-$GV-bin.zip" -o .gradle-dist/g.zip
  unzip -qo .gradle-dist/g.zip -d .gradle-dist
fi
".gradle-dist/gradle-$GV/bin/gradle" build
echo "Fertig: build/libs/skinmenu-1.0.0.jar"

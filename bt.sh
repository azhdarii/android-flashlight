#!/bin/bash
export JAVA_HOME=/workspace/toolchain/jdk-17.0.20.1+1
export ANDROID_HOME=/workspace/toolchain/android-sdk
export PATH=$JAVA_HOME/bin:$PATH
cd /workspace/app10
exec ./gradlew "$@"

#!/bin/bash

# 设置Java 17为临时环境变量
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# 验证Java版本
echo "使用Java版本:"
$JAVA_HOME/bin/java -version

# 运行Gradle同步
./gradlew tasks --refresh-dependencies


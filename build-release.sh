#!/bin/bash

# Release 构建脚本
# 用于构建已签名的 Release APK

set -e  # 遇到错误立即退出

# 设置Java 17为临时环境变量
export JAVA_HOME=/opt/homebrew/opt/openjdk@17

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo "=========================================="
echo "  Release 构建脚本"
echo "=========================================="
echo ""

# 验证Java版本
echo -e "${GREEN}使用Java版本:${NC}"
$JAVA_HOME/bin/java -version
echo ""

# 检查签名密钥配置
echo -e "${YELLOW}检查签名配置...${NC}"
if [ ! -f "local.properties" ]; then
    echo -e "${RED}错误: local.properties 文件不存在${NC}"
    exit 1
fi

# 检查密钥文件是否存在
KEYSTORE_PATH=$(grep "^keystore.path=" local.properties | cut -d'=' -f2)
if [ -z "$KEYSTORE_PATH" ]; then
    echo -e "${RED}错误: 未找到 keystore.path 配置${NC}"
    exit 1
fi

if [ ! -f "$KEYSTORE_PATH" ]; then
    echo -e "${RED}错误: 签名密钥文件不存在: $KEYSTORE_PATH${NC}"
    echo -e "${YELLOW}请先创建签名密钥:${NC}"
    echo "  keytool -genkey -v -keystore $KEYSTORE_PATH \\"
    echo "    -keyalg RSA -keysize 2048 -validity 10000 \\"
    echo "    -alias tracker-app"
    exit 1
fi

echo -e "${GREEN}✓ 签名密钥文件存在: $KEYSTORE_PATH${NC}"
echo ""

# 清理之前的构建
echo -e "${YELLOW}清理之前的构建...${NC}"
./gradlew clean
echo ""

# 构建 Release APK
echo -e "${YELLOW}开始构建 Release APK...${NC}"
./gradlew assembleRelease

# 检查构建结果
APK_PATH="app/build/outputs/apk/release/app-release.apk"
if [ -f "$APK_PATH" ]; then
    echo ""
    echo -e "${GREEN}=========================================="
    echo "  构建成功！"
    echo "==========================================${NC}"
    echo ""
    echo -e "${GREEN}APK 位置:${NC} $APK_PATH"
    
    # 显示 APK 信息
    APK_SIZE=$(du -h "$APK_PATH" | cut -f1)
    echo -e "${GREEN}APK 大小:${NC} $APK_SIZE"
    echo ""
    
    # 验证 APK 是否已签名
    echo -e "${YELLOW}验证 APK 签名...${NC}"
    if command -v apksigner &> /dev/null; then
        apksigner verify --print-certs "$APK_PATH" > /dev/null 2>&1
        if [ $? -eq 0 ]; then
            echo -e "${GREEN}✓ APK 已正确签名${NC}"
        else
            echo -e "${RED}✗ APK 签名验证失败${NC}"
        fi
    else
        echo -e "${YELLOW}提示: 未找到 apksigner，跳过签名验证${NC}"
    fi
    
    echo ""
    echo -e "${GREEN}可以安装到设备进行测试:${NC}"
    echo "  adb install -r $APK_PATH"
    echo ""
else
    echo ""
    echo -e "${RED}=========================================="
    echo "  构建失败！"
    echo "==========================================${NC}"
    echo ""
    echo -e "${RED}APK 文件未找到: $APK_PATH${NC}"
    exit 1
fi


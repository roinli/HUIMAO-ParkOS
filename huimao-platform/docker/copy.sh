#!/bin/sh

# 复制项目的文件到对应docker路径，便于一键生成镜像。
usage() {
	echo "Usage: sh copy.sh"
	exit 1
}

echo "begin package "
#打包开始
cd ..
mvn clean install -Dmaven.test.skip=true
#前端
cd ./huimao-ui
npm install --registry=https://registry.npmmirror.com
npm run build:prod
cd ../docker
# copy sql
echo "begin copy sql "
cp ../sql/huimao_platform.sql ./mysql/db
cp ../sql/huimao_config.sql ./mysql/db

# copy html
echo "begin copy html "
rm -rf ./nginx/html/dist
cp -rp ../huimao-ui/dist/** ./nginx/html/dist


# copy jar
echo "begin copy huimao-register "
cp ../huimao-register/target/huimao-gateway.jar ./nacos/jar

echo "begin copy huimao-gateway "
cp ../huimao-gateway/target/huimao-gateway.jar ./huimao/gateway/jar

echo "begin copy huimao-auth "
cp ../huimao-auth/target/huimao-auth.jar ./huimao/auth/jar

echo "begin copy huimao-demo "
cp ../huimao-demo/target/huimao-demo.jar ./huimao/demo/jar

echo "begin copy huimao-monitor "
cp ../huimao-visual/huimao-monitor/target/huimao-monitor.jar  ./huimao/visual/monitor/jar

echo "begin copy huimao-system "
cp ../huimao-modules/huimao-system/target/huimao-system.jar ./huimao/modules/system/jar

echo "begin copy huimao-file "
cp ../huimao-modules/huimao-file/target/huimao-file.jar ./huimao/modules/file/jar

echo "begin copy huimao-gen "
cp ../huimao-modules/huimao-gen/target/huimao-gen.jar ./huimao/modules/gen/jar

echo "begin copy huimao-job "
cp ../huimao-modules/huimao-job/target/huimao-job.jar ./huimao/modules/job/jar


@echo off
chcp 65001 >nul & cls
echo.
echo [信息] 复制文件到Docker目录
echo.

%~d0
cd %~dp0

cd ..
echo 编译后端
start /wait cmd /c "mvn clean package -P prod -Dmaven.test.skip=true"
echo 编译前端
cd huimao-ui
start /wait cmd /c "npm install"
start /wait cmd /c "npm run build:prod"
cd ../docker

echo 复制 sql
xcopy ..\sql\huimao_platform.sql .\mysql\db  /y
xcopy ..\sql\huimao_config.sql .\mysql\db  /y

echo 复制 html
xcopy ..\huimao-ui\dist .\nginx\html\dist  /s /e /y

echo 复制 huimao-nacos
xcopy ..\huimao-register\target\huimao-register.jar .\nacos\jar  /y

echo 复制 huimao-gateway
xcopy ..\huimao-gateway\target\huimao-gateway.jar .\huimao\gateway\jar  /y

echo 复制 huimao-auth
xcopy ..\huimao-auth\target\huimao-auth.jar .\huimao\auth\jar  /y

echo 复制 huimao-demo
xcopy ..\huimao-demo\target\huimao-demo.jar .\huimao\demo\jar  /y

echo 复制 huimao-monitor
xcopy ..\huimao-visual\huimao-monitor\target\huimao-monitor.jar  .\huimao\visual\monitor\jar  /y

echo 复制 huimao-system
xcopy ..\huimao-modules\huimao-system\target\huimao-system.jar .\huimao\modules\system\jar  /y

echo 复制 huimao-file
xcopy ..\huimao-modules\huimao-file\target\huimao-file.jar .\huimao\modules\file\jar  /y

echo 复制 huimao-gen
xcopy ..\huimao-modules\huimao-gen\target\huimao-gen.jar .\huimao\modules\gen\jar  /y

echo 复制 huimao-job
xcopy ..\huimao-modules\huimao-job\target\huimao-job.jar .\huimao\modules\job\jar  /y

@REM 前端打包成镜像
cd ./nginx
docker build -t huimao/huimao-nginx:v1.0 .

@REM nacos打包成镜像
cd ../nacos
docker build -t huimao/huimao-register:v1.0 .

@REM 打包网关镜像 (修改构建上下文)
cd ../huimao/gateway
docker build -t huimao/huimao-gateway:v1.0 .

@REM 打包认证镜像 (修改构建上下文)
cd ../../huimao/auth
docker build -t huimao/huimao-auth:v1.0 .

@REM 打包系统模块镜像 (修改构建上下文)
cd ../../huimao/modules/system
docker build -t huimao/huimao-system:v1.0 .

@REM 打包文件服务镜像 (修改构建上下文)
cd ../file
docker build -t huimao/huimao-file:v1.0 .

@REM 打包定时任务镜像
cd ../job
docker build -t huimao/huimao-job:v1.0 .

@REM 打包监视镜像
cd ../../visual/monitor
docker build -t huimao/huimao-monitor:v1.0 .

@REM 打包demo镜像
cd ../../demo
docker build -t huimao/huimao-demo:v1.0 .

@REM 打包代码生成镜像
cd ../modules/gen
docker build -t huimao/huimao-gen:v1.0 .

pause

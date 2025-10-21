@echo off
title 🚇 Restart Minikube Tunnel (Auto Full Process)
echo.
echo [INFO] Minikube 터널 자동 재시작 스크립트 시작
echo =====================================================

:: 관리자 권한 확인
net session >nul 2>&1
if %errorLevel% neq 0 (
    echo [ERROR] 관리자 권한으로 실행해주세요.
    echo        (우클릭 > "관리자 권한으로 실행")
    pause
    exit /b
)

:: 1️⃣ 기존 minikube 프로세스 종료
echo [STEP 1] 기존 minikube 프로세스 종료 중...
tasklist | findstr /I "minikube" >nul
if %errorlevel%==0 (
    echo     기존 minikube 프로세스 발견됨 → 종료 중...
    taskkill /F /IM minikube.exe >nul 2>&1
    timeout /t 2 >nul
) else (
    echo     기존 터널 프로세스 없음.
)

:: 2️⃣ 라우팅 클린업
echo [STEP 2] 라우팅 클린업 실행 중...
minikube tunnel --cleanup >nul 2>&1

:: 3️⃣ 클러스터 상태 확인 및 실행
echo [STEP 3] Minikube 클러스터 상태 확인 중...
for /f "tokens=* delims=" %%i in ('minikube status ^| findstr /I "Running"') do set STATUS=%%i
if not defined STATUS (
    echo     클러스터가 꺼져 있음 → minikube start 실행 중...
    minikube start --driver=docker
) else (
    echo     Minikube 클러스터가 이미 실행 중입니다.
)
timeout /t 2 >nul

:: 4️⃣ 새 터널 시작
echo.
echo [STEP 4] 새 Minikube 터널 시작 중...
echo ---------------------------------------------
start "Minikube Tunnel" powershell -NoExit -Command "minikube tunnel"
timeout /t 5 >nul

:: 5️⃣ 서비스 상태 자동 확인
echo [STEP 5] 현재 Service 상태 확인 (EXTERNAL-IP 할당 여부)
echo ---------------------------------------------
kubectl get svc -o wide

echo.
echo ✅ 터널이 정상적으로 시작되었는지 확인하려면 위 테이블에서 'EXTERNAL-IP' 컬럼을 보세요.
echo 💡 이 창을 닫지 않아도 됩니다. (터널은 별도의 PowerShell 창에서 실행 중)
echo =====================================================
pause

@echo off
echo Starting SkillMatch Backend...
cd /d %~dp0..\backend
call mvn spring-boot:run
pause

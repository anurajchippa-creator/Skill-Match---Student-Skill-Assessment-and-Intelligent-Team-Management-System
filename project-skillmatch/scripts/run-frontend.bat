@echo off
echo Starting SkillMatch Swing Frontend...
cd /d %~dp0..\frontend
call mvn compile exec:java -Dexec.mainClass="com.skillmatch.ui.MainFrame"
pause

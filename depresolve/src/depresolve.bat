@echo off
java -Xnoclassgc -Xshare:off -noverify -cp "%~dp0\libs\*" id.depresolve.app.Main %*
@echo off

:: Java handles "\" as an escape symbol. Because "\" in Windows is a file separator
:: it causes any Windows path to be resolved by Java incorrectly.
:: Example: "C:\a" when pass to Java main() will be converted to Java String "C:a"
:: To fix this we replace any "\" to "\\" before passing them to Java
SETLOCAL
set "javaArgs=%*"
set "javaArgs=%javaArgs:\=\\%"

java -Xshare:off -cp "%~dp0\libs\*" id.depresolve.app.Main %javaArgs%

ENDLOCAL
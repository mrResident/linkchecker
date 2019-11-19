@echo off

set EXECUTABLE_FILE=linkchecker.jar
set PROPERTIES_FILE=application-production.properties

if not exist %EXECUTABLE_FILE% (
    echo Executable file linkchecker.jar is not found!
    exit /b
)

if ""=="%1" (
    echo Running program in DEMO mode
    java -jar %EXECUTABLE_FILE%
) else (
    if "--help"=="%1" goto :Help
    if "--debug"=="%1" (
        echo Running program in DEMO mode with extended debug information
        java -jar linkchecker.jar --spring.profiles.active=demo,debug
    ) else (
        if "--production"=="%1" (
            if exist %PROPERTIES_FILE% (
                if ""=="%2" (
                    echo Running program in PRODUCTION mode
                    java -jar linkchecker.jar --spring.profiles.active=production
                ) else (
                    if "--debug"=="%2" (
                        echo Running program in PRODUCTION mode with extended debug information
                        java -jar linkchecker.jar --spring.profiles.active=production,debug
                    ) else goto :KEY_NOT_FOUND %2
                )
            ) else (
                goto :PROPERTIES_FILE_NOT_FOUND
            )
        ) else goto :KEY_NOT_FOUND %1
    )
)
exit /B

:Help
echo Usage: linkchecker_win [KEY]
echo.
echo Script without key is run program in DEMO mode. Also vailable next switches:
echo.
echo --debug - running program in DEMO mode with extended debug information.
echo.
echo --production - running program in PRODUCTION mode. For running in this mode needed additional file application-production.properties with PostgreSQL dataset information. Also for this mode available addition key --debug for runnning program with extended debug information.
echo.
echo --help - display this is message
echo.
echo Examples:
echo.
echo linkchecker_win - run program in DEMO mode
echo.
echo linkchecker_win --debug - run program in DEMO mode with extended debug information.
echo.
echo linkchecker_win --production - run program in PRODUCTION mode.
echo.
echo linkchecker_win --production --debug - run program in PRODUCTION mode with extended debug information.
echo.
echo For more information see https://gitlab.com/Aleksandrov/linkchecker/wikis/
exit /b

:KEY_NOT_FOUND
echo linkchecker_win: unknown option %~1
echo Try 'linkchecker_win --help' for more information.
exit /b

:PROPERTIES_FILE_NOT_FOUND
setlocal
echo WARNING!
echo.
echo You try run program in PRODUCTION mode. For this mode need PostgreSQL but file %PROPERTIES_FILE% with dataset information is not found. Please fill next information and run program again!
echo.
set /p PRMT="PostgreSQL database host name or IP address (default localhost): "
if ""=="%PRMT%" (
    set LINKCHECKER_PGSQL_DB_HOST=jdbc:postgresql://localhost
) else (
    set LINKCHECKER_PGSQL_DB_HOST=jdbc:postgresql://%PRMT%
)
set /p PRMT1="PostgreSQL database port (default 5432): "
if ""=="%PRMT1%" set LINKCHECKER_PGSQL_DB_PORT=5432
set /p PRMT2="PostgreSQL database name (default linkchecker): "
if ""=="%PRMT2%" set LINKCHECKER_PGSQL_DB_NAME=linkchecker
set /p LINKCHECKER_PGSQL_DB_USER="PostgreSQL database user name: "
call :getPassword LINKCHECKER_PGSQL_DB_PASSWORD "PostgreSQL database password: "
echo.
echo LINKCHECKER_PGSQL_DB_HOST=%LINKCHECKER_PGSQL_DB_HOST%>%PROPERTIES_FILE%
echo LINKCHECKER_PGSQL_DB_PORT=%LINKCHECKER_PGSQL_DB_PORT%>>%PROPERTIES_FILE%
echo LINKCHECKER_PGSQL_DB_NAME=%LINKCHECKER_PGSQL_DB_NAME%>>%PROPERTIES_FILE%
echo LINKCHECKER_PGSQL_DB_USER=%LINKCHECKER_PGSQL_DB_USER%>>%PROPERTIES_FILE%
echo LINKCHECKER_PGSQL_DB_PASSWORD=%LINKCHECKER_PGSQL_DB_PASSWORD%>>%PROPERTIES_FILE%
endlocal
exit /b

::------------------------------------------------------------------------------
:: Masks user input and returns the input as a variable.
:: Password-masking code based on http://www.dostips.com/forum/viewtopic.php?p=33538#p33538
::
:: Arguments: %1 - the variable to store the password in
:: %2 - the prompt to display when receiving input
::------------------------------------------------------------------------------
:getPassword
set "_password="

:: We need a backspace to handle character removal
for /f %%a in ('"prompt;$H&for %%b in (0) do rem"') do set "BS=%%a"

:: Prompt the user
set /p "=%~2" <nul

:keyLoop
:: Retrieve a keypress
set "key="
for /f "delims=" %%a in ('xcopy /l /w "%~f0" "%~f0" 2^>nul') do if not defined key set "key=%%a"
set "key=%key:~-1%"

:: If No keypress (enter), then exit
:: If backspace, remove character from password and console
:: Otherwise, add a character to password and go ask for next one
if defined key (
if "%key%"=="%BS%" (
if defined _password (
set "_password=%_password:~0,-1%"
set /p "=!BS! !BS!"<nul
)
) else (
set "_password=%_password%%key%"
set /p "="<nul
)
goto :keyLoop
)
echo/

:: Return password to caller
set "%~1=%_password%"
goto :eof
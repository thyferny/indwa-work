@rem
@rem Script to Setup Alpine Illuminator in Windows
@rem
@echo off
pushd "%~dp0%"
set alpine_war=AlpineIlluminator.war
set data_repository_name=ALPINE_DATA_REPOSITORY
set source_data_repository_folder=.\\ALPINE_DATA_REPOSITORY
set alpine_data_repository_path_default=%USERPROFILE%\%data_repository_name%

set /p tomcat_home_path=Please input tomcat installation path:

set tomcat_webapp_folder="%tomcat_home_path%\\webapps\"
set tomcat_startup_bat="%tomcat_home_path%\\bin\\startup.bat"
if not exist %tomcat_webapp_folder% (
echo Path %tomcat_home_path% is not a valid Tomcat installation, try again
goto end
)
echo *****************************************************************
echo Please input a path to store repository data
echo or press ENTER to accept the default:
echo %alpine_data_repository_path_default%
echo *****************************************************************
set /p alpine_data_repository_path_input="Data path:"
@rem get the data repository file path.
if "%alpine_data_repository_path_input%"=="" (
set alpine_data_repository_path=%alpine_data_repository_path_default%)else (
set alpine_data_repository_path=%alpine_data_repository_path_input%)
echo *****************************************************************
@rem :copy_data_repository_files
@rem select to drop the data repository folder if exist
@echo Copying data repository files
@if exist "%alpine_data_repository_path%" (
echo %alpine_data_repository_path% already exist,
set /p ans="would you like to remove it first.y/n:")
if "%ans%" == "y" (del /S /Q "%alpine_data_repository_path%")
xcopy /E /-Y /Q %source_data_repository_folder% "%alpine_data_repository_path%"
@echo Data repository files are copied to %alpine_data_repository_path%.
@rem :copy_war
@echo Setting up Alpine Illuminator Web application
xcopy /Y /Q %alpine_war% %tomcat_webapp_folder%
echo *****************************************************************
echo Setting Environment Variable for %data_repository_name%
@rem set Environment for ALPINE_DATA_REPOSITORY
set IsNull=true
@rem for %%a in ('reg query "HKEY_LOCAL_MACHINE\SYSTEM\ControlSet001\Control\Session Manager\Environment\"')do (
@rem echo %%a|find /i %data_repository_name%&&set IsNull=false
@rem echo %%a
@rem echo %IsNull%
@rem )
if not IsNull==true (
set %data_repository_name%=%alpine_data_repository_path%
setx %data_repository_name% %alpine_data_repository_path%
setx %data_repository_name% %alpine_data_repository_path% /M
@rem add a system environment for alpine data repository for all users, but it only take effort after reboot.
reg add "HKEY_LOCAL_MACHINE\SYSTEM\ControlSet001\Control\Session Manager\Environment" /v  %data_repository_name% /t REG_SZ /d %alpine_data_repository_path% /f)
)
echo *****************************************************************
@echo AlpineIlluminator installed complete, please restart up the tomcat application.
@echo Restarting
%tomcat_startup_bat%
@echo "Done."
:end
pause

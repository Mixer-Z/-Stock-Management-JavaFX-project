@echo off
java --module-path "%~dp0javafx-sdk-24\lib" --add-modules javafx.controls,javafx.fxml -jar "%~dp0Project.jar"
pause

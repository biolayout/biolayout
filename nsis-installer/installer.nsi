!include "MUI2.nsh"
!include "x64.nsh"
!include "fileassoc.nsh"
!include "LogicLib.nsh"

!addplugindir "."

!define LONG_NAME "BioLayout"
!define VERSION "3.5"
!define BASE_NAME "BioLayout"
!define OUTPUT_NAME "BioLayout"
!define BASE_DIR ".."

!define INSTALLER_NAME "${OUTPUT_NAME}-${VERSION}-installer.exe"
!define EXE_NAME "${BASE_NAME}-${VERSION}.exe"
!define JAR_NAME "${BASE_NAME}-${VERSION}.jar"
!define OUTPUT_EXE_NAME "${BASE_NAME}.exe"

; General
Name "${LONG_NAME}"
OutFile "${INSTALLER_NAME}"
InstallDir "$PROGRAMFILES\${LONG_NAME}"
InstallDirRegKey HKCU "Software\${LONG_NAME}" ""

; Product & Version Information
VIProductVersion "1.0.0.0"

VIAddVersionKey ProductName "${LONG_NAME}"
VIAddVersionKey Comments "${LONG_NAME}"
VIAddVersionKey LegalCopyright "� University of Edinburgh 2020"
VIAddVersionKey FileDescription "${LONG_NAME}"
VIAddVersionKey FileVersion "1.0.0.0"
VIAddVersionKey ProductVersion "1.0.0.0"

; Installer Icons
!insertmacro MUI_DEFAULT MUI_ICON "${BASE_DIR}/src/main/resources/Resources/Images/Icon.ico"
!insertmacro MUI_DEFAULT MUI_UNICON "${BASE_DIR}/src/main/resources/Resources/Images/Icon.ico"

Icon "${MUI_ICON}"
UninstallIcon "${MUI_UNICON}"

WindowIcon on

; Variables
Var MUI_TEMP
Var STARTMENU_FOLDER

; Pages
!insertmacro MUI_PAGE_WELCOME
!insertmacro MUI_PAGE_LICENSE "Licenses.txt"

!define MUI_COMPONENTSPAGE_NODESC
!insertmacro MUI_PAGE_COMPONENTS

!insertmacro MUI_PAGE_DIRECTORY

; Start Menu Folder Page Configuration
!define MUI_STARTMENUPAGE_REGISTRY_ROOT "HKCU"
!define MUI_STARTMENUPAGE_REGISTRY_KEY "Software\${LONG_NAME}"
!define MUI_STARTMENUPAGE_REGISTRY_VALUENAME "Start Menu Folder"

!insertmacro MUI_PAGE_STARTMENU Application $STARTMENU_FOLDER

!insertmacro MUI_PAGE_INSTFILES

!define MUI_FINISHPAGE_RUN
!define MUI_FINISHPAGE_RUN_CHECKED
!define MUI_FINISHPAGE_RUN_TEXT "Start ${LONG_NAME}"
!define MUI_FINISHPAGE_RUN_FUNCTION "Launch"

!insertmacro MUI_PAGE_FINISH

!insertmacro MUI_UNPAGE_WELCOME
!insertmacro MUI_UNPAGE_CONFIRM
!insertmacro MUI_UNPAGE_INSTFILES
!insertmacro MUI_UNPAGE_FINISH

;Launch function
Function Launch
    ShellExecAsUser::ShellExecAsUser "" "$INSTDIR\${OUTPUT_EXE_NAME}"
FunctionEnd

;Languages
!insertmacro MUI_LANGUAGE "English"

; Main asset
Section "-${LONG_NAME}"

    SetOutPath "$INSTDIR"

    Delete "$INSTDIR\*.jar"

    File "/oname=${OUTPUT_EXE_NAME}" "${BASE_DIR}/target/${EXE_NAME}"
    File "/oname=${JAR_NAME}" "${BASE_DIR}/target/${JAR_NAME}"

    File "Licenses.txt"

    WriteRegStr HKLM "SOFTWARE\${LONG_NAME}" "Install_Dir" "$INSTDIR"

    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${LONG_NAME}" "DisplayName" "${LONG_NAME}"
    WriteRegStr HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${LONG_NAME}" "UninstallString" '"$INSTDIR\uninstall.exe"'
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${LONG_NAME}" "NoModify" 1
    WriteRegDWORD HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${LONG_NAME}" "NoRepair" 1

    WriteUninstaller "$INSTDIR\Uninstall.exe"

    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application

    CreateDirectory "$SMPROGRAMS\$STARTMENU_FOLDER\"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\Uninstall.lnk" "$INSTDIR\Uninstall.exe"
    CreateShortCut "$SMPROGRAMS\$STARTMENU_FOLDER\${LONG_NAME}.lnk" "$INSTDIR\${OUTPUT_EXE_NAME}"

    !insertmacro UPDATEFILEASSOC
    !insertmacro MUI_STARTMENU_WRITE_END

SectionEnd

; File Associations
SectionGroup "File associations"
    Section "${BASE_NAME} layout file (.layout)"
        !insertmacro APP_ASSOCIATE "layout" "${BASE_NAME}.layout" "${BASE_NAME} Layout File" \
            "$INSTDIR\${OUTPUT_EXE_NAME},0" "Open" "$INSTDIR\${OUTPUT_EXE_NAME} $\"%1$\""
    SectionEnd

    Section "Simple interaction file (.sif)"
        !insertmacro APP_ASSOCIATE "sif" "${BASE_NAME}.sif" "${BASE_NAME} Sif File" \
            "$INSTDIR\${OUTPUT_EXE_NAME},0" "Open" "$INSTDIR\${OUTPUT_EXE_NAME} $\"%1$\""
    SectionEnd

    Section "Gene expression file (.expression)"
        !insertmacro APP_ASSOCIATE "expression" "BioLayout.expression" "BioLayout Expression File" \
            "$INSTDIR\${OUTPUT_EXE_NAME},0" "Open" "$INSTDIR\${OUTPUT_EXE_NAME} $\"%1$\""
    SectionEnd

    Section "Matrix file (.matrix)"
        !insertmacro APP_ASSOCIATE "matrix" "${BASE_NAME}.matrix" "${BASE_NAME} Matrix File" \
            "$INSTDIR\${OUTPUT_EXE_NAME},0" "Open" "$INSTDIR\${OUTPUT_EXE_NAME} $\"%1$\""
    SectionEnd
SectionGroupEnd

; Desktop shortcut
Section "Desktop shortcut"
    !insertmacro MUI_STARTMENU_WRITE_BEGIN Application
    CreateShortCut "$DESKTOP\${LONG_NAME}.lnk" "$INSTDIR\${OUTPUT_EXE_NAME}"
    !insertmacro MUI_STARTMENU_WRITE_END
SectionEnd

; Uninstaller
Section "Uninstall"

    RMDir /r "$INSTDIR"

    !insertmacro MUI_STARTMENU_GETFOLDER Application $MUI_TEMP

    Delete "$SMPROGRAMS\$MUI_TEMP\Uninstall.lnk"
    Delete "$SMPROGRAMS\$MUI_TEMP\${LONG_NAME}.lnk"

    RMDir /r "$SMPROGRAMS\$MUI_TEMP"

    Delete "$DESKTOP\${LONG_NAME}.lnk"

    DeleteRegKey HKLM "Software\Microsoft\Windows\CurrentVersion\Uninstall\${LONG_NAME}"
    DeleteRegKey HKLM "SOFTWARE\${LONG_NAME}"

    !insertmacro APP_UNASSOCIATE "sif"        "${BASE_NAME}.sif"
    !insertmacro APP_UNASSOCIATE "expression" "BioLayout.expression"
    !insertmacro APP_UNASSOCIATE "matrix"     "${BASE_NAME}.matrix"
    !insertmacro UPDATEFILEASSOC

SectionEnd

@echo off
echo Membuat Tekstur Custom PNG Kartu Identitas via Windows PowerShell...
powershell -ExecutionPolicy Bypass -File create_texture.ps1
echo.
echo Selesai! Tekstur PNG kartu identitas telah dibuat di:
echo resourcepack/assets/minecraft/textures/item/rpg_identity_card.png
pause

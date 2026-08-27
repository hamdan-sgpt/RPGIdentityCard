# PowerShell Native Image Generator Script for RPG Identity Card
Add-Type -AssemblyName System.Drawing

$baseDir = $PSScriptRoot
$textureDir = Join-Path $baseDir "resourcepack\assets\minecraft\textures\item"

if (-not (Test-Path $textureDir)) {
    New-Item -ItemType Directory -Path $textureDir -Force | Out-Null
}

function Generate-RaceCardTexture {
    param(
        [string]$raceTitle,
        [int]$r, [int]$g, [int]$b,
        [int]$bgR, [int]$bgG, [int]$bgB,
        [string]$filename
    )

    $width = 128
    $height = 128
    $bmp = New-Object System.Drawing.Bitmap($width, $height)
    $graphics = [System.Drawing.Graphics]::FromImage($bmp)
    $graphics.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias
    $graphics.TextRenderingHint = [System.Drawing.Text.TextRenderingHint]::AntiAliasGridFit

    # Background
    $bgBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, $bgR, $bgG, $bgB))
    $graphics.FillRectangle($bgBrush, 0, 0, $width, $height)

    # Outer Border (Gold / Race Color)
    $borderPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, $r, $g, $b), 3)
    $graphics.DrawRectangle($borderPen, 2, 2, 123, 123)

    # Inner Accent Border
    $innerPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 97, 175, 239), 1)
    $graphics.DrawRectangle($innerPen, 5, 5, 117, 117)

    # Header Text
    $fontHeader = New-Object System.Drawing.Font("Arial", 7, [System.Drawing.FontStyle]::Bold)
    $fontSub = New-Object System.Drawing.Font("Arial", 5, [System.Drawing.FontStyle]::Regular)
    $fontData = New-Object System.Drawing.Font("Courier New", 5, [System.Drawing.FontStyle]::Bold)
    $fontFooter = New-Object System.Drawing.Font("Arial", 4, [System.Drawing.FontStyle]::Bold)

    $goldBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, $r, $g, $b))
    $whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::White)
    $grayBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 171, 178, 191))
    $greenBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 152, 195, 121))

    $graphics.DrawString("KERAJAAN VALORIA", $fontHeader, $goldBrush, 18, 10)
    $graphics.DrawString("KARTU IDENTITAS RPG RESMI", $fontSub, $grayBrush, 14, 20)

    # Line Separator
    $linePen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, $r, $g, $b), 1)
    $graphics.DrawLine($linePen, 8, 28, 120, 28)

    # Avatar Frame Box (32x32)
    $graphics.FillRectangle($bgBrush, 10, 32, 32, 32)
    $graphics.DrawRectangle($borderPen, 10, 32, 32, 32)
    $graphics.DrawString("FOTO", $fontSub, $grayBrush, 14, 43)

    # Data Attributes
    $graphics.DrawString("ID   : ID-VALORIA", $fontData, $goldBrush, 46, 33)
    $graphics.DrawString("VERIF: ASLI (RESMI)", $fontData, $greenBrush, 46, 41)
    $graphics.DrawString("RAS  : " + $raceTitle, $fontData, $goldBrush, 46, 49)
    $graphics.DrawString("CLASS: CITIZEN", $fontData, $whiteBrush, 46, 57)

    # Line Separator 2
    $graphics.DrawLine($linePen, 8, 68, 120, 68)

    # Footer
    $graphics.DrawString("OFFICIAL ROYAL IDENTITY CARD", $fontFooter, $goldBrush, 14, 73)
    $graphics.DrawString("VERIFIED BY ROYAL SYSTEM #2026", $fontFooter, $greenBrush, 10, 81)

    # Save PNG
    $filePath = Join-Path $textureDir $filename
    $bmp.Save($filePath, [System.Drawing.Imaging.ImageFormat]::Png)

    $graphics.Dispose()
    $bmp.Dispose()
    Write-Host "TERCIPTA TEKSTUR: $filename"
}

# Generasi Tekstur per Ras
Generate-RaceCardTexture "HUMAN" 255 255 255 18 24 38 "rpg_card_human.png"
Generate-RaceCardTexture "ELF" 46 204 113 14 36 24 "rpg_card_elf.png"
Generate-RaceCardTexture "DWARF" 230 126 34 40 25 15 "rpg_card_dwarf.png"
Generate-RaceCardTexture "DEMON" 231 76 60 40 15 15 "rpg_card_demon.png"
Generate-RaceCardTexture "RPG CITIZEN" 255 255 255 18 24 38 "rpg_identity_card.png"

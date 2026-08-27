Add-Type -AssemblyName System.Drawing

$baseDir = $PSScriptRoot
if (-not $baseDir) { $baseDir = Get-Location }

$logoPath = Join-Path $baseDir "logo.png"
$namaPath = Join-Path $baseDir "nama.png"
$outDir = Join-Path $baseDir "resourcepack\assets\minecraft\textures\item"

if (-not (Test-Path $outDir)) {
    New-Item -ItemType Directory -Path $outDir -Force | Out-Null
}

function Generate-RaceCardTexture($raceName, $borderR, $borderG, $borderB, $bgR, $bgG, $bgB, $fileName) {
    $outPath = Join-Path $outDir $fileName

    $bmp = New-Object System.Drawing.Bitmap(256, 256)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $g.SmoothingMode = [System.Drawing.Drawing2D.SmoothingMode]::AntiAlias

    # Background Color per Ras
    $bgBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, $bgR, $bgG, $bgB))
    $g.FillRectangle($bgBrush, 0, 0, 256, 256)

    # Border Color per Ras
    $goldPen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, $borderR, $borderG, $borderB), 3)
    $bluePen = New-Object System.Drawing.Pen([System.Drawing.Color]::FromArgb(255, 97, 175, 239), 1)
    $g.DrawRectangle($goldPen, 4, 4, 247, 247)
    $g.DrawRectangle($bluePen, 8, 8, 239, 239)

    # Logo
    if (Test-Path $logoPath) {
        try {
            $logo = [System.Drawing.Image]::FromFile($logoPath)
            $g.DrawImage($logo, 14, 14, 50, 50)
            $logo.Dispose()
        } catch {}
    }

    # Nama Server
    if (Test-Path $namaPath) {
        try {
            $nama = [System.Drawing.Image]::FromFile($namaPath)
            $g.DrawImage($nama, 70, 16, 170, 45)
            $nama.Dispose()
        } catch {}
    }

    $g.DrawLine($goldPen, 14, 70, 242, 70)

    # Frame Pas Foto 3x4
    $photoBg = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 30, 38, 56))
    $g.FillRectangle($photoBg, 14, 80, 70, 90)
    $g.DrawRectangle($goldPen, 14, 80, 70, 90)

    $font = New-Object System.Drawing.Font("Arial", 8, [System.Drawing.FontStyle]::Bold)
    $whiteBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 255, 255, 255))
    $goldBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, $borderR, $borderG, $borderB))
    $greenBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 152, 195, 121))
    $grayBrush = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 171, 178, 191))

    $g.DrawString("PAS FOTO`n  (3x4)", $font, $grayBrush, 18, 115)

    # Teks Metadata
    $g.DrawString("ID   : ID-7492-9102", $font, $goldBrush, 92, 80)
    $g.DrawString("VERIF: ASLI (RESMI)", $font, $greenBrush, 92, 98)
    $g.DrawString("NAMA : RPG PLAYER", $font, $whiteBrush, 92, 118)
    $g.DrawString("RAS  : $raceName", $font, $goldBrush, 92, 136)
    $g.DrawString("CLASS: HIGH MAGE", $font, $whiteBrush, 92, 154)

    # Footer
    $footerBg = New-Object System.Drawing.SolidBrush([System.Drawing.Color]::FromArgb(255, 24, 32, 48))
    $g.FillRectangle($footerBg, 14, 185, 228, 57)
    $g.DrawRectangle($bluePen, 14, 185, 228, 57)

    $smallFont = New-Object System.Drawing.Font("Arial", 7, [System.Drawing.FontStyle]::Bold)
    $g.DrawString("VALORIA OFFICIAL IDENTITY CARD", $smallFont, $goldBrush, 20, 195)
    $g.DrawString("VERIFIED BY ROYAL REGISTRY SYSTEM", $smallFont, $greenBrush, 20, 215)

    $g.Dispose()
    $bmp.Save($outPath, [System.Drawing.Imaging.ImageFormat]::Png)
    $bmp.Dispose()
    Write-Host "TERCIPTA TEKSTUR: $fileName"
}

# Generasi Tekstur per Ras
Generate-RaceCardTexture "HUMAN" 200 200 200 18 24 38 "rpg_card_human.png"
Generate-RaceCardTexture "ELF" 46 204 113 14 36 24 "rpg_card_elf.png"
Generate-RaceCardTexture "DWARF" 241 196 15 40 30 15 "rpg_card_dwarf.png"
Generate-RaceCardTexture "DEMON" 231 76 60 40 15 15 "rpg_card_demon.png"
Generate-RaceCardTexture "RPG CITIZEN" 229 192 123 18 24 38 "rpg_identity_card.png"

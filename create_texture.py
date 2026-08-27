import os
import sys
import subprocess

base_dir = os.path.dirname(os.path.abspath(__file__))

try:
    from PIL import Image, ImageDraw, ImageFont
except ImportError:
    print("Library Pillow tidak ditemukan di Python. Menggunakan PowerShell fallback native Windows...")
    ps_script = os.path.join(base_dir, "create_texture.ps1")
    subprocess.check_call(["powershell", "-ExecutionPolicy", "Bypass", "-File", ps_script])
    sys.exit(0)

logo_path = os.path.join(base_dir, "logo.png")
nama_path = os.path.join(base_dir, "nama.png")
output_dir = os.path.join(base_dir, "resourcepack", "assets", "minecraft", "textures", "item")
os.makedirs(output_dir, exist_ok=True)

def create_card_texture(race_name, filename):
    img = Image.new("RGBA", (256, 256), (18, 24, 38, 255))
    draw = ImageDraw.Draw(img)

    # Frame Emas Kerajaan
    draw.rectangle([4, 4, 251, 251], outline=(229, 192, 123, 255), width=3)
    draw.rectangle([8, 8, 247, 247], outline=(97, 175, 239, 255), width=1)

    # Tempel logo.png
    if os.path.exists(logo_path):
        try:
            logo = Image.open(logo_path).convert("RGBA")
            logo.thumbnail((50, 50))
            img.paste(logo, (14, 14), logo)
        except Exception as e:
            print("Logo error:", e)

    # Tempel nama.png
    if os.path.exists(nama_path):
        try:
            nama = Image.open(nama_path).convert("RGBA")
            nama.thumbnail((170, 45))
            img.paste(nama, (70, 16), nama)
        except Exception as e:
            print("Nama error:", e)

    # Garis Pembatas Header
    draw.line([(14, 70), (242, 70)], fill=(229, 192, 123, 255), width=2)

    # Frame Pas Foto 3x4 (Tempat Head Skin Player)
    draw.rectangle([14, 80, 84, 170], fill=(30, 38, 56, 255), outline=(229, 192, 123, 255), width=2)
    draw.text((20, 115), "PAS FOTO\n  (3x4)", fill=(171, 178, 191, 255))

    # Teks Data Atribut Kartu
    draw.text((95, 80), "ID   : ID-7492-9102", fill=(229, 192, 123, 255))
    draw.text((95, 100), "VERIF: ASLI (RESMI)", fill=(152, 195, 121, 255))
    draw.text((95, 125), "NAMA : RPG PLAYER", fill=(255, 255, 255, 255))
    draw.text((95, 145), f"RAS  : {race_name.upper()}", fill=(152, 195, 121, 255))
    draw.text((95, 165), "CLASS: CITIZEN", fill=(255, 255, 255, 255))

    # Footer Stempel Kerajaan
    draw.rectangle([14, 185, 242, 242], fill=(24, 32, 48, 255), outline=(97, 175, 239, 255), width=1)
    draw.text((24, 195), "KERAJAAN VALORIA OFFICIAL IDENTITY CARD", fill=(229, 192, 123, 255))
    draw.text((24, 215), "VERIFIED BY ROYAL REGISTRY SYSTEM", fill=(152, 195, 121, 255))

    target_path = os.path.join(output_dir, filename)
    img.save(target_path)
    print("SUCCESSFULLY_CREATED:", target_path)

textures_to_generate = [
    ("HUMAN", "rpg_card_human.png"),
    ("ELF", "rpg_card_elf.png"),
    ("DWARF", "rpg_card_dwarf.png"),
    ("DEMON", "rpg_card_demon.png"),
    ("RPG CITIZEN", "rpg_identity_card.png")
]

for race, fname in textures_to_generate:
    create_card_texture(race, fname)

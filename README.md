# 🪪 RPGIdentityCard - Plugin Minecraft Java (Paper / Spigot 1.20.1)

Plugin Minecraft Java **RPGIdentityCard** dari 0 yang mensimulasikan **Kartu Identitas RPG Kerajaan** lengkap dengan Pas Foto Kepala Skin Player (Player Head 3x4 / Custom Map), Pilihan Ras, Sistem Verifikasi Keaslian ID, dan Full GUI.

---

## 🌟 Fitur Utama

1. **Atribut Kartu Identitas RPG**:
   - **Nama Karakter**: Nama RPG pemain (default: Player Name / custom).
   - **Umur Karakter**: Umur karakter (misal: 25 Tahun).
   - **Profesi / Pekerjaan**: Pilihan Pekerjaan resmi Kerajaan Valdora:
     - 🪓 **Lumberjack** (Penebang Kayu)
     - ⛏️ **Miner** (Penambang)
     - 🌾 **Farmer** (Petani)
   - **Ras Karakter**: Pilihan Ras khusus:
     - 👤 **Human** (Manusia)
     - 🧝 **Elf** (Elf)
     - ⛏️ **Dwarf** (Dwarf)
     - 👿 **Demon** (Demon)
   - **ID Unik**: Kode unik 16-digit (Contoh: `ID-7492-9102-4821`).

2. **🗺️ Custom Map KTP Real-Time (Muka Skin Player & Dynamic Data)**:
   - Kartu fisik di tangan berupa **Custom Map 2D** yang secara real-time menampilkan **Foto Muka Skin 3D Pemain** (didownload dari Mojang API), Nama Karakter, Pekerjaan, Ras, & Status Verifikasi.

3. **🔍 Sistem Verifikasi Keaslian ID (Anti-Pemalsuan)**:
   - Setiap Kartu Identitas terdaftar secara resmi di registry internal kerajaan (`data/registry.yml`) dengan **Signature Hash Cryptographic**.
   - **Command `/id verify [player]`**: Mengecek apakah ID kartu `✅ ASLI (TERVERIFIKASI RESMI)` atau `❌ PALSU / PEMALSUAN`.

4. **🖼️ Custom Pas Foto Head Skin Player (3x4)**:
   - Di dalam GUI Card (`/id`), slot 10 berisi item **`Player Head Skull`** yang menampilkan **kepala skin pemain** pembuat / pemilik kartu secara persis.

5. **🖥️ Formulir Pendaftaran FULL GUI (`/id buat`)**:
   - **Click-to-Cycle Pekerjaan & Ras**: Klik item Pekerjaan atau Ras untuk berganti opsi secara otomatis!
   - **Chat Input Prompt**: Klik Nama atau Umur untuk mengetik nilai baru di chat.
   - **Tombol Simpan & Terbitkan ID**: Menggenerasi ID unik resmi, mendaftarkan ke database verifikasi, & memberikan item fisik kartu.

6. **🎴 Item Fisik Kartu Identitas (Custom Map / Paper)**:
   - **Shift + Klik Kanan** ke pemain lain untuk menunjukkan Kartu Identitas Anda langsung ke layar pemain target.

---

## 📜 Perintah & Permissions

| Perintah | Deskripsi | Permission |
|---|---|---|
| `/id` atau `/identity` | Membuka GUI Kartu Identitas milik sendiri. | Default |
| `/id lihat <pemain>` | Membuka GUI Kartu Identitas milik pemain lain. | Default |
| `/id verify [pemain]` | **Cek keaslian ID** (Menampilkan status `ASLI` atau `PALSU`). | Default |
| `/id ambil` | Mengambil item fisik Kartu Identitas ke inventory. | Default |
| `/id buat` | Membuka GUI Formulir pendaftaran & edit. | Default |
| `/idadmin reset <player>` | Mereset data identitas pemain. | `identity.admin` |
| `/idadmin give <player>` | Memberikan fisik item Kartu Identitas ke pemain. | `identity.admin` |
| `/idadmin setras <player> <Elf/Dwarf/Demon/Human>` | Mengubah Ras pemain. | `identity.admin` |
| `/idadmin set <player> <nama/umur/profesi> <nilai>` | Mengubah data pemain. | `identity.admin` |
| `/idadmin reload` | Reload file `config.yml`. | `identity.admin` |

---

## 🚀 Cara Build & Install Plugin

1. Jalankan script `build.bat` atau ketik `mvn clean package` di terminal.
2. File JAR `target/RPGIdentityCard-1.0.0.jar` akan ter-generate.
3. Copy JAR ke folder `plugins/` server Minecraft Anda (Paper / Spigot / Purpur 1.20.1+).

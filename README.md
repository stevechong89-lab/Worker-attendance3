# 工友打卡 Android V3

这是一个可以直接放进 GitHub 的 Android Studio 项目。

## 当前 MVP 功能

- 工头扫描员工 QR
- 自动按照状态判断：
  - 未上班 → 上班
  - 已上班 → 午休出去
  - 午休中 → 午休回来
  - 已回来 → 下班
- 显示员工姓名、工头、工地、打卡时间
- 自动计算实际工作时间
- 员工模式可显示个人 QR
- 本版本先使用手机本地资料，下一版再接数据库/云端

## GitHub Actions

项目已经附带 `.github/workflows/build-apk.yml`。
上传整个项目后，进入 GitHub → Actions → Build APK → Run workflow。
成功后在 Artifacts 下载 APK。

## 重要

不要只上传根目录的几个 Java/Gradle 文件。
必须保持 `app/src/...` 和 `.github/workflows/...` 的完整目录结构。

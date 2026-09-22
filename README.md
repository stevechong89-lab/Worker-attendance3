# 工友打卡 Android

这是一个干净的新 Android Studio 项目。

功能：
- 工头扫码
- 摄像头 QR 扫描
- 员工/工头/老板测试页面
- 自动判断上班、午休出去、午休回来、下班状态
- GitHub Actions 自动构建 Debug APK

使用：
1. 建立一个全新的 GitHub repository。
2. 把这个 ZIP 解压后，把里面所有文件上传到 repository 根目录。
3. GitHub Actions 会自动开始 Build APK。
4. 成功后在 Actions -> Build APK -> Artifacts 下载 worker-attendance-apk。

注意：这是第一阶段测试版，数据目前只存在手机运行期间，尚未接云端数据库。

# Spotifeur（个人定制版）

<p align="center"><img src="docs/assets/img/banner.png" alt="Spotifeur 安卓音乐播放器" width="1200"></p>

<p align="center">
  <b>一个整合 Spotify、YouTube Music 与本地音乐的 Android 播放器</b><br>
  基于开源项目 Spotifeur 二次开发，加入个人定制功能并持续优化
</p>

<p align="center">
  <a href="https://github.com/Zhao-allen/Spotifeur/releases">查看发布版本</a> ·
  <a href="https://github.com/Zhao-allen/Spotifeur/actions">查看自动构建</a> ·
  <a href="https://github.com/Zhao-allen/Spotifeur/issues">问题反馈</a> ·
  <a href="protocol/">协议与技术记录</a>
</p>

[![构建状态](https://github.com/Zhao-allen/Spotifeur/actions/workflows/build.yml/badge.svg)](https://github.com/Zhao-allen/Spotifeur/actions/workflows/build.yml)
[![许可证：GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue.svg)](LICENSE)
[![Android 8.0+](https://img.shields.io/badge/Android-8.0%2B-green.svg)](#安装说明)

> **项目说明**
>
> 本仓库是基于 [Spotifeur 上游项目](https://github.com/jean-voila/Spotifeur)进行的个人二次开发版本。保留原项目的播放器能力，并在此基础上开发歌词通知增强、个性化音乐推荐等功能。
>
> 本仓库与 Spotify、YouTube 或原作者没有官方关联。新增功能会随着开发和测试逐步完善；尚未完成验证的功能会明确标注开发状态。

## 项目简介

Spotifeur 是一款 Android 音乐播放器，可以在同一个应用中使用 Spotify 曲库、YouTube Music 曲库和手机本地音乐。它支持歌曲搜索、播放、歌单管理、歌词、离线缓存、播放队列和多种外观设置。

本个人定制版在原有功能之上，重点围绕日常听歌体验继续改进：

- 让歌词在播放通知中更容易查看；
- 根据自己的收藏、歌单和收听历史发现新歌；
- 持续优化首页、播放体验和 Android 设备适配。

## 我的定制与增强功能

### 1. 歌词通知增强

- **独立歌词通知**：播放时通过单独的通知展示当前歌词行，并尽可能附带下一句，方便在通知栏查看。
- **播放状态同步**：根据歌曲播放、暂停和歌词进度更新通知内容。
- **与媒体通知并存**：保留系统媒体播放卡片，用于查看歌曲名称和执行播放控制；歌词通知负责显示同步歌词。
- **无歌词时隐藏**：当前歌曲没有可用同步歌词时，不显示空白歌词内容。

**Android 系统显示说明：** Android 状态栏顶部通常只显示通知图标，不允许普通应用把任意歌词文字固定显示在状态栏。歌词主要显示在下拉通知区域。三星设备的 Now Bar 是否展示第三方应用的歌词，由 One UI 的媒体通知机制决定，本项目不保证一定出现歌词。

Android 13 及以上版本需要允许 Spotifeur 发送通知。通知显示效果也可能受到系统版本、省电策略和通知设置影响。

### 2. 基于个人歌单的智能推荐（开发中）

目标不是简单重复推荐歌单里的歌曲，而是先从你的音乐习惯中提取线索，再到更大的 Spotify 曲库中寻找可能喜欢的新歌。

当前实现方向：

- 汇总 Spotify 收藏歌曲、已保存歌单、最近播放和收听历史，形成推荐种子；
- 分析多个已保存歌单，而不是只依赖单个歌单；
- 通过 Spotify 歌单推荐和 Radio 等曲库发现能力寻找候选歌曲；
- 尽量排除已经收藏、已经播放或已存在于已读取歌单中的歌曲；
- 推荐结果分批展示，支持刷新后轮换下一批候选歌曲。

**当前开发边界：** 推荐池暂存在应用运行内存中；歌单扫描有分页上限；推荐排序目前主要依赖 Spotify 返回的候选顺序，独立的流派相似度评分、长期偏好学习和持久化反馈机制仍需继续开发与验证。因此这部分目前属于开发中的功能，不代表已经实现完整的 AI 推荐系统。

### 3. 持续维护与设备适配

- 针对 Android 新版本检查通知权限和后台播放行为；
- 关注三星 One UI 设备上的媒体通知显示；
- 通过 GitHub Actions 执行 Android 编译、单元测试和 lint 检查；
- 在上游项目更新的基础上维护个人定制改动。

## 原项目主要功能

### 音乐来源
- **Spotify**：搜索并播放 Spotify 曲库，管理账户中的收藏和歌单。
- **YouTube Music**：无需 Google 账号即可搜索和播放曲库；连接 Google 账号后可管理账号歌单。
- **本地音乐**：播放设备中的音频文件。
- **多来源歌单**：在应用内组合 Spotify、YouTube Music 和本地歌曲。

### 播放体验
- 播放队列、随机播放、循环播放、睡眠定时器；
- 同步歌词与沉浸式歌词界面；
- 后台播放、锁屏媒体控制、蓝牙耳机控制；
- 音频缓存、离线下载和不同网络环境下的音质设置；
- 均衡器、音量标准化及多种播放器样式。

### 歌单与音乐库
- 收藏歌曲、专辑、艺人和歌单；
- 创建、编辑和整理歌单；
- 导入歌单链接或 CSV，导出歌单为 CSV；
- 在 Spotify 与 YouTube Music 等来源之间转换歌单；
- 查看播放历史、专辑和艺人信息。

### 界面与个性化
- 浅色、深色和跟随系统主题；
- 首页模块显示、排序和布局设置；
- 多种播放器样式、导航栏和手势操作；
- 动态配色与专辑封面颜色适配。

## 界面预览

![Spotifeur 首页、播放器、歌词和播放队列](docs/assets/img/preview-listening.png)

![Spotifeur 专辑、歌单、设置和外观页面](docs/assets/img/preview-library.png)

## 安装说明

本仓库的源码和 GitHub Actions 构建产物不一定等同于可直接日常使用的完整安装包。特别是 Spotify 原生音频运行时需要在构建时提供符合项目要求的官方 Spotify APKM。

- [查看本仓库 Releases](https://github.com/Zhao-allen/Spotifeur/releases)（用于公开源码版本及不含 Spotify 原生运行时的构建，不提供完整原生 APK）
- [查看 GitHub Actions 构建记录](https://github.com/Zhao-allen/Spotifeur/actions)
- **完整 ARM64 APK（个人使用）**：保存在独立的[私有构建仓库](https://github.com/Zhao-allen/spotifeur-private-runtime)，仅获授权的仓库成员可访问和下载。私有仓库使用单独保存的 Spotify 原生运行时构建完整 APK；不会将该专有运行时或包含它的 APK 上传到本公开仓库。

公开仓库的 CI 构建会关闭原生运行时（`nativeRuntime=false`），用于源码编译、单元测试和 lint 验证。该 APK 不包含完整 Spotify 原生播放能力，不能当作完整日常播放版本。

下载 APK 后，请核对对应发布说明中的 SHA-256 校验值，再在 Android 设备上安装。请勿把仅用于编译、测试的无原生音频运行时 APK 当作完整播放版本。

## 从源码构建

### 环境要求

- JDK 21
- Android SDK，安装项目要求的 Android SDK Platform 37
- Android 8.0 或更高版本的 ARM64 设备
- 完整 Spotify 播放功能需要提供项目指定版本的官方 Spotify Android APKM：`9.2.0.26`（build `148373686`）

### 获取源码

```bash
git clone https://github.com/Zhao-allen/Spotifeur.git
cd Spotifeur
```

### 构建完整播放版本

将符合要求的 Spotify APKM 路径传给 Gradle：

```bash
./gradlew :app:assembleDebug -PspotifyArtifact=/path/to/com.spotify.music_9.2.0.26.apkm
```

也可以将文件放在：

```
native-runtime/artifacts/spotify-9.2.0.26.apkm
```

### 仅编译、测试和代码检查

如果没有提供 APKM，可以关闭原生运行时执行编译和测试：

```bash
./gradlew -PnativeRuntime=false :app:assembleDebug :app:testDebugUnitTest :app:lintDebug
```

这种模式主要用于 CI 和开发验证，Spotify 播放可能只能持续歌曲开头的几秒，**不应当作为完整日常播放版本使用**。

### 安装到设备

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

自行构建的 APK 使用你自己的签名密钥。若要安装与现有版本签名不同的构建包，可能需要先卸载旧版本；卸载会清除本地会话和下载数据，请提前备份。

更多原生运行时说明见 [native-runtime/README.md](native-runtime/README.md)。

## 项目文档

- [原生音频运行时说明](native-runtime/README.md)
- [Spotify 协议与技术记录](protocol/)
- [常见问题](docs/FAQ.md)
- [隐私说明](docs/PRIVACY.md)
- [参与贡献](CONTRIBUTING.md)
- [安全问题反馈](SECURITY.md)

## 致谢与声明

本项目基于 [jean-voila/Spotifeur](https://github.com/jean-voila/Spotifeur) 开发。感谢上游作者及相关开源项目的工作。

相关技术参考包括：

- [librespot](https://github.com/librespot-org/librespot)
- [Resona](https://github.com/code-saksham-hash/Resona)
- [AndroidX Media3](https://github.com/androidx/media)
- [Jetpack Compose](https://developer.android.com/compose)
- [LRCLIB](https://lrclib.net)

本项目遵循 [GNU General Public License v3.0](LICENSE)。使用、修改和再分发时请遵守许可证及相关服务条款。

---

<p align="center">个人维护 · 持续开发 · 欢迎体验与反馈</p>

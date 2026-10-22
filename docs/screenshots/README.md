# 界面截图

## 记录页与回顾日历

采集日期为 2026-10-22，界面实现对应 `4772c7e9d81c70395c91cc6144b308af244a3b64`（[PR #3](https://github.com/qinjunw/DailyM/pull/3)）。Android 16（API 36）模拟器中的独立测试安装包名为 `com.percontext.community.layoutalignqa`，版本为 `0.1.3-community-layoutalignqa`。

| 文件 | 画面 | 尺寸 |
| --- | --- | --- |
| [record-home-zh-CN.png](record-home-zh-CN.png) | 记录首页：录音主面板、皮蛋平台及 12 条合成录音的列表 | 1029 × 2100 |
| [review-calendar-zh-CN.png](review-calendar-zh-CN.png) | 回顾页：2026 年 8 月六周月历，选中 31 日，所选日期及合成回顾位于皮蛋下方 | 1029 × 2100 |

两图由 [RecordReviewLayoutTest](../../app/src/androidTest/java/com/percontext/app/feature/app/RecordReviewLayoutTest.kt) 捕获 `layout_test_viewport` 节点，使用实际 `PerContextApp` 页面、合成录音与回顾及业务网关替身。测试视口为 392 × 800 dp，密度 420 dpi，字体缩放 1.0，主题为晴空奶白浅色；不含系统状态栏或导航栏。文件保留原始像素，未另行裁切、缩放或重绘。

## 模型服务与关于

采集日期为 2026-10-18，界面代码对应 `261fc813b9b79eba1e5873acbaca840144f64b3c`。测试包名为 `com.percontext.community.brandingqa`，显示版本为 `0.1.3-community-brandingqa`。

| 文件 | 画面 | 尺寸 |
| --- | --- | --- |
| [settings-about-zh-CN.png](settings-about-zh-CN.png) | 中文设置页下半部，展示模型服务与关于；API Key 为空 | 1080 × 2400 |

设置页图像为模拟器的原始完整截图，使用空白测试数据，未经裁切、缩放或重绘；它不展示本次布局修复。

## 使用边界

以上图像均来自独立模拟器测试安装，没有私人录音、转写或凭据。两张布局图为测试视口截图，非发布 APK 的整机截图；设置图仍为模拟器原始完整截图。图像不作为真实麦克风录音、离线推理或云端回顾通过的证据。布局测量、测试和实机安装启动的范围见 [验证记录](../VERIFICATION.md)。

更新截图时使用专门的模拟器或测试安装，记录界面提交和采集日期，检查版本与 README 的描述一致，并确认没有私人内容或 Key 后再提交。

# TVBox (takagen99/Box) 项目结构全面分析报告

> 分析时间：2026-02-27  
> 项目来源：[takagen99/Box](https://github.com/takagen99/Box)  
> 分析版本：最新 main 分支  
> 文档目的：为后续 UI 美化改造提供完整的结构参考

---

## 目录

1. [项目整体架构](#一项目整体架构)
2. [第一部分：页面清单梳理](#第一部分页面清单梳理)
3. [第二部分：布局文件详细分析](#第二部分布局文件详细分析)
4. [第三部分：可复用组件识别](#第三部分可复用组件识别)
5. [第四部分：资源文件整理](#第四部分资源文件整理)
6. [美化改造建议](#美化改造建议)

---

## 一、项目整体架构

### 目录结构

```
TVBox (takagen99/Box)
├── app/
│   └── src/main/
│       ├── java/com/github/tvbox/osc/
│       │   ├── base/                    # 基础类
│       │   │   ├── BaseActivity.java    # Activity 基类
│       │   │   └── BaseLazyFragment.java # Fragment 基类（懒加载）
│       │   ├── ui/
│       │   │   ├── activity/            # 所有 Activity（11个）
│       │   │   ├── fragment/            # 所有 Fragment（4个）
│       │   │   ├── adapter/             # RecyclerView 适配器
│       │   │   ├── dialog/              # 自定义对话框
│       │   │   └── tv/widget/           # 自定义 TV 控件
│       │   ├── bean/                    # 数据模型
│       │   ├── api/                     # API 配置
│       │   ├── player/                  # 播放器封装
│       │   └── util/                    # 工具类
│       └── res/
│           ├── layout/                  # 布局文件（78个）
│           ├── drawable/                # 图形资源
│           ├── values/                  # 资源值文件
│           └── font/                    # 字体资源
├── pyramid/                             # 爬虫模块
└── quickjs/                             # JS 引擎模块
```

### 技术栈概览

| 技术 | 用途 |
|------|------|
| `TvRecyclerView` (owen.tvrecyclerview) | TV 端专用 RecyclerView，支持焦点管理 |
| `NoScrollViewPager` | 自定义不可滑动的 ViewPager（用于分类切换） |
| `xyz.doikki.videoplayer` | 直播播放器 |
| `MyVideoView` | 点播自定义播放器 |
| `master.flame.danmaku` | 弹幕引擎 |
| `LoadSir` | 加载/空/错误状态管理 |
| `EventBus` | 组件间事件通信 |
| `Hawk` | 轻量级 Key-Value 存储 |
| `Glide/Picasso` | 图片加载 |
| `AutoSize` | UI 自适应（基于 mm 单位） |

---

## 第一部分：页面清单梳理

### 1.1 Activity 全量清单

```
app/src/main/java/com/github/tvbox/osc/ui/activity/
├── HomeActivity.java         ← 应用主界面（核心入口）
├── DetailActivity.java       ← 影片详情页
├── PlayActivity.java         ← 点播播放页
├── LivePlayActivity.java     ← 直播播放页
├── SearchActivity.java       ← 搜索页
├── FastSearchActivity.java   ← 快速搜索页
├── HistoryActivity.java      ← 播放历史页
├── CollectActivity.java      ← 收藏页
├── SettingActivity.java      ← 设置主页
├── AppsActivity.java         ← 应用管理页
├── DriveActivity.java        ← 网盘/文件管理页
└── PushActivity.java         ← 投屏/推送页
```

### 1.2 Fragment 全量清单

```
app/src/main/java/com/github/tvbox/osc/ui/fragment/
├── UserFragment.java         ← 首页"个人/用户"标签页（含快捷入口+热播列表）
├── GridFragment.java         ← 内容分类页（显示影视卡片网格）
├── ModelSettingFragment.java ← 设置内容页（嵌套在 SettingActivity 中）
└── PlayFragment.java         ← 播放 Fragment（用于详情页预览播放）
```

### 1.3 页面层级关系图

```
应用启动
    │
    ▼
HomeActivity（主界面）
├── 顶部栏：[应用名] [WiFi] [搜索] [布局切换] [侧边栏] [设置]
├── 分类导航栏（TvRecyclerView 横向）
│   └── 分类 Tab（首页、电影、电视剧、综艺、动漫...）
└── NoScrollViewPager（内容区域）
    ├── UserFragment（首页 Tab）
    │   ├── 快捷入口行：[历史] [直播] [搜索] [收藏] [投屏] [网盘] [设置]
    │   ├── 热播列表（网格模式 tvHotListForGrid）
    │   └── 热播列表（单行模式 tvHotListForLine）
    └── GridFragment × N（每个内容分类对应一个）
        └── TvRecyclerView（影视卡片网格）

HomeActivity → DetailActivity（点击内容卡片）
    ├── 顶部：封面图 + 影片元信息（标题/年份/地区/语言/类型/演员/导演/简介）
    ├── 操作按钮行：[播放] [搜同款] [集序] [投屏] [简介] [收藏]
    ├── 播放源选择栏（TvRecyclerView 横向）
    ├── 季/系列选择栏（TvRecyclerView 横向）
    └── 剧集/选集列表（TvRecyclerView）

DetailActivity → PlayActivity（点击播放）
    ├── MyVideoView（视频渲染层）
    ├── DanmakuView（弹幕层）
    ├── ProgressBar（加载动画）
    └── player_vod_control_view（播放控制层）

HomeActivity → LivePlayActivity（点击直播入口）
    ├── VideoView（直播视频层）
    ├── 左侧频道列表面板（分组+频道）
    ├── 右侧设置面板
    └── 底部信息栏（台标+节目单+时间）

HomeActivity → SearchActivity（点击搜索图标）
    ├── 左侧：搜索输入区 + 键盘
    ├── 中间：热词/历史记录
    └── 右侧：搜索结果网格

HomeActivity → HistoryActivity（UserFragment 历史入口）
    └── TvRecyclerView（历史卡片网格）

HomeActivity → CollectActivity（UserFragment 收藏入口）
    └── TvRecyclerView（收藏卡片网格）

HomeActivity → SettingActivity（顶部栏设置图标）
    ├── ModelSettingFragment（设置表单）
    └── 多类设置选项组
```

---

## 第二部分：布局文件详细分析

### 2.1 HomeActivity — `activity_home.xml`

**布局结构：**
```
ConstraintLayout（根布局，全屏）
├── LinearLayout#topLayout（顶部导航栏）
│   ├── View（主题色装饰竖条，5mm×固定高）
│   ├── TextView#tvName（应用名，可点击，支持跑马灯）
│   ├── ImageView#tvWifi（WiFi状态图标，50×50mm）
│   ├── ImageView#tvFind（搜索按钮，50×50mm）
│   ├── ImageView#tvStyle（布局切换按钮，50×50mm）
│   ├── ImageView#tvDrawer（侧边栏按钮，50×50mm）
│   ├── ImageView#tvMenu（设置按钮，50×50mm）
│   └── TextView#tvDate（当前时间，不可聚焦）
└── LinearLayout#contentLayout（内容区域）
    ├── TvRecyclerView#mGridViewCategory（分类Tab横向列表）
    └── NoScrollViewPager#mViewPager（内容翻页器）
```

**视觉特征：**
| 元素 | 规格 |
|------|------|
| 顶部栏高度 | 50mm，上边距 20mm |
| 左右内边距 | 50mm |
| 主题色装饰条 | 宽 5mm，使用 `?attr/color_theme`（默认红 #F80000） |
| 按钮图标 | 50×50mm，带圆角背景选择器 |
| 时间字体 | 22mm（ts_22），白色，粗体 |
| App名称字体 | 30mm（ts_30），粗体 |

**焦点管理：**
- `tvName` nextFocusRight → `tvWifi`
- `tvName` nextFocusDown → `mGridViewCategory`（分类Tab）
- `tvMenu` nextFocusDown → `tvDrive`（侧边功能按钮）

---

### 2.2 UserFragment — `fragment_user.xml`（首页Tab）

**布局结构：**
```
LinearLayout（根布局，竖向）
├── LinearLayout#tvUserHome（快捷功能按钮横排）
│   ├── LinearLayout#tvHistory（历史，可聚焦）
│   │   ├── ImageView（图标，40mm）
│   │   └── TextView（"历史"文字，24mm）
│   ├── LinearLayout#tvLive（直播）
│   ├── LinearLayout#tvSearch（搜索）
│   ├── LinearLayout#tvFavorite（收藏）
│   ├── LinearLayout#tvPush（投屏）
│   ├── LinearLayout#tvDrive（网盘）
│   └── LinearLayout#tvSetting（设置）
├── TvRecyclerView#tvHotListForGrid（网格模式热播 - 默认隐藏）
└── TvRecyclerView#tvHotListForLine（单行横滑热播 - 高度 440mm）
```

**视觉特征：**
| 元素 | 规格 |
|------|------|
| 功能按钮高度 | 100mm，最小宽度 150mm |
| 功能按钮圆角 | 15mm，背景 `shape_user_home`（半透明深灰 + 主题色焦点） |
| 图标尺寸 | 40mm，透明度 0.9 |
| 文字颜色 | `#E6FFFFFF`（90%白） |
| 文字字体 | sans-serif-black，24mm |
| 热播列表内边距 | 左右 20mm，上下 30mm |
| 卡片间距 | 水平/垂直各 20mm |

---

### 2.3 GridFragment — `fragment_grid.xml`（内容分类页）

**布局结构：**
```
LinearLayout（根布局）
└── TvRecyclerView#mGridView
    ├── 左右内边距：60mm
    ├── 上下内边距：40mm
    ├── 卡片水平间距：20mm
    └── 卡片垂直间距：20mm
```

**说明：** 纯 RecyclerView 容器，内容完全由 Adapter 控制，Item 使用 `item_grid.xml`

---

### 2.4 DetailActivity — `activity_detail.xml`（影片详情页）

**布局结构：**
```
FrameLayout（根布局，全屏）
└── LinearLayout#llLayout（竖向主容器）
    ├── LinearLayout#topLayout（顶部信息区）
    │   ├── ImageView#ivThumb（封面图，225×300mm）
    │   ├── [View#previewPlayerPlace（预览播放占位，530×300mm，默认hidden）]
    │   └── LinearLayout（右侧元信息区）
    │       ├── TextView#tvName（片名，30mm，粗体）
    │       ├── LinearLayout（元数据横排）
    │       │   ├── TextView#tvSite（来源站点）
    │       │   ├── TextView#tvYear（年份）
    │       │   ├── TextView#tvArea（地区）
    │       │   ├── TextView#tvLang（语言）
    │       │   └── TextView#tvType（类型）
    │       ├── TextView#tvActor（演员，单行截断）
    │       ├── TextView#tvDirector（导演）
    │       ├── TextView#tvDes（简介，最多4行）
    │       └── LinearLayout（操作按钮横排）
    │           ├── TextView#tvPlay（播放，100×40mm）
    │           ├── TextView#tvQuickSearch（搜同款）
    │           ├── TextView#tvSort（集序排列）
    │           ├── TextView#tvPush（投屏）
    │           ├── TextView#tvDesc（完整简介）
    │           ├── TextView#tvCollect（收藏，110×40mm）
    │           └── ImageView#tvPlayUrl（外链按钮）
    ├── LinearLayout#mEmptyPlaylist（空列表提示，默认hidden）
    ├── TvRecyclerView#mGridViewFlag（播放源选择，45mm高）
    ├── TvRecyclerView#mSeriesGroupView（季/系列选择，45mm高）
    └── TvRecyclerView#mGridView（剧集列表）
```

**视觉特征：**
| 元素 | 规格 |
|------|------|
| 左右内边距 | 50mm |
| 上边距 | 20mm |
| 封面图尺寸 | 225×300mm（3:4比例） |
| 元信息文字 | 18mm，80%白色 |
| 简介文字 | 16mm，最多4行，行距1.1 |
| 操作按钮 | 高40mm，圆角20mm，半透明深色背景 |
| 聚焦状态 | 主题色背景40% + 2mm白色边框 |

---

### 2.5 PlayActivity — `activity_play.xml`（点播播放页）

**布局结构：**
```
FrameLayout（根布局，黑色背景）
├── MyVideoView#mVideoView（全屏视频渲染层）
├── DanmakuView#danmaku（弹幕覆盖层，默认GONE）
├── ProgressBar#play_loading（加载旋转动画，50mm，居中）
├── ImageView#play_load_error（错误图标，50mm，居中，默认GONE）
└── TextView#play_load_tip（加载提示文字，白色80%，24mm）
```

**说明：** 播放器控制 UI 由 `player_vod_control_view.xml` 动态添加

---

### 2.6 PlayActivity 控制层 — `player_vod_control_view.xml`

**布局结构：**
```
FrameLayout（根布局，全覆盖）
├── LinearLayout#top_container（顶部信息栏，128mm高）
│   ├── LinearLayout（第一行）
│   │   ├── View（主题色装饰条）
│   │   ├── TextView#tv_title_top（片名标题）
│   │   ├── LinearLayout#tv_speed_top（播放速度+码率）
│   │   └── LinearLayout（右侧信息）
│   └── LinearLayout（第二行 - 集数名）
├── LinearLayout#bottom_container（底部控制栏，140mm高）
│   ├── View（顶部渐变遮罩）
│   ├── LinearLayout（控制按钮行）
│   │   ├── ImageView（上一集）
│   │   ├── ImageView（快退）
│   │   ├── ImageView（播放/暂停）
│   │   ├── ImageView（快进）
│   │   ├── ImageView（下一集）
│   │   ├── TextView（当前时间）
│   │   ├── SeekBar（进度条）
│   │   └── TextView（总时长）
│   └── LinearLayout（底部功能按钮行）
│       └── [解析源/音轨/字幕/画面比例/弹幕/返回]
└── [各种弹出菜单面板：解析源/集数/字幕/速度等]
```

---

### 2.7 LivePlayActivity — `activity_live_play.xml`（直播页）

**布局结构：**
```
FrameLayout（根布局）
├── VideoView#mVideoView（全屏直播视频）
├── TextView#tv_selected_channel（频道序号覆盖显示）
├── LinearLayout#tvLeftChannelListLayout（左侧频道面板）
│   ├── TvRecyclerView#mGroupGridView（分组列表，200mm宽）
│   ├── LinearLayout#mDivLeft（左侧EPG展开按钮）
│   ├── View（分割线）
│   ├── TvRecyclerView#mChannelGridView（频道列表，340mm宽）
│   ├── LinearLayout#mDivRight（右侧EPG展开按钮）
│   └── LinearLayout#mGroupEPG（EPG节目单面板）
│       ├── TvRecyclerView#mEpgDateGridView（日期选择，120mm）
│       └── TvRecyclerView#mEpgInfoGridView（节目列表）
├── LinearLayout#tvRightSettingLayout（右侧设置面板）
│   ├── TvRecyclerView#mSettingItemView（设置项，160mm）
│   └── TvRecyclerView#mSettingGroupView（设置分组，180mm）
└── LinearLayout#tvBottomLayout（底部信息栏）
    ├── ImageView#tv_logo（台标，400mm）
    └── LinearLayout（节目信息区）
        ├── 当前节目时间+名称+系统时间
        ├── 频道号+频道名+分辨率+来源
        └── 下一节目信息
```

---

### 2.8 SearchActivity — `activity_search.xml`

**布局结构：**
```
LinearLayout（根布局，横向三栏）
├── LinearLayout（左栏，420mm宽）
│   ├── TextView#filterBtn（全站搜索按钮）
│   ├── LinearLayout（搜索输入行）
│   │   ├── CustomEditText#etSearch（搜索框）
│   │   └── ImageView#tvSearchCheckbox（筛选按钮）
│   ├── LinearLayout（操作按钮行）
│   │   ├── TextView#tvSearch（搜索按钮）
│   │   └── TextView#tvClear（清除按钮）
│   ├── SearchKeyboard#keyBoardRoot（TV专用键盘）
│   └── LinearLayout#remoteRoot（远程控制/二维码区）
├── LinearLayout#llWord（中栏，260mm宽）
│   ├── TextView（热搜标题）
│   └── TvRecyclerView#mGridViewWord（热搜词列表）
└── LinearLayout#llLayout（右栏，剩余空间）
    ├── RelativeLayout#search_tips（搜索历史标题）
    ├── FlowLayout#tv_history（历史搜索词流式布局）
    └── TvRecyclerView#mGridView（搜索结果网格）
```

---

### 2.9 SettingActivity — `activity_setting.xml`

**布局结构：**
```
LinearLayout（根布局，横向）
├── LinearLayout（左侧菜单，180mm宽，默认GONE）
│   └── TvRecyclerView#mGridView（设置分类菜单）
└── NoScrollViewPager#mViewPager（设置内容翻页器）
    └── ModelSettingFragment（设置表单）
```

---

### 2.10 HistoryActivity / CollectActivity（相同结构）

**布局结构：**
```
LinearLayout（根布局，竖向）
├── LinearLayout（顶部标题栏）
│   ├── View（主题色装饰条，5mm）
│   ├── TextView（页面标题，34mm，粗体）
│   ├── Space（弹性填充）
│   ├── [TextView#tvDelTip（选择提示，默认GONE）]
│   ├── ImageView#tvDelete（删除按钮，50×50mm）
│   └── ImageView#tvClear（清空按钮，50×50mm）
└── TvRecyclerView#mGridView（内容卡片网格）
    ├── 左右内边距：60mm
    ├── 上下内边距：40mm
    └── 卡片间距：20mm
```

---

## 第三部分：可复用组件识别

### 3.1 影视卡片 Item 布局

#### `item_grid.xml` — 分类页影视卡片（标准卡片）

```
FrameLayout（外壳，使用 shape_thumb_radius 背景）
│  ├── 未聚焦：圆角14px，36%白色填充
│  └── 聚焦：圆角14px，主题色40%填充 + 2mm白色边框
└── FrameLayout（内容区，214×280mm）
    ├── ImageView#ivThumb（封面图，full填充）
    ├── LinearLayout（左上角标签）
    │   ├── TextView#tvYear（年份标签，shape_thumb_year背景）
    │   ├── TextView#tvArea（地区标签）
    │   └── TextView#tvLang（语言标签）
    └── LinearLayout（底部信息条）
        ├── TextView#tvNote（更新状态/集数，shape_thumb_note背景）
        └── TextView#tvName（片名，shape_thumb_bottom_name背景）
```

**卡片规格：** 214mm宽 × 280mm高（约 3:4 比例）

#### `item_user_hot_vod.xml` — 首页热播卡片（与 item_grid 结构相同）

额外包含：
- `TextView#tvRate`（评分标签，默认GONE）
- `tvNote` 支持跑马灯
- `tvName` 带文字阴影

#### `item_search.xml` — 搜索结果卡片

```
尺寸：185×260mm（略小于标准卡片）
额外：TextView#tvSite（来源站点标签）
缺少：地区/语言标签
```

#### `item_list.xml` — 列表模式影视行

```
FrameLayout（背景 shape_user_focus，可聚焦）
└── LinearLayout（50mm高）
    ├── ImageView#ivThumb（缩略图，50mm宽）
    ├── TextView#tvName（片名，weight=1）
    └── TextView#tvNote（备注，weight=4，右对齐）
```

---

### 3.2 导航与分类 Item

#### `item_home_sort.xml` — 分类 Tab 项

```xml
LinearLayout（45mm高，button_home_sort_focus背景，可聚焦）
├── TextView#tvTitle（分类名，30mm字体，70%白色）
└── ImageView#tvFilter（筛选图标，30mm，默认GONE）
```

**焦点样式（button_home_sort_focus）：**
- 未选中：透明背景
- 聚焦/选中：主题色下划线或背景高亮

#### `item_series_flag.xml` — 播放源/集数选项

用于详情页播放源选择和集数Tab

---

### 3.3 直播相关 Item

#### `item_live_channel.xml` — 频道列表项
#### `item_live_channel_group.xml` — 频道分组标题
#### `item_live_setting.xml` — 直播设置项
#### `item_epglist.xml` — EPG节目单条目

---

### 3.4 搜索相关 Item

#### `item_search_word_hot.xml` — 热搜词标签
#### `item_search_word_split.xml` — 热搜词分隔
#### `item_search_lite.xml` — 精简搜索卡片
#### `item_quick_search_lite.xml` — 快搜卡片

---

### 3.5 设置相关 Item

#### `item_setting_menu.xml` — 设置菜单项

```xml
FrameLayout（shape_setting_sort_focus背景，下边距10mm）
└── TextView#tvName（设置项名称，24mm，居中，80%白色）
```

---

### 3.6 自定义 View 组件

| 类名 | 位置 | 功能描述 |
|------|------|----------|
| `SearchKeyboard` | `ui/tv/widget/` | TV专用搜索键盘（全键盘布局，支持遥控器导航） |
| `NoScrollViewPager` | `ui/tv/widget/` | 禁止手势滑动的ViewPager（分类内容页切换） |
| `MarqueeTextView` | `ui/tv/widget/` | 自动跑马灯TextView（用于频道名/节目名） |
| `CustomEditText` | `ui/tv/widget/` | TV端自定义输入框（优化焦点处理） |
| `ChannelListView` | `ui/tv/widget/` | 自定义频道列表控件 |
| `AudioWaveView` | `ui/tv/widget/` | 音频波形可视化控件 |
| `LoadMoreView` | `ui/tv/widget/` | 上拉加载更多视图 |
| `DefaultTransformer` | `ui/tv/widget/` | ViewPager页面切换动画 |
| `FixedSpeedScroller` | `ui/tv/widget/` | 固定速度滚动器（优化ViewPager切换） |
| `MyVideoView` | `player/` | 自定义点播播放器（封装多种解码器） |

---

### 3.7 通用对话框布局

| 文件 | 用途 |
|------|------|
| `dialog_confirm.xml` | 通用确认对话框（Yes/No按钮） |
| `dialog_about.xml` | 关于对话框 |
| `dialog_api.xml` | 数据源配置对话框 |
| `dialog_api_history.xml` | 数据源历史记录 |
| `dialog_select.xml` | 通用选择列表对话框 |
| `dialog_tip.xml` | 提示信息对话框 |
| `dialog_desc.xml` | 完整简介展示对话框 |
| `dialog_grid_filter.xml` | 分类筛选对话框 |
| `dialog_media_setting.xml` | 媒体设置对话框 |
| `dialog_danmu_setting.xml` | 弹幕设置对话框 |
| `dialog_subtitle.xml` | 字幕设置对话框 |
| `dialog_push.xml` | 投屏设置对话框 |
| `dialog_backup.xml` | 备份/恢复对话框 |
| `dialog_homeoption.xml` | 首页选项对话框 |
| `dialog_webdav.xml` | WebDAV配置对话框 |
| `dialog_alistdrive.xml` | Alist网盘配置对话框 |
| `dialog_quick_search.xml` | 快速搜索对话框 |
| `dialog_search_subtitle.xml` | 字幕搜索对话框 |
| `dialog_live_password.xml` | 直播密码输入 |
| `dialog_remote.xml` | 遥控器/远程控制 |
| `dialog_reset.xml` | 重置确认对话框 |
| `dialog_xwalk.xml` | WebView引擎切换 |

---

### 3.8 状态视图

| 文件 | 用途 |
|------|------|
| `loadsir_loading_layout.xml` | 加载中状态（旋转动画，50mm居中） |
| `loadsir_empty_layout.xml` | 空数据状态（图标+文字"没找到数据"） |
| `item_view_load_more.xml` | 底部加载更多条目 |

---

## 第四部分：资源文件整理

### 4.1 颜色方案 — `res/values/colors.xml`

#### 主题色系（可切换）

```xml
<!-- 默认主题：烈红色 -->
<color name="color_theme">#F80000</color>
<color name="color_theme_80">#CCF80000</color>  <!-- 80%透明度 -->
<color name="color_theme_70">#B3F80000</color>  <!-- 70%透明度 -->
<color name="color_theme_60">#99F80000</color>  <!-- 60%透明度 -->
<color name="color_theme_50">#80F80000</color>  <!-- 50%透明度 -->
<color name="color_theme_40">#66F80000</color>  <!-- 40%透明度（焦点背景） -->
<color name="color_theme_dark_70">#B3601216</color>  <!-- 深色主题对话框按钮 -->
```

#### 内置多套主题（通过 styles.xml 切换）

| 主题名称 | 主色 | 说明 |
|----------|------|------|
| 默认（Base）| `#F80000` | 烈红色 |
| NetfxTheme | `#D81F26` | Netflix 红 |
| DoraeTheme | `#18A2E7` | 哆啦A梦蓝 |
| PepsiTheme | `#004B93` | 百事可乐蓝 |
| NarutoTheme | `#FF7439` | 火影橙 |
| MinionTheme | `#FFD55E` | 小黄人黄 |
| YagamiTheme | `#FF2F70` | 夜神粉红 |
| SakuraTheme | `#FD9BDB` | 樱花粉 |

#### 背景色系

```xml
<!-- EPG/列表背景 -->
<color name="color_32364E">#32364E</color>       <!-- 深蓝灰（设置左侧菜单） -->
<color name="color_32364E_40">#6632364E</color>  <!-- 40%深蓝灰 -->
<color name="color_3D3D3D">#3D3D3D</color>       <!-- 深灰（分割线/对话框背景） -->
<color name="color_3D3D3D_50">#803D3D3D</color>  <!-- 50%深灰（按钮普通背景） -->
<color name="color_3D3D3D_80">#CC3D3D3D</color>  <!-- 80%深灰（筛选背景） -->

<!-- 半透明黑背景系列 -->
<color name="color_000000_90">#E6000000</color>
<color name="color_000000_80">#CC000000</color>  <!-- 80%黑（直播台标背景） -->
<color name="color_000000_60">#99000000</color>  <!-- 60%黑（卡片底部信息条） -->
<color name="color_000000_40">#66000000</color>
<color name="color_0E0E0E_90">#E60E0E0E</color>  <!-- 直播面板背景 -->

<!-- 对话框背景 -->
<color name="color_6A6A6A_95">#F26A6A6A</color>  <!-- 95%中灰（主对话框背景） -->
```

#### 白色系（文字/图标）

```xml
<color name="color_FFFFFF">#FFFFFF</color>        <!-- 纯白 -->
<color name="color_FFFFFF_90">#E6FFFFFF</color>   <!-- 90%白（功能按钮文字） -->
<color name="color_FFFFFF_80">#CCFFFFFF</color>   <!-- 80%白（元信息文字） -->
<color name="color_FFFFFF_70">#B3FFFFFF</color>   <!-- 70%白（分类Tab文字） -->
<color name="color_FFFFFF_50">#80FFFFFF</color>   <!-- 50%白（Hint文字） -->
<color name="color_FFFFFF_40">#66FFFFFF</color>
<color name="color_FFFFFF_20">#32FFFFFF</color>
```

---

### 4.2 尺寸规范 — `res/values/dimens.xml`

> **注意：** 项目使用 `mm` 作为单位（配合 AutoSize 自适应框架），而非标准 `dp`，确保在不同分辨率 TV 上显示效果一致。

#### 字体大小（ts = Text Size）

| 变量名 | 值 | 推荐用途 |
|--------|----|----------|
| `ts_10` | 10mm | 极小说明文字 |
| `ts_12` | 12mm | 最小文字 |
| `ts_14` | 14mm | — |
| `ts_15` | 15mm | 直播底部小字 |
| `ts_16` | 16mm | 简介文字 |
| `ts_18` | 18mm | 元信息标签文字 |
| `ts_20` | 20mm | 标准正文 |
| `ts_22` | 22mm | 卡片片名/时间 |
| `ts_24` | 24mm | 功能按钮文字/说明 |
| `ts_26` | 26mm | 播放器标题 |
| `ts_30` | 30mm | 顶部应用名/分类Tab |
| `ts_34` | 34mm | 页面大标题 |
| `ts_36` | 36mm | 直播节目名/时间 |
| `ts_40` | 40mm | — |
| `ts_50` | 50mm | 最大标题 |

#### 间距尺寸（vs = View Size）

| 变量名 | 值 | 主要用途 |
|--------|----|----------|
| `vs_1` | 1mm | 卡片内边距 |
| `vs_2` | 2mm | 边框宽度 |
| `vs_3` | 3mm | — |
| `vs_4` | 4mm | — |
| `vs_5` | 5mm | 标签间距/圆角装饰条宽度 |
| `vs_8` | 8mm | 装饰条上下边距 |
| `vs_10` | 10mm | 元素小间距 |
| `vs_12` | 12mm | 装饰条右边距 |
| `vs_14` | 14mm | 圆角半径(标准) |
| `vs_15` | 15mm | 直播列表内边距 |
| `vs_20` | 20mm | 标准内边距/卡片间距 |
| `vs_25` | 25mm | — |
| `vs_30` | 30mm | 对话框内边距 |
| `vs_40` | 40mm | 内容区上下内边距 |
| `vs_45` | 45mm | 行高（Tab/操作按钮） |
| `vs_50` | 50mm | 顶部栏高度/图标尺寸 |
| `vs_60` | 60mm | 内容区左右内边距 |
| `vs_80` | 80mm | — |
| `vs_100` | 100mm | 功能按钮高度/详情操作按钮宽度 |
| `vs_110` | 110mm | 收藏按钮宽度 |
| `vs_120` | 120mm | 加载状态图标 |
| `vs_128` | 128mm | 空状态图标 |
| `vs_150` | 150mm | 功能按钮最小宽度 |
| `vs_180` | 180mm | 设置菜单宽度 |
| `vs_185` | 185mm | 搜索卡片宽度 |
| `vs_200` | 200mm | 直播分组列表宽度 |
| `vs_214` | 214mm | 标准卡片宽度 |
| `vs_225` | 225mm | 详情封面图宽度 |
| `vs_240` | 240mm | 系统时间显示宽度 |
| `vs_260` | 260mm | 搜索热词列表宽度 |
| `vs_280` | 280mm | 标准卡片高度 |
| `vs_300` | 300mm | 详情封面图高度/预览播放高度 |
| `vs_340` | 340mm | 直播频道列表宽度 |
| `vs_400` | 400mm | 直播台标宽度 |
| `vs_420` | 420mm | 搜索左侧栏宽度 |
| `vs_440` | 440mm | 首页单行热播高度 |
| `vs_460` | 460mm | EPG面板宽度 |
| `vs_480` | 480mm | 确认对话框宽度 |
| `vs_530` | 530mm | 预览播放宽度 |
| `vs_600` | 600mm | 关于对话框文字宽度 |
| `vs_640` | 640mm | — |
| `vs_720` | 720mm | — |
| `vs_960` | 960mm | — |

---

### 4.3 主题与样式 — `res/values/styles.xml`

#### 应用基础主题

```xml
<style name="AppTheme" parent="Theme.AppCompat.Light.DarkActionBar">
    <item name="colorPrimary">@color/color_32364E</item>       <!-- 深蓝灰 -->
    <item name="colorPrimaryDark">@color/color_32364E_40</item>
    <item name="colorAccent">?attr/color_theme</item>           <!-- 主题色 -->
</style>

<!-- 所有 Activity 使用此主题 -->
<style name="AppTheme.NoActionBar">
    <item name="android:windowActionBar">false</item>
    <item name="android:windowNoTitle">true</item>
    <item name="android:windowFullscreen">true</item>     <!-- 全屏 -->
    <item name="android:windowBackground">@android:color/transparent</item>
</style>
```

#### 自定义对话框样式

```xml
<style name="CustomDialogStyle">   <!-- 无遮罩，透明背景，全屏 -->
<style name="CustomDialogStyleDim">  <!-- 有遮罩（backgroundDimEnabled=true） -->
```

#### 视频播放器样式

```xml
<style name="video_vertical_progressBar">   <!-- 音量/亮度竖向进度条 -->
<style name="video_horizontal_progressBar"> <!-- 迷你水平进度条 -->
<style name="surfaceType_surface">          <!-- SurfaceView 渲染 -->
<style name="surfaceType_texture">          <!-- TextureView 渲染 -->
```

---

### 4.4 Drawable 资源清单

#### 形状（Shape）— 背景装饰

| 文件 | 效果描述 |
|------|----------|
| `shape_thumb_radius.xml` | 卡片外壳：无焦点36%白圆角，聚焦主题色40%+2mm白边框 |
| `shape_thumb_bottom_name.xml` | 卡片底部片名条：60%黑色，下圆角14mm |
| `shape_thumb_note.xml` | 卡片更新状态标签：60%黑色，5mm圆角 |
| `shape_thumb_year.xml` | 卡片年份/地区小标签（具体实现未查） |
| `shape_user_focus.xml` | 通用可聚焦控件背景：无焦点45%深灰，聚焦主题色40%+2mm白边框 |
| `shape_user_home.xml` | 首页功能按钮背景：无焦点50%深灰圆角，聚焦主题色40%+2mm白边框 |
| `shape_user_delete.xml` | 删除选中态遮罩 |
| `shape_dialog_bg.xml` | 标准对话框背景：深灰#3D3D3D，15mm圆角 |
| `shape_dialog_bg_main.xml` | 主要对话框背景（颜色更深） |
| `shape_dialog_filter_bg.xml` | 筛选对话框背景 |
| `shape_live_bg_bottom.xml` | 直播底部信息栏背景 |
| `shape_live_focus.xml` | 直播列表焦点背景 |
| `shape_live_select.xml` | 直播选中项背景 |
| `shape_setting_model_focus.xml` | 设置项背景（与user_focus类似） |
| `shape_setting_sort_focus.xml` | 设置菜单项背景 |
| `shape_source_flag_focus.xml` | 播放源/集数Tab背景 |
| `shape_source_series_focus.xml` | 系列选择背景 |
| `shape_player_tap.xml` | 播放器点击区域背景 |
| `shape_speed_hint.xml` | 播放速度提示背景 |
| `shape_toplayout_tvname.xml` | 首页应用名背景 |
| `bg_live.xml` | 直播频道列表面板背景 |
| `channel_num_bg.xml` | 直播频道号背景 |
| `input_search.xml` | 搜索输入框背景 |
| `input_dialog_api_input.xml` | 对话框API输入框背景 |

#### 选择器（Selector）— 状态背景

| 文件 | 用途 |
|------|------|
| `button_detail_all.xml` | 详情页操作按钮（播放/收藏等） |
| `button_detail_collect.xml` | 收藏按钮专用 |
| `button_detail_preview.xml` | 预览播放区域 |
| `button_dialog_main.xml` | 对话框主要按钮 |
| `button_dialog_vod.xml` | 对话框次要按钮/图标按钮 |
| `button_home_select.xml` | 首页选中状态 |
| `button_home_sort_focus.xml` | 首页分类Tab |
| `item_bg_selector_left.xml` | 直播左侧列表项 |
| `item_bg_selector_right.xml` | 直播右侧列表项 |
| `selector_checkbox.xml` | 复选框 |
| `set_hm.xml` | 设置-首页设置图标 |
| `set_play.xml` | 设置-播放设置图标 |
| `set_setting.xml` | 设置-其他设置图标 |

#### 图标（Icon/Vector）

| 图标 | 用途 |
|------|------|
| `hm_wifi.xml` / `hm_wifi_no.xml` | WiFi 状态图标 |
| `hm_search.xml` | 搜索图标 |
| `hm_history.xml` | 历史图标 |
| `hm_live.xml` | 直播图标 |
| `hm_fav.xml` | 收藏图标 |
| `hm_push.xml` | 投屏图标 |
| `hm_folder.xml` | 文件夹图标 |
| `hm_settings.xml` | 设置图标 |
| `hm_drawer.xml` | 侧边栏图标 |
| `hm_left_right.xml` | 左右布局切换图标 |
| `hm_up_down.xml` | 上下布局切换图标 |
| `icon_delete.xml` | 删除图标 |
| `icon_clear.xml` | 清空图标 |
| `icon_empty.xml` | 空数据图标 |
| `icon_error.xml` | 错误图标 |
| `icon_filter.xml` | 筛选图标 |
| `icon_sort.xml` | 排序图标 |
| `icon_film.xml` | 影片图标 |
| `icon_setting.xml` | 设置图标 |
| `icon_home.xml` | 首页图标 |
| `icon_lock.xml` / `icon_unlock.xml` | 锁定/解锁 |
| `icon_img_placeholder.xml` | 图片占位符 |
| `v_pause.xml` / `v_play.xml` | 播放器暂停/播放 |
| `v_prev.xml` / `v_next.xml` | 上/下一集 |
| `v_ffwd.xml` / `v_replay.xml` | 快进/重播 |
| `v_back.xml` | 返回 |
| `v_subtitle.xml` | 字幕 |
| `v_danmu.xml` | 弹幕 |
| `v_audio.xml` | 音频 |
| `v_type.xml` | 画面比例 |
| `v_aspect.xml` | 宽高比 |

#### 背景图片（Bitmap）

| 文件 | 用途 |
|------|------|
| `app_bg.png` | 应用背景图 |
| `app_banner.png` | 应用横幅 |
| `app_icon.png` | 应用图标 |
| `htov.png` / `vtoh.png` | 横竖屏切换图示 |

#### 动画/播放器资源

| 文件 | 用途 |
|------|------|
| `anim_loading.xml` | 旋转加载动画 |
| `box_controller_top_bg.xml` | 播放器顶部渐变背景 |
| `box_controller_bottom_bg.xml` | 播放器底部渐变背景 |
| `play_progress_horizontal.xml` | 播放进度条（水平） |
| `play_progress_vertical.xml` | 音量/亮度进度条（垂直） |
| `shape_player_control_vod_seek.xml` | 进度条轨道形状 |
| `shape_player_control_vod_seek_thumb.xml` | 进度条拖动圆点 |

---

## TV适配情况总结

### 焦点管理机制

**1. 显式焦点声明（nextFocus 属性）**
```xml
<!-- 示例：HomeActivity 顶部栏 -->
android:nextFocusRight="@id/tvWifi"
android:nextFocusDown="@id/mGridViewCategory"
```

**2. TvRecyclerView 内置焦点管理**
- `app:tv_selectedItemIsCentered="true"` — 选中项自动居中（焦点滚动）
- `app:tv_horizontalSpacingWithMargins` — 带边距的水平间距
- `app:tv_verticalSpacingWithMargins` — 带边距的垂直间距

**3. 焦点可见反馈（所有可聚焦组件均有）**
```
未聚焦状态：半透明深灰背景（color_3D3D3D_45 ~ 50%）
聚焦状态：主题色40%背景 + 2mm白色描边
```
这一套机制在以下 Selector 中统一定义：
- `shape_thumb_radius.xml`（卡片）
- `shape_user_focus.xml`（通用控件）
- `shape_user_home.xml`（功能按钮）
- `button_detail_all.xml`（操作按钮）

**4. 遥控器键值处理**
```java
// HomeActivity 中处理遥控器事件
@Override
public boolean onKeyDown(int keyCode, KeyEvent event) {
    switch(keyCode) {
        case KeyEvent.KEYCODE_MENU: // 菜单键
        case KeyEvent.KEYCODE_BACK: // 返回键
        ...
    }
}
```

---

## 美化改造建议

基于以上分析，以下是针对 TVBox 美化改造的重点方向和建议：

### 🎨 1. 统一设计语言

**现状问题：**
- 主题色体系已较完善（7套备选主题）
- 但背景色方案偏传统（深灰系，略显陈旧）

**建议：**
```
✅ 为 color_theme 体系新增更现代的颜色选项
✅ 将应用背景从透明/深灰改为毛玻璃效果或渐变深色
✅ 统一按钮圆角规范（目前 vs_14 / vs_20 / 15mm 混用）
```

### 🖼️ 2. 卡片组件升级

**现状问题：**
- 卡片底部信息条（shape_thumb_bottom_name）圆角只有底部，视觉较生硬
- 无任何阴影/投影效果
- 封面图加载占位符为纯色

**建议：**
```
✅ 为卡片容器添加细微阴影（CardViewShadow 已有属性定义）
✅ 改善卡片底部信息条为全渐变遮罩设计
✅ 改善图片加载占位符（骨架屏或渐变色）
✅ 聚焦动画加入缩放效果（scale 1.0 → 1.05）
```

### 📐 3. 顶部导航栏优化

**现状问题：**
- 顶部栏图标按钮布局较密集
- 左侧装饰条（5mm）视觉层次单一

**建议：**
```
✅ 图标按钮改为更大的点击区域（60×60mm），icon尺寸保持或缩小
✅ 添加图标下方文字标签（可选）
✅ 装饰条改为应用 Logo 或带图标的 App 名称区域
```

### 🎬 4. 详情页面美化

**现状问题：**
- 封面图区域为纯图片，背景直接露出黑色/透明
- 操作按钮样式较简单（半透明灰色）

**建议：**
```
✅ 封面图区域添加模糊背景（BlurBehind），取样封面色
✅ 操作按钮区域采用 Material 风格 OutlinedButton
✅ 元信息标签改为胶囊形状标签组件
✅ 评分/更新状态标签更突出（主题色背景）
```

### 📺 5. 播放器控制栏美化

**现状问题：**
- 顶底渐变背景（box_controller_top_bg/bottom_bg）已有
- 控制按钮为简单向量图形，无动效

**建议：**
```
✅ 播放/暂停按钮改为圆形背景包裹的图标
✅ 进度条增加缓冲进度显示（双层进度条）
✅ 进度条拖动圆点改为更大、更醒目的样式
✅ 速度/音轨等功能按钮改为 Chip 胶囊风格
```

### 🔤 6. 字体与排版优化

**现状问题：**
- 全局字体为系统默认，无个性化
- 字体大小规范较丰富但使用不够统一

**建议：**
```
✅ 引入一款TV适合的中文字体（如思源黑体 SC）
✅ 建立统一的字体等级规范（Title/Subtitle/Body/Caption）
✅ 标题区域（34mm+）使用 textStyle="bold" 保持一致
```

### 🌊 7. 动画与过渡效果

**现状问题：**
- 页面切换无过渡动画
- 列表加载无渐入动效

**建议：**
```
✅ Activity 切换添加 Slide/Fade 过渡动画
✅ RecyclerView Item 添加渐入动画（staggered）
✅ 焦点移动时加入平滑过渡（已有 FixedSpeedScroller）
✅ 卡片聚焦/失焦加入 scale 缩放动画
```

---

## 改造优先级矩阵

| 改造项 | 影响范围 | 实现难度 | 视觉提升 | 优先级 |
|--------|----------|----------|----------|--------|
| 统一焦点高亮样式 | 全局 | 低 | 高 | ⭐⭐⭐ P0 |
| 卡片设计升级 | 首页/分类/搜索/历史/收藏 | 低 | 高 | ⭐⭐⭐ P0 |
| 详情页布局优化 | 详情页 | 中 | 高 | ⭐⭐⭐ P0 |
| 主题色方案扩展 | 全局 | 低 | 中 | ⭐⭐ P1 |
| 播放器控制栏美化 | 播放页 | 中 | 中 | ⭐⭐ P1 |
| 顶部导航栏优化 | 主页 | 低 | 中 | ⭐⭐ P1 |
| 字体方案引入 | 全局 | 中 | 中 | ⭐⭐ P1 |
| 页面过渡动画 | 全局 | 中 | 低 | ⭐ P2 |
| 应用背景设计 | 全局 | 高 | 高 | ⭐⭐ P1 |
| 对话框美化 | 全局 | 低 | 中 | ⭐⭐ P1 |

---

*报告生成完毕。如需对某个具体页面或组件进行深度分析，请告知具体需求。*

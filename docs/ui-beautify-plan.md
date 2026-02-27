# TVBox UI 美化改造规划方案

> 版本：v1.0  
> 制定时间：2026-02-27  
> 基于：[项目结构分析报告](./project-structure-analysis.md)  
> 参考风格：可可影视（现代化TV影视应用）  
> 核心原则：**保持功能完整性，专注布局结构与视觉体验升级**

---

## 目录

1. [技术框架确认](#一技术框架确认)
2. [页面改造优先级](#二页面改造优先级排序)
3. [首页布局重构方案](#三首页布局重构详细方案)
4. [卡片组件美化方案](#四卡片组件美化方案)
5. [焦点导航优化方案](#五焦点导航优化方案)
6. [其他页面美化方案](#六其他页面美化方案)
7. [分阶段实施计划](#七分阶段实施计划)
8. [风险控制策略](#八风险控制策略)
9. [文件改动全量清单](#九文件改动全量清单)

---

## 一、技术框架确认

### 1.1 当前技术栈（关键确认）

| 技术点 | 当前实现 | 改造影响 |
|--------|----------|----------|
| **列表组件** | `TvRecyclerView` (owen:tv-recyclerview:3.0.0) | ✅ 无需替换，在其基础上扩展 |
| **页面切换** | `NoScrollViewPager` (自定义) + `FragmentPagerAdapter` | ✅ 保留，新增 Fragment 即可 |
| **图片加载** | Glide 4.16 + `ImgUtil` 封装 | ✅ 直接使用，新增圆角处理 |
| **布局单位** | `mm` (基于 AutoSize 1.2.1 自适应) | ⚠️ 所有新增尺寸必须使用 mm 单位 |
| **数据绑定** | BaseRecyclerViewAdapterHelper 2.9.49 | ✅ 保留，新建 Adapter 继承即可 |
| **主题系统** | `?attr/color_theme` 动态主题色 | ✅ 新增组件必须引用此属性 |
| **焦点管理** | TvRecyclerView 内置 + nextFocus 属性 | ✅ 继承现有机制，补充边界逻辑 |
| **无 Leanback 库** | 纯原生 RecyclerView 派生 | ✅ 改造完全可控，无框架约束 |

### 1.2 核心约束确认

```
✅ 不使用 Leanback 库 → 布局改造自由度高
✅ AutoSize mm 单位 → 所有新尺寸规范统一使用 mm
✅ TvRecyclerView 支持横向/纵向/网格布局 → 无需引入新组件
✅ BaseQuickAdapter → 新 Adapter 统一继承此类
✅ EventBus 通信机制 → 组件间数据传递沿用
```

### 1.3 新增依赖评估

改造过程中需要新增的依赖：

```gradle
// 轮播图组件（已有 ViewPager2 思路，无需额外依赖）
// 方案：使用 ViewPager2 + RecyclerView，项目已依赖 recyclerview:1.3.2

// 骨架屏（可选，提升加载体验）
// 方案：自定义 Shimmer 动画效果，无需依赖

// 圆角图片（已有 Glide RoundedCorners）
// 方案：利用现有 Glide + RequestOptions 实现

// 结论：无需新增任何依赖 ✅
```

---

## 二、页面改造优先级排序

### 2.1 优先级评估矩阵

| 页面 | 用户接触频率 | 视觉影响度 | 技术难度 | 改造价值 | **优先级** |
|------|-------------|-----------|---------|---------|-----------|
| **首页 (HomeActivity + UserFragment)** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐⭐ | 极高 | **P0** |
| **影视卡片 (item_grid 系列)** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐⭐ | ⭐⭐ | 极高 | **P0** |
| **详情页 (DetailActivity)** | ⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐⭐ | 高 | **P1** |
| **全局焦点样式** | ⭐⭐⭐⭐⭐ | ⭐⭐⭐⭐ | ⭐⭐ | 高 | **P0** |
| **对话框美化** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐ | 中 | **P1** |
| **搜索页 (SearchActivity)** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐ | 中 | **P1** |
| **历史/收藏页** | ⭐⭐⭐ | ⭐⭐⭐ | ⭐ | 中 | **P2** |
| **播放控制栏** | ⭐⭐⭐⭐ | ⭐⭐⭐ | ⭐⭐⭐ | 中 | **P2** |
| **直播页** | ⭐⭐⭐ | ⭐⭐ | ⭐⭐⭐⭐ | 低 | **P3** |
| **设置页** | ⭐⭐ | ⭐⭐ | ⭐⭐ | 低 | **P3** |

### 2.2 改造阶段规划

```
Phase 1（第一阶段）：奠定视觉基础
  ├── P0-1: 全局焦点样式统一（colors + drawables）
  ├── P0-2: 影视卡片重设计（item_grid 系列）
  └── P0-3: 首页布局重构（UserFragment + HomeActivity）

Phase 2（第二阶段）：核心页面优化
  ├── P1-1: 详情页视觉升级
  ├── P1-2: 对话框样式统一美化
  └── P1-3: 搜索页布局优化

Phase 3（第三阶段）：体验细节打磨
  ├── P2-1: 历史/收藏页统一风格
  ├── P2-2: 播放控制栏视觉优化
  └── P2-3: 过渡动画与加载动效

Phase 4（第四阶段）：次要功能完善
  ├── P3-1: 直播页局部优化
  └── P3-2: 设置页视觉统一
```

---

## 三、首页布局重构详细方案

### 3.1 当前布局 vs 目标布局对比

#### 当前布局结构
```
HomeActivity（ConstraintLayout）
├── LinearLayout [topLayout] 高度50mm，上边距20mm
│   ├── View（5mm主题色条）
│   ├── TextView（应用名 30mm）
│   ├── ImageView × 5（图标按钮 50×50mm）
│   └── TextView（时间）
└── LinearLayout [contentLayout]
    ├── TvRecyclerView（分类Tab横向，高约45mm）
    └── NoScrollViewPager
        └── UserFragment（首页内容）
            ├── LinearLayout（7个功能按钮横排 100mm高）
            └── TvRecyclerView（热播列表，单行440mm 或 多行Grid）
```

**当前首页问题清单：**
- ❌ 无轮播图/Banner区域，视觉冲击力弱
- ❌ 功能按钮行（7个入口）占据过多垂直空间
- ❌ 热播内容区域没有分类标题，内容组织混乱
- ❌ 缺少"更多"横向滑动的内容行组织
- ❌ 整体布局过于紧凑，缺乏视觉呼吸感
- ❌ 分类Tab文字过大（30mm），占用空间多

#### 目标布局结构（可可影视风格）
```
HomeActivity（ConstraintLayout）
├── LinearLayout [topLayout 改造] 高度48mm
│   ├── ImageView（应用Logo/名称，带图标）
│   ├── TvRecyclerView（分类Tab，内嵌到顶部）←【关键变化】
│   ├── ImageView（搜索图标）
│   └── TextView（时间）
└── LinearLayout [contentLayout]
    └── NoScrollViewPager
        └── UserFragment（首页内容 - 重构）
            ├── ViewPager2 [bannerArea] 高度约35%屏幕
            │   └── 轮播图卡片（带标题、简介、播放按钮）
            ├── ScrollView（内容区域）
            │   ├── 内容行1：[推荐] 标题 + TvRecyclerView（横向）
            │   ├── 内容行2：[电影] 标题 + TvRecyclerView（横向）
            │   └── 内容行3：[剧集] 标题 + TvRecyclerView（横向）
            └── LinearLayout（底部功能按钮 - 精简为图标行）
```

### 3.2 分类Tab迁移方案

**现状：** 分类Tab在 `contentLayout` 上方独占一行

**目标：** 分类Tab集成到顶部导航栏，与搜索/时间同行

```xml
<!-- 改造后的 activity_home.xml 顶部布局草图 -->
<LinearLayout
    android:id="@+id/topLayout"
    android:layout_height="@dimen/vs_55"  <!-- 从50mm增加到55mm -->
    android:gravity="center_vertical">

    <!-- Logo区域（约120mm宽） -->
    <LinearLayout android:layout_width="@dimen/vs_120">
        <View android:background="?attr/color_theme" />  <!-- 主题色竖条 -->
        <TextView android:id="@+id/tvName" />             <!-- 应用名 -->
    </LinearLayout>

    <!-- 分类Tab（占据中间弹性空间）【从下方移到此处】 -->
    <TvRecyclerView
        android:id="@+id/mGridViewCategory"
        android:layout_weight="1"
        android:layout_height="match_parent" />

    <!-- 右侧工具图标组 -->
    <ImageView android:id="@+id/tvWifi" />     <!-- WiFi状态 -->
    <ImageView android:id="@+id/tvFind" />     <!-- 搜索 -->
    <ImageView android:id="@+id/tvMenu" />     <!-- 设置 -->
    <TextView android:id="@+id/tvDate" />      <!-- 时间 -->
</LinearLayout>
```

**收益：**
- 节省原分类Tab行的 ~55mm 垂直空间
- 首页内容区域获得更多展示面积
- 整体布局更接近现代TV应用风格

### 3.3 Banner轮播图方案

#### 方案选择
由于项目已有 `androidx.recyclerview:recyclerview:1.3.2`，使用 **ViewPager2 + RecyclerView** 方案（ViewPager2已是RecyclerView封装）。

> **注意：** 首页热播数据来源于豆瓣API/站点推荐/历史记录，Banner内容应取热播列表的前5条数据。

#### Banner布局设计

```xml
<!-- 新建文件：res/layout/layout_home_banner.xml -->
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="@dimen/vs_340">  <!-- 约35%屏高：960mm×35%≈336mm -->

    <!-- 背景封面图（模糊效果，100%宽高） -->
    <ImageView
        android:id="@+id/ivBannerBg"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop"
        android:alpha="0.3" />

    <!-- 主轮播区域（ViewPager2 不支持直接在XML中写，改用FrameLayout占位+代码添加） -->
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/bannerViewPager"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:focusable="false" />

    <!-- 底部渐变遮罩 -->
    <View
        android:layout_width="match_parent"
        android:layout_height="@dimen/vs_200"
        android:layout_gravity="bottom"
        android:background="@drawable/shape_banner_gradient_bottom" />

    <!-- 左侧信息区域 -->
    <LinearLayout
        android:layout_gravity="bottom|start"
        android:layout_marginStart="@dimen/vs_60"
        android:layout_marginBottom="@dimen/vs_40"
        android:orientation="vertical">

        <TextView
            android:id="@+id/tvBannerTitle"
            android:textSize="@dimen/ts_36"
            android:textStyle="bold"
            android:textColor="@color/color_FFFFFF"
            android:shadowColor="@color/color_FF000000"
            android:shadowRadius="6" />

        <TextView
            android:id="@+id/tvBannerDesc"
            android:textSize="@dimen/ts_20"
            android:textColor="@color/color_FFFFFF_70"
            android:maxLines="2"
            android:layout_marginTop="@dimen/vs_8" />

        <LinearLayout android:layout_marginTop="@dimen/vs_15">
            <TextView
                android:id="@+id/tvBannerPlay"
                android:text="▶ 立即播放"
                android:background="@drawable/button_banner_play"
                android:focusable="true" />
            <TextView
                android:id="@+id/tvBannerDetail"
                android:text="详情"
                android:background="@drawable/button_banner_detail"
                android:focusable="true" />
        </LinearLayout>
    </LinearLayout>

    <!-- 右侧缩略图轮播指示器 -->
    <LinearLayout
        android:id="@+id/bannerIndicator"
        android:layout_gravity="bottom|end"
        android:layout_marginEnd="@dimen/vs_60"
        android:layout_marginBottom="@dimen/vs_20"
        android:orientation="horizontal" />
</FrameLayout>
```

#### Banner Item布局

```xml
<!-- 新建文件：res/layout/item_home_banner.xml -->
<FrameLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:focusable="true"
    android:background="@drawable/shape_thumb_radius">

    <ImageView
        android:id="@+id/ivBannerThumb"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scaleType="centerCrop" />
</FrameLayout>
```

### 3.4 内容行（Row）组件方案

参考可可影视的"类别标题 + 横向卡片列表"布局：

```xml
<!-- 新建文件：res/layout/layout_home_content_row.xml -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:paddingBottom="@dimen/vs_20">

    <!-- 行标题区 -->
    <LinearLayout
        android:layout_marginStart="@dimen/vs_50"
        android:layout_marginEnd="@dimen/vs_50"
        android:layout_marginBottom="@dimen/vs_10"
        android:orientation="horizontal">

        <!-- 主题色装饰条 -->
        <View
            android:layout_width="@dimen/vs_4"
            android:layout_height="match_parent"
            android:layout_marginEnd="@dimen/vs_10"
            android:background="?attr/color_theme" />

        <TextView
            android:id="@+id/tvRowTitle"
            android:textSize="@dimen/ts_26"
            android:textStyle="bold"
            android:textColor="@color/color_FFFFFF" />

        <Space android:layout_weight="1" />

        <TextView
            android:id="@+id/tvRowMore"
            android:text="更多 >"
            android:textSize="@dimen/ts_20"
            android:textColor="@color/color_FFFFFF_50"
            android:focusable="true"
            android:background="@drawable/shape_user_focus" />
    </LinearLayout>

    <!-- 横向卡片列表 -->
    <TvRecyclerView
        android:id="@+id/rvRowContent"
        android:layout_width="match_parent"
        android:layout_height="@dimen/vs_300"  <!-- 根据卡片高度调整 -->
        android:paddingStart="@dimen/vs_50"
        android:paddingEnd="@dimen/vs_50"
        android:clipToPadding="false"
        app:tv_selectedItemIsCentered="false"
        app:tv_horizontalSpacingWithMargins="@dimen/vs_15" />
</LinearLayout>
```

### 3.5 重构后的 UserFragment 布局方案

```xml
<!-- 改造 res/layout/fragment_user.xml -->
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical">

    <!-- 区块1：Banner 轮播图（35%高度） -->
    <include layout="@layout/layout_home_banner"
        android:id="@+id/homeBannerArea" />

    <!-- 区块2：快捷功能入口（精简版，图标+文字，40mm高） -->
    <HorizontalScrollView
        android:layout_height="@dimen/vs_60"
        android:scrollbars="none">
        <LinearLayout
            android:id="@+id/tvUserHome"
            android:orientation="horizontal"
            android:paddingStart="@dimen/vs_50"
            android:paddingEnd="@dimen/vs_50">
            <!-- 7个功能入口：高度从100mm压缩到60mm -->
        </LinearLayout>
    </HorizontalScrollView>

    <!-- 区块3：内容行区域（可纵向滚动） -->
    <ScrollView
        android:id="@+id/homeContentScroll"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:scrollbars="none"
        android:focusable="false">

        <LinearLayout
            android:orientation="vertical">

            <!-- 动态添加多个 layout_home_content_row -->
            <!-- Java代码中通过 inflate + addView 动态添加 -->

        </LinearLayout>
    </ScrollView>
</LinearLayout>
```

### 3.6 首页改造步骤清单

#### Step 1：顶部导航栏集成分类Tab

| 属性 | 详情 |
|------|------|
| **修改文件** | `res/layout/activity_home.xml` |
| **改造内容** | 将 `mGridViewCategory` 从 `contentLayout` 移入 `topLayout`，设置 `layout_weight="1"` 填充中间空间 |
| **Java修改** | `HomeActivity.java` — 更新 `mGridView` 的 layoutManager 和间距设置 |
| **预期效果** | 顶部栏集成导航，释放约55mm内容高度 |
| **焦点注意** | `tvName.nextFocusDown` 需更新为内容区第一个可聚焦View |

#### Step 2：新建Banner轮播组件

| 属性 | 详情 |
|------|------|
| **新建文件** | `res/layout/layout_home_banner.xml`、`res/layout/item_home_banner.xml` |
| **新建Java** | `ui/adapter/HomeBannerAdapter.java`（继承RecyclerView.Adapter） |
| **修改文件** | `ui/fragment/UserFragment.java` — 添加 ViewPager2 初始化和自动轮播逻辑 |
| **数据来源** | 复用现有热播列表数据 `homeHotVodAdapter` 的前5条 |
| **技术要点** | 使用 Handler + postDelayed 实现自动轮播（4秒间隔），遥控器左右键切换 |
| **新建Drawable** | `button_banner_play.xml`（主题色背景+圆角）、`shape_banner_gradient_bottom.xml`（底部渐变遮罩） |

#### Step 3：内容行组件构建

| 属性 | 详情 |
|------|------|
| **新建文件** | `res/layout/layout_home_content_row.xml` |
| **修改文件** | `res/layout/fragment_user.xml` — 添加 ScrollView 内容容器 |
| **修改Java** | `UserFragment.java` — 动态创建并填充多个内容行 |
| **数据策略** | 行1：推荐/豆瓣热播；行2~N：利用现有GridFragment数据或API推荐数据 |
| **注意事项** | ScrollView 内嵌 TvRecyclerView 需特殊处理焦点（见焦点方案章节） |

#### Step 4：快捷功能按钮压缩

| 属性 | 详情 |
|------|------|
| **修改文件** | `res/layout/fragment_user.xml` 中的 `tvUserHome` 区域 |
| **改造内容** | 高度从100mm → 55mm，图标从40mm → 30mm，字体从24mm → 18mm |
| **布局调整** | 改为水平 ScrollView 包裹，在小屏TV上不截断 |
| **可见性** | 保留所有7个功能按钮的显示/隐藏逻辑（`HOME_SEARCH_POSITION` 等配置） |

---

## 四、卡片组件美化方案

### 4.1 当前卡片问题分析

```
当前 item_grid.xml / item_user_hot_vod.xml：
├── 卡片尺寸：214mm × 280mm（≈ 3:4.2 略拉长）
├── 圆角：仅外壳有 14px（约1mm，几乎不可见）
├── 无阴影/投影效果
├── 封面图占满整个卡片
├── 底部信息条（片名）：60%黑色背景 + 下圆角14mm
├── 更新状态标签（tvNote）：小方形，位置在左下角片名上方
├── 年份/地区标签：位置在左上角
└── 焦点态：主题色40%背景 + 2mm白色边框（直接套在外壳上）
```

### 4.2 目标卡片效果（参考可可影视）

```
目标 item_grid_new.xml：
├── 卡片尺寸：210mm × 290mm（2:2.76，保持接近原比例）
├── 圆角：12mm（明显圆角，现代感）
├── 焦点态：缩放 1.05 + 主题色边框 + 阴影加深
├── 封面图：圆角裁切填充
├── 底部信息区：渐变遮罩（透明→黑80%）+ 白色文字
│   ├── 片名：22mm，居中，单行带跑马灯
│   └── 副信息：18mm，年份/评分，70%白
├── 右上角"更新状态"标签：主题色背景，圆角5mm
└── 左上角"来源"标签：深灰背景，小字
```

### 4.3 卡片改造方案

#### 新卡片布局设计

```xml
<!-- 修改 res/layout/item_user_hot_vod.xml（影响最广的卡片） -->
<FrameLayout
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:background="@drawable/shape_card_container"   <!-- 新建 -->
    android:focusable="true"
    android:padding="@dimen/vs_1">

    <FrameLayout
        android:id="@+id/mItemFrame"
        android:layout_width="@dimen/vs_210"
        android:layout_height="@dimen/vs_290">

        <!-- 封面图（圆角裁切） -->
        <ImageView
            android:id="@+id/ivThumb"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:scaleType="centerCrop" />
        <!-- 代码中使用 Glide RoundedCorners(12mm转px) 裁切 -->

        <!-- 底部渐变信息区（全宽渐变遮罩+文字） -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="@dimen/vs_90"
            android:layout_gravity="bottom"
            android:background="@drawable/shape_card_bottom_gradient"  <!-- 新建渐变 -->
            android:orientation="vertical"
            android:padding="@dimen/vs_8"
            android:gravity="bottom">

            <!-- 更新集数/状态标签（右下角浮动） -->
            <TextView
                android:id="@+id/tvNote"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_gravity="end"
                android:background="@drawable/shape_card_tag_note"  <!-- 主题色胶囊 -->
                android:paddingLeft="@dimen/vs_6"
                android:paddingRight="@dimen/vs_6"
                android:paddingTop="@dimen/vs_2"
                android:paddingBottom="@dimen/vs_2"
                android:textColor="@color/color_FFFFFF"
                android:textSize="@dimen/ts_16" />

            <!-- 片名 -->
            <TextView
                android:id="@+id/tvName"
                android:layout_width="match_parent"
                android:layout_height="wrap_content"
                android:ellipsize="marquee"
                android:gravity="start"
                android:marqueeRepeatLimit="marquee_forever"
                android:singleLine="true"
                android:textColor="@color/color_FFFFFF"
                android:textSize="@dimen/ts_22"
                android:textStyle="bold"
                android:layout_marginTop="@dimen/vs_3" />

            <!-- 年份 / 评分副信息 -->
            <TextView
                android:id="@+id/tvYear"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:textColor="@color/color_FFFFFF_70"
                android:textSize="@dimen/ts_16" />
        </LinearLayout>

        <!-- 左上角来源标签（可选显示） -->
        <TextView
            android:id="@+id/tvSite"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_margin="@dimen/vs_6"
            android:background="@drawable/shape_card_tag_site"
            android:paddingLeft="@dimen/vs_5"
            android:paddingRight="@dimen/vs_5"
            android:paddingTop="@dimen/vs_2"
            android:paddingBottom="@dimen/vs_2"
            android:textColor="@color/color_FFFFFF_80"
            android:textSize="@dimen/ts_16"
            android:visibility="gone" />

        <!-- 删除模式遮罩（保持原有逻辑） -->
        <FrameLayout
            android:id="@+id/delFrameLayout"
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            android:background="@drawable/shape_user_delete"
            android:visibility="gone">
            <ImageView
                android:id="@+id/imageView"
                android:layout_width="@dimen/vs_80"
                android:layout_height="@dimen/vs_80"
                android:layout_gravity="center"
                android:src="@drawable/icon_delete" />
        </FrameLayout>

    </FrameLayout>
</FrameLayout>
```

#### 新增 Drawable 文件

```xml
<!-- 1. res/drawable/shape_card_container.xml — 卡片外壳（圆角+焦点） -->
<selector>
    <item android:state_focused="true">
        <shape>
            <corners android:radius="@dimen/vs_12" />
            <solid android:color="?attr/color_theme_40" />
            <stroke android:width="@dimen/vs_2" android:color="@color/color_FFFFFF" />
        </shape>
    </item>
    <item android:state_focused="false">
        <shape>
            <corners android:radius="@dimen/vs_12" />
            <solid android:color="@color/color_3D3D3D_45" />
        </shape>
    </item>
</selector>

<!-- 2. res/drawable/shape_card_bottom_gradient.xml — 底部渐变遮罩 -->
<shape>
    <gradient
        android:angle="90"
        android:startColor="@color/color_FF000000"
        android:centerColor="@color/color_000000_80"
        android:endColor="@android:color/transparent" />
    <corners
        android:bottomLeftRadius="@dimen/vs_12"
        android:bottomRightRadius="@dimen/vs_12" />
</shape>

<!-- 3. res/drawable/shape_card_tag_note.xml — 更新状态标签（主题色） -->
<shape>
    <corners android:radius="@dimen/vs_4" />
    <solid android:color="?attr/color_theme_70" />
</shape>

<!-- 4. res/drawable/shape_card_tag_site.xml — 来源标签（深灰） -->
<shape>
    <corners android:radius="@dimen/vs_4" />
    <solid android:color="@color/color_000000_60" />
</shape>
```

### 4.4 卡片焦点缩放动画

在 `HomeHotVodAdapter.java` 中为 itemView 添加焦点动画：

```java
// 在 convert() 方法中添加
holder.itemView.setOnFocusChangeListener((v, hasFocus) -> {
    if (hasFocus) {
        v.animate()
            .scaleX(1.05f)
            .scaleY(1.05f)
            .setDuration(150)
            .start();
    } else {
        v.animate()
            .scaleX(1.0f)
            .scaleY(1.0f)
            .setDuration(150)
            .start();
    }
});
```

### 4.5 图片圆角加载

统一在 `ImgUtil.java` 中封装圆角加载方法：

```java
// 新增方法：加载带圆角的封面图
public static void loadWithRoundedCorners(Context ctx, String url, ImageView iv, int radiusDp) {
    int radiusPx = AutoSizeUtils.mm2px(ctx, radiusDp);
    RequestOptions options = new RequestOptions()
        .transform(new CenterCrop(), new RoundedCorners(radiusPx))
        .placeholder(R.drawable.img_loading_placeholder)
        .error(R.drawable.icon_img_placeholder)
        .diskCacheStrategy(DiskCacheStrategy.ALL);
    // ... Glide 加载
}
```

---

## 五、焦点导航优化方案

### 5.1 首页焦点路径设计

```
【顶部导航栏】
  tvName ──→ [分类Tab1] → [分类Tab2] → ... → tvWifi → tvFind → tvMenu
     ↓                ↓
【Banner区域】
  tvBannerPlay ←→ tvBannerDetail ←→ [指示器]
     ↓
【功能按钮行】
  tvHistory ← tvLive ← tvSearch ← tvFavorite ← tvPush ← tvDrive ← tvSetting
     ↓
【内容行1（推荐）】
  tvRowMore1 ← [卡片1] → [卡片2] → [卡片3] → [卡片4] → ...
     ↓
【内容行2（电影）】
  tvRowMore2 ← [卡片1] → [卡片2] → ...
     ↓
（持续下滚...）
```

### 5.2 关键焦点场景处理

#### 场景1：顶部导航栏 ↔ 内容区

```xml
<!-- 分类Tab下一焦点指向Banner播放按钮 -->
<!-- 在 item_home_sort.xml 中（通过代码动态设置，因为Banner是动态添加的） -->

<!-- 代码中设置：SortAdapter 的 focusChangeListener -->
view.setNextFocusDownId(R.id.tvBannerPlay);  // Tab → Banner播放按钮
tvBannerPlay.setNextFocusUpId(R.id.mGridViewCategory);  // Banner → 回到Tab
```

#### 场景2：ScrollView 内嵌 TvRecyclerView 焦点穿透

这是最复杂的场景。解决方案：

```java
// 在 UserFragment 中重写 ScrollView 的焦点处理
// 方案：使用 NestedScrollView 替代 ScrollView，
// 并为每个 TvRecyclerView 设置 descendantFocusability

// 内容行的 TvRecyclerView 设置：
rvRowContent.setDescendantFocusability(ViewGroup.FOCUS_AFTER_DESCENDANTS);

// ScrollView 设置：监听焦点变化自动滚动到可见区域
scrollView.setOnScrollChangeListener((v, scrollX, scrollY, ...) -> {
    // 自动将焦点元素滚动到可视区域
});

// 关键设置：禁止 ScrollView 本身获取焦点
scrollView.setFocusable(false);
scrollView.setFocusableInTouchMode(false);
```

#### 场景3：内容行间垂直焦点切换

```java
// TvRecyclerView 的 nextFocusDown/Up 通过代码设置
// 行1的 TvRecyclerView.setNextFocusDownId(行2 TvRecyclerView 的ID)
// 最后一行的 TvRecyclerView 焦点到底时停止（不循环）

// TvRecyclerView 内置的边界处理：
tvRecyclerView.setFocusOutFront(false);  // 顶部不越界
tvRecyclerView.setFocusOutEnd(false);    // 底部不越界
```

#### 场景4：Banner轮播焦点管理

```java
// Banner 使用 ViewPager2 时，焦点需要穿透到内部卡片
// 方案：Banner 区域使用 FrameLayout 包装，
// 内部 ViewPager2 设置 focusable="false"
// 通过 tvBannerPlay/tvBannerDetail 按钮接收焦点

// 遥控器左右键在Banner内部切换：
bannerViewPager.registerOnPageChangeCallback(...);
// 通过 KeyEvent.KEYCODE_DPAD_LEFT/RIGHT 手动切换页面
```

### 5.3 全局焦点状态样式规范

#### 统一焦点 Drawable 规范

```
所有可聚焦控件必须实现以下两种状态：

普通态（state_focused=false）：
  ├── background: color_3D3D3D_45（半透明深灰）
  └── 圆角: 具体组件决定（卡片12mm，按钮8mm，Tab6mm）

焦点态（state_focused=true）：
  ├── background: ?attr/color_theme_40（主题色40%透明）
  ├── stroke: 2mm solid @color/color_FFFFFF（白色描边）
  └── 圆角: 与普通态相同
```

#### 焦点动效规范

```
卡片类（item_grid 系列）：
  聚焦：scale 1.0 → 1.05，duration 150ms，Interpolator=AccelerateDecelerate
  失焦：scale 1.05 → 1.0，duration 100ms

按钮类（操作按钮/功能入口）：
  聚焦：scale 1.0 → 1.03，duration 100ms
  失焦：scale 1.03 → 1.0，duration 80ms

文字Tab类（分类Tab/设置菜单）：
  聚焦：仅颜色变化（FFFFFF_70 → FFFFFF），无缩放
  失焦：颜色恢复
```

---

## 六、其他页面美化方案

### 6.1 详情页（DetailActivity）— P1优先级

#### 主要改造点

**改造1：顶部区域添加模糊背景**

```xml
<!-- 在 topLayout 外层添加全屏背景图 -->
<ImageView
    android:id="@+id/ivBgBlur"
    android:layout_width="match_parent"
    android:layout_height="@dimen/vs_340"
    android:scaleType="centerCrop"
    android:alpha="0.25" />
<!-- 代码中：使用 Glide 加载封面图，BlurTransformation 处理 -->
```

**改造2：封面图添加圆角**

```xml
<!-- ivThumb 改造 -->
android:background="@drawable/shape_card_container"
<!-- Glide 加载使用 RoundedCorners(12) -->
```

**改造3：元信息标签胶囊化**

```xml
<!-- 年份/地区/语言/类型 从纯文字改为胶囊标签 -->
<TextView
    android:id="@+id/tvYear"
    android:background="@drawable/shape_card_tag_site"  <!-- 复用卡片标签样式 -->
    android:paddingLeft="@dimen/vs_8"
    android:paddingRight="@dimen/vs_8"
    android:paddingTop="@dimen/vs_3"
    android:paddingBottom="@dimen/vs_3" />
```

**改造4：操作按钮改为图标+文字**

```xml
<!-- 现在是纯文字按钮，改为图标+文字 -->
<LinearLayout
    android:id="@+id/tvPlay"
    android:background="@drawable/button_detail_primary"   <!-- 新建：主题色填充 -->
    android:orientation="horizontal">
    <ImageView android:src="@drawable/v_play" android:layout_width="@dimen/vs_20" />
    <TextView android:text="@string/det_play" />
</LinearLayout>
<!-- 主播放按钮用主题色填充，其他按钮用半透明描边 -->
```

### 6.2 对话框统一美化方案 — P1优先级

**现状问题：** 所有对话框使用 `shape_dialog_bg_main`（95%灰），圆角15mm，视觉老旧

**改造方案：**

```xml
<!-- 更新 res/drawable/shape_dialog_bg_main.xml -->
<shape>
    <corners android:radius="@dimen/vs_16" />  <!-- 从15mm → 16mm -->
    <solid android:color="@color/color_dialog_bg_new" />  <!-- 新定义颜色 -->
    <!-- 不添加 stroke，保持简洁 -->
</shape>

<!-- 新增颜色定义（colors.xml） -->
<color name="color_dialog_bg_new">#E0252530</color>  <!-- 深蓝黑，更有质感 -->

<!-- 对话框按钮统一规范 -->
<!-- 主要按钮：主题色背景，白色文字 -->
<!-- 次要按钮：描边样式，主题色文字 -->
```

### 6.3 搜索页优化 — P1优先级

**主要改造：**
- 搜索框改为圆角输入框（现为方形），圆角 `vs_25`（半圆胶囊）
- 搜索历史标签（FlowLayout）使用主题色描边胶囊样式
- 热搜词标签增加排名数字（1-10）

### 6.4 历史/收藏页 — P2优先级

**主要改造：**
- 顶部标题栏添加毛玻璃背景效果
- 与主页卡片样式统一（使用新卡片组件）
- 删除模式遮罩改为红色渐变（现为黑色）

---

## 七、分阶段实施计划

### Phase 1：视觉基础建设（预计 3-4 天）

```
Day 1：全局资源改造
  □ 1-1: 新增 Drawable 资源文件（8个新文件）
  □ 1-2: 更新 colors.xml（新增5个颜色变量）
  □ 1-3: 更新 dimens.xml（新增圆角相关尺寸）
  □ 1-4: 全面更新 shape_thumb_radius.xml 圆角值

Day 2：卡片组件重设计
  □ 2-1: 更新 item_user_hot_vod.xml（主卡片）
  □ 2-2: 更新 item_grid.xml（分类页卡片）
  □ 2-3: 更新 item_search.xml（搜索卡片）
  □ 2-4: 更新 HomeHotVodAdapter.java（添加焦点动画）
  □ 2-5: 更新 GridAdapter.java（添加焦点动画）

Day 3：首页Banner组件
  □ 3-1: 新建 layout_home_banner.xml
  □ 3-2: 新建 item_home_banner.xml
  □ 3-3: 新建 HomeBannerAdapter.java
  □ 3-4: 新建 button_banner_play.xml / button_banner_detail.xml

Day 4：首页布局重构
  □ 4-1: 修改 activity_home.xml（Tab迁移到顶部）
  □ 4-2: 修改 fragment_user.xml（新增Banner+内容行）
  □ 4-3: 修改 item_home_sort.xml（字体和间距调整）
  □ 4-4: 修改 HomeActivity.java（更新Tab相关代码）
  □ 4-5: 修改 UserFragment.java（Banner初始化+内容行构建）
```

### Phase 2：核心页面美化（预计 2-3 天）

```
Day 5：详情页改造
  □ 5-1: 修改 activity_detail.xml（模糊背景+胶囊标签+按钮图标化）
  □ 5-2: 新建 button_detail_primary.xml（主要操作按钮）
  □ 5-3: 修改 DetailActivity.java（背景图模糊加载）

Day 6：对话框+搜索页
  □ 6-1: 更新 shape_dialog_bg_main.xml
  □ 6-2: 更新 button_dialog_main.xml
  □ 6-3: 修改 activity_search.xml（搜索框圆角化）
  □ 6-4: 统一 dialog_confirm.xml / dialog_select.xml 等主要对话框

Day 7：焦点优化与联调
  □ 7-1: 全面测试焦点路径（遥控器五向导航）
  □ 7-2: 修复 ScrollView + TvRecyclerView 焦点问题
  □ 7-3: 优化 Banner 遥控器交互
  □ 7-4: 验证主题切换功能正常（8套主题）
```

### Phase 3：体验细节打磨（预计 1-2 天）

```
Day 8：历史/收藏 + 播放控制栏
  □ 8-1: 统一 activity_history.xml / activity_collect.xml 卡片样式
  □ 8-2: 优化 player_vod_control_view.xml（按钮圆形化）

Day 9：全局联调与边界处理
  □ 9-1: 验证所有8套主题在新布局下的显示效果
  □ 9-2: 处理长文字截断、特殊字符等边界情况
  □ 9-3: 性能测试（特别是首页Banner的内存占用）
  □ 9-4: 最终视觉验收
```

---

## 八、风险控制策略

### 8.1 风险识别与应对

| 风险项 | 风险等级 | 描述 | 应对策略 |
|--------|---------|------|----------|
| **ScrollView+TvRecyclerView焦点冲突** | 🔴 高 | TV端在ScrollView内嵌RecyclerView时焦点行为不可预期 | 提前做POC验证，备用方案：改用固定高度多行列表 |
| **ViewPager2兼容性** | 🟡 中 | 项目未使用ViewPager2，引入可能有适配问题 | 备用方案：用 ViewPager + PagerAdapter，或纯 RecyclerView 模拟轮播 |
| **mm单位在不同TV上的显示差异** | 🟡 中 | AutoSize基于屏幕宽度换算，小屏TV可能布局拥挤 | 所有新增尺寸添加注释，预留调整空间 |
| **Banner数据来源问题** | 🟡 中 | 首页热播数据是异步加载，Banner初始化时可能数据未就绪 | 使用Loading占位 + EventBus监听数据就绪事件 |
| **修改item布局影响多处Adapter** | 🟡 中 | item_grid.xml 被多个Adapter使用，字段变更可能报错 | 新建 item_grid_v2.xml，旧版继续保留，逐步迁移 |
| **主题切换对新颜色的兼容** | 🟢 低 | 新增颜色未纳入主题属性系统 | 所有新增交互色使用 `?attr/color_theme` 系列 |
| **首页 Tab 迁移后焦点次序改变** | 🟡 中 | Tab从下方移到顶部后，所有 nextFocusXxx 需重新设置 | 改造前先记录所有 nextFocusDown/Up 设置点 |

### 8.2 代码分支策略

```
master（主分支，不直接修改）
    │
    └── genspark_ai_developer（当前工作分支）
            │
            ├── Phase1：基础改造提交
            ├── Phase2：核心页面提交
            └── Phase3：细节打磨提交
```

### 8.3 回滚预案

```
每个 Phase 完成后创建 git tag：
  git tag -a v1.0-phase1 -m "Phase1完成：基础资源+卡片+首页Banner"
  git tag -a v1.0-phase2 -m "Phase2完成：详情页+对话框+搜索"

如有严重问题：
  git revert --no-commit HEAD~N  （回滚N个提交）
  或
  git checkout v1.0-phase1 -- specific-file.xml  （回滚特定文件）
```

### 8.4 功能完整性检查清单

每个 Phase 结束后执行：
- [ ] 主页分类切换正常
- [ ] 点击内容卡片进入详情页正常
- [ ] 播放功能正常（点播+直播）
- [ ] 搜索功能正常（键盘输入+结果展示）
- [ ] 历史记录/收藏功能正常
- [ ] 设置页面功能正常（特别是主题切换）
- [ ] 遥控器五向导航覆盖所有功能区域
- [ ] 所有8套主题下显示正常

---

## 九、文件改动全量清单

### 9.1 新建文件清单

```
res/layout/
├── layout_home_banner.xml          ← 首页Banner区域
├── item_home_banner.xml            ← Banner卡片Item
└── layout_home_content_row.xml     ← 首页内容行组件

res/drawable/
├── shape_card_container.xml        ← 新卡片外壳（圆角+焦点态）
├── shape_card_bottom_gradient.xml  ← 卡片底部渐变遮罩
├── shape_card_tag_note.xml         ← 更新状态标签（主题色）
├── shape_card_tag_site.xml         ← 来源标签（深灰）
├── shape_banner_gradient_bottom.xml← Banner底部渐变遮罩
├── button_banner_play.xml          ← Banner播放按钮
├── button_banner_detail.xml        ← Banner详情按钮
└── button_detail_primary.xml       ← 详情页主要操作按钮

java/com/github/tvbox/osc/ui/adapter/
└── HomeBannerAdapter.java          ← 首页Banner适配器
```

### 9.2 修改文件清单

```
res/layout/
├── activity_home.xml               ← Tab迁移到顶部栏
├── fragment_user.xml               ← 新增Banner+内容行结构
├── item_user_hot_vod.xml           ← 卡片视觉重设计（主要）
├── item_grid.xml                   ← 卡片视觉重设计（分类）
├── item_search.xml                 ← 卡片视觉重设计（搜索）
├── item_home_sort.xml              ← 分类Tab字体和间距
├── activity_detail.xml             ← 背景+标签+按钮改造
├── activity_search.xml             ← 搜索框圆角化
├── activity_history.xml            ← 统一卡片样式
├── activity_collect.xml            ← 统一卡片样式
├── dialog_confirm.xml              ← 按钮样式统一
└── dialog_select.xml               ← 列表项样式统一

res/drawable/
├── shape_thumb_radius.xml          ← 更新圆角值（14px→12mm）
├── shape_user_focus.xml            ← 更新圆角值
├── shape_user_home.xml             ← 更新圆角值
├── shape_dialog_bg_main.xml        ← 更新颜色和圆角
└── button_dialog_main.xml          ← 更新主题色方案

res/values/
├── colors.xml                      ← 新增颜色变量
└── dimens.xml                      ← 新增 vs_12 圆角规范值

java/com/github/tvbox/osc/
├── ui/activity/HomeActivity.java   ← Tab焦点更新+布局调整
├── ui/fragment/UserFragment.java   ← Banner初始化+内容行构建
├── ui/adapter/HomeHotVodAdapter.java← 焦点动画+新卡片字段
├── ui/adapter/GridAdapter.java     ← 焦点动画+新卡片样式
└── util/ImgUtil.java               ← 新增圆角图片加载方法
```

### 9.3 不修改文件清单（保持稳定）

```
以下核心功能文件不做改动：
├── ui/activity/PlayActivity.java        ← 播放功能核心
├── ui/activity/LivePlayActivity.java    ← 直播功能核心
├── ui/activity/SettingActivity.java     ← 设置功能核心
├── api/ApiConfig.java                   ← API配置核心
├── player/ 所有文件                     ← 播放器核心
├── base/ 所有文件                       ← 基础框架
└── bean/ 所有文件                       ← 数据模型
```

---

## 附录：关键设计决策记录

### 决策1：为什么不用 Leanback 库？

原项目已使用 `TvRecyclerView`（owen/tv-recyclerview），该库已提供TV焦点管理的核心功能，引入Leanback会大幅增加改造量和潜在冲突，且Leanback的UI样式不如原生灵活。**结论：保持现有方案，在TvRecyclerView基础上扩展。**

### 决策2：Banner 使用 ViewPager2 还是自定义 RecyclerView？

ViewPager2 内部就是 RecyclerView，更成熟稳定。但需要评估 `androidx.viewpager2` 是否已在项目中。如果未引入，则改用 `ViewPager`（项目已使用 `NoScrollViewPager`，相关代码已存在）。**结论：优先 ViewPager（风险低），若有问题再切换 ViewPager2。**

### 决策3：首页内容行数据如何获取？

现有 `UserFragment` 只展示一类热播数据，新的多行内容需要不同数据。**解决方案：**
- 行1（推荐）：继续使用现有豆瓣/站点推荐数据
- 行2~N（分类）：通过 `ApiConfig` 读取各分类首页数据，异步加载
- 数据量控制：每行最多显示10条，通过 `app:tv_selectedItemIsCentered="false"` 实现自然左对齐

### 决策4：圆角单位使用 mm 还是 dp？

项目全局使用 mm（AutoSize），新增圆角尺寸也应使用 mm。原有圆角（如`14px`）转换为约 `1mm`（非常小），新设计圆角 12mm 会更明显，在不同尺寸TV上效果一致。**结论：统一使用 mm，新增 `vs_12` 作为标准卡片圆角。**

---

*规划文档制定完毕。请在开始实施前确认以上规划方向，特别是首页Banner数据来源和ViewPager选型方案。*

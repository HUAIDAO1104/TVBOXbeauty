package com.github.tvbox.osc.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.api.ApiConfig;
import com.github.tvbox.osc.base.BaseLazyFragment;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.cache.RoomDataManger;
import com.github.tvbox.osc.event.ServerEvent;
import com.github.tvbox.osc.ui.activity.*;
import com.github.tvbox.osc.ui.adapter.HomeHotVodAdapter;
import com.github.tvbox.osc.ui.dialog.AlistDriveDialog;
import com.github.tvbox.osc.ui.tv.widget.BannerView;
import com.github.tvbox.osc.util.FastClickCheckUtil;
import com.github.tvbox.osc.util.HawkConfig;
import com.github.tvbox.osc.util.UA;
import com.github.tvbox.osc.util.ImgUtil;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.AbsCallback;
import com.lzy.okgo.model.Response;
import com.orhanobut.hawk.Hawk;
import com.owen.tvrecyclerview.widget.TvRecyclerView;
import com.owen.tvrecyclerview.widget.V7GridLayoutManager;
import com.owen.tvrecyclerview.widget.V7LinearLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * 首页 UserFragment
 * 布局：左侧竖向导航 + 右侧多分区内容区（轮播图、站点推荐、热门综艺、热门电影、热门剧集、历史记录）
 */
public class UserFragment extends BaseLazyFragment implements View.OnClickListener {

    // ── 左侧导航按钮 ──────────────────────────────────────
    private LinearLayout tvDrive;
    private LinearLayout tvLive;
    private LinearLayout tvSearch;
    private LinearLayout tvSetting;
    private LinearLayout tvHistory;
    private LinearLayout tvCollect;
    private LinearLayout tvPush;
    private LinearLayout tvDriveCookie;   // 网盘 Cookie 设置

    // ── 右侧内容组件 ──────────────────────────────────────
    private BannerView homeBanner;
    private TextView tvSectionTitle;
    private TextView tvSectionMore;
    private TextView tvVarietyMore;
    private TextView tvMovieMore;
    private TextView tvDramaMore;
    private TextView tvHistoryMore;

    // 站点推荐/豆瓣列表
    public static HomeHotVodAdapter homeHotVodAdapter;
    private List<Movie.Video> homeSourceRec;
    public static TvRecyclerView tvHotListForGrid;
    public static TvRecyclerView tvHotListForLine;

    // 热门综艺
    private HomeHotVodAdapter varietyAdapter;
    private TvRecyclerView tvVarietyList;

    // 热门电影
    private HomeHotVodAdapter movieAdapter;
    private TvRecyclerView tvMovieList;

    // 热门剧集
    private HomeHotVodAdapter dramaAdapter;
    private TvRecyclerView tvDramaList;

    // 历史记录
    private HomeHotVodAdapter historyAdapter;
    private TvRecyclerView tvHistoryList;

    // ── 工厂方法 ──────────────────────────────────────────
    public static UserFragment newInstance() {
        return new UserFragment();
    }

    public static UserFragment newInstance(List<Movie.Video> recVod) {
        return new UserFragment().setArguments(recVod);
    }

    public UserFragment setArguments(List<Movie.Video> recVod) {
        this.homeSourceRec = recVod;
        return this;
    }

    // ── 生命周期：恢复时刷新对应数据 ───────────────────────
    @Override
    public void onFragmentResume() {
        if (!Hawk.get(HawkConfig.HOME_SEARCH_POSITION, true)) {
            if (tvSearch != null) tvSearch.setVisibility(View.VISIBLE);
        } else {
            if (tvSearch != null) tvSearch.setVisibility(View.GONE);
        }
        if (!Hawk.get(HawkConfig.HOME_MENU_POSITION, true)) {
            if (tvSetting != null) tvSetting.setVisibility(View.VISIBLE);
        } else {
            if (tvSetting != null) tvSetting.setVisibility(View.VISIBLE); // 左侧导航始终显示设置
        }

        super.onFragmentResume();

        // HOME_REC=2 时刷新历史记录（主分区）
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {
            refreshMainSectionAsHistory();
        }

        // 每次恢复时刷新历史记录分区
        refreshHistorySection();
    }

    @Override
    protected int getLayoutResID() {
        return R.layout.fragment_user;
    }

    private ImgUtil.Style style;

    @Override
    protected void init() {
        EventBus.getDefault().register(this);

        // ── 绑定左侧导航按钮 ─────────────────────────────
        tvDrive      = findViewById(R.id.tvDrive);
        tvLive       = findViewById(R.id.tvLive);
        tvSearch     = findViewById(R.id.tvSearch);
        tvSetting    = findViewById(R.id.tvSetting);
        tvCollect    = findViewById(R.id.tvFavorite);
        tvHistory    = findViewById(R.id.tvHistory);
        tvPush       = findViewById(R.id.tvPush);
        tvDriveCookie = findViewById(R.id.tvDriveCookie);

        tvDrive.setOnClickListener(this);
        tvLive.setOnClickListener(this);
        tvSearch.setOnClickListener(this);
        tvSetting.setOnClickListener(this);
        tvHistory.setOnClickListener(this);
        tvPush.setOnClickListener(this);
        tvCollect.setOnClickListener(this);
        tvDriveCookie.setOnClickListener(this);

        tvDrive.setOnFocusChangeListener(navBtnFocusListener);
        tvLive.setOnFocusChangeListener(navBtnFocusListener);
        tvSearch.setOnFocusChangeListener(navBtnFocusListener);
        tvSetting.setOnFocusChangeListener(navBtnFocusListener);
        tvHistory.setOnFocusChangeListener(navBtnFocusListener);
        tvPush.setOnFocusChangeListener(navBtnFocusListener);
        tvCollect.setOnFocusChangeListener(navBtnFocusListener);
        tvDriveCookie.setOnFocusChangeListener(navBtnFocusListener);

        tvHistory.setOnLongClickListener(v -> {
            HomeActivity.homeRecf();
            return HomeActivity.reHome(mContext);
        });

        // ── 绑定右侧内容组件 ─────────────────────────────
        homeBanner    = findViewById(R.id.homeBanner);
        tvSectionTitle = findViewById(R.id.tvSectionTitle);
        tvSectionMore  = findViewById(R.id.tvSectionMore);
        tvVarietyMore  = findViewById(R.id.tvVarietyMore);
        tvMovieMore    = findViewById(R.id.tvMovieMore);
        tvDramaMore    = findViewById(R.id.tvDramaMore);
        tvHistoryMore  = findViewById(R.id.tvHistoryMore);

        tvHotListForLine = findViewById(R.id.tvHotListForLine);
        tvHotListForGrid = findViewById(R.id.tvHotListForGrid);
        tvVarietyList    = findViewById(R.id.tvVarietyList);
        tvMovieList      = findViewById(R.id.tvMovieList);
        tvDramaList      = findViewById(R.id.tvDramaList);
        tvHistoryList    = findViewById(R.id.tvHistoryList);

        // ── 初始化主分区（站点推荐/豆瓣/历史）───────────
        tvHotListForGrid.setHasFixedSize(true);
        int spanCount = 5;
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 1 && homeSourceRec != null) {
            style = ImgUtil.initStyle();
        }
        if (style != null && Hawk.get(HawkConfig.HOME_REC, 0) == 1) {
            spanCount = ImgUtil.spanCountByStyle(style, spanCount);
        }
        tvHotListForGrid.setLayoutManager(new V7GridLayoutManager(this.mContext, spanCount));

        String tvRate = "";
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 0) {
            tvRate = "豆瓣热播";
        } else if (Hawk.get(HawkConfig.HOME_REC, 0) == 1) {
            tvRate = homeSourceRec != null ? "站点推荐" : "豆瓣热播";
        }
        if (tvSectionTitle != null) {
            if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {
                tvSectionTitle.setText("最近观看");
            } else if (!tvRate.isEmpty()) {
                tvSectionTitle.setText(tvRate);
            } else {
                tvSectionTitle.setText("为您推荐");
            }
        }

        homeHotVodAdapter = new HomeHotVodAdapter(style, tvRate);
        setupVodAdapterClick(homeHotVodAdapter, true);
        setupRecyclerView(tvHotListForLine, homeHotVodAdapter, false);
        setupRecyclerView(tvHotListForGrid, homeHotVodAdapter, true);
        initHomeHotVod(homeHotVodAdapter);

        if (Hawk.get(HawkConfig.HOME_REC_STYLE, false)) {
            tvHotListForGrid.setVisibility(View.VISIBLE);
            tvHotListForLine.setVisibility(View.GONE);
        } else {
            tvHotListForGrid.setVisibility(View.GONE);
            tvHotListForLine.setVisibility(View.VISIBLE);
        }

        // ── 初始化各分区 ─────────────────────────────────
        initBanner();
        initVarietySection();
        initMovieSection();
        initDramaSection();
        refreshHistorySection();

        // ── 查看全部按钮 ─────────────────────────────────
        if (tvSectionMore != null) {
            tvSectionMore.setOnClickListener(v -> {
                FastClickCheckUtil.check(v);
                jumpActivity(SearchActivity.class);
            });
        }
        if (tvVarietyMore != null) {
            tvVarietyMore.setOnClickListener(v -> {
                FastClickCheckUtil.check(v);
                searchByType("综艺");
            });
        }
        if (tvMovieMore != null) {
            tvMovieMore.setOnClickListener(v -> {
                FastClickCheckUtil.check(v);
                searchByType("电影");
            });
        }
        if (tvDramaMore != null) {
            tvDramaMore.setOnClickListener(v -> {
                FastClickCheckUtil.check(v);
                searchByType("剧集");
            });
        }
        if (tvHistoryMore != null) {
            tvHistoryMore.setOnClickListener(v -> {
                FastClickCheckUtil.check(v);
                jumpActivity(HistoryActivity.class);
            });
        }
    }

    // ── 搜索跳转 ─────────────────────────────────────────
    private void searchByType(String type) {
        Intent intent;
        if (Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)) {
            intent = new Intent(mContext, FastSearchActivity.class);
        } else {
            intent = new Intent(mContext, SearchActivity.class);
        }
        intent.putExtra("title", type);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        mActivity.startActivity(intent);
    }

    // ── 轮播图初始化 ──────────────────────────────────────
    private void initBanner() {
        if (homeBanner == null) return;
        List<Movie.Video> bannerData = new ArrayList<>();
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 1 && homeSourceRec != null) {
            for (Movie.Video v : homeSourceRec) {
                if (v.pic != null && !v.pic.isEmpty()) {
                    bannerData.add(v);
                    if (bannerData.size() >= 8) break;  // 最多显示8张
                }
            }
        }
        if (!bannerData.isEmpty()) {
            homeBanner.setVisibility(View.VISIBLE);
            homeBanner.setData(bannerData);
            homeBanner.setOnBannerItemClickListener(video -> {
                if (video.id != null && !video.id.isEmpty()) {
                    Bundle bundle = new Bundle();
                    bundle.putString("id", video.id);
                    bundle.putString("sourceKey", video.sourceKey);
                    jumpActivity(DetailActivity.class, bundle);
                }
            });
        } else {
            homeBanner.setVisibility(View.GONE);
        }
    }

    // ── 热门综艺分区 ──────────────────────────────────────
    private void initVarietySection() {
        if (tvVarietyList == null) return;
        varietyAdapter = new HomeHotVodAdapter(null, "综艺");
        setupVodAdapterClick(varietyAdapter, false);
        setupRecyclerView(tvVarietyList, varietyAdapter, false);
        loadDoubanCategory("综艺", varietyAdapter);
    }

    // ── 热门电影分区 ──────────────────────────────────────
    private void initMovieSection() {
        if (tvMovieList == null) return;
        movieAdapter = new HomeHotVodAdapter(null, "电影");
        setupVodAdapterClick(movieAdapter, false);
        setupRecyclerView(tvMovieList, movieAdapter, false);
        loadDoubanCategory("电影", movieAdapter);
    }

    // ── 热门剧集分区 ──────────────────────────────────────
    private void initDramaSection() {
        if (tvDramaList == null) return;
        dramaAdapter = new HomeHotVodAdapter(null, "剧集");
        setupVodAdapterClick(dramaAdapter, false);
        setupRecyclerView(tvDramaList, dramaAdapter, false);
        loadDoubanCategory("剧集", dramaAdapter);
    }

    // ── 历史记录分区 ──────────────────────────────────────
    private void refreshHistorySection() {
        if (tvHistoryList == null) return;
        if (historyAdapter == null) {
            historyAdapter = new HomeHotVodAdapter(null, "历史");
            setupVodAdapterClick(historyAdapter, false);
            setupRecyclerView(tvHistoryList, historyAdapter, false);
        }
        List<VodInfo> allVodRecord = RoomDataManger.getAllVodRecord(20);
        List<Movie.Video> vodList = new ArrayList<>();
        for (VodInfo vodInfo : allVodRecord) {
            Movie.Video vod = new Movie.Video();
            vod.id = vodInfo.id;
            vod.sourceKey = vodInfo.sourceKey;
            vod.name = vodInfo.name;
            vod.pic = vodInfo.pic;
            if (vodInfo.playNote != null && !vodInfo.playNote.isEmpty()) {
                vod.note = "上次看到" + vodInfo.playNote;
            }
            vodList.add(vod);
        }
        historyAdapter.setNewData(vodList);
    }

    // ── 从豆瓣加载分类数据 ────────────────────────────────
    private void loadDoubanCategory(String tag, HomeHotVodAdapter adapter) {
        String cacheKey = "home_douban_" + tag;
        String cacheDayKey = "home_douban_day_" + tag;
        try {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);
            String today = String.format("%d%d%d", year, month, day);
            String requestDay = Hawk.get(cacheDayKey, "");
            if (requestDay.equals(today)) {
                String json = Hawk.get(cacheKey, "");
                if (!json.isEmpty()) {
                    adapter.setNewData(loadHots(json));
                    return;
                }
            }
            // 豆瓣分类热播 API
            String doubanUrl = "https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=" +
                    tag + "&playable=1&start=0";
            String userAgent = UA.random();
            OkGo.<String>get(doubanUrl).headers("User-Agent", userAgent).execute(new AbsCallback<String>() {
                @Override
                public void onSuccess(Response<String> response) {
                    String netJson = response.body();
                    Hawk.put(cacheDayKey, today);
                    Hawk.put(cacheKey, netJson);
                    mActivity.runOnUiThread(() -> adapter.setNewData(loadHots(netJson)));
                }

                @Override
                public String convertResponse(okhttp3.Response response) throws Throwable {
                    return response.body().string();
                }
            });
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    // ── 主分区切换为历史记录（HOME_REC=2）─────────────────
    private void refreshMainSectionAsHistory() {
        if (homeHotVodAdapter == null) return;
        List<VodInfo> allVodRecord = RoomDataManger.getAllVodRecord(20);
        List<Movie.Video> vodList = new ArrayList<>();
        for (VodInfo vodInfo : allVodRecord) {
            Movie.Video vod = new Movie.Video();
            vod.id = vodInfo.id;
            vod.sourceKey = vodInfo.sourceKey;
            vod.name = vodInfo.name;
            vod.pic = vodInfo.pic;
            if (vodInfo.playNote != null && !vodInfo.playNote.isEmpty()) {
                vod.note = "上次看到" + vodInfo.playNote;
            }
            vodList.add(vod);
        }
        homeHotVodAdapter.setNewData(vodList);
    }

    // ── 公共方法：设置 RecyclerView ─────────────────────
    private void setupRecyclerView(TvRecyclerView rv, HomeHotVodAdapter adapter, boolean isGrid) {
        rv.setOnItemListener(new TvRecyclerView.OnItemListener() {
            @Override
            public void onItemPreSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate()
                        .scaleX(1.0f).scaleY(1.0f)
                        .setDuration(180)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }
            @Override
            public void onItemSelected(TvRecyclerView parent, View itemView, int position) {
                itemView.animate()
                        .scaleX(1.05f).scaleY(1.05f)
                        .setDuration(180)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            }
            @Override
            public void onItemClick(TvRecyclerView parent, View itemView, int position) {}
        });
        if (!isGrid) {
            rv.setLayoutManager(new V7LinearLayoutManager(mContext, 1, false));
        }
        rv.setAdapter(adapter);
    }

    // ── 公共方法：设置 Adapter 点击事件 ─────────────────
    private void setupVodAdapterClick(HomeHotVodAdapter adapter, boolean isMainSection) {
        adapter.setOnItemClickListener((a, view, position) -> {
            if (ApiConfig.get().getSourceBeanList().isEmpty()) return;
            Movie.Video vod = (Movie.Video) a.getItem(position);
            if (isMainSection && (vod.id != null && !vod.id.isEmpty()) &&
                    (Hawk.get(HawkConfig.HOME_REC, 0) == 2) && HawkConfig.hotVodDelete) {
                adapter.remove(position);
                VodInfo vodInfo = RoomDataManger.getVodInfo(vod.sourceKey, vod.id);
                RoomDataManger.deleteVodRecord(vod.sourceKey, vodInfo);
                Toast.makeText(mContext, getString(R.string.hm_hist_del), Toast.LENGTH_SHORT).show();
            } else if (vod.id != null && !vod.id.isEmpty()) {
                Bundle bundle = new Bundle();
                bundle.putString("id", vod.id);
                bundle.putString("sourceKey", vod.sourceKey);
                if (vod.id.startsWith("msearch:")) {
                    bundle.putString("title", vod.name);
                    jumpActivity(FastSearchActivity.class, bundle);
                } else {
                    jumpActivity(DetailActivity.class, bundle);
                }
            } else {
                Intent newIntent;
                if (Hawk.get(HawkConfig.FAST_SEARCH_MODE, false)) {
                    newIntent = new Intent(mContext, FastSearchActivity.class);
                } else {
                    newIntent = new Intent(mContext, SearchActivity.class);
                }
                newIntent.putExtra("title", vod.name);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mActivity.startActivity(newIntent);
            }
        });

        if (isMainSection) {
            adapter.setOnItemLongClickListener((a, view, position) -> {
                if (ApiConfig.get().getSourceBeanList().isEmpty()) return false;
                Movie.Video vod = (Movie.Video) a.getItem(position);
                if ((vod.id != null && !vod.id.isEmpty()) && (Hawk.get(HawkConfig.HOME_REC, 0) == 2)) {
                    HawkConfig.hotVodDelete = !HawkConfig.hotVodDelete;
                    adapter.notifyDataSetChanged();
                } else {
                    Intent newIntent = new Intent(mContext, FastSearchActivity.class);
                    newIntent.putExtra("title", vod.name);
                    newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    mActivity.startActivity(newIntent);
                }
                return true;
            });
        }
    }

    // ── 主分区数据加载 ────────────────────────────────────
    private void initHomeHotVod(HomeHotVodAdapter adapter) {
        if (Hawk.get(HawkConfig.HOME_REC, 0) == 1) {
            if (homeSourceRec != null) {
                adapter.setNewData(homeSourceRec);
            }
            return;
        } else if (Hawk.get(HawkConfig.HOME_REC, 0) == 2) {
            return;
        }
        try {
            Calendar cal = Calendar.getInstance();
            int year = cal.get(Calendar.YEAR);
            int month = cal.get(Calendar.MONTH) + 1;
            int day = cal.get(Calendar.DATE);
            String today = String.format("%d%d%d", year, month, day);
            String requestDay = Hawk.get("home_hot_day", "");
            if (requestDay.equals(today)) {
                String json = Hawk.get("home_hot", "");
                if (!json.isEmpty()) {
                    adapter.setNewData(loadHots(json));
                    return;
                }
            }
            String doubanHotURL = "https://movie.douban.com/j/new_search_subjects?sort=U&range=0,10&tags=&playable=1&start=0&year_range=" + year + "," + year;
            String userAgent = UA.random();
            OkGo.<String>get(doubanHotURL).headers("User-Agent", userAgent).execute(new AbsCallback<String>() {
                @Override
                public void onSuccess(Response<String> response) {
                    String netJson = response.body();
                    Hawk.put("home_hot_day", today);
                    Hawk.put("home_hot", netJson);
                    mActivity.runOnUiThread(() -> adapter.setNewData(loadHots(netJson)));
                }

                @Override
                public String convertResponse(okhttp3.Response response) throws Throwable {
                    return response.body().string();
                }
            });
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }

    private ArrayList<Movie.Video> loadHots(String json) {
        ArrayList<Movie.Video> result = new ArrayList<>();
        try {
            JsonObject infoJson = new Gson().fromJson(json, JsonObject.class);
            JsonArray array = infoJson.getAsJsonArray("data");
            for (JsonElement ele : array) {
                JsonObject obj = (JsonObject) ele;
                Movie.Video vod = new Movie.Video();
                vod.name = obj.get("title").getAsString();
                vod.note = obj.get("rate").getAsString();
                vod.pic = obj.get("cover").getAsString() + "@User-Agent=" + UA.random() + "@Referer=https://www.douban.com/";
                result.add(vod);
            }
        } catch (Throwable th) {
            // ignore
        }
        return result;
    }

    // ── 导航按钮焦点动画 ──────────────────────────────────
    private final View.OnFocusChangeListener navBtnFocusListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            v.animate()
                    .scaleX(hasFocus ? 1.08f : 1.0f)
                    .scaleY(hasFocus ? 1.08f : 1.0f)
                    .setDuration(200)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    };

    // ── 点击事件 ──────────────────────────────────────────
    @Override
    public void onClick(View v) {
        HawkConfig.hotVodDelete = false;
        FastClickCheckUtil.check(v);
        int id = v.getId();
        if (id == R.id.tvLive) {
            jumpActivity(LivePlayActivity.class);
        } else if (id == R.id.tvSearch) {
            jumpActivity(SearchActivity.class);
        } else if (id == R.id.tvSetting) {
            jumpActivity(SettingActivity.class);
        } else if (id == R.id.tvHistory) {
            jumpActivity(HistoryActivity.class);
        } else if (id == R.id.tvPush) {
            jumpActivity(PushActivity.class);
        } else if (id == R.id.tvFavorite) {
            jumpActivity(CollectActivity.class);
        } else if (id == R.id.tvDrive) {
            jumpActivity(DriveActivity.class);
        } else if (id == R.id.tvDriveCookie) {
            // 打开网盘 Cookie 设置对话框
            try {
                AlistDriveDialog dialog = new AlistDriveDialog(mContext, null);
                dialog.show();
            } catch (Throwable e) {
                // 如果 AlistDriveDialog 不支持 null，直接跳转 DriveActivity
                jumpActivity(DriveActivity.class);
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void server(ServerEvent event) {
        if (event.type == ServerEvent.SERVER_CONNECTION) {
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}

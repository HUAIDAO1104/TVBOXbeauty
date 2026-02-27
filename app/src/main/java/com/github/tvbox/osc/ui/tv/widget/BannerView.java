package com.github.tvbox.osc.ui.tv.widget;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.Movie;
import com.github.tvbox.osc.util.ImgUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页轮播图组件
 * - 自动播放（5秒间隔）
 * - 支持焦点导航，焦点时暂停自动播放
 * - 圆点指示器跟随滑动
 * - 支持点击回调
 */
public class BannerView extends FrameLayout {

    private static final int AUTO_PLAY_INTERVAL = 5000;

    private ViewPager mViewPager;
    private LinearLayout mDotsContainer;
    private BannerAdapter mAdapter;
    private final List<Movie.Video> mDataList = new ArrayList<>();
    private OnBannerItemClickListener mClickListener;

    private final Handler mHandler = new Handler(Looper.getMainLooper());
    private boolean mAutoPlay = true;
    private boolean mHasFocus = false;

    private final Runnable mAutoPlayRunnable = new Runnable() {
        @Override
        public void run() {
            if (mAutoPlay && !mHasFocus && mDataList.size() > 1) {
                int next = (mViewPager.getCurrentItem() + 1) % mDataList.size();
                mViewPager.setCurrentItem(next, true);
            }
            mHandler.postDelayed(this, AUTO_PLAY_INTERVAL);
        }
    };

    public BannerView(Context context) {
        super(context);
        init(context);
    }

    public BannerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public BannerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.view_banner, this, true);
        mViewPager = findViewById(R.id.bannerViewPager);
        mDotsContainer = findViewById(R.id.bannerDots);

        mAdapter = new BannerAdapter(context);
        mViewPager.setAdapter(mAdapter);
        mViewPager.setOffscreenPageLimit(2);

        // 轮播图切换动画
        mViewPager.setPageTransformer(false, (page, position) -> {
            float absPos = Math.abs(position);
            page.setAlpha(1.0f - absPos * 0.3f);
            page.setScaleY(1.0f - absPos * 0.05f);
        });

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

            @Override
            public void onPageSelected(int position) {
                updateDots(position % Math.max(mDataList.size(), 1));
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        // 焦点变化：聚焦时暂停自动播放
        mViewPager.setOnFocusChangeListener((v, hasFocus) -> {
            mHasFocus = hasFocus;
        });
        setFocusable(true);
        setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    }

    public void setData(List<Movie.Video> dataList) {
        mDataList.clear();
        if (dataList != null) {
            mDataList.addAll(dataList);
        }
        mAdapter.notifyDataSetChanged();
        buildDots();
        if (!mDataList.isEmpty()) {
            mViewPager.setCurrentItem(0, false);
        }
    }

    public void setOnBannerItemClickListener(OnBannerItemClickListener listener) {
        this.mClickListener = listener;
    }

    public void startAutoPlay() {
        mAutoPlay = true;
        mHandler.removeCallbacks(mAutoPlayRunnable);
        mHandler.postDelayed(mAutoPlayRunnable, AUTO_PLAY_INTERVAL);
    }

    public void stopAutoPlay() {
        mAutoPlay = false;
        mHandler.removeCallbacks(mAutoPlayRunnable);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        startAutoPlay();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        stopAutoPlay();
    }

    private void buildDots() {
        mDotsContainer.removeAllViews();
        int count = mDataList.size();
        if (count <= 1) {
            mDotsContainer.setVisibility(View.GONE);
            return;
        }
        mDotsContainer.setVisibility(View.VISIBLE);
        for (int i = 0; i < count; i++) {
            View dot = new View(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    (int) getResources().getDimension(R.dimen.vs_8),
                    (int) getResources().getDimension(R.dimen.vs_8));
            lp.setMargins(
                    (int) getResources().getDimension(R.dimen.vs_4), 0,
                    (int) getResources().getDimension(R.dimen.vs_4), 0);
            dot.setLayoutParams(lp);
            dot.setBackground(i == 0
                    ? getResources().getDrawable(R.drawable.shape_banner_dot_active, getContext().getTheme())
                    : getResources().getDrawable(R.drawable.shape_banner_dot_normal, getContext().getTheme()));
            mDotsContainer.addView(dot);
        }
    }

    private void updateDots(int position) {
        int count = mDotsContainer.getChildCount();
        for (int i = 0; i < count; i++) {
            View dot = mDotsContainer.getChildAt(i);
            if (dot != null) {
                dot.setBackground(i == position
                        ? getResources().getDrawable(R.drawable.shape_banner_dot_active, getContext().getTheme())
                        : getResources().getDrawable(R.drawable.shape_banner_dot_normal, getContext().getTheme()));
            }
        }
    }

    public interface OnBannerItemClickListener {
        void onItemClick(Movie.Video video);
    }

    private class BannerAdapter extends PagerAdapter {

        private final Context mContext;

        BannerAdapter(Context context) {
            this.mContext = context;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View itemView = LayoutInflater.from(mContext).inflate(R.layout.item_banner, container, false);
            Movie.Video video = mDataList.get(position);

            ImageView ivThumb = itemView.findViewById(R.id.ivBannerThumb);
            TextView tvName = itemView.findViewById(R.id.tvBannerName);
            TextView tvDesc = itemView.findViewById(R.id.tvBannerDesc);
            TextView tvType = itemView.findViewById(R.id.tvBannerType);
            TextView tvYear = itemView.findViewById(R.id.tvBannerYear);

            // 加载封面图（使用ImgUtil.loadBanner：ARGB_8888高清、CenterInside不裁切、DATA缓存保全分辨率）
            ImgUtil.loadBanner(video.pic != null ? video.pic : "", ivThumb, 12);

            tvName.setText(video.name != null ? video.name : "");

            if (video.des != null && !video.des.isEmpty()) {
                tvDesc.setText(video.des);
                tvDesc.setVisibility(View.VISIBLE);
            } else {
                tvDesc.setVisibility(View.GONE);
            }

            // type字段为分类名称
            if (video.type != null && !video.type.isEmpty()) {
                tvType.setText(video.type);
                tvType.setVisibility(View.VISIBLE);
            } else {
                tvType.setVisibility(View.GONE);
            }

            // year字段为int类型，0表示未知
            if (video.year > 0) {
                tvYear.setText(String.valueOf(video.year));
                tvYear.setVisibility(View.VISIBLE);
            } else {
                tvYear.setVisibility(View.GONE);
            }

            // 点击事件
            itemView.setOnClickListener(v -> {
                if (mClickListener != null) {
                    mClickListener.onItemClick(video);
                }
            });

            // 焦点变化监听
            itemView.setOnFocusChangeListener((v, hasFocus) -> {
                mHasFocus = hasFocus;
                v.animate()
                        .scaleX(hasFocus ? 1.02f : 1.0f)
                        .scaleY(hasFocus ? 1.02f : 1.0f)
                        .setDuration(200)
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .start();
            });

            container.addView(itemView);
            return itemView;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}

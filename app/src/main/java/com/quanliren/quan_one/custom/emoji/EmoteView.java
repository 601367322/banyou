package com.quanliren.quan_one.custom.emoji;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.PopupWindow;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quanliren.quan_one.activity.R;
import com.quanliren.quan_one.activity.base.BaseActivity;
import com.quanliren.quan_one.activity.seting.EmoticonListActivity_;
import com.quanliren.quan_one.application.AppClass;
import com.quanliren.quan_one.bean.emoticon.EmoticonActivityListBean.EmoticonZip;
import com.quanliren.quan_one.bean.emoticon.EmoticonActivityListBean.EmoticonZip.EmoticonImageBean;
import com.quanliren.quan_one.custom.GalleryNavigator;
import com.quanliren.quan_one.custom.ScrollViewPager;
import com.quanliren.quan_one.dao.DBHelper;
import com.quanliren.quan_one.fragment.base.BaseFragment;
import com.quanliren.quan_one.util.DrawableCache;
import com.quanliren.quan_one.util.ImageUtil;
import com.quanliren.quan_one.util.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

@EFragment(R.layout.emoticon_view)
public class EmoteView extends BaseFragment implements EmoteGridView.EmoticonListener {

    public static final int EMOTICONRESPONSE = 1;

    @ViewById(R.id.tab_ll)
    public LinearLayout tab_ll;
    @ViewById(R.id.emoticon_delete_btn)
    public LinearLayout emoticon_delete_btn;
    @ViewById(R.id.viewpager)
    public ScrollViewPager viewpager;
    @ViewById(R.id.gallerynavigator)
    public GalleryNavigator gallerynavigator;
    GifImageView gif;
    int position = 0;

    EmoteGridView.EmoticonListener listener;

    @Override
    public void init() {
        super.init();
        initView();
    }

    public void setListener(EmoteGridView.EmoticonListener listener) {
        this.listener = listener;
    }

    List<View> tabList = new ArrayList<>();

    private EmoticonsEditText editText;

    public void setEditText(EmoticonsEditText editText) {
        this.editText = editText;
    }

    mPagerAdapter adapter;
    List<Fragment> views = new ArrayList<>();

    public static final int PAGENUM = 21;
    public static final int PAGENUM_LARGE = 8;

    void initView() {

        ArrayList<ArrayList<String>> emotion = new ArrayList<>();

        createTab(R.drawable.weixiao, true);
        ArrayList<String> temp = null;
        for (int i = 0; i < AppClass.mEmoticons2.size(); i++) {
            if (i % PAGENUM == 0) {
                temp = new ArrayList<>();
                emotion.add(temp);
            }
            temp.add(AppClass.mEmoticons2.get(i));
        }

        for (ArrayList<String> emoticon : emotion) {
            EmoticonFragment ef = new EmoticonFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("emoticon", emoticon);
            bundle.putInt("emotionId", 2);
            ef.setArguments(bundle);
            ef.setListener(this);
            views.add(ef);
        }

        emotion = new ArrayList<>();
        createTab(R.drawable.expression, true);
        for (int i = 0; i < AppClass.mEmoticons1.size(); i++) {
            if (i % PAGENUM == 0) {
                temp = new ArrayList<String>();
                emotion.add(temp);
            }
            temp.add(AppClass.mEmoticons1.get(i));
        }

        for (ArrayList<String> emoticon : emotion) {
            EmoticonFragment ef = new EmoticonFragment();
            Bundle bundle = new Bundle();
            bundle.putStringArrayList("emoticon", emoticon);
            bundle.putInt("emotionId", 1);
            ef.setArguments(bundle);
            ef.setListener(this);
            views.add(ef);
        }

        final MyOnPageChangeListener listener = new MyOnPageChangeListener();
        viewpager.setOnPageChangeListener(listener);
        viewpager.setAdapter(adapter = new mPagerAdapter(getChildFragmentManager()));
        viewpager.setOffscreenPageLimit(1);
        viewpager.post(new Runnable() {
            @Override
            public void run() {
                listener.onPageSelected(0);
            }
        });
        gallerynavigator.setPaints(getResources().getColor(R.color.actionbar),
                getResources().getColor(R.color.darkgray));
    }

    DisplayImageOptions options_no_default = new DisplayImageOptions.Builder().build();

    public void addMoreEmote() {
        try {
            List<EmoticonZip> emoticonList = DBHelper.emoticonZipDao.getAllMyEmoticon(ac.getUser().getId());
            for (EmoticonZip emoticonBean : emoticonList) {
                ArrayList<ArrayList<EmoticonImageBean>> emotions_large = new ArrayList<>();
                createTab(ImageLoader.getInstance().loadImageSync(
                        Util.FILE + emoticonBean.getIconfile(), options_no_default), true);
                ArrayList<EmoticonImageBean> temp_e = null;

                for (int i = 0; i < emoticonBean.getImglist().size(); i++) {
                    if (i % PAGENUM_LARGE == 0) {
                        temp_e = new ArrayList<>();
                        emotions_large.add(temp_e);
                    }
                    temp_e.add(emoticonBean.getImglist().get(i));
                }
                for (ArrayList<EmoticonImageBean> emoticon : emotions_large) {
                    EmoticonFragmentLarge ef = new EmoticonFragmentLarge();
                    Bundle bundle = new Bundle();
                    bundle.putSerializable("emoticon", emoticon);
                    ef.setArguments(bundle);
                    ef.setListener(this);
                    views.add(ef);
                }
            }
            adapter.notifyDataSetChanged();

            createTab(R.drawable.ic_add_emoticon, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createTab(int res, boolean b) {
        createTab(((BitmapDrawable) getResources().getDrawable(res))
                .getBitmap(), b);
    }

    public void createTab(Bitmap bitmap, boolean b) {
        LinearLayout ll = new LinearLayout(getActivity());
        ll.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ll.setBackgroundResource(R.drawable.emoticon_tab_left_bg);
        ll.setGravity(Gravity.CENTER);

        ImageView img = new ImageView(getActivity());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ImageUtil.dip2px(getActivity(), 20), ImageUtil.dip2px(
                getActivity(), 20));
        img.setLayoutParams(lp);
        img.setScaleType(ScaleType.CENTER_CROP);
        img.setImageBitmap(bitmap);

        ll.addView(img);
        ll.setTag(views.size());

        if (b) {
            ll.setOnClickListener(tabClick);
        } else {
            ll.setOnClickListener(addClick);
        }
        tab_ll.addView(ll);
        tabList.add(ll);

    }

    OnClickListener addClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            EmoticonListActivity_.intent(EmoteView.this).startForResult(EMOTICONRESPONSE);
        }
    };

    int lastPosition = 0;

    @Override
    public void onPause() {
        super.onPause();
        lastPosition = viewpager.getCurrentItem();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == EMOTICONRESPONSE) {
            tab_ll.removeAllViews();
            tabList = new ArrayList<>();
            FragmentTransaction ft = getChildFragmentManager().beginTransaction();
            for (int i = 0; i < views.size(); i++) {
                ft.remove(views.get(i));
            }
            ft.commitAllowingStateLoss();
            getChildFragmentManager().executePendingTransactions();
            views = new ArrayList<>();
            viewpager.removeAllViews();
            initView();
            addMoreEmote();

            viewpager.post(new Runnable() {
                @Override
                public void run() {
                    viewpager.setCurrentItem(lastPosition,false);
                }
            });
        }
    }


    OnClickListener tabClick = new OnClickListener() {

        @Override
        public void onClick(View v) {
            int index = (Integer) v.getTag();
            viewpager.setCurrentItem(index);
        }
    };

    class mPagerAdapter extends FragmentPagerAdapter {

        public mPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int arg0) {
            return views.get(arg0);
        }

        @Override
        public int getCount() {
            return views.size();
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position,
                                   Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {

            super.destroyItem(container, position, object);
        }
    }

    public class MyOnPageChangeListener implements OnPageChangeListener {

        public void onPageSelected(int position) {
            EmoteView.this.position = position;
            View firstTemp = null;
            for (int i = 0; i < tabList.size(); i++) {
                int tag = (Integer) tabList.get(i).getTag();
                if (position >= tag) {
                    firstTemp = tabList.get(i);
                }
            }
            if (firstTemp != null) {
                for (View view : tabList) {
                    int tag = (Integer) view.getTag();
                    if (tag == (Integer) firstTemp.getTag()) {
                        view.setSelected(true);
                    } else {
                        view.setSelected(false);
                    }
                }
            }

            int firstIndex = tabList.indexOf(firstTemp);
            View secondTemp = null;
            int count;
            if (firstIndex + 1 < tabList.size()) {
                secondTemp = tabList.get(firstIndex + 1);
                count = (Integer) secondTemp.getTag()
                        - (Integer) firstTemp.getTag();
            } else {
                count = views.size() - (Integer) firstTemp.getTag();
            }
            gallerynavigator.setSize(count);
            gallerynavigator.setPosition(position
                    - (Integer) firstTemp.getTag());
            gallerynavigator.invalidate();
        }

        public void onPageScrolled(int position, float bai, int x) {
        }

        public void onPageScrollStateChanged(int arg0) {
        }

    }

    @Click
    public void emoticon_delete_btn(View view) {
        if (editText != null) {
            int start = editText.getSelectionStart();
            String content = editText.getText().toString();
            if (TextUtils.isEmpty(content)) {
                return;
            }
            String startContent = content.substring(0, start);
            String endContent = content.substring(start, content.length());
            String lastContent = content.substring(start - 1, start);
            int last = startContent.lastIndexOf("[");
            int lastChar = startContent.substring(0, startContent.length() - 1)
                    .lastIndexOf("]");

            if ("]".equals(lastContent) && last > lastChar) {
                if (last != -1) {
                    editText.setText(startContent.substring(0, last)
                            + endContent);
                    // 定位光标位置
                    CharSequence info = editText.getText();
                    if (info instanceof Spannable) {
                        Spannable spanText = (Spannable) info;
                        Selection.setSelection(spanText, last);
                    }
                    return;
                }
            }
            editText.setText(startContent.substring(0, start - 1) + endContent);
            // 定位光标位置
            CharSequence info = editText.getText();
            if (info instanceof Spannable) {
                Spannable spanText = (Spannable) info;
                Selection.setSelection(spanText, start - 1);
            }
        }
    }

    @Override
    public void onEmoticonLongPressCancle() {
        viewpager.setEnableTouchScroll(true);
        ((BaseActivity) getActivity()).setSwipeBackEnable(true);
        if (loadedBean != null) {
            GifDrawable gd = (GifDrawable) gif.getDrawable();
            if (gd != null)
                gd.stop();
            gif.setImageDrawable(null);
        }
        loadedBean = null;
        if (gif != null)
            gif.setVisibility(View.GONE);
        if (pop != null) {
            pop.dismiss();
        }

        if (listener != null) {
            listener.onEmoticonLongPressCancle();
        }
    }

    @Override
    public void onEmoticonClick(EmoticonImageBean bean) {

        if (bean instanceof EmoticonImageBean.EmoticonRes) {

            String text = null;
            text = bean.getNickname();

            int nums = (int) (Util.getLengthString(editText.getText()
                    .toString() + text) / 2);
            if (Util.getLengthString(editText.getText().toString() + text) % 2 > 0) {
                nums++;
            }
            if (nums > editText.max_nickname_length) {
                return;
            }

            if (editText != null && !TextUtils.isEmpty(text)) {
                int start = editText.getSelectionStart();
                CharSequence content = editText.getText().insert(start, text);
                editText.setText(content);
                // 定位光标位置
                CharSequence info = editText.getText();
                if (info instanceof Spannable) {
                    Spannable spanText = (Spannable) info;
                    try {
                        Selection.setSelection(spanText, start + text.length());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        } else {
            if (listener != null) {
                listener.onEmoticonClick(bean);
            }
        }
    }

    EmoticonImageBean loadedBean = null;
    PopupWindow pop;

    @Override
    public void onEmoticonLongPress(EmoticonImageBean bean, int[] xy, int[] wh) {
        viewpager.setEnableTouchScroll(false);
        ((BaseActivity) getActivity()).setSwipeBackEnable(false);
        if (gif == null) {
            gif = new GifImageView(getActivity());
            gif.setLayoutParams(new LayoutParams(ImageUtil.dip2px(
                    getActivity(), 60), ImageUtil.dip2px(getActivity(), 60)));
        }
        if ((loadedBean == null || (bean != null && !loadedBean.equals(bean)))
                && bean != null) {
            if (loadedBean != null) {
                GifDrawable gd = (GifDrawable) gif.getDrawable();
                if (gd != null)
                    gd.stop();
                gif.setImageDrawable(null);
            }

            loadedBean = bean;
            gif.setVisibility(View.VISIBLE);
            if (bean instanceof EmoticonImageBean.EmoticonRes)
                gif.setImageResource(((EmoticonImageBean.EmoticonRes) bean).getRes());
            else {
                DrawableCache.getInstance().displayDrawable(gif,
                        bean.getGiffile());
            }

            if (pop == null) {
                pop = new PopupWindow(gif, ImageUtil.dip2px(getActivity(), 60),
                        ImageUtil.dip2px(getActivity(), 60));
                pop.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.emoticon_popup_bg));
            } else {
                pop.dismiss();
            }
            if (bean instanceof EmoticonImageBean.EmoticonRes) {
                pop.setWidth(ImageUtil.dip2px(getActivity(), 60));
                pop.setHeight(ImageUtil.dip2px(getActivity(), 60));
            } else {
                pop.setWidth(ImageUtil.dip2px(getActivity(), 120));
                pop.setHeight(ImageUtil.dip2px(getActivity(), 120));
            }
            int x = (int) ((float) (pop.getWidth() - wh[0]) / 2);
            x = xy[0] - x;
            if (x < 0) {
                x = 0;
            } else if (x + pop.getWidth() > getResources().getDisplayMetrics().widthPixels) {
                x = getResources().getDisplayMetrics().widthPixels
                        - pop.getWidth();
            }
            pop.showAtLocation(
                    viewpager,
                    Gravity.NO_GRAVITY,
                    x,
                    xy[1] - pop.getHeight()
                            - ImageUtil.dip2px(getActivity(), 20));
        }
        if (listener != null) {
            listener.onEmoticonLongPress(bean, xy, wh);
        }
    }
}

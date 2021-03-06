package com.quanliren.quan_one.fragment.near;


import android.content.Intent;
import android.view.View;
import android.widget.ImageView;
import android.widget.AdapterView;
import android.widget.GridView;

import com.quanliren.quan_one.activity.R;
import com.quanliren.quan_one.activity.rank.RankListActivity_;
import com.quanliren.quan_one.activity.through.ThroughActivity_;
import com.quanliren.quan_one.adapter.NearPeopleAdapter;
import com.quanliren.quan_one.adapter.NearPeopleTwoAdapter;
import com.quanliren.quan_one.adapter.base.BaseAdapter;
import com.quanliren.quan_one.api.NearPeopleApi;
import com.quanliren.quan_one.api.base.BaseApi;
import com.quanliren.quan_one.bean.CustomFilterBean;
import com.quanliren.quan_one.bean.User;
import com.quanliren.quan_one.dao.CustomFilterBeanDao;
import com.quanliren.quan_one.dao.DBHelper;
import com.quanliren.quan_one.fragment.base.BaseViewPagerChildListFragment;
import com.quanliren.quan_one.util.ImageUtil;
import com.quanliren.quan_one.util.URL;
import com.quanliren.quan_one.util.Util;

import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;
import org.json.JSONObject;

import java.util.List;

@EFragment
public class NearPeopleFragment extends BaseViewPagerChildListFragment<User> {

    public static final int NEAR_PEOPLE_FILTER = 11;

    CustomFilterBeanDao filterDao;
    @ViewById
    ImageView date_list;
    @ViewById
    ImageView data_grid;

    GridView rankGridView;//人气榜
    NearPeopleHeaderRankingAdapter rankAdapter;

    View popularView;


    @Override
    public Class<?> getClazz() {
        return User.class;
    }

    @Override
    public boolean needBack() {
        return false;
    }

    @Override
    public BaseAdapter<User> getAdapter() {
        return new NearPeopleTwoAdapter(getActivity());
    }

    @Override
    public BaseApi getApi() {
        return new NearPeopleApi(getActivity());
    }

    @Override
    public int getConvertViewRes() {
        return R.layout.near_people;
    }

    @Override
    public boolean needLocation() {
        return true;
    }

    @Override
    public boolean needCache() {
        return true;
    }

    @Override
    public void init() {
        super.init();
        filterDao = DBHelper.customFilterBeanDao;

        if (maction_bar != null)
            maction_bar.setVisibility(View.GONE);

    }

    @Override
    public void initListView() {
        super.initListView();

        listview.setClipToPadding(false);
        listview.setPadding(0, 0, 0, ImageUtil.dip2px(getActivity(), 8));

        /*adImg = new ShakeImageView(getActivity());
        adImg.addToListView(listview);*/

        //人气排行
        popularView = View.inflate(getActivity(), R.layout.head_hot_ranking, null);
        popularView.setVisibility(View.GONE);
        rankGridView = (GridView) popularView.findViewById(R.id.gridview);
        rankGridView.setAdapter(rankAdapter = new NearPeopleHeaderRankingAdapter(getActivity()));

        rankGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                RankListActivity_.intent(getActivity()).start();
            }
        });

        listview.addHeaderView(popularView);
    }

    @Override
    public void initParams() {
        super.initParams();
        api.initParam();
    }

    @Click(R.id.ground_through)
    public void ground_through(View view) {
        if (ac.getUserInfo().getIsvip() < 2) {
            Util.goVip(getActivity(), 2);
            return;
        } else if (ac.getUserInfo().getIsvip() == 2) {
            ThroughActivity_.intent(getActivity()).start();
        }
    }
    NearPeopleTwoAdapter twoAdapter;
    NearPeopleAdapter nearAdapter;
    @Click(R.id.date_list)
    public void date_list(View view) {
        //  切换到列表数据
        if (nearAdapter == null) {
            nearAdapter = new NearPeopleAdapter(getActivity());
        }
        nearAdapter.setList(adapter.getList());
        adapter=nearAdapter;
        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        date_list.setVisibility(View.GONE);
        data_grid.setVisibility(View.VISIBLE);
    }

    @Click(R.id.data_grid)
    public void data_grid(View view) {
        //  切换到表格数据
        if(twoAdapter==null){
            twoAdapter=new NearPeopleTwoAdapter(getActivity());
        }
        twoAdapter.setList(adapter.getList());
        adapter=twoAdapter;
        listview.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        date_list.setVisibility(View.VISIBLE);
        data_grid.setVisibility(View.GONE);
    }

    @OnActivityResult(value = NEAR_PEOPLE_FILTER)
    public void onFilterResult(Intent data) {
        if (data != null && init.get()) {
            CustomFilterBean cfb_sex;
            CustomFilterBean cfb_time;
            int sexIndex = data.getIntExtra("sexIndex", -1);
            int timeIndex = data.getIntExtra("timeIndex", -1);
            if (sexIndex != -1) {
                cfb_sex = new CustomFilterBean("性别", "", "sex", sexIndex);
                filterDao.deleteById("sex");
                filterDao.create(cfb_sex);
            } else {
                filterDao.deleteById("sex");
            }
            if (timeIndex != -1) {
                cfb_time = new CustomFilterBean("时间", "", "actime", timeIndex);
                filterDao.deleteById("actime");
                filterDao.create(cfb_time);
            } else {
                filterDao.deleteById("actime");
            }
            swipeRefresh();
        }
    }

    @Override
    public boolean needTitle() {
        return false;
    }

    @Override
    public void listview(int position) {
        if (adapter instanceof NearPeopleAdapter) {
            User user = adapter.getList().get(position - 1);
            Util.startUserInfoActivity(getActivity(), user);
        }
    }

    @Override
    public void onSuccessRefreshUI(JSONObject jo, List<User> list, boolean cache) {
        super.onSuccessRefreshUI(jo, list, cache);

        try {
            if (jo.getJSONObject(URL.RESPONSE).has("popList")) {
                if (popularView != null) {
                    popularView.setVisibility(View.VISIBLE);
                }
                List<User> user = Util.jsonToList(jo.getJSONObject(URL.RESPONSE).getString("popList"), User.class);
                rankAdapter.setList(user);
                rankAdapter.notifyDataSetChanged();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



}

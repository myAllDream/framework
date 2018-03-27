package com.framework.app.presenter;



import com.framework.app.bean.PlatformBean;
import com.framework.app.contract.AddBillFragmentContract;
import com.framework.app.net.ApiService;
import com.framework.app.net.NetClient;
import com.framework.app.net.SimpleSubscriber;
import com.framework.app.utils.ToastUtils;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by admin on 2018/3/1.
 */

public class AddBillPresenter implements AddBillFragmentContract.Presenter{

    private AddBillFragmentContract.View mView;
    private ApiService mApiService;
    public AddBillPresenter(AddBillFragmentContract.View mView){
        this.mView=mView;
        mApiService= NetClient.getInstance().net().create(ApiService.class);
    }
    @Override
    public void getPlatforms(String cityCode) {
        mView.showLoading("");
        mApiService.getPlatforms("1","1")
                .unsubscribeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new SimpleSubscriber<PlatformBean>(mView) {
                    @Override
                    public void success(PlatformBean platformBean) {
                        mView.getPlatformsSuccess(platformBean);
                    }

                    @Override
                    public void error(String errMessage) {
                        ToastUtils.show(errMessage);
                    }
                });
    }
}
package com.love_cookies.beauty.view.activity;

import android.app.WallpaperManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.RelativeLayout;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.ActionSheetDialog;
import com.like.LikeButton;
import com.like.OnLikeListener;
import com.love_cookies.beauty.R;
import com.love_cookies.beauty.app.BeautyApplication;
import com.love_cookies.beauty.model.bean.BeautyBean;
import com.love_cookies.cookie_library.activity.BaseActivity;
import com.love_cookies.cookie_library.utils.ProgressDialogUtils;
import com.love_cookies.cookie_library.utils.ToastUtils;
import com.love_cookies.cookie_library.widget.PinchImageView;
import com.love_cookies.beauty.presenter.DetailPresenter;
import com.love_cookies.beauty.view.interfaces.IDetail;

import org.xutils.view.annotation.ContentView;
import org.xutils.view.annotation.ViewInject;
import org.xutils.x;

import java.io.File;

/**
 * Created by xiekun on 2016/7/27 0027.
 *
 * 图片详情页
 */
@ContentView(R.layout.activity_detail)
public class DetailActivity extends BaseActivity implements IDetail, View.OnLongClickListener {

    private BeautyBean.ResultsEntity mBeauty;
    private ActionSheetDialog actionSheetDialog = null;
    private String imagePath;

    @ViewInject(R.id.root_view)
    private RelativeLayout rootView;
    @ViewInject(R.id.image_iv)
    private PinchImageView imageView;
    @ViewInject(R.id.like_button)
    private LikeButton likeButton;

    private DetailPresenter detailPresenter = new DetailPresenter(this);

    /**
     * 初始化控件
     * @param savedInstanceState
     */
    @Override
    public void initWidget(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);// 去掉信息栏
        mBeauty = getIntent().getExtras().getParcelable("beauty");
        rootView.setBackgroundColor(Color.parseColor(mBeauty.getBackgroundColor()));
        x.image().bind(imageView, mBeauty.getUrl(), BeautyApplication.NormalImageOptions);
        imageView.setOnLongClickListener(this);

        imagePath = BeautyApplication.FILE_PATH + mBeauty.get_id() + ".jpg";

        String[] stringItems = getResources().getStringArray(R.array.image_item);
        actionSheetDialog = new ActionSheetDialog(this, stringItems, null);
        actionSheetDialog.setOnOperItemClickL(new OnOperItemClickL() {
            @Override
            public void onOperItemClick(AdapterView<?> parent, final View view, int position, long id) {
                ProgressDialogUtils.showProgress(DetailActivity.this, true);
                switch (position) {
                    case 0:
                        downloadFile(mBeauty.getUrl());
                        actionSheetDialog.dismiss();
                        break;
                    case 1:
                        getAndSetWallpaper(mBeauty.getUrl());
                        actionSheetDialog.dismiss();
                        break;
                    default:
                        break;
                }
            }
        });

        likeButton.setLiked(detailPresenter.isLove(mBeauty));

        likeButton.setOnLikeListener(new OnLikeListener() {
            @Override
            public void liked(LikeButton likeButton) {
                doLove(mBeauty);
            }

            @Override
            public void unLiked(LikeButton likeButton) {
                doUnLove(mBeauty);
            }
        });
    }

    /**
     * 控件点击事件
     * @param view
     */
    @Override
    public void widgetClick(View view) {

    }

    /**
     * 控件长按事件
     * @param v
     * @return
     */
    @Override
    public boolean onLongClick(View v) {
        actionSheetDialog.isTitleShow(false).show();
        return true;
    }

    /**
     * Activity销毁
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (actionSheetDialog != null && actionSheetDialog.isShowing()) {
            actionSheetDialog.dismiss();
        }
    }

    /**
     * 下载文件
     * @param url
     */
    public void downloadFile(String url) {
        File file = new File(imagePath);
        if (!file.exists()) {
            detailPresenter.downloadFile(url, imagePath);
        } else {
            ProgressDialogUtils.hideProgress();
            ToastUtils.show(DetailActivity.this, R.string.image_exists);
        }
    }

    /**
     * 获取设置壁纸的资源
     */
    public void getAndSetWallpaper(String url) {
        File file = new File(imagePath);
        if (!file.exists()) {
            detailPresenter.getWallpaper(url, imagePath);
        } else {
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
            setPhoneWallpaper(bitmap);
        }
    }

    /**
     * 设置为壁纸
     * @param bitmap
     */
    public void setPhoneWallpaper(Bitmap bitmap) {
        try {
            WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
            wallpaperManager.setBitmap(bitmap);
            ProgressDialogUtils.hideProgress();
            ToastUtils.show(DetailActivity.this, R.string.wallpaper_success);
        } catch (Exception e) {
            ProgressDialogUtils.hideProgress();
            ToastUtils.show(DetailActivity.this, R.string.wallpaper_faile);
        }
    }

    /**
     * 下载文件成功
     */
    @Override
    public void downloadFileSuccess() {
        ProgressDialogUtils.hideProgress();
        ToastUtils.show(DetailActivity.this, R.string.image_save_success);
    }

    /**
     * 下载文件失败
     */
    @Override
    public void downloadFileFailed() {
        ProgressDialogUtils.hideProgress();
        ToastUtils.show(DetailActivity.this, R.string.image_save_faile);
    }

    /**
     * 获取壁纸成功
     */
    @Override
    public void getWallpaperSuccess() {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        setPhoneWallpaper(bitmap);
    }

    /**
     * 获取壁纸失败
     */
    @Override
    public void getWallpaperFailed() {
        ProgressDialogUtils.hideProgress();
        ToastUtils.show(DetailActivity.this, R.string.wallpaper_faile);
    }

    /**
     * 喜欢
     * @param beauty
     */
    @Override
    public void doLove(BeautyBean.ResultsEntity beauty) {
        detailPresenter.doLove(beauty);
    }

    /**
     * 喜欢成功
     */
    @Override
    public void doLoveSuccess() {
        likeButton.setLiked(true);
        ToastUtils.show(DetailActivity.this, R.string.love_success);
    }

    /**
     * 喜欢失败
     */
    @Override
    public void doLoveFailed() {
        likeButton.setLiked(false);
        ToastUtils.show(DetailActivity.this, R.string.love_faile);
    }

    /**
     * 不喜欢
     * @param beauty
     */
    @Override
    public void doUnLove(BeautyBean.ResultsEntity beauty) {
        detailPresenter.doUnLove(beauty);
    }

    /**
     * 不喜欢成功
     */
    @Override
    public void doUnLoveSuccess() {
        likeButton.setLiked(false);
        ToastUtils.show(DetailActivity.this, R.string.unlove_success);
    }

    /**
     * 不喜欢失败
     */
    @Override
    public void doUnLoveFailed() {
        likeButton.setLiked(true);
        ToastUtils.show(DetailActivity.this, R.string.unlove_faile);
    }

}

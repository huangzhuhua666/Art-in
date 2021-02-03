package com.tw.artin.ui.activity

import android.os.Build
import android.view.View
import com.blankj.utilcode.constant.PermissionConstants
import com.blankj.utilcode.util.PermissionUtils
import com.blankj.utilcode.util.ToastUtils
import com.bumptech.glide.Glide
import com.hjq.http.EasyHttp
import com.hjq.http.listener.HttpCallback
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.config.PictureMimeType
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import com.lxj.xpopup.XPopup
import com.lxj.xpopup.interfaces.OnSelectListener
import com.tw.artin.R
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.http.api.FeekBackApi
import com.tw.artin.http.api.UpLoadApi
import com.tw.artin.http.model.HttpData
import com.tw.artin.http.model.UpLoadData
import com.tw.artin.util.GlideEngine
import com.tw.artin.util.ImageLoader
import kotlinx.android.synthetic.main.feedback_activity.*
import java.io.File


class FeedBackActivity : TwActivity() {

    val imageDialog by lazy {

        XPopup.Builder(this)
            .hasShadowBg(false)
            .asBottomList(resources.getString(R.string.pick_image01)
                ,arrayOf(
                    resources.getString(R.string.pick_image02),
                    resources.getString(R.string.pick_image03)),
                object : OnSelectListener {
                    override fun onSelect(position: Int, text: String?) {
                        imageDoIt(position)
                    }
                })
    }

    var mPos = 0
    var mAttach1 = ""
    var mAttach2 = ""
    var mAttach3 = ""

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.feedback_activity
    }

    override fun initView() {
        setOnClickListener(R.id.iv_i01,R.id.iv_i02,R.id.iv_i03,
            R.id.iv_del01,R.id.iv_del02,R.id.iv_del03,R.id.tv_submit)
    }

    override fun initData() {
    }

    override fun onClick(v: View?) {
        super.onClick(v)

        when(v?.id){

            R.id.iv_i01 -> {
                mPos = 0
                if (mAttach1 == ""){
                    imageDialog.show()
                }else{

                    XPopup.Builder(this)
                        .asImageViewer(iv_i01, mAttach1, ImageLoader())
                        .show()

                }
            }

            R.id.iv_i02 -> {
                mPos = 1

                if (mAttach2 == ""){
                    imageDialog.show()
                }else{
                    XPopup.Builder(this)
                        .asImageViewer(iv_i02, mAttach2, ImageLoader())
                        .show()
                }
            }

            R.id.iv_i03 -> {
                mPos = 2

                if (mAttach3 == ""){
                    imageDialog.show()
                }else{
                    XPopup.Builder(this)
                        .asImageViewer(iv_i03, mAttach3, ImageLoader())
                        .show()
                }
            }

            R.id.iv_del01 ->{
                mAttach1 = ""
                Glide.with(iv_i01)
                    .load(R.mipmap.icon_add_pic)
                    .into(iv_i01)
            }

            R.id.iv_del02 ->{
                mAttach2 = ""
                Glide.with(iv_i02)
                    .load(R.mipmap.icon_add_pic)
                    .into(iv_i02)
            }

            R.id.iv_del03 ->{
                mAttach3 = ""
                Glide.with(iv_i03)
                    .load(R.mipmap.icon_add_pic)
                    .into(iv_i03)
            }

            R.id.tv_submit -> SaveData()

        }
    }

    fun SaveData(){

        val mTitle = et_theme.text.toString()
        if (mTitle.isEmpty()){
            ToastUtils.showShort(R.string.feedback_toa_t02)
            return
        }

        val mContent = et_content.text.toString()
        if (mContent.isEmpty()){
            ToastUtils.showShort(R.string.feedback_t06)
            return
        }

        EasyHttp.post(this)
            .api(
                FeekBackApi().apply {
                    title = mTitle
                    content = mContent

                    if (mAttach1.isNotEmpty()){
                        attach1 = mAttach1
                    }

                    if (mAttach2.isNotEmpty()){
                        attach2 = mAttach2
                    }

                    if (mAttach3.isNotEmpty()){
                        attach3 = mAttach3
                    }
                }
            )
            .request(object : HttpCallback<HttpData<String>>(this){

                override fun onSucceed(result: HttpData<String>?) {
                    super.onSucceed(result)

                    ToastUtils.showShort(R.string.feedback_t07)
                    //finish()
                }
            })

    }

    fun imageDoIt(pos : Int){

        //低于android 11
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {

            XXPermissions.with(this)
                .permission(Permission.Group.STORAGE)
                .request(object : OnPermissionCallback{
                    override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                        Chuli(pos)
                    }

                    override fun onDenied(permissions: MutableList<String>?, never: Boolean) {

                        if (never) {
                            ToastUtils.showShort(R.string.permission_error2)
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(this@FeedBackActivity, permissions)
                        } else {
                            ToastUtils.showShort(R.string.permission_error)
                        }
                    }
                })

        }else{

            //android 11

            XXPermissions.with(this)
                .permission(Permission.MANAGE_EXTERNAL_STORAGE)
                .request(object : OnPermissionCallback{
                    override fun onGranted(permissions: MutableList<String>?, all: Boolean) {
                        Chuli(pos)
                    }

                    override fun onDenied(permissions: MutableList<String>?, never: Boolean) {

                        if (never) {
                            ToastUtils.showShort(R.string.permission_error2)
                            // 如果是被永久拒绝就跳转到应用权限系统设置页面
                            XXPermissions.startPermissionActivity(this@FeedBackActivity, permissions)
                        } else {
                            ToastUtils.showShort(R.string.permission_error)
                        }
                    }
                })

        }



    }

    private fun Chuli(pos: Int) {

        if (pos == 0) {

            PictureSelector.create(this@FeedBackActivity)
                .openCamera(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.instance)
                .isCompress(true)
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: MutableList<LocalMedia>?) {

                        result?.run {

                            val path = if (get(0).compressPath == null) {
                                get(0).androidQToPath
                            } else {
                                get(0).compressPath
                            }

                            doImage(path)
                        }

                    }

                    override fun onCancel() {
                    }
                })

        } else {

            PictureSelector.create(this@FeedBackActivity)
                .openGallery(PictureMimeType.ofImage())
                .imageEngine(GlideEngine.instance)
                .isCamera(false)
                .isCompress(true)
                .selectionMode(PictureConfig.SINGLE)
                .forResult(object : OnResultCallbackListener<LocalMedia> {
                    override fun onResult(result: MutableList<LocalMedia>?) {

                        result?.run {

                            val path = if (get(0).compressPath == null) {
                                get(0).androidQToPath
                            } else {
                                get(0).compressPath
                            }

                            doImage(path)

                        }
                    }

                    override fun onCancel() {
                    }
                })
        }
    }

    fun doImage(path : String){

        val file = File(path)
        if (!file.exists()){
            ToastUtils.showShort(R.string.pick_image04)
            return
        }

        EasyHttp.post(this)
            .api(
                UpLoadApi(file)
            )
            .request(object : HttpCallback<UpLoadData>(this){

                override fun onSucceed(result: UpLoadData?) {
                    super.onSucceed(result)

                    result?.let {

                        when(mPos){

                            0 -> {
                                mAttach1 = it.url
                                Glide.with(iv_i01)
                                    .load(mAttach1)
                                    .into(iv_i01)
                            }

                            1 -> {
                                mAttach2 = it.url
                                Glide.with(iv_i02)
                                    .load(mAttach2)
                                    .into(iv_i02)
                            }

                            2 -> {
                                mAttach3 = it.url
                                Glide.with(iv_i03)
                                    .load(mAttach3)
                                    .into(iv_i03)
                            }

                            else -> {}
                        }

                    }

                }
            })

    }

}
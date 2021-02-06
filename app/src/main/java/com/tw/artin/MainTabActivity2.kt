package com.tw.artin

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.AppUtils
import com.blankj.utilcode.util.BusUtils
import com.lxj.xpopup.XPopup
import com.tw.artin.base.BaseInfVp
import com.tw.artin.base.common.TwActivity
import com.tw.artin.ui.activity.CoverActivity
import com.tw.artin.ui.adapter.ViewPager2Adapter
import com.tw.artin.ui.fragment.DeviceFragment
import com.tw.artin.ui.fragment.NScenesFragment
import com.tw.artin.ui3.fragment.ControllerFragment
import com.tw.artin.ui2.fragment.MeFragment
import com.tw.artin.view.CustomTabEntity
import com.tw.artin.view.OnTabSelectListener
import com.tw.artin.vp.TotalControl
import com.tw.artin.vp2.LightControl
import kotlinx.android.synthetic.main.main_tab_activity.*
import java.util.*

class MainTabActivity2 : TwActivity(){

    val LED_INFO = 1000

    var delNoteScenes_address = 0

    var main_pos = 0

    //蓝牙是否开启
    var isBluetooth = false

    val bleListenerReceiver by lazy {
        BluetoothMonitorReceiver(this)
    }

    val mBluetoothAdapter by lazy {
        BluetoothAdapter.getDefaultAdapter()
    }

    lateinit var shearControl : LightControl

    lateinit var mainControl : TotalControl

    private val mTabEntities = arrayListOf<CustomTabEntity>()

    private val mClass = ArrayList<Class<*>>()

    override fun <T : BaseInfVp.BasePresenter> getPresenter(): T? {
        return null
    }

    override fun getLayoutId(): Int {
        return R.layout.main_tab_activity
    }

    override fun initView() {

        BusUtils.register(this)

        shearControl = LightControl(this)
        shearControl.init()

        mainControl = TotalControl(this)
        mainControl.init()

        //监听蓝牙开启关闭
        registerReceiver(
            bleListenerReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            }
        )

        setBottomTab()

    }

    override fun initData() {

        if (mBluetoothAdapter != null){
            isBluetooth = mBluetoothAdapter.isEnabled
        }

    }

    private fun setBottomTab() {
        mTabEntities.add(
            TabEntity(
                resources.getString(R.string.tab_t01),
                R.mipmap.home_icon_shebei_click, R.mipmap.home_icon_shebei
            )
        )

        mTabEntities.add(
            TabEntity(
                resources.getString(R.string.tab_t02),
                R.mipmap.home_icon_yaokong_click, R.mipmap.home_icon_yaokong
            )
        )

        mTabEntities.add(
            TabEntity(
                resources.getString(R.string.tab_t03),
                R.mipmap.home_icon_changjign_click, R.mipmap.home_icon_changjign
            )
        )

        mTabEntities.add(
            TabEntity(
                resources.getString(R.string.tab_t04),
                R.mipmap.home_icon_me_click, R.mipmap.home_icon_me
            )
        )

        mClass.add(DeviceFragment::class.java)
        mClass.add(ControllerFragment::class.java)
        mClass.add(NScenesFragment::class.java)
        mClass.add(MeFragment::class.java)

        main_vp2.apply {

            offscreenPageLimit = 1

            //禁止滑动
            isUserInputEnabled = false

            adapter = ViewPager2Adapter(this@MainTabActivity2, mClass)

            //禁止滑动，取消监听
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {

                override fun onPageSelected(position: Int) {

                    main_pos = position
                    tab_layout.currentTab = position
                    super.onPageSelected(position)
                }
            })
        }

        tab_layout.apply {

            setTabData(mTabEntities)

            setOnTabSelectListener(object : OnTabSelectListener {

                override fun onTabSelect(position: Int) {

                    main_pos = position
                    main_vp2.currentItem = position
                }

                override fun onTabReselect(position: Int) {
                }

            })
        }

        main_vp2.currentItem = main_pos

    }

    class BluetoothMonitorReceiver(val mActivity : MainTabActivity2) : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent?) {

            intent?.let {

                val mAction = it.action ?: ""

                if (mAction == BluetoothAdapter.ACTION_STATE_CHANGED){

                    val blueState = it.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0)

                    when(blueState){

                        BluetoothAdapter.STATE_ON ->{
                            mActivity.isBluetooth = true
                            mActivity.sendBluetooth(true)
                        }

                        BluetoothAdapter.STATE_OFF ->{
                            mActivity.isBluetooth = false
                            mActivity.sendBluetooth(false)
                        }

                    }
                }
            }
        }
    }

    fun sendBluetooth(isBluetooth : Boolean){

        if (main_pos == 0){
            BusUtils.postSticky("search_device",isBluetooth)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != Activity.RESULT_OK){
            return
        }

        if (requestCode == LED_INFO){

            data?.let {

                val isDel = it.getBooleanExtra("isDel",false)

                if (isDel){

                    delNoteScenes_address = it.getIntExtra("unicastAddress",0)

                    shearControl.resetNoteScenes(
                        delNoteScenes_address
                    )

                }else{
                    //取消分组订阅
                    shearControl.delGroupNodeInfo(
                        it.getIntExtra("unicastAddress",0),
                        it.getIntExtra("group_address",0)
                    )

                }
            }
        }
    }

    @BusUtils.Bus(tag = "logout")
    fun onEvent(){

        startActivity(
            Intent(this, CoverActivity::class.java).apply {
                putExtra("isLogout",true)
            }
        )
        finish()
    }

    @BusUtils.Bus(tag = "getElementSize")
    fun onEvent01(datas : String){

        val data = datas.split(",")

        val size = shearControl.getLedInfoElementSize(data[0].toInt())
        val typeStr = if (size == 6){
            "HSI/CCT/EFFECT"
        }else{
            "CCT/EFFECT"
        }

        BusUtils.postSticky("typeElement",typeStr)

        postDelayed({
            shearControl.lightInfo(data[0].toInt(),data[1].toInt(),size)
        },200)
    }

    @BusUtils.Bus(tag = "edit_note_name")
    fun onEvent03(){
        shearControl.operating_mesh = 0
        shearControl.upDataScenes()
    }

    @BusUtils.Bus(tag = "dfuModel")
    fun onEvent04(address : Int){
        shearControl.setDfuModel(address)
    }

    override fun onBackPressed() {

        XPopup.Builder(this)
            .hasShadowBg(false)
            .asConfirm(resources.getString(R.string.exit_app_t01),
                resources.getString(R.string.exit_app_t02)
            )
            {
                AppUtils.exitApp()
            }
            .bindLayout(R.layout.dialog_message)
            .show()

    }

    override fun onDestroy() {
        mainControl.onDestroy()
        unregisterReceiver(bleListenerReceiver)
        BusUtils.unregister(this)
        super.onDestroy()
    }

    class TabEntity(
        override var tabTitle: String?,
        override val tabSelectedIcon: Int,
        override val tabUnselectedIcon: Int
    ) : CustomTabEntity


}
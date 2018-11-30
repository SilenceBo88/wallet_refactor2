package com.hunter.wallet.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.hunter.wallet.R;
import com.hunter.wallet.entity.BtcTx;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.EthTransfer;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.service.TokenManageService;
import com.hunter.wallet.service.TxManageService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.utils.AppUtils;
import com.hunter.wallet.utils.BtcUtil;
import com.hunter.wallet.utils.RemindUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

public class BtcTxInfoActivity extends FragmentActivity implements View.OnClickListener, WalletManageService.UnlockWalletCallback {
    private static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private TxManageService txManageService = TxManageService.getInstance();
    private TokenManageService tokenManageService = TokenManageService.getInstance();
    private WalletManageService walletManageService = WalletManageService.getInstance();

    //    private LineChart mChart;
    private TextView balanceView;
    private ListView txListView;

    private WalletInfo wallet;
    private Timer updateTimer;
    private TextView coinvalue;

    //比特币信息
    //交易列表
    private List<BtcTx> btcTxList = null;
    //余额
    private String btcBalance = "0";
    //汇率：美元
    private String rate = "1";
    //手续费
    private String fee = "0.00001" ;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallet = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());

        setContentView(R.layout.coin_info_layout);
        TextView infoWalletName = findViewById(R.id.infoWalletName);
//        mChart = findViewById(R.id.lineChart);
        balanceView = findViewById(R.id.balanceView);
        txListView = findViewById(R.id.transcationList);
        coinvalue = findViewById(R.id.coinvalue);

        //设置标题为BTC
        ((TextView)findViewById(R.id.infoWalletName)).setText("BTC");
        if (btcTxList == null){
            btcTxList = new ArrayList<BtcTx>();
        }

        findViewById(R.id.coinInfoPreBut).setOnClickListener(this);
        findViewById(R.id.sendTransaction).setOnClickListener(this);
        findViewById(R.id.incomeBut).setOnClickListener(this);

        drawChart();
        drawTransferList();

        txListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                BtcTx itemAtPosition = (BtcTx) parent.getItemAtPosition(position);
                Intent intent = new Intent(BtcTxInfoActivity.this, BtcTxDetailActivity.class);
                intent.putExtra(BtcTx.class.getName(), itemAtPosition);
                startActivity(intent);
            }
        });
    }

    // 获取Btc信息
    private void getBtcInfo() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        //比特币地址
                        String btcAddress = BtcUtil.getAddress(wallet.getPubkey());
                        try {
                            btcTxList = BtcUtil.getTxByAddr(btcAddress);
                            btcBalance = BtcUtil.getBalance(btcAddress);
                            rate = BtcUtil.getRate();
                            fee = BtcUtil.getEstimateFee();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (btcBalance != null){
                            balanceView.setText(btcBalance);
                            if (rate!=null){
                                double val = Double.valueOf(btcBalance) * Double.valueOf(rate);
                                double value = new BigDecimal(val).setScale(4,BigDecimal.ROUND_HALF_UP).doubleValue();
                                coinvalue.setText("≈ $ " + value);
                            }
                        }
                        if (btcTxList != null){
                            Log.d("btcTxList: ", btcTxList.toString());
                            drawTransferList();
                            ((ArrayAdapter) txListView.getAdapter()).notifyDataSetChanged();
                        }
                    }
                });
            }
        }, 0, 5000);
    }

    private void stopGetBtcInfoThread() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    /*private void startUpdateThread() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                balance = txManageService.getWalletBalance(BtcTxInfoActivity.this, wallet, ethToken);
                ethTransfers.clear();
                ethTransfers.addAll(txManageService.getEthTransfer(BtcTxInfoActivity.this, wallet, ethToken));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        balanceView.setText(balance.toString());
                        coinvalue.setText("≈ $ " + tokenManageService.getTokenPriceUsd(BtcTxInfoActivity.this, ethToken).multiply(balance).toString());
                        ((ArrayAdapter) txListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        }, 0, 10000);
    }*/

    /*private void stopUpdateThread() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }*/

    @Override
    protected void onResume() {
        super.onResume();
        try {
            wallet = walletManageService.getWallet(wallet.getId());
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
        /*startUpdateThread();*/
        getBtcInfo();
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*stopUpdateThread();*/
        stopGetBtcInfoThread();
    }

    private void drawChart() {
        // 绘制图标
    }

    private void drawTransferList() {
        txListView.setAdapter(new ArrayAdapter<BtcTx>(this, R.layout.tx_list_view_item, btcTxList) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.tx_list_view_item, null);
                }
                BtcTx item = getItem(position);
                ImageView icon = convertView.findViewById(R.id.transcationIcon);
                TextView statusText = convertView.findViewById(R.id.statusText);
                TextView ethMsg = convertView.findViewById(R.id.ethMsg);
                TextView tranNum = convertView.findViewById(R.id.tranNum);
                TextView tranTime = convertView.findViewById(R.id.tranTime);

                if (item.getConfirmations() == 0) {
                    icon.setImageResource(R.drawable.tx_jinxingzhong);
                    statusText.setText("交易中");
                    if (item.getTxType() == 1){
                        ethMsg.setText("-\b" + item.getAmount());
                        ethMsg.setTextColor(Color.RED);
                    }else if (item.getTxType() == 2){
                        ethMsg.setText("+\b" + item.getAmount());
                        ethMsg.setTextColor(Color.BLUE);
                    }
                } else if (item.getConfirmations() > 0 && item.getTxType() == 1) {
                    icon.setImageResource(R.drawable.tx_pay);
                    statusText.setText("转出");
                    ethMsg.setText("-\b" + item.getAmount());
                    ethMsg.setTextColor(Color.RED);
                } else if (item.getConfirmations() > 0 && item.getTxType() == 2){
                    icon.setImageResource(R.drawable.tx_income);
                    statusText.setText("收入");
                    ethMsg.setText("+\b" + item.getAmount());
                    ethMsg.setTextColor(Color.BLUE);
                }else{
                    icon.setImageResource(R.drawable.tx_fail);
                    statusText.setText("失败");
                }

                tranNum.setText(item.getTxHash());
                long time = new Long(item.getTime()) * 1000;
                tranTime.setText(sf.format(time));
                return convertView;
            }
        });
    }

//
//    // 启动更新线程
//    private void startUpdate() {
//        if (updateTimer != null) {
//            updateTimer.cancel();
//        }
//        updateTimer = new Timer();
//        updateTimer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            int startBlock = txCache.getLastBlock() + 1;
//                            int endBlock = EthQueryUtil.getEthRecentBlock();
//                            List<TxBean> txBeans = null;
//                            txBeans = EthQueryUtil.getEthTx(wallet.getAddr(), startBlock, endBlock);
//                            txCache.getTxBeans().addAll(txBeans);
//                            txCache.setLastBlock(endBlock);
//                            for (TxBean txBean : txCache.getTxBeans()) {
//                                // TODO 更新交易状态
//                            }
//                            saveTxCache(txCache);
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    }
//                }).start();
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            balance = EthQueryUtil.getEthBalance(wallet.getAddr());
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                        Log.i("EthTxInfoActivity", "startUpdate balance = " + balance);
//                        updateHanler.sendMessage(new Message());
//                    }
//                }).start();
//            }
//        }, 0, 15000);
//    }

//    private void stopUpdate() {
//        if (updateTimer != null) {
//            updateTimer.cancel();
//            updateTimer = null;
//        }
//    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.coinInfoPreBut:
                BtcTxInfoActivity.this.finish();
                break;
            case R.id.sendTransaction: {
                BigDecimal ethBalance = txManageService.getWalletBalance(BtcTxInfoActivity.this, wallet, tokenManageService.getEthToken(BtcTxInfoActivity.this));
                if (wallet.isHasLock()) {
                    walletManageService.unlockWallet(BtcTxInfoActivity.this, wallet, BtcTxInfoActivity.this);
                } else if (Double.valueOf(btcBalance) <= 0) {
                    RemindUtils.toastShort(this, "BTC余额不足");
                } else {
                    Intent intent = new Intent(BtcTxInfoActivity.this, BtcTxActivity.class);
                    intent.putExtra(WalletInfo.class.getName(), wallet);
                    intent.putExtra("btcBalance", btcBalance);
                    intent.putExtra("fee", fee);
                    startActivity(intent);
                }
            }
            break;
            case R.id.incomeBut: {
                Intent intent = new Intent(BtcTxInfoActivity.this, BtcAddressShowActivity.class);
                intent.putExtra(WalletInfo.class.getName(), wallet);
                startActivity(intent);
            }
            break;
        }
    }

    @Override
    public void onBack() {

    }

    @Override
    public void onSuccess() {
        wallet.setHasLock(false);
        RemindUtils.toastShort(this, "解锁成功");
    }

    @Override
    public void onFail(String msg) {

    }

    @Override
    public void onPinLock() {
        AppUtils.startActivity(this, UnlockActivity.class, Intent.FLAG_ACTIVITY_CLEAR_TASK);
    }
//
//    //更新缓存
//    private void saveTxCache(TxCache txCache) {
//        SharedPreferencesUtils.writeString(this, "tx_cache", Numeric.toHexString(wallet.getAddr()), JsonUtils.objectToJson(txCache));
//    }
//
//    private TxCache getTxCache() {
//        String json = SharedPreferencesUtils.getString(this, "tx_cache", Numeric.toHexString(wallet.getAddr()), null);
//        if (StringUtils.hasText(json)) {
//            return JsonUtils.jsonToPojo(json, TxCache.class);
//        } else {
//            return new TxCache();
//        }
//    }
//
//    private static class TxCache {
//        private int lastBlock = -1;
//        private List<TxBean> txBeans = new ArrayList<>();
//
//        public TxCache() {
//        }
//
//        public int getLastBlock() {
//            return lastBlock;
//        }
//
//        public void setLastBlock(int lastBlock) {
//            this.lastBlock = lastBlock;
//        }
//
//        public List<TxBean> getTxBeans() {
//            return txBeans;
//        }
//
//        public void setTxBeans(List<TxBean> txBeans) {
//            this.txBeans = txBeans;
//        }
//    }

//    //折线图横轴自定义显示数据
//    private String x1String[];
//
//    private void initmChart() {
//        mChart.setDrawGridBackground(false);
//        // 无描述文本
//        mChart.getDescription().setEnabled(false);
//        // 使能拖动和缩放
//        mChart.setDragEnabled(true);
//        mChart.setScaleEnabled(false);
//        // 如果为false，则x，y两个方向可分别缩放
//        mChart.setPinchZoom(true);
//        // 没有数据的时候，显示“暂无数据”
//        mChart.setNoDataText("暂无数据");
//        //去掉LineSet标签
//        Legend legend = mChart.getLegend();
//        legend.setEnabled(false);
//        //设置x轴位置
//        XAxis xAxis = mChart.getXAxis();
//        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
//        xAxis.setLabelCount(3);
//        xAxis.setValueFormatter(new IAxisValueFormatter() {
//            @Override
//            public String getFormattedValue(float value, AxisBase axis) {
//                return x1String[(int) value - 1];
//            }
//        });
//        YAxis axisLeft = mChart.getAxisLeft();
//        axisLeft.setEnabled(false);
//        YAxis axisRight = mChart.getAxisRight();
//        axisRight.setDrawGridLines(false);
//        axisRight.setLabelCount(3);
//        // axisRight.setDrawLabels(false);
//        init();
//    }
//
//    private void init() {
//        //初始化数据
//
//        String xl[] = {"1", "2", "3", "4", "5"}; //横轴数据
//        x1String = DateUtils.getLineDataXData(xl.length);
//        String yl[] = {"0", "0", "0", "0", "10"}; //竖轴数据
//
//        if (balance != null) {
//            yl[yl.length - 1] = balance.toString();
//        }
//
//        LineData data = getData(xl, yl);
//        mChart.setData(data);
//        mChart.animateX(0);//动画时间
//    }
//
//    private LineData getData(String[] xx, String[] yy) {
//        ArrayList<Entry> yVals = new ArrayList<Entry>();
//        for (int i = 0; i < yy.length; i++) {
//            yVals.add(new Entry(Float.parseFloat(xx[i]), Float.parseFloat(yy[i])));
//        }
//        LineDataSet set = new LineDataSet(yVals, "");
//        set.setDrawValues(false);
//        set.setMode(LineDataSet.Mode.CUBIC_BEZIER);//设置曲线为圆滑的线
//        set.setCubicIntensity(0.1f);
//        set.setDrawCircles(false);  //设置有圆点
//        set.setLineWidth(1f);    //设置线的宽度
//        set.setDrawFilled(true);//设置包括的范围区域填充颜色
//        set.setCircleColor(getResources().getColor(R.color.text_color_blue, null));
//        set.setColor(getResources().getColor(R.color.colorPrimary, null));
//        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
//        dataSets.add(set); // add the datasets
//        LineData data = new LineData(dataSets);
//        return data;
//    }

}

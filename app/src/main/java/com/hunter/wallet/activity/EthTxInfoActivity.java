package com.hunter.wallet.activity;


import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.EthToken;
import com.hunter.wallet.entity.EthTransfer;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.service.TokenManageService;
import com.hunter.wallet.service.TxManageService;
import com.hunter.wallet.service.WalletManageService;
import com.hunter.wallet.entity.WalletInfo;
import com.hunter.wallet.utils.AppUtils;
import com.hunter.wallet.utils.RemindUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class EthTxInfoActivity extends FragmentActivity implements View.OnClickListener, WalletManageService.UnlockWalletCallback {
    private static SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private TxManageService txManageService = TxManageService.getInstance();
    private TokenManageService tokenManageService = TokenManageService.getInstance();
    private WalletManageService walletManageService = WalletManageService.getInstance();

    //    private LineChart mChart;
    private TextView balanceView;
    private ListView txListView;

    private WalletInfo wallet;
    private EthToken ethToken;
    private BigDecimal balance;
    private List<EthTransfer> ethTransfers;
    private Timer updateTimer;
    private TextView coinvalue;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        wallet = (WalletInfo) getIntent().getSerializableExtra(WalletInfo.class.getName());
        ethToken = (EthToken) getIntent().getSerializableExtra(EthToken.class.getName());
        balance = txManageService.getWalletBalance(this, wallet, ethToken);
        ethTransfers = txManageService.getEthTransfer(this, wallet, ethToken);


        setContentView(R.layout.coin_info_layout);
        TextView infoWalletName = findViewById(R.id.infoWalletName);
//        mChart = findViewById(R.id.lineChart);
        balanceView = findViewById(R.id.balanceView);
        txListView = findViewById(R.id.transcationList);
        coinvalue = findViewById(R.id.coinvalue);

        findViewById(R.id.coinInfoPreBut).setOnClickListener(this);
        findViewById(R.id.sendTransaction).setOnClickListener(this);
        findViewById(R.id.incomeBut).setOnClickListener(this);

        infoWalletName.setText(ethToken.getSymbol());
        balanceView.setText(balance.toString());
        drawChart();
        drawTransferList();

        txListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EthTransfer itemAtPosition = (EthTransfer) parent.getItemAtPosition(position);
                Intent intent = new Intent(EthTxInfoActivity.this, EthTxDetailActivity.class);
                intent.putExtra(EthTransfer.class.getName(), itemAtPosition);
                intent.putExtra(EthToken.class.getName(), ethToken);
                startActivity(intent);
            }
        });
    }

    private void startUpdateThread() {
        if (updateTimer != null) {
            updateTimer.cancel();
        }
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                balance = txManageService.getWalletBalance(EthTxInfoActivity.this, wallet, ethToken);
                ethTransfers.clear();
                ethTransfers.addAll(txManageService.getEthTransfer(EthTxInfoActivity.this, wallet, ethToken));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        balanceView.setText(balance.toString());
                        coinvalue.setText("≈ $ " + tokenManageService.getTokenPriceUsd(EthTxInfoActivity.this, ethToken).multiply(balance).toString());
                        ((ArrayAdapter) txListView.getAdapter()).notifyDataSetChanged();
                    }
                });
            }
        }, 0, 10000);
    }

    private void stopUpdateThread() {
        if (updateTimer != null) {
            updateTimer.cancel();
            updateTimer = null;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        try {
            wallet = walletManageService.getWallet(wallet.getId());
        } catch (UnexpectedException e) {
            e.printStackTrace();
        }
        startUpdateThread();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopUpdateThread();
    }

    private void drawChart() {
        // 绘制图标
    }

    private void drawTransferList() {
        txListView.setAdapter(new ArrayAdapter<EthTransfer>(this, R.layout.tx_list_view_item, ethTransfers) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                if (convertView == null) {
                    convertView = getLayoutInflater().inflate(R.layout.tx_list_view_item, null);
                }
                EthTransfer item = getItem(position);
                ImageView icon = convertView.findViewById(R.id.transcationIcon);
                TextView statusText = convertView.findViewById(R.id.statusText);
                TextView ethMsg = convertView.findViewById(R.id.ethMsg);
                TextView tranNum = convertView.findViewById(R.id.tranNum);
                TextView tranTime = convertView.findViewById(R.id.tranTime);

                if (item.getStatus() == EthTransfer.STATUS_UNCOMMIT) {
                    icon.setImageResource(R.drawable.tx_jinxingzhong);
                    statusText.setText("交易中");
                } else if (item.getStatus() == EthTransfer.STATUS_ERROR) {
                    icon.setImageResource(R.drawable.tx_fail);
                    statusText.setText("失败");
                } else if (Arrays.equals(item.getFrom(), wallet.getAddr())) {
                    icon.setImageResource(R.drawable.tx_pay);
                    statusText.setText("转出");
                } else {
                    icon.setImageResource(R.drawable.tx_income);
                    statusText.setText("收入");
                }
                BigDecimal amount = new BigDecimal(item.getValue()).divide(BigDecimal.TEN.pow(ethToken.getDecimals()));

                if (Arrays.equals(item.getFrom(), wallet.getAddr())) {
                    ethMsg.setText("-\b" + amount);
                    ethMsg.setTextColor(Color.RED);
                } else {
                    ethMsg.setText("+\b" + amount);
                    ethMsg.setTextColor(Color.BLUE);
                }
                tranNum.setText(item.getHash());
                tranTime.setText(sf.format(item.getTime()));
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
                EthTxInfoActivity.this.finish();
                break;
            case R.id.sendTransaction: {
                BigDecimal ethBalance = txManageService.getWalletBalance(EthTxInfoActivity.this, wallet, tokenManageService.getEthToken(EthTxInfoActivity.this));
                if (wallet.isHasLock()) {
                    walletManageService.unlockWallet(EthTxInfoActivity.this, wallet, EthTxInfoActivity.this);
                } else if (ethBalance.compareTo(BigDecimal.ZERO) <= 0) {
                    RemindUtils.toastShort(this, "ETH余额不足");
                } else if (balance.compareTo(BigDecimal.ZERO) <= 0) {
                    RemindUtils.toastShort(this, ethToken.getSymbol() + "余额不足");
                } else {
                    Intent intent = new Intent(EthTxInfoActivity.this, EthTxActivity.class);
                    intent.putExtra(WalletInfo.class.getName(), wallet);
                    intent.putExtra(EthToken.class.getName(), ethToken);
                    startActivity(intent);
                }
            }
            break;
            case R.id.incomeBut: {
                Intent intent = new Intent(EthTxInfoActivity.this, AddressShowActivity.class);
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

package com.hunter.wallet.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.Price;
import com.hunter.wallet.utils.HTTPUtils;
import com.hunter.wallet.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PriceFragment extends Fragment implements View.OnClickListener {
    public static final int HANGQING_PERCENT_CHANGE_1H = 1;
    public static final int HANGQING_PERCENT_CHANGE_24H = 2;
    public static final int HANGQING_PERCENT_CHANGE_7D = 3;

    private View view;
    private RecyclerView recyclerView;

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        Log.i("PriceFragment", "onHiddenChanged hidden:"+ hidden);
        if(hidden){
        } else {
        }
    }

    private TextView floatText;

    private HangQingAdapter adapter;
    private List<Price> data = new ArrayList<>();
    private int page = 1;
    private boolean last = false;
    private Timer timer;
    private boolean isFlash = false;
    private int type;


    @Override
    public void onPause() {
        super.onPause();
        timer.cancel();
    }

    @Override
    public void onStop() {
        super.onStop();
        timer.cancel();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        timer.cancel();
    }



    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            recyclerView.setAdapter(adapter);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.main_hangqing_fragment, null);
        super.onCreate(savedInstanceState);
        recyclerView = view.findViewById(R.id.hangQingList);
        floatText = view.findViewById(R.id.floatText);

        recyclerView.setAdapter( new HangQingAdapter(data));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        type = HANGQING_PERCENT_CHANGE_24H;

        floatText.setOnClickListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (recyclerView.canScrollVertically(1)) {
                } else {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            List<Price> list = getPage();
                            if (null == data) {
                                data = new ArrayList<>();
                            }
                            data.addAll(list);
                            new Handler(Looper.getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    adapter.update(data);
                                }
                            });
                        }
                    }).start();
                }
            }
        });
        new Thread(new Runnable() {
            @Override
            public void run() {
                String jsonStr = "";
                if (null == jsonStr || jsonStr.trim().equals("")) {
                    data = getPage();

                } else {
                    Log.i("HangQing", "缓存命中");
                    data = JsonUtils.jsonToList(jsonStr, Price.class);
                }
                adapter = new HangQingAdapter(data);
                handler.sendMessage(new Message());
            }
        }).start();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!isFlash) {
                    int num = (null == data ? 10 : data.size());
                    List<Price> list = HTTPUtils.getList("http://wallet.hdayun.com/market/getPrice?start=0&limit=" + num, Price.class);
                    if (null != list && list.size() > 0) {
                        data = list;
                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                adapter.update(data);
                            }
                        });
                    }
                }
            }
        }, 3000, 15000);
    }

    private void changePeriod(int type) {
        switch (type) {
            case HANGQING_PERCENT_CHANGE_1H:
                floatText.setText("涨跌幅(\b1h\b)");
                break;
            case HANGQING_PERCENT_CHANGE_24H:
                floatText.setText("涨跌幅(\b24h\b)");
                break;
            case HANGQING_PERCENT_CHANGE_7D:
                floatText.setText("涨跌幅(\b7d\b)");
                break;
        }
        recyclerView.getAdapter().notifyDataSetChanged();
    }

    public List<Price> getPage() {
        isFlash = true;
        List<Price> list = HTTPUtils.getList("http://wallet.hdayun.com/market/getPrice?start=" + (page - 1) * 10 + "&limit=10", Price.class);
        if (null != list) {
            if (list.size() == 10) {
                page++;
            } else {
                last = true;
            }
            list.forEach(System.out::println);
            isFlash = false;
            return list;
        } else {
            isFlash = false;
            return null;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.floatText:
                type++;
                type %= 3;
                changePeriod(type);
                break;
        }
    }

    public class HangQingAdapter extends RecyclerView.Adapter<HangQingAdapter.ViewHolder> {

        private List<Price> data;

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.main_hangqing_fragment_item_layout, parent, false);
            ViewHolder holder = new ViewHolder(view);
            return holder;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            Price price = data.get(position);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("onBindViewHolder", price.toString());
                }
            });
            holder.coinName.setText(price.getName());
            holder.coinSymName.setText(price.getSymbol());
            holder.priceCNY.setText("¥" + price.getPriceCNY());
            holder.priceUSD.setText("" + price.getPriceUSD());
            double percentChange = 0;
            switch (PriceFragment.this.type) {
                case PriceFragment.HANGQING_PERCENT_CHANGE_1H:
                    percentChange = price.getPercentChange1h();
                    break;
                case PriceFragment.HANGQING_PERCENT_CHANGE_24H:
                    percentChange = price.getPercentChange24h();
                    break;
                case PriceFragment.HANGQING_PERCENT_CHANGE_7D:
                    percentChange = price.getPercentChange7d();
                    break;
            }

            if (percentChange > 0) {
                holder.icon.setImageResource(R.drawable.js);
                holder.view.setBackgroundResource(R.drawable.fillet_fill_green);
            } else {
                holder.icon.setImageResource(R.drawable.jx);
                holder.view.setBackgroundResource(R.drawable.fillet_fill_rad);
            }
            holder.num.setText(Math.abs(percentChange) + "%");
        }

        private HangQingAdapter(List<Price> data) {
            this.data = data;
        }

        public void update(List<Price> data) {
            this.data = data;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            return null == data ? 0 : data.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView coinName;
            TextView coinSymName;
            TextView priceUSD;
            TextView priceCNY;
            ImageView icon;
            TextView num;
            View view;

            public ViewHolder(View itemView) {
                super(itemView);
                coinName = itemView.findViewById(R.id.coinName);
                coinSymName = itemView.findViewById(R.id.coinSymName);
                priceUSD = itemView.findViewById(R.id.priceUSD);
                priceCNY = itemView.findViewById(R.id.priceCNY);
                icon = itemView.findViewById(R.id.walletIcon);
                num = itemView.findViewById(R.id.num);
                view = itemView.findViewById(R.id.bgLayout);
            }
        }
    }

}

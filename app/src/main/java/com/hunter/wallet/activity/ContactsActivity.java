package com.hunter.wallet.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.Contacts;
import com.hunter.wallet.fragment.UserFragment;
import com.hunter.wallet.utils.JsonUtils;
import com.hunter.wallet.utils.SharedPreferencesUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactsActivity extends Activity implements View.OnClickListener {
    private ListView listView;
    private String startForm;
    private List<Contacts> contactsList = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info_fragment_contacts_layout);

        listView = findViewById(R.id.listView);
        findViewById(R.id.contactsPreBut).setOnClickListener(this);
        findViewById(R.id.contactsAddbut).setOnClickListener(this);

        Intent intent = getIntent();
        startForm = intent.getStringExtra("startFrom");

        listView.setAdapter(new ArrayAdapter<Contacts>(ContactsActivity.this
                , R.layout.main_info_fragment_contacts_item
                , contactsList) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                if (convertView == null) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.main_info_fragment_contacts_item, null);
                }
                Contacts item = getItem(position);
                TextView name = convertView.findViewById(R.id.name);
                TextView address = convertView.findViewById(R.id.address);
                address.setText(item.getAddress());
                name.setText(item.getName());
                return convertView;
            }
        });

        if (startForm != null && startForm.equals(UserFragment.class.getName())) {
            // 用户界面进入，打开编辑界面
        } else {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    Contacts contacts = (Contacts) parent.getItemAtPosition(position);
                    Intent intent = new Intent();
                    intent.putExtra("result", contacts.getAddress());
                    setResult(Activity.RESULT_OK, intent);
                    ContactsActivity.this.finish();
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        contactsList.clear();
        contactsList.addAll(getContacts());
        ((ArrayAdapter) listView.getAdapter()).notifyDataSetChanged();
    }
//
//    Handler handler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            listView.setAdapter((ListAdapter) msg.obj);
//            if (itemClickAble) {
//                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        Contacts contacts = (Contacts) parent.getItemAtPosition(position);
//                        Intent intent = new Intent();
//                        intent.putExtra("result", contacts.getAddress());
//                        setResult(Activity.RESULT_OK, intent);
//                        ContactsActivity.this.finish();
//                    }
//                });
//            }
//        }
//    };

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.contactsPreBut:
                finish();
                break;
            case R.id.contactsAddbut:
                Intent intent = new Intent(ContactsActivity.this, ContactsAddActivity.class);
                startActivity(intent);
                break;

        }
    }

//    public class ContactsAdapter extends ArrayAdapter {
//        private final int resourceId;
//
//        public ContactsAdapter(Context context, int textViewResourceId, List<Contacts> objects) {
//            super(context, textViewResourceId, objects);
//            resourceId = textViewResourceId;
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            ViewHolder holder;
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(resourceId, null);
//                holder = new ViewHolder(convertView);
//                convertView.setTag(holder);
//            } else {
//                holder = (ViewHolder) convertView.getTag();
//            }
//            Contacts item = (Contacts) getItem(position);
//            holder.address.setText(item.getAddress());
//            holder.name.setText(item.getName());
//            return convertView;
//        }
//
//        private class ViewHolder {
//            TextView name;
//            TextView address;
//
//            public ViewHolder(View view) {
//                name = (TextView) view.findViewById(R.id.name);
//                address = (TextView) view.findViewById(R.id.address);
//            }
//        }
//    }
//
//    private void init() {
//        List<Contacts> data = getContacts();
//        if (null == data || data.size() < 1) {
//            findViewById(R.id.noContacts).setVisibility(View.VISIBLE);
//            listView.setVisibility(View.GONE);
//            return;
//        }
//        ContactsAdapter adapter = new ContactsAdapter(ContactsActivity.this
//                , R.layout.main_info_fragment_contacts_item
//                , data);
//        Message message = new Message();
//        message.obj = adapter;
//        handler.sendMessage(message);
//    }

    public List<Contacts> getContacts() {
        List<Contacts> data = new ArrayList();
        Map<String, ?> all = SharedPreferencesUtils.getAll(this, "Contacts");
        all.forEach((k, v) -> {
            Contacts contacts = JsonUtils.jsonToPojo(v.toString(), Contacts.class);
            data.add(contacts);
        });
        return data;
    }
}

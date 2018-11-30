package com.hunter.wallet.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.hunter.wallet.R;
import com.hunter.wallet.entity.Contacts;
import com.hunter.wallet.utils.AddressEncoder;
import com.hunter.wallet.utils.JsonUtils;
import com.hunter.wallet.utils.SharedPreferencesUtils;
import com.xys.libzxing.zxing.activity.CaptureActivity;

import java.io.IOException;

/**
 * Created by DT0814 on 2018/8/31.
 */

public class ContactsAddActivity extends Activity implements View.OnClickListener {
    private EditText address;
    private static int ETH_TYPE = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_info_fragment_contacts_add_layout);
        address = findViewById(R.id.address);
        findViewById(R.id.addContactsPreBut).setOnClickListener(this);
        findViewById(R.id.addContactsBut).setOnClickListener(this);
        findViewById(R.id.saoyisao).setOnClickListener(this);
    }

    public Contacts write(Contacts contacts) {
        contacts.setCid(getId());
        SharedPreferencesUtils.writeString(this, "Contacts", "contacts_" + contacts.getCid(), JsonUtils.objectToJson(contacts));
        return contacts;
    }

    private int getId() {
        int anInt = SharedPreferencesUtils.getInt(this, "ContactsID", "id", 1);
        SharedPreferencesUtils.writeInt(this, "ContactsID", "id", ++anInt);
        return anInt;
    }

    /**
     * 扫过二维码回调
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Bundle bundle = data.getExtras();
            String result = bundle.getString("result");
            try {
                if (result.startsWith("0x") || result.startsWith("0X")) {
                    address.setText(result);
                } else if (result.startsWith("iban:XE") || result.startsWith("IBAN:XE")) {
                    address.setText(AddressEncoder.decodeICAP(result).getAddress());
                } else if (result.startsWith("iban:") || result.startsWith("IBAN:")) {
                    address.setText(AddressEncoder.decodeLegacyLunary(result).getAddress());
                } else if (result.startsWith("ethereum:") || result.startsWith("ETHEREUM:")) {
                    address.setText(AddressEncoder.decodeERC(result).getAddress());
                }
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(ContactsAddActivity.this, "二维码解析失败", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addContactsPreBut:
                ContactsAddActivity.this.finish();
                break;
            case R.id.saoyisao:
                if (ContextCompat.checkSelfPermission(ContactsAddActivity.this,
                        android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    //先判断有没有权限 ，没有就在这里进行权限的申请
                    ActivityCompat.requestPermissions(ContactsAddActivity.this,
                            new String[]{android.Manifest.permission.CAMERA}, 1);
                } else {
                    startActivityForResult(new Intent(ContactsAddActivity.this, CaptureActivity.class), 0);
                }
                break;
            case R.id.addContactsBut:
                String contactsNameStr = ((TextView) findViewById(R.id.contactsName)).getText().toString().trim();
                String addressStr = address.getText().toString().trim();
                if (contactsNameStr.equals("")) {
                    Toast.makeText(ContactsAddActivity.this, "联系人不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (addressStr.equals("")) {
                    Toast.makeText(ContactsAddActivity.this, "地址不能为空", Toast.LENGTH_SHORT).show();
                    return;
                }
                Contacts contacts = new Contacts(contactsNameStr, addressStr, ETH_TYPE);
                String phoneStr = ((TextView) findViewById(R.id.phone)).getText().toString().trim();
                String emailStr = ((TextView) findViewById(R.id.email)).getText().toString().trim();
                String remarksStr = ((TextView) findViewById(R.id.remarks)).getText().toString().trim();
                if (!phoneStr.equals("")) {
                    contacts.setPhone(phoneStr);
                }
                if (!emailStr.equals("")) {
                    contacts.setEmile(emailStr);
                }
                if (!remarksStr.equals("")) {
                    contacts.setRemarks(remarksStr);
                }

                Contacts write = write(contacts);
                if (null != write) {
                    Log.i("addContacts", write.toString());
                    Toast.makeText(ContactsAddActivity.this, "添加成功", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ContactsAddActivity.this, "添加失败", Toast.LENGTH_SHORT).show();
                }
                ContactsAddActivity.this.finish();
                break;
        }
    }
}

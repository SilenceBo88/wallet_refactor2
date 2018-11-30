package com.hunter.wallet.service;

import android.util.Log;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hunter.wallet.entity.InvokeResult;
import com.hunter.wallet.entity.UserInfo;
import com.hunter.wallet.exception.PinLockedException;
import com.hunter.wallet.exception.UnexpectedException;
import com.hunter.wallet.exception.VerifyFailException;
import com.hunter.wallet.utils.StringUtils;

import org.web3j.utils.Numeric;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserManageService {
    static {
        System.loadLibrary("wsservice");
    }

    private static UserManageService instance = new UserManageService();

    private UserManageService() {
    }

    public static UserManageService getInstance() {
        return instance;
    }

    public native UserInfo getUserInfo() throws UnexpectedException;

    public native void userInit(byte[] pin, String mobile, byte[] signature)
            throws UnexpectedException;

    public native void changePin(byte[] pin, byte[] newPin)
            throws PinLockedException, VerifyFailException, UnexpectedException;

    public native void rebindMobile(byte[] pin, String newMobile, byte[] signature)
            throws PinLockedException, VerifyFailException, UnexpectedException;

    public native void unlockPin(byte[] signature) throws UnexpectedException;

    public native void resetWallet(byte[] pin, byte[] signature)
            throws PinLockedException, VerifyFailException, UnexpectedException;


    private static final String walletPlatformHost = "http://wallet.hdayun.com";
    private static final String sendAuthcodePath = walletPlatformHost + "/user/sendAuthcode";
    private static final String bindMobilePath = walletPlatformHost + "/user/bindMobile";
    private static final String rebindMobilePath = walletPlatformHost + "/user/rebindMobile";
    private static final String unlockWalletPath = walletPlatformHost + "/user/unlockPin";
    private static final String resetWalletPath = walletPlatformHost + "/user/resetWallet";

    public interface UserManageCallback {
        void onSuccess(String msg);

        void onFail(String msg);
    }

    public void sendAuthcode(String mobile, UserManageCallback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        FormBody formBody = new FormBody.Builder()
                .add("mobile", mobile)
                .build();
        Request request = new Request.Builder().url(sendAuthcodePath).post(formBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFail("网络错误");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                TypeReference<InvokeResult<Object>> typeReference = new TypeReference<InvokeResult<Object>>() {
                };
                InvokeResult<Object> invokeResult = objectMapper.readValue(result, typeReference);
                if (invokeResult.isSuccess()) {
                    if (callback != null) {
                        callback.onSuccess("发送成功");
                    }
                } else {
                    if (callback != null) {
                        callback.onFail("发送失败");
                    }
                }
            }
        });
    }

    private void requestSignature(String path, FormBody formBody, Callback callback) {
        OkHttpClient okHttpClient = new OkHttpClient();
        Request request = new Request.Builder().url(path).post(formBody).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(callback);
    }

    public void userInit(String pin, String mobile, String authcode, UserManageCallback callback)
            throws UnexpectedException {

        UserInfo userInfo = getUserInfo();
        FormBody formBody = new FormBody.Builder()
                .add("mobile", mobile)
                .add("authcode", authcode)
                .add("deviceId", Numeric.toHexStringNoPrefix(userInfo.getDeviceId()))
                .build();
        requestSignature(bindMobilePath, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFail("网络错误");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                InvokeResult<String> invokeResult = objectMapper.readValue(response.body().string(), new TypeReference<InvokeResult<String>>() {
                });
                if (invokeResult.isSuccess()) {
                    try {
                        userInit(StringUtils.toUTF8(pin), mobile, Numeric.hexStringToByteArray(invokeResult.getData()));
                        if (callback != null) {
                            callback.onSuccess("初始化成功");
                        }
                    } catch (UnexpectedException e) {
                        e.printStackTrace();
                        if (callback != null) {
                            callback.onFail(e.getMessage());
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onFail("验证失败");
                    }
                }
            }
        });
    }

    public void changePin(String pin, String newPin)
            throws UnexpectedException, VerifyFailException, PinLockedException {
        changePin(StringUtils.toUTF8(pin), StringUtils.toUTF8(newPin));
    }

    public interface RebindMobileCallback {
        void onSuccess();

        void onFail(String msg);

        void onPinlock();
    }

    public void rebindMobile(String pin, String authcode, String newMobile, String newAuchcode, RebindMobileCallback callback)
            throws UnexpectedException {
        UserInfo userInfo = getUserInfo();
        FormBody formBody = new FormBody.Builder()
                .add("mobile", userInfo.getBindMobile())
                .add("authcode", authcode)
                .add("newMobile", newMobile)
                .add("newAuchcode", newAuchcode)
                .add("deviceId", Numeric.toHexStringNoPrefix(userInfo.getDeviceId()))
                .build();

        requestSignature(rebindMobilePath, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFail("网络错误");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                InvokeResult<String> invokeResult = objectMapper.readValue(response.body().string(), new TypeReference<InvokeResult<String>>() {
                });
                if (invokeResult.isSuccess()) {
                    try {
                        rebindMobile(StringUtils.toUTF8(pin), newMobile, Numeric.hexStringToByteArray(invokeResult.getData()));

                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } catch (UnexpectedException e) {
                        e.printStackTrace();
                        if (callback != null) {
                            callback.onFail(e.getMessage());
                        }
                    } catch (PinLockedException e) {
                        if (callback != null) {
                            callback.onPinlock();
                        }
                    } catch (VerifyFailException e) {
                        if (callback != null) {
                            callback.onFail("PIN码错误");
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onFail("验证失败");
                    }
                }
            }
        });
    }

    public interface UnlockPinCallback {
        void onSuccess(String msg);

        void onFail(String msg);
    }

    public void unlockPin(String authcode, UnlockPinCallback callback)
            throws UnexpectedException {
        UserInfo userInfo = getUserInfo();
        FormBody formBody = new FormBody.Builder()
                .add("mobile", userInfo.getBindMobile())
                .add("authcode", authcode)
                .add("userAuthcode", Numeric.toHexStringNoPrefix(userInfo.getAuthCode()))
                .add("deviceId", Numeric.toHexStringNoPrefix(userInfo.getDeviceId()))
                .build();
        requestSignature(unlockWalletPath, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFail("网络错误");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                InvokeResult<String> invokeResult = objectMapper.readValue(response.body().string(), new TypeReference<InvokeResult<String>>() {
                });
                if (invokeResult.isSuccess()) {
                    try {
                        unlockPin(Numeric.hexStringToByteArray(invokeResult.getData()));
                        if (callback != null) {
                            callback.onSuccess("解锁成功");
                        }
                    } catch (UnexpectedException e) {
                        e.printStackTrace();
                        if (callback != null) {
                            callback.onFail(e.getMessage());
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onFail("验证失败");
                    }
                }
            }
        });
    }

    public interface ResetCallback {
        void onSuccess();

        void onFail(String msg);

        void onPinLock();
    }

    public void reset(String pin, String authcode, ResetCallback callback)
            throws UnexpectedException {
        UserInfo userInfo = getUserInfo();
        FormBody formBody = new FormBody.Builder()
                .add("mobile", userInfo.getBindMobile())
                .add("authcode", authcode)
                .add("userAuthcode", Numeric.toHexStringNoPrefix(userInfo.getAuthCode()))
                .add("deviceId", Numeric.toHexStringNoPrefix(userInfo.getDeviceId()))
                .build();
        requestSignature(resetWalletPath, formBody, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (callback != null) {
                    callback.onFail("网络错误");
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                String body = response.body().string();
                Log.d("resetWALLET", body);
                InvokeResult<String> invokeResult = objectMapper.readValue(body, new TypeReference<InvokeResult<String>>() {
                });
                if (invokeResult.isSuccess()) {
                    try {
                        resetWallet(StringUtils.toUTF8(pin), Numeric.hexStringToByteArray(invokeResult.getData()));
                        if (callback != null) {
                            callback.onSuccess();
                        }
                    } catch (UnexpectedException e) {
                        e.printStackTrace();
                        if (callback != null) {
                            callback.onFail(e.getMessage());
                        }
                    } catch (PinLockedException e) {
                        if (callback != null) {
                            callback.onPinLock();
                        }
                    } catch (VerifyFailException e) {
                        if (callback != null) {
                            callback.onFail("PIN码错误");
                        }
                    }
                } else {
                    if (callback != null) {
                        callback.onFail("验证失败");
                    }
                }
            }
        });
    }
}

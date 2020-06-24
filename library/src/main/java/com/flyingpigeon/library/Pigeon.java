package com.flyingpigeon.library;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.flyingpigeon.library.anotation.RequestLarge;
import com.flyingpigeon.library.anotation.ResponseLarge;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static com.flyingpigeon.library.Config.PREFIX;
import static com.flyingpigeon.library.ServiceManager.KEY_FLAGS;

/**
 * @author xiaozhongcen
 * @date 20-6-8
 * @since 1.0.0
 */
public final class Pigeon {

    private static final String TAG = PREFIX + Pigeon.class.getSimpleName();

    private String authority;
    private Context mContext;
    private final Uri base;

    private Pigeon(Builder builder) {
        authority = builder.authority;
        mContext = builder.mContext;
        base = Uri.parse("content://" + authority);
    }


    public <T> T create(final Class<T> service) {
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class[]{service}, new InvocationHandler() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return (T) call(service, proxy, method, args);
            }
        });
    }

    public FlyPigeon route(String route) {
        return new FlyPigeon(this, route);
    }

    public LargeFlyPigeon route(String route, Object... params) {
        return new LargeFlyPigeon(this, route, params);
    }


    String fly(String route, Object[] params) {
        return null;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private Object call(Class<?> service, Object proxy, Method method, Object[] args) {
        // large
        RequestLarge requestLarge = method.getAnnotation(RequestLarge.class);
        ResponseLarge responseLarge = method.getAnnotation(ResponseLarge.class);
        int flags = 0;
        if (requestLarge == null) {
            flags = ParametersSpec.setRequestNormal(flags);
        } else {
            flags = ParametersSpec.setRequestLarge(flags);
        }
        if (responseLarge == null) {
            flags = ParametersSpec.setResponseNormal(flags);
        } else {
            flags = ParametersSpec.setResponseLarge(flags);
        }

        boolean isRequestLarge = ParametersSpec.isRequestParameterLarge(flags);
        if (isRequestLarge) {
            return callByContentProvideInsert(service, proxy, method, args);
        }

        Bundle bundle = ServiceManager.getInstance().buildRequest(service, proxy, method, args);
        bundle.putInt(KEY_FLAGS, flags);
        ContentResolver contentResolver = mContext.getContentResolver();
        Bundle response = contentResolver.call(base, method.getName(), null, bundle);
        Object o = null;
        try {
            o = ServiceManager.getInstance().parseReponse(response, method);
        } catch (CallRemoteException e) {
            throw new RuntimeException(e);
        }
        return o;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private @Nullable
    Object callByContentProvideInsert(Class<?> service, Object proxy, Method method, Object[] args) {
        ContentValues contentValues = ServiceManager.getInstance().buildRequestInsert(service, proxy, method, args);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = base.buildUpon().appendPath("pigeon/0/" + method.getName()).build();
        Uri result = contentResolver.insert(uri, contentValues);
        return result.getQueryParameter("result");
    }

    Bundle fly(@NonNull Bundle in) {
        ServiceManager.getInstance().buildRequestRoute(in);
        ContentResolver contentResolver = mContext.getContentResolver();
        Bundle response = contentResolver.call(base, "", null, in);
        try {
            ServiceManager.getInstance().parseReponse(response);
        } catch (CallRemoteException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public static Pigeon.Builder newBuilder(@NonNull Context context) {
        Objects.requireNonNull(context);
        return new Builder(context);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    String routeLargeRequest(String route, Object[] params) {
        ContentValues contentValues = ServiceManager.getInstance().buildRouteRequestInsert(route, params);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = base.buildUpon().appendPath("pigeon/1").appendPath(route).build();
        Log.e(TAG, "uri:" + uri.toString() + " contentValues:" + contentValues + " contentResolver:" + contentResolver);
        Uri result = contentResolver.insert(uri, contentValues);
        if (null == result) {
            return "";
        }
        return result.getQueryParameter("result");
    }

    String routeLargeResponse(String route, Object[] params) {
        String[] args = ServiceManager.getInstance().buildRequestQuery(route, params);
        ContentResolver contentResolver = mContext.getContentResolver();
        Uri uri = base.buildUpon().appendPath("pigeon/10/" + route).build();
        Cursor result = contentResolver.query(uri, new String[]{}, "", args, "");
        if (null != result) {
            result.close();
        }
        return null;
    }


    public static final class Builder {

        private Context mContext;
        private String authority;

        private Builder(Context context) {
            this.mContext = context;
        }

        public Builder setAuthority(@NonNull String authority) {
            if (TextUtils.isEmpty(authority)) {
                throw new IllegalArgumentException("authorities error");
            }
            this.authority = authority;
            return this;
        }

        public Builder setAuthority(@NonNull Class<?> service) {
            PackageInfo packageInfos = null;
            try {
                PackageManager packageManager = mContext.getPackageManager();
                if (packageManager != null) {
                    packageInfos =
                            packageManager.getPackageInfo(mContext.getPackageName(), PackageManager.GET_PROVIDERS);
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (packageInfos != null && packageInfos.providers != null) {
                for (ProviderInfo providerInfo : packageInfos.providers) {
                    if (providerInfo.name.equals(service.getName())) {
                        authority = providerInfo.authority;
                    }
                }
            }
            if (TextUtils.isEmpty(this.authority)) {
                new IllegalArgumentException("service is not exists");
            }
            return this;
        }

        public Pigeon build() {
            return new Pigeon(this);
        }


    }

}

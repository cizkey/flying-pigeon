/*
 * Copyright (C)  Justson(https://github.com/Justson/flying-pigeon)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.flyingpigeon.library;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * @author xiaozhongcen
 * @date 20-6-12
 * @since 1.0.0
 */
public class Empty implements Parcelable, Serializable {
    public Empty() {
    }

    protected Empty(Parcel in) {
    }

    public static final Creator<Empty> CREATOR = new Creator<Empty>() {
        @Override
        public Empty createFromParcel(Parcel in) {
            return new Empty(in);
        }

        @Override
        public Empty[] newArray(int size) {
            return new Empty[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}

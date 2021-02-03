/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;

/**
 * To be used as a wrapper class to create generic level status message.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public final class LightCtlStatus extends GenericStatusMessage implements Parcelable {

    private static final String TAG = LightCtlStatus.class.getSimpleName();
    private static final int LIGHT_CTL_STATUS_MANDATORY_LENGTH = 4;
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_CTL_STATUS;
    private int mPresentCtlLightness;
    private int mPresentCtlTemperature;
    private int mPresentCtlDeltaUv;
    private int mPresentCtlAll;

    private static final Creator<LightCtlStatus> CREATOR = new Creator<LightCtlStatus>() {
        @Override
        public LightCtlStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new LightCtlStatus(message);
        }

        @Override
        public LightCtlStatus[] newArray(int size) {
            return new LightCtlStatus[size];
        }
    };

    /**
     * Constructs LightCtlStatus message
     *
     * @param message access message
     */
    public LightCtlStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        Log.v(TAG, "Received light ctl status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        mPresentCtlLightness = buffer.getShort() & 0xFFFF;
        mPresentCtlTemperature = buffer.getShort() & 0xFFFF;
        mPresentCtlDeltaUv = buffer.getShort();
        Log.v(TAG, "Present lightness: " + mPresentCtlLightness);
        Log.v(TAG, "Present temperature: " + mPresentCtlTemperature);
        Log.v(TAG, "Present delta_uv: " + mPresentCtlDeltaUv);
    }

    @Override
    int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the present level of the GenericOnOffModel
     *
     * @return present level
     */
    public final int getPresentLightness() {
        return mPresentCtlLightness;
    }

    /**
     * Returns the present level of the GenericOnOffModel
     *
     * @return present level
     */
    public final int getPresentTemperature() {
        return mPresentCtlTemperature;
    }


    public final int getPresentDeltaUv() {
        return mPresentCtlDeltaUv;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }
}

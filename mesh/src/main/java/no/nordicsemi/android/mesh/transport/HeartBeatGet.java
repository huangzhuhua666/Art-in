package no.nordicsemi.android.mesh.transport;


import androidx.annotation.NonNull;

import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class to create light ctl get message.
 */
@SuppressWarnings("unused")
public class HeartBeatGet extends GenericMessage {

    private static final String TAG = HeartBeatGet.class.getSimpleName();
    private static final int OP_CODE = (0xC0 | 0x14) << 16;


    /**
     * Constructs LightCtlGet message.
     *
     * @param appKey {@link ApplicationKey} key for this message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public HeartBeatGet(@NonNull final ApplicationKey appKey) throws IllegalArgumentException {
        super(appKey);
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
    }
}

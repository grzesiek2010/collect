package org.odk.collect.android;

import android.os.StrictMode;

import org.junit.Before;

public abstract class BaseRobolectricTest {

    @Before
    public void before() {
        StrictMode.VmPolicy policy = new StrictMode.VmPolicy.Builder().build();
        StrictMode.setVmPolicy(policy); // should not result in an exception
    }
}

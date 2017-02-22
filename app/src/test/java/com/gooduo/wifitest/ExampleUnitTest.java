package com.gooduo.wifitest;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {
        ControllerPoint p=new ControllerPoint(5);
        p.setLevel(12);
        assertEquals(10, p.getLevel());
    }

    @Test
    public void threadTest() throws Exception{



    }
}
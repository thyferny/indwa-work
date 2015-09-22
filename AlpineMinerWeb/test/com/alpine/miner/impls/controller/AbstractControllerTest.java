package com.alpine.miner.impls.controller;


import com.alpine.miner.impls.persistence.AbstractPersistenceTest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public abstract class AbstractControllerTest extends AbstractPersistenceTest {

    public static MockHttpServletRequest request;
    public static MockHttpServletResponse response;
    public static Gson gson;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeAbstractControllerTest() throws Exception {
        refreshMocks();

        gson = new GsonBuilder()
                .setDateFormat(java.text.DateFormat.LONG)
                .setPrettyPrinting()
                .serializeSpecialFloatingPointValues()
                .create();
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterAbstractControllerTest() throws Exception {

    }

    public static void refreshMocks() {
        response = new MockHttpServletResponse();
        request = new MockHttpServletRequest();
        request.addHeader("user-agent", "mozilla");
    }
}

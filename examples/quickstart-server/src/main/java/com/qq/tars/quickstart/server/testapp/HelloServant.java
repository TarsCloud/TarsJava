// **********************************************************************
// This file was generated by a TARS parser!
// TARS version 1.0.1.
// **********************************************************************

package com.qq.tars.quickstart.server.testapp;

import com.qq.tars.protocol.annotation.Servant;

@Servant
public interface HelloServant {
    public String hello(int no, String name);
}

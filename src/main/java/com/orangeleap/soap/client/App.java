package com.orangeleap.soap.client;

import com.orangeleap.soap.client.TestWS;

public class App 
{
  public static void main( String[] args )
  {
    TestWS testWS = new TestWS();
    
    testWS.init(args);
  }
}

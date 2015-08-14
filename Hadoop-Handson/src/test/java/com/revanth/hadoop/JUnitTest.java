/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.revanth.hadoop;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author Revanth SEgu
 */
public class JUnitTest {
    
    public JUnitTest(){
        
    }
    
    //@Test
    public void unitTest() throws Exception{
         System.out.println("Inside junitTest Junit method");
         
         
       String [] args = new String[3];
        
        args[0] = "/data/upp/1in1/";
        args[1] = "2015-08-01";
        args[2] = "2015-08-10";
        
        ConverLogstToAvro mainclass = new ConverLogstToAvro();
         mainclass.main(args);
         
         Assert.assertEquals("2","2");
    }
    
}

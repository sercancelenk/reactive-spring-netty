package com.example.reactivespring;

import lombok.extern.log4j.Log4j2;

@Log4j2
public class Util {
    public static void sleep(long l) {
        try{
            Thread.sleep(l);
        }catch (Exception e){
            log.error(e);
        }
    }
}

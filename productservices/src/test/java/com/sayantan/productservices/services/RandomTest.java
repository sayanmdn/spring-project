package com.sayantan.productservices.services;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class RandomTest {
    @Test
    void testOnePlusOneIsTwo(){
        int i = 1+1;

        assertEquals(2, i);
    }

//    @Test
//    void testTwoIntoTwoIsFour(){
//
//    }
}

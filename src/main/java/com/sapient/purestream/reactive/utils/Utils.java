package com.sapient.purestream.reactive.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
public class Utils {

     public static <T> T toObj(String json, Class<T> cl) throws java.io.IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, cl);
    }


}

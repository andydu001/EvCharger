package com.example.evcharger;

import java.util.HashMap;

public class Map extends HashMap<String, Integer> {
    String k;
    Integer obj;

    Integer putOb(String k, Integer obj){


        return super.put(k, obj);
    }


}

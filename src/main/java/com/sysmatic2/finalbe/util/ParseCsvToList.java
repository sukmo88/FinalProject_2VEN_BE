package com.sysmatic2.finalbe.util;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ParseCsvToList {
    /**
     * CSV 문자열을 Integer 리스트로 변환
     *
     * @param csv CSV 형식 문자열 (예: "1,2,3")
     * @return Integer 리스트 (예: [1, 2, 3])
     */
    public static List<Integer> parseCsvToIntegerList(String csv){
        if(csv == null || csv.isEmpty()){
            return new ArrayList<>();
        }

        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .map(Integer::parseInt)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    /**
     * CSV 문자열을 String 리스트로 변환
     *
     * @param csv CSV 형식 문자열 (예: "yes,no,null")
     * @return Integer 리스트 (예: [yes, no, null])
     */
    public static List<String> parseCsvToStringList(String csv){
        if(csv == null || csv.isEmpty()){
            return new ArrayList<>();
        }

        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .collect(Collectors.toCollection(ArrayList::new));
    }
}

//package com.sysmatic2.finalbe.config;
//
//import io.github.cdimascio.dotenv.Dotenv;
//import org.springframework.stereotype.Component;
//
//@Component
//public class EnvConfig {
//    private final Dotenv dotenv;
//
//    public EnvConfig() {
//        this.dotenv = Dotenv.configure()
//                .directory("./") // .env 파일 경로
//                .load();
//    }
//
//    public String get(String key) {
//        return dotenv.get(key);
//    }
//}

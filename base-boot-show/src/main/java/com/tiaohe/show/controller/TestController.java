package com.tiaohe.show.controller;

import com.tiaohe.lock.annotation.RedisDistributedLock;
import lombok.SneakyThrows;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @SneakyThrows
    @PostMapping("/testRedissonLock/{key}")
    @RedisDistributedLock(key = "#key")
    public String testRedissonLock(@PathVariable String key) {
        Thread.sleep(100000);
        return "success";
    }

    @PostMapping("/testRedissonLock2/{key}")
    @SneakyThrows
    @RedisDistributedLock(key = "#key", isSync = true)
    public String testRedissonLock2(@PathVariable String key) {
        Thread.sleep(100000);
        return "success";
    }
}

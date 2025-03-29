package com.tiaohe.show.controller;

import com.tiaohe.lock.annotation.RedisDistributedLock;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test")
public class TestController {
    @PostMapping("/testRedissonLock/{key}")
    @RedisDistributedLock(key = "#key")
    public String testRedissonLock(@PathVariable String key) throws InterruptedException {
        Thread.sleep(100000);
        return "success";
    }
}

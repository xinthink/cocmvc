package xink.spring.web.controllers;

import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping({"test/ann/", "/test/annotation"})
public class TestAnnoController {

    @RequestMapping("action1")
    public void relativePath() {}

    @RequestMapping("/action2")
    public void absolutePath() {}

    public void conventionalAction() {}
}

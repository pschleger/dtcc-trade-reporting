package com.java_template.common.tool;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.java_template.common.auth.Authentication;
import com.java_template.common.util.HttpUtils;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class WorkflowImportTool {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(Authentication.class, CyodaInit.class);
        context.register(HttpUtils.class, CyodaInit.class);
        context.registerBean(ObjectMapper.class, () -> new ObjectMapper());
        context.refresh();

        Authentication auth = context.getBean(Authentication.class);
        HttpUtils httpUtils = context.getBean(HttpUtils.class);

        CyodaInit init = new CyodaInit(httpUtils, auth);
        init.initCyoda().join();

        context.close();
    }
}


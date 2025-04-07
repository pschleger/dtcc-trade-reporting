package com.java_template.auxiliary;


import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Component
public class Randomizer {
    private static final Random random = new Random();

    public <T> T getRandomElement(List<T> list) {
        if (list == null || list.isEmpty()) {
            return null;
        }
        int randomIndex = random.nextInt(list.size());
        return list.get(randomIndex);
    }
}


package com.helospark.tactview.core.timeline.effect.transition.random;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.helospark.lightdi.annotation.Service;
import com.helospark.tactview.core.util.cacheable.Cacheable;

@Service
public class ShuffledNumberService {

    @Cacheable(cacheTimeInMilliseconds = 100000)
    public List<Integer> shuffledNumbers(int number, int seed) {
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < number; ++i) {
            numbers.add(i);
        }
        Collections.shuffle(numbers, new Random(seed));

        return numbers;
    }

}

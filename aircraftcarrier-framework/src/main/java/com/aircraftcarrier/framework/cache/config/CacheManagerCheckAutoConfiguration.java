/*
 * Copyright 2012-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.aircraftcarrier.framework.cache.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Configuration;

/**
 * @author panqingcui
 */
@Configuration
public class CacheManagerCheckAutoConfiguration implements CommandLineRunner {
    private static final Logger logger = LoggerFactory.getLogger(CacheManagerCheckAutoConfiguration.class);
    private final CacheManager cacheManager;

    public CacheManagerCheckAutoConfiguration(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public void run(String... strings) {
        logger.info("Using cache manager: " + this.cacheManager.getClass().getName());
    }
}

package org.monkeynuthead.webfluxsample;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class ItemConfiguration {

    @Bean
    ItemRepository itemRepository() {
        //return new HardCodedItemRepository();
        return new MemDBItemRepository();
    }

}

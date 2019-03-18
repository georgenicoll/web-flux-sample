package org.monkeynuthead.webfluxsample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

final class HardCodedItemRepository implements ItemRepository {

    private static final Logger log = LoggerFactory.getLogger(HardCodedItemRepository.class);

    HardCodedItemRepository() {
        log.info("Constructing...");
    }

    @Override
    public Flux<Item> findAllItems() {
        return Flux.just(
                Item.of("1", "First Item"),
                Item.of("2", "Second Item"),
                Item.of("3", "Third Item"),
                Item.of("4", "Fourth Item")
        );
    }

}

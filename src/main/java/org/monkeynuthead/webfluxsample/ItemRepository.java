package org.monkeynuthead.webfluxsample;

import reactor.core.publisher.Flux;

public interface ItemRepository {

    Flux<Item> findAllItems();

}

package org.monkeynuthead.webfluxsample;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping(path = "/items")
public class ItemController {

    private final ItemRepository itemRepository;

    public ItemController(final ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @GetMapping(path = "/all", produces = "application/stream+json")
    Flux<Item> allItems() {
        return itemRepository.findAllItems();
    }

}

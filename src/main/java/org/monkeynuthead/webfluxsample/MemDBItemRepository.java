package org.monkeynuthead.webfluxsample;

import com.github.davidmoten.rx.jdbc.Database;
import org.jooby.Jooby;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import rx.Observable;
import rx.RxReactiveStreams;

class MemDBItemRepository implements ItemRepository {

    private static final Logger log = LoggerFactory.getLogger(MemDBItemRepository.class);

    private final Database db;

    MemDBItemRepository() {
        log.info("Constructing...");
        db = Database.from("jdbc:h2:mem:{mem.seed};DB_CLOSE_DELAY=-1", "sa", "");
        createTable();
        insertRow("1", "The First Item");
        insertRow("2", "The Second Item");
        insertRow("3", "The Third Item");
        insertRow("4", "The Fourth Item");
        outputCount();
    }

    @Override
    public Flux<Item> findAllItems() {
        final Observable<Item> itemsObservable = db.select("SELECT id, description FROM items")
                .get(resultSet -> Item.of(resultSet.getString(1), resultSet.getString(2)));
        return Flux.from(RxReactiveStreams.toPublisher(itemsObservable));
    }

    private void createTable() {
        db.update("CREATE TABLE items (id VARCHAR(10) NOT NULL, description VARCHAR(50) NOT NULL)").execute();
    }

    private void insertRow(final String id, final String description) {
        db.update("INSERT INTO items (id, description) VALUES (:id, :description)")
                .parameter("id", id)
                .parameter("description", description)
                .execute();
    }

    private void outputCount() {
        final Integer count = db.select("SELECT count(*) FROM items")
                .getAs(Integer.class)
                .toBlocking().first();
        log.info("There are now {} record(s) in the items table", count);
    }

}

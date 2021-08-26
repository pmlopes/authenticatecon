const Future = Java.type('io.vertx.core.Future');

/**
 * This is a dummy database, just for demo purposes.
 * In a real world scenario you should be using something like:
 *
 * <ul>
 *   <li>Postgres</li>
 *   <li>MySQL</li>
 *   <li>Mongo</li>
 *   <li>Redis</li>
 *   <li>...</li>
 * </ul>
 */
const database = [];

export function fetcher(query) {

    return Future.succeededFuture(
        database.filter(entry => {
            if (query.getUserName()) {
                return query.getUserName() === entry.getUserName();
            }
            if (query.getCredID()) {
                return query.getCredID() === entry.getCredID();
            }
            // This is a bad query! both username and credID are null
            return false;
        })
    );
}

export function updater(authenticator) {

    print(authenticator)
    let updated = 0;

    database
        .forEach(entry => {
            if (authenticator.getCredID() === entry.getCredID()) {
                // update existing counter
                entry.setCounter(authenticator.getCounter());
                updated++;
            }
        });

    print(updated)

    if (updated > 0) {
        return Future.succeededFuture();
    } else {
        database.push(authenticator);
        return Future.succeededFuture();
    }
}
